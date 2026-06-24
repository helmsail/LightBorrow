package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * TraceId 透传拦截器。将 MDC traceId 写入下游请求头实现链路追踪。
 */
public class TraceIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get(HttpConstant.MDC_TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(HttpConstant.X_TRACE_ID, traceId);
        }
        return execution.execute(request, body);
    }
}
