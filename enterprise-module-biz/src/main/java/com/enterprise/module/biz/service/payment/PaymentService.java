package com.enterprise.module.biz.service.payment;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentPageReqVO;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;

/**
 * 收付款流水 Service 接口
 *
 * @author 企业管理平台
 */
public interface PaymentService {

    /**
     * 登记收付款（校验单据状态与累计金额，超出单据总额将拒绝）
     *
     * @param createReqVO 收付款信息
     * @return 流水编号
     */
    Long createPayment(PaymentSaveReqVO createReqVO);

    /**
     * 删除收付款流水（仅限管理员权限）
     *
     * @param id 流水编号
     */
    void deletePayment(Long id);

    /**
     * 获得收付款流水分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果
     */
    PageResult<PaymentDO> getPaymentPage(PaymentPageReqVO pageReqVO);

    /**
     * 汇总指定单据的已收/已付金额
     *
     * @param bizType 单据类型（1=销售单 2=采购单）
     * @param orderId 单据编号
     * @return 累计金额
     */
    java.math.BigDecimal getPaidSumByOrder(String bizType, Long orderId);

}
