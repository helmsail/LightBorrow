package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.http.TraceIdFilter;
import jakarta.servlet.DispatcherType;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

/**
 * 注册 TraceId 过滤器、MDC 上下文传递装饰器、CORS 跨域配置。
 *
 * CORS 生产安全：allowedOrigins 默认空列表，生产环境必须在 application.yaml 中
 * 配置 lightborrow.http.allowed-origins 显式指定域名。
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class WebConfig {

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("traceIdFilter");
        return registration;
    }

    /** CORS 配置：allowedOrigins 默认空列表，生产必须显式配置 */
    @Bean
    @ConditionalOnMissingBean
    public WebMvcConfigurer corsConfigurer(HttpProperties httpProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> origins = httpProperties.getAllowedOrigins();
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(origins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    /** 解决 @Async / 线程池不继承父线程 MDC 的问题，finally 中清理防止上下文泄漏 */
    @Bean
    @ConditionalOnMissingBean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            return () -> {
                try {
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    }
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        };
    }
}
