package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PromptTemplateServiceTest {

    private ToolRegistry toolRegistry;
    private PromptTemplateService service;

    @BeforeEach
    void setUp() {
        toolRegistry = mock(ToolRegistry.class);
        service = new PromptTemplateService(toolRegistry);
    }

    @Test
    void shouldRenderSimpleTemplate() {
        String result = service.getRaw("prompts/rewrite-prompt.md");
        assertThat(result).contains("对话输入重写助手");
    }

    @Test
    void shouldReturnEmptyForMissingTemplate() {
        String result = service.getRaw("nonexistent.md");
        assertThat(result).isEmpty();
    }
}
