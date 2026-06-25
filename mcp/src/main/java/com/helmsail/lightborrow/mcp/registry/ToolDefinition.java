package com.helmsail.lightborrow.mcp.registry;

import lombok.Getter;

import java.util.Map;

/**
 * 工具定义。描述工具的元数据及可执行句柄。
 */
@Getter
public class ToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;  // JSON Schema
    private final ToolExecutor executor;

    public ToolDefinition(String name, String description,
                          Map<String, Object> parameters,
                          ToolExecutor executor) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.executor = executor;
    }

    /**
     * 执行工具。
     *
     * @param args 参数 Map
     * @return 工具执行结果字符串
     */
    public String execute(Map<String, Object> args) {
        return executor.execute(args);
    }

    @FunctionalInterface
    public interface ToolExecutor {
        String execute(Map<String, Object> args);
    }
}
