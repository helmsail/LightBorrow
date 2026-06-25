package com.helmsail.lightborrow.mcp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注一个方法为 MCP 工具方法。被标注的方法将自动注册到 ToolRegistry。
 * 通过 {@link #params()} 声明的参数元数据将生成完整的 JSON Schema。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {

    /** 工具名称，供 LLM 调用时使用。推荐 snake_case。 */
    String name();

    /** 工具描述，LLM 通过此描述判断何时调用。 */
    String description();

    /** 工具参数元数据。未声明时根据方法参数类型自动推断简化 Schema。 */
    McpParam[] params() default {};
}
