package com.helmsail.lightborrow.framework.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingClientHttpRequestInterceptorTest {

    private final LoggingClientHttpRequestInterceptor interceptor = new LoggingClientHttpRequestInterceptor();

    @Mock
    private ClientHttpRequestExecution execution;

    @Test
    void shouldDelegateToExecution() throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://example.com/api"));
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], org.springframework.http.HttpStatus.OK);
        when(execution.execute(any(), any())).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, "body".getBytes(), execution);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void shouldRethrowIOException() throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://example.com/api"));
        when(execution.execute(any(), any())).thenThrow(new IOException("connection timeout"));

        assertThatThrownBy(() -> interceptor.intercept(request, "body".getBytes(), execution))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("connection timeout");
    }
}
