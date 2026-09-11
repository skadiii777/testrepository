package com.enterprise.module.biz.service.purchase;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.PurchasePageReqVO;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.PurchaseSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.enterprise.module.biz.service.stock.StockService;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 采购单 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class PurchaseServiceImpl implements PurchaseService {
    @Resource private com.enterprise.module.biz.service.support.BizReferenceService references;

    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private StockService stockService;



    @Override
    public Long createPurchase(PurchaseSaveReqVO createReqVO) {
        PurchaseDO purchase = BeanUtils.toBean(createReqVO, PurchaseDO.class);
        var product = references.product(purchase.getProductId(), purchase.getProductName());
        var warehouse = references.warehouse(purchase.getWarehouseId(), purchase.getWarehouse());
        purchase.setProductId(product.getId()); purchase.setProductName(product.getProductName());
        purchase.setWarehouseId(warehouse.getId()); purchase.setWarehouse(warehouse.getName());
        purchase.setStatus("0"); // 强制草稿，库存联动在"完成"流转时发生
        purchase.setTotalAmount(java.math.BigDecimal.valueOf(purchase.getQuantity()).multiply(purchase.getPrice())); // 总金额服务端强算，不信前端
        purchaseMapper.insert(purchase);
        return purchase.getId();
    }

    @Override
    public void transitionPurchase(Long id, String action) {
        PurchaseDO purchase = validatePurchaseExists(id);
        String from = purchase.getStatus();
        String to;
        if ("confirm".equals(action)) {
            if (!"0".equals(from)) {
                throw exception(ORDER_STATUS_TRANSITION_INVALID);
            }
            to = "1";
        } else if ("void".equals(action)) {
            if ("2".equals(from) || "3".equals(from)) {
                throw exception(ORDER_STATUS_TRANSITION_INVALID);
            }
            to = "3";
        } else {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        // CAS 状态流转：并发下仅一个请求成功，防止重复流转
        if (purchaseMapper.updateStatusByCas(id, from, to) == 0) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void completePurchase(Long id) {
        PurchaseDO purchase = purchaseMapper.selectForUpdate(id);
        if (purchase == null) throw exception(PURCHASE_NOT_EXISTS);
        if (!"1".equals(purchase.getStatus())) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        // CAS 抢占状态：并发重复完成只有一个请求能改到，随后才动库存（失败回滚）
        if (purchaseMapper.updateStatusByCas(id, "1", "2") == 0) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        stockService.changeStock(purchase.getProductId(), purchase.getWarehouseId(), purchase.getQuantity(),
                "purchase", purchase.getPurchaseCode());
        PurchaseDO update = new PurchaseDO();
        update.setId(id);
        update.setStatus("2");
        purchaseMapper.updateById(update);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updatePurchase(PurchaseSaveReqVO updateReqVO) {
        PurchaseDO exist = purchaseMapper.selectForUpdate(updateReqVO.getId());
        if (exist == null) throw exception(PURCHASE_NOT_EXISTS);
        // 已完成单据与库存流水绑定，禁止修改；纠错走退货/红冲
        if ("2".equals(exist.getStatus())) {
            throw exception(ORDER_COMPLETED_LOCKED);
        }
        PurchaseDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseDO.class);
        var product = references.product(updateReqVO.getProductId() != null ? updateReqVO.getProductId() : exist.getProductId(), updateReqVO.getProductName());
        var warehouse = references.warehouse(updateReqVO.getWarehouseId() != null ? updateReqVO.getWarehouseId() : exist.getWarehouseId(), updateReqVO.getWarehouse());
        updateObj.setProductId(product.getId()); updateObj.setProductName(product.getProductName());
        updateObj.setWarehouseId(warehouse.getId()); updateObj.setWarehouse(warehouse.getName());
        updateObj.setStatus(null); // 状态只能通过流转接口变更
        // 总金额服务端强算：数量/单价留空取库内原值，且不信前端传入的 totalAmount
        updateObj.setTotalAmount(null);
        PurchaseDO current = exist;
        if (current != null) {
            Long qty = updateObj.getQuantity() != null ? updateObj.getQuantity() : current.getQuantity();
            java.math.BigDecimal price = updateObj.getPrice() != null ? updateObj.getPrice() : current.getPrice();
            if (qty != null && price != null) {
                updateObj.setTotalAmount(java.math.BigDecimal.valueOf(qty).multiply(price));
                updateObj.setQuantity(qty);
                updateObj.setPrice(price);
            }
        }
        purchaseMapper.updateById(updateObj);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deletePurchase(Long id) {
        PurchaseDO locked = purchaseMapper.selectForUpdate(id);
        if (locked == null) throw exception(PURCHASE_NOT_EXISTS);
        // 已完成单据与库存流水绑定，禁止删除
        if ("2".equals(locked.getStatus())) {
            throw exception(ORDER_COMPLETED_LOCKED);
        }
        purchaseMapper.deleteById(id);
    }

    private PurchaseDO validatePurchaseExists(Long id) {
        PurchaseDO purchase = purchaseMapper.selectById(id);
        if (purchase == null) {
            throw exception(PURCHASE_NOT_EXISTS);
        }
        return purchase;
    }

    @Override
    public PurchaseDO getPurchase(Long id) {
        return purchaseMapper.selectById(id);
    }

    @Override
    public PageResult<PurchaseDO> getPurchasePage(PurchasePageReqVO pageReqVO) {
        return purchaseMapper.selectPage(pageReqVO);
    }
}