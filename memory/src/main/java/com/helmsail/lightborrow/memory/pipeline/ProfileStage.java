package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.memory.exception.MemoryException;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 用户画像阶段。从 PostgreSQL behavior 表加载用户最近行为。
 */
@Slf4j
public class ProfileStage implements MemoryStage {

    private final JdbcTemplate jdbcTemplate;

    public ProfileStage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void load(MemoryContext ctx) {
        try {
            String sql = """
                    SELECT action, target_type, target_id, created_at
                    FROM behavior
                    WHERE user_id = ?
                    ORDER BY created_at DESC
                    LIMIT 5
                    """;
            List<Map<String, Object>> behaviors = jdbcTemplate.queryForList(sql, ctx.getUserId());
            if (behaviors.isEmpty()) {
                ctx.setProfileSummary("新用户");
            } else {
                StringBuilder sb = new StringBuilder("最近行为：");
                for (Map<String, Object> b : behaviors) {
                    sb.append(b.get("action")).append(" ")
                            .append(b.get("target_type")).append(" ")
                            .append(b.get("target_id")).append("; ");
                }
                ctx.setProfileSummary(sb.toString());
            }
            log.debug("[Memory] 画像加载 userId={}", ctx.getUserId());
        } catch (DataAccessException e) {
            log.error("[Memory] 画像加载失败 userId={}", ctx.getUserId(), e);
            throw new MemoryException(ErrorCode.MEMORY_PROFILE_FAILED, e, ctx.getUserId());
        }
    }
}
