package com.helmsail.lightborrow.framework.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.connection.stream.RecordId;

import java.util.Map;

/**
 * Redis Stream 消息发布者。基于 StringRedisTemplate 的 opsForStream()。
 */
@Slf4j
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发布消息到指定 Stream。
     *
     * @param stream  Stream 名称
     * @param message 消息内容（Map 形式）
     * @return 消息 ID
     */
    public RecordId publish(String stream, Map<String, String> message) {
        RecordId id = stringRedisTemplate.opsForStream().add(stream, message);
        log.debug("[MQ] 消息发布 stream={}, id={}", stream, id);
        return id;
    }
}
