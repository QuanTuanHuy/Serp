/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/ptm-task/src/core/service"
	"github.com/serp/ptm-task/src/core/usecase"
	client "github.com/serp/ptm-task/src/infrastructure/client"
	store "github.com/serp/ptm-task/src/infrastructure/store/adapter"
	"github.com/serp/ptm-task/src/kernel/utils"
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"github.com/serp/ptm-task/src/ui/router"
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
		fx.Provide(store.NewProjectAdapter),
		fx.Provide(store.NewTaskAdapter),
		fx.Provide(store.NewTaskTemplateAdapter),
		fx.Provide(store.NewNoteAdapter),

		// Services
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewProjectService),
		fx.Provide(service.NewTaskService),
		fx.Provide(service.NewTaskTemplateService),
		fx.Provide(service.NewNoteService),

		// Use cases
		fx.Provide(usecase.NewProjectUseCase),
		fx.Provide(usecase.NewTaskUseCase),
		fx.Provide(usecase.NewNoteUseCase),

		// Controllers
		fx.Provide(controller.NewProjectController),
		fx.Provide(controller.NewTaskController),
		fx.Provide(controller.NewNoteController),

		// Router
		fx.Provide(NewRouterConfig),

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(router.RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
