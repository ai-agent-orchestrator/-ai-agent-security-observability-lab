# Roadmap

## Phase 1. Metric-Based Detection

Goal:

```text
Detect suspicious AI agent behavior through custom metric combinations.
```

Experiments:

```text
retry + policy violation
tool error + retry
external API call + policy violation
approval required + retry
```

Current implementation source:

```text
spring-boot-api-observability-practice
feature/agent-risk-pattern-metrics-practice
```

## Phase 2. Grafana Alerting

Goal:

```text
Turn suspicious metric combinations into alert candidates.
```

Candidate alert:

```promql
increase(agent_policy_violation_total[5m]) >= 1
and
increase(agent_retry_count_total[5m]) >= 3
```

## Phase 3. Security Event Metrics

Goal:

```text
Connect Spring Security events to custom metrics.
```

Candidate metrics:

```text
security_auth_failures_total
security_access_denied_total
security_admin_endpoint_access_total
security_blocked_requests_total
```

## Phase 4. NeMo Guardrails Integration

Target:

```text
October
```

Goal:

```text
Map detected behavior patterns to customized NVIDIA NeMo Guardrails.
```

Expected flow:

```text
metric detection
-> alert
-> guardrail decision
-> block / approve / escalate
-> incident record
```

## Phase 5. Portfolio Interpretation

Goal:

```text
Explain why cost-aware security observability matters for AI agent systems.
```

Core framing:

```text
API success is not enough.
Agent behavior, cost, and security boundaries must be observable.
```
