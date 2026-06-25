package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RewritePipeline {

    private final List<RewriteStage> stages;

    public RewritePipeline(List<RewriteStage> stages) {
        this.stages = stages;
    }

    /**
     * 执行完整重写流程。
     *
     * @param ctx 当前对话上下文
     */
    public void execute(ConversationContext ctx) {
        log.debug("[Core] 输入重写开始 userId={}", ctx.getUserId());
        for (RewriteStage stage : stages) {
            stage.rewrite(ctx);
        }
        log.debug("[Core] 输入重写完成: '{}' -> '{}'",
                ctx.getUserInput(), ctx.getRewrittenInput());
    }
}
