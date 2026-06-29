package com.helmsail.lightborrow.core.agent;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 级别语义缓存。
 *
 * <p>当用户发送相同或高度相似的问题时，直接返回缓存结果，避免重复调用 LLM。
 * 当前为本地内存实现，后续可迁移到 Redis。
 *
 * <p>通过 {@code lightborrow.core.cache.enabled=true} 启用。
 */
@Slf4j
public class AgentCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int maxSize;

    public AgentCache(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 获取缓存结果。
     *
     * @param queryHash 查询的 hash 值
     * @return 缓存条目，未命中返回 null
     */
    public CacheEntry get(String queryHash) {
        CacheEntry entry = cache.get(queryHash);
        if (entry != null) {
            log.debug("[AgentCache] 缓存命中: hash={}", queryHash);
        }
        return entry;
    }

    /**
     * 写入缓存。
     *
     * @param queryHash 查询 hash
     * @param answer    Agent 回答
     * @param toolsUsed 使用的工具
     */
    public void put(String queryHash, String answer, String toolsUsed) {
        if (cache.size() >= maxSize) {
            // 简单淘汰：清空缓存（生产环境用 LRU）
            cache.clear();
        }
        cache.put(queryHash, new CacheEntry(answer, toolsUsed, System.currentTimeMillis()));
        log.debug("[AgentCache] 缓存写入: hash={}", queryHash);
    }

    public void clear() {
        cache.clear();
        log.info("[AgentCache] 缓存已清空");
    }

    public record CacheEntry(String answer, String toolsUsed, long timestamp) {}
}
