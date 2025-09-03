/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"github.com/gin-gonic/gin"
)

func GetUserIDFromContext(c *gin.Context) (int64, bool) {
	if userID, exists := c.Get("userID"); exists {
		if id, ok := userID.(int64); ok {
			return id, true
		}
	}
	return 0, false
}

func GetUserEmailFromContext(c *gin.Context) (string, bool) {
	if email, exists := c.Get("userEmail"); exists {
		if emailStr, ok := email.(string); ok {
			return emailStr, true
		}
	}
	return "", false
}

func GetUserFullNameFromContext(c *gin.Context) (string, bool) {
	if fullName, exists := c.Get("userFullName"); exists {
		if nameStr, ok := fullName.(string); ok {
			return nameStr, true
		}
	}
	return "", false
}

func GetTokenFromContext(c *gin.Context) (string, bool) {
	if token, exists := c.Get("token"); exists {
		if tokenStr, ok := token.(string); ok {
			return tokenStr, true
		}
	}
	return "", false
}

func GetRolesFromContext(c *gin.Context) ([]string, bool) {
	if roles, exists := c.Get("roles"); exists {
		if roleSlice, ok := roles.([]string); ok {
			return roleSlice, true
		}
	}
	return nil, false
}

func GetPermissionsFromContext(c *gin.Context) ([]string, bool) {
	if permissions, exists := c.Get("permissions"); exists {
		if permSlice, ok := permissions.([]string); ok {
			return permSlice, true
		}
	}
	return nil, false
}

func GetAuthoritiesFromContext(c *gin.Context) ([]string, bool) {
	if authorities, exists := c.Get("authorities"); exists {
		if authSlice, ok := authorities.([]string); ok {
			return authSlice, true
		}
	}
	return nil, false
}

func IsAuthenticated(c *gin.Context) bool {
	if authenticated, exists := c.Get("authenticated"); exists {
		if auth, ok := authenticated.(bool); ok {
			return auth
		}
	}
	// If authenticated flag is not set, check if userID exists
	_, exists := GetUserIDFromContext(c)
	return exists
}

func HasRole(c *gin.Context, roleName string) bool {
	roles, exists := GetRolesFromContext(c)
	if !exists {
		return false
	}

	for _, role := range roles {
		if role == roleName {
			return true
		}
	}
	return false
}

func HasPermission(c *gin.Context, permissionName string) bool {
	permissions, exists := GetPermissionsFromContext(c)
	if !exists {
		return false
	}

	for _, permission := range permissions {
		if permission == permissionName {
			return true
		}
	}
	return false
}

func HasAnyRole(c *gin.Context, roleNames ...string) bool {
	for _, roleName := range roleNames {
		if HasRole(c, roleName) {
			return true
		}
	}
	return false
}

func HasAnyPermission(c *gin.Context, permissionNames ...string) bool {
	for _, permissionName := range permissionNames {
		if HasPermission(c, permissionName) {
			return true
		}
	}
	return false
}
