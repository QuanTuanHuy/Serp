/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/notification-service/src/kernel/properties"
	"go.uber.org/zap"
)

type JWTUtils struct {
	keycloakProps *properties.KeycloakProperties
	jwksUtils     *KeycloakJwksUtils
	logger        *zap.Logger
}

type Claims struct {
	UserID         int64                     `json:"uid"`
	TenantID       int64                     `json:"tid"`
	Email          string                    `json:"email"`
	FullName       string                    `json:"name"`
	RealmAccess    map[string]any            `json:"realm_access"`
	ResourceAccess map[string]map[string]any `json:"resource_access"`
	jwt.RegisteredClaims
}

// ValidateToken validates the JWT token string and set user info in the context
func (j *JWTUtils) ValidateToken(c *gin.Context, tokenString string) (bool, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		kid, ok := token.Header["kid"].(string)
		if !ok {
			return nil, fmt.Errorf("missing or invalid kid in token header")
		}

		publicKey, err := j.jwksUtils.GetPublicKey(kid)
		if err != nil {
			return nil, fmt.Errorf("failed to get public key: %w", err)
		}

		return publicKey, nil
	})

	if err != nil {
		return false, err
	}
	claims, ok := token.Claims.(*Claims)
	if !ok || !token.Valid {
		return false, fmt.Errorf("invalid token claims")
	}

	c.Set("userID", claims.UserID)
	c.Set("tenantID", claims.TenantID)
	c.Set("userEmail", claims.Email)
	c.Set("fullName", claims.FullName)
	c.Set("token", tokenString)
	return true, nil
}

func NewJWTUtils(
	appProps *properties.AppProperties,
	jwksUtils *KeycloakJwksUtils,
	logger *zap.Logger) *JWTUtils {
	return &JWTUtils{
		keycloakProps: &appProps.Keycloak,
		jwksUtils:     jwksUtils,
		logger:        logger,
	}
}
