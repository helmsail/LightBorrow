package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;

/**
 * HTTP 请求/响应日志拦截器。INFO 输出请求概要，DEBUG 输出完整体（敏感 Header 脱敏），
 * WARN 标记 4xx/5xx 或慢调用，ERROR 标记调用失败。
 */
@Slf4j
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie",
            "x-auth-token", "x-api-key", "x-access-key"
    );

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();

        // === 请求日志 ===
        log.info("[HTTP] >>> {} {}", request.getMethod(), request.getURI());

        if (log.isDebugEnabled()) {
            log.debug("[HTTP] >>> headers={} | body={}",
                    sanitizeHeaders(request), new String(body, StandardCharsets.UTF_8));
        }

        // === 执行 ===
        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException e) {
            log.error("[HTTP] {} {} 调用失败, 耗时={}ms, error={}",
                    request.getMethod(), request.getURI(),
                    System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }

        // === 响应日志 ===
        long duration = System.currentTimeMillis() - start;
        int statusCode = response.getStatusCode().value();

        if (statusCode >= 400 || duration > HttpConstant.SLOW_THRESHOLD_MILLIS) {
            log.warn("[HTTP] {} {} 响应异常, status={}, 耗时={}ms",
                    request.getMethod(), request.getURI(),
                    statusCode, duration);
        } else {
            log.info("[HTTP] {} {} 响应成功, status={}, 耗时={}ms",
                    request.getMethod(), request.getURI(),
                    statusCode, duration);
        }

        return response;
    }

    /** 脱敏敏感 Header */
    private String sanitizeHeaders(HttpRequest request) {
        var headers = new HashMap<>(request.getHeaders());
        headers.keySet().removeIf(k -> SENSITIVE_HEADERS.contains(k.toLowerCase()));
        return headers.toString();
    }
}
