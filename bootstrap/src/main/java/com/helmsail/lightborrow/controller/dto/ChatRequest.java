package com.helmsail.lightborrow.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        String userId,
        String sessionId,
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 4000, message = "消息内容不能超过4000字") String content) {}
