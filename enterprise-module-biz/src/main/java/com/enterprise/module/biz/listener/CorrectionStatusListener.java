package com.enterprise.module.biz.listener;

import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.enterprise.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.enterprise.module.biz.service.correction.AttendanceCorrectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 补卡流程的状态监听器：流程结束后（通过/驳回）回写补卡单状态
 *
 * 审批通过时自动回写考勤记录（与本地直批路径一致）。
 *
 * @author 企业管理平台
 */
@Component
public class CorrectionStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    @Lazy
    private AttendanceCorrectionService correctionService;

    @Override
    protected String getProcessDefinitionKey() {
        return "biz_correction";
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        System.out.println("[CorrectionStatusListener] 收到事件: key=" + event.getProcessDefinitionKey()
                + " status=" + event.getStatus() + " businessKey=" + event.getBusinessKey());
        correctionService.updateCorrectionStatusFromBpm(Long.parseLong(event.getBusinessKey()),
                event.getStatus(), event.getBusinessKey());
    }
}
