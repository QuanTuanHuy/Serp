/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/pm-core/src/core/service"
	client "github.com/serp/pm-core/src/infrastructure/client"
	store "github.com/serp/pm-core/src/infrastructure/store/adapter"
	"github.com/serp/pm-core/src/kernel/utils"
	"github.com/serp/pm-core/src/ui/middleware"
	"github.com/serp/pm-core/src/ui/router"
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

		// Client Adapters
		fx.Provide(client.NewRedisAdapter),
		fx.Provide(client.NewKafkaProducerAdapter),

		// Store Adapters
		fx.Provide(store.NewDBTransactionAdapter),
		fx.Provide(store.NewProjectAdapter),
		fx.Provide(store.NewProjectMemberAdapter),
		fx.Provide(store.NewWorkItemAdapter),
		fx.Provide(store.NewWorkItemAssignmentAdapter),
		fx.Provide(store.NewWorkItemDependencyAdapter),
		fx.Provide(store.NewSprintAdapter),
		fx.Provide(store.NewMilestoneAdapter),
		fx.Provide(store.NewBoardAdapter),
		fx.Provide(store.NewBoardColumnAdapter),
		fx.Provide(store.NewCommentAdapter),
		fx.Provide(store.NewActivityLogAdapter),
		fx.Provide(store.NewLabelAdapter),
		fx.Provide(store.NewWorkItemLabelAdapter),

		// Services
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewProjectMemberService),

		// Use Cases

		// Controllers

		// Router
		fx.Provide(NewRouterConfig),

		// Lifecycle
		fx.Invoke(InitializeDB),
		fx.Invoke(router.RegisterRoutes),
		fx.Invoke(StartServer),
	)
}
