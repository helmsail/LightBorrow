package com.helmsail.lightborrow.rag.strategy.retrieval;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.mapper.VectorDocumentMapper;
import com.helmsail.lightborrow.aiinfra.model.entity.VectorDocumentEntity;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 混合检索服务。结合向量检索和关键词检索，提高召回率。
 */
@Slf4j
public class HybridSearchService {

    /** 向量检索权重 */
    private static final double VECTOR_WEIGHT = 0.7;

    /** 关键词检索权重 */
    private static final double KEYWORD_WEIGHT = 0.3;

    private final VectorDocumentMapper vectorDocumentMapper;
    private final EmbeddingModel embeddingModel;

    public HybridSearchService(VectorDocumentMapper vectorDocumentMapper,
                                EmbeddingModel embeddingModel) {
        this.vectorDocumentMapper = vectorDocumentMapper;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 执行混合检索。
     *
     * @param query       查询文本
     * @param vectorTopK  向量检索数量
     * @param keywordTopK 关键词检索数量
     * @param finalTopK   最终返回数量
     * @return 排序后的 DocumentChunk 列表
     */
    public List<DocumentChunk> search(String query, int vectorTopK, int keywordTopK, int finalTopK) {
        // 1. 向量检索
        List<ScoredChunk> vectorResults = vectorSearch(query, vectorTopK);

        // 2. 关键词检索
        List<ScoredChunk> keywordResults = keywordSearch(query, keywordTopK);

        // 3. 合并去重 + 加权排序
        return mergeAndRerank(vectorResults, keywordResults, finalTopK);
    }

    private List<ScoredChunk> vectorSearch(String query, int topK) {
        try {
            float[] embedding = embeddingModel.embed(query);
            if (embedding == null || embedding.length == 0) return List.of();

            String vecStr = JsonUtil.toPgVector(embedding);
            List<VectorDocumentEntity> entities = vectorDocumentMapper.searchSimilar(vecStr, topK * 2);
            return entities.stream()
                    .map(e -> {
                        DocumentChunk chunk = toDocumentChunk(e);
                        double score = e.getDistance() != null ? e.getDistance() : 0;
                        return new ScoredChunk(chunk, score * VECTOR_WEIGHT, "vector");
                    })
                    .filter(c -> c.chunk() != null && c.chunk().getContent() != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[RAG] 向量检索失败", e);
            return List.of();
        }
    }

    private List<ScoredChunk> keywordSearch(String query, int topK) {
        try {
            // 提取关键词（取最长的3个词）
            String[] words = query.split("[\\s,，。；;：:]+");
            List<String> keywords = new ArrayList<>();
            for (String word : words) {
                if (word.length() >= 2) {
                    keywords.add(word);
                }
            }

            if (keywords.isEmpty()) return List.of();

            // 对每个关键词做 ILIKE 检索
            Map<String, ScoredChunk> merged = new HashMap<>();
            for (String keyword : keywords) {
                try {
                    List<VectorDocumentEntity> entities = vectorDocumentMapper.searchByKeyword(
                            "%" + keyword + "%", topK);
                    for (VectorDocumentEntity e : entities) {
                        DocumentChunk chunk = toDocumentChunk(e);
                        if (chunk == null || chunk.getContent() == null) continue;

                        // 关键词匹配度：关键词在内容中出现的次数占比
                        String content = chunk.getContent().toLowerCase();
                        String kw = keyword.toLowerCase();
                        int count = content.split(kw, -1).length - 1;
                        double score = (double) count / content.length() * 100;

                        merged.merge(e.getId(),
                                new ScoredChunk(chunk, score * KEYWORD_WEIGHT, "keyword"),
                                (a, b) -> new ScoredChunk(a.chunk(), a.score() + b.score(), "hybrid"));
                    }
                } catch (Exception ex) {
                    log.debug("[RAG] 关键词 '{}' 检索跳过", keyword);
                }
            }

            return new ArrayList<>(merged.values());
        } catch (Exception e) {
            log.warn("[RAG] 关键词检索失败", e);
            return List.of();
        }
    }

    private List<DocumentChunk> mergeAndRerank(List<ScoredChunk> vector, List<ScoredChunk> keyword,
                                                int topK) {
        // 合并
        Map<String, ScoredChunk> merged = new HashMap<>();
        for (ScoredChunk sc : vector) {
            merged.put(sc.chunk().getId(), sc);
        }
        for (ScoredChunk sc : keyword) {
            merged.merge(sc.chunk().getId(), sc,
                    (a, b) -> new ScoredChunk(a.chunk(), a.score() + b.score(), "hybrid"));
        }

        // 按分数降序排列
        return merged.values().stream()
                .sorted(Comparator.<ScoredChunk>comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .map(ScoredChunk::chunk)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private DocumentChunk toDocumentChunk(VectorDocumentEntity entity) {
        if (entity.getMetadata() == null || entity.getMetadata().isBlank()) return null;
        try {
            Map<String, Object> meta = JsonUtil.fromJson(entity.getMetadata(), Map.class);
            return DocumentChunk.builder()
                    .id(entity.getId())
                    .documentId((String) meta.getOrDefault("documentId", ""))
                    .content((String) meta.get("content"))
                    .chunkIndex(meta.get("chunkIndex") instanceof Number
                            ? ((Number) meta.get("chunkIndex")).intValue() : 0)
                    .metadata(Map.of("distance", entity.getDistance() != null ? entity.getDistance() : 0))
                    .build();
        } catch (Exception e) {
            log.warn("[RAG] metadata 解析失败 id={}", entity.getId(), e);
            return null;
        }
    }

    private record ScoredChunk(DocumentChunk chunk, double score, String source) {}
}

