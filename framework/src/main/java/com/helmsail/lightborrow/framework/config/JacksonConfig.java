package com.helmsail.lightborrow.framework.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.framework.util.SpringContextHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Jackson 全局配置。使用 Jackson2ObjectMapperBuilderCustomizer 允许 {@code spring.jackson.*} 叠加。
 * 将 Spring 管理的 ObjectMapper 注入 {@link JsonUtil}。
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer globalJacksonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
    }

    @Bean
    public InitializingBean jsonUtilObjectMapperSyncer(ObjectMapper objectMapper) {
        return () -> JsonUtil.setObjectMapper(objectMapper);
    }

    /**
     * Spring Context 持有者。注册为 @Bean 确保通过 auto-configuration 加载。
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
}
