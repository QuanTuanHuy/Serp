# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## API gateway

Go 1.25+ service using Gin, Uber FX, Redis, gobreaker, and golib/golib-gin. Role: central HTTP and WebSocket gateway for SERP services.

Entrypoint: `src/main.go`. DI bootstrap: `src/cmd/bootstrap/all.go`. Runtime config: `src/config/default.yaml`, `src/config/local.yaml`, `src/config/production.yaml`.

## Commands

Run from `api_gateway/`.

```bash
go mod download
./run-dev.sh
./run-prod.sh
go run src/main.go
go build -o bin/api-gateway ./src/main.go
go build ./src
go fmt ./...
go vet ./...
go test ./...
go test -short ./...
go test ./src/ui/controller/common
go test ./src/ui/controller/common -run '^TestGenericProxyController_CRM_POSTDoesNotRetry$' -count=1
go test ./src/ui/controller/common -run '^TestWebSocketProxyController_Discuss_UpgradeAndPathRewrite$' -count=1
go test ./src/ui/controller/common -bench . -run '^$'
```

No module-local `golangci-lint` config exists. Use `go fmt ./...`, `go vet ./...`, and `go test ./...` as checks. Use `-count=1` for targeted reruns.

## Architecture

Core flow: router -> controller -> service -> port/adapter. Controllers must not call adapters directly.

- `src/ui/router/*_router.go`: route registration only.
- `src/ui/controller/**`: HTTP handlers by domain.
- `src/core/service/**`: gateway orchestration logic.
- `src/core/port/**`: port interfaces with `I` prefix.
- `src/infrastructure/client/**`: downstream HTTP client adapters.
- `src/infrastructure/adapter/**`: shared infrastructure adapters.
- `src/kernel/properties/**`: config structs with `Prefix()`.
- `src/kernel/utils/**`: auth, validation, response, transport, JWT helpers.
- `src/cmd/modules/*.go` and `src/cmd/bootstrap/all.go`: FX wiring.

## Conventions

- New Go files use the standard author header from nearby files.
- Run `go fmt ./...` after edits.
- Controllers bind/validate input, call one service method, abort on error, then return `c.JSON(res.Code, res)`.
- Reuse validation helpers: `ValidateAndParseID`, `ValidatePaginationParams`, `ValidateAndBindJSON`, `ValidateAndBindQuery`, `ParseInt64Query`, `ParseStringQuery`.
- Use `GenericProxyController` for pure pass-through routing; use dedicated controllers only for custom validation, rewriting, auth context, or response shaping.
- Downstream adapters use `utils.BuildHeadersFromContext(ctx)` for Authorization forwarding.
- Use `JWTMiddleware.AuthenticateJWT()` when claim inspection or user rate limiting is needed.
- Use `utils.BaseAPIClient`, `utils.NewDefaultCircuitBreaker()`, and `utils.NewResilientTransport(...)` instead of one-off HTTP client patterns.
- Register new providers in `src/cmd/modules/*.go` and `src/cmd/bootstrap/all.go`.
- If adding a downstream service, update `src/kernel/properties/external_service_properties.go` and all three config YAML files.

## Testing

Existing tests focus on reverse proxy and WebSocket behavior in `src/ui/controller/common`. Use `gin.SetMode(gin.TestMode)` and `httptest.NewServer`. Add focused tests for path rewriting, header forwarding, auth behavior, rate limiting, retry behavior, and circuit breaker behavior.
