package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void cleanUp() {
        MDC.clear();
    }

    @Test
    void shouldUseTraceIdFromHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpConstant.X_TRACE_ID, "abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, resp) -> {
            assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isEqualTo("abc123");
        });

        assertThat(response.getHeader(HttpConstant.X_TRACE_ID)).isEqualTo("abc123");
    }

    @Test
    void shouldGenerateUuidWhenNoHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String[] capturedTraceId = new String[1];
        filter.doFilterInternal(request, response, (req, resp) -> {
            capturedTraceId[0] = MDC.get(HttpConstant.MDC_TRACE_ID);
        });

        String traceId = capturedTraceId[0];
        assertThat(traceId).isNotNull().isNotEmpty();
        assertThat(traceId).hasSize(32);
        assertThat(response.getHeader(HttpConstant.X_TRACE_ID)).isEqualTo(traceId);
    }

    @Test
    void shouldGenerateUuidWhenHeaderIsBlank() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpConstant.X_TRACE_ID, "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, resp) -> {
            assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).hasSize(32);
        });
    }

    @Test
    void shouldCleanupMdcAfterRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpConstant.X_TRACE_ID, "test");

        filter.doFilterInternal(request, new MockHttpServletResponse(), (req, resp) -> {
            assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isEqualTo("test");
        });

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }

    @Test
    void shouldPassRequestToChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};

        filter.doFilterInternal(request, response, (req, resp) -> chainCalled[0] = true);

        assertThat(chainCalled[0]).isTrue();
    }

    @Test
    void shouldCleanupMdcEvenWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpConstant.X_TRACE_ID, "test");

        assertThatThrownBy(() ->
                filter.doFilterInternal(request, new MockHttpServletResponse(),
                        (req, resp) -> { throw new RuntimeException("chain error"); })
        ).isInstanceOf(RuntimeException.class);

        assertThat(MDC.get(HttpConstant.MDC_TRACE_ID)).isNull();
    }
}