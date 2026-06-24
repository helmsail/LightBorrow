package com.helmsail.lightborrow.framework.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Jackson 全局配置。通过 {@link Jackson2ObjectMapperBuilderCustomizer} 而非直接声明 ObjectMapper Bean，
 * 允许消费者通过 {@code spring.jackson.*} 叠加自定义配置。
 * 将 Spring 管理的 ObjectMapper 注入 {@link JsonUtil}，使静态工具类与 Spring 配置一致。
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
}
