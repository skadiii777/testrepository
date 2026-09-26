package com.enterprise.module.biz.service.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.enterprise.module.system.api.notify.NotifyMessageSendApi;
import com.enterprise.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.annotation.Resource;

/**
 * 审批事件站内信联动：提交 → 通知审批人；审批结果 → 通知申请人。
 *
 * 解决的问题：审批积压对审批人不可见、审批结果申请人无从得知（两处模块联动断点）。
 * 通知失败只记日志，绝不影响审批主流程。
 */
@Slf4j
@Service
public class BizApprovalNotifyService {

    /** 审批人：当前生产由管理员统一审批（与 BizDigestService 惯例一致） */
    private static final Long APPROVER_USER_ID = 1L;

    public static final String TEMPLATE_PENDING = "biz_approval_pending";
    public static final String TEMPLATE_RESULT = "biz_approval_result";

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    /** 提交申请时通知审批人。applicantUserId 用于避免自己通知自己。 */
    public void notifyPending(String type, Long applicantUserId, String applicantName, String summary) {
        if (APPROVER_USER_ID.equals(applicantUserId)) {
            return; // 审批人本人提交，无需自通知
        }
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("type", type);
            params.put("applicant", applicantName);
            params.put("summary", summary);
            notifyMessageSendApi.sendSingleMessageToAdmin(
                    new NotifySendSingleToUserReqDTO().setUserId(APPROVER_USER_ID)
                            .setTemplateCode(TEMPLATE_PENDING).setTemplateParams(params));
            log.info("[notifyPending][{} 待办已通知审批人: {} {}]", type, applicantName, summary);
        } catch (Exception e) {
            log.warn("[notifyPending][{} 审批待办通知失败，不影响提交]", type, e);
        }
    }

    /** 审批完成后通知申请人（creator 即申请人用户 id）。 */
    public void notifyResult(String type, Long applicantUserId, boolean approved, String remark) {
        if (applicantUserId == null) {
            return;
        }
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("type", type);
            params.put("result", approved ? "通过" : "驳回");
            params.put("remark", remark == null || remark.isBlank() ? "无" : remark);
            notifyMessageSendApi.sendSingleMessageToAdmin(
                    new NotifySendSingleToUserReqDTO().setUserId(applicantUserId)
                            .setTemplateCode(TEMPLATE_RESULT).setTemplateParams(params));
        } catch (Exception e) {
            log.warn("[notifyResult][{} 结果通知失败，不影响审批]", type, e);
        }
    }
}
