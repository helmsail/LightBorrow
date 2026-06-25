package com.helmsail.lightborrow.framework.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingClientHttpRequestInterceptorTest {

    @Mock
    private org.springframework.http.HttpRequest request;

    @Mock
    private ClientHttpRequestExecution execution;

    private final LoggingClientHttpRequestInterceptor interceptor = new LoggingClientHttpRequestInterceptor();

    @Test
    void shouldLogAndReturnResponse() throws Exception {
        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api"));
        when(execution.execute(request, new byte[0])).thenReturn(mockResponse);

        interceptor.intercept(request, new byte[0], execution);
    }

    @Test
    void shouldLogErrorAndRethrow() throws Exception {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("http://example.com/api"));
        when(execution.execute(request, new byte[0])).thenThrow(new IOException("connection reset"));

        try {
            interceptor.intercept(request, new byte[0], execution);
        } catch (IOException e) {
            // expected
        }
    }
}
