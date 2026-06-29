package com.helmsail.lightborrow.memory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helmsail.lightborrow.memory.model.entity.UserMemoryEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMemoryMapper extends BaseMapper<UserMemoryEntity> {

    @Insert("""
            INSERT INTO user_memory (user_id, memory_type, content, embedding, created_at)
            VALUES (#{userId}, #{memoryType}, #{content}, #{embedding}::vector, NOW())
            """)
    void insertWithVector(@Param("userId") String userId,
                          @Param("memoryType") String memoryType,
                          @Param("content") String content,
                          @Param("embedding") String embedding);

    @Select("""
            SELECT id, user_id, memory_type, content, embedding::text AS embedding, created_at,
                   1 - (embedding <=> #{queryVector}::vector) AS distance
            FROM user_memory
            WHERE user_id = #{userId}
              AND 1 - (embedding <=> #{queryVector}::vector) > #{threshold}
            ORDER BY embedding <=> #{queryVector}::vector
            LIMIT #{topK}
            """)
    List<UserMemoryEntity> searchSimilarByUser(@Param("userId") String userId,
                                                @Param("queryVector") String queryVector,
                                                @Param("topK") int topK,
                                                @Param("threshold") double threshold);
}
