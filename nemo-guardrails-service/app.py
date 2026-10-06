from contextlib import asynccontextmanager
from pathlib import Path
from typing import Literal

from fastapi import FastAPI
from pydantic import BaseModel, Field
from nemoguardrails import LLMRails, RailsConfig
from nemoguardrails.rails.llm.options import RailStatus, RailType


CONFIG_PATH = Path(__file__).parent / "config"
rails = LLMRails(RailsConfig.from_path(str(CONFIG_PATH)))


class Message(BaseModel):
    role: Literal["user", "assistant"]
    content: str = Field(min_length=1)


class InputCheckRequest(BaseModel):
    messages: list[Message] = Field(min_length=1)


class InputCheckResponse(BaseModel):
    status: str
    content: str
    rail: str | None = None


@asynccontextmanager
async def lifespan(_: FastAPI):
    await rails.startup()
    yield
    await rails.shutdown()


app = FastAPI(title="Legal Chat NeMo Input Rails", lifespan=lifespan)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/v1/input-rails/check", response_model=InputCheckResponse)
async def check_input(request: InputCheckRequest) -> InputCheckResponse:
    result = await rails.check_async(
        [message.model_dump() for message in request.messages],
        rail_types=[RailType.INPUT],
    )

    return InputCheckResponse(
        status=result.status.name,
        content=result.content,
        rail=result.rail,
    )
