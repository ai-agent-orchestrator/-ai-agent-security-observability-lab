# AI Cost Control Study Guide

## Purpose

The legal chatbot uses two cost-control layers:

```text
Provider hard limit
  -> external billing and project safety boundary

Spring application cost control
  -> blocks a request before the LLM call when the local budget is exhausted
```

The provider limit is the final billing boundary. The Spring layer gives the application an earlier, explainable decision point.

## Request flow

```text
JWT
  -> Input Guardrail
  -> estimated token/cost calculation
  -> budget reservation
  -> LLM call with max_completion_tokens
  -> actual usage reconciliation
  -> Output Guardrail
  -> response or fallback
```

## Why reserve before calling

The application cannot know the exact completion token count before the model responds. It therefore reserves an upper-bound estimate using the configured maximum output token count.

```text
estimated input tokens
  + max output tokens
  -> estimated cost
  -> budget check
```

If the reservation would exceed the configured budget, the external provider is never called.

After the response, the reservation is replaced with the actual prompt/completion usage reported by the provider.

## Configuration

```properties
llm.max-output-tokens=${LLM_MAX_OUTPUT_TOKENS:512}
llm.request-timeout-ms=${LLM_REQUEST_TIMEOUT_MS:30000}
llm.cost.monthly-budget-usd=${LLM_MONTHLY_BUDGET_USD:7}
llm.cost.input-usd-per-1k-tokens=${LLM_INPUT_USD_PER_1K_TOKENS:0.15}
llm.cost.output-usd-per-1k-tokens=${LLM_OUTPUT_USD_PER_1K_TOKENS:0.60}
```

The prices are configurable estimates. They must be updated when the selected provider/model pricing changes. They are not a replacement for the provider billing dashboard.

## Important code reading

`AiCostControlService.reserve(...)` runs before `LlmClient.chat(...)`.

```java
var reservation = costControlService.reserve(
        systemPrompt,
        input.content(),
        "configured-model"
);
```

An empty `Optional` means the request is stopped before the external API call:

```java
return AiChatResponse.technicalFallback(
        "사용량 한도에 도달했습니다. 잠시 후 다시 시도해 주세요.",
        traceId,
        "configured-model",
        "COST_LIMIT_EXCEEDED");
```

After a successful call, actual provider usage is recorded:

```java
costControlService.recordActualUsage(
        reservation.get(),
        llm.promptTokens(),
        llm.completionTokens());
```

If the call fails, the reservation is released so a failed request does not permanently consume the in-process reservation:

```java
costControlService.release(reservation.get(), "LLM_FAILURE");
```

## Metrics

```text
ai.cost.estimated
ai.cost.actual
ai.cost.blocked
ai.cost.reserved.usd
ai.cost.actual.usd
ai.cost.budget.usd
ai.llm.tokens
```

## Scope and limitation

The current implementation is intentionally a single-instance learning version. The reservation ledger is held in the Spring process. A multi-instance deployment would move the reservation and monthly ledger to Redis or a database and use an atomic transaction. The provider hard limit remains necessary in either design.
