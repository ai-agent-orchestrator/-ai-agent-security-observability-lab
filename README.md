# AI Agent Security Observability Lab

This repository is a security-focused lab for detecting suspicious AI agent behavior through custom metrics, Prometheus, Grafana, and future guardrail integration.

It now contains executable Spring Boot code, not only notes:

```text
spring/guardrail-ready-policy-checker
```

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
