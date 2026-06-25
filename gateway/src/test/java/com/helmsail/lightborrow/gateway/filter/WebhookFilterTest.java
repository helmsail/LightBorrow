package com.helmsail.lightborrow.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.gateway.adapter.ChannelAdapter;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.ratelimit.GatewayRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookFilterTest {

    private static final String CHANNEL_FEISHU = "feishu";

    @Mock
    private ChannelAdapter feishuAdapter;

    @Mock
    private GatewayRateLimiter rateLimiter;

    private WebhookFilter filter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(feishuAdapter.getChannel()).thenReturn(CHANNEL_FEISHU);
        filter = new WebhookFilter(List.of(feishuAdapter), rateLimiter, objectMapper);
    }

    // ========== 路径路由 ==========

    @Test
    void shouldSkipNonWebhookPaths() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    // ========== 渠道识别 ==========

    @Test
    void shouldReturn400ForUnknownChannel() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/unknown");
        request.setContent("{}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("不支持的渠道");
        verify(chain, never()).doFilter(any(), any());
    }

    // ========== 验签 ==========

    @Test
    void shouldReturn401OnSignatureFailure() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/feishu");
        request.setContent("{\"event\":{}}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new GatewayException(ErrorCode.GATEWAY_CHANNEL_ERROR, "签名失败"))
                .when(feishuAdapter).verifyRequest(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("渠道适配失败");
        verify(chain, never()).doFilter(any(), any());
    }

    // ========== 限流 ==========

    @Test
    void shouldReturn429OnRateLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/feishu");
        String body = "{\"event\":{\"sender\":{\"sender_id\":{\"user_id\":\"user1\"}}}}";
        request.setContent(body.getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(feishuAdapter.extractUserId(body)).thenReturn("user1");
        when(rateLimiter.allowRequest("user1")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("请求过于频繁");
        verify(chain, never()).doFilter(any(), any());
    }

    // ========== 成功流程 ==========

    @Test
    void shouldPassOnValidRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/feishu");
        String body = "{\"event\":{\"sender\":{\"sender_id\":{\"user_id\":\"user1\"}}}}";
        request.setContent(body.getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(feishuAdapter.extractUserId(body)).thenReturn("user1");
        when(rateLimiter.allowRequest("user1")).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), eq(response));
    }

    // ========== 空 userId 不触发限流 ==========

    @Test
    void shouldSkipRateLimitWhenUserIdEmpty() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/feishu");
        request.setContent("{}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(feishuAdapter.extractUserId(any())).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(rateLimiter, never()).allowRequest(any());
        verify(chain).doFilter(any(), eq(response));
    }

}
