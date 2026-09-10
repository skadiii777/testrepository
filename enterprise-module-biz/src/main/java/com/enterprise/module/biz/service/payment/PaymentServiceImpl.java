package com.enterprise.module.biz.service.payment;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentPageReqVO;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 收付款流水 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class PaymentServiceImpl implements PaymentService {

    /** 单据状态：已完成 */
    private static final String ORDER_STATUS_COMPLETED = "2";
    /** 单据类型：1=销售单 2=采购单 */
    private static final String BIZ_TYPE_SALES = "1";
    private static final String BIZ_TYPE_PURCHASE = "2";

    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private com.enterprise.module.biz.dal.mysql.contract.ContractMapper contractMapper;

    @Override
    public Long createPayment(PaymentSaveReqVO createReqVO) {
        // 1.1 校验收付类型与单据类型匹配：收款对应销售单，付款对应采购单
        String bizType = createReqVO.getBizType();
        if (!BIZ_TYPE_SALES.equals(bizType) && !BIZ_TYPE_PURCHASE.equals(bizType)) {
            throw exception(PAYMENT_BIZ_TYPE_INVALID);
        }
        boolean isReceipt = BIZ_TYPE_SALES.equals(createReqVO.getPaymentType());
        if (isReceipt != BIZ_TYPE_SALES.equals(bizType)) {
            throw exception(PAYMENT_TYPE_BIZ_MISMATCH);
        }
        // 1.2 关联目标二选一：挂单据（原强校验链）或挂合同（纯合同回款，如收定金）
        BigDecimal totalAmount;
        String orderCode;
        String partyName;
        if (createReqVO.getOrderId() != null) {
            if (BIZ_TYPE_SALES.equals(bizType)) {
                SalesDO sales = salesMapper.selectById(createReqVO.getOrderId());
                if (sales == null) {
                    throw exception(PAYMENT_ORDER_NOT_EXISTS);
                }
                validateOrderCompleted(sales.getStatus());
                totalAmount = resolveTotalAmount(sales.getQuantity(), sales.getPrice(), sales.getTotalAmount());
                orderCode = sales.getSalesCode();
                partyName = sales.getCustomerName();
            } else {
                PurchaseDO purchase = purchaseMapper.selectById(createReqVO.getOrderId());
                if (purchase == null) {
                    throw exception(PAYMENT_ORDER_NOT_EXISTS);
                }
                validateOrderCompleted(purchase.getStatus());
                totalAmount = resolveTotalAmount(purchase.getQuantity(), purchase.getPrice(), purchase.getTotalAmount());
                orderCode = purchase.getPurchaseCode();
                partyName = purchase.getSupplierName();
            }
            // 1.3 校验累计收付金额不超过单据总额（纯合同回款无单据总额约束）
            BigDecimal paidSum = paymentMapper.selectPaidSumByOrder(bizType, createReqVO.getOrderId());
            if (paidSum.add(createReqVO.getAmount()).compareTo(totalAmount) > 0) {
                throw exception(PAYMENT_AMOUNT_EXCEED,
                        paidSum.stripTrailingZeros().toPlainString(),
                        totalAmount.stripTrailingZeros().toPlainString());
            }
        } else if (createReqVO.getContractId() != null) {
            orderCode = null;
            partyName = null;
        } else {
            throw exception(PAYMENT_TARGET_REQUIRED);
        }
        // 1.4 挂合同时校验合同存在；纯合同回款时对方名称取合同客户
        if (createReqVO.getContractId() != null) {
            com.enterprise.module.biz.dal.dataobject.contract.ContractDO contract =
                    contractMapper.selectById(createReqVO.getContractId());
            if (contract == null) {
                throw exception(PAYMENT_CONTRACT_NOT_EXISTS);
            }
            if (partyName == null) {
                partyName = contract.getCustomerName();
                orderCode = contract.getContractCode();
            }
        }

        // 2. 插入流水（只增不删改）
        PaymentDO payment = BeanUtils.toBean(createReqVO, PaymentDO.class);
        payment.setOrderCode(orderCode);
        payment.setPartyName(partyName);
        payment.setPaymentNo(generatePaymentNo(createReqVO.getPaymentType()));
        paymentMapper.insert(payment);
        return payment.getId();
    }

    @Override
    public void deletePayment(Long id) {
        validatePaymentExists(id);
        paymentMapper.deleteById(id);
    }

    @Override
    public PageResult<PaymentDO> getPaymentPage(PaymentPageReqVO pageReqVO) {
        return paymentMapper.selectPage(pageReqVO);
    }

    @Override
    public BigDecimal getPaidSumByOrder(String bizType, Long orderId) {
        return paymentMapper.selectPaidSumByOrder(bizType, orderId);
    }

    private void validateOrderCompleted(String status) {
        if (!ORDER_STATUS_COMPLETED.equals(status)) {
            throw exception(PAYMENT_ORDER_NOT_COMPLETED);
        }
    }

    /**
     * 单据总额：优先取 total_amount 列；历史单据该列可能为空，退回 数量 × 单价
     */
    private BigDecimal resolveTotalAmount(Long quantity, BigDecimal price, BigDecimal totalAmount) {
        if (totalAmount != null) {
            return totalAmount;
        }
        return BigDecimal.valueOf(quantity).multiply(price);
    }

    private void validatePaymentExists(Long id) {
        if (paymentMapper.selectById(id) == null) {
            throw exception(PAYMENT_NOT_EXISTS);
        }
    }

    /**
     * 生成收付单号：SK/FK + yyyyMMddHHmmss（收款=SK，付款=FK）
     */
    private String generatePaymentNo(String paymentType) {
        return ("1".equals(paymentType) ? "SK" : "FK")
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
