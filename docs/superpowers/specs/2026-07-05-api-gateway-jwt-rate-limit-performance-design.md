# API Gateway JWT and Rate Limit Performance Design

## Goal

Optimize the API Gateway protected-route hot path by reducing repeated JWT parsing and validation work, while preparing rate-limit measurement before changing Redis rate-limit behavior.

This design applies to `api_gateway` only. It is the next performance step after Account routes were moved to `GenericProxyController` with an Account-aware JWT gate.

## Current State

Protected routes currently run through `JWTMiddleware.AuthenticateJWTContext()`. That middleware performs these operations:

1. Extract bearer token from the `Authorization` header.
2. Call `JWTUtils.ValidateToken(token)`.
3. Call `JWTUtils.IsAccessToken(token)`.
4. Call `JWTUtils.ExtractRoles(token)`.
5. Store user and role context on Gin.

This means a single protected request can parse and verify the same JWT multiple times before it reaches the proxied upstream service.

Rate limiting currently has two paths:

- Global IP rate limiting installed at the Gin engine level.
- User rate limiting installed after JWT on protected route groups or inside the Account proxy JWT gate.

The rate-limit middleware already has focused benchmarks using a fake limiter. The Redis adapter should be measured before changing its algorithm.

## Decision

Use approach A from the brainstorming discussion:

- Optimize JWT now.
- Measure rate-limit cost now.
- Do not change Redis rate-limit semantics in this change.

The intended protected-route flow is:

```text
Gin route
-> JWTMiddleware.AuthenticateJWTContext()
   -> JWTUtils.ValidateAccessToken()
      -> parse + verify signature once
      -> validate expiry, issuer, and audience
      -> validate token type
      -> extract roles from validated claims
   -> set Gin context once
-> RateLimitMiddleware.ApplyUserRateLimit()
-> GenericProxyController or controller
```

## JWT API Design

Add a small result type to `api_gateway/src/kernel/utils/jwt_utils.go`:

```go
type ValidatedJWT struct {
    Claims *Claims
    Roles  []string
}
```

Add a new method:

```go
func (j *JWTUtils) ValidateAccessToken(tokenString string) (*ValidatedJWT, error)
```

`ValidateAccessToken` is responsible for a single complete authentication pass:

- Parse JWT with `jwt.ParseWithClaims`.
- Verify the signing method is RSA.
- Resolve the public key from the existing JWKS cache by `kid`.
- Preserve current expiry, issuer, and audience validation behavior.
- Check JWT header `typ`; if present, it must be `JWT`.
- Check payload `typ`; if present, it must be `Bearer`.
- Extract realm and resource roles directly from the already validated `Claims`.
- Return claims and roles together.

The `Claims` struct should add an explicit token type field, for example:

```go
TokenType string `json:"typ"`
```

This lets `ValidateAccessToken` check the payload token type without parsing the token a second time as map claims.

The payload `typ` claim is optional for backward compatibility. If it is missing, a valid signed token is accepted as it is today.

## Middleware Design

Change `JWTMiddleware.AuthenticateJWTContext()` from:

```text
ValidateToken -> IsAccessToken -> ExtractRoles
```

to:

```text
ValidateAccessToken -> set claims + roles
```

The middleware must continue setting the same Gin context keys:

- `userID`
- `userEmail`
- `userFullName`
- `preferredUsername`
- `emailVerified`
- `token`
- `roles`

It must preserve the same public error behavior:

- Missing or malformed bearer header returns the existing unauthorized response.
- Invalid or expired token returns the existing unauthorized response.
- Invalid token type returns the existing unauthorized response.

## Backward Compatibility

Keep existing `JWTUtils` methods so other code keeps compiling:

- `ValidateToken()`
- `ExtractRoles()`
- `IsAccessToken()`
- `IsRefreshToken()`
- role and claim helper methods such as `HasRole()`, `GetSubjectFromToken()`, and related helpers.

Where practical, `ExtractRoles()` should use a shared helper that extracts roles from `*Claims` after validation. This reduces duplicated role parsing logic.

The existing role middleware methods (`RequireRole`, `RequireAnyRole`, `RequireRealmRole`, `RequireResourceRole`) are not the primary hot path for this change. They can keep their current external behavior. A deeper role-middleware refactor is a separate optimization if those routes become measurable bottlenecks.

## Rate-Limit Measurement Design

Do not change the runtime rate-limit algorithm in this change.

Keep and extend focused measurements:

- `RateLimitMiddleware_IPRateLimit`: middleware overhead with a fake limiter.
- `RateLimitMiddleware_UserRateLimit`: user-scoped middleware overhead with `userID` in Gin context.
- Route override matching: verify and measure path normalization and override lookup behavior when route override count grows.
- Redis adapter behavior: add focused tests or benchmarks for `RateLimiterAdapter.CheckRateLimit` if the existing dependency set supports an in-process Redis-compatible test server. If no suitable dependency exists, keep Redis adapter benchmark as optional and avoid adding a new dependency just for this phase.

This preserves production semantics while giving the next optimization pass enough evidence to choose between:

- Redis Lua script to collapse commands into one atomic round trip.
- A bounded local short-window cache for repeated allow checks.
- Route-specific rate-limit tuning.
- Keeping the existing pipeline if Redis cost is not material.

## Testing

Add or update focused tests for JWT:

- `ValidateAccessToken` returns expected claims and roles for a valid access token.
- `ValidateAccessToken` accepts a valid token when payload `typ` is missing.
- `ValidateAccessToken` rejects a token when header `typ` is present and not `JWT`.
- `ValidateAccessToken` rejects a token when payload `typ` is present and not `Bearer`.
- `AuthenticateJWTContext` sets the same Gin context values as before, including `roles`.
- Existing missing-header and abort-chain middleware behavior remains unchanged.

Add or update focused tests for rate-limit measurement behavior:

- IP rate-limit benchmark still uses a fake limiter and reports allocations.
- User rate-limit benchmark still uses a fake limiter and reports allocations.
- Route override lookup handles exact route keys and normalized trailing slashes.
- If a Redis-compatible in-process test dependency already exists, benchmark or test adapter command behavior without requiring external infrastructure.

## Benchmarking

Use Go benchmarks before and after the JWT change:

```bash
go test ./src/kernel/utils -bench 'JWT' -benchmem -run '^$' -count=5
go test ./src/ui/middleware -bench 'RateLimit' -benchmem -run '^$' -count=5
```

Expected JWT benchmark comparison:

- Existing `BenchmarkJWTUtils_CurrentAuthenticateJWTWork` represents the old middleware-style work.
- New `BenchmarkJWTUtils_ValidateAccessToken` represents the optimized one-pass path.

The improvement should show lower `ns/op`, lower allocations, or both for the protected-route JWT work. If the benchmark does not improve, the implementation should be rechecked before proceeding.

## Verification

Run from `api_gateway/`:

```bash
go test ./src/kernel/utils ./src/ui/middleware -count=1
go test ./src/kernel/utils ./src/ui/middleware -bench 'JWT|RateLimit' -benchmem -run '^$' -count=5
go test ./... -count=1
go vet ./...
```

Use a local `GOCACHE` inside the repository if the default Go cache is blocked by the sandbox.

## Production Safety

This change must not weaken authentication or rate limiting:

- JWT signature verification remains mandatory.
- Issuer and audience validation remain unchanged.
- Expiry validation remains unchanged.
- Token type validation becomes part of the single-pass access-token validation.
- Rate-limit allow, deny, headers, and fail-open-on-limiter-error behavior remain unchanged.
- No new runtime dependency is introduced for rate limiting in this phase.

## Non-Goals

- Do not change Account, CRM, PTM, or other route mappings.
- Do not change Redis rate-limit algorithm in this phase.
- Do not add a local rate-limit cache in this phase.
- Do not introduce a new metrics system in this phase.
- Do not delete existing JWT helper APIs.
- Do not refactor all role middleware unless required by tests.
