package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestClient 拦截器：自动透传 traceId 到下游服务。
 */
@Slf4j
public class TraceIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get(HttpConstant.MDC_TRACE_ID);
        if (traceId != null) {
            request.getHeaders().add(HttpConstant.HEADER_TRACE_ID, traceId);
        }
        return execution.execute(request, body);
    }
}
