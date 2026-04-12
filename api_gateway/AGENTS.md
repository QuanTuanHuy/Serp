# AGENTS.md - API Gateway Guide for Coding Agents

This file is specific to `api_gateway/`.
Use it together with the repository-level `AGENTS.md` in the repo root.

## Module Overview

- Language: Go 1.25+
- Frameworks/libraries: Gin, Uber FX, Redis, gobreaker, golib/golib-gin
- Role: central HTTP and WebSocket gateway for SERP services
- Entrypoint: `src/main.go`
- DI bootstrap: `src/cmd/bootstrap/all.go`
- Runtime config: `src/config/default.yaml`, `src/config/local.yaml`, `src/config/production.yaml`
- Core flow: router -> controller -> service -> port/adapter
- Do not make controllers call adapters directly.

## Build, Run, Lint, and Test Commands

Run commands from `api_gateway/`.

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

- There is no module-local `golangci-lint` config today; `go vet ./...` is the standard lint/static check.
- `go test -short ./...` is useful because the WebSocket load test is intentionally heavier.
- `-count=1` is recommended for targeted reruns so Go does not reuse cached results.

## Project Layout Conventions

- `src/ui/router/*_router.go`: route registration only.
- `src/ui/controller/**`: HTTP handlers grouped by domain (`account`, `crm`, `ptm`, `common`).
- `src/core/service/**`: gateway orchestration logic.
- `src/core/port/**`: port interfaces; interfaces use the `I` prefix.
- `src/infrastructure/client/**`: downstream HTTP client adapters.
- `src/infrastructure/adapter/**`: shared infrastructure adapters such as Redis rate limiting.
- `src/kernel/properties/**`: config structs with `Prefix()` and constructor helpers.
- `src/kernel/utils/**`: auth, validation, response, transport, JWT, and helper code.
- `src/cmd/modules/*.go` and `src/cmd/bootstrap/all.go`: FX wiring; new providers must be registered here.

## Code Style Guidelines

### File headers

- New Go source files should use the standard header block used throughout this module:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/
```

- Match nearby file style if you are editing an older file that already uses a slightly different comment shape.

### Imports

- Group imports in normal Go order: standard library, blank line, third-party/internal packages.
- Keep aliases consistent with existing local patterns: `request`, `response`, `port`, `service`, `adapter`, `account`, `crm`, `ptm`.
- Remove unused imports; rely on `go fmt` and the compiler to keep imports clean.

### Formatting

- Always run `go fmt ./...` after edits.
- Use standard `gofmt` layout and tabs; do not hand-align code.
- Prefer short functions, early returns, and small helper functions over nested logic.
- Split long constructor or route-registration signatures across lines like the existing code.
- Separate validation, service call, and response-writing steps with blank lines.

### Naming

- Exported identifiers: `PascalCase`.
- Unexported identifiers: `camelCase`.
- Interfaces: `IThingService`, `IThingClientPort`, `IRateLimiterPort`.
- Constructors: `NewThingController`, `NewThingService`, `NewThingClientAdapter`.
- Files: snake_case with role suffixes such as `user_service.go`, `crm_router.go`, `user_client_adapter.go`, `jwt_middleware.go`, `*_test.go`.
- Package names stay lowercase and generic by layer: `controller`, `service`, `adapter`, `router`, `middleware`, `utils`, `properties`, `port`.

### Types and function signatures

- Service and adapter methods should accept `context.Context`; pass `c.Request.Context()` from controllers.
- Keep `*gin.Context` in routers, controllers, and middleware only.
- Use pointer fields for optional query params and optional request data where the existing DTOs already do so.
- Return interface types from constructors when the package already exposes interface-based boundaries.
- Use `map[string]any` only when the gateway is intentionally forwarding flexible downstream payloads.
- Prefer dedicated DTO structs plus validation helpers for stable request contracts.

### Controllers and routers

- Controllers should stay thin: bind/validate input, call one service method, abort on error, then return `c.JSON(res.Code, res)`.
- Reuse existing helpers before adding new parsing logic: `ValidateAndParseID`, `ValidatePaginationParams`, `ValidateAndBindJSON`, `ValidateAndBindQuery`, `ParseInt64Query`, `ParseStringQuery`, and related helpers.
- Put new routes in the feature router file, then ensure that router is invoked from `src/ui/router/router.go`.
- Use `GenericProxyController` for pure pass-through routing.
- Use a dedicated controller when the gateway needs custom validation, path/query rewriting, auth-context handling, or response shaping.

### Auth and context propagation

- `middleware.AuthMiddleware()` forwards the bearer token into request context for downstream clients, but does not validate JWT claims.
- `JWTMiddleware.AuthenticateJWT()` validates the token and sets `userID`, `roles`, and related claim data on Gin context.
- If a route needs claim inspection or `UserRateLimit()`, use `JWTMiddleware.AuthenticateJWT()`.
- Downstream client adapters should call `utils.BuildHeadersFromContext(ctx)` so Authorization forwarding stays consistent.

### Error handling and logging

- Prefer early returns on every validation or downstream failure.
- In controllers and middleware, use `utils.AbortErrorHandle`, `utils.AbortErrorHandleCustomMessage`, and `utils.AbortValidationError` instead of ad-hoc JSON payloads.
- Use constants from `src/core/domain/constant/error.go`; avoid hard-coded status/error numbers.
- In adapters, wrap errors with `%w` and log non-2xx downstream responses before unmarshalling.
- Use `github.com/golibs-starter/golib/log`, not `fmt.Println`.
- Preserve the existing downstream `BaseResponse` contract unless you are intentionally changing the API.

### HTTP client and resilience patterns

- Reuse `utils.BaseAPIClient`; do not create one-off HTTP client patterns unless necessary.
- Adapter calls typically use `utils.NewDefaultCircuitBreaker()`.
- HTTP reverse proxy transport should keep using `utils.NewResilientTransport(...)`.
- WebSocket proxy transport should keep using circuit breaker behavior without retry logic.
- Only idempotent methods should be retried in this module.

### Configuration

- New config structs belong in `src/kernel/properties` and should implement `Prefix()`.
- Use `mapstructure` tags when YAML field names do not match plain Go naming.
- If you add a new downstream service, update `src/kernel/properties/external_service_properties.go` and all three config YAML files.
- WebSocket services also need `WebSocketPath` config and registration in `NewWebSocketProxyController`.

## Adding New Gateway Functionality

- For a pure proxy route, prefer adding service mapping/config plus router wiring instead of building a custom controller.
- For a custom endpoint, add the port interface, adapter, service, controller, module registration, and router registration together.
- If downstream calls require Authorization forwarding, make sure the route uses `AuthMiddleware()` or `JWTMiddleware()` and the adapter uses `BuildHeadersFromContext(ctx)`.
- If the route needs user claims or user-scoped rate limiting, use `JWTMiddleware.AuthenticateJWT()`.
- For new WebSocket traffic, update service config, `NewWebSocketProxyController`, and the feature router together.

## Testing Guidelines

- Existing automated tests live in `src/ui/controller/common` and focus on reverse proxy and WebSocket behavior.
- Use `gin.SetMode(gin.TestMode)` in Gin tests.
- Use `httptest.NewServer` for both upstream mocks and gateway instances.
- Test names follow `TestType_Behavior`; benchmarks follow `BenchmarkType_Behavior`.
- Add focused tests for path rewriting, header forwarding, auth behavior, rate limiting, retry behavior, and circuit breaker behavior.
- Extend the nearest package tests for new middleware or proxy behavior rather than relying only on manual verification.

## Module-Specific Gotchas

- Register every new provider in both `src/cmd/modules/*.go` and `src/cmd/bootstrap/all.go`, or FX startup will fail.
- Route registration happens under `p.Engine.Group(p.App.Path())`; do not accidentally bypass the application base path.
- `ServiceProperty.BaseURL()` prepends `http://` when the host lacks a scheme; do not double-prefix URLs.
- Many account routes use `AuthMiddleware()` rather than `JWTMiddleware()`; that forwards auth but does not populate claim fields.
- Global IP rate limiting is installed in `RegisterGinRouters`; user rate limiting is opt-in per route group.
- Proxy and WebSocket code manually manage headers, circuit breaker behavior, and retry semantics; change those paths carefully.
