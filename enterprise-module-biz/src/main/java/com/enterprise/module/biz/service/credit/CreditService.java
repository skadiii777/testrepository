package com.enterprise.module.biz.service.credit;

import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 客户信用额度：应收 = 已完成销售单总额 − 净收款（含红字冲销）；
 * 额度 0 = 不限额。按客户名匹配（与现有单据数据模型一致）。
 */
@Service
public class CreditService {

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PaymentMapper paymentMapper;

    /** 当前应收（确认即占用：已确认+已完成销售单 − 净收款） */
    public BigDecimal receivable(String customerName) {
        BigDecimal billed = salesMapper.selectList(new LambdaQueryWrapperX<SalesDO>()
                        .eq(SalesDO::getCustomerName, customerName)
                        .in(SalesDO::getStatus, "1", "2")).stream()
                .map(s -> s.getTotalAmount() == null ? BigDecimal.ZERO : s.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = paymentMapper.selectList(new LambdaQueryWrapperX<PaymentDO>()
                        .eq(PaymentDO::getPartyName, customerName)
                        .eq(PaymentDO::getBizType, "1")).stream()
                .map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return billed.subtract(paid).max(BigDecimal.ZERO);
    }

    /** 客户信用视图：额度、应收、剩余可用（额度 0 = 不限额，remaining=null） */
    public CreditView view(String customerName) {
        CustomerDO customer = customerMapper.selectByName(customerName);
        BigDecimal limit = customer != null && customer.getCreditLimit() != null
                ? customer.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal receivable = receivable(customerName);
        return new CreditView(limit, receivable,
                limit.signum() > 0 ? limit.subtract(receivable) : null);
    }

    /** 确认销售单前的额度校验（不限额直接放行） */
    public void checkCredit(String customerName, BigDecimal newAmount) {
        if (newAmount == null || newAmount.signum() <= 0) return;
        CustomerDO customer = customerMapper.selectByName(customerName);
        if (customer == null || customer.getCreditLimit() == null
                || customer.getCreditLimit().signum() <= 0) return;
        CreditView view = view(customerName);
        if (view.remaining.compareTo(newAmount) < 0) {
            throw exception(ErrorCodeConstants.CREDIT_LIMIT_EXCEEDED,
                    view.receivable, view.limit, view.remaining);
        }
    }

    public record CreditView(BigDecimal limit, BigDecimal receivable, BigDecimal remaining) {}
}
