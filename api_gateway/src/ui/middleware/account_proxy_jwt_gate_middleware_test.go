/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

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
