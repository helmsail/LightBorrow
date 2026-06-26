package com.helmsail.lightborrow.aiinfra.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 动态认证拦截器 —— 每次请求都从配置源实时读取 api-key 和 base-url，
 * 配置中心变更后无需重启即可生效。
 *
 * <p>适用于 {@code RestClient}，配合 {@link DynamicExchangeFilter} 共同覆盖 LLM 和 Embedding。
 */
public class DynamicAuthInterceptor implements ClientHttpRequestInterceptor {

    private final Supplier<String> baseUrlSupplier;
    private final Supplier<String> apiKeySupplier;

    public DynamicAuthInterceptor(Supplier<String> baseUrlSupplier, Supplier<String> apiKeySupplier) {
        this.baseUrlSupplier = baseUrlSupplier;
        this.apiKeySupplier = apiKeySupplier;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        // 1. 动态设置 Authorization Header
        String apiKey = apiKeySupplier.get();
        if (apiKey != null && !apiKey.isBlank()) {
            request.getHeaders().setBearerAuth(apiKey);
        }

        // 2. 如果 baseUrl 变化，重新解析 URI（支持相对路径 {{path}} 到绝对 URL 的转换）
        String baseUrl = baseUrlSupplier.get();
        if (baseUrl != null && !baseUrl.isBlank()) {
            URI original = request.getURI();
            if (!original.isAbsolute() && original.toString().startsWith("/")) {
                // 构造新的 HttpRequest 覆盖 URI
                URI resolved = URI.create(baseUrl + original.toString());
                request.getHeaders().set(HttpHeaders.HOST, resolved.getHost());
                return execution.execute(new ResolvedUriRequest(request, resolved), body);
            }
        }

        return execution.execute(request, body);
    }

    /**
     * 包装原始请求，覆盖 {@link #getURI()} 返回解析后的绝对 URL。
     */
    private record ResolvedUriRequest(HttpRequest delegate, URI resolvedUri) implements HttpRequest {

        @Override
        public HttpMethod getMethod() {
            return delegate.getMethod();
        }

        @Override
        public URI getURI() {
            return resolvedUri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }
    }
}
