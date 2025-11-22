/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"fmt"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/kernel/utils"
	"go.uber.org/zap"
)

type JWTMiddleware struct {
	keycloakProps *properties.KeycloakProperties
	jwksUtils     *utils.KeycloakJwksUtils
	logger        *zap.Logger
}

func NewJWTMiddleware(
	appProps *properties.AppProperties,
	jwksUtils *utils.KeycloakJwksUtils,
	logger *zap.Logger) *JWTMiddleware {
	return &JWTMiddleware{
		keycloakProps: &appProps.Keycloak,
		jwksUtils:     jwksUtils,
		logger:        logger,
	}
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

func (m *JWTMiddleware) AuthenticateJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing authorization header")
			c.Abort()
			return
		}
		const bearerPrefix = "Bearer "
		if !strings.HasPrefix(authHeader, bearerPrefix) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid authorization header format")
			c.Abort()
			return
		}
		tokenString := strings.TrimPrefix(authHeader, bearerPrefix)
		if tokenString == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing token")
			c.Abort()
			return
		}

		token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (any, error) {
			if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
			}
			kid, ok := token.Header["kid"].(string)
			if !ok {
				return nil, fmt.Errorf("missing or invalid kid in token header")
			}

			publicKey, err := m.jwksUtils.GetPublicKey(kid)
			if err != nil {
				return nil, fmt.Errorf("failed to get public key: %w", err)
			}

			return publicKey, nil
		})

		if err != nil {
			m.logger.Error("Failed to parse and verify JWT", zap.Error(err))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid or expired token")
			c.Abort()
			return
		}

		if !token.Valid {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token")
			c.Abort()
			return
		}

		claims, ok := token.Claims.(*Claims)
		if !ok {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token claims")
			c.Abort()
			return
		}

		if claims.ExpiresAt != nil && claims.ExpiresAt.Time.Before(time.Now()) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Token has expired")
			c.Abort()
			return
		}

		expectedIss := m.keycloakProps.ExpectedIssuer
		if expectedIss != "" && claims.Issuer != expectedIss {
			m.logger.Error("Token issuer mismatch",
				zap.String("expected", expectedIss),
				zap.String("actual", claims.Issuer))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token issuer")
			c.Abort()
			return
		}

		realmRoles := m.extractRealmRoles(claims)
		resourceRoles := m.extractResourceRoles(claims)
		allRoles := m.mergeRoles(realmRoles, resourceRoles)

		// Set user information in context
		c.Set("userID", claims.UserID)
		c.Set("tenantID", claims.TenantID)
		c.Set("userEmail", claims.Email)
		c.Set("userFullName", claims.FullName)
		c.Set("token", tokenString)
		c.Set("authenticated", true)
		c.Set("realmRoles", realmRoles)
		c.Set("resourceRoles", resourceRoles)
		c.Set("allRoles", allRoles)

		m.logger.Debug("JWT authentication successful",
			zap.Int64("userID", claims.UserID),
			zap.Int64("tenantID", claims.TenantID),
			zap.String("email", claims.Email))

		c.Next()
	}
}

func (m *JWTMiddleware) extractRealmRoles(claims *Claims) []string {
	roles := []string{}
	if claims.RealmAccess != nil {
		if rolesInterface, ok := claims.RealmAccess["roles"]; ok {
			if rolesList, ok := rolesInterface.([]any); ok {
				for _, role := range rolesList {
					if roleStr, ok := role.(string); ok {
						roles = append(roles, roleStr)
					}
				}
			}
		}
	}
	return roles
}

func (m *JWTMiddleware) extractResourceRoles(claims *Claims) []string {
	roles := []string{}
	if claims.ResourceAccess != nil {
		for _, clientAccess := range claims.ResourceAccess {
			if rolesInterface, ok := clientAccess["roles"]; ok {
				if rolesList, ok := rolesInterface.([]any); ok {
					for _, role := range rolesList {
						if roleStr, ok := role.(string); ok {
							roles = append(roles, roleStr)
						}
					}
				}
			}
		}
	}
	return roles
}

// func (m *JWTMiddleware) extractResourceRolesForClient(claims *Claims, clientID string) []string {
// 	roles := []string{}
// 	if claims.ResourceAccess != nil {
// 		if clientAccess, ok := claims.ResourceAccess[clientID]; ok {
// 			if rolesInterface, ok := clientAccess["roles"]; ok {
// 				if rolesList, ok := rolesInterface.([]any); ok {
// 					for _, role := range rolesList {
// 						if roleStr, ok := role.(string); ok {
// 							roles = append(roles, roleStr)
// 						}
// 					}
// 				}
// 			}
// 		}
// 	}
// 	return roles
// }

func (m *JWTMiddleware) mergeRoles(realmRoles, resourceRoles []string) []string {
	roleMap := make(map[string]bool)
	for _, role := range realmRoles {
		roleMap[role] = true
	}
	for _, role := range resourceRoles {
		roleMap[role] = true
	}

	allRoles := make([]string, 0, len(roleMap))
	for role := range roleMap {
		allRoles = append(allRoles, role)
	}
	return allRoles
}
