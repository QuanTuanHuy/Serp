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
	keyCloakController *account.KeycloakController) {
	authV1 := group.Group("/api/v1/auth")
	{
		authV1.POST("/register", authController.Register)
		authV1.POST("/login", authController.Login)
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
	}
}
