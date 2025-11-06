/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/ptm-task/src/ui/middleware"
	"go.uber.org/fx"
)

func All() fx.Option {
	return fx.Options(
		// Core infrastructure
		fx.Provide(NewLogger),
		fx.Provide(NewConfig),
		fx.Provide(NewAppProperties),
		fx.Provide(NewDatabase),
		fx.Provide(NewRedisClient),
		fx.Provide(NewGinEngine),

		// Middleware
		fx.Provide(middleware.NewJWTMiddleware),

		// Add your services, use cases, controllers here when ready

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
