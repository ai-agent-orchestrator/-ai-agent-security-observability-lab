# Evidence Log

## 2026-09-17

### Planned Capture Set

```text
1. policy violation + retry
2. tool error + retry
3. external API call + policy violation
4. approval required + retry
5. policy check decisions
```

### Portfolio Message

```text
This lab does not only observe whether an API succeeded.
It observes whether an AI agent behaved safely, cheaply, and within policy boundaries.
```

### Main Interpretation

```text
policy = ontology + security doctrine
```

Ontology identifies the target object, action, tool, dependency, and sensitivity.

Security doctrine decides whether to allow, deny, require approval, escalate, alert, or apply a guardrail.

### Captured Evidence

Basic policy decision evidence:

```text
docs/evidence/basic
```

Risk-pattern evidence:

```text
docs/evidence/risk-patterns
```

Portfolio write-up:

```text
PORTFOLIO.md
```

Core portfolio sentence:

```text
Success is not enough.
AI agent risk appears in behavior metrics, not only in the response body.
```
