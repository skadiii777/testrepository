package com.enterprise.module.biz.job;

import com.enterprise.framework.quartz.core.handler.JobHandler;
import com.enterprise.module.biz.service.digest.BizDigestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 经营周报摘要 Job
 *
 * 每周一 09:00（infra_job 配置）聚合上周数据并推送站内信给管理员。
 * handler_name 对应 Bean 名：bizWeeklyDigestJob
 *
 * @author 企业管理平台
 */
@Component
public class BizWeeklyDigestJob implements JobHandler {

    @Resource
    private BizDigestService digestService;

    @Override
    public String execute(String param) throws Exception {
        return digestService.sendWeeklyDigest();
    }
}
