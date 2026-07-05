/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"errors"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/constant"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type JWTMiddleware struct {
	jwtUtils *utils.JWTUtils
}

func NewJWTMiddleware(jwtUtils *utils.JWTUtils) *JWTMiddleware {
	return &JWTMiddleware{
		jwtUtils: jwtUtils,
	}
}

// AuthenticateJWT validates JWT token and extracts user information.
func (m *JWTMiddleware) AuthenticateJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		if !m.AuthenticateJWTContext(c) {
			return
		}

		c.Next()
	}
}

// AuthenticateJWTContext validates JWT token, stores claims on Gin context,
// and returns whether the request may continue.
func (m *JWTMiddleware) AuthenticateJWTContext(c *gin.Context) bool {
	authHeader := c.GetHeader("Authorization")
	if authHeader == "" {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	const bearerPrefix = "Bearer "
	if !strings.HasPrefix(authHeader, bearerPrefix) {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	token := strings.TrimPrefix(authHeader, bearerPrefix)
	if token == "" {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
		c.Abort()
		return false
	}

	validated, err := m.jwtUtils.ValidateAccessToken(token)
	if err != nil {
		message := "Invalid or expired token"
		if errors.Is(err, utils.ErrInvalidTokenType) {
			message = "Invalid token type"
		}
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, message)
		c.Abort()
		return false
	}

	claims := validated.Claims

	// Set user information in context
	c.Set("userID", claims.UserID)
	c.Set("userEmail", claims.Email)
	c.Set("userFullName", claims.FullName)
	c.Set("preferredUsername", claims.PreferredUsername)
	c.Set("emailVerified", claims.EmailVerified)
	c.Set("token", token)
	c.Set("roles", validated.Roles)

	log.Debugc(c, "JWT authentication successful for user: ", claims.UserID, " (", claims.Email, ")")
	return true
}

// RequireRole checks if user has specific role
func (m *JWTMiddleware) RequireRole(roleName string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		if !m.jwtUtils.HasRole(tokenStr, roleName) {
			utils.AbortErrorHandle(c, constant.GeneralForbidden)
			c.Abort()
			return
		}

		c.Next()
	}
}

// RequireAnyRole checks if user has any of the specified roles
func (m *JWTMiddleware) RequireAnyRole(roleNames ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		hasAnyRole := false
		for _, roleName := range roleNames {
			if m.jwtUtils.HasRole(tokenStr, roleName) {
				hasAnyRole = true
				break
			}
		}

		if !hasAnyRole {
			utils.AbortErrorHandle(c, constant.GeneralForbidden)
			c.Abort()
			return
		}

		c.Next()
	}
}

// RequireRealmRole checks if user has specific realm role in Keycloak
func (m *JWTMiddleware) RequireRealmRole(roleName string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		if !m.jwtUtils.HasRealmRole(tokenStr, roleName) {
			utils.AbortErrorHandle(c, constant.GeneralForbidden)
			c.Abort()
			return
		}

		c.Next()
	}
}

// RequireResourceRole checks if user has specific resource role for a client
func (m *JWTMiddleware) RequireResourceRole(clientId string, roleName string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		if !m.jwtUtils.HasResourceRole(tokenStr, clientId, roleName) {
			utils.AbortErrorHandle(c, constant.GeneralForbidden)
			c.Abort()
			return
		}

		c.Next()
	}
}

// OptionalJWT extracts user information if token is present but doesn't require authentication
func (m *JWTMiddleware) OptionalJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.Next()
			return
		}

		const bearerPrefix = "Bearer "
		if !strings.HasPrefix(authHeader, bearerPrefix) {
			c.Next()
			return
		}

		token := strings.TrimPrefix(authHeader, bearerPrefix)
		if token == "" {
			c.Next()
			return
		}

		claims, err := m.jwtUtils.ValidateToken(token)
		if err != nil {
			log.Warn(c, "Optional JWT validation failed: ", err)
			c.Next()
			return
		}

		c.Set("userID", claims.UserID)
		c.Set("userEmail", claims.Email)
		c.Set("userFullName", claims.FullName)
		c.Set("preferredUsername", claims.PreferredUsername)
		c.Set("emailVerified", claims.EmailVerified)
		c.Set("token", token)
		c.Set("authenticated", true)

		if subject, err := m.jwtUtils.GetSubjectFromToken(token); err == nil {
			c.Set("subject", subject)
		}

		if roles, err := m.jwtUtils.ExtractRoles(token); err == nil {
			c.Set("roles", roles)
		}

		c.Next()
	}
}
