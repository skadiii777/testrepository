package com.enterprise.module.biz.listener;

import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.enterprise.module.biz.service.leave.LeaveService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 请假流程的状态监听器：流程结束后（通过/驳回）回写请假单状态
 *
 * 审批通过时自动扣减假期余额；驳回不扣。
 *
 * @author 企业管理平台
 */
@Component
public class LeaveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    @Lazy
    private LeaveService leaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return "biz_leave";
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        leaveService.updateLeaveStatusFromBpm(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
