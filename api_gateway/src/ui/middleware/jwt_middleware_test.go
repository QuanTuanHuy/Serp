/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

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
