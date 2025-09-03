# Smart ERP (Like Odoo) - AI Coding Guide

## Architecture Overview

This is a **microservices architecture** with event-driven communication using **Kafka** and **clean architecture** patterns. The system consists of:

- **api_gateway** (Go, port 8080) - API gateway/router with minimal endpoints
- **account** (Java, Spring Boot, port 8081) - User account management service
- **logging_tracker** (Java, Spring Boot, port 8082) - Track logging of microservices
- **serp_web** (NextJS, Redux, Shadcn, Tailwind CSS) - Frontend application

## Go Services Architecture Pattern

### Dependency Injection with Uber FX
All Go services use **Uber FX** for dependency injection. Each service follows this bootstrap pattern:

```go
// src/main.go
func main() {
    fx.New(bootstrap.All()).Run()
}

// src/cmd/bootstrap/all.go - Essential DI pattern
fx.Options(
    golib.AppOpt(),                    // Core app setup
    golibdata.RedisOpt(),             // Redis connection
    golibdata.DatasourceOpt(),        // PostgreSQL connection
    fx.Provide(adapter.NewXXXAdapter), // Infrastructure layer
    fx.Provide(service.NewXXXService), // Domain services  
    fx.Provide(usecase.NewXXXUseCase), // Business logic
    fx.Provide(controller.NewXXXController), // HTTP handlers
)
```

## Java Services Architecture Pattern

### Spring Boot with Clean Architecture
use standard Spring patterns:
- `@RestController` for HTTP endpoints with path `/api/v1/`
- `@Service` classes for business logic 
- `@Repository` interfaces with JPA
- `@Configuration` classes for security and JWT setup
- **Lombok** annotations (`@RequiredArgsConstructor`, `@Data`) throughout


### Clean Architecture Layers
```
src/
├── core/
│   ├── domain/entity/     # Business entities (TaskEntity, CommentEntity)
│   ├── domain/dto/        # Request/response DTOs
│   ├── domain/enum/       # Status, Priority, ActiveStatus enums
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
    └── utils/            # Shared utilities
```

## Key Development Patterns

### 1. Security
JWT (Public/Private Key) for secure communication between services.

### 2. Configuration Management
**YAML-based with environment overrides:**
- Go: `config/{default,local,production}.yaml` + golib config binding
- Java: `application.yaml` + `@ConfigurationProperties` classes

### 3. Error Handling & Permissions
Business logic follows this pattern ():
- Always validate request before operations
- Handle errors gracefully and return appropriate HTTP responses

### 4. Kafka Event Handling
Message handlers in `ui/kafka/` follow this pattern:
```go
func (h *Handler) HandleMessage(ctx context.Context, topic string, key string, value []byte) error {
    log.Info(ctx, "Handling message for topic: ", topic)
    // Process event asynchronously
}
```

## Development Workflows

### Running Services
```bash
# Start infrastructure (from project root)
docker-compose up -d

# Run Go services individually
cd api_gateway && go run src/main.go

# Run Spring Boot services
cd account && ./mvnw spring-boot:run

# Run React frontend
cd serp_web && npm run dev
```

### Service Communication
- **Frontend** → **api_gateway** → **backend services**

### Frontend Configuration

## Critical Patterns to Follow

### 1. Port Adapter Pattern
Always implement port interfaces(redis, database store, message producer) in `core/port/` before creating adapters in `infrastructure/`

### 2. Use Case Orchestration  
Business logic belongs in use cases, not controllers. Use cases coordinate between services and handle cross-cutting concerns like permissions.

### 3. Entity Consistency
Domain entities use consistent field patterns:
- `ID int64` for primary keys
- `CreatedAt, UpdatedAt int64` for timestamps  
- `UserID int64` for ownership tracking

Model use consistent field patterns:
- CreatedAt, UpdatedAt time.Time(for Golang) and LocalDateTime(for Java)

### 4. Kafka Integration
Events are produced in use cases and consumed by dedicated handlers. Each service can both produce and consume relevant domain events.

## When Adding New Features

### Go Services
1. **Define entity** in `core/domain/entity/`
2. **Create repository interface** in `core/port/store/`
3. **Create model** in `core/port/store/model/`
4. **Create mapper** in `core/port/store/mapper/`
5. **Implement adapter** in `infrastructure/store/adapter/`
6. **Add service** for business rules in `core/service/`
7. **Create use case** for orchestration in `core/usecase/`
8. **Add controller** for HTTP endpoints in `ui/controller/`
9. **Register dependencies** in `cmd/bootstrap/all.go`
10. **Add routes** in `ui/router/`

### Java Services
1. **Define entity** in `core/domain/entity/`
2. **Create repository interface** in `core/port/store/`
3. **Create model** in `core/port/store/model/`
4. **Create mapper** in `core/port/store/mapper/`
5. **Implement adapter** in `infrastructure/store/adapter/`
6. **Add service** for business rules in `core/service/`
7. **Create use case** for orchestration in `core/usecase/`
8. **Add controller** for HTTP endpoints in `ui/controller/`

### Add (authors: QuanTuanHuy, Description: Part of Serp Project) to all relevant files

The api_gateway acts as a simple API gateway - most business logic should be added to serp_account, crm, serp_notification, ptm_task_manager, ptm_schedule, sale, hr or appropriate domain services.
