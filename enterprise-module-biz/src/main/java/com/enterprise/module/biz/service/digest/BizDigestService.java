package com.enterprise.module.biz.service.digest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.dal.mysql.correction.AttendanceCorrectionMapper;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.biz.dal.mysql.expense.ExpenseMapper;
import com.enterprise.module.biz.dal.mysql.leave.LeaveMapper;
import com.enterprise.module.biz.dal.mysql.purchase.PurchaseMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.system.api.notify.NotifyMessageSendApi;
import com.enterprise.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 经营周报摘要 Service：聚合上周数据并通过站内信推送
 *
 * @author 企业管理平台
 */
@Service
@Validated
@Slf4j
public class BizDigestService {

    /** 发送对象：管理员（userId=1） */
    private static final Long ADMIN_USER_ID = 1L;
    /** 站内信模板编号 */
    private static final String TEMPLATE_CODE = "biz_weekly_digest";

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private ExpenseMapper expenseMapper;
    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    /**
     * 计算并推送上周经营摘要
     *
     * @return 摘要文本
     */
    public String sendWeeklyDigest() {
        LocalDate thisMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        LocalDate lastSunday = thisMonday.minusDays(1);
        String start = lastMonday.toString();
        String end = lastSunday.toString();

        // 新增客户（按创建时间）
        Long customerCount = customerMapper.selectCount(new QueryWrapper<>(
                new com.enterprise.module.biz.dal.dataobject.customer.CustomerDO())
                .between("create_time", lastMonday.atStartOfDay(), lastSunday.atTime(23, 59, 59)));

        // 销售/采购（按业务日期）
        Map<String, Object> sales = salesMapper.selectSumByDate(start).stream()
                .filter(r -> String.valueOf(r.get("date")).compareTo(start) >= 0
                        && String.valueOf(r.get("date")).compareTo(end) <= 0)
                .reduce((a, b) -> {
                    b.put("total", new java.math.BigDecimal(String.valueOf(a.get("total")))
                            .add(new java.math.BigDecimal(String.valueOf(b.get("total")))));
                    return b;
                }).orElse(Map.of("total", java.math.BigDecimal.ZERO));
        java.math.BigDecimal salesAmount = new java.math.BigDecimal(String.valueOf(sales.get("total")));
        Long salesCount = salesMapper.selectCount(new QueryWrapper<>(
                new com.enterprise.module.biz.dal.dataobject.sales.SalesDO())
                .between("sales_date", start, end));

        Map<String, Object> purchase = purchaseMapper.selectSumByDate(start).stream()
                .filter(r -> String.valueOf(r.get("date")).compareTo(start) >= 0
                        && String.valueOf(r.get("date")).compareTo(end) <= 0)
                .reduce((a, b) -> {
                    b.put("total", new java.math.BigDecimal(String.valueOf(a.get("total")))
                            .add(new java.math.BigDecimal(String.valueOf(b.get("total")))));
                    return b;
                }).orElse(Map.of("total", java.math.BigDecimal.ZERO));
        java.math.BigDecimal purchaseAmount = new java.math.BigDecimal(String.valueOf(purchase.get("total")));
        Long purchaseCount = purchaseMapper.selectCount(new QueryWrapper<>(
                new com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO())
                .between("purchase_date", start, end));

        // 待审批（当前时点）
        Long leavePending = leaveMapper.selectCount(new QueryWrapper<LeaveDO>().eq("status", "0"));
        Long expensePending = expenseMapper.selectCount(new QueryWrapper<ExpenseDO>().eq("status", "0"));
        Long correctionPending = correctionMapper.selectCount(
                new QueryWrapper<AttendanceCorrectionDO>().eq("status", "0"));

        Map<String, Object> params = new HashMap<>();
        params.put("customerCount", customerCount);
        params.put("salesCount", salesCount);
        params.put("salesAmount", salesAmount.stripTrailingZeros().toPlainString());
        params.put("purchaseCount", purchaseCount);
        params.put("purchaseAmount", purchaseAmount.stripTrailingZeros().toPlainString());
        params.put("leavePending", leavePending);
        params.put("expensePending", expensePending);
        params.put("correctionPending", correctionPending);

        notifyMessageSendApi.sendSingleMessageToAdmin(
                new NotifySendSingleToUserReqDTO().setUserId(ADMIN_USER_ID)
                        .setTemplateCode(TEMPLATE_CODE).setTemplateParams(params));

        String summary = "周报（" + start + " ~ " + end + "）：新增客户 " + customerCount
                + "，销售 " + salesCount + " 张/" + salesAmount + " 元，采购 " + purchaseCount
                + " 张/" + purchaseAmount + " 元";
        log.info("[sendWeeklyDigest] {}", summary);
        return summary;
    }
}
