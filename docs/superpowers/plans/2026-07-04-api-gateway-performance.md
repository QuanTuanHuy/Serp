# API Gateway Performance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add repeatable gateway performance baselines, then tune HTTP transport pooling for generic proxy and custom adapter hot paths without changing public API behavior.

**Architecture:** Start with Go-native benchmarks against mock upstream servers so gateway overhead is isolated from downstream services. Add an `app.transport` property group and a transport/client factory, then wire it into `GenericProxyController` and `BaseAPIClient` users while preserving retry, circuit breaker, context, and response-shape behavior.

**Tech Stack:** Go 1.25, Gin, `net/http`, `httptest`, Uber FX, gobreaker, golib config properties.

---

## File Structure

- `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`
  - Extend existing generic proxy tests and benchmarks with small/medium GET and POST benchmark scenarios.
- `api_gateway/src/kernel/utils/base_api_benchmark_test.go`
  - New benchmark file for `BaseAPIClient` GET/POST and unmarshal overhead against mock upstreams.
- `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`
  - New benchmark file that measures current repeated JWT validation and role extraction cost with an in-memory RSA key.
- `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`
  - New benchmark file that measures IP and user rate-limit middleware overhead with a fake in-memory limiter.
- `api_gateway/src/kernel/properties/transport_properties.go`
  - New config struct for upstream HTTP transport pool and timeout knobs.
- `api_gateway/src/kernel/properties/transport_properties_test.go`
  - New tests for default transport property values.
- `api_gateway/src/kernel/utils/http_transport.go`
  - Add configurable transport creation, allow resilient transport to receive a base transport, and keep current retry/circuit-breaker semantics.
- `api_gateway/src/kernel/utils/http_transport_test.go`
  - New tests for transport defaults and transport-chain behavior.
- `api_gateway/src/kernel/utils/base_api.go`
  - Add a `BaseAPIClientFactory` and `NewBaseAPIClientWithHTTPClient`; keep existing constructor behavior intact.
- `api_gateway/src/kernel/utils/base_api_test.go`
  - New tests that verify factory-created clients use configured timeout and a non-nil transport.
- `api_gateway/src/ui/controller/common/generic_proxy_controller.go`
  - Inject transport properties into proxy construction and use a configured upstream transport.
- `api_gateway/src/cmd/bootstrap/all.go`
  - Register `TransportProperties` and `BaseAPIClientFactory`.
- `api_gateway/src/config/local.yaml`
  - Add `app.transport` values for local/dev.
- `api_gateway/src/config/production.yaml`
  - Add conservative `app.transport` values for production.
- `api_gateway/src/infrastructure/client/account/*.go`
  - Replace direct `utils.NewBaseAPIClient(...)` construction with `clientFactory.New(...)`.
- `api_gateway/src/infrastructure/client/crm/*.go`
  - Replace direct `utils.NewBaseAPIClient(...)` construction with `clientFactory.New(...)`.
- `api_gateway/src/infrastructure/client/ptm/*.go`
  - Replace direct `utils.NewBaseAPIClient(...)` construction with `clientFactory.New(...)`.
- `docs/superpowers/specs/2026-07-04-api-gateway-performance-design.md`
  - Reference only; do not edit unless implementation discovers a design contradiction.

## Task 1: Extend Generic Proxy Benchmarks

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Add benchmark helpers**

Append these helpers near the existing `BenchmarkGenericProxyController_CRM_GET_200`:

```go
func newBenchmarkGenericProxyGateway(b *testing.B, responseBody []byte) (*httptest.Server, *http.Client) {
	b.Helper()
	gin.SetMode(gin.TestMode)

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(responseBody)
	}))

	u, err := url.Parse(upstream.URL)
	if err != nil {
		upstream.Close()
		b.Fatalf("parse upstream URL: %v", err)
	}

	resProps := defaultResilienceProps()
	resProps.MaxRetries = 0

	controller := NewGenericProxyController(
		&properties.ExternalServiceProperties{
			CrmService: properties.ServiceProperty{Host: u.Hostname(), Port: u.Port()},
		},
		resProps,
	)

	r := gin.New()
	r.Any("/crm/api/v1/*proxyPath", controller.ProxyHandler("crm"))

	gateway := httptest.NewServer(r)
	b.Cleanup(func() {
		gateway.Close()
		upstream.Close()
	})

	return gateway, &http.Client{}
}

func benchmarkGenericProxyGET(b *testing.B, responseBody []byte) {
	gateway, client := newBenchmarkGenericProxyGateway(b, responseBody)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := client.Get(gateway.URL + "/crm/api/v1/leads?stage=open")
		if err != nil {
			b.Fatalf("request failed: %v", err)
		}
		_, _ = io.ReadAll(resp.Body)
		resp.Body.Close()
		if resp.StatusCode != http.StatusOK {
			b.Fatalf("expected %d, got %d", http.StatusOK, resp.StatusCode)
		}
	}
}

func benchmarkGenericProxyPOST(b *testing.B, requestBody []byte, responseBody []byte) {
	gateway, client := newBenchmarkGenericProxyGateway(b, responseBody)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		req, err := http.NewRequest(
			http.MethodPost,
			gateway.URL+"/crm/api/v1/leads",
			bytes.NewReader(requestBody),
		)
		if err != nil {
			b.Fatalf("new request: %v", err)
		}
		req.Header.Set("Content-Type", "application/json")

		resp, err := client.Do(req)
		if err != nil {
			b.Fatalf("request failed: %v", err)
		}
		_, _ = io.ReadAll(resp.Body)
		resp.Body.Close()
		if resp.StatusCode != http.StatusOK {
			b.Fatalf("expected %d, got %d", http.StatusOK, resp.StatusCode)
		}
	}
}
```

Add `bytes` to the test file imports:

```go
import (
	"bytes"
	"io"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"sync/atomic"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/kernel/properties"
)
```

- [ ] **Step 2: Add benchmark cases**

Append these benchmark functions:

```go
func BenchmarkGenericProxyController_CRM_GET_SmallJSON(b *testing.B) {
	benchmarkGenericProxyGET(b, []byte(`{"code":200,"status":"success","data":{"id":1}}`))
}

func BenchmarkGenericProxyController_CRM_GET_MediumJSON(b *testing.B) {
	item := `{"id":1,"name":"lead","stage":"open","ownerId":1001,"amount":123456789}`
	body := []byte(`{"code":200,"status":"success","data":[` + strings.Repeat(item+",", 49) + item + `]}`)
	benchmarkGenericProxyGET(b, body)
}

func BenchmarkGenericProxyController_CRM_POST_SmallJSON(b *testing.B) {
	benchmarkGenericProxyPOST(
		b,
		[]byte(`{"name":"lead","stage":"open"}`),
		[]byte(`{"code":200,"status":"success","data":{"id":1}}`),
	)
}

func BenchmarkGenericProxyController_CRM_POST_MediumJSON(b *testing.B) {
	requestBody := []byte(`{"name":"lead","notes":"` + strings.Repeat("x", 4096) + `"}`)
	responseBody := []byte(`{"code":200,"status":"success","data":{"id":1}}`)
	benchmarkGenericProxyPOST(b, requestBody, responseBody)
}
```

- [ ] **Step 3: Run existing tests to catch benchmark regressions**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController' -count=1
```

Expected: PASS.

- [ ] **Step 4: Run benchmark baseline**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -bench '^BenchmarkGenericProxyController_CRM_' -benchmem -run '^$' -count=5
```

Expected: benchmark output includes `ns/op`, `B/op`, and `allocs/op` for GET small, GET medium, POST small, POST medium, and the existing GET benchmark.

- [ ] **Step 5: Commit benchmark harness**

Commit this benchmark expansion:

```bash
git add src/ui/controller/common/generic_proxy_controller_test.go
git commit -m "test(gateway): expand generic proxy benchmarks"
```

## Task 2: Add BaseAPIClient Benchmarks

**Files:**
- Create: `api_gateway/src/kernel/utils/base_api_benchmark_test.go`

- [ ] **Step 1: Create the benchmark file**

Create `api_gateway/src/kernel/utils/base_api_benchmark_test.go`:

```go
package utils

import (
	"context"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

type benchmarkBaseResponse struct {
	Code   int    `json:"code"`
	Status string `json:"status"`
	Data   any    `json:"data"`
}

func newBenchmarkBaseAPIClient(b *testing.B, responseBody string) (*BaseAPIClient, func()) {
	b.Helper()

	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(responseBody))
	}))

	client := NewBaseAPIClient(upstream.URL, 5*time.Second)
	return client, upstream.Close
}

func benchmarkBaseAPIClientGET(b *testing.B, responseBody string) {
	apiClient, cleanup := newBenchmarkBaseAPIClient(b, responseBody)
	defer cleanup()

	ctx := context.Background()
	headers := BuildDefaultHeaders()

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := apiClient.GET(ctx, "/api/v1/items", headers)
		if err != nil {
			b.Fatalf("GET failed: %v", err)
		}
		var decoded benchmarkBaseResponse
		if err := apiClient.UnmarshalResponse(ctx, resp, &decoded); err != nil {
			b.Fatalf("unmarshal failed: %v", err)
		}
		if decoded.Code != http.StatusOK {
			b.Fatalf("expected code %d, got %d", http.StatusOK, decoded.Code)
		}
	}
}

func benchmarkBaseAPIClientPOST(b *testing.B, requestBody map[string]any, responseBody string) {
	apiClient, cleanup := newBenchmarkBaseAPIClient(b, responseBody)
	defer cleanup()

	ctx := context.Background()
	headers := BuildDefaultHeaders()

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		resp, err := apiClient.POST(ctx, "/api/v1/items", requestBody, headers)
		if err != nil {
			b.Fatalf("POST failed: %v", err)
		}
		var decoded benchmarkBaseResponse
		if err := apiClient.UnmarshalResponse(ctx, resp, &decoded); err != nil {
			b.Fatalf("unmarshal failed: %v", err)
		}
		if decoded.Code != http.StatusOK {
			b.Fatalf("expected code %d, got %d", http.StatusOK, decoded.Code)
		}
	}
}

func BenchmarkBaseAPIClient_GET_SmallJSON(b *testing.B) {
	benchmarkBaseAPIClientGET(b, `{"code":200,"status":"success","data":{"id":1}}`)
}

func BenchmarkBaseAPIClient_GET_MediumJSON(b *testing.B) {
	item := `{"id":1,"name":"item","description":"` + strings.Repeat("x", 128) + `"}`
	body := `{"code":200,"status":"success","data":[` + strings.Repeat(item+",", 49) + item + `]}`
	benchmarkBaseAPIClientGET(b, body)
}

func BenchmarkBaseAPIClient_POST_SmallJSON(b *testing.B) {
	benchmarkBaseAPIClientPOST(
		b,
		map[string]any{"name": "item", "status": "active"},
		`{"code":200,"status":"success","data":{"id":1}}`,
	)
}

func BenchmarkBaseAPIClient_POST_MediumJSON(b *testing.B) {
	benchmarkBaseAPIClientPOST(
		b,
		map[string]any{"name": "item", "description": strings.Repeat("x", 4096)},
		`{"code":200,"status":"success","data":{"id":1}}`,
	)
}
```

- [ ] **Step 2: Run BaseAPIClient benchmark baseline**

Run from `api_gateway`:

```bash
go test ./src/kernel/utils -bench '^BenchmarkBaseAPIClient_' -benchmem -run '^$' -count=5
```

Expected: PASS with benchmark output for four `BaseAPIClient` scenarios.

- [ ] **Step 3: Commit BaseAPIClient benchmarks**

```bash
git add src/kernel/utils/base_api_benchmark_test.go
git commit -m "test(gateway): add base api client benchmarks"
```

## Task 3: Add Middleware and JWT Cost Benchmarks

**Files:**
- Create: `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`
- Create: `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`

- [ ] **Step 1: Add rate limit middleware benchmarks**

Create `api_gateway/src/ui/middleware/rate_limit_middleware_benchmark_test.go`:

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

type benchmarkRateLimiter struct{}

func (benchmarkRateLimiter) CheckRateLimit(
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

func newBenchmarkRateLimitMiddleware() *RateLimitMiddleware {
	return NewRateLimitMiddleware(
		benchmarkRateLimiter{},
		&properties.RateLimitProperties{
			Enabled:     true,
			DefaultIP:   properties.RateLimitRule{Limit: 1000, WindowSecs: 60},
			DefaultUser: properties.RateLimitRule{Limit: 2000, WindowSecs: 60},
		},
	)
}

func BenchmarkRateLimitMiddleware_IPRateLimit(b *testing.B) {
	gin.SetMode(gin.TestMode)
	m := newBenchmarkRateLimitMiddleware()

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

func BenchmarkRateLimitMiddleware_UserRateLimit(b *testing.B) {
	gin.SetMode(gin.TestMode)
	m := newBenchmarkRateLimitMiddleware()

	r := gin.New()
	r.Use(func(c *gin.Context) {
		c.Set("userID", int64(123))
		c.Next()
	})
	r.Use(m.UserRateLimit())
	r.GET("/bench", func(c *gin.Context) {
		c.String(http.StatusOK, "ok")
	})

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		w := httptest.NewRecorder()
		req := httptest.NewRequest(http.MethodGet, "/bench", nil)
		r.ServeHTTP(w, req)
		if w.Code != http.StatusOK {
			b.Fatalf("expected %d, got %d", http.StatusOK, w.Code)
		}
	}
}
```

- [ ] **Step 2: Add JWT utility benchmarks**

Create `api_gateway/src/kernel/utils/jwt_utils_benchmark_test.go`:

```go
package utils

import (
	"crypto/rand"
	"crypto/rsa"
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/api-gateway/src/kernel/properties"
)

func newBenchmarkJWTUtils(b *testing.B) (*JWTUtils, string) {
	b.Helper()

	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		b.Fatalf("generate key: %v", err)
	}

	claims := Claims{
		UserID:            123,
		Email:             "user@example.com",
		FullName:          "Test User",
		PreferredUsername: "test.user",
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

	token := jwt.NewWithClaims(jwt.SigningMethodRS256, claims)
	token.Header["kid"] = "bench-key"
	token.Header["typ"] = "JWT"

	tokenString, err := token.SignedString(privateKey)
	if err != nil {
		b.Fatalf("sign token: %v", err)
	}

	jwks := &KeycloakJwksUtils{
		keycloakProps: &properties.KeycloakProperties{},
		keyCache: map[string]*rsa.PublicKey{
			"bench-key": &privateKey.PublicKey,
		},
		lastFetch: time.Now(),
		cacheTTL:  time.Hour,
	}

	utils := NewJWTUtils(&properties.KeycloakProperties{
		ExpectedIssuer:   "https://keycloak.example/realms/serp",
		ExpectedAudience: "serp-api-gateway",
	}, jwks)

	return utils, tokenString
}

func BenchmarkJWTUtils_ValidateToken(b *testing.B) {
	jwtUtils, token := newBenchmarkJWTUtils(b)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		claims, err := jwtUtils.ValidateToken(token)
		if err != nil {
			b.Fatalf("validate token: %v", err)
		}
		if claims.UserID != 123 {
			b.Fatalf("expected user id 123, got %d", claims.UserID)
		}
	}
}

func BenchmarkJWTUtils_CurrentAuthenticateJWTWork(b *testing.B) {
	jwtUtils, token := newBenchmarkJWTUtils(b)

	b.ReportAllocs()
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		if _, err := jwtUtils.ValidateToken(token); err != nil {
			b.Fatalf("validate token: %v", err)
		}
		if !jwtUtils.IsAccessToken(token) {
			b.Fatalf("expected access token")
		}
		roles, err := jwtUtils.ExtractRoles(token)
		if err != nil {
			b.Fatalf("extract roles: %v", err)
		}
		if len(roles) != 2 {
			b.Fatalf("expected 2 roles, got %d", len(roles))
		}
	}
}
```

- [ ] **Step 3: Run middleware and JWT benchmark baselines**

Run from `api_gateway`:

```bash
go test ./src/ui/middleware -bench '^BenchmarkRateLimitMiddleware_' -benchmem -run '^$' -count=5
go test ./src/kernel/utils -bench '^BenchmarkJWTUtils_' -benchmem -run '^$' -count=5
```

Expected: PASS with separate benchmark output for rate limit middleware and JWT utility work.

- [ ] **Step 4: Commit middleware/JWT benchmarks**

```bash
git add src/ui/middleware/rate_limit_middleware_benchmark_test.go src/kernel/utils/jwt_utils_benchmark_test.go
git commit -m "test(gateway): add middleware cost benchmarks"
```

## Task 4: Add Transport Properties

**Files:**
- Create: `api_gateway/src/kernel/properties/transport_properties.go`
- Create: `api_gateway/src/kernel/properties/transport_properties_test.go`
- Modify: `api_gateway/src/config/local.yaml`
- Modify: `api_gateway/src/config/production.yaml`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`

- [ ] **Step 1: Create transport properties**

Create `api_gateway/src/kernel/properties/transport_properties.go`:

```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import (
	"time"

	"github.com/golibs-starter/golib/config"
)

type TransportProperties struct {
	MaxIdleConns          int           `mapstructure:"maxIdleConns"`
	MaxIdleConnsPerHost   int           `mapstructure:"maxIdleConnsPerHost"`
	MaxConnsPerHost       int           `mapstructure:"maxConnsPerHost"`
	IdleConnTimeout       time.Duration `mapstructure:"idleConnTimeout"`
	DialTimeout           time.Duration `mapstructure:"dialTimeout"`
	TLSHandshakeTimeout   time.Duration `mapstructure:"tlsHandshakeTimeout"`
	ResponseHeaderTimeout time.Duration `mapstructure:"responseHeaderTimeout"`
	ExpectContinueTimeout time.Duration `mapstructure:"expectContinueTimeout"`
}

func (t TransportProperties) Prefix() string {
	return "app.transport"
}

func NewTransportProperties(loader config.Loader) (*TransportProperties, error) {
	props := NewDefaultTransportProperties()
	err := loader.Bind(&props)
	return &props, err
}

func NewDefaultTransportProperties() *TransportProperties {
	return &TransportProperties{
		MaxIdleConns:          200,
		MaxIdleConnsPerHost:   100,
		MaxConnsPerHost:       200,
		IdleConnTimeout:       90 * time.Second,
		DialTimeout:           5 * time.Second,
		TLSHandshakeTimeout:   5 * time.Second,
		ResponseHeaderTimeout: 15 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
}
```

- [ ] **Step 2: Add properties tests**

Create `api_gateway/src/kernel/properties/transport_properties_test.go`:

```go
package properties

import (
	"testing"
	"time"
)

func TestTransportProperties_Defaults(t *testing.T) {
	props := NewDefaultTransportProperties()

	if props.MaxIdleConns != 200 {
		t.Fatalf("expected MaxIdleConns 200, got %d", props.MaxIdleConns)
	}
	if props.MaxIdleConnsPerHost != 100 {
		t.Fatalf("expected MaxIdleConnsPerHost 100, got %d", props.MaxIdleConnsPerHost)
	}
	if props.MaxConnsPerHost != 200 {
		t.Fatalf("expected MaxConnsPerHost 200, got %d", props.MaxConnsPerHost)
	}
	if props.IdleConnTimeout != 90*time.Second {
		t.Fatalf("expected IdleConnTimeout 90s, got %v", props.IdleConnTimeout)
	}
	if props.ResponseHeaderTimeout != 15*time.Second {
		t.Fatalf("expected ResponseHeaderTimeout 15s, got %v", props.ResponseHeaderTimeout)
	}
}

func TestTransportProperties_Prefix(t *testing.T) {
	if prefix := (TransportProperties{}).Prefix(); prefix != "app.transport" {
		t.Fatalf("expected prefix app.transport, got %s", prefix)
	}
}
```

- [ ] **Step 3: Register properties in bootstrap**

In `api_gateway/src/cmd/bootstrap/all.go`, add this provider next to the other app properties:

```go
golib.ProvideProps(properties.NewTransportProperties),
```

The properties block should include:

```go
golib.ProvideProps(properties.NewExternalServicePropeties),
golib.ProvideProps(properties.NewKeycloakProperties),
golib.ProvideProps(properties.NewCorsProperties),
golib.ProvideProps(properties.NewRateLimitProperties),
golib.ProvideProps(properties.NewResilienceProperties),
golib.ProvideProps(properties.NewTransportProperties),
```

- [ ] **Step 4: Add YAML config**

In both `api_gateway/src/config/local.yaml` and `api_gateway/src/config/production.yaml`, add this under `app:` after `resilience:`:

```yaml
  transport:
    maxIdleConns: 200
    maxIdleConnsPerHost: 100
    maxConnsPerHost: 200
    idleConnTimeout: 90s
    dialTimeout: 5s
    tlsHandshakeTimeout: 5s
    responseHeaderTimeout: 15s
    expectContinueTimeout: 1s
```

- [ ] **Step 5: Verify properties compile**

Run from `api_gateway`:

```bash
go test ./src/kernel/properties -run '^TestTransportProperties' -count=1
```

Expected: PASS.

- [ ] **Step 6: Commit transport properties**

```bash
git add src/kernel/properties/transport_properties.go src/kernel/properties/transport_properties_test.go src/cmd/bootstrap/all.go src/config/local.yaml src/config/production.yaml
git commit -m "feat(gateway): add transport pool properties"
```

## Task 5: Add Transport and BaseAPIClient Factories

**Files:**
- Modify: `api_gateway/src/kernel/utils/http_transport.go`
- Create: `api_gateway/src/kernel/utils/http_transport_test.go`
- Modify: `api_gateway/src/kernel/utils/base_api.go`
- Create: `api_gateway/src/kernel/utils/base_api_test.go`
- Modify: `api_gateway/src/cmd/bootstrap/all.go`

- [ ] **Step 1: Extend transport construction**

In `api_gateway/src/kernel/utils/http_transport.go`, add imports for `net` and local properties:

```go
import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net"
	"net/http"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/sony/gobreaker/v2"
)
```

Add these functions:

```go
func NewUpstreamTransport(props *properties.TransportProperties) *http.Transport {
	if props == nil {
		props = properties.NewDefaultTransportProperties()
	}

	return &http.Transport{
		Proxy: http.ProxyFromEnvironment,
		DialContext: (&net.Dialer{
			Timeout:   props.DialTimeout,
			KeepAlive: 30 * time.Second,
		}).DialContext,
		ForceAttemptHTTP2:     true,
		MaxIdleConns:          props.MaxIdleConns,
		MaxIdleConnsPerHost:   props.MaxIdleConnsPerHost,
		MaxConnsPerHost:       props.MaxConnsPerHost,
		IdleConnTimeout:       props.IdleConnTimeout,
		TLSHandshakeTimeout:   props.TLSHandshakeTimeout,
		ResponseHeaderTimeout: props.ResponseHeaderTimeout,
		ExpectContinueTimeout: props.ExpectContinueTimeout,
	}
}

func NewHTTPClient(timeout time.Duration, props *properties.TransportProperties) *http.Client {
	if timeout == 0 {
		timeout = 30 * time.Second
	}

	return &http.Client{
		Timeout:   timeout,
		Transport: NewUpstreamTransport(props),
	}
}
```

Change `NewResilientTransport` to accept a base transport:

```go
func NewResilientTransport(
	base http.RoundTripper,
	cb *gobreaker.CircuitBreaker[*http.Response],
	maxRetries int,
	initialDelay, maxDelay time.Duration,
) *ResilientTransport {
	if base == nil {
		base = http.DefaultTransport
	}

	retryTransport := NewRetryTransport(base, maxRetries, initialDelay, maxDelay)
	cbTransport := NewCircuitBreakerTransport(retryTransport, cb)

	return &ResilientTransport{
		transport: cbTransport,
	}
}
```

- [ ] **Step 2: Add transport tests**

Create `api_gateway/src/kernel/utils/http_transport_test.go`:

```go
package utils

import (
	"net/http"
	"testing"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/sony/gobreaker/v2"
)

func TestNewUpstreamTransport_UsesProperties(t *testing.T) {
	props := &properties.TransportProperties{
		MaxIdleConns:          11,
		MaxIdleConnsPerHost:   12,
		MaxConnsPerHost:       13,
		IdleConnTimeout:       14 * time.Second,
		DialTimeout:           15 * time.Second,
		TLSHandshakeTimeout:   16 * time.Second,
		ResponseHeaderTimeout: 17 * time.Second,
		ExpectContinueTimeout: 18 * time.Second,
	}

	transport := NewUpstreamTransport(props)

	if transport.MaxIdleConns != 11 {
		t.Fatalf("expected MaxIdleConns 11, got %d", transport.MaxIdleConns)
	}
	if transport.MaxIdleConnsPerHost != 12 {
		t.Fatalf("expected MaxIdleConnsPerHost 12, got %d", transport.MaxIdleConnsPerHost)
	}
	if transport.MaxConnsPerHost != 13 {
		t.Fatalf("expected MaxConnsPerHost 13, got %d", transport.MaxConnsPerHost)
	}
	if transport.ResponseHeaderTimeout != 17*time.Second {
		t.Fatalf("expected ResponseHeaderTimeout 17s, got %v", transport.ResponseHeaderTimeout)
	}
}

func TestNewResilientTransport_UsesProvidedBaseTransport(t *testing.T) {
	base := http.DefaultTransport
	cb := gobreaker.NewCircuitBreaker[*http.Response](gobreaker.Settings{Name: "test"})

	transport := NewResilientTransport(base, cb, 0, time.Millisecond, time.Millisecond)
	rt, ok := transport.transport.(*CircuitBreakerTransport)
	if !ok {
		t.Fatalf("expected CircuitBreakerTransport, got %T", transport.transport)
	}
	retry, ok := rt.Base.(*RetryTransport)
	if !ok {
		t.Fatalf("expected RetryTransport, got %T", rt.Base)
	}
	if retry.Next != base {
		t.Fatalf("expected retry transport to use provided base transport")
	}
}
```

- [ ] **Step 3: Add BaseAPIClient factory**

In `api_gateway/src/kernel/utils/base_api.go`, add this type and constructor after `BaseAPIClient`:

```go
type BaseAPIClientFactory struct {
	transportProps *properties.TransportProperties
}

func NewBaseAPIClientFactory(transportProps *properties.TransportProperties) *BaseAPIClientFactory {
	return &BaseAPIClientFactory{transportProps: transportProps}
}

func (f *BaseAPIClientFactory) New(baseURL string, timeout time.Duration) *BaseAPIClient {
	return NewBaseAPIClientWithHTTPClient(NewHTTPClient(timeout, f.transportProps), baseURL, timeout)
}
```

Add the properties import to `base_api.go`:

```go
import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/kernel/properties"
)
```

Add this constructor after `NewBaseAPIClient`:

```go
func NewBaseAPIClientWithHTTPClient(client *http.Client, baseURL string, timeout time.Duration) *BaseAPIClient {
	if timeout == 0 {
		timeout = 30 * time.Second
	}

	return &BaseAPIClient{
		client:  client,
		timeout: timeout,
		baseURL: baseURL,
	}
}
```

Change `NewBaseAPIClient` to use `NewHTTPClient` with default transport properties:

```go
func NewBaseAPIClient(baseURL string, timeout time.Duration) *BaseAPIClient {
	if timeout == 0 {
		timeout = 30 * time.Second
	}

	return &BaseAPIClient{
		client:  NewHTTPClient(timeout, properties.NewDefaultTransportProperties()),
		timeout: timeout,
		baseURL: baseURL,
	}
}
```

- [ ] **Step 4: Add BaseAPIClient factory tests**

Create `api_gateway/src/kernel/utils/base_api_test.go`:

```go
package utils

import (
	"net/http"
	"testing"
	"time"

	"github.com/serp/api-gateway/src/kernel/properties"
)

func TestBaseAPIClientFactory_NewUsesConfiguredHTTPClient(t *testing.T) {
	factory := NewBaseAPIClientFactory(&properties.TransportProperties{
		MaxIdleConns:          10,
		MaxIdleConnsPerHost:   10,
		MaxConnsPerHost:       10,
		IdleConnTimeout:       time.Minute,
		DialTimeout:           time.Second,
		TLSHandshakeTimeout:   time.Second,
		ResponseHeaderTimeout: time.Second,
		ExpectContinueTimeout: time.Second,
	})

	apiClient := factory.New("http://example.test", 7*time.Second)
	httpClient, ok := apiClient.client.(*http.Client)
	if !ok {
		t.Fatalf("expected *http.Client, got %T", apiClient.client)
	}
	if httpClient.Timeout != 7*time.Second {
		t.Fatalf("expected timeout 7s, got %v", httpClient.Timeout)
	}
	if httpClient.Transport == nil {
		t.Fatalf("expected non-nil transport")
	}
}
```

- [ ] **Step 5: Register BaseAPIClientFactory**

In `api_gateway/src/cmd/bootstrap/all.go`, add this provider near other utilities:

```go
fx.Provide(utils.NewBaseAPIClientFactory),
```

The utilities block should include:

```go
fx.Provide(utils.NewJWTUtils),
fx.Provide(utils.NewKeycloakJwksUtils),
fx.Provide(utils.NewBaseAPIClientFactory),
fx.Provide(middleware.NewJWTMiddleware),
```

- [ ] **Step 6: Run utility tests**

Run from `api_gateway`:

```bash
go test ./src/kernel/properties ./src/kernel/utils -count=1
```

Expected: PASS.

- [ ] **Step 7: Commit transport factories**

```bash
git add src/kernel/utils/http_transport.go src/kernel/utils/http_transport_test.go src/kernel/utils/base_api.go src/kernel/utils/base_api_test.go src/cmd/bootstrap/all.go
git commit -m "feat(gateway): add configurable upstream transport factory"
```

## Task 6: Wire Transport Into Generic Proxy

**Files:**
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller.go`
- Modify: `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`

- [ ] **Step 1: Update GenericProxyController constructor**

Change the constructor signature:

```go
func NewGenericProxyController(
	svcProps *properties.ExternalServiceProperties,
	resProps *properties.ResilienceProperties,
	transportProps *properties.TransportProperties,
) *GenericProxyController {
```

Inside the route loop, call `buildProxy` with transport properties:

```go
for _, route := range routes {
	proxy, err := controller.buildProxy(route, resProps, transportProps)
	if err != nil {
		log.Warn("Failed to build proxy for service %s: %v", route.Name, err)
		continue
	}
	controller.proxies[route.Name] = proxy
}
```

Change `buildProxy` signature:

```go
func (c *GenericProxyController) buildProxy(
	route ServiceRoute,
	resProps *properties.ResilienceProperties,
	transportProps *properties.TransportProperties,
) (*httputil.ReverseProxy, error) {
```

Change proxy transport construction:

```go
proxy.Transport = utils.NewResilientTransport(
	utils.NewUpstreamTransport(transportProps),
	cb,
	resProps.MaxRetries,
	resProps.InitialDelay,
	resProps.MaxDelay,
)
```

- [ ] **Step 2: Update tests to pass transport defaults**

In `api_gateway/src/ui/controller/common/generic_proxy_controller_test.go`, every `NewGenericProxyController` call should use:

```go
controller := NewGenericProxyController(
	&properties.ExternalServiceProperties{
		CrmService: properties.ServiceProperty{Host: host, Port: port},
	},
	defaultResilienceProps(),
	properties.NewDefaultTransportProperties(),
)
```

Use the same third argument for `FirstMileService` and `SecondMileService` test constructors.

- [ ] **Step 3: Run proxy tests**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController' -count=1
```

Expected: PASS.

- [ ] **Step 4: Run generic proxy benchmarks**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -bench '^BenchmarkGenericProxyController_CRM_' -benchmem -run '^$' -count=5
```

Expected: PASS with benchmark output.

- [ ] **Step 5: Commit proxy transport wiring**

```bash
git add src/ui/controller/common/generic_proxy_controller.go src/ui/controller/common/generic_proxy_controller_test.go
git commit -m "perf(gateway): use configured transport for proxy routes"
```

## Task 7: Wire BaseAPIClientFactory Into Custom Adapters

**Files:**
- Modify: `api_gateway/src/infrastructure/client/account/auth_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/department_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/keycloak_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/menu_display_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/module_access_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/module_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/organization_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/permission_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/role_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/subscription_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/subscription_plan_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/account/user_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/crm/contact_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/crm/customer_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/crm/lead_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/crm/opportunity_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/availability_calendar_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/note_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/project_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/schedule_event_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/schedule_plan_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/schedule_task_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/schedule_window_client_adapter.go`
- Modify: `api_gateway/src/infrastructure/client/ptm/task_client_adapter.go`

- [ ] **Step 1: Update account adapter constructors**

For each account adapter constructor, add `clientFactory *utils.BaseAPIClientFactory` as a second constructor dependency and replace `utils.NewBaseAPIClient(...)` with `clientFactory.New(...)`.

Example for `api_gateway/src/infrastructure/client/account/auth_client_adapter.go`:

```go
func NewAuthClientAdapter(
	authProps *properties.ExternalServiceProperties,
	clientFactory *utils.BaseAPIClientFactory,
) port.IAuthClientPort {
	baseUrl := "http://" + authProps.AccountService.Host + ":" + authProps.AccountService.Port + "/account-service"
	apiClient := clientFactory.New(baseUrl, authProps.AccountService.Timeout)

	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &AuthClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
```

Apply the same constructor dependency and `clientFactory.New(...)` replacement to these account files:

```text
api_gateway/src/infrastructure/client/account/auth_client_adapter.go
api_gateway/src/infrastructure/client/account/department_client_adapter.go
api_gateway/src/infrastructure/client/account/keycloak_client_adapter.go
api_gateway/src/infrastructure/client/account/menu_display_client_adapter.go
api_gateway/src/infrastructure/client/account/module_access_client_adapter.go
api_gateway/src/infrastructure/client/account/module_client_adapter.go
api_gateway/src/infrastructure/client/account/organization_client_adapter.go
api_gateway/src/infrastructure/client/account/permission_client_adapter.go
api_gateway/src/infrastructure/client/account/role_client_adapter.go
api_gateway/src/infrastructure/client/account/subscription_client_adapter.go
api_gateway/src/infrastructure/client/account/subscription_plan_client_adapter.go
api_gateway/src/infrastructure/client/account/user_client_adapter.go
```

- [ ] **Step 2: Update CRM adapter constructors**

For each CRM adapter constructor, add `clientFactory *utils.BaseAPIClientFactory` as a second constructor dependency and replace `utils.NewBaseAPIClient(...)` with `clientFactory.New(...)`.

Example for `api_gateway/src/infrastructure/client/crm/lead_client_adapter.go`:

```go
func NewLeadClientAdapter(
	props *properties.ExternalServiceProperties,
	clientFactory *utils.BaseAPIClientFactory,
) port.ILeadClientPort {
	baseURL := fmt.Sprintf("http://%s:%s/crm", props.CrmService.Host, props.CrmService.Port)

	return &LeadClientAdapter{
		apiClient:      clientFactory.New(baseURL, props.CrmService.Timeout),
		circuitBreaker: utils.NewDefaultCircuitBreaker(),
	}
}
```

Apply the same constructor dependency and `clientFactory.New(...)` replacement to these CRM files:

```text
api_gateway/src/infrastructure/client/crm/contact_client_adapter.go
api_gateway/src/infrastructure/client/crm/customer_client_adapter.go
api_gateway/src/infrastructure/client/crm/lead_client_adapter.go
api_gateway/src/infrastructure/client/crm/opportunity_client_adapter.go
```

- [ ] **Step 3: Update PTM adapter constructors**

For each PTM adapter constructor, add `clientFactory *utils.BaseAPIClientFactory` as a second constructor dependency and replace `utils.NewBaseAPIClient(...)` with `clientFactory.New(...)`.

Example for `api_gateway/src/infrastructure/client/ptm/task_client_adapter.go`:

```go
func NewTaskClientAdapter(
	taskManagerProps *properties.ExternalServiceProperties,
	clientFactory *utils.BaseAPIClientFactory,
) port.ITaskClientPort {
	baseURL := fmt.Sprintf("http://%s:%s", taskManagerProps.PTMTask.Host, taskManagerProps.PTMTask.Port)
	apiClient := clientFactory.New(baseURL, taskManagerProps.PTMTask.Timeout)
	circuitBreaker := utils.NewDefaultCircuitBreaker()

	return &TaskClientAdapter{
		apiClient:      apiClient,
		circuitBreaker: circuitBreaker,
	}
}
```

Apply the same constructor dependency and `clientFactory.New(...)` replacement to these PTM files:

```text
api_gateway/src/infrastructure/client/ptm/availability_calendar_client_adapter.go
api_gateway/src/infrastructure/client/ptm/note_client_adapter.go
api_gateway/src/infrastructure/client/ptm/project_client_adapter.go
api_gateway/src/infrastructure/client/ptm/schedule_event_client_adapter.go
api_gateway/src/infrastructure/client/ptm/schedule_plan_client_adapter.go
api_gateway/src/infrastructure/client/ptm/schedule_task_client_adapter.go
api_gateway/src/infrastructure/client/ptm/schedule_window_client_adapter.go
api_gateway/src/infrastructure/client/ptm/task_client_adapter.go
```

- [ ] **Step 4: Verify no direct adapter construction remains**

Run from the repo root:

```bash
rg "NewBaseAPIClient\\(" api_gateway/src/infrastructure/client
```

Expected: no output.

- [ ] **Step 5: Format and compile**

Run from `api_gateway`:

```bash
go fmt ./...
go test ./src/infrastructure/client/... -run '^$' -count=1
go test ./src/cmd/... -run '^$' -count=1
```

Expected: PASS or `[no test files]` without compile errors.

- [ ] **Step 6: Commit adapter factory wiring**

```bash
git add src/infrastructure/client src/cmd/bootstrap/all.go src/kernel/utils/base_api.go
git commit -m "perf(gateway): use transport-aware base api client factory"
```

## Task 8: Compare Benchmarks and Run Verification

**Files:**
- Verify only; no code edits expected.

- [ ] **Step 1: Run focused behavior tests**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -run '^TestGenericProxyController' -count=1
```

Expected: PASS.

- [ ] **Step 2: Run utility and middleware tests**

Run from `api_gateway`:

```bash
go test ./src/kernel/properties ./src/kernel/utils ./src/ui/middleware -count=1
```

Expected: PASS.

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

Expected: PASS with no vet findings.

- [ ] **Step 5: Run benchmark comparison commands**

Run from `api_gateway`:

```bash
go test ./src/ui/controller/common -bench '^BenchmarkGenericProxyController_CRM_' -benchmem -run '^$' -count=5
go test ./src/kernel/utils -bench '^BenchmarkBaseAPIClient_' -benchmem -run '^$' -count=5
go test ./src/ui/middleware -bench '^BenchmarkRateLimitMiddleware_' -benchmem -run '^$' -count=5
go test ./src/kernel/utils -bench '^BenchmarkJWTUtils_' -benchmem -run '^$' -count=5
```

Expected: PASS with stable benchmark output. Compare the output to the baseline captured before transport wiring.

- [ ] **Step 6: Commit final verification note if benchmark numbers are documented**

If benchmark output is copied into an implementation summary document, commit that document:

```bash
git add docs/superpowers/plans/2026-07-04-api-gateway-performance.md
git commit -m "docs: record api gateway performance verification"
```

If benchmark output is reported only in the final response, do not create a documentation-only commit.

## Task 9: Decide Next Optimization From Data

**Files:**
- Verify only; no code edits expected in this task.

- [ ] **Step 1: Classify benchmark results**

Use the benchmark output from Task 8 and classify the next bottleneck:

```text
Generic proxy high ns/op or allocs/op: inspect reverse proxy transport, retry wrapping, request body buffering.
BaseAPIClient high ns/op or allocs/op: inspect JSON marshal/unmarshal, full response body reads, per-request logging.
Rate limit high ns/op or allocs/op: inspect Redis round trips with real Redis benchmark before changing algorithm.
JWT current-work benchmark much slower than single validation: plan a separate JWT single-parse optimization.
```

- [ ] **Step 2: Stop after classification**

Do not implement JWT, Redis Lua, or logging changes in this plan. Create a follow-up design or implementation plan if benchmark data shows one of those changes is worth doing.

- [ ] **Step 3: Final commit check**

Run from repo root:

```bash
git status --short
```

Expected: only intended files are modified or the tree is clean.
