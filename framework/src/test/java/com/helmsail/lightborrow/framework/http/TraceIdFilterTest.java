package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private TraceIdFilter filter;

    @Test
    void shouldUseTraceIdFromRequestHeader() throws Exception {
        when(request.getHeader(HttpConstant.HEADER_TRACE_ID)).thenReturn("header-trace-id");

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
        verify(response).setHeader(HttpConstant.HEADER_TRACE_ID, "header-trace-id");
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldGenerateTraceIdWhenHeaderNotPresent() throws Exception {
        when(request.getHeader(HttpConstant.HEADER_TRACE_ID)).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldGenerateTraceIdWhenHeaderIsBlank() throws Exception {
        when(request.getHeader(HttpConstant.HEADER_TRACE_ID)).thenReturn("   ");

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSetResponseHeaderWithTraceId() throws Exception {
        when(request.getHeader(HttpConstant.HEADER_TRACE_ID)).thenReturn("trace-from-header");

        filter.doFilter(request, response, chain);

        verify(response).setHeader(HttpConstant.HEADER_TRACE_ID, "trace-from-header");
    }

    @Test
    void shouldCleanupMdcAfterFilter() throws Exception {
        when(request.getHeader(HttpConstant.HEADER_TRACE_ID)).thenReturn("test-trace");

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }

    @Test
    void shouldHaveHighestPrecedence() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
    }
}
