package com.helmsail.lightborrow.framework.http;

import com.helmsail.lightborrow.framework.constant.HttpConstant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceIdClientHttpRequestInterceptorTest {

    @Mock
    private HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    private final TraceIdClientHttpRequestInterceptor interceptor = new TraceIdClientHttpRequestInterceptor();

    @Test
    void shouldAddTraceIdHeaderWhenPresentInMdc() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        lenient().when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[0])).thenReturn(mock(ClientHttpResponse.class));

        MDC.put(HttpConstant.MDC_TRACE_ID, "mdc-trace-id");
        try {
            interceptor.intercept(request, new byte[0], execution);
        } finally {
            MDC.clear();
        }

        assertThat(headers.getFirst(HttpConstant.HEADER_TRACE_ID)).isEqualTo("mdc-trace-id");
    }

    @Test
    void shouldNotAddHeaderWhenMdcEmpty() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        lenient().when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[0])).thenReturn(mock(ClientHttpResponse.class));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(headers.getFirst(HttpConstant.HEADER_TRACE_ID)).isNull();
    }
}
