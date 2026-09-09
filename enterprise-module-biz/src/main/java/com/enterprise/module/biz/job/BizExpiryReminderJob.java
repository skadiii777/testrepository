package com.enterprise.module.biz.job;

import com.enterprise.framework.quartz.core.handler.JobHandler;
import com.enterprise.module.biz.service.expiry.BizExpiryReminderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 到期提醒 Job
 *
 * 每日 09:00（infra_job 配置）扫描合同到期与商机超期并推送站内信给管理员。
 * handler_name 对应 Bean 名：bizExpiryReminderJob
 *
 * @author 企业管理平台
 */
@Component
public class BizExpiryReminderJob implements JobHandler {

    @Resource
    private BizExpiryReminderService expiryReminderService;

    @Override
    public String execute(String param) throws Exception {
        return expiryReminderService.runReminder();
    }

}
