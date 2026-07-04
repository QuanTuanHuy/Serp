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
