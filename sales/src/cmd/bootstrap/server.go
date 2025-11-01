/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/serp/sales/src/kernel/properties"
	"github.com/serp/sales/src/ui/controller"
	"github.com/serp/sales/src/ui/middleware"
	"github.com/serp/sales/src/ui/router"
	"go.uber.org/fx"
	"go.uber.org/zap"
)

func NewRouterConfig(
	appProps *properties.AppProperties,
	engine *gin.Engine,
	quotationController *controller.QuotationController,
	jwtMiddleware *middleware.JWTMiddleware,
	logger *zap.Logger,
) *router.RouterConfig {
	return &router.RouterConfig{
		AppProps:            appProps,
		Engine:              engine,
		QuotationController: quotationController,
		JWTMiddleware:       jwtMiddleware,
		Logger:              logger,
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
