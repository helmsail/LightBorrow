package com.helmsail.lightborrow.mcp.registry;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDefinitionTest {

    @Test
    void shouldCreateToolDefinition() {
        ToolDefinition.ToolExecutor executor = args -> "executed: " + args;
        ToolDefinition def = new ToolDefinition("test_tool", "A test tool",
                Map.of("type", "object"), executor);

        assertThat(def.getName()).isEqualTo("test_tool");
        assertThat(def.getDescription()).isEqualTo("A test tool");
        assertThat(def.getParameters()).containsEntry("type", "object");
    }

    @Test
    void executeShouldDelegateToExecutor() {
        ToolDefinition.ToolExecutor executor = args -> {
            Object input = args.get("input");
            return "Result: " + input;
        };
        ToolDefinition def = new ToolDefinition("echo", "echo tool",
                Map.of(), executor);

        String result = def.execute(Map.of("input", "hello"));
        assertThat(result).isEqualTo("Result: hello");
    }

    @Test
    void executeShouldHandleEmptyArgs() {
        ToolDefinition.ToolExecutor executor = args -> "done";
        ToolDefinition def = new ToolDefinition("noop", "no-op tool",
                Map.of(), executor);

        String result = def.execute(Map.of());
        assertThat(result).isEqualTo("done");
    }
}
