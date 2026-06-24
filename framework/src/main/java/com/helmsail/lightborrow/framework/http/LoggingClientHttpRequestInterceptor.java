package com.helmsail.lightborrow.framework.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 日志拦截器。Debug 输出完整请求体（含 Headers），Warn 标记异常响应或慢调用（>5s）。
 */
@Slf4j
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("[HTTP] >>> {} {} | headers={} | body={}",
                    request.getMethod(), request.getURI(),
                    sanitizeHeaders(request), new String(body, StandardCharsets.UTF_8));
        } else {
            log.info("[HTTP] >>> {} {}", request.getMethod(), request.getURI());
        }

        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException e) {
            log.error("[HTTP] {} {} 调用失败, 耗时={}ms, error={}",
                    request.getMethod(), request.getURI(),
                    System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }

        long duration = System.currentTimeMillis() - start;
        int statusCode = response.getStatusCode().value();

        if (statusCode >= 400 || duration > 5000) {
            log.warn("[HTTP] {} {} 响应异常, status={}, 耗时={}ms",
                    request.getMethod(), request.getURI(),
                    statusCode, duration);
        } else {
            log.debug("[HTTP] {} {} 响应成功, status={}, 耗时={}ms",
                    request.getMethod(), request.getURI(),
                    statusCode, duration);
        }

        return response;
    }

    private String sanitizeHeaders(HttpRequest request) {
        var headers = request.getHeaders();
        var sanitized = new java.util.HashMap<>(headers);
        sanitized.keySet().removeIf(k ->
                k.equalsIgnoreCase("Authorization")
                        || k.equalsIgnoreCase("Cookie")
                        || k.equalsIgnoreCase("Set-Cookie"));
        return sanitized.toString();
    }
}
