/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/sales/src/core/service"
	"github.com/serp/sales/src/core/usecase"
	"github.com/serp/sales/src/infrastructure/client"
	"github.com/serp/sales/src/infrastructure/store/adapter"
	"github.com/serp/sales/src/ui/controller"
	"github.com/serp/sales/src/ui/middleware"
	"github.com/serp/sales/src/ui/router"
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

		// Client adapters
		fx.Provide(client.NewRedisAdapter),

		// Store adapters
		fx.Provide(adapter.NewDBTransactionAdapter),
		fx.Provide(adapter.NewQuotationStoreAdapter),

		// Services
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewQuotationService),

		// Use cases
		fx.Provide(usecase.NewQuotationUseCase),

		// Controllers
		fx.Provide(controller.NewQuotationController),

		// Middleware
		fx.Provide(middleware.NewJWTMiddleware),

		// Router
		fx.Provide(NewRouterConfig),

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(router.RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
