package com.helmsail.lightborrow.core.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentCacheTest {

    private AgentCache cache;

    @BeforeEach
    void setUp() {
        cache = new AgentCache(100);
    }

    @Test
    void shouldReturnNullOnCacheMiss() {
        var result = cache.get("nonexistent");
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnCachedResult() {
        cache.put("hash1", "answer1", "tool1");
        var entry = cache.get("hash1");
        assertThat(entry).isNotNull();
        assertThat(entry.answer()).isEqualTo("answer1");
        assertThat(entry.toolsUsed()).isEqualTo("tool1");
    }

    @Test
    void shouldHandleMultipleEntries() {
        cache.put("h1", "a1", "t1");
        cache.put("h2", "a2", "t2");

        assertThat(cache.get("h1").answer()).isEqualTo("a1");
        assertThat(cache.get("h2").answer()).isEqualTo("a2");
    }

    @Test
    void shouldClearCache() {
        cache.put("h1", "a1", "t1");
        cache.clear();
        assertThat(cache.get("h1")).isNull();
    }
}
