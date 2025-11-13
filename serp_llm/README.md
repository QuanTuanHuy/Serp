# SERP LLM Service

**AI Assistant Service for SERP ERP System**

Author: QuanTuanHuy  
Part of Serp Project

---

## 📋 Overview

SERP LLM is a Python-based microservice that provides AI capabilities to the SERP ERP system. It integrates with **Google Gemini** (via OpenAI-compatible API) to deliver intelligent features like:

- 💬 Conversational AI for CRM, Tasks, Schedules
- 🔍 Semantic search across business data
- 💡 Smart suggestions and automation
- 📊 Context-aware insights

Built with **Clean Architecture**, fully async, and production-ready.

---

## 🎯 Tech Stack

- **Language**: Python 3.11+
- **Framework**: FastAPI (async)
- **Database**: PostgreSQL 15+ with pgvector
- **ORM**: SQLAlchemy 2.0 (async)
- **Cache**: Redis 7+
- **Messaging**: Kafka (aiokafka)
- **LLM**: Google Gemini (via OpenAI SDK compatibility)
- **Package Manager**: Poetry

---

## 📁 Project Structure

```
serp_llm/
├── src/
│   ├── main.py                    # FastAPI application
│   ├── config/                    # Settings & configuration
│   ├── core/                      # Business logic (Clean Architecture)
│   │   ├── domain/                # Entities, DTOs, Enums
│   │   ├── port/                  # Interfaces (DIP)
│   │   ├── service/               # Domain services
│   │   └── usecase/               # Application use cases
│   ├── infrastructure/            # External implementations
│   │   ├── db/                    # Database (SQLAlchemy)
│   │   ├── llm/                   # LLM clients
│   │   └── cache/                 # Redis cache
│   └── ui/                        # User Interface layer
│       └── api/                   # API layer
│           ├── routes/            # Endpoints
│           └── middleware/        # Auth, logging, etc.
├── alembic/                       # Database migrations
├── tests/                         # Unit & integration tests
├── pyproject.toml                 # Poetry dependencies
├── Dockerfile                     # Production container
└── run-dev.sh                     # Development script
```

---

## 🚀 Quick Start

### Prerequisites

- Python 3.11+
- PostgreSQL 15+ with pgvector extension
- Redis 7+
- Poetry (optional, but recommended)

### 1. Clone and Setup

```bash
cd serp_llm

# Copy environment template
cp .env.example .env

# Edit .env with your values
# IMPORTANT: Set OPENAI_API_KEY, DATABASE_URL, REDIS_HOST
```

### 2. Install Dependencies

**With Poetry (recommended):**
```bash
poetry install
```

**Without Poetry:**
```bash
pip install -r requirements.txt  # Generate first: poetry export -f requirements.txt --output requirements.txt
```

### 3. Setup Database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE serp_llm;"

# Enable pgvector extension
psql -U postgres -d serp_llm -c "CREATE EXTENSION vector;"

# Run migrations (if using Alembic)
poetry run alembic upgrade head
```

### 4. Run Service

**Development mode (with auto-reload):**
```bash
chmod +x run-dev.sh
./run-dev.sh
```

**Or manually:**
```bash
poetry run uvicorn src.main:app --host 0.0.0.0 --port 8087 --reload
```

**Production:**
```bash
poetry run uvicorn src.main:app --host 0.0.0.0 --port 8087 --workers 4
```

### 5. Verify

```bash
# Health check
curl http://localhost:8087/health

# API docs (development only)
open http://localhost:8087/docs
```

---

## 🔧 Configuration

Edit `.env` file:

```bash
# Server
PORT=8087
ENVIRONMENT=development

# Database (PostgreSQL with pgvector)
DATABASE_URL=postgresql+asyncpg://serp:serp@localhost:5432/serp_llm

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Gemini Configuration (Default)
# Get API key from https://aistudio.google.com/apikey
OPENAI_API_KEY=your-gemini-api-key-here
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/

# LLM Defaults (Gemini models)
DEFAULT_MODEL=gemini-2.0-flash
DEFAULT_TEMPERATURE=0.7
DEFAULT_MAX_TOKENS=8192
EMBEDDING_MODEL=text-embedding-004

# Keycloak (JWT validation)
KEYCLOAK_SERVER_URL=http://localhost:8180
KEYCLOAK_REALM=serp
```

---

## 🐳 Docker Deployment

### Build Image

```bash
docker build -t serp-llm:latest .
```

### Run Container

```bash
docker run -d \
  --name serp-llm \
  -p 8087:8087 \
  --env-file .env \
  serp-llm:latest
```

### Docker Compose Integration

Add to `docker-compose.dev.yml`:

```yaml
services:
  serp_llm:
    build: ./serp_llm
    container_name: serp_llm
    ports:
      - "8087:8087"
    environment:
      - DATABASE_URL=postgresql+asyncpg://serp:serp@postgres:5432/serp_llm
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    env_file:
      - ./serp_llm/.env
    depends_on:
      - postgres
      - redis
      - kafka
    networks:
      - serp-network
```

---

## 📡 API Endpoints

### Health & Info

```http
GET  /                  # Service info
GET  /health            # Health check
GET  /docs              # OpenAPI docs (dev only)
```

### Chat (Coming Soon)

```http
POST /api/v1/chat                    # Create conversation
POST /api/v1/chat/{id}/message       # Send message
GET  /api/v1/chat/{id}/stream        # Stream response (SSE)
GET  /api/v1/chat                    # List conversations
```

---

## 🧪 Testing

```bash
# Run all tests
poetry run pytest

# With coverage
poetry run pytest --cov=src --cov-report=html

# Specific test file
poetry run pytest tests/unit/test_llm_service.py
```

---

## 🔌 LLM Provider Setup

### Google Gemini (Default - OpenAI-compatible API)

Get your API key from [Google AI Studio](https://aistudio.google.com/apikey)

```bash
# .env
OPENAI_API_KEY=your-gemini-api-key-here
OPENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
DEFAULT_MODEL=gemini-2.0-flash
EMBEDDING_MODEL=text-embedding-004
```

**Available Gemini Models:**
- `gemini-2.0-flash` - Fastest, best for real-time chat (default)
- `gemini-2.0-flash-exp` - Experimental version
- `gemini-2.5-pro` - Most capable, advanced reasoning
- `gemini-2.5-flash` - Balanced speed and capability

**Gemini Features via OpenAI SDK:**
- ✅ Chat completions (streaming supported)
- ✅ Function calling
- ✅ Vision (image understanding)
- ✅ Audio understanding
- ✅ Structured output
- ✅ Embeddings (`text-embedding-004`)
- ✅ Batch API
- ✅ Thinking/reasoning mode (Pro models)

### OpenAI (Alternative)

```bash
# .env
OPENAI_API_KEY=sk-proj-xxx
OPENAI_BASE_URL=https://api.openai.com/v1
DEFAULT_MODEL=gpt-4-turbo
EMBEDDING_MODEL=text-embedding-3-small
```

The service automatically detects the model type and handles accordingly.

---

## 🗄️ Database Migrations

```bash
# Create new migration
poetry run alembic revision --autogenerate -m "Add new table"

# Apply migrations
poetry run alembic upgrade head

# Rollback
poetry run alembic downgrade -1
```

---

## 📊 Monitoring

### Logs

Logs are output to stdout in JSON format (production) or text format (development).

```bash
# View logs
docker logs -f serp-llm

# Or with docker-compose
docker-compose logs -f serp_llm
```

### Metrics

Prometheus metrics available at `/metrics` (if enabled).

---

## 🤝 Integration with Other Services

### Service-to-Service Communication

SERP LLM calls other services via HTTP:

```python
# Example: Get customer data from CRM service
import httpx

async with httpx.AsyncClient() as client:
    response = await client.get(
        f"{settings.crm_service_url}/api/v1/customers/{customer_id}",
        headers={"Authorization": f"Bearer {token}"}
    )
```

### Kafka Events

Subscribe to events from other services:

```python
# Listen to task creation events
# Topic: task.created
# Use for: Auto-suggest subtasks, estimate time, etc.
```

---

## 🛠️ Development

### Code Quality

```bash
# Format code
poetry run black src/

# Lint
poetry run ruff check src/

# Type check
poetry run mypy src/
```

### Adding New Features

Follow Clean Architecture:

1. **Domain**: Add entity in `src/core/domain/entities/`
2. **Port**: Define interface in `src/core/ports/`
3. **Infrastructure**: Implement in `src/infrastructure/`
4. **Service**: Add business logic in `src/core/services/`
5. **Use Case**: Orchestrate in `src/core/usecases/`
6. **API**: Add endpoint in `src/api/routes/`

---

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8087` |
| `ENVIRONMENT` | Environment (development/production) | `development` |
| `DATABASE_URL` | PostgreSQL connection URL | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `OPENAI_API_KEY` | OpenAI/Gemini API key | - |
| `DEFAULT_MODEL` | Default LLM model | `gpt-4-turbo` |
| `KEYCLOAK_SERVER_URL` | Keycloak server URL | - |

See `.env.example` for complete list.

---

## 🚨 Troubleshooting

**Issue: Database connection failed**
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Verify pgvector extension
psql -U postgres -d serp_llm -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```

**Issue: Redis connection failed**
```bash
# Check Redis is running
redis-cli ping
# Should return: PONG
```

**Issue: OpenAI API errors**
- Verify API key is correct
- Check API quota/billing
- Ensure `OPENAI_BASE_URL` is correct

---

## 📄 License

Part of SERP Project

---

## 👤 Author

**QuanTuanHuy**

For questions or issues, please contact the development team.
