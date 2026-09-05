package com.enterprise.module.infra.api.logger;

import com.enterprise.framework.common.biz.infra.logger.ApiAccessLogCommonApi;
import com.enterprise.framework.common.biz.infra.logger.dto.ApiAccessLogCreateReqDTO;
import com.enterprise.module.infra.service.logger.ApiAccessLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * API 访问日志的 API 实现类
 *
 * @author 企业管理平台源码
 */
@Service
@Validated
public class ApiAccessLogApiImpl implements ApiAccessLogCommonApi {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO) {
        apiAccessLogService.createApiAccessLog(createDTO);
    }

}
