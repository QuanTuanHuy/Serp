/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"go.uber.org/zap"
)

type RouterConfig struct {
	AppProps          *properties.AppProperties
	Engine            *gin.Engine
	ProjectController *controller.ProjectController
	JWTMiddleware     *middleware.JWTMiddleware
	RoleMiddleware    *middleware.RoleMiddleware
	Logger            *zap.Logger
}

func RegisterRoutes(config *RouterConfig) {
	config.Engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": config.AppProps.Name,
		})
	})

	apiV1 := config.Engine.Group(fmt.Sprintf("%s/api/v1", config.AppProps.Path))
	apiV1.Use(config.JWTMiddleware.AuthenticateJWT())
	{
		projects := apiV1.Group("/projects")
		{
			projects.POST("", config.ProjectController.CreateProject)
			projects.GET("", config.ProjectController.GetAllProjects)
			projects.GET("/:id", config.ProjectController.GetProjectByID)
			projects.PATCH("/:id", config.ProjectController.UpdateProject)
			projects.DELETE("/:id", config.ProjectController.DeleteProject)
		}
	}

	config.Logger.Info("Routes registered successfully")
}
