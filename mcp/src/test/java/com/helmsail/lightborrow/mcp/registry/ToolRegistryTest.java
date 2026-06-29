package com.helmsail.lightborrow.mcp.registry;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.mcp.exception.McpException;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolRegistryTest {

    @Test
    void shouldThrowMcpExceptionForUnknownTool() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThatThrownBy(() -> registry.getTool("nonexistent"))
                .isInstanceOf(McpException.class)
                .satisfies(e -> assertThat(((McpException) e).getCode())
                        .isEqualTo(ErrorCode.MCP_TOOL_NOT_FOUND.getCode()));
    }

    @Test
    void hasToolShouldReturnFalseForUnknownTool() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThat(registry.hasTool("nonexistent")).isFalse();
    }

    @Test
    void hasToolShouldReturnFalseForEmptyRegistry() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThat(registry.hasTool("nonexistent")).isFalse();
    }

    @Test
    void invokeShouldThrowMcpExceptionForUnknownTool() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThatThrownBy(() -> registry.invoke("nonexistent", Map.of()))
                .isInstanceOf(McpException.class);
    }

    @Test
    void getToolDefinitionsShouldReturnEmptyListForEmptyRegistry() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThat(registry.getToolDefinitions()).isEmpty();
    }

    @Test
    void getToolDefinitionsShouldReturnImmutableList() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getBeanDefinitionNames()).thenReturn(new String[0]);

        ToolRegistry registry = new ToolRegistry(appCtx);
        registry.onContextRefreshed(null);

        assertThatThrownBy(() -> registry.getToolDefinitions().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
