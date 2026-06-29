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

@Slf4j
public class HistoryStage implements MemoryStage {

    private static final String HISTORY_KEY_PREFIX = "history:";

    private final StringRedisTemplate stringRedisTemplate;
    private final int maxHistory;

    public HistoryStage(StringRedisTemplate stringRedisTemplate, MemoryProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.maxHistory = properties.getMaxHistory();
    }

    @Override
    public void load(MemoryContext ctx) {
        if (stringRedisTemplate == null) {
            ctx.setHistoryMessages(new ArrayList<>());
            return;
        }
        try {
            String key = historyKey(ctx.getUserId(), ctx.getSessionId());
            List<String> messages = stringRedisTemplate.opsForList().range(key, -maxHistory, -1);
            ctx.setHistoryMessages(messages != null ? messages : new ArrayList<>());
            log.debug("[Memory] 历史加载 userId={}, sessionId={}, count={}", ctx.getUserId(),
                    ctx.getSessionId(), ctx.getHistoryMessages().size());
        } catch (DataAccessException e) {
            log.error("[Memory] 历史加载失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_HISTORY_FAILED, e, ctx.getUserId());
        }
    }

    @Override
    public void save(MemoryContext ctx) {
        if (stringRedisTemplate == null) return;
        try {
            String key = historyKey(ctx.getUserId(), ctx.getSessionId());
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size != null && size > maxHistory) {
                stringRedisTemplate.opsForList().trim(key, size - maxHistory, -1);
            }
        } catch (DataAccessException e) {
            log.error("[Memory] 历史保存失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_HISTORY_FAILED, e, ctx.getUserId());
        }
    }

    public void appendMessage(String userId, String sessionId, String messageJson) {
        if (stringRedisTemplate == null) return;
        try {
            String key = historyKey(userId, sessionId);
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

    /** history 改为按 sessionId 隔离：history:{userId}:{sessionId} */
    private static String historyKey(String userId, String sessionId) {
        return sessionId != null ? HISTORY_KEY_PREFIX + userId + ":" + sessionId : HISTORY_KEY_PREFIX + userId;
    }
}
