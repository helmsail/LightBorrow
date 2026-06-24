package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.http.LoggingClientHttpRequestInterceptor;
import com.helmsail.lightborrow.framework.http.TraceIdClientHttpRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * RestClient 自动配置。使用 JdkClientHttpRequestFactory（Java 11+ 内置 HttpClient），
 * 无需引入 Apache HttpClient 或 OkHttp。注册 TraceId 透传和日志拦截器。
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(HttpProperties.class)
public class RestClientConfig {

    @Bean
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder(
            HttpProperties httpProperties,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            LoggingClientHttpRequestInterceptor loggingInterceptor) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(httpProperties.getConnectTimeout()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(httpProperties.getReadTimeout());

        return RestClient.builder()
                .requestFactory(factory)
                .requestInterceptor(traceIdInterceptor)
                .requestInterceptor(loggingInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdClientHttpRequestInterceptor traceIdClientHttpRequestInterceptor() {
        return new TraceIdClientHttpRequestInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingClientHttpRequestInterceptor loggingClientHttpRequestInterceptor() {
        return new LoggingClientHttpRequestInterceptor();
    }
}
