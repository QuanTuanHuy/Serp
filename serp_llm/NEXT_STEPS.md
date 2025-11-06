# SERP LLM - Next Steps

## ✅ Base Project Setup Complete!

Bạn đã có một base project hoàn chỉnh với:

- ✅ Clean Architecture structure
- ✅ FastAPI với async support
- ✅ SQLAlchemy 2.0 (async) + PostgreSQL
- ✅ Redis cache infrastructure
- ✅ OpenAI client (hỗ trợ cả Gemini)
- ✅ Configuration management (Pydantic Settings)
- ✅ Logging middleware
- ✅ Health check endpoints
- ✅ Docker support
- ✅ Alembic migrations

---

## 🚀 Cách chạy project

### 1. Install dependencies

```bash
cd serp_llm

# With Poetry
poetry install

# Without Poetry (generate requirements.txt first)
poetry export -f requirements.txt --output requirements.txt --without-hashes
pip install -r requirements.txt
```

### 2. Setup environment

```bash
# Copy .env template
cp .env.example .env

# Edit .env - QUAN TRỌNG: Cập nhật các giá trị sau:
# - OPENAI_API_KEY=sk-xxx (hoặc Gemini API key)
# - DATABASE_URL=postgresql+asyncpg://serp:serp@localhost:5432/serp_llm
# - REDIS_HOST=localhost
```

### 3. Setup database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE serp_llm;"

# Enable pgvector (nếu cần vector search sau này)
psql -U postgres -d serp_llm -c "CREATE EXTENSION vector;"

# Run migrations
poetry run alembic upgrade head
```

### 4. Run service

```bash
# Development mode
chmod +x run-dev.sh
./run-dev.sh

# Or manually
poetry run uvicorn src.main:app --reload --host 0.0.0.0 --port 8087
```

### 5. Test

```bash
# Health check
curl http://localhost:8087/health

# API docs
open http://localhost:8087/docs
```

---

## 📋 Next Development Steps

### Phase 1: Core Chat Functionality (Week 1)

#### 1.1 Create Chat DTOs
```bash
src/core/domain/schemas/request/chat_request.py
src/core/domain/schemas/response/chat_response.py
```

**Chat Request:**
- CreateConversationRequest
- SendMessageRequest
- ChatCompletionRequest

**Chat Response:**
- ConversationResponse
- MessageResponse
- StreamingChunkResponse

#### 1.2 Create Repository & Services
```bash
src/core/ports/store/conversation_port.py
src/infrastructure/db/repositories/conversation_repository.py
src/core/services/conversation_service.py
src/core/services/llm_service.py
```

#### 1.3 Create Use Cases
```bash
src/core/usecases/chat_usecase.py
```

**Use cases:**
- CreateConversation
- SendMessage
- GetConversationHistory
- StreamChatCompletion

#### 1.4 Create API Endpoints
```bash
src/api/routes/chat.py
```

**Endpoints:**
- POST /api/v1/chat - Create conversation
- POST /api/v1/chat/{id}/message - Send message
- GET /api/v1/chat/{id}/stream - Stream response (SSE)
- GET /api/v1/chat - List conversations
- GET /api/v1/chat/{id} - Get conversation detail

### Phase 2: Context Building (Week 2)

#### 2.1 Service Clients
```bash
src/infrastructure/http/base_client.py
src/infrastructure/http/crm_client.py
src/infrastructure/http/ptm_task_client.py
src/infrastructure/http/account_client.py
```

#### 2.2 Context Builder Service
```bash
src/core/services/context_builder_service.py
```

**Features:**
- Fetch customer data from CRM
- Fetch task data from PTM Task
- Fetch user info from Account
- Build context prompt

#### 2.3 Auth Middleware
```bash
src/api/middleware/auth_middleware.py
```

**Features:**
- Validate JWT from Keycloak
- Extract user_id, tenant_id
- Service-to-service token management

### Phase 3: Advanced Features (Week 3-4)

#### 3.1 Embeddings & Semantic Search
```bash
src/core/domain/entities/document_entity.py
src/infrastructure/db/models/document_model.py
src/core/services/embedding_service.py
src/core/usecases/semantic_search_usecase.py
src/api/routes/embeddings.py
```

#### 3.2 AI Suggestions
```bash
src/core/domain/entities/suggestion_entity.py
src/infrastructure/db/models/suggestion_model.py
src/core/services/suggestion_service.py
src/core/usecases/suggestion_usecase.py
src/api/routes/suggestions.py
```

#### 3.3 Kafka Integration
```bash
src/infrastructure/messaging/kafka_producer.py
src/infrastructure/messaging/kafka_consumer.py
src/kafka/handlers/task_event_handler.py
src/kafka/handlers/crm_event_handler.py
```

**Events to handle:**
- task.created → Auto-suggest subtasks
- customer.created → Generate customer insights
- lead.updated → Predict conversion probability

### Phase 4: Production Ready (Week 5)

#### 4.1 Testing
```bash
tests/unit/test_llm_service.py
tests/unit/test_conversation_service.py
tests/integration/test_chat_api.py
```

#### 4.2 Monitoring
- Prometheus metrics
- Error tracking
- Performance monitoring
- Token usage tracking

#### 4.3 Rate Limiting & Caching
- Rate limit by user/tenant
- Cache LLM responses
- Cost optimization

---

## 🔧 Utility Commands

### Database Migrations

```bash
# Create new migration
poetry run alembic revision --autogenerate -m "Description"

# Apply migrations
poetry run alembic upgrade head

# Rollback
poetry run alembic downgrade -1

# Show current revision
poetry run alembic current

# Show migration history
poetry run alembic history
```

### Code Quality

```bash
# Format code
poetry run black src/

# Lint
poetry run ruff check src/

# Type check
poetry run mypy src/

# Run all quality checks
poetry run black src/ && poetry run ruff check src/ && poetry run mypy src/
```

### Testing

```bash
# Run all tests
poetry run pytest

# Run with coverage
poetry run pytest --cov=src --cov-report=html

# Run specific test
poetry run pytest tests/unit/test_llm_service.py -v

# Watch mode (install pytest-watch)
poetry run ptw
```

---

## 📝 Example Code

### Create a simple chat endpoint

```python
# src/api/routes/chat.py
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from src.infrastructure.db import get_db
from src.infrastructure.llm import OpenAIClient
from src.core.domain.schemas.request.chat_request import SendMessageRequest
from src.core.domain.schemas.response.chat_response import MessageResponse

router = APIRouter(prefix="/api/v1/chat", tags=["Chat"])

@router.post("/{conversation_id}/message", response_model=MessageResponse)
async def send_message(
    conversation_id: int,
    request: SendMessageRequest,
    db: AsyncSession = Depends(get_db),
):
    # Initialize LLM client
    llm_client = OpenAIClient()
    
    # Build messages
    messages = [
        {"role": "system", "content": "You are a helpful assistant."},
        {"role": "user", "content": request.content}
    ]
    
    # Get completion
    response = await llm_client.chat_completion(messages=messages)
    
    # Save to database (simplified)
    # ... repository logic here
    
    return MessageResponse(
        id=1,
        conversation_id=conversation_id,
        role="assistant",
        content=response["content"],
        tokens_used=response["tokens"]["total"]
    )
```

### Use Redis cache

```python
from src.infrastructure.cache import redis_cache

# In your service
async def get_customer_insight(customer_id: int):
    # Check cache first
    cache_key = f"customer:insight:{customer_id}"
    cached = await redis_cache.get(cache_key)
    
    if cached:
        return cached
    
    # Generate insight
    insight = await generate_insight(customer_id)
    
    # Cache for 1 hour
    await redis_cache.set(cache_key, insight, ttl=3600)
    
    return insight
```

---

## 🐛 Debugging Tips

1. **Check logs**: FastAPI outputs detailed logs in development mode
2. **Use breakpoints**: Add `import pdb; pdb.set_trace()` for debugging
3. **Test LLM client**: Try calling OpenAI API directly first
4. **Database issues**: Check connection with `psql -U serp -d serp_llm`
5. **Redis issues**: Test with `redis-cli ping`

---

## 📚 Resources

- **FastAPI Docs**: https://fastapi.tiangolo.com/
- **SQLAlchemy 2.0**: https://docs.sqlalchemy.org/en/20/
- **OpenAI API**: https://platform.openai.com/docs/api-reference
- **Pydantic**: https://docs.pydantic.dev/latest/
- **Alembic**: https://alembic.sqlalchemy.org/

---

## 🤝 Need Help?

Nếu gặp vấn đề:
1. Check logs trong terminal
2. Verify environment variables trong `.env`
3. Test từng component riêng lẻ
4. Review API docs tại http://localhost:8087/docs

Good luck! 🚀
