package com.helmsail.lightborrow.mcp.tools;

import com.helmsail.lightborrow.mcp.annotation.McpTool;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统工具：LLM 通过此工具向用户反问以收集信息。
 * 当用户需求信息不完整时，LLM 调用此工具向用户提问。
 */
@Slf4j
public class AskUserQuestionTool {

    @McpTool(name = "ask_user_question",
             description = """
             当用户需求信息不完整时，调用此工具向用户提问以收集信息。
             可以一次问 1-2 个问题，不要一次问太多。
             示例：用户说"帮我借一台电脑"，但没说借哪台、借多久。
             """)
    public String askUser(String question) {
        log.info("[MCP] 反问用户: {}", question);
        return "[系统已向用户提问: " + question + "，等待用户回答]";
    }
}
