package com.helmsail.lightborrow.rag.strategy.retrieval;

import com.helmsail.lightborrow.rag.model.DocumentChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 引文生成器。为 RAG 回答添加来源引用标注。
 */
@Slf4j
public class CitationGenerator {

    /**
     * 构建带引文的 Prompt 上下文。
     *
     * @param chunks 检索到的文档块
     * @return 带引文标记的引用文本
     */
    public String buildCitedContext(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            String source = chunk.getDocumentId() != null && !chunk.getDocumentId().isBlank()
                    ? chunk.getDocumentId()
                    : "来源" + (i + 1);
            sb.append("[").append(i + 1).append("] ")
                    .append(chunk.getContent()).append("\n")
                    .append("   ── 来自: ").append(source).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 获取要求引文标注的 System Prompt 片段。
     */
    public String getCitationInstruction() {
        return """
                ## 回答要求
                1. 在回答中标注信息来源，使用 [1]、[2] 格式标注对应的参考资料编号
                2. 如果回答内容来自多个来源，分别标注
                3. 如果参考资料不足以回答，请如实告知，不要编造
                4. 保持回答简洁准确""";
    }
}
