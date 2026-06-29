package com.helmsail.lightborrow.memory.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Memory 模块配置。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "lightborrow.memory")
public class MemoryProperties {

    /** 历史消息最大条数 */
    @Min(value = 1, message = "历史消息条数必须 >= 1")
    private int maxHistory = 20;
}
