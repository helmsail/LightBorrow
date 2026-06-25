package com.helmsail.lightborrow.aiinfra.vector;

import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Array;
import java.sql.Connection;
import java.util.List;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_VECTOR_SEARCH_FAILED;
import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_VECTOR_STORE_FAILED;

/**
 * PostgreSQL pgvector 向量存储实现。支持余弦相似度检索 + HNSW 索引。
 */
@Slf4j
public class PgVectorStore implements VectorStore {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final int dimension;

    public PgVectorStore(JdbcTemplate jdbcTemplate, AiProperties.VectorProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = properties.getTableName();
        this.dimension = properties.getDimension();
        initTable(properties);
    }

    private void initTable(AiProperties.VectorProperties properties) {
        // 初始化 pgvector 表结构（仅 PostgreSQL 生效）
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        String createTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(64) PRIMARY KEY,
                    embedding vector(%d),
                    metadata JSONB,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(tableName, dimension);
        jdbcTemplate.execute(createTable);
        String createIndex = """
                CREATE INDEX IF NOT EXISTS idx_%s_embedding
                ON %s USING hnsw (embedding vector_cosine_ops)
                WITH (m = %d, ef_construction = %d)
                """.formatted(tableName, tableName, properties.getHnswM(), properties.getHnswEfConstruction());
        jdbcTemplate.execute(createIndex);
        log.info("Initialized pgvector table '{}' with HNSW index (dim={})", tableName, dimension);
    }

    @Override
    public void upsert(VectorDocument document) {
        String sql = """
                INSERT INTO %s (id, embedding, metadata)
                VALUES (?, ?::vector, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET embedding = EXCLUDED.embedding, metadata = EXCLUDED.metadata
                """.formatted(tableName);
        try {
            jdbcTemplate.update(sql, document.getId(), toPgVector(document.getEmbedding()), document.getMetadata());
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_STORE_FAILED, e, "upsert", document.getId());
        }
    }

    @Override
    public void upsertAll(List<VectorDocument> documents) {
        String sql = """
                INSERT INTO %s (id, embedding, metadata)
                VALUES (?, ?::vector, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET embedding = EXCLUDED.embedding, metadata = EXCLUDED.metadata
                """.formatted(tableName);
        try {
            jdbcTemplate.batchUpdate(sql, documents, documents.size(), (ps, doc) -> {
                ps.setString(1, doc.getId());
                ps.setObject(2, toPgVector(doc.getEmbedding()));
                ps.setString(3, doc.getMetadata());
            });
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_STORE_FAILED, e, "upsertAll", documents.size());
        }
    }

    @Override
    public List<VectorDocument> search(float[] embedding, int topK) {
        String sql = """
                SELECT id, embedding::text, metadata,
                       1 - (embedding <=> ?::vector) AS distance
                FROM %s
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """.formatted(tableName);
        try {
            String vecStr = toPgVector(embedding);
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                float[] emb = parseVector(rs.getString("embedding"));
                return new VectorDocument(
                        rs.getString("id"),
                        emb,
                        rs.getString("metadata"),
                        rs.getDouble("distance")
                );
            }, vecStr, vecStr, topK);
        } catch (Exception e) {
            throw new AiException(AI_VECTOR_SEARCH_FAILED, e, tableName);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM %s WHERE id = ?".formatted(tableName);
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void clear() {
        jdbcTemplate.execute("TRUNCATE TABLE %s".formatted(tableName));
    }

    /**
     * 将 float[] 转换为 pgvector 字符串格式，如 '[0.1,0.2,0.3]'。
     */
    private String toPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append(']');
        return sb.toString();
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
