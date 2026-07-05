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
