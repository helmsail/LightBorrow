package com.helmsail.lightborrow.aiinfra.config;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Supplier;

/**
 * 动态认证 Filter —— 每次 WebClient 请求都实时读取 api-key 和 base-url，
 * 配置中心变更后无需重启即可生效。
 *
 * <p>适用于 WebFlux {@code WebClient} 的 SSE 流式调用。
 */
public class DynamicExchangeFilter implements ExchangeFilterFunction {

    private final Supplier<String> baseUrlSupplier;
    private final Supplier<String> apiKeySupplier;

    public DynamicExchangeFilter(Supplier<String> baseUrlSupplier, Supplier<String> apiKeySupplier) {
        this.baseUrlSupplier = baseUrlSupplier;
        this.apiKeySupplier = apiKeySupplier;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest.Builder builder = ClientRequest.from(request);

        // 1. 动态设置 Authorization Header
        String apiKey = apiKeySupplier.get();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.headers(headers -> headers.setBearerAuth(apiKey));
        }

        // 2. 如果 baseUrl 变化，重新解析 URI
        String baseUrl = baseUrlSupplier.get();
        if (baseUrl != null && !baseUrl.isBlank()) {
            URI original = request.url();
            if (!original.isAbsolute() && original.toString().startsWith("/")) {
                URI resolved = URI.create(baseUrl + original.toString());
                builder.url(resolved);
            }
        }

        return next.exchange(builder.build());
    }
}
