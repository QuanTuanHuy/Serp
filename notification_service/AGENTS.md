# AGENTS.md - Guide for AI Coding Agents

This guide is specific to `notification_service/`.
Use it when reading, changing, or extending the Notification Service.

## Module Overview
`notification_service` is a Go 1.25 service for real-time user notifications.
It uses Gin for HTTP, Uber FX for dependency injection and lifecycle, Gorm for PostgreSQL, Redis for caching, Sarama for Kafka, and Gorilla WebSocket for push delivery.

Main layout:
```text
src/
├── main.go
├── cmd/bootstrap/        # FX wiring, config, infra, startup hooks
├── core/
│   ├── domain/           # entities, DTOs, enums, constants, mappers
│   ├── port/             # store and client interfaces
│   ├── service/          # domain/business services
│   ├── usecase/          # application orchestration
│   └── websocket/        # hub, client, WebSocket message types
├── infrastructure/
│   ├── client/           # Redis and Kafka adapters
│   └── store/            # Gorm adapters, models, mappers
├── kernel/               # shared utils and config properties
└── ui/                   # controllers, middleware, router, Kafka handlers
```

Respect the dependency direction:
- `ui` -> `core/usecase`
- `core/usecase` -> `core/service`
- `core/service` -> `core/port/*`
- `infrastructure` implements ports
- `cmd/bootstrap` assembles everything with FX

## Build, Run, Test, and Quality Commands
Run commands from `notification_service/`.

### Development / Build
```bash
./run-dev.sh                     # load .env and run the service
./run-prod.sh                    # load .env.prod and run the service
go run src/main.go               # start directly
go build -o bin/app src/main.go  # build binary
```

### Tests
```bash
go test ./...
go test ./src/core/service
go test ./src/core/service -run TestValidateNotification -v
go test ./src/core/service -run TestMarkAsRead -v
go test ./src/core/websocket -run TestConcurrentConnections -v
go test ./... -run TestCreateSetsUserStatusAndInvalidates -v
```
Use package-scoped `go test ./path/to/pkg -run TestName` for a single test.
Current tests live mainly in `src/core/service` and `src/core/websocket`.

### Benchmark / Load-style Test
```bash
go test ./src/core/websocket -bench BenchmarkWebsocketBroadcast -run ^$
```

### Formatting / Lint / Static Checks
```bash
go fmt ./...
go vet ./...
go test ./...
```
There is no dedicated golangci-lint config in this module, so `go fmt`, `go vet`, and `go test` are the practical local quality gate.

## Runtime and Configuration Notes
- Main config files: `src/config/default.yaml`, `src/config/local.yaml`, `src/config/production.yaml`
- Default port: `8090`
- Base API path: `/notification/api/v1`
- WebSocket endpoint: `/notification/ws`
- `run-dev.sh` loads `.env`; config is merged via Viper and supports `${VAR}` / `${VAR:-default}` expansion

## Architecture Rules
- Keep controllers, middleware, and Kafka handlers thin; business rules belong in services and use cases.
- Put transaction orchestration in `transaction_service.go` and pass `*gorm.DB` explicitly to write methods.
- Do not let controllers or handlers talk directly to Gorm adapters.
- Keep store logic in `infrastructure/store/adapter` and mapping in `infrastructure/store/mapper`.
- Keep Kafka idempotency, retry, and DLQ behavior inside `ui/kafka/message_processing_middleware.go` and related services.

## File and Naming Conventions
- New Go files should keep the module header style:
```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```
- Interfaces use the `I` prefix: `INotificationService`, `ITransactionService`, `INotificationPort`.
- Constructors use `New...`: `NewNotificationService`, `NewHub`, `NewJWTMiddleware`.
- Domain structs use suffixes like `*Entity`, `*Model`, `*Response`, `*Request`, `*Adapter`, `*UseCase`, `*Service`.
- Enum-like string types live in `core/domain/enum` and constants in `core/domain/constant`.
- Preserve existing package names even when directories are unusual, such as `src/ui/controller.go`.

## Imports and Formatting
- Follow normal Go grouping: standard library, external dependencies, then internal `github.com/serp/notification-service/...` imports.
- Use aliases only when they clarify intent or disambiguate packages, such as `client`, `store`, `adapter`, `ws`, or `kafkahandler`.
- Let `go fmt` control indentation and spacing; do not hand-format against it.
- Keep exported names in `PascalCase`, unexported names in `camelCase`, constants in `PascalCase` or `UPPER_SNAKE_CASE` only when already established.
- Prefer small focused functions and early returns for invalid state.

## Types and Function Signatures
- Use `int64` for IDs and epoch-millisecond timestamps in domain entities.
- Gorm models use `time.Time`; mapping layers convert to and from domain types.
- Pass `context.Context` as the first parameter in service, adapter, and port methods.
- For write paths, pass `*gorm.DB` explicitly so the caller controls the transaction.
- Use pointers in request/response/domain structs for optional values like `*bool`, `*int64`, `*string`.
- Prefer `map[string]any` and `json.RawMessage` only where payloads are intentionally dynamic.

## Validation and DTO Rules
- Request DTOs use `json`, `form`, and `binding` tags; preserve these when adding fields.
- Query parameter DTOs commonly embed `BaseParams`.
- Use `utils.ValidateAndBindJSON` and `utils.ValidateAndBindQuery` in controllers instead of duplicating binding logic.
- Prefer validator tags for structural validation and service logic for business validation.
- Keep validation error responses consistent with `kernel/utils/validator.go` and `kernel/utils/response.go`.

## Error Handling
- Return `error`; do not panic for normal business failures.
- Use `errors.New(...)` for module business errors backed by `core/domain/constant/bussiness_error.go`.
- Use `fmt.Errorf("...: %w", err)` when wrapping infrastructure or adapter failures.
- Let controllers route failures through `utils.ErrorHandle`, `utils.AbortErrorHandle`, or `utils.AbortErrorHandleCustomMessage`.
- Keep HTTP/business error mapping centralized in `core/domain/constant` and `kernel/utils/response.go`.

## Transactions, Cache, and Async Work
- Use `ITransactionService.ExecuteInTransaction` for multi-step writes.
- Start transactions through the transaction service, not ad hoc `db.Begin()` calls in business code.
- Notification and preference services update Redis caches asynchronously with goroutines; preserve cache invalidation when changing write paths.
- If you add a new write operation that affects unread counts or preferences, update the matching Redis keys.
- Be careful when using goroutines: do not capture mutable request state unsafely.

## Kafka and Idempotency Rules
- Register new Kafka consumers and handlers in `src/cmd/bootstrap/all.go` and `src/cmd/bootstrap/kafka_consumer.go`.
- Route Kafka message processing through `MessageProcessingMiddleware` so idempotency, retry, and DLQ behavior stay intact.
- Keep event metadata in `core/domain/dto/message/base.go` consistent with upstream producers.
- When adding a new event type, update constants, handler routing, request binding, and tests together.

## WebSocket and Delivery Rules
- WebSocket connections are authenticated from the `token` query parameter and then mapped to user/tenant context.
- Delivery flows through `DeliveryService` and the WebSocket hub, not directly from controllers.
- The hub supports multiple concurrent clients per user; do not assume one user equals one connection.
- Preserve non-blocking broadcast behavior and client cleanup semantics in `core/websocket`.

## Logging Guidelines
- Use `zap.Logger` consistently.
- Log structured fields like `userID`, `tenantID`, `notificationID`, `event_id`, `topic`, and `key`.
- Use `Info` for lifecycle and successful processing, `Warn` for recoverable conditions, and `Error` for failed I/O or unexpected state.
- Never log secrets, raw JWTs, or sensitive user payloads unless absolutely necessary.

## Testing Style
- Tests use the standard `testing` package, hand-written stubs, and `zap.NewNop()`.
- Keep tests table-free if a direct stub setup is clearer; match the style already used in `notification_service_test.go`.
- Prefer focused unit tests around services, idempotency behavior, and hub concurrency behavior.
- When adding new logic, test both success and failure paths, plus cache side effects if relevant.
- Use `t.Run(...)` for grouped scenarios when it improves readability.

## Module-Specific Gotchas
- Every new dependency, service, controller, or adapter must be registered in `src/cmd/bootstrap/all.go`, or FX startup will fail.
- The directory `src/ui/controller.go` is intentional; do not rename it casually during unrelated changes.
- `InitializeDB` currently uses `AutoMigrate`; schema changes may have startup effects, so review model changes carefully.
- User and tenant IDs come from Gin context helpers in `kernel/utils/context_utils.go`; do not re-parse them in each controller.
- Kafka consumer startup is lifecycle-managed through FX hooks; keep start/stop logic symmetric.
