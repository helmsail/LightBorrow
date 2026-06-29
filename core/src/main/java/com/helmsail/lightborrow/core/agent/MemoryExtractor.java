package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 记忆提取器。用 LLM 从对话中提取关键信息。
 *
 * <p>提取内容：用户常用资产、偏好、行为模式等。
 * 输出格式：每行一条记忆。
 */
@Slf4j
public class MemoryExtractor {

    private static final String EXTRACT_PROMPT = """
            从以下对话中提取关于用户的关键信息，如常用资产、偏好、行为模式、重要事件等。
            要求：
            1. 每行输出一条记忆，格式：- 记忆内容
            2. 只输出有实际价值的长期信息，不要输出临时性或一次性的对话内容
            3. 如果对话中没有值得长期记住的信息，输出"无"
            4. 用中文输出
            """;

    private final ChatModel chatModel;

    public MemoryExtractor(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 从对话中提取记忆。
     *
     * @param userInput   用户输入
     * @param finalAnswer Agent 最终回答
     * @return 提取出的记忆列表
     */
    public List<String> extract(String userInput, String finalAnswer) {
        if (userInput == null || finalAnswer == null) return List.of();

        String dialogue = "用户: " + userInput + "\n助手: " + finalAnswer;

        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(EXTRACT_PROMPT),
                        ChatMessage.user(dialogue)))
                .temperature(0.3)
                .maxTokens(512)
                .stream(false)
                .build();

        try {
            var response = chatModel.chat(request);
            if (response == null || response.content() == null) return List.of();

            String content = response.content().trim();
            if ("无".equals(content) || content.isBlank()) return List.of();

            return parseMemories(content);
        } catch (Exception e) {
            log.warn("[MemoryExtractor] 提取失败", e);
            return List.of();
        }
    }

    /** 解析 LLM 输出，提取每行记忆。 */
    private List<String> parseMemories(String content) {
        List<String> memories = new ArrayList<>();
        Pattern pattern = Pattern.compile("^-\\s*(.+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String memory = matcher.group(1).trim();
            if (!memory.isBlank()) {
                memories.add(memory);
            }
        }
        // 如果正则没匹配到但内容不为空，整段作为一条记忆
        if (memories.isEmpty() && !content.isBlank()) {
            memories.add(content);
        }
        return memories;
    }
}
