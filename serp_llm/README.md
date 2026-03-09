# SERP LLM Service

AI assistant service for the SERP ERP system. Provides conversational AI, semantic search, and intelligent suggestions via Google Gemini.

**Port:** `8089` | **Python:** 3.12+ | **Framework:** FastAPI + SQLAlchemy 2.0

## Quick Start

```bash
# 1. Set up environment
cp .env.example .env  # Configure OPENAI_API_KEY, DATABASE_URL, etc.

# 2. Install dependencies
poetry install

# 3. Setup database
psql -U postgres -c "CREATE DATABASE serp_llm;"
psql -U postgres -d serp_llm -c "CREATE EXTENSION vector;"
poetry run alembic upgrade head

# 4. Run
./run-dev.sh

# Or manually
poetry run uvicorn src.main:app --host 0.0.0.0 --port 8089 --reload
```

**Prerequisites:** Python 3.12+, PostgreSQL 15+ (with pgvector), Redis, Poetry

## Features

- **Conversational AI** - Chat with context for CRM, Tasks, Schedules
- **Semantic Search** - Vector embeddings via pgvector
- **Multi-Model Support** - Google Gemini (default) or OpenAI
- **Streaming Responses** - SSE for real-time chat
- **Clean Architecture** - Domain-driven design with async throughout

## API Routes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Service info |
| GET | `/health` | Health check |
| GET | `/docs` | OpenAPI docs (dev only) |
| POST | `/api/v1/chat` | Create conversation |
| POST | `/api/v1/chat/{id}/message` | Send message |
| GET | `/api/v1/chat/{id}/stream` | Stream response (SSE) |
| GET | `/api/v1/chat` | List conversations |

## Configuration

### Environment Variables (`.env`)

```bash
# Server
PORT=8089
ENVIRONMENT=development

# Database (PostgreSQL with pgvector)
DATABASE_URL=postgresql+asyncpg://user:password@localhost:5432/serp_llm

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# LLM - Google Gemini (default)
OPENAI_API_KEY=<your-gemini-api-key>
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
DEFAULT_MODEL=gemini-2.0-flash
EMBEDDING_MODEL=text-embedding-004

# LLM - OpenAI (alternative)
# OPENAI_BASE_URL=https://api.openai.com/v1
# DEFAULT_MODEL=gpt-4-turbo

# Keycloak
KEYCLOAK_SERVER_URL=http://localhost:8180
KEYCLOAK_REALM=serp
```

Get Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey)

## Project Structure

```
src/
├── main.py                     # FastAPI application
├── config/settings.py          # Pydantic settings
├── core/
│   ├── domain/                 # Entities, DTOs, enums, exceptions
│   ├── mapper/                 # Entity <-> Model mappers
│   ├── service/                # Domain services
│   └── usecase/                # Application use cases
├── infrastructure/
│   ├── db/                     # SQLAlchemy models, repositories
│   ├── llm/                    # OpenAI/Gemini client
│   └── cache/                  # Redis cache
├── kernel/utils/               # Auth, JWT, response helpers
└── ui/
    ├── api/v1/                 # Controllers, router
    └── middleware/             # JWT, logging, exception handlers
```

## Development

```bash
# Run
./run-dev.sh

# Test
poetry run pytest
poetry run pytest --cov=src --cov-report=html

# Format & lint
poetry run black src/
poetry run ruff check src/
poetry run mypy src/

# Database migrations
poetry run alembic revision --autogenerate -m "Add new table"
poetry run alembic upgrade head
poetry run alembic downgrade -1
```

## Docker

```bash
# Build
docker build -t serp-llm:latest .

# Run
docker run -d --name serp-llm -p 8089:8089 --env-file .env serp-llm:latest
```

## Related Documentation

- [AGENTS.md](../AGENTS.md) - Code style and development guidelines
- [API Gateway](../api_gateway/README.md) - Request routing
