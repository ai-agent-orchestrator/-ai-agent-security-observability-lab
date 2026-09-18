package com.ohgiraffers.handlermethod.service;

import com.ohgiraffers.handlermethod.dto.AdminDashboardSummaryResponse;
import com.ohgiraffers.handlermethod.dto.SecurityEventHistoryResponse;
import com.ohgiraffers.handlermethod.repository.AgentRunHistoryRepository;
import com.ohgiraffers.handlermethod.repository.AgentRiskHistoryRepository;
import com.ohgiraffers.handlermethod.repository.SecurityEventHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDashboardService {

    private final AgentRunHistoryRepository agentRunHistoryRepository;
    private final AgentRiskHistoryRepository agentRiskHistoryRepository;
    private final SecurityEventHistoryRepository securityEventHistoryRepository;
    private final AgentRiskHistoryQueryService agentRiskHistoryQueryService;

    public AdminDashboardService(AgentRunHistoryRepository agentRunHistoryRepository,
                                 AgentRiskHistoryRepository agentRiskHistoryRepository,
                                 SecurityEventHistoryRepository securityEventHistoryRepository,
                                 AgentRiskHistoryQueryService agentRiskHistoryQueryService) {
        this.agentRunHistoryRepository = agentRunHistoryRepository;
        this.agentRiskHistoryRepository = agentRiskHistoryRepository;
        this.securityEventHistoryRepository = securityEventHistoryRepository;
        this.agentRiskHistoryQueryService = agentRiskHistoryQueryService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse summary() {
        return new AdminDashboardSummaryResponse(
                agentRunHistoryRepository.count(),
                agentRiskHistoryRepository.count(),
                securityEventHistoryRepository.count(),
                agentRiskHistoryQueryService.summarize()
        );
    }

    @Transactional(readOnly = true)
    public List<SecurityEventHistoryResponse> recentSecurityEvents() {
        return securityEventHistoryRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(SecurityEventHistoryResponse::from)
                .toList();
    }
}
