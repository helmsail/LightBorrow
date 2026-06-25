package com.helmsail.lightborrow.framework.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private HashOperations<String, Object, Object> hashOps;
    @Mock
    private ListOperations<String, String> listOps;
    @Mock
    private SetOperations<String, String> setOps;
    @Mock
    private ZSetOperations<String, String> zSetOps;

    private RedisService redisService;

    @Captor
    private ArgumentCaptor<List<String>> listCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        redisService = new RedisService(redisTemplate);
    }

    // ========== String ==========

    @Test
    void set_shouldDelegate() {
        redisService.set("k", "v");
        verify(valueOps).set("k", "v");
    }

    @Test
    void setWithTimeout_shouldDelegate() {
        Duration timeout = Duration.ofSeconds(30);
        redisService.set("k", "v", timeout);
        verify(valueOps).set("k", "v", timeout);
    }

    @Test
    void get_shouldReturnValue() {
        when(valueOps.get("k")).thenReturn("v");
        assertThat(redisService.get("k")).isEqualTo("v");
    }

    @Test
    void delete_shouldDelegate() {
        redisService.delete("k");
        verify(redisTemplate).delete("k");
    }

    @Test
    void deleteMultiple_shouldDelegate() {
        redisService.delete("k1", "k2");
        verify(redisTemplate).delete(listCaptor.capture());
        assertThat(listCaptor.getValue()).containsExactly("k1", "k2");
    }

    @Test
    void hasKey_shouldDelegate() {
        when(redisTemplate.hasKey("k")).thenReturn(true);
        assertThat(redisService.hasKey("k")).isTrue();
    }

    @Test
    void expire_shouldDelegate() {
        Duration timeout = Duration.ofMinutes(5);
        when(redisTemplate.expire("k", timeout)).thenReturn(true);
        assertThat(redisService.expire("k", timeout)).isTrue();
    }

    @Test
    void getExpire_shouldReturnSeconds() {
        when(redisTemplate.getExpire("k", TimeUnit.SECONDS)).thenReturn(120L);
        assertThat(redisService.getExpire("k")).isEqualTo(120L);
    }

    // ========== Hash ==========

    @Test
    void hSet_shouldDelegate() {
        redisService.hSet("k", "f", "v");
        verify(hashOps).put("k", "f", "v");
    }

    @Test
    void hGet_shouldReturnValue() {
        when(hashOps.get("k", "f")).thenReturn("v");
        assertThat(redisService.hGet("k", "f")).isEqualTo("v");
    }

    @Test
    void hGet_shouldConvertNullToString() {
        when(hashOps.get("k", "f")).thenReturn(null);
        assertThat(redisService.hGet("k", "f")).isNull();
    }

    @Test
    void hGet_shouldCallToStringOnObject() {
        when(hashOps.get("k", "f")).thenReturn(123);
        assertThat(redisService.hGet("k", "f")).isEqualTo("123");
    }

    @Test
    void hGetAll_shouldReturnEntries() {
        Map<Object, Object> entries = Map.of("f1", "v1", "f2", "v2");
        when(hashOps.entries("k")).thenReturn(entries);
        assertThat(redisService.hGetAll("k")).isEqualTo(entries);
    }

    @Test
    void hDelete_shouldDelegate() {
        when(hashOps.delete("k", (Object) "f1", "f2")).thenReturn(2L);
        assertThat(redisService.hDelete("k", "f1", "f2")).isEqualTo(2L);
    }

    @Test
    void hHasKey_shouldDelegate() {
        when(hashOps.hasKey("k", "f")).thenReturn(true);
        assertThat(redisService.hHasKey("k", "f")).isTrue();
    }

    @Test
    void hKeys_shouldReturnKeys() {
        when(hashOps.keys("k")).thenReturn(Set.of((Object) "f1", "f2"));
        assertThat(redisService.hKeys("k")).containsExactlyInAnyOrder("f1", "f2");
    }

    @Test
    void hSize_shouldReturnSize() {
        when(hashOps.size("k")).thenReturn(5L);
        assertThat(redisService.hSize("k")).isEqualTo(5L);
    }

    @Test
    void hIncrement_shouldDelegate() {
        when(hashOps.increment("k", "f", 3L)).thenReturn(10L);
        assertThat(redisService.hIncrement("k", "f", 3L)).isEqualTo(10L);
    }

    @Test
    void hDecrement_shouldNegateDelta() {
        when(hashOps.increment("k", "f", -2L)).thenReturn(5L);
        assertThat(redisService.hDecrement("k", "f", 2L)).isEqualTo(5L);
    }

    // ========== List ==========

    @Test
    void lPush_shouldDelegate() {
        when(listOps.leftPush("k", "v")).thenReturn(1L);
        assertThat(redisService.lPush("k", "v")).isEqualTo(1L);
    }

    @Test
    void rPush_shouldDelegate() {
        when(listOps.rightPush("k", "v")).thenReturn(1L);
        assertThat(redisService.rPush("k", "v")).isEqualTo(1L);
    }

    @Test
    void lPop_shouldReturnValue() {
        when(listOps.leftPop("k")).thenReturn("v");
        assertThat(redisService.lPop("k")).isEqualTo("v");
    }

    @Test
    void rPop_shouldReturnValue() {
        when(listOps.rightPop("k")).thenReturn("v");
        assertThat(redisService.rPop("k")).isEqualTo("v");
    }

    @Test
    void lRange_shouldReturnList() {
        when(listOps.range("k", 0, -1)).thenReturn(List.of("a", "b"));
        assertThat(redisService.lRange("k", 0, -1)).containsExactly("a", "b");
    }

    @Test
    void lTrim_shouldDelegate() {
        redisService.lTrim("k", 0, 10);
        verify(listOps).trim("k", 0, 10);
    }

    @Test
    void lLen_shouldReturnSize() {
        when(listOps.size("k")).thenReturn(3L);
        assertThat(redisService.lLen("k")).isEqualTo(3L);
    }

    // ========== Value Increment/Decrement ==========

    @Test
    void increment_shouldDelegate() {
        when(valueOps.increment("k")).thenReturn(5L);
        assertThat(redisService.increment("k")).isEqualTo(5L);
    }

    @Test
    void incrementWithDelta_shouldDelegate() {
        when(valueOps.increment("k", 3)).thenReturn(8L);
        assertThat(redisService.increment("k", 3)).isEqualTo(8L);
    }

    @Test
    void decrement_shouldDelegate() {
        when(valueOps.decrement("k")).thenReturn(4L);
        assertThat(redisService.decrement("k")).isEqualTo(4L);
    }

    @Test
    void decrementWithDelta_shouldDelegate() {
        when(valueOps.decrement("k", 2)).thenReturn(3L);
        assertThat(redisService.decrement("k", 2)).isEqualTo(3L);
    }

    // ========== Set ==========

    @Test
    void sAdd_shouldDelegate() {
        when(setOps.add("k", "a", "b")).thenReturn(2L);
        assertThat(redisService.sAdd("k", "a", "b")).isEqualTo(2L);
    }

    @Test
    void sRemove_shouldDelegate() {
        when(setOps.remove("k", "a", "b")).thenReturn(1L);
        assertThat(redisService.sRemove("k", "a", "b")).isEqualTo(1L);
    }

    @Test
    void sIsMember_shouldDelegate() {
        when(setOps.isMember("k", "a")).thenReturn(true);
        assertThat(redisService.sIsMember("k", "a")).isTrue();
    }

    @Test
    void sMembers_shouldReturnSet() {
        when(setOps.members("k")).thenReturn(Set.of("a", "b"));
        assertThat(redisService.sMembers("k")).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void sSize_shouldReturnSize() {
        when(setOps.size("k")).thenReturn(3L);
        assertThat(redisService.sSize("k")).isEqualTo(3L);
    }

    // ========== ZSet ==========

    @Test
    void zAdd_shouldDelegate() {
        when(zSetOps.add("k", "v", 1.0)).thenReturn(true);
        assertThat(redisService.zAdd("k", "v", 1.0)).isTrue();
    }

    @Test
    void zAddBatch_shouldDelegate() {
        Set<ZSetOperations.TypedTuple<String>> tuples = Set.of(
                ZSetOperations.TypedTuple.of("a", 1.0),
                ZSetOperations.TypedTuple.of("b", 2.0)
        );
        when(zSetOps.add("k", tuples)).thenReturn(2L);
        assertThat(redisService.zAdd("k", tuples)).isEqualTo(2L);
    }

    @Test
    void zRemove_shouldDelegate() {
        when(zSetOps.remove("k", (Object) "a", "b")).thenReturn(2L);
        assertThat(redisService.zRemove("k", "a", "b")).isEqualTo(2L);
    }

    @Test
    void zScore_shouldReturnScore() {
        when(zSetOps.score("k", "v")).thenReturn(3.5);
        assertThat(redisService.zScore("k", "v")).isEqualTo(3.5);
    }

    @Test
    void zCard_shouldReturnCount() {
        when(zSetOps.zCard("k")).thenReturn(5L);
        assertThat(redisService.zCard("k")).isEqualTo(5L);
    }

    @Test
    void zRangeByScore_shouldReturnSet() {
        when(zSetOps.rangeByScore("k", 0, 100)).thenReturn(Set.of("a", "b"));
        assertThat(redisService.zRangeByScore("k", 0, 100)).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void zRange_shouldReturnSet() {
        when(zSetOps.range("k", 0, -1)).thenReturn(Set.of("a", "b"));
        assertThat(redisService.zRange("k", 0, -1)).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void zReverseRange_shouldReturnSet() {
        when(zSetOps.reverseRange("k", 0, -1)).thenReturn(Set.of("b", "a"));
        assertThat(redisService.zReverseRange("k", 0, -1)).containsExactlyInAnyOrder("b", "a");
    }

    @Test
    void zIncrementScore_shouldReturnNewScore() {
        when(zSetOps.incrementScore("k", "v", 2.0)).thenReturn(5.0);
        assertThat(redisService.zIncrementScore("k", "v", 2.0)).isEqualTo(5.0);
    }

    // ========== 特殊操作 ==========

    @Test
    void setIfAbsent_shouldDelegate() {
        Duration timeout = Duration.ofSeconds(30);
        when(valueOps.setIfAbsent("k", "v", timeout)).thenReturn(true);
        assertThat(redisService.setIfAbsent("k", "v", timeout)).isTrue();
    }
}
