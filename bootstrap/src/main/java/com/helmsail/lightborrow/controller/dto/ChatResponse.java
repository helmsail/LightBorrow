package com.helmsail.lightborrow.controller.dto;

import com.helmsail.lightborrow.core.agent.AgentResultType;

public record ChatResponse(AgentResultType type, String content) {}
