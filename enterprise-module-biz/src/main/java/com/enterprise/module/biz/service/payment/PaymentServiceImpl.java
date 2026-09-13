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
import com.enterprise.module.biz.service.fms.FmsVoucherService;
import com.enterprise.module.biz.service.support.BizDocumentNo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@Validated
public class PaymentServiceImpl implements PaymentService {
    /** 自动凭证用的标准科目编码（对应 fms_voucher.sql 种子，缺失则降级不生成） */
    private static final String ACC_CASH = "1001";
    private static final String ACC_BANK = "1002";
    private static final String ACC_PAYABLE = "2202";
    private static final String ACC_REVENUE = "6001";

    @Resource private PaymentMapper paymentMapper;
    @Resource private SalesMapper salesMapper;
    @Resource private PurchaseMapper purchaseMapper;
    @Resource private ContractMapper contractMapper;
    @Resource private FmsVoucherService fmsVoucherService;

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
        createAutoVoucher(payment);
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
        createAutoVoucher(reversal);
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
        // 按 合同×收付方式 汇总净余额，红冲时沿用对应货币资金科目（现金收款退现金、银行收款退银行）
        Map<Long, Map<String, BigDecimal>> balances = new LinkedHashMap<>();
        for (PaymentDO p : ledger) {
            String method = p.getPaymentMethod() == null ? "2" : p.getPaymentMethod();
            balances.computeIfAbsent(p.getContractId(), k -> new LinkedHashMap<>())
                    .merge(method, p.getAmount(), BigDecimal::add);
        }
        for (var byContract : balances.entrySet()) {
            for (var byMethod : byContract.getValue().entrySet()) {
                BigDecimal refund = remaining.min(byMethod.getValue());
                if (refund.signum() <= 0) continue;
                PaymentDO flash = new PaymentDO();
                flash.setPaymentNo(BizDocumentNo.next("HK"));
                flash.setPaymentType(type);
                flash.setBizType(type);
                flash.setOrderId(ret.getOrderId());
                flash.setOrderCode(ret.getOrderCode());
                flash.setPartyName(ret.getPartyName());
                flash.setContractId(byContract.getKey());
                flash.setAmount(refund.negate());
                flash.setPaymentDate(LocalDate.now().toString());
                flash.setPaymentMethod(byMethod.getKey());
                flash.setSourceReturnId(ret.getId());
                flash.setRequestId("return:" + ret.getId() + ":" + byContract.getKey() + ":" + byMethod.getKey());
                flash.setRemark("退货红冲：" + ret.getReturnNo());
                paymentMapper.insert(flash);
                createAutoVoucher(flash);
                remaining = remaining.subtract(refund);
                if (remaining.signum() <= 0) return;
            }
        }
    }
    /**
     * 收付款流水自动生成已记账凭证（与收付款同事务，流水落库即入账）。
     * 方向：收款=借货币资金/贷主营业务收入；付款=借应付账款/贷货币资金（赊购口径，
     * 入库凭证借库存/贷应付，付款冲应付）；负数流水（冲销/退货红冲）取反向分录。
     * 幂等：同一流水 id 只生成一张；标准科目被删时降级跳过并告警，不阻塞资金主流程。
     */
    private void createAutoVoucher(PaymentDO p) {
        BigDecimal amount = p.getAmount() == null ? BigDecimal.ZERO : p.getAmount().abs();
        if (amount.signum() <= 0) return;
        boolean income = "1".equals(p.getPaymentType());
        boolean negative = p.getAmount().signum() < 0;
        String moneyCode = "1".equals(p.getPaymentMethod()) ? ACC_CASH : ACC_BANK;
        String oppositeCode = income ? ACC_REVENUE : ACC_PAYABLE;
        // 正向收款/负向付款：借货币资金；正向付款/负向收款：借对方科目
        String debitCode = (income != negative) ? moneyCode : oppositeCode;
        String creditCode = (income != negative) ? oppositeCode : moneyCode;
        String action = negative ? (income ? "收款冲销" : "付款冲销") : (income ? "收款" : "付款");
        String summary = action + " " + p.getPaymentNo() + (p.getPartyName() != null ? "｜" + p.getPartyName() : "");
        fmsVoucherService.createSimplePosted("payment", p.getId(),
                LocalDate.parse(p.getPaymentDate()), summary, debitCode, creditCode, amount);
    }

    private BigDecimal sum(List<PaymentDO> rows) {
        return rows.stream().map(PaymentDO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override public PageResult<PaymentDO> getPaymentPage(PaymentPageReqVO req) { return paymentMapper.selectPage(req); }
    @Override public BigDecimal getPaidSumByOrder(String type, Long id) { return paymentMapper.selectPaidSumByOrder(type, id); }
}
