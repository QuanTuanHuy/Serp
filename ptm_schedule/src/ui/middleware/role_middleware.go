/*
Author: QuanTuanHuy
Description: Part of Serp Project - Role-based Authorization Middleware
*/

package middleware

import (
	"slices"

	"github.com/gin-gonic/gin"
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

type RoleMiddleware struct {
}

func NewRoleMiddleware() *RoleMiddleware {
	return &RoleMiddleware{}
}

func (m *RoleMiddleware) RequireRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		allRoles, err := utils.GetAllRolesFromContext(c)
		if err != nil {
			log.Warn(c, "Failed to get roles from context, userID: ", userID, ", error: ", err)
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
			log.Warn(c, "User does not have required role, userID: ", userID,
				", requiredRoles: ", roles, ", userRoles: ", allRoles)
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		c.Next()
	}
}

func (m *RoleMiddleware) RequireRealmRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		realmRoles, err := utils.GetRealmRolesFromContext(c)
		if err != nil {
			log.Warn(c, "Failed to get realm roles from context, userID: ", userID, ", error: ", err)
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
			log.Warn(c, "User does not have required realm role, userID: ", userID,
				", requiredRoles: ", roles, ", userRealmRoles: ", realmRoles)
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		log.Debug(c, "Realm role check passed, userID: ", userID, ", requiredRoles: ", roles)

		c.Next()
	}
}

func (m *RoleMiddleware) RequireResourceRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		resourceRoles, err := utils.GetResourceRolesFromContext(c)
		if err != nil {
			log.Warn(c, "Failed to get resource roles from context, userID: ", userID, ", error: ", err)
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
			log.Warn(c, "User does not have required resource role, userID: ", userID,
				", requiredRoles: ", roles, ", userResourceRoles: ", resourceRoles)
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
			return
		}

		log.Debug(c, "Resource role check passed, userID: ", userID, ", requiredRoles: ", roles)

		c.Next()
	}
}

func (m *RoleMiddleware) RequireAllRoles(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		userID, _ := utils.GetUserIDFromContext(c)

		allRoles, err := utils.GetAllRolesFromContext(c)
		if err != nil {
			log.Warn(c, "Failed to get roles from context, userID: ", userID, ", error: ", err)
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Access denied")
			return
		}

		roleMap := make(map[string]bool)
		for _, userRole := range allRoles {
			roleMap[userRole] = true
		}

		for _, requiredRole := range roles {
			if !roleMap[requiredRole] {
				log.Warn(c, "User missing required role, userID: ", userID,
					", missingRole: ", requiredRole, ", requiredRoles: ", roles, ", userRoles: ", allRoles)
				utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, "Insufficient permissions")
				return
			}
		}

		log.Debug(c, "All roles check passed, userID: ", userID, ", requiredRoles: ", roles)

		c.Next()
	}
}
