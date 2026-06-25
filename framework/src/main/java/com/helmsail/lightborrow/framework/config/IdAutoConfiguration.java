package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.util.IdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ID 生成器自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(IdProperties.class)
public class IdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(IdProperties idProperties) {
        return new IdGenerator(idProperties.getWorkerId());
    }
}
