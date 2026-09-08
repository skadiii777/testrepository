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

    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private StockService stockService;



    @Override
    public Long createPurchase(PurchaseSaveReqVO createReqVO) {
        PurchaseDO purchase = BeanUtils.toBean(createReqVO, PurchaseDO.class);
        purchase.setStatus("0"); // 强制草稿，库存联动在"完成"流转时发生
        if (purchase.getTotalAmount() == null) { // 总额 = 数量 × 单价
            purchase.setTotalAmount(java.math.BigDecimal.valueOf(purchase.getQuantity()).multiply(purchase.getPrice()));
        }
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
        PurchaseDO update = new PurchaseDO();
        update.setId(id);
        update.setStatus(to);
        purchaseMapper.updateById(update);
    }

    @Override
    public void completePurchase(Long id) {
        PurchaseDO purchase = validatePurchaseExists(id);
        if (!"1".equals(purchase.getStatus())) {
            throw exception(ORDER_STATUS_TRANSITION_INVALID);
        }
        stockService.changeStock(purchase.getProductName(), "默认仓库", purchase.getQuantity(),
                "purchase", purchase.getPurchaseCode());
        PurchaseDO update = new PurchaseDO();
        update.setId(id);
        update.setStatus("2");
        purchaseMapper.updateById(update);
    }
    @Override
    public void updatePurchase(PurchaseSaveReqVO updateReqVO) {
        validatePurchaseExists(updateReqVO.getId());
        PurchaseDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseDO.class);
        updateObj.setStatus(null); // 状态只能通过流转接口变更
        purchaseMapper.updateById(updateObj);
    }


    @Override
    public void deletePurchase(Long id) {
        validatePurchaseExists(id);
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