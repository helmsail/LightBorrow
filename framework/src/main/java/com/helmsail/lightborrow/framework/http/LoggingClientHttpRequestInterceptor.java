package com.helmsail.lightborrow.framework.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestClient 日志拦截器。记录 HTTP 请求耗时、状态码。
 */
@Slf4j
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            long elapsed = System.currentTimeMillis() - start;
            log.debug("[HTTP] {} {} 完成, 耗时={}ms, status={}",
                    request.getMethod(), request.getURI(), elapsed,
                    response.getStatusCode());
            return response;
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[HTTP] {} {} 调用失败, 耗时={}ms, error={}",
                    request.getMethod(), request.getURI(), elapsed, e.getMessage());
            throw e;
        }
    }
}
