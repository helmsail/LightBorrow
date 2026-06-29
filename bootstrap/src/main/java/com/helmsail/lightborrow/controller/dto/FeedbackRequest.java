package com.helmsail.lightborrow.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record FeedbackRequest(
        @NotBlank String userId,
        @NotBlank String sessionId,
        @NotBlank String rating,
        String comment) {}
