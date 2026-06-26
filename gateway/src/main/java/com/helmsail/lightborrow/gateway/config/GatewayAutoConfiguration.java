package com.helmsail.lightborrow.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.gateway.adapter.DingTalkAdapter;
import com.helmsail.lightborrow.gateway.adapter.FeishuAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Gateway 模块自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DingTalkAdapter dingTalkAdapter(GatewayProperties gatewayProperties,
                                           ObjectMapper objectMapper) {
        return new DingTalkAdapter(gatewayProperties, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeishuAdapter feishuAdapter(GatewayProperties gatewayProperties,
                                       ObjectMapper objectMapper) {
        return new FeishuAdapter(gatewayProperties, objectMapper);
    }
}
