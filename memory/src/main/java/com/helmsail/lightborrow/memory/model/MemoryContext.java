package com.helmsail.lightborrow.memory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryContext {

    private String userId;
    private String sessionId;
    private SessionState sessionState;
    private List<String> historyMessages;
    private String profileSummary;
    private boolean newSession;
}
