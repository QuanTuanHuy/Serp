# API Gateway JWT Rate Limit Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Optimize protected API Gateway JWT authentication by validating each access token once, and add focused rate-limit measurement without changing Redis rate-limit semantics.

**Architecture:** Add `JWTUtils.ValidateAccessToken()` as the one-pass JWT hot path that returns validated claims and roles together. Update `JWTMiddleware.AuthenticateJWTContext()` to use that result, and extend benchmarks/tests to compare JWT before/after and measure route-override rate-limit overhead.

**Tech Stack:** Go 1.25, Gin, `github.com/golang-jwt/jwt/v5`, Go `testing` benchmarks, existing Redis rate-limit adapter.

---

## File Structure

- Modify `api_gateway/src/kernel/utils/jwt_utils.go`
  - Add `Claims.TokenType`.
  - Add `ValidatedJWT`.
  - Add `ValidateAccessToken`.
  - Add shared claims parsing, validation, token-type, and role extraction helpers.
  - Keep existing public helper methods compiling.
- Create `api_gateway/src/kernel/utils/jwt_utils_access_token_test.go`
  - Focused tests for one-pass access-token validation and token-type behavior.
- Modify `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`
  - Add payload token type to benchmark token.
  - Add `BenchmarkJWTUtils_ValidateAccessToken`.
- Modify `api_gateway/src/ui/middleware/jwt_middleware.go`
  - Switch `AuthenticateJWTContext` to `ValidateAccessToken`.
  - Preserve existing Gin context keys and error messages.
- Modify `api_gateway/src/ui/middleware/jwt_middleware_test.go`
  - Add a real signed JWT test through `httptest` JWKS so middleware context behavior is verified without private-field access.
- Modify `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`
  - Extend the fake limiter to capture key/rule values.
  - Add route-override normalization coverage.
- Modify `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`
  - Add route-override benchmark using the existing fake limiter.
- Do not modify `api_gateway/src/infrastructure/adapter/rate_limiter_adapter.go`
  - `go.mod` has no in-process Redis test dependency such as miniredis or redismock, and this phase must not add a new dependency.

## Task 1: Add Failing JWT Utils Access-Token Tests

**Files:**
- Create: `api_gateway/src/kernel/utils/jwt_utils_access_token_test.go`

- [ ] **Step 1: Create test file with signed-token helpers and access-token test cases**

Add this file:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"crypto/rand"
	"crypto/rsa"
	"slices"
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/api-gateway/src/kernel/properties"
)

const (
	testJWTIssuer   = "https://keycloak.example/realms/serp"
	testJWTAudience = "serp-api-gateway"
	testJWTKeyID    = "test-key"
)

func newTestJWTUtils(t *testing.T) (*JWTUtils, *rsa.PrivateKey) {
	t.Helper()

	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generate key: %v", err)
	}

	jwks := &KeycloakJwksUtils{
		keycloakProps: &properties.KeycloakProperties{},
		keyCache: map[string]*rsa.PublicKey{
			testJWTKeyID: &privateKey.PublicKey,
		},
		lastFetch: time.Now(),
		cacheTTL:  time.Hour,
	}

	jwtUtils := NewJWTUtils(&properties.KeycloakProperties{
		ExpectedIssuer:   testJWTIssuer,
		ExpectedAudience: testJWTAudience,
	}, jwks)

	return jwtUtils, privateKey
}

func newTestClaims(tokenType string) Claims {
	return Claims{
		UserID:            123,
		Email:             "user@example.com",
		FullName:          "Test User",
		PreferredUsername: "test.user",
		EmailVerified:     true,
		TokenType:         tokenType,
		RealmAccess: map[string]interface{}{
			"roles": []interface{}{"USER", "ADMIN"},
		},
		ResourceAccess: map[string]interface{}{
			"serp-api": map[string]interface{}{
				"roles": []interface{}{"PROJECT_VIEWER", "USER"},
			},
		},
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    testJWTIssuer,
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			Audience:  []string{testJWTAudience},
			Subject:   "123",
		},
	}
}

func signTestJWT(t *testing.T, privateKey *rsa.PrivateKey, claims Claims, headerType string) string {
	t.Helper()

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = testJWTKeyID
	if headerType != "" {
		token.Header["typ"] = headerType
	}

	tokenString, err := token.SignedString(privateKey)
	if err != nil {
		t.Fatalf("sign token: %v", err)
	}
	return tokenString
}

func assertRolesContain(t *testing.T, roles []string, expected ...string) {
	t.Helper()

	for _, role := range expected {
		if !slices.Contains(roles, role) {
			t.Fatalf("expected roles %v to contain %q", roles, role)
		}
	}
}

func TestJWTUtils_ValidateAccessToken_ReturnsClaimsAndRoles(t *testing.T) {
	jwtUtils, privateKey := newTestJWTUtils(t)
	token := signTestJWT(t, privateKey, newTestClaims("Bearer"), "JWT")

	validated, err := jwtUtils.ValidateAccessToken(token)
	if err != nil {
		t.Fatalf("validate access token: %v", err)
	}

	if validated.Claims.UserID != 123 {
		t.Fatalf("expected user id 123, got %d", validated.Claims.UserID)
	}
	if validated.Claims.Email != "user@example.com" {
		t.Fatalf("expected email user@example.com, got %s", validated.Claims.Email)
	}
	assertRolesContain(t, validated.Roles, "USER", "ADMIN", "PROJECT_VIEWER")
}

func TestJWTUtils_ValidateAccessToken_AcceptsMissingPayloadTokenType(t *testing.T) {
	jwtUtils, privateKey := newTestJWTUtils(t)
	token := signTestJWT(t, privateKey, newTestClaims(""), "JWT")

	if _, err := jwtUtils.ValidateAccessToken(token); err != nil {
		t.Fatalf("expected missing payload token type to be accepted, got %v", err)
	}
}

func TestJWTUtils_ValidateAccessToken_RejectsInvalidHeaderTokenType(t *testing.T) {
	jwtUtils, privateKey := newTestJWTUtils(t)
	token := signTestJWT(t, privateKey, newTestClaims("Bearer"), "JWE")

	if _, err := jwtUtils.ValidateAccessToken(token); err == nil {
		t.Fatalf("expected invalid header token type to be rejected")
	}
}

func TestJWTUtils_ValidateAccessToken_RejectsInvalidPayloadTokenType(t *testing.T) {
	jwtUtils, privateKey := newTestJWTUtils(t)
	token := signTestJWT(t, privateKey, newTestClaims("Refresh"), "JWT")

	if _, err := jwtUtils.ValidateAccessToken(token); err == nil {
		t.Fatalf("expected invalid payload token type to be rejected")
	}
}
```

- [ ] **Step 2: Run the new tests and verify they fail**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/utils -run 'TestJWTUtils_ValidateAccessToken' -count=1
```

Expected: FAIL because `Claims.TokenType`, `ValidatedJWT`, and `JWTUtils.ValidateAccessToken` do not exist yet.

## Task 2: Implement One-Pass JWT Access-Token Validation

**Files:**
- Modify: `api_gateway/src/kernel/utils/jwt_utils.go`
- Test: `api_gateway/src/kernel/utils/jwt_utils_access_token_test.go`

- [ ] **Step 1: Add token type and validated result types**

In `api_gateway/src/kernel/utils/jwt_utils.go`, update `Claims` and add `ValidatedJWT` after `Claims`:

```go
type Claims struct {
	UserID            int64                  `json:"uid"`
	Email             string                 `json:"email"`
	FullName          string                 `json:"name"`
	PreferredUsername string                 `json:"preferred_username"`
	EmailVerified     bool                   `json:"email_verified"`
	TokenType         string                 `json:"typ"`
	RealmAccess       map[string]interface{} `json:"realm_access"`
	ResourceAccess    map[string]interface{} `json:"resource_access"`
	AuthorizedParty   string                 `json:"azp"`
	SessionId         string                 `json:"sid"`
	jwt.RegisteredClaims
}

type ValidatedJWT struct {
	Claims *Claims
	Roles  []string
}

var ErrInvalidTokenType = errors.New("invalid token type")
```

- [ ] **Step 2: Replace `ValidateToken` internals and add access-token helpers**

In `api_gateway/src/kernel/utils/jwt_utils.go`, replace the current `ValidateToken` function and add these helpers near it:

```go
func (j *JWTUtils) ValidateAccessToken(tokenString string) (*ValidatedJWT, error) {
	claims, token, err := j.parseAndValidateClaims(tokenString)
	if err != nil {
		log.Error("Failed to parse JWT token: ", err)
		return nil, err
	}

	if err := validateJWTHeaderType(token); err != nil {
		return nil, err
	}

	if claims.TokenType != "" && claims.TokenType != "Bearer" {
		return nil, ErrInvalidTokenType
	}

	return &ValidatedJWT{
		Claims: claims,
		Roles:  extractRolesFromClaims(claims),
	}, nil
}

func (j *JWTUtils) ValidateToken(tokenString string) (*Claims, error) {
	claims, _, err := j.parseAndValidateClaims(tokenString)
	if err != nil {
		log.Error("Failed to parse JWT token: ", err)
		return nil, err
	}

	return claims, nil
}

func (j *JWTUtils) parseAndValidateClaims(tokenString string) (*Claims, *jwt.Token, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		return j.signingKeyForToken(token)
	})
	if err != nil {
		return nil, nil, err
	}

	claims, ok := token.Claims.(*Claims)
	if !ok || !token.Valid {
		return nil, nil, errors.New("invalid token")
	}

	if err := j.validateRegisteredClaims(claims); err != nil {
		return nil, nil, err
	}

	return claims, token, nil
}

func (j *JWTUtils) signingKeyForToken(token *jwt.Token) (interface{}, error) {
	if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
		return nil, errors.New("unexpected signing method")
	}

	keyId, ok := token.Header["kid"].(string)
	if !ok {
		log.Warn("No key ID found in JWT header, skipping signature verification")
		return nil, errors.New("no key ID in JWT header")
	}

	publicKey, err := j.keycloakJwksUtils.GetPublicKey(keyId)
	if err != nil {
		log.Warn("Could not find public key for key ID: ", keyId, ", error: ", err)
		return nil, err
	}

	return publicKey, nil
}

func (j *JWTUtils) validateRegisteredClaims(claims *Claims) error {
	if claims.ExpiresAt != nil && time.Now().After(claims.ExpiresAt.Time) {
		log.Error("JWT token is expired")
		return errors.New("token is expired")
	}

	if j.keycloakProps.ExpectedIssuer != "" && claims.Issuer != j.keycloakProps.ExpectedIssuer {
		log.Error("JWT token issuer mismatch. Expected: ", j.keycloakProps.ExpectedIssuer, ", Actual: ", claims.Issuer)
		return errors.New("token issuer mismatch")
	}

	if j.keycloakProps.ExpectedAudience != "" {
		audienceFound := slices.Contains(claims.Audience, j.keycloakProps.ExpectedAudience)
		if !audienceFound {
			log.Error("JWT token audience mismatch. Expected: ", j.keycloakProps.ExpectedAudience, ", Actual: ", claims.Audience)
			return errors.New("token audience mismatch")
		}
	}

	return nil
}

func validateJWTHeaderType(token *jwt.Token) error {
	if tokenType, ok := token.Header["typ"].(string); ok && tokenType != "JWT" {
		return ErrInvalidTokenType
	}

	return nil
}
```

- [ ] **Step 3: Share role extraction from validated claims**

In `api_gateway/src/kernel/utils/jwt_utils.go`, replace `ExtractRoles`, `GetRealmRolesFromToken`, and `GetResourceRolesFromToken` with:

```go
func (j *JWTUtils) ExtractRoles(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	return extractRolesFromClaims(claims), nil
}

func extractRolesFromClaims(claims *Claims) []string {
	var roles []string

	roles = append(roles, extractRealmRolesFromClaims(claims)...)

	if claims != nil && claims.ResourceAccess != nil {
		for _, clientAccess := range claims.ResourceAccess {
			if clientMap, ok := clientAccess.(map[string]interface{}); ok {
				if clientRoles, ok := clientMap["roles"].([]interface{}); ok {
					for _, role := range clientRoles {
						if roleStr, ok := role.(string); ok {
							roles = append(roles, roleStr)
						}
					}
				}
			}
		}
	}

	unique := make(map[string]bool)
	var result []string
	for _, role := range roles {
		if !unique[role] {
			unique[role] = true
			result = append(result, role)
		}
	}

	return result
}

func extractRealmRolesFromClaims(claims *Claims) []string {
	var roles []string
	if claims != nil && claims.RealmAccess != nil {
		if realmRoles, ok := claims.RealmAccess["roles"].([]interface{}); ok {
			for _, role := range realmRoles {
				if roleStr, ok := role.(string); ok {
					roles = append(roles, roleStr)
				}
			}
		}
	}

	return roles
}

func extractResourceRolesFromClaims(claims *Claims, clientId string) []string {
	var roles []string
	if claims != nil && claims.ResourceAccess != nil {
		if clientAccess, ok := claims.ResourceAccess[clientId].(map[string]interface{}); ok {
			if clientRoles, ok := clientAccess["roles"].([]interface{}); ok {
				for _, role := range clientRoles {
					if roleStr, ok := role.(string); ok {
						roles = append(roles, roleStr)
					}
				}
			}
		}
	}

	return roles
}

func (j *JWTUtils) GetRealmRolesFromToken(tokenString string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	return extractRealmRolesFromClaims(claims), nil
}

func (j *JWTUtils) GetResourceRolesFromToken(tokenString string, clientId string) ([]string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	return extractResourceRolesFromClaims(claims, clientId), nil
}
```

Delete the old bodies of these three methods so each method is defined once.

- [ ] **Step 4: Format and run focused JWT utils tests**

Run from `api_gateway/`:

```powershell
gofmt -w src/kernel/utils/jwt_utils.go src/kernel/utils/jwt_utils_access_token_test.go
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/utils -run 'TestJWTUtils_ValidateAccessToken' -count=1
```

Expected: PASS for all `ValidateAccessToken` tests.

- [ ] **Step 5: Commit JWT utils implementation**

Run from repository root:

```powershell
git add -- api_gateway/src/kernel/utils/jwt_utils.go api_gateway/src/kernel/utils/jwt_utils_access_token_test.go
git commit -m "perf(gateway): validate jwt access tokens once"
```

## Task 3: Switch JWT Middleware to One-Pass Validation

**Files:**
- Modify: `api_gateway/src/ui/middleware/jwt_middleware.go`
- Modify: `api_gateway/src/ui/middleware/jwt_middleware_test.go`

- [ ] **Step 1: Add signed-JWT middleware context test**

Append these imports to `api_gateway/src/ui/middleware/jwt_middleware_test.go`:

```go
import (
	"crypto/rand"
	"crypto/rsa"
	"encoding/base64"
	"math/big"
	"net/http"
	"net/http/httptest"
	"slices"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
)
```

Replace the existing import block with the full block above, then append this helper and test:

```go
const (
	middlewareJWTIssuer   = "https://keycloak.example/realms/serp"
	middlewareJWTAudience = "serp-api-gateway"
	middlewareJWTKeyID    = "middleware-key"
)

func newSignedJWTMiddleware(t *testing.T) (*JWTMiddleware, string) {
	t.Helper()

	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generate key: %v", err)
	}

	jwksServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(`{"keys":[` + rsaPublicJWK(&privateKey.PublicKey) + `]}`))
	}))
	t.Cleanup(jwksServer.Close)

	jwtUtils := utils.NewJWTUtils(&properties.KeycloakProperties{
		JwkSetUri:        jwksServer.URL,
		ExpectedIssuer:   middlewareJWTIssuer,
		ExpectedAudience: middlewareJWTAudience,
	}, utils.NewKeycloakJwksUtils(&properties.KeycloakProperties{
		JwkSetUri: jwksServer.URL,
	}))

	claims := utils.Claims{
		UserID:            456,
		Email:             "middleware@example.com",
		FullName:          "Middleware User",
		PreferredUsername: "middleware.user",
		EmailVerified:     true,
		TokenType:         "Bearer",
		RealmAccess: map[string]interface{}{
			"roles": []interface{}{"USER", "ADMIN"},
		},
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    middlewareJWTIssuer,
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			Audience:  []string{middlewareJWTAudience},
			Subject:   "456",
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = middlewareJWTKeyID
	token.Header["typ"] = "JWT"

	tokenString, err := token.SignedString(privateKey)
	if err != nil {
		t.Fatalf("sign token: %v", err)
	}

	return NewJWTMiddleware(jwtUtils), tokenString
}

func rsaPublicJWK(publicKey *rsa.PublicKey) string {
	n := base64.RawURLEncoding.EncodeToString(publicKey.N.Bytes())
	e := base64.RawURLEncoding.EncodeToString(big.NewInt(int64(publicKey.E)).Bytes())

	return `{"kty":"RSA","kid":"` + middlewareJWTKeyID + `","use":"sig","alg":"RS256","n":"` + n + `","e":"` + e + `"}`
}

func TestJWTMiddleware_AuthenticateJWTContext_ValidTokenSetsContext(t *testing.T) {
	gin.SetMode(gin.TestMode)
	m, token := newSignedJWTMiddleware(t)

	r := gin.New()
	r.GET("/protected", func(c *gin.Context) {
		if !m.AuthenticateJWTContext(c) {
			t.Fatalf("expected auth to continue")
		}

		if userID, exists := c.Get("userID"); !exists || userID != int64(456) {
			t.Fatalf("expected userID 456, got %v exists=%v", userID, exists)
		}
		if email, exists := c.Get("userEmail"); !exists || email != "middleware@example.com" {
			t.Fatalf("expected userEmail middleware@example.com, got %v exists=%v", email, exists)
		}
		if tokenValue, exists := c.Get("token"); !exists || tokenValue != token {
			t.Fatalf("expected raw token in context")
		}

		rolesValue, exists := c.Get("roles")
		if !exists {
			t.Fatalf("expected roles in context")
		}
		roles, ok := rolesValue.([]string)
		if !ok {
			t.Fatalf("expected []string roles, got %T", rolesValue)
		}
		if !slices.Contains(roles, "USER") || !slices.Contains(roles, "ADMIN") {
			t.Fatalf("expected USER and ADMIN roles, got %v", roles)
		}

		c.String(http.StatusOK, "ok")
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected", nil)
	req.Header.Set("Authorization", "Bearer "+token)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
}
```

- [ ] **Step 2: Run middleware context regression test before switching implementation**

Run from `api_gateway/`:

```powershell
gofmt -w src/ui/middleware/jwt_middleware_test.go
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/middleware -run 'TestJWTMiddleware_AuthenticateJWTContext_ValidTokenSetsContext' -count=1
```

Expected before the middleware switch: PASS is possible because old code is behaviorally correct. If it passes, continue; this test is a regression guard for context parity rather than a red-only test.

- [ ] **Step 3: Update `AuthenticateJWTContext` to use `ValidateAccessToken`**

In `api_gateway/src/ui/middleware/jwt_middleware.go`, add the standard-library `errors` import:

```go
import (
	"errors"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/utils"
)
```

In `api_gateway/src/ui/middleware/jwt_middleware.go`, replace this block:

```go
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
```

with:

```go
validated, err := m.jwtUtils.ValidateAccessToken(token)
if err != nil {
	message := "Invalid or expired token"
	if errors.Is(err, utils.ErrInvalidTokenType) {
		message = "Invalid token type"
	}
	utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, message)
	c.Abort()
	return false
}

claims := validated.Claims
```

Then replace the role extraction block:

```go
roles, err := m.jwtUtils.ExtractRoles(token)
if err != nil {
	log.Warn(c, "Failed to extract roles: ", err)
	roles = []string{}
}
c.Set("roles", roles)
```

with:

```go
c.Set("roles", validated.Roles)
```

If `log` becomes unused in `jwt_middleware.go`, remove the import only if no other function in the file uses it. `OptionalJWT` still uses `log`, so the import should remain.

- [ ] **Step 4: Run focused middleware tests**

Run from `api_gateway/`:

```powershell
gofmt -w src/ui/middleware/jwt_middleware.go src/ui/middleware/jwt_middleware_test.go
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/middleware -run 'TestJWTMiddleware_' -count=1
```

Expected: PASS for missing-header tests and valid-token context test.

- [ ] **Step 5: Commit middleware switch**

Run from repository root:

```powershell
git add -- api_gateway/src/ui/middleware/jwt_middleware.go api_gateway/src/ui/middleware/jwt_middleware_test.go
git commit -m "perf(gateway): use one-pass jwt validation in middleware"
```

## Task 4: Add JWT One-Pass Benchmark

**Files:**
- Modify: `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`

- [ ] **Step 1: Add payload token type to benchmark token**

In `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`, add `TokenType: "Bearer"` to the `claims := Claims{...}` literal:

```go
claims := Claims{
	UserID:            123,
	Email:             "user@example.com",
	FullName:          "Test User",
	PreferredUsername: "test.user",
	TokenType:         "Bearer",
	RealmAccess: map[string]interface{}{
		"roles": []interface{}{"USER", "ADMIN"},
	},
	RegisteredClaims: jwt.RegisteredClaims{
		Issuer:    "https://keycloak.example/realms/serp",
		ExpiresAt: jwt.NewNumericDate(time.Now().Add(time.Hour)),
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		Audience:  []string{"serp-api-gateway"},
		Subject:   "123",
	},
}
```

- [ ] **Step 2: Add `ValidateAccessToken` benchmark**

Append this benchmark to `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`:

```go
func BenchmarkJWTUtils_ValidateAccessToken(b *testing.B) {
	jwtUtils, token := newBenchmarkJWTUtils(b)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		validated, err := jwtUtils.ValidateAccessToken(token)
		if err != nil {
			b.Fatalf("validate access token: %v", err)
		}
		if validated.Claims.UserID != 123 {
			b.Fatalf("expected user id 123, got %d", validated.Claims.UserID)
		}
		if len(validated.Roles) != 2 {
			b.Fatalf("expected 2 roles, got %d", len(validated.Roles))
		}
	}
}
```

- [ ] **Step 3: Run JWT benchmarks**

Run from `api_gateway/`:

```powershell
gofmt -w src/kernel/utils/jwt_utils_benchmark_test.go
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/utils -bench 'JWT' -benchmem -run '^$' -count=5
```

Expected: PASS. `BenchmarkJWTUtils_ValidateAccessToken` should be materially cheaper than `BenchmarkJWTUtils_CurrentAuthenticateJWTWork` in `ns/op`, allocations, or both.

- [ ] **Step 4: Commit benchmark**

Run from repository root:

```powershell
git add -- api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go
git commit -m "test(gateway): benchmark one-pass jwt validation"
```

## Task 5: Add Rate-Limit Route Override Measurement

**Files:**
- Modify: `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`
- Modify: `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`

- [ ] **Step 1: Extend fake test limiter to capture values**

In `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`, update `testRateLimiter` and `CheckRateLimit` to:

```go
type testRateLimiter struct {
	called     bool
	allowed    bool
	key        string
	limit      int
	windowSecs int
}

func (t *testRateLimiter) CheckRateLimit(
	ctx context.Context,
	key string,
	limit int,
	windowSecs int,
) (*port.RateLimitResult, error) {
	t.called = true
	t.key = key
	t.limit = limit
	t.windowSecs = windowSecs
	return &port.RateLimitResult{
		Allowed:    t.allowed,
		Limit:      limit,
		Remaining:  limit - 1,
		ResetAt:    time.Now().Add(time.Duration(windowSecs) * time.Second).Unix(),
		RetryAfter: 1,
	}, nil
}
```

- [ ] **Step 2: Add route override normalization test**

Append this test to `api_gateway/src/ui/middleware/rate_limit_middleware_test.go`:

```go
func TestRateLimitMiddleware_ApplyUserRateLimit_RouteOverrideUsesNormalizedPath(t *testing.T) {
	gin.SetMode(gin.TestMode)
	limiter := &testRateLimiter{allowed: true}
	overrideRule := properties.RateLimitRule{Limit: 3, WindowSecs: 30}
	m := NewRateLimitMiddleware(limiter, &properties.RateLimitProperties{
		Enabled:     true,
		DefaultUser: properties.RateLimitRule{Limit: 10, WindowSecs: 60},
		RouteOverrides: []properties.RouteOverride{
			{
				Method: http.MethodGet,
				Path:   "/protected",
				User:   &overrideRule,
			},
		},
	})

	r := gin.New()
	r.GET("/protected/", func(c *gin.Context) {
		c.Set("userID", int64(123))
		if m.ApplyUserRateLimit(c) {
			c.String(http.StatusOK, "ok")
		}
	})

	w := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodGet, "/protected/", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, w.Code)
	}
	if limiter.limit != 3 {
		t.Fatalf("expected override limit 3, got %d", limiter.limit)
	}
	if limiter.windowSecs != 30 {
		t.Fatalf("expected override window 30, got %d", limiter.windowSecs)
	}
	if limiter.key != "user:123:GET:/protected" {
		t.Fatalf("expected override key user:123:GET:/protected, got %s", limiter.key)
	}
}
```

- [ ] **Step 3: Add route override benchmark**

In `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`, add `fmt` to imports:

```go
import (
	"context"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	port "github.com/serp/api-gateway/src/core/port/rate_limiter"
	"github.com/serp/api-gateway/src/kernel/properties"
)
```

Then append:

```go
func newBenchmarkRateLimitMiddlewareWithOverrides(overrideCount int) *RateLimitMiddleware {
	overrides := make([]properties.RouteOverride, 0, overrideCount+1)
	for i := 0; i < overrideCount; i++ {
		ipRule := properties.RateLimitRule{Limit: 1000 + i, WindowSecs: 60}
		overrides = append(overrides, properties.RouteOverride{
			Method: http.MethodGet,
			Path:   fmt.Sprintf("/bench/%d", i),
			IP:     &ipRule,
		})
	}

	targetRule := properties.RateLimitRule{Limit: 2000, WindowSecs: 30}
	overrides = append(overrides, properties.RouteOverride{
		Method: http.MethodGet,
		Path:   "/bench",
		IP:     &targetRule,
	})

	return NewRateLimitMiddleware(
		benchmarkRateLimiter{},
		&properties.RateLimitProperties{
			Enabled:        true,
			DefaultIP:      properties.RateLimitRule{Limit: 1000, WindowSecs: 60},
			DefaultUser:    properties.RateLimitRule{Limit: 2000, WindowSecs: 60},
			RouteOverrides: overrides,
		},
	)
}

func BenchmarkRateLimitMiddleware_IPRateLimit_RouteOverrideLookup(b *testing.B) {
	gin.SetMode(gin.TestMode)
	m := newBenchmarkRateLimitMiddlewareWithOverrides(500)

	r := gin.New()
	r.Use(m.IPRateLimit())
	r.GET("/bench", func(c *gin.Context) {
		c.String(http.StatusOK, "ok")
	})

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		w := httptest.NewRecorder()
		req := httptest.NewRequest(http.MethodGet, "/bench", nil)
		req.RemoteAddr = "127.0.0.1:12345"
		r.ServeHTTP(w, req)
		if w.Code != http.StatusOK {
			b.Fatalf("expected %d, got %d", http.StatusOK, w.Code)
		}
	}
}
```

- [ ] **Step 4: Run focused rate-limit tests and benchmarks**

Run from `api_gateway/`:

```powershell
gofmt -w src/ui/middleware/rate_limit_middleware_test.go src/ui/middleware/rate_limit_middleware_benchmark_test.go
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/middleware -run 'TestRateLimitMiddleware_' -count=1
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/ui/middleware -bench 'RateLimit' -benchmem -run '^$' -count=5
```

Expected: tests and benchmarks PASS. The benchmark output includes `BenchmarkRateLimitMiddleware_IPRateLimit_RouteOverrideLookup`.

- [ ] **Step 5: Commit rate-limit measurement**

Run from repository root:

```powershell
git add -- api_gateway/src/ui/middleware/rate_limit_middleware_test.go api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go
git commit -m "test(gateway): measure rate limit route override overhead"
```

## Task 6: Final Verification

**Files:**
- Verify all files touched by Tasks 1-5.

- [ ] **Step 1: Run focused package tests**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/utils ./src/ui/middleware -count=1
```

Expected: PASS for both packages.

- [ ] **Step 2: Run benchmark suite required by the spec**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./src/kernel/utils ./src/ui/middleware -bench 'JWT|RateLimit' -benchmem -run '^$' -count=5
```

Expected: PASS. Capture representative output for `BenchmarkJWTUtils_CurrentAuthenticateJWTWork`, `BenchmarkJWTUtils_ValidateAccessToken`, and rate-limit benchmarks in the final implementation summary.

- [ ] **Step 3: Run full module tests**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go test ./... -count=1
```

Expected: PASS for all packages.

- [ ] **Step 4: Run vet**

Run from `api_gateway/`:

```powershell
$env:GOCACHE='D:\User2\open_source\serp\api_gateway\.codex-go-cache'; go vet ./...
```

Expected: no output and exit code 0.

- [ ] **Step 5: Check repository status**

Run from repository root:

```powershell
git status --short
```

Expected: no modified tracked files from this plan. Pre-existing unrelated untracked files may remain; do not stage or delete them.

- [ ] **Step 6: Clean local Go cache if it was created**

Run from repository root only after verifying the resolved path is inside `api_gateway`:

```powershell
$cachePath = Resolve-Path 'api_gateway\.codex-go-cache' -ErrorAction SilentlyContinue
if ($cachePath -and $cachePath.Path.StartsWith((Resolve-Path 'api_gateway').Path)) {
    Remove-Item -LiteralPath $cachePath.Path -Recurse -Force
}
```

Expected: local `.codex-go-cache` is removed if present. This command is destructive and must use approval if the sandbox requires it.
