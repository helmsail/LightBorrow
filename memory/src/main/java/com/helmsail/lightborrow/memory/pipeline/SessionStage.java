package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.memory.exception.MemoryException;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import com.helmsail.lightborrow.memory.model.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * 会话状态阶段。从 Redis Hash {@code session:{userId}} 加载/保存会话状态。
 */
@Slf4j
public class SessionStage implements MemoryStage {

    private static final String SESSION_KEY_PREFIX = "session:";

    private final StringRedisTemplate stringRedisTemplate;

    public SessionStage(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void load(MemoryContext ctx) {
        try {
            String key = SESSION_KEY_PREFIX + ctx.getUserId();
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                ctx.setNewSession(true);
                ctx.setSessionState(SessionState.builder()
                        .userId(ctx.getUserId())
                        .status("ACTIVE")
                        .createdAt(System.currentTimeMillis())
                        .lastAccessAt(System.currentTimeMillis())
                        .build());
            } else {
                ctx.setNewSession(false);
                ctx.setSessionState(SessionState.builder()
                        .userId(ctx.getUserId())
                        .intent((String) entries.get("intent"))
                        .status((String) entries.getOrDefault("status", "ACTIVE"))
                        .createdAt(toLong(entries.get("createdAt")))
                        .lastAccessAt(toLong(entries.get("lastAccessAt")))
                        .build());
            }
        } catch (DataAccessException e) {
            log.error("[Memory] 会话加载失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_SESSION_FAILED, e, ctx.getUserId());
        }
    }

    @Override
    public void save(MemoryContext ctx) {
        try {
            SessionState state = ctx.getSessionState();
            if (state == null) return;
            String key = SESSION_KEY_PREFIX + ctx.getUserId();
            state.setLastAccessAt(System.currentTimeMillis());
            stringRedisTemplate.opsForHash().putAll(key, Map.of(
                    "intent", state.getIntent() != null ? state.getIntent() : "",
                    "status", state.getStatus() != null ? state.getStatus() : "ACTIVE",
                    "createdAt", String.valueOf(state.getCreatedAt()),
                    "lastAccessAt", String.valueOf(state.getLastAccessAt())
            ));
        } catch (DataAccessException e) {
            log.error("[Memory] 会话保存失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_SESSION_FAILED, e, ctx.getUserId());
        }
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        return Long.parseLong(val.toString());
    }
}
