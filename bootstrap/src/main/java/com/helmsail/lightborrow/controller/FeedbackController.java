package com.helmsail.lightborrow.controller;

import com.helmsail.lightborrow.controller.dto.FeedbackRequest;
import com.helmsail.lightborrow.memory.service.FeedbackService;
import com.helmsail.lightborrow.framework.model.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback")
    public Result<Void> feedback(@Valid @RequestBody FeedbackRequest request) {
        feedbackService.record(
                request.userId(),
                request.sessionId(),
                request.rating(),
                request.comment());
        return Result.success();
    }
}
