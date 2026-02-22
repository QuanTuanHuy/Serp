/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package middleware

import (
	"slices"

	"github.com/gin-gonic/gin"
	"github.com/serp/pm-core/src/core/domain/constant"
	"github.com/serp/pm-core/src/kernel/utils"
	"go.uber.org/zap"
)

type RoleMiddleware struct {
	logger *zap.Logger
}

func NewRoleMiddleware(logger *zap.Logger) *RoleMiddleware {
	return &RoleMiddleware{
		logger: logger,
	}
}

func (m *RoleMiddleware) RequireRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		allRoles, err := utils.GetAllRolesFromContext(c)
		if err != nil {
			m.logger.Warn("Failed to get roles from context",
				zap.Int64("userID", userID),
				zap.Error(err))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Access denied")
			return
		}

		hasRole := false
		for _, requiredRole := range roles {
			if slices.Contains(allRoles, requiredRole) {
				hasRole = true
				break
			}
		}

		if !hasRole {
			m.logger.Warn("User does not have required role",
				zap.Int64("userID", userID),
				zap.Strings("requiredRoles", roles),
				zap.Strings("userRoles", allRoles))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		c.Next()
	}
}
