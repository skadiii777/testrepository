package com.enterprise.module.biz.service.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationMoveDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationStockDO;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.dal.mysql.stock.StockMapper;
import com.enterprise.module.biz.dal.mysql.wms.WmsLocationMapper;
import com.enterprise.module.biz.dal.mysql.wms.WmsLocationMoveMapper;
import com.enterprise.module.biz.dal.mysql.wms.WmsLocationStockMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.module.system.api.user.AdminUserApi;
import com.enterprise.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * WMS 库位库存：上架/下架/移库只动库位库存（主库存的分配视图），
 * 未分配量 = 仓库库存 - 库位合计；上架前校验未分配充足（产品行锁防并发超分）。
 */
@Slf4j
@Service
@Validated
public class WmsStockServiceImpl implements WmsStockService {

    @Resource
    private WmsLocationMapper locationMapper;
    @Resource
    private WmsLocationStockMapper locationStockMapper;
    @Resource
    private WmsLocationMoveMapper locationMoveMapper;
    @Resource
    private StockMapper stockMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private WmsTaskService taskService;

    @Override
    public PageResult<WmsLocationStockDO> getStockPage(WmsStockPageReqVO pageReqVO) {
        return locationStockMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsUnassignedRespVO> getUnassigned(Long warehouseId) {
        List<StockDO> stocks = warehouseId != null
                ? stockMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockDO>()
                        .eq(StockDO::getWarehouseId, warehouseId))
                : stockMapper.selectList();
        List<WmsLocationStockDO> allocs = warehouseId != null
                ? locationStockMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WmsLocationStockDO>()
                        .eq(WmsLocationStockDO::getWarehouseId, warehouseId))
                : locationStockMapper.selectList();
        Map<String, Long> allocated = new LinkedHashMap<>();
        for (WmsLocationStockDO a : allocs) {
            allocated.merge(a.getWarehouseId() + ":" + a.getProductId(), a.getQuantity(), Long::sum);
        }
        Map<String, WmsUnassignedRespVO> result = new LinkedHashMap<>();
        for (StockDO s : stocks) {
            WmsUnassignedRespVO vo = new WmsUnassignedRespVO();
            vo.setWarehouseId(s.getWarehouseId());
            vo.setWarehouseName(s.getWarehouse());
            vo.setProductId(s.getProductId());
            vo.setProductName(s.getProductName());
            vo.setMainQuantity(s.getQuantity() == null ? 0 : s.getQuantity());
            vo.setAllocated(allocated.getOrDefault(s.getWarehouseId() + ":" + s.getProductId(), 0L));
            vo.setUnassigned(vo.getMainQuantity() - vo.getAllocated());
            result.put(s.getWarehouseId() + ":" + s.getProductId(), vo);
        }
        return new ArrayList<>(result.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void putaway(WmsOperateReqVO reqVO) {
        validateQuantity(reqVO.getQuantity());
        WmsLocationDO location = validateEnabledLocation(reqVO.getLocationId());
        // 产品行锁串行化同产品的上架/出库，防未分配超分
        productMapper.selectForUpdate(reqVO.getProductId());
        Long allocated = locationStockMapper.sumQuantityByWarehouseAndProduct(location.getWarehouseId(), reqVO.getProductId());
        StockDO main = stockMapper.selectByProductAndWarehouse(reqVO.getProductId(), location.getWarehouseId());
        long mainQty = main != null && main.getQuantity() != null ? main.getQuantity() : 0;
        if (main == null) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_NO_STOCK);
        }
        if (mainQty - allocated < reqVO.getQuantity()) {
            throw exception(ErrorCodeConstants.WMS_UNASSIGNED_NOT_ENOUGH);
        }
        var product = productMapper.selectById(reqVO.getProductId());
        adjustLocationStock(location, reqVO.getProductId(), product.getProductName(), reqVO.getQuantity());
        insertMove("putaway", location.getWarehouseId(), reqVO.getProductId(), product.getProductName(),
                reqVO.getQuantity(), null, null, location.getId(), location.getCode(), reqVO.getRemark());
        // WMS W2：消耗上架任务（FIFO，部分上架支持）
        taskService.consumePutaway(location.getWarehouseId(), reqVO.getProductId(), reqVO.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(WmsOperateReqVO reqVO) {
        validateQuantity(reqVO.getQuantity());
        WmsLocationDO from = validateEnabledLocation(reqVO.getFromLocationId());
        var product = productMapper.selectForUpdate(reqVO.getProductId());
        deductLocationStock(from, reqVO.getProductId(), reqVO.getQuantity());
        insertMove("remove", from.getWarehouseId(), reqVO.getProductId(), product.getProductName(),
                reqVO.getQuantity(), from.getId(), from.getCode(), null, null, reqVO.getRemark());
        // WMS W2：消耗拣货任务（FIFO，部分拣货支持）
        taskService.consumePick(from.getWarehouseId(), reqVO.getProductId(), reqVO.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(WmsOperateReqVO reqVO) {
        validateQuantity(reqVO.getQuantity());
        WmsLocationDO from = validateEnabledLocation(reqVO.getFromLocationId());
        WmsLocationDO to = validateEnabledLocation(reqVO.getLocationId());
        if (!from.getWarehouseId().equals(to.getWarehouseId())) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_WAREHOUSE_MISMATCH);
        }
        var product = productMapper.selectForUpdate(reqVO.getProductId());
        deductLocationStock(from, reqVO.getProductId(), reqVO.getQuantity());
        adjustLocationStock(to, reqVO.getProductId(), product.getProductName(), reqVO.getQuantity());
        insertMove("move", from.getWarehouseId(), reqVO.getProductId(), product.getProductName(),
                reqVO.getQuantity(), from.getId(), from.getCode(), to.getId(), to.getCode(), reqVO.getRemark());
    }

    @Override
    public PageResult<WmsMoveRespVO> getMovePage(WmsMovePageReqVO pageReqVO) {
        PageResult<WmsLocationMoveDO> page = locationMoveMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(page, WmsMoveRespVO.class);
    }

    private void validateQuantity(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw exception(ErrorCodeConstants.WMS_QUANTITY_INVALID);
        }
    }

    private WmsLocationDO validateEnabledLocation(Long id) {
        WmsLocationDO location = locationMapper.selectById(id);
        if (location == null) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_NOT_EXISTS);
        }
        if (location.getStatus() != 0) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_DISABLED);
        }
        return location;
    }

    /** 库位库存增加（无行则建行） */
    private void adjustLocationStock(WmsLocationDO location, Long productId, String productName, Long delta) {
        WmsLocationStockDO stock = locationStockMapper.selectByLocationAndProduct(location.getId(), productId);
        if (stock == null) {
            locationStockMapper.insert(WmsLocationStockDO.builder()
                    .warehouseId(location.getWarehouseId()).warehouseName(location.getWarehouseName())
                    .locationId(location.getId()).locationCode(location.getCode())
                    .productId(productId).productName(productName)
                    .quantity(delta).build());
            return;
        }
        if (locationStockMapper.adjustQuantity(stock.getId(), delta) == 0) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_STOCK_NOT_ENOUGH);
        }
    }

    /** 库位库存扣减（不足拦截） */
    private void deductLocationStock(WmsLocationDO location, Long productId, Long quantity) {
        WmsLocationStockDO stock = locationStockMapper.selectByLocationAndProduct(location.getId(), productId);
        if (stock == null || locationStockMapper.adjustQuantity(stock.getId(), -quantity) == 0) {
            throw exception(ErrorCodeConstants.WMS_LOCATION_STOCK_NOT_ENOUGH);
        }
    }

    private void insertMove(String moveType, Long warehouseId, Long productId, String productName,
                            Long quantity, Long fromId, String fromCode, Long toId, String toCode, String remark) {
        String operator = null;
        try {
            AdminUserRespDTO user = adminUserApi.getUser(getLoginUserId());
            operator = user != null ? user.getNickname() : null;
        } catch (Exception e) {
            log.warn("[insertMove] 获取操作人昵称失败", e);
        }
        locationMoveMapper.insert(WmsLocationMoveDO.builder()
                .moveType(moveType).warehouseId(warehouseId)
                .productId(productId).productName(productName).quantity(quantity)
                .fromLocationId(fromId).fromLocationCode(fromCode)
                .toLocationId(toId).toLocationCode(toCode)
                .operatorName(operator).remark(remark).build());
    }
}
