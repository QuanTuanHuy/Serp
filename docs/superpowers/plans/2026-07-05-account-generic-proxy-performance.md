# Account Generic Proxy Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route Account service HTTP traffic through `GenericProxyController` while preserving exact `/api/v1/...` source paths, `/account-service/api/v1/...` target paths, JWT protection for protected Account APIs, and user rate limiting after JWT.

**Architecture:** Add an Account-aware JWT gate middleware that checks protected overrides first, then public route patterns, then either skips JWT for public routes or runs JWT + user rate limit before proxying. Replace the current Account gateway controller route registrations with a single `/api/v1/*proxyPath` proxy route and remove Account module/controller dependencies from the runtime FX graph.

**Tech Stack:** Go 1.25, Gin, `httptest`, existing `JWTMiddleware`, existing `RateLimitMiddleware`, existing `GenericProxyController`, Uber FX.

---

## File Structure

- `api_gateway/src/ui/middleware/jwt_middleware.go`
  - Add `AuthenticateJWTContext(c *gin.Context) bool` so Account gate can validate JWT without prematurely calling `c.Next()`.
- `api_gateway/src/ui/middleware/jwt_middleware_test.go`
  - Add focused tests for missing/invalid Authorization behavior in the new helper.
- `api_gateway/src/ui/middleware/rate_limit_middleware.go`
  - Add `ApplyUserRateLimit(c *gin.Context) bool` so Account gate can apply user rate limiting without prematurely calling `c.Next()`.
- `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`
  - Add focused tests for the new helper with a fake limiter.
- `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware.go`
  - New Account public/protected route classifier and gate middleware.
- `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware_test.go`
  - Tests for public skip, protected JWT path, user rate limit call, app path normalization, public wildcard matching, and protected override precedence.
- `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`
  - Add Account-specific path rewrite and request preservation tests.
- `api_gateway/src/ui/router/account_router.go`
  - Replace many Account route registrations with a single catch-all proxy route.
- `api_gateway/src/ui/router/account_router_test.go`
  - Add router-level tests for public proxy and protected missing-token rejection.
- `api_gateway/src/ui/router/router.go`
  - Remove Account controller fields from `RegisterRoutersIn`; add `AccountProxyJWTGateMiddleware`.
- `api_gateway/src/cmd/bootstrap/all.go`
  - Register the Account JWT gate middleware and stop invoking `modules.AccountModule()`.
- `docs/superpowers/specs/2026-07-05-account-generic-proxy-performance-design.md`
  - Reference only; update only if implementation uncovers another design contradiction.

## Task 1: Add Non-Advancing JWT and User Rate-Limit Helpers

**Files:**
- Modify: `api_gateway/src/ui/middleware/jwt_middleware.go`
- Create: `api_gateway/src/ui/middleware/jwt_middleware_test.go`
- Modify: `api_gateway/src/ui/middleware/rate_limit_middleware.go`
- Create: `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`

- [ ] **Step 1: Add JWT helper tests**

Create `api_gateway/src/ui/middleware/jwt_middleware_test.go`:

```go
package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestJWTMiddleware_AuthenticateJWTContext_MissingHeaderAborts(t *testing.T) {
	gin.SetMode(gin.TestMode)
	m := NewJWTMiddleware(nil)

	r := gin.New()
	r.GET("/protected", func(c *gin.Context) {
		if m.AuthenticateJWTContext(c) {
			c.String(http.StatusOK, "ok")
		}
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Fatalf("expected status %d, got %d", http.StatusUnauthorized, w.Code)
	}
}

func TestJWTMiddleware_AuthenticateJWT_MissingHeaderDoesNotCallNext(t *testing.T) {
	gin.SetMode(gin.TestMode)
	m := NewJWTMiddleware(nil)

	called := false
	r := gin.New()
	r.Use(m.AuthenticateJWT())
	r.GET("/protected", func(c *gin.Context) {
		called = true
		c.String(http.StatusOK, "ok")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		t.Fatalf("expected status %d, got %d", http.StatusUnauthorized, w.Code)
	}
	if called {
		t.Fatalf("expected next handler not to be called")
	}
}
```

- [ ] **Step 2: Run JWT helper tests and confirm they fail**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -run '^TestJWTMiddleware_AuthenticateJWT' -count=1
```

Expected: FAIL because `AuthenticateJWTContext` does not exist yet.

- [ ] **Step 3: Refactor `jwt_middleware.go`**

Replace `AuthenticateJWT()` with a wrapper over this helper, preserving existing behavior:

```go
// AuthenticateJWT validates JWT token and extracts user information.
func (m *JWTMiddleware) AuthenticateJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !m.AuthenticateJWTContext(c) {
			return
		}
		c.Next()
	}
}

// AuthenticateJWTContext validates JWT token, stores claims on Gin context,
// and returns whether the request may continue.
func (m *JWTMiddleware) AuthenticateJWTContext(c *gin.Context) bool {
	authHeader := c.GetHeader("Authorization")
	if authHeader == "" {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	const bearerPrefix = "Bearer "
	if !strings.HasPrefix(authHeader, bearerPrefix) {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	token := strings.TrimPrefix(authHeader, bearerPrefix)
	if token == "" {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	claims, err := m.jwtUtils.ValidateToken(token)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid or expired token")
		c.Abort()
		return false
	}

	if !m.jwtUtils.IsAccessToken(token) {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token type")
		c.Abort()
		return false
	}

	c.Set("userID", claims.UserID)
	c.Set("userEmail", claims.Email)
	c.Set("userFullName", claims.FullName)
	c.Set("preferredUsername", claims.PreferredUsername)
	c.Set("emailVerified", claims.EmailVerified)
	c.Set("token", token)

	roles, err := m.jwtUtils.ExtractRoles(token)
	if err != nil {
		log.Warn(c, "Failed to extract roles: ", err)
		roles = []string{}
	}
	c.Set("roles", roles)

	log.Debugc(c, "JWT authentication successful for user: ", claims.UserID, " (", claims.Email, ")")
	return true
}
```

- [ ] **Step 4: Add user rate-limit helper tests**

Create `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`:

```go
package middleware

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	port "github.com/serp/api-gateway/src/core/port/rate_limiter"
	"github.com/serp/api-gateway/src/kernel/properties"
)

type testRateLimiter struct {
	called  bool
	allowed bool
}

func (t *testRateLimiter) CheckRateLimit(
	ctx context.Context,
	key string,
	limit int,
	windowSecs int,
) (*port.RateLimitResult, error) {
	t.called = true
	return &port.RateLimitResult{
		Allowed:    t.allowed,
		Limit:      limit,
		Remaining:  limit - 1,
		ResetAt:    time.Now().Add(time.Duration(windowSecs) * time.Second).Unix(),
		RetryAfter: 1,
	}, nil
}

func newTestRateLimitMiddleware(limiter *testRateLimiter) *RateLimitMiddleware {
	return NewRateLimitMiddleware(limiter, &properties.RateLimitProperties{
		Enabled:     true,
		DefaultUser: properties.RateLimitRule{Limit: 10, WindowSecs: 60},
	})
}

func TestRateLimitMiddleware_ApplyUserRateLimit_Allowed(t *testing.T) {
	gin.SetMode(gin.TestMode)
	limiter := &testRateLimiter{allowed: true}
	m := newTestRateLimitMiddleware(limiter)

	r := gin.New()
	r.GET("/protected", func(c *gin.Context) {
		c.Set("userID", int64(123))
		if m.ApplyUserRateLimit(c) {
			c.String(http.StatusOK, "ok")
		}
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if !limiter.called {
		t.Fatalf("expected rate limiter to be called")
	}
}

func TestRateLimitMiddleware_UserRateLimit_BlockedDoesNotCallNext(t *testing.T) {
	gin.SetMode(gin.TestMode)
	limiter := &testRateLimiter{allowed: false}
	m := newTestRateLimitMiddleware(limiter)

	called := false
	r := gin.New()
	r.Use(func(c *gin.Context) {
		c.Set("userID", int64(123))
		c.Next()
	})
	r.Use(m.UserRateLimit())
	r.GET("/protected", func(c *gin.Context) {
		called = true
		c.String(http.StatusOK, "ok")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusTooManyRequests {
		t.Fatalf("expected status %d, got %d", http.StatusTooManyRequests, w.Code)
	}
	if called {
		t.Fatalf("expected next handler not to be called")
	}
}
```

- [ ] **Step 5: Run rate-limit helper tests and confirm they fail**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -run '^TestRateLimitMiddleware_(ApplyUserRateLimit|UserRateLimit)' -count=1
```

Expected: FAIL because `ApplyUserRateLimit` does not exist yet.

- [ ] **Step 6: Refactor `rate_limit_middleware.go`**

Replace `UserRateLimit()` with a wrapper over this helper:

```go
// UserRateLimit returns a Gin middleware that applies user-based rate limiting.
// This should be applied per-group after JWT middleware that sets "userID" in context.
func (m *RateLimitMiddleware) UserRateLimit() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !m.ApplyUserRateLimit(c) {
			return
		}
		c.Next()
	}
}

// ApplyUserRateLimit applies user-based rate limiting without advancing Gin's handler chain.
func (m *RateLimitMiddleware) ApplyUserRateLimit(c *gin.Context) bool {
	if !m.props.Enabled {
		return true
	}

	userID, exists := c.Get("userID")
	if !exists {
		return true
	}

	rule := m.props.DefaultUser
	override, routeKey := m.resolveRouteOverride(c)
	isRouteOverride := false
	if override != nil && override.User != nil {
		rule = *override.User
		isRouteOverride = true
	}

	if !isValidRateLimitRule(rule) {
		log.Warn(c,
			"Invalid user rate limit rule, allowing request. limit=",
			rule.Limit,
			", windowSecs=",
			rule.WindowSecs,
		)
		return true
	}

	key := fmt.Sprintf("user:%v", userID)
	if isRouteOverride {
		key = fmt.Sprintf("user:%v:%s", userID, routeKey)
	}

	result, err := m.rateLimiter.CheckRateLimit(c.Request.Context(), key, rule.Limit, rule.WindowSecs)
	if err != nil {
		log.Warn(c, "Rate limiter unavailable for user, allowing request: ", err)
		return true
	}

	setRateLimitHeaders(c, result)

	if !result.Allowed {
		c.Header("Retry-After", strconv.Itoa(result.RetryAfter))
		utils.AbortErrorHandleCustomMessage(c,
			constant.GeneralTooManyRequests,
			"User rate limit exceeded. Try again later.",
		)
		c.Abort()
		return false
	}

	return true
}
```

- [ ] **Step 7: Run middleware tests**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -run '^(TestJWTMiddleware_AuthenticateJWT|TestRateLimitMiddleware_(ApplyUserRateLimit|UserRateLimit))' -count=1
```

Expected: PASS.

- [ ] **Step 8: Commit helper refactor**

```bash
git add src/ui/middleware/jwt_middleware.go src/ui/middleware/jwt_middleware_test.go src/ui/middleware/rate_limit_middleware.go src/ui/middleware/rate_limit_middleware_test.go
git commit -m "refactor(gateway): expose non-advancing auth middleware helpers"
```

## Task 2: Add Account Proxy JWT Gate Middleware

**Files:**
- Create: `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware.go`
- Create: `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware_test.go`

- [ ] **Step 1: Add failing gate tests**

Create `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware_test.go`:

```go
package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

type fakeAccountAuthenticator struct {
	called bool
	allow  bool
}

func (f *fakeAccountAuthenticator) AuthenticateJWTContext(c *gin.Context) bool {
	f.called = true
	if !f.allow {
		c.Status(http.StatusUnauthorized)
		c.Abort()
		return false
	}
	c.Set("userID", int64(123))
	return true
}

type fakeAccountUserLimiter struct {
	called bool
	allow  bool
}

func (f *fakeAccountUserLimiter) ApplyUserRateLimit(c *gin.Context) bool {
	f.called = true
	if !f.allow {
		c.Status(http.StatusTooManyRequests)
		c.Abort()
		return false
	}
	return true
}

func TestAccountProxyJWTGate_PublicRouteSkipsJWTAndRateLimit(t *testing.T) {
	gin.SetMode(gin.TestMode)
	auth := &fakeAccountAuthenticator{allow: false}
	limiter := &fakeAccountUserLimiter{allow: false}
	gate := newAccountProxyJWTGateMiddleware(auth, limiter)

	r := gin.New()
	r.POST("/api/v1/auth/login", gate.Handler(""), func(c *gin.Context) {
		c.String(http.StatusOK, "proxied")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodPost, "/api/v1/auth/login", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if auth.called {
		t.Fatalf("expected public route to skip JWT")
	}
	if limiter.called {
		t.Fatalf("expected public route to skip user rate limit")
	}
}

func TestAccountProxyJWTGate_ProtectedRouteRunsJWTThenRateLimit(t *testing.T) {
	gin.SetMode(gin.TestMode)
	auth := &fakeAccountAuthenticator{allow: true}
	limiter := &fakeAccountUserLimiter{allow: true}
	gate := newAccountProxyJWTGateMiddleware(auth, limiter)

	r := gin.New()
	r.GET("/api/v1/users/profile/me", gate.Handler(""), func(c *gin.Context) {
		c.String(http.StatusOK, "proxied")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/v1/users/profile/me", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if !auth.called {
		t.Fatalf("expected protected route to run JWT")
	}
	if !limiter.called {
		t.Fatalf("expected protected route to run user rate limit")
	}
}

func TestAccountProxyJWTGate_ProtectedOverrideWinsOverPublicWildcard(t *testing.T) {
	gin.SetMode(gin.TestMode)
	auth := &fakeAccountAuthenticator{allow: true}
	limiter := &fakeAccountUserLimiter{allow: true}
	gate := newAccountProxyJWTGateMiddleware(auth, limiter)

	r := gin.New()
	r.GET("/api/v1/modules/my-modules", gate.Handler(""), func(c *gin.Context) {
		c.String(http.StatusOK, "proxied")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/v1/modules/my-modules", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if !auth.called {
		t.Fatalf("expected protected override route to run JWT")
	}
}

func TestAccountProxyJWTGate_NormalizesAppPath(t *testing.T) {
	gin.SetMode(gin.TestMode)
	auth := &fakeAccountAuthenticator{allow: false}
	limiter := &fakeAccountUserLimiter{allow: false}
	gate := newAccountProxyJWTGateMiddleware(auth, limiter)

	r := gin.New()
	r.POST("/gateway/api/v1/auth/login", gate.Handler("/gateway"), func(c *gin.Context) {
		c.String(http.StatusOK, "proxied")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodPost, "/gateway/api/v1/auth/login", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if auth.called {
		t.Fatalf("expected normalized public route to skip JWT")
	}
}
```

- [ ] **Step 2: Run gate tests and confirm they fail**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -run '^TestAccountProxyJWTGate_' -count=1
```

Expected: FAIL because the account gate does not exist.

- [ ] **Step 3: Add gate implementation**

Create `api_gateway/src/ui/middleware/account_proxy_jwt_gate_middleware.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type jwtContextAuthenticator interface {
	AuthenticateJWTContext(c *gin.Context) bool
}

type userRateLimitApplier interface {
	ApplyUserRateLimit(c *gin.Context) bool
}

type AccountProxyJWTGateMiddleware struct {
	authenticator jwtContextAuthenticator
	userLimiter   userRateLimitApplier
}

type accountProxyRoutePattern struct {
	method  string
	pattern string
}

var accountProxyProtectedOverrides = []accountProxyRoutePattern{
	{method: http.MethodGet, pattern: "/api/v1/modules/my-modules"},
}

var accountProxyPublicRoutes = []accountProxyRoutePattern{
	{method: http.MethodPost, pattern: "/api/v1/auth/login"},
	{method: http.MethodPost, pattern: "/api/v1/auth/register"},
	{method: http.MethodPost, pattern: "/api/v1/auth/get-token"},
	{method: http.MethodPost, pattern: "/api/v1/auth/refresh-token"},
	{method: http.MethodPost, pattern: "/api/v1/auth/revoke-token"},
	{method: http.MethodGet, pattern: "/api/v1/auth/reset-password/validate"},
	{method: http.MethodPost, pattern: "/api/v1/auth/reset-password/confirm"},
	{method: http.MethodGet, pattern: "/api/v1/permissions/**"},
	{method: http.MethodGet, pattern: "/api/v1/roles/**"},
	{method: http.MethodGet, pattern: "/api/v1/modules/**"},
	{method: http.MethodGet, pattern: "/api/v1/menu-displays/**"},
	{method: http.MethodGet, pattern: "/api/v1/keycloak/**"},
	{method: http.MethodGet, pattern: "/api/v1/subscription-plans/**"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*/tree"},
	{method: http.MethodGet, pattern: "/api/v1/organizations/*/departments/*/members"},
	{method: http.MethodPost, pattern: "/api/v1/invitations/*/accept"},
}

func NewAccountProxyJWTGateMiddleware(
	jwtMiddleware *JWTMiddleware,
	rateLimitMiddleware *RateLimitMiddleware,
) *AccountProxyJWTGateMiddleware {
	return newAccountProxyJWTGateMiddleware(jwtMiddleware, rateLimitMiddleware)
}

func newAccountProxyJWTGateMiddleware(
	authenticator jwtContextAuthenticator,
	userLimiter userRateLimitApplier,
) *AccountProxyJWTGateMiddleware {
	return &AccountProxyJWTGateMiddleware{
		authenticator: authenticator,
		userLimiter:   userLimiter,
	}
}

func (m *AccountProxyJWTGateMiddleware) Handler(appPath string) gin.HandlerFunc {
	return func(c *gin.Context) {
		normalizedPath := normalizeAccountProxyPath(c.Request.URL.Path, appPath)
		if isAccountProxyPublicRoute(c.Request.Method, normalizedPath) {
			c.Next()
			return
		}

		if !m.authenticator.AuthenticateJWTContext(c) {
			return
		}
		if !m.userLimiter.ApplyUserRateLimit(c) {
			return
		}

		c.Next()
	}
}

func isAccountProxyPublicRoute(method string, requestPath string) bool {
	if matchesAccountProxyRoute(accountProxyProtectedOverrides, method, requestPath) {
		return false
	}

	return matchesAccountProxyRoute(accountProxyPublicRoutes, method, requestPath)
}

func matchesAccountProxyRoute(patterns []accountProxyRoutePattern, method string, requestPath string) bool {
	for _, route := range patterns {
		if route.method != method {
			continue
		}
		if matchAccountProxyPathPattern(route.pattern, requestPath) {
			return true
		}
	}
	return false
}

func matchAccountProxyPathPattern(pattern string, requestPath string) bool {
	pattern = normalizeAccountProxyPath(pattern, "")
	requestPath = normalizeAccountProxyPath(requestPath, "")

	if strings.HasSuffix(pattern, "/**") {
		prefix := strings.TrimSuffix(pattern, "/**")
		return requestPath == prefix || strings.HasPrefix(requestPath, prefix+"/")
	}

	patternParts := splitAccountProxyPath(pattern)
	requestParts := splitAccountProxyPath(requestPath)
	if len(patternParts) != len(requestParts) {
		return false
	}

	for i := range patternParts {
		if patternParts[i] == "*" {
			continue
		}
		if patternParts[i] != requestParts[i] {
			return false
		}
	}

	return true
}

func normalizeAccountProxyPath(path string, appPath string) string {
	if path == "" {
		return "/"
	}

	normalized := "/" + strings.Trim(path, "/")
	if appPath != "" && appPath != "/" {
		normalizedAppPath := "/" + strings.Trim(appPath, "/")
		if normalized == normalizedAppPath {
			return "/"
		}
		if strings.HasPrefix(normalized, normalizedAppPath+"/") {
			normalized = strings.TrimPrefix(normalized, normalizedAppPath)
		}
	}

	if normalized != "/" {
		normalized = strings.TrimRight(normalized, "/")
	}
	if normalized == "" {
		return "/"
	}

	return normalized
}

func splitAccountProxyPath(path string) []string {
	path = strings.Trim(path, "/")
	if path == "" {
		return []string{}
	}
	return strings.Split(path, "/")
}
```

- [ ] **Step 4: Run gate tests**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -run '^TestAccountProxyJWTGate_' -count=1
```

Expected: PASS.

- [ ] **Step 5: Run all middleware tests**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -count=1
```

Expected: PASS.

- [ ] **Step 6: Commit Account gate middleware**

```bash
git add src/ui/middleware/account_proxy_jwt_gate_middleware.go src/ui/middleware/account_proxy_jwt_gate_middleware_test.go
git commit -m "feat(gateway): add account proxy jwt gate"
```

## Task 3: Add Account Proxy Path Parity Tests

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Add failing Account path parity test**

Append this test to `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`:

```go
func TestGenericProxyController_Account_RewritePathAndPreserveRequest(t *testing.T) {
	gin.SetMode(gin.TestMode)

	type upstreamRequest struct {
		method string
		path   string
		query  string
		auth   string
		body   string
	}

	var got upstreamRequest

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		body, _ := io.ReadAll(r.Body)
		got = upstreamRequest{
			method: r.Method,
			path:   r.URL.Path,
			query:  r.URL.RawQuery,
			auth:   r.Header.Get("Authorization"),
			body:   string(body),
		}
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	}))
	defer upstream.Close()

	u, err := url.Parse(upstream.URL)
	if err != nil {
		t.Fatalf("parse upstream url: %v", err)
	}

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			AccountService: properties.ServiceProperty{Host: u.Hostname(), Port: u.Port()},
		},
		defaultResilienceProps(),
		properties.NewDefaultTransportProperties(),
	)

	r := gin.New()
	r.Any("/api/v1/*proxyPath", controller.ProxyHandler("account"))
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	req, err := http.NewRequest(
		http.MethodPost,
		gateway.URL+"/api/v1/organizations/10/modules/20/auto-grant/backfill?dryRun=true",
		strings.NewReader(`{"scope":"all"}`),
	)
	if err != nil {
		t.Fatalf("new request: %v", err)
	}
	req.Header.Set("Authorization", "Bearer account-token")
	req.Header.Set("Content-Type", "application/json")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatalf("do request: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, resp.StatusCode)
	}
	if got.method != http.MethodPost {
		t.Fatalf("expected method %q, got %q", http.MethodPost, got.method)
	}
	if got.path != "/account-service/api/v1/organizations/10/modules/20/auto-grant/backfill" {
		t.Fatalf("expected path %q, got %q",
			"/account-service/api/v1/organizations/10/modules/20/auto-grant/backfill", got.path)
	}
	if got.query != "dryRun=true" {
		t.Fatalf("expected query %q, got %q", "dryRun=true", got.query)
	}
	if got.auth != "Bearer account-token" {
		t.Fatalf("expected auth %q, got %q", "Bearer account-token", got.auth)
	}
	if got.body != `{"scope":"all"}` {
		t.Fatalf("expected body %q, got %q", `{"scope":"all"}`, got.body)
	}
}
```

- [ ] **Step 2: Run the new parity test**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController_Account_RewritePathAndPreserveRequest$' -count=1
```

Expected: PASS because `GenericProxyController` already has the correct Account `SourcePrefix` and `TargetPrefix`.

- [ ] **Step 3: Commit Account proxy parity test**

```bash
git add src/ui/controller/common/generic_proxy_controller_test.go
git commit -m "test(gateway): cover account proxy path parity"
```

## Task 4: Rewrite Account Router to Use Generic Proxy Catch-All

**Files:**
- Modify: `api_gateway/src/ui/router/account_router.go`
- Create: `api_gateway/src/ui/router/account_router_test.go`

- [ ] **Step 1: Add router tests**

Create `api_gateway/src/ui/router/account_router_test.go`:

```go
package router

import (
	"context"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	port "github.com/serp/api-gateway/src/core/port/rate_limiter"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func newTestAccountProxyController(t *testing.T, upstreamURL string) *common.GenericProxyController {
	t.Helper()
	u, err := url.Parse(upstreamURL)
	if err != nil {
		t.Fatalf("parse upstream url: %v", err)
	}
	resProps := properties.NewDefaultResilienceProperties()

	return common.NewGenericProxyController(
		&properties.ExternalServiceProperties{
			AccountService: properties.ServiceProperty{Host: u.Hostname(), Port: u.Port()},
		},
		&resProps,
		properties.NewDefaultTransportProperties(),
	)
}

func TestRegisterAccountRoutes_PublicRouteProxiesWithoutAuthorization(t *testing.T) {
	gin.SetMode(gin.TestMode)

	var gotPath string
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	}))
	defer upstream.Close()

	r := gin.New()
	RegisterAccountRoutes(
		r.Group(""),
		"",
		newTestAccountProxyController(t, upstream.URL),
		middleware.NewAccountProxyJWTGateMiddleware(
			middleware.NewJWTMiddleware(nil),
			newTestRouterRateLimitMiddleware(),
		),
	)

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodPost, "/api/v1/auth/login", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if gotPath != "/account-service/api/v1/auth/login" {
		t.Fatalf("expected upstream path %q, got %q", "/account-service/api/v1/auth/login", gotPath)
	}
}

func TestRegisterAccountRoutes_ProtectedRouteRequiresAuthorization(t *testing.T) {
	gin.SetMode(gin.TestMode)

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		t.Fatalf("protected route without auth should not reach upstream")
	}))
	defer upstream.Close()

	r := gin.New()
	RegisterAccountRoutes(
		r.Group(""),
		"",
		newTestAccountProxyController(t, upstream.URL),
		middleware.NewAccountProxyJWTGateMiddleware(
			middleware.NewJWTMiddleware(nil),
			newTestRouterRateLimitMiddleware(),
		),
	)

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/api/v1/users/profile/me", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusUnauthorized {
		body, _ := io.ReadAll(w.Body)
		t.Fatalf("expected status %d, got %d body=%s", http.StatusUnauthorized, w.Code, string(body))
	}
}

type routerAllowingRateLimiter struct{}

func newTestRouterRateLimitMiddleware() *middleware.RateLimitMiddleware {
	return middleware.NewRateLimitMiddleware(
		routerAllowingRateLimiter{},
		&properties.RateLimitProperties{
			Enabled:     true,
			DefaultUser: properties.RateLimitRule{Limit: 10, WindowSecs: 60},
		},
	)
}

func (routerAllowingRateLimiter) CheckRateLimit(
	ctx context.Context,
	key string,
	limit int,
	windowSecs int,
) (*port.RateLimitResult, error) {
	return &port.RateLimitResult{
		Allowed:    true,
		Limit:      limit,
		Remaining:  limit - 1,
		ResetAt:    time.Now().Add(time.Duration(windowSecs) * time.Second).Unix(),
		RetryAfter: 0,
	}, nil
}
```

- [ ] **Step 2: Run router tests and confirm they fail**

Run from `api_gateway`:

```bash
go test ./src/ui/router -run '^TestRegisterAccountRoutes_' -count=1
```

Expected: FAIL because `RegisterAccountRoutes` still requires Account controllers and does not use the new signature.

- [ ] **Step 3: Rewrite account route registration**

Replace `api_gateway/src/ui/router/account_router.go` with:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterAccountRoutes(
	group *gin.RouterGroup,
	appPath string,
	genericProxyController *common.GenericProxyController,
	accountProxyJWTGateMiddleware *middleware.AccountProxyJWTGateMiddleware,
) {
	accountV1 := group.Group("/api/v1")
	{
		accountV1.Any(
			"/*proxyPath",
			accountProxyJWTGateMiddleware.Handler(appPath),
			genericProxyController.ProxyHandler("account"),
		)
	}
}
```

- [ ] **Step 4: Run router tests**

Run from `api_gateway`:

```bash
go test ./src/ui/router -run '^TestRegisterAccountRoutes_' -count=1
```

Expected: PASS.

- [ ] **Step 5: Commit account router rewrite**

```bash
git add src/ui/router/account_router.go src/ui/router/account_router_test.go
git commit -m "perf(gateway): route account traffic through generic proxy"
```

## Task 5: Clean FX Runtime Dependencies

**Files:**
- Modify: `api_gateway/src/ui/router/router.go`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`

- [ ] **Step 1: Remove Account controller dependencies from router input**

In `api_gateway/src/ui/router/router.go`, remove the Account controller import:

```go
account "github.com/serp/api-gateway/src/ui/controller/account"
```

Remove these fields from `RegisterRoutersIn`:

```go
AuthController             *account.AuthController
UserController             *account.UserController
KeycloakController         *account.KeycloakController
RoleController             *account.RoleController
PermissionController       *account.PermissionController
ModuleController           *account.ModuleController
SubscriptionController     *account.SubscriptionController
SubscriptionPlanController *account.SubscriptionPlanController
ModuleAccessController     *account.ModuleAccessController
MenuDisplayController      *account.MenuDisplayController
OrganizationController     *account.OrganizationController
DepartmentController       *account.DepartmentController
```

Add this field:

```go
AccountProxyJWTGateMiddleware *middleware.AccountProxyJWTGateMiddleware
```

- [ ] **Step 2: Update `RegisterAccountRoutes` call**

Change the call in `RegisterGinRouters` to:

```go
RegisterAccountRoutes(
	group,
	p.App.Path(),
	p.GenericProxyController,
	p.AccountProxyJWTGateMiddleware,
)
```

- [ ] **Step 3: Register gate and stop invoking Account module**

In `api_gateway/src/cmd/bootstrap/all.go`, remove this line from `All()`:

```go
modules.AccountModule(),
```

Add the Account gate provider near other middleware providers:

```go
fx.Provide(middleware.NewAccountProxyJWTGateMiddleware),
```

The utilities/middleware provider block should include:

```go
fx.Provide(utils.NewJWTUtils),
fx.Provide(utils.NewKeycloakJwksUtils),
fx.Provide(utils.NewBaseAPIClientFactory),
fx.Provide(middleware.NewJWTMiddleware),
fx.Provide(middleware.NewCorsMiddleware),
fx.Provide(middleware.NewAccountProxyJWTGateMiddleware),
fx.Provide(common.NewGenericProxyController),
fx.Provide(common.NewWebSocketProxyController),
```

- [ ] **Step 4: Format changed Go files**

Run from `api_gateway`:

```bash
gofmt -w src/ui/middleware/jwt_middleware.go src/ui/middleware/rate_limit_middleware.go src/ui/middleware/account_proxy_jwt_gate_middleware.go src/ui/middleware/account_proxy_jwt_gate_middleware_test.go src/ui/controller/common/generic_proxy_controller_test.go src/ui/router/account_router.go src/ui/router/account_router_test.go src/ui/router/router.go src/cmd/bootstrap/all.go
```

Expected: command exits 0.

- [ ] **Step 5: Run compile-focused checks**

Run from `api_gateway`:

```bash
go test ./src/ui/router ./src/cmd/... -run '^$' -count=1
```

Expected: PASS or `[no test files]` with no compile errors.

- [ ] **Step 6: Commit FX cleanup**

```bash
git add src/ui/router/router.go src/cmd/bootstrap/all.go
git commit -m "perf(gateway): remove account module from gateway runtime"
```

## Task 6: Full Verification and Benchmark Comparison

**Files:**
- Verify only; no code edits expected.

- [ ] **Step 1: Run focused tests**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware ./src/ui/controller/common ./src/ui/router -count=1
```

Expected: PASS.

- [ ] **Step 2: Run command package compile checks**

Run from `api_gateway`:

```bash
go test ./src/cmd/... -run '^$' -count=1
```

Expected: PASS or `[no test files]` with no compile errors.

- [ ] **Step 3: Run full module test sweep**

Run from `api_gateway`:

```bash
go test ./... -count=1
```

Expected: PASS.

- [ ] **Step 4: Run vet**

Run from `api_gateway`:

```bash
go vet ./...
```

Expected: exits 0 with no vet findings.

- [ ] **Step 5: Run representative proxy benchmark**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -bench '^BenchmarkGenericProxyController_CRM_' -benchmem -run '^$' -count=5
```

Expected: PASS with benchmark output. Use this as a reverse-proxy hot-path sanity check; do not block on small Windows timing variance.

- [ ] **Step 6: Run middleware benchmark**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -bench '^BenchmarkRateLimitMiddleware_' -benchmem -run '^$' -count=5
```

Expected: PASS with benchmark output for IP and user rate-limit middleware.

- [ ] **Step 7: Final git status check**

Run from repo root:

```bash
git status --short
```

Expected: only unrelated pre-existing untracked files remain, such as `docs/superpowers/specs/2026-07-05-paginated-modules-and-ux-design.md`.
