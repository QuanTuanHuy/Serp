# Plan: Add Service Endpoints to API Gateway

This guide documents the step-by-step process for integrating new backend service endpoints into the Go-based API Gateway. The gateway follows Clean Architecture with Uber FX dependency injection, acting as a transparent proxy that forwards JWT tokens and mirrors backend service paths. The pattern involves creating 6 layers: Port Interface → Client Adapter → Service Interface/Implementation → Controller → Router → Module Registration.

## Steps

1. **Create Port Interface in `src/core/port/client/{service}/`** - Define `I{Entity}ClientPort` interface with method signatures matching backend operations (e.g., `GetUsers`, `CreateRole`), returning `(*dto.BaseResponse, error)` for all methods.

2. **Implement Client Adapter in `src/infrastructure/client/{service}/`** - Build `{Entity}ClientAdapter` struct implementing the port, using `BaseAPIClient` for HTTP calls (GET/POST/PUT/PATCH/DELETE), forwarding JWT tokens via headers, applying circuit breaker, and constructing URLs from `ExternalServiceProperties`.

3. **Create Service Layer in `src/core/service/{service}/`** - Define `I{Entity}Service` interface and `{Entity}ServiceImpl` struct that delegates to client port with thin error logging wrapper, registered via constructor `New{Entity}Service(clientPort)`.

4. **Build Controller in `src/ui/controller/{service}/`** - Implement `{Entity}Controller` struct with Gin handlers that validate requests using `ParamValidator`, extract user context via `GetUserIDFromContext`, call services, and return `BaseResponse` via `response.Success` or `response.Error`.

5. **Register Routes in `src/ui/router/{service}_router.go`** - Create `Register{Service}Routes` function that groups endpoints under `/account/api/v1` or `/{service}/api/v1`, applies `AuthMiddleware` or `JWTMiddleware` based on auth requirements, and maps HTTP methods to controller functions.

6. **Wire Module in `src/cmd/modules/{service}_module.go` and `src/cmd/bootstrap/all.go`** - Create `{Service}ModuleOpt()` returning `fx.Options` with `fx.Provide` for all client adapters/services/controllers, then add module to `all.go` bootstrap sequence after infrastructure modules but before router invocation.

## Further Considerations

1. **Path Prefix Standardization** - Currently `account` uses `/account/api/v1` while `ptm` uses `/ptm/api/v1`. Should new services follow service-specific prefix pattern or standardize to `/api/v1/{service}`? Recommend: Keep service-specific prefixes for clearer service boundaries.

2. **Configuration Management** - Add service URLs to `src/config/default.yaml` under `externalServices.{service}` with `baseUrl`, `timeout`, `retryAttempts`. Document required environment variables in service-specific `.env` files.

3. **Middleware Application Strategy** - Use `AuthMiddleware` for simple token forwarding to backend (backend validates) OR `JWTMiddleware` for gateway-level validation with role checks. Recommend: Use `JWTMiddleware` only for gateway-specific authorization logic; prefer backend validation for business rules.

4. **Error Handling Enhancement** - Currently gateway returns generic errors. Consider parsing backend `BaseResponse` error details and forwarding structured error messages to frontend for better debugging experience.

5. **Health Check Integration** - Add `/actuator/health/{service}` endpoints that probe backend service health and aggregate in main `/actuator/health` endpoint for comprehensive monitoring.

6. **Request Logging & Tracing** - Implement logging middleware to capture request ID, method, path, duration, and status code. Consider adding distributed tracing (Jaeger/Zipkin) correlation IDs for cross-service debugging.
