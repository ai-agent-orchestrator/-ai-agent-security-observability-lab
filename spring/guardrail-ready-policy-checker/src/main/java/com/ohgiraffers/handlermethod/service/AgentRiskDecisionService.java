package com.ohgiraffers.handlermethod.service;

import com.ohgiraffers.handlermethod.dto.AgentRiskAnalyzeRequest;
import com.ohgiraffers.handlermethod.dto.AgentRiskAnalyzeResponse;
import com.ohgiraffers.handlermethod.entity.AgentRiskHistory;
import com.ohgiraffers.handlermethod.repository.AgentRiskHistoryRepository;
import com.ohgiraffers.handlermethod.support.AgentMetricRecorder;
import com.ohgiraffers.handlermethod.support.TraceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentRiskDecisionService {

    private final AgentMetricRecorder agentMetricRecorder;
    private final AgentRiskHistoryRepository agentRiskHistoryRepository;

    public AgentRiskDecisionService(AgentMetricRecorder agentMetricRecorder,
                                    AgentRiskHistoryRepository agentRiskHistoryRepository) {
        this.agentMetricRecorder = agentMetricRecorder;
        this.agentRiskHistoryRepository = agentRiskHistoryRepository;
    }

    @Transactional
    public AgentRiskAnalyzeResponse analyze(AgentRiskAnalyzeRequest request) {
        String userInput = valueOrDefault(request == null ? null : request.userInput(), "empty agent request");
        String toolName = valueOrDefault(request == null ? null : request.toolName(), "unknown");
        int retryCount = positiveOrDefault(request == null ? null : request.retryCount(), 0);
        int promptTokens = positiveOrDefault(request == null ? null : request.promptTokens(), 120);
        int completionTokens = positiveOrDefault(request == null ? null : request.completionTokens(), 80);

        boolean policyViolation = flag(request == null ? null : request.policyViolation())
                || containsAny(userInput, "delete", "customer records", "sensitive", "blocked");
        boolean toolError = flag(request == null ? null : request.toolError())
                || containsAny(userInput, "failing tool", "tool error", "unstable");
        boolean externalApiCall = flag(request == null ? null : request.externalApiCall())
                || containsAny(toolName, "external")
                || containsAny(userInput, "external api", "upload");
        boolean approvalRequired = flag(request == null ? null : request.approvalRequired())
                || containsAny(userInput, "email", "send customer report", "approval");
        boolean dbWrite = flag(request == null ? null : request.dbWrite())
                || containsAny(toolName, "database", "db");

        List<String> signals = new ArrayList<>();
        int riskScore = 0;

        if (policyViolation) {
            signals.add("POLICY_VIOLATION");
            riskScore += 40;
            agentMetricRecorder.recordPolicyViolation("risk_decision_policy_violation");
        }

        if (retryCount > 0) {
            signals.add("RETRY");
            riskScore += Math.min(retryCount * 8, 25);
            agentMetricRecorder.recordRetryCount(retryCount);
        }

        if (externalApiCall) {
            signals.add("EXTERNAL_API");
            riskScore += 25;
            agentMetricRecorder.recordExternalApiCall("risk-decision-external-api", policyViolation ? "risky" : "called");
        }

        if (toolError) {
            signals.add("TOOL_ERROR");
            riskScore += 15;
            agentMetricRecorder.recordToolError(toolName, "risk_decision_tool_error");
        }

        if (approvalRequired) {
            signals.add("APPROVAL_REQUIRED");
            riskScore += 20;
            agentMetricRecorder.recordApprovalRequired("risk_decision_approval_boundary");
        }

        if (dbWrite) {
            signals.add("DB_WRITE");
            riskScore += 15;
            agentMetricRecorder.recordDbWrite("risk-decision-db", policyViolation ? "risky" : "attempted");
        }

        if (signals.isEmpty()) {
            signals.add("SAFE_REQUEST");
        }

        agentMetricRecorder.recordToolCall(toolName, "risk_decision_analyze");
        agentMetricRecorder.recordTokenCost(promptTokens, completionTokens);

        int boundedRiskScore = Math.min(riskScore, 100);
        String riskLevel = riskLevel(boundedRiskScore);
        String decision = decision(signals, boundedRiskScore);
        String recommendedAction = recommendedAction(decision, riskLevel);
        String traceId = TraceContext.currentTraceId();

        AgentRiskHistory history = AgentRiskHistory.create(
                userInput,
                toolName,
                decision,
                boundedRiskScore,
                riskLevel,
                String.join(",", signals),
                recommendedAction,
                true,
                traceId
        );
        AgentRiskHistory savedHistory = agentRiskHistoryRepository.save(history);

        return new AgentRiskAnalyzeResponse(
                decision,
                boundedRiskScore,
                riskLevel,
                signals,
                recommendedAction,
                true,
                savedHistory.getId(),
                traceId
        );
    }

    private String decision(List<String> signals, int riskScore) {
        if (signals.contains("POLICY_VIOLATION")
                && signals.contains("EXTERNAL_API")
                && signals.contains("RETRY")) {
            return "HIGH_RISK_AGENT_BEHAVIOR";
        }

        if (signals.contains("POLICY_VIOLATION") && signals.contains("EXTERNAL_API")) {
            return "RISKY_EXTERNAL_ACCESS";
        }

        if (signals.contains("POLICY_VIOLATION") && signals.contains("RETRY")) {
            return "SUSPICIOUS_RETRY";
        }

        if (signals.contains("APPROVAL_REQUIRED") && signals.contains("RETRY")) {
            return "APPROVAL_BYPASS_RISK";
        }

        if (signals.contains("TOOL_ERROR") && signals.contains("RETRY")) {
            return "UNSTABLE_TOOL_LOOP";
        }

        if (riskScore >= 60) {
            return "SUSPICIOUS";
        }

        return "SAFE";
    }

    private String riskLevel(int riskScore) {
        if (riskScore >= 80) {
            return "HIGH";
        }

        if (riskScore >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String recommendedAction(String decision, String riskLevel) {
        if ("HIGH_RISK_AGENT_BEHAVIOR".equals(decision) || "RISKY_EXTERNAL_ACCESS".equals(decision)) {
            return "BLOCK_AND_ESCALATE";
        }

        if ("SUSPICIOUS_RETRY".equals(decision) || "APPROVAL_BYPASS_RISK".equals(decision)) {
            return "BLOCK_OR_REQUIRE_APPROVAL";
        }

        if ("UNSTABLE_TOOL_LOOP".equals(decision)) {
            return "DISABLE_TOOL_TEMPORARILY";
        }

        if ("HIGH".equals(riskLevel)) {
            return "CREATE_INCIDENT";
        }

        if ("MEDIUM".equals(riskLevel)) {
            return "REVIEW";
        }

        return "ALLOW";
    }

    private boolean containsAny(String value, String... keywords) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private boolean flag(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private int positiveOrDefault(Integer value, int defaultValue) {
        if (value == null || value < 0) {
            return defaultValue;
        }

        return value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
