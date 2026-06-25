package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CleanRewriteStage implements RewriteStage {

    @Override
    public void rewrite(ConversationContext ctx) {
        String input = ctx.getUserInput();
        if (input == null || input.isBlank()) {
            return;
        }

        // 去除首尾空白
        String cleaned = input.trim();

        // 去除多余空白（保留单空格）
        cleaned = cleaned.replaceAll("\\s+", " ");

        ctx.setRewrittenInput(cleaned);
        log.debug("[Core] CleanStage: '{}' -> '{}'", input, cleaned);
    }
}
