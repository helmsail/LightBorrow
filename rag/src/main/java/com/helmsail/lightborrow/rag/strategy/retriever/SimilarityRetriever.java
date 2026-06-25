package com.helmsail.lightborrow.rag.strategy.retriever;

import com.helmsail.lightborrow.rag.config.RagProperties;
import com.helmsail.lightborrow.rag.model.DocumentChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SimilarityRetriever implements RetrievalStrategy {

    private final double similarityThreshold;

    public SimilarityRetriever(RagProperties ragProperties) {
        this.similarityThreshold = ragProperties != null ? ragProperties.getSimilarityThreshold() : 0.5;
    }

    @Override
    public List<DocumentChunk> retrieve(float[] queryEmbedding,
                                         List<DocumentChunk> candidates, int topK) {
        if (queryEmbedding == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .filter(c -> c.getEmbedding() != null)
                .map(c -> new ScoredChunk(c, cosineSimilarity(queryEmbedding, c.getEmbedding())))
                .filter(scored -> scored.score() >= similarityThreshold)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .map(ScoredChunk::chunk)
                .collect(Collectors.toList());
    }

    /**
     * 计算两个 float 向量的余弦相似度。
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            log.warn("[RAG] 向量维度不匹配: {} vs {}", a.length, b.length);
            return 0;
        }
        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0 : dotProduct / denominator;
    }

    private record ScoredChunk(DocumentChunk chunk, double score) {}
}
