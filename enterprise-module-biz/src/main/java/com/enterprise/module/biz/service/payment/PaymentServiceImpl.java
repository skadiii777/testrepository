package com.enterprise.module.biz.service.payment;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.*;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.dal.dataobject.returnorder.ReturnDO;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.dal.mysql.contract.ContractMapper;
import com.enterprise.module.biz.service.support.BizDocumentNo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

@Service
@Validated
public class PaymentServiceImpl implements PaymentService {
    @Resource private PaymentMapper paymentMapper;
    @Resource private SalesMapper salesMapper;
    @Resource private PurchaseMapper purchaseMapper;
    @Resource private ContractMapper contractMapper;

    /** All money changes lock the same order (or standalone contract) before reading any ledger totals. */
    private PaymentDO lockTarget(String type, Long orderId, Long contractId) {
        if (!Set.of("1", "2").contains(type)) throw exception(PAYMENT_BIZ_TYPE_INVALID);
        PaymentDO target = new PaymentDO();
        target.setBizType(type);
        target.setOrderId(orderId);
        target.setContractId(contractId);
        if (orderId != null) {
            if ("1".equals(type)) {
                var order = salesMapper.selectForUpdate(orderId);
                if (order == null) throw exception(PAYMENT_ORDER_NOT_EXISTS);
                if (!"2".equals(order.getStatus())) throw exception(PAYMENT_ORDER_NOT_COMPLETED);
                target.setAmount(order.getTotalAmount());
                target.setOrderCode(order.getSalesCode());
                target.setPartyName(order.getCustomerName());
            } else {
                var order = purchaseMapper.selectForUpdate(orderId);
                if (order == null) throw exception(PAYMENT_ORDER_NOT_EXISTS);
                if (!"2".equals(order.getStatus())) throw exception(PAYMENT_ORDER_NOT_COMPLETED);
                target.setAmount(order.getTotalAmount());
                target.setOrderCode(order.getPurchaseCode());
                target.setPartyName(order.getSupplierName());
            }
        }
        if (contractId != null) {
            var contract = contractMapper.selectForUpdate(contractId);
            if (contract == null) throw exception(PAYMENT_CONTRACT_NOT_EXISTS);
            if (orderId == null) {
                target.setOrderCode(contract.getContractCode());
                target.setPartyName(contract.getCustomerName());
            }
        }
        if (orderId == null && contractId == null) throw exception(PAYMENT_TARGET_REQUIRED);
        return target;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPayment(PaymentSaveReqVO req) {
        if (!Objects.equals(req.getPaymentType(), req.getBizType())) throw exception(PAYMENT_TYPE_BIZ_MISMATCH);
        if (req.getOrderId() == null && !"1".equals(req.getPaymentType())) throw exception(PAYMENT_TARGET_REQUIRED);
        PaymentDO target = lockTarget(req.getBizType(), req.getOrderId(), req.getContractId());
        if (req.getAmount() == null || req.getAmount().signum() <= 0) throw exception(PAYMENT_AMOUNT_INVALID);
        if (req.getRequestId() != null) {
            PaymentDO prior = paymentMapper.selectByRequestId(req.getRequestId());
            if (prior != null) {
                if (!Objects.equals(prior.getOrderId(), req.getOrderId())
                        || !Objects.equals(prior.getContractId(), req.getContractId())
                        || !Objects.equals(prior.getBizType(), req.getBizType())
                        || prior.getAmount().compareTo(req.getAmount()) != 0
                        || !Objects.equals(prior.getPaymentDate(), req.getPaymentDate())
                        || !Objects.equals(prior.getPaymentMethod(), req.getPaymentMethod())
                        || !Objects.equals(prior.getRemark(), req.getRemark())) throw exception(PAYMENT_REQUEST_CONFLICT);
                return prior.getId();
            }
        }
        if (req.getOrderId() != null) {
            BigDecimal paid = sum(paymentMapper.selectCurrentByOrder(req.getBizType(), req.getOrderId()));
            if (target.getAmount() == null || paid.add(req.getAmount()).compareTo(target.getAmount()) > 0)
                throw exception(PAYMENT_AMOUNT_EXCEED, paid, target.getAmount());
        }
        PaymentDO payment = BeanUtils.toBean(req, PaymentDO.class);
        payment.setOrderCode(target.getOrderCode());
        payment.setPartyName(target.getPartyName());
        payment.setPaymentNo(BizDocumentNo.next("1".equals(req.getPaymentType()) ? "SK" : "FK"));
        paymentMapper.insert(payment);
        return payment.getId();
    }

    /** Keep the old endpoint explicit: silently deleting or implicitly reversing without a reason is forbidden. */
    @Override public void deletePayment(Long id) { throw exception(PAYMENT_DELETE_FORBIDDEN); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reversePayment(Long id, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 200) throw exception(PAYMENT_REASON_REQUIRED);
        PaymentDO before = paymentMapper.selectById(id);
        if (before == null) throw exception(PAYMENT_NOT_EXISTS);
        lockTarget(before.getBizType(), before.getOrderId(), before.getContractId());
        PaymentDO original = paymentMapper.selectForUpdate(id);
        if (original == null) throw exception(PAYMENT_NOT_EXISTS);
        var existing = paymentMapper.selectByRequestId("reverse:" + id);
        if (existing != null) return existing.getId();
        if (original.getAmount().signum() <= 0 || original.getReversalOfId() != null
                || original.getSourceReturnId() != null) throw exception(PAYMENT_REVERSAL_INVALID);
        var ledger = original.getOrderId() != null
                ? paymentMapper.selectCurrentByOrder(original.getBizType(), original.getOrderId())
                : paymentMapper.selectCurrentByContract(original.getContractId());
        BigDecimal available = ledger.stream().filter(p -> Objects.equals(p.getContractId(), original.getContractId()))
                .map(PaymentDO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(original.getAmount()) < 0) throw exception(PAYMENT_REVERSAL_INVALID);
        PaymentDO reversal = new PaymentDO();
        reversal.setPaymentNo(BizDocumentNo.next("CX"));
        reversal.setPaymentType(original.getPaymentType());
        reversal.setBizType(original.getBizType());
        reversal.setOrderId(original.getOrderId());
        reversal.setOrderCode(original.getOrderCode());
        reversal.setPartyName(original.getPartyName());
        reversal.setContractId(original.getContractId());
        reversal.setAmount(original.getAmount().negate());
        reversal.setPaymentDate(LocalDate.now().toString());
        reversal.setPaymentMethod(original.getPaymentMethod());
        reversal.setReversalOfId(id);
        reversal.setRequestId("reverse:" + id);
        reversal.setRemark(reason.trim());
        paymentMapper.insert(reversal);
        return reversal.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundForReturn(ReturnDO ret) {
        String type = ret.getReturnType();
        lockTarget(type, ret.getOrderId(), null);
        var ledger = paymentMapper.selectCurrentByOrder(type, ret.getOrderId());
        if (ledger.stream().anyMatch(p -> Objects.equals(p.getSourceReturnId(), ret.getId()))) return;
        BigDecimal remaining = ret.getTotalAmount();
        if (remaining == null || remaining.signum() <= 0) return;
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        for (PaymentDO p : ledger) balances.merge(p.getContractId(), p.getAmount(), BigDecimal::add);
        for (var entry : balances.entrySet()) {
            BigDecimal refund = remaining.min(entry.getValue());
            if (refund.signum() <= 0) continue;
            PaymentDO flash = new PaymentDO();
            flash.setPaymentNo(BizDocumentNo.next("HK"));
            flash.setPaymentType(type);
            flash.setBizType(type);
            flash.setOrderId(ret.getOrderId());
            flash.setOrderCode(ret.getOrderCode());
            flash.setPartyName(ret.getPartyName());
            flash.setContractId(entry.getKey());
            flash.setAmount(refund.negate());
            flash.setPaymentDate(LocalDate.now().toString());
            flash.setSourceReturnId(ret.getId());
            flash.setRequestId("return:" + ret.getId() + ":" + entry.getKey());
            flash.setRemark("退货红冲：" + ret.getReturnNo());
            paymentMapper.insert(flash);
            remaining = remaining.subtract(refund);
        }
    }
    private BigDecimal sum(List<PaymentDO> rows) {
        return rows.stream().map(PaymentDO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override public PageResult<PaymentDO> getPaymentPage(PaymentPageReqVO req) { return paymentMapper.selectPage(req); }
    @Override public BigDecimal getPaidSumByOrder(String type, Long id) { return paymentMapper.selectPaidSumByOrder(type, id); }
}
