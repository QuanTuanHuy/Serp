/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/golibs-starter/golib"
	golibdata "github.com/golibs-starter/golib-data"
	golibgin "github.com/golibs-starter/golib-gin"
	"github.com/serp/ptm-task/src/core/service"
	"github.com/serp/ptm-task/src/core/usecase"
	adapter2 "github.com/serp/ptm-task/src/infrastructure/client"
	"github.com/serp/ptm-task/src/infrastructure/store/adapter"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/kernel/utils"
	"github.com/serp/ptm-task/src/ui/controller"
	"github.com/serp/ptm-task/src/ui/middleware"
	"github.com/serp/ptm-task/src/ui/router"
	"go.uber.org/fx"
)

func All() fx.Option {
	return fx.Options(
		golib.AppOpt(),
		golib.PropertiesOpt(),
		golib.LoggingOpt(),
		golib.EventOpt(),
		golib.BuildInfoOpt(Version, CommitHash, BuildTime),
		golib.ActuatorEndpointOpt(),
		golib.HttpClientOpt(),

		// Provide datasource auto properties
		golibdata.RedisOpt(),
		golibdata.DatasourceOpt(),

		// Provide properties
		golib.ProvideProps(properties.NewJwtProperties),
		golib.ProvideProps(properties.NewKeycloakProperties),
		golib.ProvideProps(properties.NewKafkaProducerProperties),

		fx.Invoke(InitializeDB),

		// Provide adapter
		fx.Provide(adapter2.NewRedisAdapter),
		fx.Provide(adapter2.NewKafkaProducerAdapter),
		fx.Provide(adapter.NewDBTransactionAdapter),
		fx.Provide(adapter.NewProjectStoreAdapter),
		fx.Provide(adapter.NewGroupTaskStoreAdapter),
		fx.Provide(adapter.NewTaskStoreAdapter),
		fx.Provide(adapter.NewCommentStoreAdapter),
		fx.Provide(adapter.NewNoteStoreAdapter),

		// Provide service
		fx.Provide(service.NewProjectService),
		fx.Provide(service.NewGroupTaskService),
		fx.Provide(service.NewTaskService),
		fx.Provide(service.NewCommentService),
		fx.Provide(service.NewNoteService),

		// Provide usecase
		fx.Provide(usecase.NewProjectUseCase),
		fx.Provide(usecase.NewGroupTaskUseCase),
		fx.Provide(usecase.NewTaskUseCase),
		fx.Provide(usecase.NewCommentUseCase),
		fx.Provide(usecase.NewNoteUseCase),

		// Provide controller
		fx.Provide(controller.NewProjectController),
		fx.Provide(controller.NewGroupTaskController),
		fx.Provide(controller.NewTaskController),
		fx.Provide(controller.NewCommentController),
		fx.Provide(controller.NewNoteController),

		// Provide JWT components
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(utils.NewJWTUtils),
		fx.Provide(middleware.NewJWTMiddleware),

		golibgin.GinHttpServerOpt(),
		fx.Invoke(router.RegisterGinRouters),
		golibgin.OnStopHttpServerOpt(),
	)
}
