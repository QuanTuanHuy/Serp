/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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

func TestRateLimitMiddleware_ApplyUserRateLimit_RouteOverrideUsesNormalizedPath(t *testing.T) {
	gin.SetMode(gin.TestMode)
	limiter := &testRateLimiter{allowed: true}
	overrideRule := properties.RateLimitRule{Limit: 3, WindowSecs: 30}
	m := NewRateLimitMiddleware(limiter, &properties.RateLimitProperties{
		Enabled:     true,
		DefaultUser: properties.RateLimitRule{Limit: 10, WindowSecs: 60},
		RouteOverrides: map[string]properties.RouteOverride{
			"protected": {
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
