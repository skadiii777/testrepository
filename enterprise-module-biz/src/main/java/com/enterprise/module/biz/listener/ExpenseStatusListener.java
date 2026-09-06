package com.enterprise.module.biz.listener;

import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.enterprise.module.biz.service.expense.ExpenseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 报销流程的状态监听器：流程结束后（通过/驳回）回写报销单状态
 *
 * BPM 状态映射：2=通过 -> 表 1 已通过；3=驳回 -> 表 2 已驳回
 *
 * @author 企业管理平台
 */
@Component
public class ExpenseStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    @Lazy
    private ExpenseService expenseService;

    @Override
    protected String getProcessDefinitionKey() {
        return "biz_expense";
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        expenseService.updateExpenseStatusFromBpm(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
