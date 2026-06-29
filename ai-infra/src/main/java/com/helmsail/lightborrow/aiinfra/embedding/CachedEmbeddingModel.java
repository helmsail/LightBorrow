package com.helmsail.lightborrow.aiinfra.embedding;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU 缓存装饰的 Embedding 模型。对高频同文本请求避免重复 API 调用。
 * 线程安全，使用 {@link java.util.concurrent.locks.ReentrantReadWriteLock} 保护。
 */
@Slf4j
public class CachedEmbeddingModel implements EmbeddingModel {

    private final EmbeddingModel delegate;
    private final LruCache cache;

    /**
     * @param delegate 被装饰的真实 Embedding 模型
     * @param maxSize  最大缓存条目数
     */
    public CachedEmbeddingModel(EmbeddingModel delegate, int maxSize) {
        this.delegate = delegate;
        this.cache = new LruCache(maxSize);
    }

    @Override
    public float[] embed(String text) {
        float[] cached = cache.get(text);
        if (cached != null) {
            log.debug("Cache hit for embedding text (len={})", text.length());
            return cached;
        }
        log.debug("Cache miss for embedding text (len={})", text.length());
        float[] result = delegate.embed(text);
        cache.put(text, result);
        return result;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        return delegate.embedBatch(texts);
    }

    /**
     * 线程安全的 LRU 缓存。使用读写锁实现并发安全：
     * 读操作使用读锁（可并发），写操作使用写锁（互斥）。
     */
    private static class LruCache {

        private final LinkedHashMap<String, float[]> map;
        private final ReentrantReadWriteLock lock;

        LruCache(int maxSize) {
            this.lock = new ReentrantReadWriteLock();
            this.map = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, float[]> eldest) {
                    return size() > maxSize;
                }
            };
        }

        float[] get(String key) {
            lock.readLock().lock();
            try {
                return map.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }

        void put(String key, float[] value) {
            lock.writeLock().lock();
            try {
                map.put(key, value);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
