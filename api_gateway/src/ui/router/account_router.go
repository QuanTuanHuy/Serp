/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	account "github.com/serp/api-gateway/src/ui/controller/account"
)

func RegisterAccountRoutes(group *gin.RouterGroup, authController *account.AuthController) {
	authV1 := group.Group("/api/v1/auth")
	{
		authV1.POST("/register", authController.Register)
		authV1.POST("/login", authController.Login)
	}
}
