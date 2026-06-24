package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 入站 traceId 处理。读取上游 X-Trace-Id 或生成 UUID，写入 MDC 使同一请求日志可关联；
 * 响应头回写透传下游。请求结束清理防止线程池复用污染。
 */
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            // 从请求头获取 traceId，没有则生成
            String traceId = request.getHeader(HttpConstant.X_TRACE_ID);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }

            // 写入 MDC
            MDC.put(HttpConstant.MDC_TRACE_ID, traceId);

            // 响应头回写
            response.setHeader(HttpConstant.X_TRACE_ID, traceId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(HttpConstant.MDC_TRACE_ID);
        }
    }
}
