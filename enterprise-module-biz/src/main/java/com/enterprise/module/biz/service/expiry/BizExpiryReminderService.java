package com.enterprise.module.biz.service.expiry;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;
import com.enterprise.module.biz.dal.dataobject.contract.ContractDO;
import com.enterprise.module.biz.dal.mysql.business.BusinessMapper;
import com.enterprise.module.biz.dal.mysql.contract.ContractMapper;
import com.enterprise.module.system.api.notify.NotifyMessageSendApi;
import com.enterprise.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 到期提醒 Service：扫描合同到期与商机超期，通过站内信推送管理员
 *
 * 合同（执行中）到期前 30/7/1 天及当天各提醒一次；
 * 商机超期未成交（预计成交日已过、未赢单/输单）每周一提醒一次。
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class BizExpiryReminderService {

    /** 发送对象：管理员（userId=1） */
    private static final Long ADMIN_USER_ID = 1L;
    /** 站内信模板编号 */
    private static final String TEMPLATE_CODE = "biz_expiry_reminder";
    /** 合同状态：执行中（仅执行中的合同提醒） */
    private static final String CONTRACT_STATUS_ACTIVE = "1";
    /** 商机终局阶段：赢单/输单（终局不提醒） */
    private static final String STAGE_WIN = "5";
    private static final String STAGE_LOSE = "6";
    /** 合同到期提醒阈值（剩余天数） */
    private static final List<Long> CONTRACT_THRESHOLDS = List.of(30L, 7L, 1L, 0L);

    @Resource
    private ContractMapper contractMapper;
    @Resource
    private BusinessMapper businessMapper;
    @Resource
    private com.enterprise.module.biz.dal.mysql.stock.StockMapper stockMapper;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    /**
     * 扫描并推送到期提醒
     *
     * @return 提醒摘要文本
     */
    public String runReminder() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> lines = new ArrayList<>();

        // 1. 合同到期（执行中）
        List<ContractDO> contracts = contractMapper.selectList(new QueryWrapper<>(new ContractDO())
                .eq("status", CONTRACT_STATUS_ACTIVE)
                .isNotNull("end_date")
                .ne("end_date", ""));
        for (ContractDO contract : contracts) {
            LocalDate endDate;
            try {
                endDate = LocalDate.parse(contract.getEndDate(), fmt);
            } catch (Exception e) {
                continue; // 日期格式异常的跳过
            }
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
            if (!CONTRACT_THRESHOLDS.contains(daysLeft)) {
                continue;
            }
            String desc = daysLeft == 0 ? "今日到期" : "将于 " + daysLeft + " 天后到期";
            lines.add(String.format("合同《%s》（%s）%s，金额 %s 元",
                    contract.getContractCode(), contract.getCustomerName(), desc,
                    contract.getAmount() == null ? "0" : contract.getAmount().stripTrailingZeros().toPlainString()));
        }

        // 2. 低库存预警：现存量 ≤ 预警下限的产品，随每日提醒一并推送
        List<com.enterprise.module.biz.dal.dataobject.stock.StockDO> lowStocks =
                stockMapper.selectList(new QueryWrapper<>(new com.enterprise.module.biz.dal.dataobject.stock.StockDO())
                        .apply("quantity <= min_quantity")
                        .gt("min_quantity", 0));
        for (com.enterprise.module.biz.dal.dataobject.stock.StockDO stock : lowStocks) {
            long suggest = stock.getMinQuantity() == null ? 0
                    : Math.max(stock.getMinQuantity() - (stock.getQuantity() == null ? 0 : stock.getQuantity()), 0);
            lines.add(String.format("产品「%s」（%s）库存 %d 件，低于预警下限 %d 件，建议补货 %d 件",
                    stock.getProductName(), stock.getWarehouse(),
                    stock.getQuantity() == null ? 0 : stock.getQuantity(),
                    stock.getMinQuantity() == null ? 0 : stock.getMinQuantity(), suggest));
        }

        // 3. 商机超期未成交（预计成交日已过且未到终局）——仅周一提醒
        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            List<BusinessDO> businesses = businessMapper.selectList(new QueryWrapper<>(new BusinessDO())
                    .lt("expected_date", today.format(fmt))
                    .notIn("stage", STAGE_WIN, STAGE_LOSE));
            for (BusinessDO business : businesses) {
                if (business.getExpectedDate() == null || business.getExpectedDate().isEmpty()) {
                    continue;
                }
                LocalDate expected;
                try {
                    expected = LocalDate.parse(business.getExpectedDate(), fmt);
                } catch (Exception e) {
                    continue;
                }
                long overdue = java.time.temporal.ChronoUnit.DAYS.between(expected, today);
                lines.add(String.format("商机「%s」（%s）已超期 %d 天未成交，当前阶段：%s",
                        business.getName(), business.getCustomerName(), overdue, business.getStage()));
            }
        }

        if (lines.isEmpty()) {
            log.info("[runReminder] 无到期提醒");
            return "到期提醒：今日无待提醒事项";
        }

        String content = String.join("；", lines);
        Map<String, Object> params = new HashMap<>();
        params.put("content", content);
        notifyMessageSendApi.sendSingleMessageToAdmin(
                new NotifySendSingleToUserReqDTO().setUserId(ADMIN_USER_ID)
                        .setTemplateCode(TEMPLATE_CODE).setTemplateParams(params));
        String summary = "到期提醒：" + content;
        log.info("[runReminder] {}", summary);
        return summary;
    }

}
