package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.mcp.registry.ToolDefinition;
import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板服务。从 classpath 加载 .md 模板文件，用 {{var}} 占位符替换变量。
 *
 * <p>模板存储在 {@code core/src/main/resources/prompts/} 目录下。
 * 修改模板文件无需重新编译，重启即可（生产环境可配合配置中心动态加载）。
 */
@Slf4j
public class PromptTemplateService {

    private final ToolRegistry toolRegistry;
    private final Map<String, String> cache = new ConcurrentHashMap<>(4);

    public PromptTemplateService(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    /**
     * 渲染指定模板，填充 ConversationContext 中的变量。
     *
     * @param templatePath classpath 路径，如 "prompts/system-prompt.md"
     * @param ctx          对话上下文
     * @return 渲染后的文本
     */
    public String render(String templatePath, ConversationContext ctx) {
        String template = loadTemplate(templatePath);
        String result = template;

        // {{tools}} — 工具列表
        result = result.replace("{{tools}}", renderTools());

        // {{userInfo}} — 用户信息
        StringBuilder userInfo = new StringBuilder();
        userInfo.append("- 用户ID: ").append(ctx.getUserId()).append("\n");
        if (ctx.getMemoryContext() != null && ctx.getMemoryContext().getProfileSummary() != null) {
            userInfo.append("- ").append(ctx.getMemoryContext().getProfileSummary()).append("\n");
        }
        result = result.replace("{{userInfo}}", userInfo.toString());

        // {{history}} — 对话历史
        StringBuilder history = new StringBuilder();
        if (ctx.getMemoryContext() != null
                && ctx.getMemoryContext().getHistoryMessages() != null
                && !ctx.getMemoryContext().getHistoryMessages().isEmpty()) {
            history.append("## 对话历史（最近 ")
                    .append(ctx.getMemoryContext().getHistoryMessages().size())
                    .append(" 条）\n");
            for (String msg : ctx.getMemoryContext().getHistoryMessages()) {
                history.append(msg).append("\n");
            }
        }
        result = result.replace("{{history}}", history.toString());

        // {{longTermMemory}} — 长期记忆
        StringBuilder memoryInfo = new StringBuilder();
        if (ctx.getLongTermMemories() != null && !ctx.getLongTermMemories().isEmpty()) {
            memoryInfo.append("## 关于你的历史记忆\n");
            for (String mem : ctx.getLongTermMemories()) {
                memoryInfo.append(mem).append("\n");
            }
        }
        result = result.replace("{{longTermMemory}}", memoryInfo.toString());

        return result;
    }

    /** 渲染工具列表。 */
    private String renderTools() {
        List<ToolDefinition> tools = toolRegistry.getToolDefinitions();
        StringBuilder sb = new StringBuilder();
        for (ToolDefinition tool : tools) {
            sb.append("- ").append(tool.getName())
                    .append(": ").append(tool.getDescription()).append("\n");
        }
        return sb.toString();
    }

    /** 从 classpath 加载模板，带缓存。 */
    private String loadTemplate(String path) {
        return cache.computeIfAbsent(path, p -> {
            try {
                ClassPathResource resource = new ClassPathResource(p);
                if (!resource.exists()) {
                    log.warn("[Prompt] 模板不存在: {}, 使用默认提示", p);
                    return "";
                }
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                log.info("[Prompt] 加载模板: {} ({} bytes)", p, content.length());
                return content;
            } catch (IOException e) {
                log.error("[Prompt] 模板加载失败: {}", p, e);
                return "";
            }
        });
    }

    /** 清空缓存（用于热加载）。 */
    public void clearCache() {
        cache.clear();
        log.info("[Prompt] 模板缓存已清空");
    }

    /**
     * 获取模板原始内容，不做变量替换。
     */
    public String getRaw(String templatePath) {
        return loadTemplate(templatePath);
    }
}
