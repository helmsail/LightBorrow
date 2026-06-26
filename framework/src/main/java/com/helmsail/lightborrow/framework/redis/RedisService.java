package com.helmsail.lightborrow.framework.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作服务。基于 StringRedisTemplate，提供常用操作封装。
 * 所有 Redis key 建议统一前缀管理。
 */
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    // ========== String 操作 ==========

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public Boolean setIfAbsent(String key, String value, Duration timeout) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    public Boolean expire(String key, Duration timeout) {
        return stringRedisTemplate.expire(key, timeout);
    }

    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    // ========== 通用操作 ==========

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
