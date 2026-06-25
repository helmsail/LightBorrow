package com.helmsail.lightborrow.framework.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis Stream 消息发布器。消费端直接使用 StreamMessageListenerContainer 原生 API。
 */
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate redisTemplate;

    /**
     * 发布消息到 Stream。
     *
     * @param streamKey Stream 键
     * @param body      消息体（会被序列化为 JSON）
     * @return 消息 ID
     */
    public RecordId publish(String streamKey, Object body) {
        ObjectRecord<String, Object> record = ObjectRecord.create(streamKey, body);
        return redisTemplate.opsForStream().add(record);
    }

    /**
     * 发布消息到 Stream，并限制 Stream 最大长度（近似裁剪，等价于 XADD MAXLEN ~ count）。
     *
     * @param streamKey Stream 键
     * @param body      消息体
     * @param maxLen    最大长度（超过时自动裁剪旧消息）
     * @return 消息 ID
     */
    public RecordId publish(String streamKey, Object body, long maxLen) {
        ObjectRecord<String, Object> record = ObjectRecord.create(streamKey, body);
        return redisTemplate.opsForStream().add(record,
                XAddOptions.maxlen(maxLen).approximateTrimming(true));
    }
}
