/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/ui/middleware"
	"go.uber.org/fx"
	"go.uber.org/zap"
)

func StartServer(lc fx.Lifecycle, engine *gin.Engine, appProps *properties.AppProperties, zapLogger *zap.Logger) {
	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			go func() {
				addr := fmt.Sprintf(":%d", appProps.Port)
				zapLogger.Info("Starting HTTP server",
					zap.String("address", addr),
					zap.String("service", appProps.Name))

				if err := engine.Run(addr); err != nil {
					zapLogger.Fatal("Failed to start server", zap.Error(err))
				}
			}()
			return nil
		},
		OnStop: func(ctx context.Context) error {
			zapLogger.Info("Shutting down HTTP server")
			return nil
		},
	})
}

func RegisterRoutes(engine *gin.Engine, appProps *properties.AppProperties, jwtMiddleware *middleware.JWTMiddleware, logger *zap.Logger) {
	engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "UP",
			"service": appProps.Name,
		})
	})

	// API v1 routes with JWT authentication
	apiV1 := engine.Group(fmt.Sprintf("%s/api/v1", appProps.Path))
	apiV1.Use(jwtMiddleware.AuthenticateJWT())
	{
		// Add your routes here when ready
		// Example:
		// tasks := apiV1.Group("/tasks")
		// {
		//     tasks.POST("", taskController.CreateTask)
		//     tasks.GET("", taskController.GetAllTasks)
		//     tasks.GET("/:id", taskController.GetTaskByID)
		//     tasks.PUT("/:id", taskController.UpdateTask)
		//     tasks.DELETE("/:id", taskController.DeleteTask)
		// }
	}

	logger.Info("Routes registered successfully")
}
