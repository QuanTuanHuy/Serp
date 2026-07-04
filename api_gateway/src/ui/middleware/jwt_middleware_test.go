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
