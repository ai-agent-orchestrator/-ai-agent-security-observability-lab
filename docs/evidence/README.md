# Evidence Gallery

This folder stores visual evidence for the AI agent security observability lab.

The goal is not to collect random screenshots. Each image should prove one security-observability point:

```text
metric combination
-> behavior interpretation
-> PromQL / Grafana evidence
-> security or guardrail meaning
```

## Naming Rule

Use this pattern:

```text
YYYY-MM-DD-topic-tool.png
```

Examples:

```text
2026-09-17-policy-violation-retry-prometheus.png
2026-09-17-policy-violation-retry-grafana.png
2026-09-17-approval-required-retry-postman.png
```

## Evidence Set

| Pattern | Capture | Meaning |
| --- | --- | --- |
| policy violation + retry | Prometheus or Grafana | The agent repeatedly approached a blocked or risky action. |
| tool error + retry | Prometheus or Grafana | The agent repeatedly called an unstable or failing tool. |
| external API call + policy violation | Prometheus or Grafana | A risky request moved toward an external dependency. |
| approval required + retry | Prometheus or Grafana | The agent repeatedly approached a human approval boundary. |
| policy check decision | Postman | A request was classified as ALLOWED, DENIED, or APPROVAL_REQUIRED. |
| actuator auth failure | Postman and Prometheus | A protected operational endpoint rejected an unauthenticated request. |
| actuator access denied | Postman and Prometheus | A protected operational endpoint rejected an authenticated non-admin user. |

## PromQL Evidence Queries

```promql
increase(agent_policy_violation_total[5m])
```

```promql
increase(agent_retry_count_total[5m])
```

```promql
increase(agent_tool_errors_total[5m])
```

```promql
increase(agent_external_api_calls_total[5m])
```

```promql
increase(agent_approval_required_total[5m])
```

```promql
sum by (decision, policy) (
  increase(agent_policy_check_total[5m])
)
```

## Interpretation Rule

Single metrics answer:

```text
What happened?
```

Metric combinations answer:

```text
What behavior pattern is emerging?
```

That distinction is the core of this lab.
