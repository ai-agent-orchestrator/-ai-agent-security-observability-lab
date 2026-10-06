package com.ohgiraffers.handlermethod.service;

import com.ohgiraffers.handlermethod.dto.AiChatRequest;
import com.ohgiraffers.handlermethod.dto.AiChatResponse;
import com.ohgiraffers.handlermethod.guardrail.InputGuardrailClient;
import com.ohgiraffers.handlermethod.guardrail.InputGuardrailResult;
import com.ohgiraffers.handlermethod.guardrail.OutputGuardrailClient;
import com.ohgiraffers.handlermethod.guardrail.OutputGuardrailResult;
import com.ohgiraffers.handlermethod.llm.LlmClient;
import com.ohgiraffers.handlermethod.support.AiMetricRecorder;
import com.ohgiraffers.handlermethod.support.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final String FALLBACK_MESSAGE =
            "현재 요청을 안전하게 처리할 수 없습니다. 잠시 후 다시 시도하거나 전문가에게 상담받아 주세요.";

    private final InputGuardrailClient inputGuardrailClient;
    private final OutputGuardrailClient outputGuardrailClient;
    private final LlmClient llmClient;
    private final AiMetricRecorder metricRecorder;
    private final String systemPrompt;

    public AiService(
            InputGuardrailClient inputGuardrailClient,
            OutputGuardrailClient outputGuardrailClient,
            LlmClient llmClient,
            AiMetricRecorder metricRecorder,
            @Value("${llm.system-prompt}") String systemPrompt
    ) {
        this.inputGuardrailClient = inputGuardrailClient;
        this.outputGuardrailClient = outputGuardrailClient;
        this.llmClient = llmClient;
        this.metricRecorder = metricRecorder;
        this.systemPrompt = systemPrompt;
    }

    public AiChatResponse chat(AiChatRequest request) {
        String traceId = TraceContext.currentTraceId();
        var timer = metricRecorder.start();

        try {
            InputGuardrailResult input = inputGuardrailClient.check(request.message());
            if (!input.allowed()) {
                metricRecorder.recordRequest("input_blocked", "none");
                return AiChatResponse.fallback(
                        input.content(), traceId, "none", "INPUT_GUARDRAIL_BLOCKED");
            }

            LlmClient.LlmResponse llm = llmClient.chat(systemPrompt, input.content());
            metricRecorder.recordTokens(llm.model(), llm.promptTokens(), llm.completionTokens());

            OutputGuardrailResult output = outputGuardrailClient.check(llm.content());
            if (!output.allowed()) {
                metricRecorder.recordRequest("output_blocked", llm.model());
                return AiChatResponse.fallback(
                        output.content(), traceId, llm.model(), "OUTPUT_GUARDRAIL_BLOCKED");
            }

            metricRecorder.recordRequest("success", llm.model());
            return AiChatResponse.success(output.content(), traceId, llm.model());
        } catch (RuntimeException exception) {
            metricRecorder.recordRequest("fallback", "unknown");
            return AiChatResponse.technicalFallback(
                    FALLBACK_MESSAGE, traceId, "unknown", exception.getClass().getSimpleName());
        } finally {
            metricRecorder.stop(timer);
        }
    }
}
