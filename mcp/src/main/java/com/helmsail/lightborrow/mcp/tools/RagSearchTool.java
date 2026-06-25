package com.helmsail.lightborrow.mcp.tools;

import com.helmsail.lightborrow.mcp.annotation.McpParam;
import com.helmsail.lightborrow.mcp.annotation.McpTool;
import lombok.extern.slf4j.Slf4j;

/**
 * RAG 检索工具。通过 {@link McpTool} 暴露给 LLM，内部调用 RAG 在线 Pipeline。
 */
@Slf4j
public class RagSearchTool {

    private final com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline ragOnlinePipeline;

    public RagSearchTool(com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline ragOnlinePipeline) {
        this.ragOnlinePipeline = ragOnlinePipeline;
    }

    @McpTool(name = "query_knowledge",
            description = "搜索知识库获取IT资产相关政策和指南信息。当用户询问政策、流程、规则等知识性问题时调用。",
            params = {
                @McpParam(name = "question", desc = "用户的问题，如「借用电脑的流程是什么」")
            })
    public String queryKnowledge(String question) {
        log.info("[MCP] RAG 知识检索: {}", question);
        return ragOnlinePipeline.execute(question);
    }
}
