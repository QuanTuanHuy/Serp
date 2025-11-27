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
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"github.com/serp/ptm-task/src/ui/router"
	"go.uber.org/fx"
	"go.uber.org/zap"
)

func NewRouterConfig(
	appProps *properties.AppProperties,
	engine *gin.Engine,
	projectController *controller.ProjectController,
	taskController *controller.TaskController,
	noteController *controller.NoteController,
	jwtMiddleware *middleware.JWTMiddleware,
	roleMiddleware *middleware.RoleMiddleware,
	logger *zap.Logger,
) *router.RouterConfig {
	return &router.RouterConfig{
		AppProps:          appProps,
		Engine:            engine,
		ProjectController: projectController,
		TaskController:    taskController,
		NoteController:    noteController,
		JWTMiddleware:     jwtMiddleware,
		RoleMiddleware:    roleMiddleware,
		Logger:            logger,
	}
}

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
