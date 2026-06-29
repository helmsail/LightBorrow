package com.helmsail.lightborrow.mcp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 {@link McpTool} 方法的单个参数。提供参数的名称、描述、是否必填等元数据，
 * 用于生成完整的 JSON Schema，帮助 LLM 正确理解参数含义并生成准确的 tool_calls。
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface McpParam {

    /** 参数名称 */
    String name();

    /** 参数描述 */
    String desc() default "";

    /** 是否必填，默认 true */
    boolean required() default true;
}
