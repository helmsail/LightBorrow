package com.helmsail.lightborrow.framework.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * Redis Stream 消费端容器。ErrorHandler 防止消费者异常阻塞。
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
public class StreamConfig {

    @Bean(destroyMethod = "stop")
    @ConditionalOnMissingBean
    public StreamMessageListenerContainer<?, ?> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofMillis(100))
                .batchSize(10)
                .serializer(new GenericJackson2JsonRedisSerializer(objectMapper))
                .errorHandler(e -> log.error("[Stream] 消费异常, error={}", e.getMessage(), e))
                .build();

        var container = StreamMessageListenerContainer.create(connectionFactory, options);
        container.start();
        return container;
    }
}
