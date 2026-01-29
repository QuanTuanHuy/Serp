# SERP - Smart ERP Microservices Architecture Guide

## Architecture Overview

Event-driven microservices ERP using **Clean Architecture**, **Kafka**, and **Keycloak**. All requests flow through API Gateway with JWT.

| Service | Port | Lang | Description |
|---------|------|------|-------------|
| `api_gateway` | 8080 | Go | JWT validation, routing (NO business logic) |
| `account` | 8081 | Java | User/auth/RBAC, Keycloak admin |
| `crm` | 8086 | Java | Customers, leads, opportunities |
| `ptm_task` | 8083 | Go | Personal task management |
| `ptm_schedule` | 8084 | Go | Calendar & scheduling |
| `ptm_optimization` | 8085 | Java | Task optimization |
| `purchase_service` | 8088 | Java | Purchase orders |
| `logistics` | 8089 | Java | Inventory & shipping |
| `notification_service` | 8090 | Go | Push notifications |
| `mailservice` | 8091 | Java | Email templates |
| `serp_llm` | 8089 | Python | AI/RAG (Gemini) |
| `discuss_service` | 8092 | Java | Discussions, attachments (S3), WebSockets |
| `sales` | 8087 | Go | Order management, quotations |
| `serp_web` | 3000 | TS | Next.js 15 + Redux + Shadcn |

**Infra:** PostgreSQL:5432, Redis:6379, Kafka:9092, Keycloak:8180

> ⚠️ **Port Conflict:** `logistics` and `serp_llm` both use 8089. Set `SERVER_PORT` in `.env` to resolve.

## Clean Architecture Layers

All backend services follow this structure:
```
src/
├── core/
│   ├── domain/entity/     # Business entities (domain types)
│   ├── domain/dto/        # Request/response DTOs
│   ├── domain/enum/       # Status, Priority enums
│   ├── mapper/            # Entity ↔ DTO mappers
│   ├── port/store/        # Repository interfaces
│   ├── port/client/       # External client interfaces
│   ├── service/           # Business rules (validation, domain logic)
│   └── usecase/           # Orchestration (transactions, Kafka events)
├── infrastructure/
│   ├── store/adapter/     # Repository implementations
│   ├── store/model/       # DB models (GORM/JPA annotations)
│   ├── store/mapper/      # Model ↔ Entity mappers
│   └── client/            # Redis, Kafka, external APIs
├── ui/
│   ├── controller/        # HTTP handlers
│   ├── router/            # Routes (Go only)
│   ├── kafka/             # Kafka consumers
│   └── middleware/        # Auth, logging (Go only)
└── kernel/
    ├── properties/        # Config from YAML/.env
    └── utils/             # AuthUtils, ResponseUtils
```

**Flow:** Controller → UseCase → Service → Port (interface) → Adapter (impl)

## Key Development Patterns

### Entity vs Model Separation
- **Entities** (`core/domain/entity/`): Business logic, domain types
  - Go: `int64` IDs, `*int64` timestamps (Unix ms)
  - Java: `Long` IDs, Lombok `@SuperBuilder @Getter @Setter extends BaseEntity`
- **Models** (`infrastructure/store/model/`): DB persistence only
  - Go: `time.Time`, GORM tags (`gorm:"not null;index"`)
  - Java: `@Entity @Table @Column`, JPA annotations
- **Mappers** required for conversions (e.g., `TaskEntity` ↔ `TaskModel` via `TaskModelMapper`)

### Go: Uber FX Dependency Injection
All components MUST be registered in `src/cmd/bootstrap/all.go`:
```go
fx.Options(
    fx.Provide(store.NewTaskAdapter),       // Adapter
    fx.Provide(service.NewTaskService),     // Service
    fx.Provide(usecase.NewTaskUseCase),     // UseCase
    fx.Provide(controller.NewTaskController), // Controller
    fx.Invoke(router.RegisterRoutes),       // Routes
)
```
**⚠️ Missing registration = runtime panic (build succeeds)!** See [ptm_task/src/cmd/bootstrap/all.go](../ptm_task/src/cmd/bootstrap/all.go) for example.

### Java: Spring Boot Conventions
- `@RestController @RequestMapping("/api/v1/...")` for endpoints
- `@RequiredArgsConstructor` for constructor injection
- Migrations: Flyway in `src/main/resources/db/migration/V{N}__description.sql`
- Run: `./run-dev.sh` (loads .env) — NOT direct `./mvnw spring-boot:run`

### Transactions & Kafka Events
Use `TransactionService.ExecuteInTransaction` in use cases:
```go
result, err := t.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
    task, err := t.taskService.CreateTask(ctx, tx, userID, request)
    if err != nil { return nil, err }
    err = t.kafkaProducer.SendMessageAsync(ctx, "TASK_TOPIC", task.ID, task)
    return task, err
})
```
**Produce Kafka events after successful DB commit.**

### JWT Authentication Context
All authenticated requests flow through API Gateway. Extract user context:
- **Go:** `utils.GetUserIDFromContext(c)`, `utils.GetTenantIDFromContext(c)`
- **Java:** `authUtils.getCurrentUserId()`, `authUtils.getCurrentTenantId()`

## Development Workflows

### Running Locally
```bash
# 1. Start infrastructure
docker-compose -f docker-compose.dev.yml up -d

# 2. Run services (each in separate terminal)
cd account && ./run-dev.sh       # Java: port 8081
cd ptm_task && ./run-dev.sh      # Go: port 8083
cd serp_llm && ./run-dev.sh      # Python: port 8089
cd serp_web && npm run dev       # Next.js: port 3000
```

### Adding New Features

**Go Services (10 steps):**
1. Entity in `core/domain/entity/` → 2. DTO in `core/domain/dto/`
3. Model in `infrastructure/store/model/` (GORM tags)
4. Mapper in `infrastructure/store/mapper/`
5. Port interface in `core/port/store/`
6. Adapter in `infrastructure/store/adapter/`
7. Service in `core/service/` → 8. UseCase in `core/usecase/`
9. Controller in `ui/controller/` + route in `ui/router/`
10. **Register ALL new components in `cmd/bootstrap/all.go`** ← critical!

**Java Services (9 steps):**
1. Entity (`@SuperBuilder extends BaseEntity`)
2. DTO (request/response with `@Valid`)
3. Model (`@Entity @Table`)
4. Repository (`extends JpaRepository`)
5. Port + Adapter (implements port)
6. Service (`@Service @RequiredArgsConstructor`)
7. UseCase (orchestration, transactions)
8. Controller (`@RestController @RequestMapping`)
9. Migration in `db/migration/V{N}__description.sql`

### Frontend (serp_web)
```
src/
├── app/                 # Next.js pages (crm/[customerId]/, ptm/tasks/)
├── modules/             # Feature modules (self-contained)
│   ├── crm/
│   │   ├── api/         # RTK Query endpoints (crmApi.ts)
│   │   ├── components/  # CRM-specific UI
│   │   ├── store/       # Redux slices
│   │   ├── types/       # TypeScript interfaces
│   │   └── index.ts     # Barrel exports
│   └── settings/, purchase/, logistics/...
├── shared/components/ui/  # Shadcn components
└── lib/store/api/         # Base RTK Query config (apiSlice.ts)
```

**API Pattern:** Use `api.injectEndpoints()` with `extraOptions: { service: 'crm' }`:
```typescript
export const crmApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCustomers: builder.query<...>({
      query: ({ filters, pagination }) => ({ url: '/customers', params: {...} }),
      extraOptions: { service: 'crm' }, // Routes to /crm/api/v1/customers
    }),
  }),
});
```

**Skills:** Read serp frontend module skills for detailed patterns.

**Rules:** Modules are isolated (no cross-imports). Use Shadcn UI. Read `api_gateway` for endpoint routes.
Always run `npm run lint`, `npm run type-check`, and `npm run format` after changes.

## Common Gotchas

1. **Forgot to register FX module (Go)**: Build succeeds but runtime panic → Check `cmd/bootstrap/all.go`
2. **Transaction not rolling back**: Use `txService.ExecuteInTransaction`, not manual `tx.Begin()`
3. **Entity/Model field mismatch**: Mapper must handle all conversions (e.g., Unix ms ↔ `time.Time`)
4. **Environment variables not loading**: Ensure `.env` exists and use `./run-dev.sh`
5. **Keycloak JWT fails**: Check JWKS URL is accessible, realm name matches `application.yml`

## Testing

- **Java**: `@SpringBootTest` with test database (see `account/src/test/`)
- **Go**: Table-driven tests with `testify/mock` for ports
- **Debug**: `docker-compose logs -f <service>`, Keycloak at `localhost:8180`

## File Headers
```go
// Go/Java:
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

// Python:
"""
Author: QuanTuanHuy
Description: Part of Serp Project
"""
```

## API Gateway Philosophy
API Gateway is minimal - only routing, auth, rate limiting. Business logic lives in domain services. Do NOT add feature logic to gateway.
