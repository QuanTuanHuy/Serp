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
