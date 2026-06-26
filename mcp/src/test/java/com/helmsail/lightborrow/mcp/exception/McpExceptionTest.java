package com.helmsail.lightborrow.mcp.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class McpExceptionTest {

    @Test
    void shouldCreateWithErrorCode() {
        McpException e = new McpException(ErrorCode.MCP_TOOL_NOT_FOUND, "test_tool");
        assertThat(e.getCode()).isEqualTo(ErrorCode.MCP_TOOL_NOT_FOUND.getCode());
        assertThat(e.getMessage()).isEqualTo(ErrorCode.MCP_TOOL_NOT_FOUND.getMessage());
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("connection error");
        McpException e = new McpException(ErrorCode.MCP_TOOL_EXECUTION_FAILED, cause, "my_tool");
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(ErrorCode.MCP_TOOL_EXECUTION_FAILED.getCode());
    }
}
