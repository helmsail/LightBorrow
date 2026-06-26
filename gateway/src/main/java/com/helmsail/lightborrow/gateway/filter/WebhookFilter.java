package com.helmsail.lightborrow.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.model.Result;
import com.helmsail.lightborrow.gateway.adapter.ChannelAdapter;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.ratelimit.GatewayRateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Webhook 请求安全过滤链。
 *
 * <p>拦截 {@code /webhook/*} 请求，依次执行：
 * <ol>
 *   <li>渠道识别 — 从路径中提取 channel</li>
 *   <li>签名验签 — 委托对应 {@link ChannelAdapter#verifyRequest}</li>
 *   <li>个人限流 — 通过 {@link GatewayRateLimiter} 检查</li>
 * </ol>
 * 任意环节失败则直接返回错误响应（JSON），不进入 Controller。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebhookFilter implements Filter {

    private static final String WEBHOOK_PATH_PREFIX = "/webhook/";

    private final Map<String, ChannelAdapter> adapterMap;
    private final GatewayRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public WebhookFilter(List<ChannelAdapter> adapters,
                         GatewayRateLimiter rateLimiter,
                         ObjectMapper objectMapper) {
        this.adapterMap = new HashMap<>(adapters.size());
        for (ChannelAdapter adapter : adapters) {
            this.adapterMap.put(adapter.getChannel(), adapter);
        }
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 仅处理 /webhook/{channel} 路径
        String path = request.getRequestURI();
        if (!path.startsWith(WEBHOOK_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String channel = path.substring(WEBHOOK_PATH_PREFIX.length());
        ChannelAdapter adapter = adapterMap.get(channel);

        if (adapter == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ErrorCode.GATEWAY_CHANNEL_ERROR.getCode(), "不支持的渠道: " + channel);
            return;
        }

        // 读取 body
        byte[] bodyBytes = request.getInputStream().readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        try {
            // 1. 签名验签
            adapter.verifyRequest(extractHeaders(request), body);
            log.debug("[WebhookFilter] 渠道 {} 验签通过", channel);

            // 2. 个人限流（从原始 body 提取 userId）
            String userId = adapter.extractUserId(body);
            if (!userId.isEmpty() && !rateLimiter.allowRequest(userId)) {
                log.warn("[WebhookFilter] 用户 {} 触发限流", userId);
                writeError(response, 429,
                        ErrorCode.GATEWAY_RATE_LIMITED.getCode(), "请求过于频繁");
                return;
            }

            // 全部通过，放行（使用缓存 body 的 wrapper 保证下游可读）
            chain.doFilter(new CachedBodyHttpServletRequest(request, bodyBytes), response);

        } catch (GatewayException e) {
            log.warn("[WebhookFilter] 渠道 {} 验签失败: {}", channel, e.getMessage());
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    e.getCode(), e.getMessage());
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>(16);
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }
        return headers;
    }

    private void writeError(HttpServletResponse response, int httpStatus,
                            int code, String msg) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(code, msg));
    }
}
