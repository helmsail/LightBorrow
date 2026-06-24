package com.helmsail.lightborrow.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightborrow.id")
public class IdProperties {

    /** 机器 ID (0 - 1023)，多机部署时每台配置不同值 */
    private long workerId = 1;
}
