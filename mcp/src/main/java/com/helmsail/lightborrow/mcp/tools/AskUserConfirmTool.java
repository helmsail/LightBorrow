package com.helmsail.lightborrow.mcp.tools;

import com.helmsail.lightborrow.mcp.annotation.McpTool;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统工具：LLM 在执行高风险操作（借用/转借/取消）前，通过此工具请用户确认。
 */
@Slf4j
public class AskUserConfirmTool {

    @McpTool(name = "ask_user_confirm",
             description = """
             执行借用/转借/取消等操作前，向用户确认信息。
             参数 summary 是把所有已收集信息汇总成一句话请用户确认。
             """)
    public String askConfirm(String summary) {
        log.info("[MCP] 请求用户确认: {}", summary);
        return "[系统已请求用户确认: " + summary + "，等待用户确认]";
    }
}
