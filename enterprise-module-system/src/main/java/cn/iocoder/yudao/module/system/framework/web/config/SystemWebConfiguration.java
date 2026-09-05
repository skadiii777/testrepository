package com.enterprise.module.system.framework.web.config;

import com.enterprise.framework.swagger.config.YudaoSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的 web 组件的 Configuration
 *
 * @author 企业管理平台源码
 */
@Configuration(proxyBeanMethods = false)
public class SystemWebConfiguration {

    /**
     * system 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi systemGroupedOpenApi() {
        return YudaoSwaggerAutoConfiguration.buildGroupedOpenApi("system");
    }

}
