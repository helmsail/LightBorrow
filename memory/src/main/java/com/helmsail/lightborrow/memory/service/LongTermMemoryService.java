package com.helmsail.lightborrow.memory.service;

import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.memory.mapper.UserMemoryMapper;
import com.helmsail.lightborrow.memory.model.entity.UserMemoryEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 长期记忆服务。支持记忆的提取保存和检索。
 *
 * <p>在 Agent 处理完成后提取关键信息并存入 pgvector，
 * 在下一次处理时检索相关记忆注入 System Prompt。
 */
@Slf4j
public class LongTermMemoryService {

    private static final int TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.75;

    private final UserMemoryMapper memoryMapper;
    private final EmbeddingModel embeddingModel;

    public LongTermMemoryService(UserMemoryMapper memoryMapper, EmbeddingModel embeddingModel) {
        this.memoryMapper = memoryMapper;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 提取记忆并保存。
     *
     * @param userId      用户 ID
     * @param memories    提取出的记忆文本列表（来自 MemoryExtractor）
     */
    public void saveMemories(String userId, List<String> memories) {
        if (memories == null || memories.isEmpty()) return;

        for (String memory : memories) {
            try {
                float[] embedding = embeddingModel.embed(memory);
                if (embedding == null || embedding.length == 0) continue;

                String vecStr = JsonUtil.toPgVector(embedding);
                memoryMapper.insertWithVector(userId, "entity", memory, vecStr);
                log.debug("[Memory] 保存记忆 userId={}, content={}", userId, truncate(memory, 50));
            } catch (Exception e) {
                log.warn("[Memory] 记忆保存失败 userId={}", userId, e);
            }
        }
    }

    /**
     * 检索用户相关的历史记忆。
     *
     * @param userId 用户 ID
     * @param query  当前用户输入
     * @return 记忆文本列表，可直接注入 System Prompt
     */
    public List<String> retrieve(String userId, String query) {
        try {
            float[] embedding = embeddingModel.embed(query);
            if (embedding == null || embedding.length == 0) return List.of();

            String vecStr = JsonUtil.toPgVector(embedding);
            List<UserMemoryEntity> results = memoryMapper.searchSimilarByUser(
                    userId, vecStr, TOP_K, SIMILARITY_THRESHOLD);

            return results.stream()
                    .map(e -> "- " + e.getContent())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[Memory] 记忆检索失败 userId={}", userId, e);
            return List.of();
        }
    }

    private static String truncate(String str, int maxLen) {
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }
}
