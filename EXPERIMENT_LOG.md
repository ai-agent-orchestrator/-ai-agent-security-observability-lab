# Experiment Log

## 2026-09-17

### Topic

Agent risk pattern metrics.

### What Was Tested

The Spring Boot observability project added scenario APIs that intentionally emit multiple related metrics together.

```text
POST /api/agent/risk-patterns/policy-violation-retry
POST /api/agent/risk-patterns/tool-error-retry
POST /api/agent/risk-patterns/external-api-policy-violation
POST /api/agent/risk-patterns/approval-required-retry
```

### Core Insight

Suspicious AI agent behavior is not a single number.

It is a pattern made from metric combinations.

```text
policy violation + retry
-> repeated attempts around a blocked action

tool error + retry
-> repeated calls to an unstable tool

external API call + policy violation
-> risky behavior moving toward an external dependency

approval required + retry
-> repeated attempts near a human approval boundary
```

### Security Interpretation

The key question is not only:

```text
Did the AI agent answer successfully?
```

The more important security question is:

```text
What did the agent repeatedly try to do when it was blocked?
```

### Next Step

Use Grafana to place the related metric signals on one panel.

Panel candidate:

```text
Agent Risk Pattern Signals
```

Queries:

```promql
increase(agent_retry_count_total[5m])
```

```promql
increase(agent_policy_violation_total[5m])
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
