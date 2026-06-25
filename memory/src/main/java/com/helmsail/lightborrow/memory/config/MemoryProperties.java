package com.helmsail.lightborrow.memory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Memory 模块配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lightborrow.memory")
public class MemoryProperties {

    /** 历史消息最大条数 */
    private int maxHistory = 20;

    /** 会话超时时间（分钟） */
    private int sessionTimeoutMinutes = 30;
}
