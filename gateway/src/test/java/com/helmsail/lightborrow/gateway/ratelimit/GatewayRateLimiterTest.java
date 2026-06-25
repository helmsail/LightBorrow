package com.helmsail.lightborrow.gateway.ratelimit;

import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bucket4j 令牌桶算法单元测试。
 *
 * <p>直接测试 Bucket4j 令牌桶行为（不依赖 Redis），确保限流策略正确性。
 */
class GatewayRateLimiterTest {

    // ========== 令牌桶算法行为测试 ==========

    @Test
    void bucket_shouldAllowWithinCapacity() {
        Bucket bucket = new LocalBucketBuilder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillGreedy(3, Duration.ofSeconds(60))
                        .build())
                .build();

        assertThat(bucket.tryConsume(1)).isTrue();  // 第 1 次：剩余 2
        assertThat(bucket.tryConsume(1)).isTrue();  // 第 2 次：剩余 1
        assertThat(bucket.tryConsume(1)).isTrue();  // 第 3 次：剩余 0
        assertThat(bucket.tryConsume(1)).isFalse(); // 第 4 次：拒绝
    }

    @Test
    void bucket_shouldRefillOverTime() throws InterruptedException {
        Bucket bucket = new LocalBucketBuilder()
                .addLimit(Bandwidth.builder()
                        .capacity(1)
                        .refillGreedy(1, Duration.ofSeconds(1))
                        .build())
                .build();

        assertThat(bucket.tryConsume(1)).isTrue();   // 消耗
        assertThat(bucket.tryConsume(1)).isFalse();  // 立即再试，拒绝

        Thread.sleep(1100);

        assertThat(bucket.tryConsume(1)).isTrue();   // 补充后放行
    }

    @Test
    void bucket_shouldSupportBurst() {
        Bucket bucket = new LocalBucketBuilder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofSeconds(10))
                        .build())
                .build();

        assertThat(bucket.tryConsume(10)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();
    }

    @Test
    void bucket_shouldNotExceedCapacity() {
        Bucket bucket = new LocalBucketBuilder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofSeconds(1))
                        .build())
                .build();

        assertThat(bucket.tryConsume(5)).isTrue();
        assertThat(bucket.tryConsume(1)).isFalse();
    }

    // ========== GatewayProperties 默认值测试 ==========

    @Test
    void properties_shouldHaveDefaults() {
        var properties = new GatewayProperties();
        assertThat(properties.getRateLimitMaxRequests()).isEqualTo(20);
        assertThat(properties.getRateLimitWindowSeconds()).isEqualTo(60);
    }
}
