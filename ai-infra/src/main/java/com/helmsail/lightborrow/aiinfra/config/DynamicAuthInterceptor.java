package com.helmsail.lightborrow.aiinfra.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.function.Supplier;

/** 每次请求从配置源实时读取 api-key，配置中心变更后无需重启。 */
public class DynamicAuthInterceptor implements ClientHttpRequestInterceptor {

    private final Supplier<String> apiKeySupplier;

    public DynamicAuthInterceptor(Supplier<String> apiKeySupplier) {
        this.apiKeySupplier = apiKeySupplier;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        // 动态设置 Authorization Header
        String apiKey = apiKeySupplier.get();
        if (apiKey != null && !apiKey.isBlank()) {
            request.getHeaders().setBearerAuth(apiKey);
        }

        return execution.execute(request, body);
    }
}
