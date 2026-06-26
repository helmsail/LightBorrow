package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.memory.config.MemoryProperties;
import com.helmsail.lightborrow.memory.exception.MemoryException;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话历史阶段。从 Redis List {@code history:{userId}} 加载/保存消息。
 * 滑动窗口维护最近 N 条消息。
 */
@Slf4j
public class HistoryStage implements MemoryStage {

    private static final String HISTORY_KEY_PREFIX = "history:";

    private final StringRedisTemplate stringRedisTemplate;
    private final int maxHistory;

    public HistoryStage(StringRedisTemplate stringRedisTemplate, MemoryProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.maxHistory = properties != null ? properties.getMaxHistory() : 20;
    }

    @Override
    public void load(MemoryContext ctx) {
        try {
            String key = HISTORY_KEY_PREFIX + ctx.getUserId();
            List<String> messages = stringRedisTemplate.opsForList().range(key, -maxHistory, -1);
            ctx.setHistoryMessages(messages != null ? messages : new ArrayList<>());
            log.debug("[Memory] 历史加载 userId={}, count={}", ctx.getUserId(),
                    ctx.getHistoryMessages().size());
        } catch (DataAccessException e) {
            log.error("[Memory] 历史加载失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_HISTORY_FAILED, e, ctx.getUserId());
        }
    }

    @Override
    public void save(MemoryContext ctx) {
        try {
            String key = HISTORY_KEY_PREFIX + ctx.getUserId();
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size != null && size > maxHistory) {
                stringRedisTemplate.opsForList().trim(key, size - maxHistory, -1);
            }
        } catch (DataAccessException e) {
            log.error("[Memory] 历史保存失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_HISTORY_FAILED, e, ctx.getUserId());
        }
    }

    /** 追加单条消息到历史。超出窗口自动裁剪。 */
    public void appendMessage(String userId, String messageJson) {
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            stringRedisTemplate.opsForList().rightPush(key, messageJson);
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size != null && size > maxHistory) {
                stringRedisTemplate.opsForList().trim(key, size - maxHistory, -1);
            }
        } catch (DataAccessException e) {
            log.error("[Memory] 消息追加失败 userId={}", userId, e);
            throw new MemoryException(ErrorCode.MEMORY_HISTORY_FAILED, e, userId);
        }
    }
}
