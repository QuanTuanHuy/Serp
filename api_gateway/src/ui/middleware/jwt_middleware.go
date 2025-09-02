/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
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

// AuthenticateJWT validates JWT token and extracts user information
func (m *JWTMiddleware) AuthenticateJWT() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
			c.Abort()
			return
		}

		const bearerPrefix = "Bearer "
		if !strings.HasPrefix(authHeader, bearerPrefix) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
			c.Abort()
			return
		}

		token := strings.TrimPrefix(authHeader, bearerPrefix)
		if token == "" {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Missing or invalid authorization header")
			c.Abort()
			return
		}

		claims, err := m.jwtUtils.ValidateToken(token)
		if err != nil {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid or expired token")
			c.Abort()
			return
		}

		if !m.jwtUtils.IsAccessToken(token) {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Invalid token type")
			c.Abort()
			return
		}

		// Set user information in context
		c.Set("userID", claims.UserID)
		c.Set("userEmail", claims.Email)
		c.Set("userFullName", claims.FullName)
		c.Set("token", token)

		roles, err := m.jwtUtils.ExtractRoles(token)
		if err != nil {
			log.Warn(c, "Failed to extract roles: ", err)
			roles = []string{}
		}
		c.Set("roles", roles)

		// Extract and set permissions
		permissions, err := m.jwtUtils.ExtractPermissions(token)
		if err != nil {
			log.Warn(c, "Failed to extract permissions: ", err)
			permissions = []string{}
		}
		c.Set("permissions", permissions)

		log.Info(c, "JWT authentication successful for user: ", claims.UserID)
		c.Next()
	}
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

// RequirePermission checks if user has specific permission
func (m *JWTMiddleware) RequirePermission(permissionName string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
			c.Abort()
			return
		}

		if !m.jwtUtils.HasPermission(tokenStr, permissionName) {
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

// RequireAnyPermission checks if user has any of the specified permissions
func (m *JWTMiddleware) RequireAnyPermission(permissionNames ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		token, exists := c.Get("token")
		if !exists {
			utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
			c.Abort()
			return
		}

		tokenStr, ok := token.(string)
		if !ok {
			utils.AbortErrorHandle(c, constant.GeneralInternalServerError)
			c.Abort()
			return
		}

		hasAnyPermission := false
		for _, permissionName := range permissionNames {
			if m.jwtUtils.HasPermission(tokenStr, permissionName) {
				hasAnyPermission = true
				break
			}
		}

		if !hasAnyPermission {
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
		c.Set("token", token)
		c.Set("authenticated", true)

		if roles, err := m.jwtUtils.ExtractRoles(token); err == nil {
			c.Set("roles", roles)
		}

		if permissions, err := m.jwtUtils.ExtractPermissions(token); err == nil {
			c.Set("permissions", permissions)
		}

		c.Next()
	}
}
