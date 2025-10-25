/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	account "github.com/serp/api-gateway/src/ui/controller/account"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterAccountRoutes(group *gin.RouterGroup,
	authController *account.AuthController,
	userController *account.UserController,
	keyCloakController *account.KeycloakController,
	roleController *account.RoleController,
	permissionController *account.PermissionController,
	moduleController *account.ModuleController,
	subscriptionController *account.SubscriptionController,
	subscriptionPlanController *account.SubscriptionPlanController,
	moduleAccessController *account.ModuleAccessController,
	menuDisplayController *account.MenuDisplayController,
	organizationController *account.OrganizationController) {
	authV1 := group.Group("/api/v1/auth")
	{
		authV1.POST("/register", authController.Register)
		authV1.POST("/login", authController.Login)
		authV1.POST("/get-token", authController.GetToken)
		authV1.POST("/refresh-token", authController.RefreshToken)
		authV1.POST("/revoke-token", authController.RevokeToken)
	}

	keycloakV1 := group.Group("/api/v1/keycloak")
	{
		keycloakV1.GET("/clients/:clientId/client-secret", keyCloakController.GetKeycloakClientSecret)
	}

	userV1 := group.Group("/api/v1/users")
	{
		userV1.Use(middleware.AuthMiddleware()).GET("/profile/me", userController.GetMyProfile)
		userV1.Use(middleware.AuthMiddleware()).GET("", userController.GetUsers)
		userV1.Use(middleware.AuthMiddleware()).POST("/assign-roles", userController.AssignRolesToUser)
	}

	roleV1 := group.Group("/api/v1/roles")
	{
		roleV1.Use(middleware.AuthMiddleware()).POST("", roleController.CreateRole)
		roleV1.Use(middleware.AuthMiddleware()).GET("", roleController.GetAllRoles)
		roleV1.Use(middleware.AuthMiddleware()).POST("/:roleId/permissions", roleController.AddPermissionsToRole)
		roleV1.Use(middleware.AuthMiddleware()).PATCH("/:roleId", roleController.UpdateRole)
	}

	permissionV1 := group.Group("/api/v1/permissions")
	{
		permissionV1.Use(middleware.AuthMiddleware()).POST("", permissionController.CreatePermission)
		permissionV1.Use(middleware.AuthMiddleware()).GET("", permissionController.GetAllPermissions)
	}

	moduleV1 := group.Group("/api/v1/modules")
	{
		moduleV1.Use(middleware.AuthMiddleware()).POST("", moduleController.CreateModule)
		moduleV1.Use(middleware.AuthMiddleware()).GET("/:moduleId", moduleController.GetModuleById)
		moduleV1.Use(middleware.AuthMiddleware()).PUT("/:moduleId", moduleController.UpdateModule)
		moduleV1.Use(middleware.AuthMiddleware()).GET("", moduleController.GetAllModules)
		moduleV1.Use(middleware.AuthMiddleware()).POST("/:moduleId/registration", moduleController.UserRegisterModule)
		moduleV1.Use(middleware.AuthMiddleware()).GET("/my-modules", moduleController.GetMyModules)
	}

	adminSubscriptionV1 := group.Group("/api/v1/admin/subscriptions")
	{
		adminSubscriptionV1.Use(middleware.AuthMiddleware()).GET("", subscriptionController.GetAllSubscriptions)
		adminSubscriptionV1.Use(middleware.AuthMiddleware()).PUT("/:subscriptionId/activate", subscriptionController.ActivateSubscription)
		adminSubscriptionV1.Use(middleware.AuthMiddleware()).PUT("/:subscriptionId/reject", subscriptionController.RejectSubscription)
		adminSubscriptionV1.Use(middleware.AuthMiddleware()).PUT("/:subscriptionId/expire", subscriptionController.ExpireSubscription)
	}

	subscriptionV1 := group.Group("/api/v1/subscriptions")
	{
		subscriptionV1.Use(middleware.AuthMiddleware()).POST("/subscribe", subscriptionController.Subscribe)
		subscriptionV1.Use(middleware.AuthMiddleware()).POST("/trial", subscriptionController.StartTrial)
		subscriptionV1.Use(middleware.AuthMiddleware()).PUT("/upgrade", subscriptionController.UpgradeSubscription)
		subscriptionV1.Use(middleware.AuthMiddleware()).PUT("/downgrade", subscriptionController.DowngradeSubscription)
		subscriptionV1.Use(middleware.AuthMiddleware()).PUT("/cancel", subscriptionController.CancelSubscription)
		subscriptionV1.Use(middleware.AuthMiddleware()).PUT("/renew", subscriptionController.RenewSubscription)
		subscriptionV1.Use(middleware.AuthMiddleware()).PUT("/:subscriptionId/extend-trial", subscriptionController.ExtendTrial)
		subscriptionV1.Use(middleware.AuthMiddleware()).GET("/me/active", subscriptionController.GetActiveSubscription)
		subscriptionV1.Use(middleware.AuthMiddleware()).GET("/:subscriptionId", subscriptionController.GetSubscriptionById)
		subscriptionV1.Use(middleware.AuthMiddleware()).GET("/me/history", subscriptionController.GetSubscriptionHistory)
	}

	subscriptionPlanV1 := group.Group("/api/v1/subscription-plans")
	{
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).POST("", subscriptionPlanController.CreatePlan)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).PUT("/:planId", subscriptionPlanController.UpdatePlan)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).DELETE("/:planId", subscriptionPlanController.DeletePlan)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).GET("/:planId", subscriptionPlanController.GetPlanById)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).GET("/code/:planCode", subscriptionPlanController.GetPlanByCode)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).GET("", subscriptionPlanController.GetAllPlans)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).POST("/:planId/modules", subscriptionPlanController.AddModuleToPlan)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).DELETE("/:planId/modules/:moduleId", subscriptionPlanController.RemoveModuleFromPlan)
		subscriptionPlanV1.Use(middleware.AuthMiddleware()).GET("/:planId/modules", subscriptionPlanController.GetPlanModules)
	}

	adminOrganizationsV1 := group.Group("/api/v1/admin/organizations")
	{
		adminOrganizationsV1.Use(middleware.AuthMiddleware()).GET("", organizationController.GetOrganizations)
		adminOrganizationsV1.Use(middleware.AuthMiddleware()).GET("/:organizationId", organizationController.GetOrganizationById)
	}

	organizationsV1 := group.Group("/api/v1/organizations")
	{
		organizationsV1.Use(middleware.AuthMiddleware()).GET("/me", organizationController.GetMyOrganization)
		organizationsV1.Use(middleware.AuthMiddleware()).POST("/:organizationId/users", organizationController.CreateUserForOrganization)
		organizationsV1.Use(middleware.AuthMiddleware()).GET("/:organizationId/modules/:moduleId/access", moduleAccessController.CanOrganizationAccessModule)
		organizationsV1.Use(middleware.AuthMiddleware()).GET("/:organizationId/modules", moduleAccessController.GetAccessibleModulesForOrganization)
		organizationsV1.Use(middleware.AuthMiddleware()).POST("/:organizationId/modules/:moduleId/users", moduleAccessController.AssignUserToModule)
		organizationsV1.Use(middleware.AuthMiddleware()).POST("/:organizationId/modules/:moduleId/users/bulk", moduleAccessController.BulkAssignUsersToModule)
		organizationsV1.Use(middleware.AuthMiddleware()).DELETE("/:organizationId/modules/:moduleId/users/:userId", moduleAccessController.RevokeUserAccessToModule)
		organizationsV1.Use(middleware.AuthMiddleware()).GET("/:organizationId/modules/:moduleId/users", moduleAccessController.GetUsersWithAccessToModule)
		organizationsV1.Use(middleware.AuthMiddleware()).GET("/:organizationId/users/me/modules", moduleAccessController.GetModulesAccessibleByUser)
	}

	menuDisplayV1 := group.Group("/api/v1/menu-displays")
	{
		menuDisplayV1.Use(middleware.AuthMiddleware()).POST("", menuDisplayController.CreateMenuDisplay)
		menuDisplayV1.Use(middleware.AuthMiddleware()).PUT("/:id", menuDisplayController.UpdateMenuDisplay)
		menuDisplayV1.Use(middleware.AuthMiddleware()).DELETE("/:id", menuDisplayController.DeleteMenuDisplay)
		menuDisplayV1.Use(middleware.AuthMiddleware()).GET("/get-by-module/:moduleId", menuDisplayController.GetMenuDisplaysByModuleId)
		menuDisplayV1.Use(middleware.AuthMiddleware()).POST("/assign-to-role", menuDisplayController.AssignMenuDisplaysToRole)
		menuDisplayV1.Use(middleware.AuthMiddleware()).POST("/unassign-from-role", menuDisplayController.UnassignMenuDisplaysFromRole)
		menuDisplayV1.Use(middleware.AuthMiddleware()).GET("/get-by-role-ids", menuDisplayController.GetMenuDisplaysByRoleIds)
	}
}
