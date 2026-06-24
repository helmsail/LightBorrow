package com.helmsail.lightborrow.framework.mq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * Redis Stream 消费端容器。pollTimeout=100ms, batchSize=10。
 * 容器在配置时直接 start()，消费者注入后即可使用。
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
public class StreamConfig {

    @Bean(destroyMethod = "stop")
    @ConditionalOnMissingBean
    public StreamMessageListenerContainer<?, ?> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofMillis(100))
                .batchSize(10)
                .build();

        var container = StreamMessageListenerContainer.create(connectionFactory, options);
        container.start();
        return container;
    }
}
