package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.config.CoreProperties;
import com.helmsail.lightborrow.core.model.ConversationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RewritePipeline {

    private final List<RewriteStage> stages;
    private final CoreProperties coreProperties;

    public RewritePipeline(List<RewriteStage> stages, CoreProperties coreProperties) {
        this.stages = stages;
        this.coreProperties = coreProperties;
    }

    public void execute(ConversationContext ctx) {
        if (!coreProperties.isEnableRewrite()) {
            log.debug("[Core] 输入重写已禁用");
            return;
        }

        log.debug("[Core] 输入重写开始 userId={}", ctx.getUserId());
        for (RewriteStage stage : stages) {
            stage.rewrite(ctx);
        }
        log.debug("[Core] 输入重写完成: '{}' -> '{}'",
                ctx.getUserInput(), ctx.getRewrittenInput());
    }
}
