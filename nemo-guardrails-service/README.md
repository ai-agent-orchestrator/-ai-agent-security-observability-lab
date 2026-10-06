# Legal Chat NeMo Input Rails

This service runs the legal chatbot input and output policies separately from the Spring MVC application.
It does not implement RAG or the main chatbot LLM call. It validates user messages before an LLM call and assistant responses after an LLM call.

## Run

PowerShell:

```powershell
cd nemo-guardrails-service
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
$env:OPENAI_API_KEY = "your-provider-key"
python -m uvicorn app:app --host 127.0.0.1 --port 8000
```

The Spring adapter calls `POST /v1/input-rails/check`.

## Request

```json
{
  "messages": [
    {"role": "user", "content": "민사소송 준비서류를 알려줘"}
  ]
}
```

## Response

```json
{
  "status": "PASSED",
  "content": "민사소송 준비서류를 알려줘",
  "rail": null
}
```

The Spring service exposes the protected integration check at:

```text
POST http://localhost:8080/api/legal/guardrail/input
```

The Spring output adapter is also protected by JWT:

```text
POST http://localhost:8080/api/legal/guardrail/output
```

It sends the assistant response to NeMo's `POST /v1/output-rails/check` endpoint.

The NeMo model key is read from the process environment. It is never stored in the repository.
