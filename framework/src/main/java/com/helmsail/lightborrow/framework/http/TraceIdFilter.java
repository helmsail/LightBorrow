package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器。为每个 HTTP 请求注入 traceId。
 * 优先透传上游请求头中的 traceId，不存在则自动生成。
 */
@Slf4j
public class TraceIdFilter implements Filter, Ordered {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // 从请求头获取 traceId，不存在则生成
            String traceId = request.getHeader(HttpConstant.HEADER_TRACE_ID);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(HttpConstant.MDC_TRACE_ID, traceId);
            response.setHeader(HttpConstant.HEADER_TRACE_ID, traceId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(HttpConstant.MDC_TRACE_ID);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
