# Account Generic Proxy Performance Design

## Goal

Optimize API Gateway performance for Account service routes by moving Account HTTP traffic from gateway-specific controllers, services, and adapters to the existing `GenericProxyController`, while preserving public URL behavior, protected JWT enforcement, and exact source-to-target path mapping.

This design applies only to `api_gateway` Account routing. PTM, CRM, WebSocket, and other service routing are out of scope.

## Current State

`api_gateway/src/ui/router/account_router.go` currently mixes:

- Direct generic proxy routes for some Account endpoints.
- Gateway Account controllers that call gateway services and downstream Account client adapters.
- `AuthMiddleware()` on most custom Account routes, which forwards Authorization context for gateway adapters but does not validate JWT.

The Account service already owns its public/protected route policy in `account/src/main/resources/application.yml`. It also runs with:

```yaml
server:
  servlet:
    context-path: /account-service
```

Its public controllers use `/api/v1/...` mappings under that servlet context.

## Decision

Use one Account catch-all proxy route in API Gateway and protect it with an Account-aware JWT gate middleware:

```go
accountV1 := group.Group("/api/v1")
accountV1.Any("/*proxyPath", accountJWTGate.Handler(), genericProxyController.ProxyHandler("account"))
```

The JWT gate middleware decides per request:

- If `(method, path)` matches an explicit protected override, run gateway JWT validation even if a broader public wildcard would also match.
- If `(method, path)` matches the Account public route patterns, skip gateway JWT validation and proxy directly.
- Otherwise, run `JWTMiddleware.AuthenticateJWT()`.
- After successful JWT validation on protected routes, run `RateLimitMiddleware.UserRateLimit()`.
- Then call `GenericProxyController.ProxyHandler("account")`.

This avoids Gin wildcard conflicts that can happen when exact public routes and a protected `/*proxyPath` route are registered under the same `/api/v1` prefix.

## Components

Add a small Account proxy security middleware under `api_gateway/src/ui/middleware`, for example `AccountProxyJWTGateMiddleware`.

Responsibilities:

- Normalize the incoming request path to the gateway service path before matching. If `p.App.Path()` is configured, the app path should not be part of the Account public/protected pattern comparison.
- Match `(method, normalizedPath)` against explicit protected overrides before matching public route patterns.
- For public routes, call `c.Next()` without JWT validation or user rate limiting.
- For protected routes, run the existing `JWTMiddleware.AuthenticateJWT()` logic, stop if it aborts the request, then run `RateLimitMiddleware.UserRateLimit()`, and finally continue to the proxy.

It should not know any Account business rules beyond the public route pattern list. Account role and permission authorization remains owned by the Account service.

## Path Mapping

Keep the existing Account route mapping in `GenericProxyController`:

```go
ServiceRoute{
    Name:         "account",
    SourcePrefix: "/api/v1",
    TargetPrefix: "/account-service/api/v1",
    Target:       props.AccountService.BaseURL(),
}
```

Required path parity examples:

| Gateway source path | Account target path |
| --- | --- |
| `/api/v1/auth/login` | `/account-service/api/v1/auth/login` |
| `/api/v1/auth/reset-password/validate?token=abc` | `/account-service/api/v1/auth/reset-password/validate?token=abc` |
| `/api/v1/users/profile/me` | `/account-service/api/v1/users/profile/me` |
| `/api/v1/admin/subscriptions/7/activate` | `/account-service/api/v1/admin/subscriptions/7/activate` |
| `/api/v1/organizations/10/modules/20/auto-grant/backfill` | `/account-service/api/v1/organizations/10/modules/20/auto-grant/backfill` |
| `/api/v1/organizations/10/departments/3/members` | `/account-service/api/v1/organizations/10/departments/3/members` |
| `/api/v1/menu-displays/5/roles` | `/account-service/api/v1/menu-displays/5/roles` |
| `/api/v1/invitations/token-123/accept` | `/account-service/api/v1/invitations/token-123/accept` |

The proxy must preserve query string, request body, method, and Authorization header.

## Public Account Route Patterns

The gateway JWT gate should mirror the Account service public URL behavior for Account routes that are intentionally public. These patterns come from `account/src/main/resources/application.yml` and should be kept explicit in gateway tests:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/get-token`
- `POST /api/v1/auth/refresh-token`
- `POST /api/v1/auth/revoke-token`
- `GET /api/v1/auth/reset-password/validate`
- `POST /api/v1/auth/reset-password/confirm`
- `GET /api/v1/permissions/**`
- `GET /api/v1/roles/**`
- `GET /api/v1/modules/**`
- `GET /api/v1/menu-displays/**`
- `GET /api/v1/keycloak/**`
- `GET /api/v1/subscription-plans/**`
- `GET /api/v1/organizations/*/departments`
- `GET /api/v1/organizations/*/departments/*`
- `GET /api/v1/organizations/*/departments/*/tree`
- `GET /api/v1/organizations/*/departments/*/members`
- `POST /api/v1/invitations/*/accept`

All other Account routes are protected in the gateway by JWT before being proxied.

Explicit protected overrides must take precedence over public wildcards. For example, `GET /api/v1/modules/**` is public for general module reads, but `GET /api/v1/modules/my-modules` must still pass through gateway JWT because it is user-scoped.

## Dependency Cleanup

After Account routes use generic proxy only:

- `RegisterAccountRoutes` should no longer require Account controllers.
- `RegisterGinRoutersIn` should remove Account controller fields.
- `RegisterGinRouters` should call `RegisterAccountRoutes` with only generic proxy, JWT middleware, and rate-limit middleware dependencies needed by the Account JWT gate.
- `bootstrap.All()` should stop invoking `modules.AccountModule()` so Account gateway adapters, services, and controllers are no longer instantiated on startup.

The Account gateway controller/service/adapter source files can remain in the repository for this change. Deleting them is a separate cleanup step to reduce blast radius.

## Error Handling

For protected Account routes:

- Missing or invalid JWT should return the existing `JWTMiddleware.AuthenticateJWT()` error response.
- User rate-limit failures should return the existing `RateLimitMiddleware.UserRateLimit()` response.
- Downstream Account failures should continue to use `GenericProxyController` reverse proxy error behavior.

For public Account routes:

- Gateway should not validate JWT.
- Account service remains responsible for request validation and response contracts.

## Testing

Add focused API Gateway tests before changing routing:

- Public route without Authorization proxies successfully.
- Protected route without Authorization is rejected by gateway JWT middleware.
- Protected route with valid JWT reaches the upstream mock.
- Path rewrite parity for representative Account paths listed above.
- Query string preservation.
- Request body preservation for POST/PUT/PATCH.
- Authorization header forwarding for protected routes.
- Account module is no longer required by FX compile checks after bootstrap cleanup.

Verification commands:

```bash
go test ./src/ui/controller/common ./src/ui/router -count=1
go test ./src/cmd/... -run '^$' -count=1
go test ./... -count=1
go vet ./...
```

## Performance Expectation

The hot path should become:

```text
Gin route -> Account JWT gate -> GenericProxyController -> Account service
```

This removes the current Account gateway controller/service/adapter/BaseAPIClient path for Account HTTP routes. The expected improvement is lower gateway CPU, fewer allocations, less JSON marshal/unmarshal work inside the gateway, and lower maintenance cost for Account endpoint additions.

## Non-Goals

- Do not change Account service controller paths.
- Do not change Account service security configuration in this change.
- Do not proxy PTM custom routes in this change.
- Do not delete Account gateway controller/service/adapter files in this change unless the implementation plan explicitly adds a separate cleanup task.
