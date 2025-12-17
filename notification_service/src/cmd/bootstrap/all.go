/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/notification-service/src/core/service"
	"github.com/serp/notification-service/src/core/usecase"
	client "github.com/serp/notification-service/src/infrastructure/client"
	store "github.com/serp/notification-service/src/infrastructure/store/adapter"
	"github.com/serp/notification-service/src/kernel/utils"
	"github.com/serp/notification-service/src/ui/controller.go"
	"github.com/serp/notification-service/src/ui/middleware"
	"github.com/serp/notification-service/src/ui/router"
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
		fx.Provide(middleware.NewRoleMiddleware),

		// Utils
		fx.Provide(utils.NewKeycloakJwksUtils),

		// Adapter
		fx.Provide(client.NewRedisAdapter),
		fx.Provide(client.NewKafkaProducerAdapter),

		fx.Provide(store.NewDBTransactionAdapter),

		// Services
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewPreferenceService),

		// Use cases
		fx.Provide(usecase.NewPreferenceUseCase),

		// Controllers
		fx.Provide(controller.NewPreferenceController),

		// Router
		fx.Provide(NewRouterConfig),

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(router.RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
