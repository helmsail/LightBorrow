package com.helmsail.lightborrow.core.rewrite;

import com.helmsail.lightborrow.core.model.ConversationContext;

public interface RewriteStage {

    /**
     * 执行重写处理。
     *
     * @param ctx 当前对话上下文
     */
    void rewrite(ConversationContext ctx);
}
