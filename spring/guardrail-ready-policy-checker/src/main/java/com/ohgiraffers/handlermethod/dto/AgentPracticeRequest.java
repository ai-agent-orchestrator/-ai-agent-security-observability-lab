package com.ohgiraffers.handlermethod.dto;

public record AgentPracticeRequest(
        String userInput,
        String toolName,
        int planSteps,
        int retryCount,
        int promptTokens,
        int completionTokens
) {
}
