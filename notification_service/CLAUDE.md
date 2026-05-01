# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Notification service

Go 1.25 service for real-time user notifications. Uses Gin, Uber FX, Gorm/PostgreSQL, Redis, Sarama/Kafka, and Gorilla WebSocket.

Default port: `8090`. Base API path: `/notification/api/v1`. WebSocket endpoint: `/notification/ws`. Config: `src/config/default.yaml`, `src/config/local.yaml`, `src/config/production.yaml`.

## Commands

Run from `notification_service/`.

```bash
./run-dev.sh
./run-prod.sh
go run src/main.go
go build -o bin/app src/main.go
go test ./...
go test ./src/core/service
go test ./src/core/service -run TestValidateNotification -v
go test ./src/core/service -run TestMarkAsRead -v
go test ./src/core/websocket -run TestConcurrentConnections -v
go test ./... -run TestCreateSetsUserStatusAndInvalidates -v
go test ./src/core/websocket -bench BenchmarkWebsocketBroadcast -run ^$
go fmt ./...
go vet ./...
```

No dedicated golangci-lint config exists. Use `go fmt`, `go vet`, and `go test` as quality gates.

## Architecture

- `src/main.go`: entrypoint.
- `src/cmd/bootstrap`: FX wiring, config, infra, startup hooks.
- `src/core/domain`: entities, DTOs, enums, constants, mappers.
- `src/core/port`: store and client interfaces.
- `src/core/service`: domain/business services.
- `src/core/usecase`: application orchestration.
- `src/core/websocket`: hub, client, WebSocket message types.
- `src/infrastructure/client`: Redis and Kafka adapters.
- `src/infrastructure/store`: Gorm adapters, models, mappers.
- `src/kernel`: shared utils and config properties.
- `src/ui`: controllers, middleware, router, Kafka handlers.

Dependency flow: `ui` -> `core/usecase` -> `core/service` -> `core/port/*`; infrastructure implements ports; `cmd/bootstrap` assembles with FX.

## Conventions

- New Go files use the standard author header from nearby files.
- Run `go fmt ./...` after edits.
- Keep controllers, middleware, and Kafka handlers thin; business rules belong in services and use cases.
- Use `ITransactionService.ExecuteInTransaction` for multi-step writes. Pass `*gorm.DB` explicitly to write methods.
- Do not let controllers or handlers talk directly to Gorm adapters.
- Use `utils.ValidateAndBindJSON` and `utils.ValidateAndBindQuery` in controllers.
- Return `error`; do not panic for business failures.
- Wrap infrastructure errors with `fmt.Errorf("...: %w", err)`.
- Register new Kafka consumers and handlers in `src/cmd/bootstrap/all.go` and `src/cmd/bootstrap/kafka_consumer.go`.
- Route Kafka message processing through `MessageProcessingMiddleware` so idempotency, retry, and DLQ behavior stay intact.
- Delivery flows through `DeliveryService` and WebSocket hub, not directly from controllers.
- The hub supports multiple concurrent clients per user; do not assume one connection per user.
- Use `zap.Logger`; never log secrets, raw JWTs, or sensitive user payloads.
- Every new dependency, service, controller, or adapter must be registered in `src/cmd/bootstrap/all.go`.

## Testing

Tests use the standard `testing` package, hand-written stubs, and `zap.NewNop()`. Current tests live mainly in `src/core/service` and `src/core/websocket`. Prefer focused unit tests around services, idempotency behavior, hub concurrency, and cache side effects.
