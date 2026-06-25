package com.helmsail.lightborrow.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 资产业务服务。封装对 asset/borrow/transfer 表的 JDBC 操作。
 */
@Slf4j
public class AssetService {

    private static final int DEFAULT_LIMIT = 20;

    private final JdbcTemplate jdbcTemplate;

    public AssetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> queryAsset(String code, String name, String keyword,
                                                 int limit, int offset) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (code != null && !code.isBlank()) {
            conditions.add("code = ?");
            params.add(code);
        }
        if (name != null && !name.isBlank()) {
            conditions.add("name ILIKE ?");
            params.add(name);
        }
        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(name ILIKE ? OR description ILIKE ?)");
            params.add(keyword);
            params.add(keyword);
        }

        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        String sql = "SELECT * FROM asset" + where + " ORDER BY id LIMIT ? OFFSET ?";
        params.add(Math.min(limit, 100));
        params.add(Math.max(offset, 0));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    public List<Map<String, Object>> queryMyBorrows(String userId, int limit, int offset) {
        String sql = """
                SELECT b.*, a.name AS asset_name, a.code AS asset_code
                FROM borrow b
                JOIN asset a ON b.asset_id = a.id
                WHERE b.user_id = ?
                ORDER BY b.created_at DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.queryForList(sql, userId, Math.min(limit, 100), Math.max(offset, 0));
    }

    public void submitBorrow(String userId, String assetCode, String reason, String expectedReturnAt) {
        String sql = """
                INSERT INTO borrow (user_id, asset_id, reason, expected_return_at, status, created_at)
                SELECT ?, a.id, ?, ?, 'pending', NOW()
                FROM asset a WHERE a.code = ?
                """;
        jdbcTemplate.update(sql, userId, reason, expectedReturnAt, assetCode);
        log.info("[MCP] 借用申请提交 userId={}, assetCode={}", userId, assetCode);
    }

    public void submitTransfer(String fromUserId, String borrowId, String toUserId) {
        String sql = """
                INSERT INTO transfer (from_user_id, borrow_id, to_user_id, status, created_at)
                VALUES (?, ?, ?, 'pending', NOW())
                """;
        jdbcTemplate.update(sql, fromUserId, borrowId, toUserId);
        log.info("[MCP] 转借发起 fromUserId={}, borrowId={}, toUserId={}", fromUserId, borrowId, toUserId);
    }

    public void cancelBorrow(String borrowId, String userId) {
        String sql = "UPDATE borrow SET status = 'cancelled', updated_at = NOW() WHERE id = ? AND user_id = ?";
        int rows = jdbcTemplate.update(sql, borrowId, userId);
        if (rows == 0) {
            log.warn("[MCP] 取消借用失败: borrowId={}, userId={}", borrowId, userId);
        }
    }

    public void confirmTransfer(String transferId, String userId) {
        String sql = "UPDATE transfer SET status = 'confirmed', updated_at = NOW() WHERE id = ? AND to_user_id = ?";
        int rows = jdbcTemplate.update(sql, transferId, userId);
        if (rows == 0) {
            log.warn("[MCP] 确认转借失败: transferId={}, userId={}", transferId, userId);
        }
    }
}
