package com.enterprise.module.biz.service.purchase;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.PurchasePageReqVO;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.PurchaseSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;

/**
 * 采购单 Service 接口
 *
 * @author 企业管理平台
 */
public interface PurchaseService {

    /**
     * 创建采购单（强制草稿状态）
     */
    Long createPurchase(PurchaseSaveReqVO createReqVO);

    /**
     * 状态流转：0草稿→1已确认 / 0,1→3已作废
     */
    void transitionPurchase(Long id, String action);

    /**
     * 完成采购单（1已确认→2已完成，自动入库）
     */
    void completePurchase(Long id);

    /**
     * 更新采购单
     */
    void updatePurchase(PurchaseSaveReqVO updateReqVO);

    /**
     * 删除采购单
     */
    void deletePurchase(Long id);

    /**
     * 获得采购单
     */
    PurchaseDO getPurchase(Long id);

    /**
     * 获得采购单分页
     */
    PageResult<PurchaseDO> getPurchasePage(PurchasePageReqVO pageReqVO);

}