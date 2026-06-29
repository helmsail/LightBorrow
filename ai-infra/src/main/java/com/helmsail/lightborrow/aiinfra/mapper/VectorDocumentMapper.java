package com.helmsail.lightborrow.aiinfra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helmsail.lightborrow.aiinfra.model.entity.VectorDocumentEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VectorDocumentMapper extends BaseMapper<VectorDocumentEntity> {

    @Insert("""
            INSERT INTO vector_documents (id, embedding, metadata)
            VALUES (#{id}, #{embedding}::vector, #{metadata}::jsonb)
            ON CONFLICT (id) DO UPDATE
            SET embedding = EXCLUDED.embedding, metadata = EXCLUDED.metadata
            """)
    void upsertVector(@Param("id") String id,
                      @Param("embedding") String embedding,
                      @Param("metadata") String metadata);

    @Select("""
            SELECT id, embedding::text AS embedding, metadata,
                   1 - (embedding <=> #{queryVector}::vector) AS distance
            FROM vector_documents
            ORDER BY embedding <=> #{queryVector}::vector
            LIMIT #{topK}
            """)
    List<VectorDocumentEntity> searchSimilar(@Param("queryVector") String queryVector,
                                              @Param("topK") int topK);

    @Select("""
            SELECT id, embedding::text AS embedding, metadata, 0 AS distance
            FROM vector_documents
            WHERE metadata->>'content' ILIKE #{keyword}
            LIMIT #{topK}
            """)
    List<VectorDocumentEntity> searchByKeyword(@Param("keyword") String keyword,
                                                @Param("topK") int topK);
}
