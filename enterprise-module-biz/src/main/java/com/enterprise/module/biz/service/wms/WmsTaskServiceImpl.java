package com.enterprise.module.biz.service.wms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsTaskPageReqVO;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.WmsTaskRespVO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsPickTaskDO;
import com.enterprise.module.biz.dal.dataobject.wms.WmsPutawayTaskDO;
import com.enterprise.module.biz.dal.mysql.wms.WmsPickTaskMapper;
import com.enterprise.module.biz.dal.mysql.wms.WmsPutawayTaskMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
public class WmsTaskServiceImpl implements WmsTaskService {

    @Resource
    private WmsPutawayTaskMapper putawayTaskMapper;
    @Resource
    private WmsPickTaskMapper pickTaskMapper;

    @Override
    public PageResult<WmsTaskRespVO> getTaskPage(String type, WmsTaskPageReqVO reqVO) {
        if ("pick".equals(type)) {
            PageResult<WmsPickTaskDO> page = pickTaskMapper.selectPage(reqVO, new LambdaQueryWrapperX<WmsPickTaskDO>()
                    .eqIfPresent(WmsPickTaskDO::getWarehouseId, reqVO.getWarehouseId())
                    .likeIfPresent(WmsPickTaskDO::getProductName, reqVO.getProductName())
                    .eqIfPresent(WmsPickTaskDO::getStatus, reqVO.getStatus())
                    .orderByDesc(WmsPickTaskDO::getId));
            PageResult<WmsTaskRespVO> result = BeanUtils.toBean(page, WmsTaskRespVO.class);
            for (int i = 0; i < result.getList().size(); i++) {
                WmsTaskRespVO vo = result.getList().get(i);
                WmsPickTaskDO d = page.getList().get(i);
                vo.setType("pick");
                vo.setSourceCode(d.getSalesCode());
                vo.setDoneQuantity(d.getPickedQuantity());
            }
            return result;
        }
        PageResult<WmsPutawayTaskDO> page = putawayTaskMapper.selectPage(reqVO, new LambdaQueryWrapperX<WmsPutawayTaskDO>()
                .eqIfPresent(WmsPutawayTaskDO::getWarehouseId, reqVO.getWarehouseId())
                .likeIfPresent(WmsPutawayTaskDO::getProductName, reqVO.getProductName())
                .eqIfPresent(WmsPutawayTaskDO::getStatus, reqVO.getStatus())
                .orderByDesc(WmsPutawayTaskDO::getId));
        PageResult<WmsTaskRespVO> result = BeanUtils.toBean(page, WmsTaskRespVO.class);
        for (int i = 0; i < result.getList().size(); i++) {
            WmsTaskRespVO vo = result.getList().get(i);
            WmsPutawayTaskDO d = page.getList().get(i);
            vo.setType("putaway");
            vo.setSourceCode(d.getPurchaseCode());
            vo.setDoneQuantity(d.getPutawayQuantity());
        }
        return result;
    }

    @Override
    public void createPutawayTask(PurchaseDO purchase) {
        if (putawayTaskMapper.selectByPurchase(purchase.getId()) != null) {
            return;
        }
        putawayTaskMapper.insert(WmsPutawayTaskDO.builder()
                .purchaseId(purchase.getId()).purchaseCode(purchase.getPurchaseCode())
                .productId(purchase.getProductId()).productName(purchase.getProductName())
                .warehouseId(purchase.getWarehouseId()).warehouseName(purchase.getWarehouse())
                .quantity(purchase.getQuantity()).putawayQuantity(0L).status(0).build());
    }

    @Override
    public void createPickTask(SalesDO sales) {
        if (pickTaskMapper.selectBySales(sales.getId()) != null) {
            return;
        }
        pickTaskMapper.insert(WmsPickTaskDO.builder()
                .salesId(sales.getId()).salesCode(sales.getSalesCode())
                .productId(sales.getProductId()).productName(sales.getProductName())
                .warehouseId(sales.getWarehouseId()).warehouseName(sales.getWarehouse())
                .quantity(sales.getQuantity()).pickedQuantity(0L).status(0).build());
    }

    @Override
    public void consumePutaway(Long warehouseId, Long productId, Long quantity) {
        long remaining = quantity;
        for (WmsPutawayTaskDO t : putawayTaskMapper.selectOpenTasks(warehouseId, productId)) {
            if (remaining <= 0) break;
            long take = Math.min(t.getQuantity() - t.getPutawayQuantity(), remaining);
            if (take <= 0) continue;
            if (putawayTaskMapper.addProgress(t.getId(), take) > 0) {
                putawayTaskMapper.completeIfDone(t.getId());
                remaining -= take;
            }
        }
        logUnmatched("上架", remaining, warehouseId, productId);
    }

    @Override
    public void consumePick(Long warehouseId, Long productId, Long quantity) {
        long remaining = quantity;
        for (WmsPickTaskDO t : pickTaskMapper.selectOpenTasks(warehouseId, productId)) {
            if (remaining <= 0) break;
            long take = Math.min(t.getQuantity() - t.getPickedQuantity(), remaining);
            if (take <= 0) continue;
            if (pickTaskMapper.addProgress(t.getId(), take) > 0) {
                pickTaskMapper.completeIfDone(t.getId());
                remaining -= take;
            }
        }
        logUnmatched("拣货", remaining, warehouseId, productId);
    }

    /** 手动作业不受任务约束：任务缺口仅记日志 */
    private void logUnmatched(String action, long remaining, Long warehouseId, Long productId) {
        if (remaining > 0) {
            log.info("[consume] {}量剩余 {} 未匹配到进行中任务，warehouseId={} productId={}",
                    action, remaining, warehouseId, productId);
        }
    }
}
