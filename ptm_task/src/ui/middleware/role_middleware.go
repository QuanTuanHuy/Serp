/*
Author: QuanTuanHuy
Description: Part of Serp Project - Role-based Authorization Middleware
*/

package middleware

import (
	"slices"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/kernel/utils"
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
			}
			if hasRole {
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

		// m.logger.Debug("Role check passed",
		// 	zap.Int64("userID", userID),
		// 	zap.Strings("requiredRoles", roles))

		c.Next()
	}
}

func (m *RoleMiddleware) RequireRealmRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		realmRoles, err := utils.GetRealmRolesFromContext(c)
		if err != nil {
			m.logger.Warn("Failed to get realm roles from context",
				zap.Int64("userID", userID),
				zap.Error(err))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Access denied")
			return
		}

		hasRole := false
		for _, requiredRole := range roles {
			if slices.Contains(realmRoles, requiredRole) {
				hasRole = true
			}
			if hasRole {
				break
			}
		}

		if !hasRole {
			m.logger.Warn("User does not have required realm role",
				zap.Int64("userID", userID),
				zap.Strings("requiredRoles", roles),
				zap.Strings("userRealmRoles", realmRoles))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		m.logger.Debug("Realm role check passed",
			zap.Int64("userID", userID),
			zap.Strings("requiredRoles", roles))

		c.Next()
	}
}

func (m *RoleMiddleware) RequireResourceRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		resourceRoles, err := utils.GetResourceRolesFromContext(c)
		if err != nil {
			m.logger.Warn("Failed to get resource roles from context",
				zap.Int64("userID", userID),
				zap.Error(err))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Access denied")
			return
		}

		hasRole := false
		for _, requiredRole := range roles {
			if slices.Contains(resourceRoles, requiredRole) {
				hasRole = true
			}
			if hasRole {
				break
			}
		}

		if !hasRole {
			m.logger.Warn("User does not have required resource role",
				zap.Int64("userID", userID),
				zap.Strings("requiredRoles", roles),
				zap.Strings("userResourceRoles", resourceRoles))
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		m.logger.Debug("Resource role check passed",
			zap.Int64("userID", userID),
			zap.Strings("requiredRoles", roles))

		c.Next()
	}
}

func (m *RoleMiddleware) RequireAllRoles(roles ...string) gin.HandlerFunc {
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

		roleMap := make(map[string]bool)
		for _, userRole := range allRoles {
			roleMap[userRole] = true
		}

		for _, requiredRole := range roles {
			if !roleMap[requiredRole] {
				m.logger.Warn("User missing required role",
					zap.Int64("userID", userID),
					zap.String("missingRole", requiredRole),
					zap.Strings("requiredRoles", roles),
					zap.Strings("userRoles", allRoles))
				utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
				return
			}
		}

		m.logger.Debug("All roles check passed",
			zap.Int64("userID", userID),
			zap.Strings("requiredRoles", roles))

		c.Next()
	}
}
