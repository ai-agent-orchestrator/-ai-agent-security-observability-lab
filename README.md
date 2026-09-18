# AI Agent Security Observability Lab

This repository is a security-focused lab for detecting suspicious AI agent behavior through custom metrics, Prometheus, Grafana, and future guardrail integration.

It now contains executable Spring Boot code, not only notes:

```text
spring/guardrail-ready-policy-checker
```

Frontend metric reaction prototype:

```text
docs/frontend/metric-reaction-prototype.html
```

The prototype calls:

```text
POST http://localhost:8080/api/agent/risk/analyze
```

and displays the backend risk decision as a simple frontend reaction message.

The main idea:

```text
AI agent security is not only about the final answer.
It is about what the agent repeatedly tried to do, which tools it called, where it failed, and whether it approached risky actions.
```

## Core Thesis

Single metrics are useful, but security meaning often appears in combinations.

```text
policy violation + retry
-> repeated attempts around a blocked action

tool error + retry
-> repeated calls to an unstable or failing tool

external API call + policy violation
-> risky behavior moving toward an external dependency

approval required + retry
-> repeated attempts near a human approval boundary
```

The project direction:

```text
Detect suspicious agent behavior early
-> interpret metric combinations
-> alert or escalate
-> apply customized guardrails
-> record the incident
-> update policy and metrics
```

## Policy Model

The working model for this lab:

```text
policy = ontology + security doctrine
```

Ontology answers:

```text
What object is being touched?
What action is being attempted?
Which tool or external dependency is involved?
Is the data sensitive?
Is the action reversible?
```

Security doctrine answers:

```text
Allow it?
Deny it?
Require human approval?
Escalate it?
Record it as an incident?
Apply a guardrail?
```

Example:

```text
request: delete all customer records
ontology: customer_records + bulk_delete + database_tool
security doctrine: irreversible sensitive-data operation
decision: DENIED
metric: agent_policy_violation_total{policy="dangerous_database_operation"}
```

## Why This Matters

API success is no longer enough.

```text
The API succeeded.
But was it cheap, safe, stable, and policy-compliant?
```

For AI agents, cost and security are tightly connected:

```text
retry
-> repeated cost

tool call
-> execution cost and operational risk

external API call
-> data exposure and dependency risk

policy violation
-> blocked or dangerous behavior

approval required
-> human control boundary
```

## Current Implementation Source

The executable Spring Boot lab now lives directly in this repository:

```text
spring/guardrail-ready-policy-checker
```

It was promoted and adjusted from:

```text
C:\myLectureWs\spring-boot-api-observability-practice
```

Related GitHub repository:

```text
https://github.com/ai-agent-orchestrator/spring-boot-api-observability-practice
```

Relevant branches:

```text
feature/jpa-n-plus-one-practice
-> SQL statement count metric for N+1 observability

feature/agent-custom-metrics-practice
-> agent behavior metrics

feature/agent-risk-pattern-metrics-practice
-> suspicious agent behavior metric combinations
-> guardrail-ready policy checker
```

This repository is the security-centered home. The code keeps the N+1 observability practice as a cost-signal baseline, then extends it into AI agent behavior metrics, risk-pattern metric combinations, and a guardrail-ready policy checker.

Run it locally:

```powershell
cd spring/guardrail-ready-policy-checker
.\gradlew.bat bootRun
```

Health check:

```text
http://localhost:8080/actuator/health
```

Prometheus metrics:

```text
http://localhost:8080/actuator/prometheus
```

Lab guide endpoint:

```text
http://localhost:8080/api/security-observability/guide
```

Evidence gallery:

```text
docs/evidence
```

The evidence folder is where Postman, Prometheus, and Grafana screenshots are collected with interpretation notes. The goal is to show metric combinations, not only isolated metric values.

Portfolio evidence:

```text
PORTFOLIO.md
```

The portfolio document connects API scenarios, PromQL queries, screenshots, and PM/security interpretation.

Spring Security lab:

```text
SECURITY_LAB.md
```

The security lab protects Actuator endpoints and records authentication or authorization failures as custom metrics.

Security portfolio evidence:

```text
SECURITY_PORTFOLIO.md
```

The security portfolio connects 401/403/admin access tests with Prometheus screenshots and beginner-friendly security interpretation.

## Metric Groups

```text
Agent behavior
-> agent_tool_calls_total
-> agent_tool_errors_total
-> agent_retry_count_total
-> agent_cost_tokens_total
-> agent_external_api_calls_total
-> agent_db_write_total

Security / guardrail signals
-> agent_policy_violation_total
-> agent_approval_required_total

Spring Security signals
-> security_auth_failures_total
-> security_access_denied_total

Internal cost
-> practice_api_sql_statements_total
-> practice_api_request_duration_seconds
-> practice_api_requests_total
```

## Suspicious Behavior PromQL

```promql
increase(agent_policy_violation_total[5m]) >= 1
and
increase(agent_retry_count_total[5m]) >= 3
```

```promql
increase(agent_tool_errors_total[5m])
and
increase(agent_retry_count_total[5m])
```

```promql
increase(agent_external_api_calls_total[5m])
and
increase(agent_policy_violation_total[5m])
```

## Planned October NeMo Guardrails Extension

This repository does not integrate NVIDIA NeMo Guardrails yet.

The plan is:

```text
Learn NVIDIA NeMo Guardrails
-> map each detected behavior pattern to a customized guardrail
-> connect metrics and alerts to guardrail actions
-> test detect -> block / approve / escalate flows
```

Planned mappings:

```text
policy-violation-retry
-> custom guardrail: stop repeated risky attempts and require human review

tool-error-retry
-> custom guardrail: limit retries and route to fallback handling

external-api-policy-violation
-> custom guardrail: block outbound access or require approval

approval-required-retry
-> custom guardrail: freeze action until explicit approval is recorded
```

## Guardrail-Ready Policy Checker

The Spring Boot implementation in this repository includes a mock policy checker that can later be replaced with a NeMo Guardrails adapter.

Implementation branch:

```text
spring/guardrail-ready-policy-checker
```

Current API:

```http
POST /api/agent/policy-check
```

Current flow:

```text
AgentPolicyCheckController
-> AgentPolicyCheckService
-> AgentMetricRecorder
-> Prometheus / Grafana
```

Current mock decisions:

```text
search public policy documents
-> ALLOWED

delete all customer records
-> DENIED

send customer report by email
-> APPROVAL_REQUIRED
```

Current metrics:

```text
agent_policy_check_total
agent_policy_allowed_total
agent_policy_violation_total
agent_approval_required_total
```

Replacement point:

```text
Current:
AgentPolicyCheckService -> mock if-based rules

Future:
AgentPolicyCheckService -> NVIDIA NeMo Guardrails adapter
```

## Agent Risk Decision Engine

The next step is not only recording metrics.

The backend now converts combined agent behavior signals into a risk decision:

```text
metric collection
-> metric interpretation
-> risk score
-> risk level
-> recommended action
-> guardrail-ready decision
```

API:

```http
POST /api/agent/risk/analyze
```

Example request:

```json
{
  "userInput": "upload sensitive customer report to external api repeatedly",
  "toolName": "external-api",
  "retryCount": 3,
  "promptTokens": 120,
  "completionTokens": 80,
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
  "guardrailReady": true,
  "historyId": 1,
  "traceId": "generated-trace-id"
}
```

Decision map:

```text
POLICY_VIOLATION + RETRY
-> SUSPICIOUS_RETRY
-> BLOCK

POLICY_VIOLATION + EXTERNAL_API
-> RISKY_EXTERNAL_ACCESS
-> BLOCK_AND_ESCALATE

POLICY_VIOLATION + RETRY + EXTERNAL_API
-> HIGH_RISK_AGENT_BEHAVIOR
-> BLOCK_AND_ESCALATE

TOOL_ERROR + RETRY
-> UNSTABLE_TOOL_LOOP
-> DISABLE_TOOL_TEMPORARILY

APPROVAL_REQUIRED + RETRY
-> APPROVAL_BYPASS_RISK
-> REQUIRE_APPROVAL
```

Recommended actions:

```text
ALLOW
-> continue the agent request

REQUIRE_APPROVAL
-> pause execution until a human approves the action

BLOCK
-> block the risky request

BLOCK_AND_ESCALATE
-> block immediately and escalate as a high-risk event

DISABLE_TOOL_TEMPORARILY
-> stop using an unstable or repeatedly failing tool

CREATE_INCIDENT
-> create an incident record for operator review
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

History:

```text
Each risk decision is saved to agent_risk_history.
Prometheus shows behavior volume.
DB history preserves the interpreted incident-like decision.
```

History APIs:

```http
GET /api/agent/risk/history
GET /api/agent/risk/history?riskLevel=HIGH
GET /api/agent/risk/history?decision=HIGH_RISK_AGENT_BEHAVIOR
GET /api/agent/risk/summary
```

Why JPA history matters:

```text
Prometheus
-> how many times did a signal increase?

JPA / DB history
-> which request received which risk decision?
```

This connects JPA and observability:

```text
AgentRiskHistory is not a simple CRUD table.
It is an audit-like event record for AI agent risk decisions.
```

SQL examples:

```sql
select *
from agent_risk_history
order by created_at desc;
```

```sql
select *
from agent_risk_history
where risk_level = 'HIGH'
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

Future NeMo connection:

```text
Current:
AgentRiskDecisionService -> rule-based risk decision

Future:
AgentRiskDecisionService -> NeMo Guardrails adapter / policy runtime
```

## Project-Ready Backend Additions

This branch turns the lab into a project-ready backend prototype.

Implemented additions:

```text
1. Agent Run History
-> agent_run_history records each analyzed agent run.

2. Risk Score Calculation
-> AgentRiskDecisionService calculates riskScore and riskLevel.

3. Risk Pattern API
-> POST /api/agent/risk/analyze converts behavior signals into a risk decision.

4. Security Event History
-> security_event_history records 401 authentication failures and 403 access denied events.

5. Admin Dashboard API
-> GET /api/admin/dashboard/summary
-> GET /api/admin/dashboard/security-events

6. Guardrail Adapter Interface
-> GuardrailDecisionClient is the future replacement point for NeMo Guardrails.
-> RuleBasedGuardrailDecisionClient is the current mock/rule-based implementation.

7. Policy Rule Separation
-> AgentRiskPolicyRule separates keyword-based policy rules from the service flow.

8. Java 21 Virtual Thread Async Agent Job
-> POST /api/agent/jobs returns a jobId immediately.
-> The risk analysis runs on a Java 21 virtual thread.
-> GET /api/agent/jobs/{jobId} returns PENDING, RUNNING, COMPLETED, or FAILED.

9. Incident Prototype
-> High-risk decisions create risk_incident records.
-> Operators can acknowledge or resolve incidents.
-> GET /api/incidents
-> PATCH /api/incidents/{incidentId}/ack
-> PATCH /api/incidents/{incidentId}/resolve
```

Backend map:

```text
JSON request
-> DTO
-> risk policy rules
-> risk score / decision
-> metric record
-> JPA history save
-> admin summary API
-> future NeMo Guardrails adapter
```

This keeps the project aligned with the class requirements:

```text
Spring REST API
+ JPA / SQL history
+ business logic validation
+ Prometheus observability
+ security event tracking
+ React-ready JSON APIs
+ Java 21 virtual thread async jobs
+ incident response prototype
```

Async agent job APIs:

```http
POST /api/agent/jobs
GET /api/agent/jobs/{jobId}
GET /api/agent/jobs
```

Async flow:

```text
POST /api/agent/jobs
-> save PENDING job
-> return jobId
-> run AgentRiskDecisionService on a virtual thread
-> save COMPLETED or FAILED status
-> frontend or Postman checks status by jobId
```

Why virtual threads fit this project:

```text
AI agent servers often wait for LLM APIs, external APIs, tool calls, and DB operations.
Java 21 virtual threads make this waiting-heavy backend flow easier to model without making WebSocket the center of the project.
```

## Architecture Explanation for Portfolio

This project is built around one backend flow:

```text
AI agent request
-> JSON contract
-> Spring DTO
-> risk decision service
-> metric recording
-> JPA history
-> guardrail-ready response
```

The frontend is intentionally treated as a control surface.
The important logic belongs to the backend.

```text
Frontend
-> sends JSON
-> displays the result

Backend
-> interprets risk
-> records metrics
-> saves history
-> prepares guardrail action
```

### JSON to Backend Flow

The request starts as JSON:

```json
{
  "userInput": "upload sensitive customer report to external api repeatedly",
  "toolName": "external-api",
  "retryCount": 3,
  "policyViolation": true,
  "externalApiCall": true
}
```

Spring maps that JSON into a request DTO:

```text
JSON key
-> DTO field
-> service input
```

The service then extracts behavior signals:

```text
POLICY_VIOLATION
RETRY
EXTERNAL_API_CALL
TOOL_ERROR
APPROVAL_REQUIRED
DB_WRITE
```

These signals are converted into a backend risk decision:

```text
POLICY_VIOLATION + RETRY + EXTERNAL_API_CALL
-> HIGH_RISK_AGENT_BEHAVIOR
-> BLOCK_AND_ESCALATE
```

The final response is again returned as JSON:

```json
{
  "decision": "HIGH_RISK_AGENT_BEHAVIOR",
  "riskScore": 89,
  "riskLevel": "HIGH",
  "recommendedAction": "BLOCK_AND_ESCALATE",
  "guardrailReady": true
}
```

### Metric and History Separation

This project separates metric observation from event history.

```text
Prometheus
-> how many times did the signal happen?

JPA / SQL history
-> which request caused which risk decision?
```

Prometheus answers operational questions:

```text
Did policy violations increase?
Did retries spike?
Did external API calls happen near risky requests?
```

JPA history answers audit-style questions:

```text
What did the agent request?
Which tool was involved?
Which decision was made?
Which action was recommended?
When did it happen?
```

This means the system does not only observe behavior volume.
It also preserves the interpreted risk event.

### Why This Is Guardrail-Ready

The current implementation uses rule-based policy checks.
That is intentional.

```text
Current:
GuardrailDecisionClient -> RuleBasedGuardrailDecisionClient

Future:
GuardrailDecisionClient -> NemoGuardrailsDecisionClient
```

The interface keeps the backend structure stable.
When NVIDIA NeMo Guardrails is added later, the policy runtime can be replaced without changing the whole controller and API structure.

Portfolio summary:

```text
This project converts raw AI agent behavior signals into backend risk decisions,
records them as metrics and JPA history,
and prepares the architecture for future guardrail enforcement.
```

## Portfolio Positioning

This project is not a chatbot demo.

It is a lab for:

```text
cost-aware AI backend observability
agent behavior detection
security event metrics
guardrail-ready architecture
human approval boundaries
incident interpretation
```

The PM-level question:

```text
What behavior, cost, and risk should be visible before an AI agent is trusted with autonomy?
```
