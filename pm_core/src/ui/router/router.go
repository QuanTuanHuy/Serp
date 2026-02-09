/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/pm-core/src/core/domain/enum"
	"github.com/serp/pm-core/src/kernel/properties"
	"github.com/serp/pm-core/src/ui/middleware"
	"go.uber.org/zap"
)

type RouterConfig struct {
	AppProps       *properties.AppProperties
	Engine         *gin.Engine
	JWTMiddleware  *middleware.JWTMiddleware
	RoleMiddleware *middleware.RoleMiddleware
	Logger         *zap.Logger
}

func RegisterRoutes(config *RouterConfig) {
	config.Engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": config.AppProps.Name,
		})
	})

	apiV1 := config.Engine.Group(fmt.Sprintf("%s/api/v1", config.AppProps.Path))
	apiV1.Use(config.JWTMiddleware.AuthenticateJWT(), config.RoleMiddleware.RequireRole(string(enum.RoleUser), string(enum.RolePMAdmin)))
	{
	}

	config.Logger.Info("Routes registered successfully")
}
