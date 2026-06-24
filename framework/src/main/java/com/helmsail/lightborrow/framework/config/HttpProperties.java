package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightborrow.http")
public class HttpProperties {

    /** 连接超时（毫秒） */
    private int connectTimeout = HttpConstant.DEFAULT_CONNECT_TIMEOUT;

    /** 读取超时（毫秒） */
    private int readTimeout = HttpConstant.DEFAULT_READ_TIMEOUT;
}
