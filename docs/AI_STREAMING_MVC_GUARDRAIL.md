# Spring MVC Streaming AI Flow

This project keeps the existing JSON endpoint and adds a separate streaming endpoint:

```text
POST /api/ai/chat          -> one JSON response
POST /api/ai/chat/stream   -> text/event-stream response
```

## Runtime flow

```text
React fetch POST
  -> JWT authentication
  -> input NeMo rail
  -> application-side cost reservation
  -> OpenAI-compatible streaming request
  -> buffer approximately 50 tokens
  -> include approximately 20 tokens of previous context
  -> output NeMo rail
  -> send only approved chunk through SseEmitter
  -> reconcile actual token usage
  -> send done event and record metrics
```

`chunk-size` and `context-size` are approximate in the Spring adapter. The adapter uses
four characters as one estimated token for buffering. The provider remains the source of
truth for final usage, which is received in the terminal streaming event when the provider
supports `stream_options.include_usage`.

## SSE events

```text
event: meta
data: {"traceId":"...","reason":"OUTPUT_RAIL_BUFFERED",...}

event: token
data: {"traceId":"...","text":"...",...}

event: done
data: {"traceId":"...","promptTokens":123,"completionTokens":45,...}
```

Blocked or failed requests use `blocked` or `error` events and complete the emitter.
The existing `/api/ai/chat` endpoint is unchanged, so the project can compare buffered
JSON behavior with guarded streaming behavior.

## Configuration

```properties
llm.streaming.chunk-size=${LLM_STREAMING_CHUNK_SIZE:50}
llm.streaming.context-size=${LLM_STREAMING_CONTEXT_SIZE:20}
```

`stream_first=false` is represented by the service order: the Spring adapter calls the
output rail before sending a token event. This prevents the unapproved chunk from being
sent to React, at the cost of a small delay before the first token.

## Important limitation

The current implementation calls the existing NeMo output check for each buffered chunk.
The NeMo Python service must be running at `guardrails.base-url`, and the configured LLM
provider must support OpenAI-compatible SSE chat completions. Without `LLM_API_KEY`, the
stream emits an `LLM_STREAM_UNAVAILABLE` fallback without making an external call.

The in-memory cost ledger is still an application learning implementation. A production
multi-instance deployment would move reservations and actual usage to a shared store such
as Redis or a database.
