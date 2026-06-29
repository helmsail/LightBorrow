package com.helmsail.lightborrow.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.helmsail.lightborrow.controller.dto.ChatRequest;
import com.helmsail.lightborrow.controller.dto.ChatResponse;
import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.AgentResult;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.model.Result;
import com.helmsail.lightborrow.framework.sentinel.SentinelAutoConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final AgentLoop agentLoop;
    private final HttpServletRequest httpServletRequest;

    public ChatController(AgentLoop agentLoop, HttpServletRequest httpServletRequest) {
        this.agentLoop = agentLoop;
        this.httpServletRequest = httpServletRequest;
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String userId = resolveUserId(request);
        try (Entry entry = SphU.entry(SentinelAutoConfiguration.RESOURCE_CHAT_API, EntryType.IN, 1, userId)) {
            logAudit(userId, request.content());
            AgentResult result = agentLoop.process(userId, request.sessionId(), request.content());
            log.info("[Chat] userId={}, type={}", userId, result.getType());
            return Result.success(new ChatResponse(result.getType(), result.getContent()));
        } catch (BlockException e) {
            return Result.error(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String userId,
            @RequestParam(required = false) String sessionId,
            @RequestParam @NotBlank String content) {
        try (Entry entry = SphU.entry(SentinelAutoConfiguration.RESOURCE_CHAT_API, EntryType.IN, 1, userId)) {
            logAudit(userId, content);
            SseEmitter emitter = new SseEmitter(300_000L);

            CompletableFuture.runAsync(() -> {
                try {
                    agentLoop.process(userId, sessionId, content, (progress) -> {
                        try {
                            emitter.send(SseEmitter.event().name("progress").data(progress));
                        } catch (Exception e) {
                            log.warn("[SSE] 发送失败 userId={}", userId, e);
                        }
                    });
                    emitter.complete();
                } catch (Exception e) {
                    log.error("[SSE] 处理异常 userId={}", userId, e);
                    emitter.completeWithError(e);
                }
            });

            return emitter;
        } catch (BlockException e) {
            SseEmitter emitter = new SseEmitter(300_000L);
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("请求过于频繁，请稍后再试"));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            });
            return emitter;
        }
    }

    private String resolveUserId(ChatRequest request) {
        return request.userId() != null ? request.userId() : UUID.randomUUID().toString();
    }

    private void logAudit(String userId, String content) {
        String ip = httpServletRequest.getRemoteAddr();
        String ua = httpServletRequest.getHeader("User-Agent");
        log.info("[Chat] userId={}, content={}, ip={}", userId, content, ip);
        log.debug("[Chat] userId={}, ua={}", userId, ua);
    }
}
