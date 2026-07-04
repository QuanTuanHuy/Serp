/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	resp, err := http.Post(gateway.URL+"/api/v1/auth/login", "application/json", nil)
	if err != nil {
		t.Fatalf("post login: %v", err)
	}
	_, _ = io.ReadAll(resp.Body)
	resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, resp.StatusCode)
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
	gateway := httptest.NewServer(r)
	defer gateway.Close()

	resp, err := http.Get(gateway.URL + "/api/v1/users/profile/me")
	if err != nil {
		t.Fatalf("get protected route: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusUnauthorized {
		body, _ := io.ReadAll(resp.Body)
		t.Fatalf("expected status %d, got %d body=%s", http.StatusUnauthorized, resp.StatusCode, string(body))
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
