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

func GetTenantIDFromContext(c *gin.Context) (int64, bool) {
	if tenantID, exists := c.Get("tenantID"); exists {
		if id, ok := tenantID.(int64); ok {
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

func GetTokenFromContext(c *gin.Context) (string, bool) {
	if token, exists := c.Get("token"); exists {
		if tokenStr, ok := token.(string); ok {
			return tokenStr, true
		}
	}
	return "", false
}

func IsAuthenticated(c *gin.Context) bool {
	if authenticated, exists := c.Get("authenticated"); exists {
		if auth, ok := authenticated.(bool); ok {
			return auth
		}
	}
	_, exists := GetUserIDFromContext(c)
	return exists
}
