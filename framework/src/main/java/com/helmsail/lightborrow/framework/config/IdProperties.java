package com.helmsail.lightborrow.framework.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 雪花算法 ID 生成器配置属性。
 */
@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "lightborrow.id")
public class IdProperties {

    /** 机器 ID (0 - 1023)，多机部署时每台配置不同值 */
    @Min(0)
    @Max(1023)
    private long workerId = 1;
}
