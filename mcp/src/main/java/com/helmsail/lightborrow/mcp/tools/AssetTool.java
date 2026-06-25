package com.helmsail.lightborrow.mcp.tools;

import com.helmsail.lightborrow.mcp.annotation.McpParam;
import com.helmsail.lightborrow.mcp.annotation.McpTool;
import com.helmsail.lightborrow.mcp.service.AssetService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 资产业务工具。提供 6 个 {@link McpTool} 方法供 LLM 调用。
 */
@Slf4j
public class AssetTool {

    private final AssetService assetService;

    public AssetTool(AssetService assetService) {
        this.assetService = assetService;
    }

    @McpTool(name = "query_asset",
            description = "按资产编码(code)、资产名称(name)或关键词(keyword)查询IT资产信息。至少提供code/name/keyword中的一个。",
            params = {
                @McpParam(name = "code", desc = "资产编码，如 IT-001", required = false),
                @McpParam(name = "name", desc = "资产名称关键词", required = false),
                @McpParam(name = "keyword", desc = "模糊搜索关键词（匹配名称或描述）", required = false),
                @McpParam(name = "limit", desc = "每页数量，默认20，最大100", required = false),
                @McpParam(name = "offset", desc = "偏移量，默认0", required = false)
            })
    public String queryAsset(Map<String, Object> args) {
        String code = (String) args.getOrDefault("code", "");
        String name = (String) args.getOrDefault("name", "");
        String keyword = (String) args.getOrDefault("keyword", "");
        int limit = parseInt(args.getOrDefault("limit", "20"));
        int offset = parseInt(args.getOrDefault("offset", "0"));
        var result = assetService.queryAsset(
                code.isBlank() ? null : code,
                name.isBlank() ? null : "%" + name + "%",
                keyword.isBlank() ? null : "%" + keyword + "%",
                limit, offset);
        return formatResult("资产查询结果", result);
    }

    @McpTool(name = "query_my_borrows",
            description = "查询当前用户的借用记录。无需参数，自动获取当前用户ID。",
            params = {
                @McpParam(name = "userId", desc = "用户ID", required = false),
                @McpParam(name = "limit", desc = "每页数量，默认20，最大100", required = false),
                @McpParam(name = "offset", desc = "偏移量，默认0", required = false)
            })
    public String queryMyBorrows(Map<String, Object> args) {
        String userId = (String) args.getOrDefault("userId", "");
        int limit = parseInt(args.getOrDefault("limit", "20"));
        int offset = parseInt(args.getOrDefault("offset", "0"));
        var result = assetService.queryMyBorrows(userId, limit, offset);
        return formatResult("借用记录查询结果", result);
    }

    @McpTool(name = "borrow_asset",
            description = "提交IT资产借用申请。需要资产编码(assetCode)、借用原因(reason)、预计归还时间(expectedReturnAt)。",
            params = {
                @McpParam(name = "assetCode", desc = "资产编码，如 IT-001"),
                @McpParam(name = "reason", desc = "借用原因说明"),
                @McpParam(name = "expectedReturnAt", desc = "预计归还时间，格式 yyyy-MM-dd"),
                @McpParam(name = "userId", desc = "用户ID", required = false)
            })
    public String borrowAsset(Map<String, Object> args) {
        String userId = (String) args.getOrDefault("userId", "");
        String assetCode = (String) args.get("assetCode");
        String reason = (String) args.get("reason");
        String expectedReturnAt = (String) args.get("expectedReturnAt");
        assetService.submitBorrow(userId, assetCode, reason, expectedReturnAt);
        return "借用申请已提交，资产编码: " + assetCode;
    }

    @McpTool(name = "transfer_asset",
            description = "发起IT资产转借。需要转借记录ID(borrowId)和接受人ID(toUserId)。",
            params = {
                @McpParam(name = "borrowId", desc = "借用记录ID"),
                @McpParam(name = "toUserId", desc = "接受人用户ID"),
                @McpParam(name = "userId", desc = "发起转借的用户ID", required = false)
            })
    public String transferAsset(Map<String, Object> args) {
        String fromUserId = (String) args.getOrDefault("userId", "");
        String borrowId = (String) args.get("borrowId");
        String toUserId = (String) args.get("toUserId");
        assetService.submitTransfer(fromUserId, borrowId, toUserId);
        return "转借请求已发起，等待对方确认";
    }

    @McpTool(name = "cancel_borrow",
            description = "取消借用申请。需要借用记录ID(borrowId)。",
            params = {
                @McpParam(name = "borrowId", desc = "借用记录ID"),
                @McpParam(name = "userId", desc = "用户ID", required = false)
            })
    public String cancelBorrow(Map<String, Object> args) {
        String userId = (String) args.getOrDefault("userId", "");
        String borrowId = (String) args.get("borrowId");
        assetService.cancelBorrow(borrowId, userId);
        return "借用申请已取消";
    }

    @McpTool(name = "confirm_transfer",
            description = "确认接收转借。需要转借记录ID(transferId)。",
            params = {
                @McpParam(name = "transferId", desc = "转借记录ID"),
                @McpParam(name = "userId", desc = "确认接收的用户ID", required = false)
            })
    public String confirmTransfer(Map<String, Object> args) {
        String userId = (String) args.getOrDefault("userId", "");
        String transferId = (String) args.get("transferId");
        assetService.confirmTransfer(transferId, userId);
        return "转借已确认接收";
    }

    private static int parseInt(Object value) {
        if (value == null) return 20;
        return Integer.parseInt(value.toString());
    }

    private String formatResult(String title, List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return title + ": 无数据";
        }
        StringBuilder sb = new StringBuilder(title).append(":\n");
        for (Map<String, Object> row : data) {
            sb.append("- ").append(row).append("\n");
        }
        return sb.toString();
    }
}
