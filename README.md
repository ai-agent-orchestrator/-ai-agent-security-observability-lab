# AI Agent Security Observability Lab

This repository records hands-on experiments for AI agent security observability.

The focus is direct evidence:

```text
Postman request
-> Spring Boot custom metric
-> Prometheus query
-> screenshot evidence
-> security interpretation
```

## Experiment Evidence

- [Full screenshot gallery](docs/evidence/GALLERY.md)
- [AI agent risk metric portfolio](PORTFOLIO.md)
- [Spring Security observability portfolio](SECURITY_PORTFOLIO.md)
- [Evidence notes](docs/evidence/README.md)

Representative results:

![three metric agent risk pattern](docs/evidence/risk-patterns/2026-09-17-three-metric-policy-violation-retry-tool-calls-prometheus.png)

```text
policy violation + retry + tool calls increased together.
This means the agent repeatedly approached a blocked action while continuing tool activity.
```

![external api policy violation](docs/evidence/risk-patterns/2026-09-17-external-api-policy-violation-prometheus.png)

```text
External API call and policy violation were observed in the same time window.
This pattern indicates that a risky agent request moved toward an external dependency.
```

Observed metric combinations:

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

Conclusion:

```text
AI agent security is not only about the final answer.
It is about what the agent repeatedly tried to do, which tools it called, where it failed, and whether it approached risky actions.
```

Executable Spring Boot code:

```text
spring/guardrail-ready-policy-checker
```

## Portfolio Experiment Results

The sections below summarize the actual experiments captured in this repository.

Full pages:

- [AI agent risk metric portfolio](PORTFOLIO.md)
- [Spring Security observability portfolio](SECURITY_PORTFOLIO.md)
- [Full screenshot gallery](docs/evidence/GALLERY.md)

## 1. Policy Decision Metrics

PromQL:

```promql
sum by (decision, policy) (
  increase(agent_policy_check_total[5m])
)
```

![policy decision metrics](docs/evidence/basic/2026-09-17-policy-check-decisions-prometheus-allowed-denied.png)

```text
ALLOWED, DENIED, and APPROVAL_REQUIRED decisions are visible as separate metric series.
This means agent requests can be observed by policy outcome, not only by HTTP status.
```

## 2. Allowed Request

Scenario:

```http
POST /api/agent/policy-check
```

```json
{
  "userInput": "search public policy documents",
  "toolName": "search"
}
```

PromQL:

```promql
increase(agent_policy_allowed_total[5m])
```

![allowed request increase](docs/evidence/basic/2026-09-17-policy-check-allowed-increase-prometheus.png)

```text
Safe requests are counted separately.
This provides a baseline for normal agent behavior.
```

## 3. Denied Request

Scenario:

```http
POST /api/agent/policy-check
```

```json
{
  "userInput": "delete all customer records",
  "toolName": "database"
}
```

![denied request postman](docs/evidence/basic/2026-09-17-policy-check-denied-postman.png)

PromQL:

```promql
increase(agent_policy_violation_total{policy="dangerous_database_operation"}[5m])
```

![denied request prometheus](docs/evidence/basic/2026-09-17-policy-violation-dangerous-database-prometheus.png)

```text
Dangerous database or bulk customer-data operations are denied and emitted as policy violation metrics.
This turns policy enforcement into observable security evidence.
```

## 4. Approval Required

Scenario:

```http
POST /api/agent/policy-check
```

```json
{
  "userInput": "send customer report by email",
  "toolName": "email"
}
```

![approval required postman](docs/evidence/basic/2026-09-17-policy-check-approval-required-postman.png)

PromQL:

```promql
increase(agent_approval_required_total{reason="sensitive_action"}[5m])
```

![approval required increase](docs/evidence/basic/2026-09-17-approval-required-increase-prometheus.png)

```text
APPROVAL_REQUIRED is not a simple failure.
It is a human approval boundary that can be measured independently.
```

## 5. Policy Violation + Retry

Scenario:

```http
POST /api/agent/risk-patterns/policy-violation-retry
```

```json
{
  "userInput": "delete all customer records repeatedly",
  "toolName": "database"
}
```

![policy violation retry postman](docs/evidence/risk-patterns/2026-09-17-policy-violation-retry-postman.png)

PromQL:

```promql
increase(agent_policy_violation_total[5m])
or
increase(agent_retry_count_total[5m])
```

![policy violation retry prometheus](docs/evidence/risk-patterns/2026-09-17-policy-violation-retry-prometheus.png)

```text
Policy violation and retry increased together.
This indicates repeated attempts around a blocked or risky action.
```

## 6. Tool Error + Retry

Scenario:

```http
POST /api/agent/risk-patterns/tool-error-retry
```

```json
{
  "userInput": "search recent policy documents with failing tool",
  "toolName": "search"
}
```

![tool error retry postman](docs/evidence/risk-patterns/2026-09-17-tool-error-retry-postman.png)

PromQL:

```promql
increase(agent_tool_errors_total[5m])
or
increase(agent_retry_count_total[5m])
```

![tool error retry prometheus](docs/evidence/risk-patterns/2026-09-17-tool-error-retry-prometheus.png)

```text
Tool error and retry increased together.
This indicates repeated calls to an unstable or failing tool.
```

## 7. External API + Policy Violation

Scenario:

```http
POST /api/agent/risk-patterns/external-api-policy-violation
```

```json
{
  "userInput": "upload sensitive customer report to external api",
  "toolName": "external-api"
}
```

![external api policy violation postman](docs/evidence/risk-patterns/2026-09-17-external-api-policy-violation-postman.png)

PromQL:

```promql
increase(agent_external_api_calls_total[5m])
or
increase(agent_policy_violation_total[5m])
```

![external api policy violation prometheus](docs/evidence/risk-patterns/2026-09-17-external-api-policy-violation-prometheus.png)

```text
External API call and policy violation were observed in the same time window.
This pattern indicates that a risky agent request moved toward an external dependency.
```

## 8. Approval Required + Retry

Scenario:

```http
POST /api/agent/risk-patterns/approval-required-retry
```

```json
{
  "userInput": "send customer report by email repeatedly",
  "toolName": "email"
}
```

![approval required retry postman](docs/evidence/risk-patterns/2026-09-17-approval-required-retry-postman.png)

PromQL:

```promql
increase(agent_approval_required_total[5m])
or
increase(agent_retry_count_total[5m])
```

![approval required retry prometheus](docs/evidence/risk-patterns/2026-09-17-approval-required-retry-prometheus.png)

```text
Approval-required actions and retries increased together.
This indicates repeated attempts near a human approval boundary.
```

## 9. Three-Metric Risk Pattern

PromQL:

```promql
increase(agent_policy_violation_total[5m])
or
increase(agent_retry_count_total[5m])
or
increase(agent_tool_calls_total[5m])
```

![three metric policy violation retry tool calls](docs/evidence/risk-patterns/2026-09-17-three-metric-policy-violation-retry-tool-calls-prometheus.png)

```text
Policy violation, retry, and tool calls increased together.
This is stronger than a single metric because it shows repeated risky behavior plus continued tool activity.
```

## 10. External Three-Metric Risk Pattern

PromQL:

```promql
increase(agent_external_api_calls_total[5m])
or
increase(agent_policy_violation_total[5m])
or
increase(agent_tool_calls_total[5m])
```

![three metric external policy tool calls](docs/evidence/risk-patterns/2026-09-17-three-metric-external-policy-tool-calls-prometheus.png)

```text
External API activity, policy violation, and tool calls appeared in the same experiment set.
This is useful evidence for future guardrail rules around external dependency access.
```

## Security Observability Evidence

Security evidence is documented in detail in [SECURITY_PORTFOLIO.md](SECURITY_PORTFOLIO.md).

![no auth 401](docs/evidence/security/2026-09-17-actuator-metrics-no-auth-401-postman.png)

![security auth failures](docs/evidence/security/2026-09-17-security-auth-failures-prometheus.png)

![user 403](docs/evidence/security/2026-09-17-actuator-metrics-user-403-postman.png)

![security access denied](docs/evidence/security/2026-09-17-security-access-denied-prometheus.png)

```text
401 and 403 are not only blocked requests.
They are security events that can be measured as Prometheus metrics.
```

## Implementation Source

```text
spring/guardrail-ready-policy-checker
```

## Next Direction

```text
metric collection
-> metric combination interpretation
-> risk decision API
-> DB history
-> incident prototype
-> future NVIDIA NeMo Guardrails adapter
```
