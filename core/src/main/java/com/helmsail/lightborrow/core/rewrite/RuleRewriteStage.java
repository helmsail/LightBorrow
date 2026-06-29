package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 规则驱动的输入重写阶段。
 *
 * <p>放在 CleanRewriteStage 之后、LlmRewriteStage 之前，通过规则判断是否真正需要 LLM 重写：
 * <ul>
 *   <li>输入包含明确动宾结构（如"借 MacBook"）→ 跳过 LLM 重写，节省 Token</li>
 *   <li>输入包含指代代词（它、那个、这个）→ 放行给 LLM 消除歧义</li>
 *   <li>输入很短（< 5 字符）或只有语气词 → 跳过 LLM 重写</li>
 * </ul>
 */
@Slf4j
public class RuleRewriteStage implements RewriteStage {

    /** 指代代词：需要 LLM 消除歧义 */
    private static final Pattern PRONOUN_PATTERN = Pattern.compile(
            "(它|他|她|它们|他们|她们|那个|这个|哪些|这些|那些|刚才|之前|上面|上述)");

    /** 明确动宾模式：动词 + 名词，不需要重写 */
    private static final Pattern VERB_NOUN_PATTERN = Pattern.compile(
            "^(借|查|看|找|还|转|申请|取消|确认|查询|查看|搜索|搜索一下|帮我查|我要借|我想借|我想查).{2,}");

    /** 纯语气词或太短 */
    private static final Pattern SHORT_INPUT_PATTERN = Pattern.compile(
            "^.{1,4}$");

    @Override
    public void rewrite(ConversationContext ctx) {
        String input = ctx.getRewrittenInput() != null
                ? ctx.getRewrittenInput() : ctx.getUserInput();

        if (input == null || input.isBlank()) {
            return;
        }

        // 太短 → 不重写
        if (SHORT_INPUT_PATTERN.matcher(input).matches()) {
            log.debug("[Core] RuleRewriteStage: 输入太短，跳过 LLM 重写: '{}'", input);
            ctx.setRequireRewrite(false);
            return;
        }

        // 包含指代代词 → 需要 LLM 重写
        if (PRONOUN_PATTERN.matcher(input).find()) {
            log.debug("[Core] RuleRewriteStage: 检测到指代代词，需要 LLM 重写: '{}'", input);
            ctx.setRequireRewrite(true);
            return;
        }

        // 明确的动宾结构 → 不需要重写
        if (VERB_NOUN_PATTERN.matcher(input).find()) {
            log.debug("[Core] RuleRewriteStage: 明确的动宾结构，跳过 LLM 重写: '{}'", input);
            ctx.setRequireRewrite(false);
            return;
        }

        // 无历史对话 → 不重写
        if (ctx.getMemoryContext() == null
                || ctx.getMemoryContext().getHistoryMessages() == null
                || ctx.getMemoryContext().getHistoryMessages().isEmpty()) {
            log.debug("[Core] RuleRewriteStage: 无对话历史，跳过 LLM 重写");
            ctx.setRequireRewrite(false);
            return;
        }

        // 其他情况保守起见，放行给 LLM
        ctx.setRequireRewrite(true);
    }
}
