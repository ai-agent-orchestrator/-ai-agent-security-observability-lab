# JWT + NeMo Input Rail Integration

## Request flow

```text
React
  -> POST /api/auth/login
  -> Bearer access token
  -> POST /api/legal/guardrail/input
  -> Spring JWT filter
  -> Spring RestClient
  -> NeMo Guardrails /v1/input-rails/check
  -> PASSED, MODIFIED, or BLOCKED
```

The new `/api/legal/**` routes require a JWT. Existing practice APIs remain available so the lab's earlier observability exercises do not break.

## Start NeMo Guardrails

```powershell
cd nemo-guardrails-service
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
$env:OPENAI_API_KEY = "your-provider-key"
python -m uvicorn app:app --host 127.0.0.1 --port 8000
```

The provider key is read from the process environment. It is not stored in Git.

## Test with an HTTP client

Login:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "user123"
}
```

Copy `accessToken` from the response and use it here:

```http
POST http://localhost:8080/api/legal/guardrail/input
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "message": "민사소송 준비서류를 알려줘"
}
```

Expected response for an allowed message:

```json
{
  "status": "PASSED",
  "allowed": true,
  "content": "민사소송 준비서류를 알려줘",
  "rail": null,
  "traceId": "..."
}
```

The Spring adapter records `ai.input.rail.decisions`, `ai.input.rail.errors`, and `ai.input.rail.duration` through Micrometer.

## Important boundary

The authenticated Input Rail integration point and a matching Output Rail check are now available. The project still does not call the main LLM or implement SSE. The future AI chat service should call the input rail before the LLM and the output rail after the LLM, then return a fallback response when either check is blocked.
