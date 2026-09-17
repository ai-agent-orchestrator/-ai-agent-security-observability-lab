package com.ohgiraffers.handlermethod.service;

import com.ohgiraffers.handlermethod.dto.AgentPracticeRequest;
import com.ohgiraffers.handlermethod.dto.AgentPracticeResponse;
import com.ohgiraffers.handlermethod.support.AgentMetricRecorder;
import com.ohgiraffers.handlermethod.support.TraceContext;
import org.springframework.stereotype.Service;

@Service
public class AgentPracticeService {

    private final AgentMetricRecorder agentMetricRecorder;

    public AgentPracticeService(AgentMetricRecorder agentMetricRecorder) {
        this.agentMetricRecorder = agentMetricRecorder;
    }

    public AgentPracticeResponse run(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 3);
        int retryCount = retryCount(request, 0);

        agentMetricRecorder.recordToolCall(toolName, "success");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 120), completionTokens(request, 80));

        return response(
                "run",
                "ALLOWED",
                "Simulated agent run completed.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse toolError(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 2);
        int retryCount = retryCount(request, 1);

        agentMetricRecorder.recordToolCall(toolName, "error");
        agentMetricRecorder.recordToolError(toolName, "simulated_tool_failure");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 80), completionTokens(request, 20));

        return response(
                "tool-error",
                "FAILED",
                "Simulated agent tool failure recorded.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse retry(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 5);
        int retryCount = retryCount(request, 3);

        agentMetricRecorder.recordToolCall(toolName, "retried");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 140), completionTokens(request, 90));

        return response(
                "retry",
                "RETRIED",
                "Simulated agent retry behavior recorded.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse approval(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 4);
        int retryCount = retryCount(request, 0);

        agentMetricRecorder.recordToolCall(toolName, "approval_required");
        agentMetricRecorder.recordApprovalRequired("sensitive_action");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 160), completionTokens(request, 60));

        return response(
                "approval",
                "APPROVAL_REQUIRED",
                "Simulated sensitive action requires human approval.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse policyViolation(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 1);
        int retryCount = retryCount(request, 0);

        agentMetricRecorder.recordToolCall(toolName, "denied");
        agentMetricRecorder.recordPolicyViolation("mock_guardrail_policy");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 60), completionTokens(request, 10));

        return response(
                "policy-violation",
                "DENIED",
                "Simulated guardrail-ready policy violation recorded.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse externalApi(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 3);
        int retryCount = retryCount(request, 0);

        agentMetricRecorder.recordToolCall(toolName, "external_api");
        agentMetricRecorder.recordExternalApiCall("mock-llm-provider", "success");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 200), completionTokens(request, 120));

        return response(
                "external-api",
                "ALLOWED",
                "Simulated external API call recorded.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    public AgentPracticeResponse dbWrite(AgentPracticeRequest request) {
        String toolName = toolName(request);
        int planSteps = planSteps(request, 2);
        int retryCount = retryCount(request, 0);

        agentMetricRecorder.recordToolCall(toolName, "db_write");
        agentMetricRecorder.recordDbWrite("agent_memory", "success");
        agentMetricRecorder.recordPlanSteps(planSteps);
        agentMetricRecorder.recordRetryCount(retryCount);
        agentMetricRecorder.recordTokenCost(promptTokens(request, 100), completionTokens(request, 40));

        return response(
                "db-write",
                "ALLOWED",
                "Simulated agent database write recorded.",
                toolName,
                planSteps,
                retryCount,
                request
        );
    }

    private AgentPracticeResponse response(String mode,
                                           String decision,
                                           String message,
                                           String toolName,
                                           int planSteps,
                                           int retryCount,
                                           AgentPracticeRequest request) {
        return new AgentPracticeResponse(
                mode,
                decision,
                message,
                toolName,
                planSteps,
                retryCount,
                promptTokens(request, 0) + completionTokens(request, 0),
                TraceContext.currentTraceId()
        );
    }

    private String toolName(AgentPracticeRequest request) {
        if (request == null || request.toolName() == null || request.toolName().isBlank()) {
            return "search";
        }

        return request.toolName();
    }

    private int planSteps(AgentPracticeRequest request, int defaultValue) {
        if (request == null || request.planSteps() <= 0) {
            return defaultValue;
        }

        return request.planSteps();
    }

    private int retryCount(AgentPracticeRequest request, int defaultValue) {
        if (request == null || request.retryCount() < 0) {
            return defaultValue;
        }

        return request.retryCount();
    }

    private int promptTokens(AgentPracticeRequest request, int defaultValue) {
        if (request == null || request.promptTokens() <= 0) {
            return defaultValue;
        }

        return request.promptTokens();
    }

    private int completionTokens(AgentPracticeRequest request, int defaultValue) {
        if (request == null || request.completionTokens() <= 0) {
            return defaultValue;
        }

        return request.completionTokens();
    }
}
