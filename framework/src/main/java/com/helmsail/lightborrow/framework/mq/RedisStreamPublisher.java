package com.helmsail.lightborrow.framework.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis Stream 消息发布器。消费端直接使用 StreamMessageListenerContainer 原生 API。
 */
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate redisTemplate;

    public RecordId publish(String streamKey, Object body) {
        ObjectRecord<String, Object> record = ObjectRecord.create(streamKey, body);
        return redisTemplate.opsForStream().add(record);
    }
}
