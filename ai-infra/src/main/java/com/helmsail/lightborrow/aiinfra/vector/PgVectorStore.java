package com.helmsail.lightborrow.aiinfra.vector;

import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import com.helmsail.lightborrow.aiinfra.mapper.VectorDocumentMapper;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.aiinfra.model.entity.VectorDocumentEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_VECTOR_SEARCH_FAILED;
import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_VECTOR_STORE_FAILED;

/** 支持余弦相似度检索 + HNSW 索引。基于 MyBatis-Plus 操作 pgvector 表。 */
@Slf4j
public class PgVectorStore implements VectorStore {

    private final VectorDocumentMapper mapper;
    private final String tableName;

    public PgVectorStore(VectorDocumentMapper mapper, AiProperties.VectorProperties properties) {
        this.mapper = mapper;
        this.tableName = properties.getTableName();
    }

    @Override
    public void upsert(VectorDocument document) {
        try {
            mapper.upsertVector(document.getId(), JsonUtil.toPgVector(document.getEmbedding()), document.getMetadata());
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_STORE_FAILED, e, "upsert", document.getId());
        }
    }

    @Override
    public void upsertAll(List<VectorDocument> documents) {
        for (VectorDocument doc : documents) {
            upsert(doc);
        }
    }

    @Override
    public List<VectorDocument> search(float[] embedding, int topK) {
        try {
            String queryVector = JsonUtil.toPgVector(embedding);
            List<VectorDocumentEntity> entities = mapper.searchSimilar(queryVector, topK);
            return entities.stream()
                    .map(e -> {
                        float[] emb = parseVector(e.getEmbedding());
                        return new VectorDocument(e.getId(), emb, e.getMetadata(),
                                e.getDistance() != null ? e.getDistance() : 0);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_SEARCH_FAILED, e, tableName);
        }
    }

    @Override
    public void delete(String id) {
        try {
            mapper.deleteById(id);
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_STORE_FAILED, e, "delete", id);
        }
    }

    @Override
    public void clear() {
        try {
            mapper.delete(null);
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_STORE_FAILED, e, "clear");
        }
    }

    /**
     * 解析 pgvector 文本格式 '[...]' 为 float[]。
     */
    private float[] parseVector(String vectorStr) {
        String trimmed = vectorStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] parts = trimmed.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
