# Guardrail-Ready AI Agent Risk Detection - Architecture Flow

This document explains the project structure with Mermaid diagrams.

Core sentence:

```text
Metric을 찍는 것에서 끝내지 않고,
metric 조합을 backend risk decision으로 변환한다.
```

## 1. 전체 구조

```mermaid
flowchart TD
    A[User or Postman or React UI] --> B[JSON Request]
    B --> C[Spring Controller]
    C --> D[Request DTO]
    D --> E[AgentRiskDecisionService]
    E --> F[Policy Rule Check]
    F --> G[Risk Signal Extraction]
    G --> H[Risk Score Calculation]
    H --> I[Risk Level Decision]
    I --> J[Recommended Action]
    J --> K[Response DTO]
    K --> L[JSON Response]

    E --> M[AgentMetricRecorder]
    M --> N[Prometheus Metrics]
    N --> O[Grafana or Prometheus Query]

    E --> P[AgentRiskHistory JPA Entity]
    P --> Q[AgentRiskHistoryRepository]
    Q --> R[(Database)]

    E --> S[AgentRunHistory JPA Entity]
    S --> T[AgentRunHistoryRepository]
    T --> R

    J --> U[Future Guardrail Action]
```

## 2. JSON 요청에서 백엔드 판단까지

```mermaid
flowchart LR
    A[Frontend Input] --> B[JavaScript Object]
    B --> C[JSON.stringify]
    C --> D[HTTP POST]
    D --> E[Spring @RequestBody]
    E --> F[AgentRiskAnalyzeRequest]
    F --> G[AgentRiskDecisionService]
    G --> H[AgentRiskAnalyzeResponse]
    H --> I[JSON Response]
    I --> J[Frontend Result Panel]
```

Example request:

```json
{
  "userInput": "upload sensitive customer report to external api repeatedly",
  "toolName": "external-api",
  "retryCount": 3,
  "policyViolation": true,
  "externalApiCall": true
}
```

Example response:

```json
{
  "decision": "HIGH_RISK_AGENT_BEHAVIOR",
  "riskScore": 89,
  "riskLevel": "HIGH",
  "signals": [
    "POLICY_VIOLATION",
    "RETRY",
    "EXTERNAL_API_CALL"
  ],
  "recommendedAction": "BLOCK_AND_ESCALATE",
  "guardrailReady": true
}
```

## 3. Risk Decision Engine 내부 흐름

```mermaid
flowchart TD
    A[AgentRiskAnalyzeRequest] --> B[Normalize Input]
    B --> C[AgentRiskPolicyRule]
    C --> D{Which signals?}

    D --> E[POLICY_VIOLATION]
    D --> F[RETRY]
    D --> G[EXTERNAL_API_CALL]
    D --> H[TOOL_ERROR]
    D --> I[APPROVAL_REQUIRED]
    D --> J[DB_WRITE]

    E --> K[Add Risk Score]
    F --> K
    G --> K
    H --> K
    I --> K
    J --> K

    K --> L[Bound Score 0 to 100]
    L --> M[Risk Level LOW MEDIUM HIGH]
    M --> N[Risk Decision]
    N --> O[Recommended Action]
    O --> P[AgentRiskAnalyzeResponse]
```

## 4. Decision Map

```mermaid
flowchart TD
    A[Signals] --> B{Pattern}

    B -->|No risky signal| C[SAFE]
    C --> C1[ALLOW]

    B -->|TOOL_ERROR + RETRY| D[UNSTABLE_TOOL_LOOP]
    D --> D1[DISABLE_TOOL_TEMPORARILY]

    B -->|POLICY_VIOLATION + RETRY| E[SUSPICIOUS_RETRY]
    E --> E1[BLOCK]

    B -->|POLICY_VIOLATION + EXTERNAL_API_CALL| F[RISKY_EXTERNAL_ACCESS]
    F --> F1[BLOCK_AND_ESCALATE]

    B -->|APPROVAL_REQUIRED + RETRY| G[APPROVAL_BYPASS_RISK]
    G --> G1[REQUIRE_APPROVAL]

    B -->|POLICY_VIOLATION + RETRY + EXTERNAL_API_CALL| H[HIGH_RISK_AGENT_BEHAVIOR]
    H --> H1[BLOCK_AND_ESCALATE]
```

## 5. JPA와 SQL 저장 흐름

```mermaid
flowchart TD
    A[Risk Decision Created] --> B[AgentRiskHistory.create]
    B --> C[AgentRiskHistoryRepository.save]
    C --> D[JPA EntityManager]
    D --> E[Hibernate]
    E --> F[SQL INSERT]
    F --> G[(agent_risk_history table)]

    A --> H[AgentRunHistory.create]
    H --> I[AgentRunHistoryRepository.save]
    I --> J[JPA EntityManager]
    J --> K[Hibernate]
    K --> L[SQL INSERT]
    L --> M[(agent_run_history table)]
```

SQL examples:

```sql
select *
from agent_risk_history
order by created_at desc;
```

```sql
select decision, count(*)
from agent_risk_history
group by decision;
```

```sql
select recommended_action, count(*)
from agent_risk_history
group by recommended_action;
```

## 6. Prometheus Metric 흐름

```mermaid
flowchart TD
    A[AgentRiskDecisionService] --> B[AgentMetricRecorder]

    B --> C[agent_tool_calls_total]
    B --> D[agent_retry_count_total]
    B --> E[agent_policy_violation_total]
    B --> F[agent_external_api_calls_total]
    B --> G[agent_tool_errors_total]
    B --> H[agent_approval_required_total]
    B --> I[agent_cost_tokens_total]

    C --> J[Actuator Prometheus Endpoint]
    D --> J
    E --> J
    F --> J
    G --> J
    H --> J
    I --> J

    J --> K[Prometheus Scrape]
    K --> L[PromQL Query]
    L --> M[Grafana or Evidence Screenshot]
```

PromQL examples:

```promql
increase(agent_policy_violation_total[5m])
or
increase(agent_retry_count_total[5m])
or
increase(agent_external_api_calls_total[5m])
```

```promql
increase(agent_policy_violation_total[5m])
and
increase(agent_retry_count_total[5m])
and
increase(agent_external_api_calls_total[5m])
```

## 7. Prometheus와 DB History 차이

```mermaid
flowchart LR
    A[Agent Risk Event] --> B[Prometheus Metric]
    A --> C[JPA DB History]

    B --> D[How many times did it happen]
    B --> E[Time series trend]
    B --> F[Alert candidate]

    C --> G[Which request caused it]
    C --> H[Which decision was made]
    C --> I[Which action was recommended]
    C --> J[Audit-like event record]
```

Important idea:

```text
Prometheus = 몇 번 발생했나
DB History = 어떤 요청이 어떤 위험 판단을 받았나
```

## 8. Security Event 흐름

```mermaid
flowchart TD
    A[Request to Protected Actuator Endpoint] --> B{Authenticated?}

    B -->|No| C[401 Unauthorized]
    C --> D[security_auth_failures_total]
    C --> E[SecurityEventHistory AUTH_FAILURE]

    B -->|Yes but not ADMIN| F[403 Forbidden]
    F --> G[security_access_denied_total]
    F --> H[SecurityEventHistory ACCESS_DENIED]

    B -->|ADMIN| I[Allow Request]

    E --> J[(security_event_history table)]
    H --> J
```

Security meaning:

```text
401 = authentication failure = who are you?
403 = authorization failure = I know who you are, but you do not have permission.
```

## 9. Admin Dashboard API 흐름

```mermaid
flowchart TD
    A[React Admin Dashboard or Postman] --> B[GET /api/admin/dashboard/summary]
    B --> C[AdminDashboardController]
    C --> D[AdminDashboardService]

    D --> E[AgentRunHistoryRepository.count]
    D --> F[AgentRiskHistoryRepository.count]
    D --> G[SecurityEventHistoryRepository.count]
    D --> H[AgentRiskHistoryQueryService.summarize]

    E --> I[AdminDashboardSummaryResponse]
    F --> I
    G --> I
    H --> I
    I --> J[JSON Summary Response]
```

Admin summary purpose:

```text
React 화면은 나중에 붙여도 된다.
중요한 것은 백엔드가 dashboard-ready JSON을 줄 수 있다는 점이다.
```

## 10. Guardrail Adapter 확장 흐름

```mermaid
flowchart TD
    A[Agent Request] --> B[GuardrailDecisionClient Interface]

    B --> C[Current RuleBasedGuardrailDecisionClient]
    C --> D[Keyword and Rule Based Decision]

    B --> E[Future NemoGuardrailsDecisionClient]
    E --> F[NVIDIA NeMo Guardrails]
    F --> G[Policy Runtime Decision]

    D --> H[GuardrailDecisionResult]
    G --> H
    H --> I[Risk Decision Engine]
    I --> J[Metric and History and Response]
```

Current:

```text
GuardrailDecisionClient -> RuleBasedGuardrailDecisionClient
```

Future:

```text
GuardrailDecisionClient -> NemoGuardrailsDecisionClient
```

## 11. React 연결 최소 흐름

```mermaid
flowchart TD
    A[React Input Form] --> B[userInput state]
    A --> C[toolName state]
    A --> D[retryCount state]

    B --> E[Build Request JSON]
    C --> E
    D --> E

    E --> F[fetch POST /api/agent/risk/analyze]
    F --> G[Spring Backend]
    G --> H[Risk Response JSON]
    H --> I[React Result State]
    I --> J[Decision Card]
    I --> K[Risk Score Badge]
    I --> L[Recommended Action]
```

Frontend role:

```text
프론트는 판단하는 곳이 아니다.
프론트는 JSON을 보내고 결과를 보여주는 창구다.
위험 판단, metric 기록, JPA 저장은 백엔드가 담당한다.
```

## 12. 학습용 최종 지도

```mermaid
flowchart TD
    A[JSON Contract] --> B[DTO Mapping]
    B --> C[Service Logic]
    C --> D[Policy Rule]
    D --> E[Risk Decision]
    E --> F[Metric]
    E --> G[JPA History]
    G --> H[SQL]
    E --> I[Admin API]
    E --> J[Future Guardrail]

    F --> K[Prometheus]
    K --> L[Grafana or Alert]

    J --> M[Future NVIDIA NeMo Guardrails]
```

One sentence to memorize:

```text
JSON으로 들어온 agent 요청을 DTO로 받고,
Service에서 위험 신호를 해석하고,
metric과 JPA history로 남긴 뒤,
guardrail-ready decision을 JSON으로 반환한다.
```
