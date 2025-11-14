# SERP - Smart ERP (Like Odoo) Microservices Architecture Guide

## Architecture Overview

Event-driven microservices ERP system using **Clean Architecture**, **Kafka messaging**, and **Keycloak authentication**. Services communicate through API Gateway with JWT tokens.

**Core Services:**
- `api_gateway` (Go:8080) - Routes requests, validates JWT, minimal business logic
- `account` (Java:8081) - User/auth/org/RBAC management, Keycloak integration
- `crm` (Java:8086) - Customer/lead/opportunity/activity tracking
- `sales` (Go:8087) - Order management, quotation, pricing
- `ptm_task` (Go:8083), `ptm_schedule` (Go:8084), `ptm_optimization` (Java:8085) - Personal productivity
- `logging_tracker` (Java:8082) - Centralized audit trails & monitoring
- `mailservice` (Java:8087) - Email management, template-based emails
- `serp_web` (Next.js 15) - Redux + Shadcn UI frontend
- `serp_llm` (Python:8087) - AI assistant with Google Gemini integration

**Infrastructure:** PostgreSQL, Redis, Kafka, Keycloak (OAuth2/OIDC), Docker Compose

## Critical Architecture Patterns

### 1. Clean Architecture Layers (Backend Services)
```
src/
├── core/
│   ├── domain/entity/     # Business entities (TaskEntity, CommentEntity)
│   ├── domain/dto/        # Request/response DTOs
│   ├── domain/enum/       # Status, Priority, ActiveStatus enums
│   ├── mapper             # Entity <-> DTO mappers
│   ├── port/store/        # Repository interfaces (*_port.go)
│   ├── port/client/       # Client interfaces (*_port.go)
│   ├── service/           # Domain services (business rules)
│   └── usecase/           # Application use cases (orchestration)
├── infrastructure/
│   ├── store/adapter/     # Database implementations
│   ├── store/model/       # Database models (TaskModel, ProjectModel)
│   ├── store/repository/  # Database repository (only for Java)
│   ├── store/mapper/      # Database mappers (TaskModel <-> TaskEntity)
│   └── client/            # External service clients, Redis client, Kafka producers
├── ui/
│   ├── controller/        # HTTP endpoint handlers
│   ├── router/            # Route definitions (Ony for Go)
│   ├── kafka/             # Kafka message handlers
│   └── middleware/        # Authentication, logging middleware (Only for Go)
└── kernel/
    ├── properties/       # Configuration properties (read from YAML files)
    └── utils/            # Shared utilities (AuthUtils, ResponseUtils, etc)
```

**Key Rule:** Controllers delegate to UseCases → UseCases orchestrate Services → Services contain business logic.

## Key Development Patterns

### 1. Security
JWT (Keycloak) for secure communication between services.

### 2. Entity vs Model Separation
- **Entities** (`core/domain/entity/`): Business models with domain types
  - Go: `int64` IDs, `*int64` timestamps (Unix ms), enums as custom types
  - Java: `Long` IDs, `LocalDateTime` timestamps, Lombok `@SuperBuilder/@Data`
- **Models** (`infrastructure/store/model/`): DB persistence with ORM annotations
  - Go: `time.Time` timestamps, GORM tags (`gorm:"not null;index"`)
  - Java: `@Entity @Table`, JPA annotations, `LocalDateTime` with `@Column`
- **Mappers** convert between layers: `TaskModelMapper.ToEntity(taskModel)`

### 3. Uber FX Dependency Injection (Go Services)
All Go services bootstrap with FX modules in `src/cmd/bootstrap/all.go`:
```go
fx.Options(
    golib.AppOpt(),                          // Base app
    golibdata.DatasourceOpt(),              // PostgreSQL
    golibdata.RedisOpt(),                   // Redis
    fx.Provide(adapter.NewTaskAdapter),     // Infrastructure
    fx.Provide(service.NewTaskService),     // Domain services
    fx.Provide(usecase.NewTaskUseCase),     // Use cases
    fx.Provide(controller.NewTaskController), // Controllers
    fx.Invoke(router.RegisterRoutes),       // Route setup
)
```
**Always register new components here after creation.**

### 4. Java Spring Boot Patterns
- `@RestController` with `@RequestMapping("/api/v1/...")` for all endpoints
- `@RequiredArgsConstructor` for constructor injection (final fields)
- `@ConfigurationProperties(prefix = "app.keycloak")` for config binding from `application.yml`
- Service path: `/crm/api/v1/customers`, `/account/api/v1/users`
- Environment variables loaded from `.env` → `application.yml` (e.g., `${DB_URL}`, `${REDIS_HOST}`)
- Run with `./run-dev.sh` (loads .env first) or `./mvnw spring-boot:run`

### 5. Python FastAPI Patterns (serp_llm)
- Clean Architecture with async SQLAlchemy 2.0 + pgvector
- Google Gemini via OpenAI-compatible SDK (`openai` package)
- Configuration via `.env`: `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `DATABASE_URL`
- Alembic for database migrations: `poetry run alembic upgrade head`
- Run with `./run-dev.sh` or `poetry run uvicorn src.main:app --reload --port 8087`

### 6. Transaction Management
Use `TransactionService.ExecuteInTransaction` in use cases for multi-step operations:
```go
// Go pattern
result, err := t.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
    task, err := t.taskService.CreateTask(ctx, tx, userID, request)
    if err != nil { return nil, err }
    err = t.taskService.PushCreateTaskToKafka(ctx, task) // Kafka after DB success
    return task, err
})
```

### 7. Kafka Event Pattern
- **Produce** events in use cases (after successful DB commit) via `IKafkaProducerPort`
- **Consume** events in `ui/kafka/` handlers with `HandleMessage(ctx, topic, key, value)`
- Event topics: `TASK_TOPIC`, `USER_EVENT_TOPIC` (defined in `domain/constant/`)
- Use async producers (`SendMessageAsync`) for non-critical events

### 8. JWT Authentication Flow
1. User logs in → Account service → Keycloak → JWT access token
2. Frontend sends `Authorization: Bearer <token>` → API Gateway
3. Gateway validates JWT via Keycloak JWKS → Extracts user ID → Forwards JWT to backend
4. Backend services validate JWT via `JWTMiddleware` (Go) or Spring Security filter (Java)
5. Controllers extract `userID`, `tenantID` from context: `utils.GetUserIDFromContext(c)` (Go) or `AuthUtils.getCurrentUserId()` (Java)

**Service-to-service:** Use Keycloak service account tokens (client credentials flow).

### 9. Database & Configuration Management
- **Java services**: Use Flyway for migrations (in `src/main/resources/db/migration/`)
- **Python services**: Use Alembic for migrations (in `alembic/versions/`)
- **Go services**: Manual migration scripts or GORM AutoMigrate for dev
- **Configuration loading**: `.env` files → Environment variables → `application.yml` (Java) or properties structs (Go)

## Development Workflows

### Running Services Locally
```bash
# 1. Start infrastructure (PostgreSQL, Redis, Kafka, Keycloak)
docker-compose -f docker-compose.dev.yml up -d

# 2. Run services with environment variables from .env files
cd account && ./run-dev.sh  # Java services use ./mvnw spring-boot:run
cd ptm_task && ./run-dev.sh # Go services use go run src/main.go
cd serp_llm && ./run-dev.sh # Python services use poetry run uvicorn
cd serp_web && npm run dev  # Frontend on :3000
```

**Environment Files:** Each service has `.env` with `DB_URL`, `REDIS_HOST`, `KAFKA_BOOTSTRAP_SERVERS`, `CLIENT_SECRET`

**Infrastructure URLs:**
- PostgreSQL: `localhost:5432` (user: serp, pass: serp)
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- Keycloak: `http://localhost:8180` (admin: serp-admin/serp-admin)

### Adding New Features (Backend)

**Go Services:**
1. Define entity in `core/domain/entity/` (business fields, domain types)
2. Define DTO in `core/domain/dto/request|response/`
3. Create model in `infrastructure/store/model/` (GORM annotations)
4. Create mapper in `infrastructure/store/mapper/` (ToEntity/ToModel)
5. Create port interface in `core/port/store/` (`ITaskPort`)
6. Implement adapter in `infrastructure/store/adapter/` (GORM queries)
7. Add service in `core/service/` (validation, business rules)
8. Add use case in `core/usecase/` (transaction orchestration, Kafka)
9. Add controller in `ui/controller/` (HTTP handlers, response utils)
10. Register route in `ui/router/` with middleware
11. **Register all components in `cmd/bootstrap/all.go`**

**Java Services:**
1. Entity (`@SuperBuilder @Getter @Setter extends BaseEntity`)
2. DTO (request/response with `@Valid` annotations)
3. Model (`@Entity @Table` with JPA annotations)
4. Mapper (MapStruct or manual methods)
5. Repository (`extends JpaRepository<Model, Long>`)
6. Port interface in `core/port/store/`
7. Adapter implements port, uses repository
8. Service (`@Service @RequiredArgsConstructor`, business logic)
9. UseCase (orchestration, transaction, Kafka producer)
10. Controller (`@RestController @RequestMapping`, delegates to UseCase)

**Python Services (serp_llm):**
1. Entity in `src/core/domain/entities/` (Pydantic BaseModel)
2. DTO in `src/core/domain/dto/` (request/response schemas)
3. Model in `src/infrastructure/db/models/` (SQLAlchemy declarative)
4. Port interface in `src/core/port/` (Protocol or ABC)
5. Adapter in `src/infrastructure/` (implements port)
6. Service in `src/core/service/` (async business logic)
7. UseCase in `src/core/usecase/` (orchestration)
8. Router in `src/ui/api/routes/` (FastAPI router)
9. Run Alembic migration: `poetry run alembic revision --autogenerate -m "description"`

**Key: Use cases orchestrate, controllers validate input, services enforce rules.**

### Frontend Structure (serp_web)
```
src/
├── app/
│   ├── crm/[customerId]/    # Dynamic routes
│   └── ptm/tasks/           # Module pages
├── modules/                  # Feature modules (self-contained)
│   ├── crm/
│   │   ├── components/      # CRM-specific UI (CustomerCard, LeadForm)
│   │   ├── hooks/           # useCustomers(), useLeads() - RTK Query
│   │   ├── services/        # crmApi.ts (RTK Query endpoints)
│   │   ├── store/           # customerSlice.ts (Redux state)
│   │   ├── types/           # Customer, Lead types
│   │   └── index.ts         # Barrel exports
│   └── ptm/                 # Same pattern
├── shared/
│   ├── components/ui/       # Shadcn components (Button, Card, Dialog)
│   ├── hooks/               # useAuth(), useDebounce()
│   ├── services/api/        # Base API config (RTK Query baseQuery)
│   └── utils/               # formatDate(), cn() classnames
└── lib/
    └── store.ts             # Redux store with RTK Query middleware
```

**Rules:** Modules are isolated (no cross-imports), Barrel exports, communicate via Redux. Use Shadcn UI components. API calls via RTK Query. Always read `api_gateway` for endpoint details.

## Common Gotchas & Solutions

1. **Forgot to register FX module (Go)**: Build succeeds but runtime panic. → Check `cmd/bootstrap/all.go`
2. **Transaction not rolling back**: Use `txService.ExecuteInTransaction`, not manual `tx.Begin()`
3. **Entity/Model field mismatch**: Mapper must handle all conversions (e.g., Unix ms ↔ `time.Time`)
4. **Environment variables not loading**: Ensure `.env` exists and `run-dev.sh` is used (not direct `mvnw`/`go run`)
5. **Keycloak JWT validation fails**: Check JWKS URL is accessible and realm name matches in `application.yml`

## Testing & Debugging

- **Java tests**: Use `@SpringBootTest` with test database. See `account/src/test/`
- **Go tests**: Table-driven tests with mocks. Use `testify/mock` for ports
- **View logs**: `docker-compose -f docker-compose.dev.yml logs -f <service-name>`
- **Database**: Connect to PostgreSQL at `localhost:5432` (user: serp, pass: serp)
- **Keycloak admin**: `http://localhost:8180` (admin: serp-admin/serp-admin)
- **Check Kafka topics**: Use Kafka UI or `kafka-console-consumer`

## File Headers
All Go/Java files must start with:
```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```

Python files use:
```python
"""
Author: QuanTuanHuy
Description: Part of Serp Project
"""
```

## API Gateway Philosophy
API Gateway is minimal - only routing, auth, rate limiting. Business logic lives in domain services (account, crm, etc.). Do NOT add feature logic to gateway.
