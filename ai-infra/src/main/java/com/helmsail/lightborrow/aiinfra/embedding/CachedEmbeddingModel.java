package com.helmsail.lightborrow.aiinfra.embedding;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LRU 缓存装饰的 Embedding 模型。对高频同文本请求避免重复 API 调用。
 * 线程安全，使用 {@linkplain java.util.Collections#synchronizedMap synchronizedMap} 包装。
 */
@Slf4j
public class CachedEmbeddingModel implements EmbeddingModel {

    private final EmbeddingModel delegate;
    private final Map<String, float[]> cache;

    /**
     * @param delegate 被装饰的真实 Embedding 模型
     * @param maxSize  最大缓存条目数
     */
    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public CachedEmbeddingModel(EmbeddingModel delegate, int maxSize) {
        this.delegate = delegate;
        // accessOrder=true 使 LinkedHashMap 按访问顺序排序（LRU）
        this.cache = java.util.Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, float[]> eldest) {
                return size() > maxSize;
            }
        });
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
}
