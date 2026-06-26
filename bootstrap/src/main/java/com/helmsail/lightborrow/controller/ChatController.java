package com.helmsail.lightborrow.controller;

import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.AgentResult;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.model.Result;
import com.helmsail.lightborrow.framework.ratelimit.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Web Chat 聊天 API。提供浏览器端直接使用的对话接口。
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final AgentLoop agentLoop;
    private final RateLimiter rateLimiter;

    public ChatController(AgentLoop agentLoop, RateLimiter rateLimiter) {
        this.agentLoop = agentLoop;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String userId = request.userId() != null ? request.userId() : "web-user";

        // 限流
        if (!rateLimiter.allowRequest(userId)) {
            return Result.error(ErrorCode.TOO_MANY_REQUESTS);
        }

        log.info("[Chat] userId={}, content={}", userId, request.content());
        AgentResult result = agentLoop.process(userId, request.content());
        log.info("[Chat] userId={}, type={}", userId, result.getType());

        return Result.success(new ChatResponse(result.getType(), result.getContent()));
    }

    /** 聊天请求体 */
    public record ChatRequest(
            String userId,
            @NotBlank(message = "消息内容不能为空")
            @Size(max = 4000, message = "消息内容不能超过4000字") String content) {}

    /** 聊天响应体 */
    public record ChatResponse(String type, String content) {}
}
