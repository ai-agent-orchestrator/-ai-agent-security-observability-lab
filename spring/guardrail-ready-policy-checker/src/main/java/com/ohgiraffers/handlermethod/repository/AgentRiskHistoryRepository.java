package com.ohgiraffers.handlermethod.repository;

import com.ohgiraffers.handlermethod.entity.AgentRiskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRiskHistoryRepository extends JpaRepository<AgentRiskHistory, Long> {
}
