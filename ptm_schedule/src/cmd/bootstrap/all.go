/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/golibs-starter/golib"
	golibdata "github.com/golibs-starter/golib-data"
	golibgin "github.com/golibs-starter/golib-gin"
	"github.com/serp/ptm-schedule/src/core/service"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/infrastructure/store/adapter"
	"github.com/serp/ptm-schedule/src/kernel/properties"
	"github.com/serp/ptm-schedule/src/kernel/utils"
	"github.com/serp/ptm-schedule/src/ui/controller"
	"github.com/serp/ptm-schedule/src/ui/middleware"
	"github.com/serp/ptm-schedule/src/ui/router"
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
		golib.ProvideProps(properties.NewKeycloakProperties),

		fx.Invoke(InitializeDB),

		// Provide adapter
		fx.Provide(adapter.NewDBTransactionAdapter),
		fx.Provide(adapter.NewSchedulePlanStoreAdapter),

		// Provide service
		fx.Provide(service.NewTransactionService),
		fx.Provide(service.NewSchedulePlanService),

		// Provide usecase
		fx.Provide(usecase.NewSchedulePlanUseCase),

		// Provide controller
		fx.Provide(controller.NewSchedulePlanController),

		// Provide JWT components
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(utils.NewJWTUtils),
		fx.Provide(middleware.NewJWTMiddleware),

		golibgin.GinHttpServerOpt(),
		fx.Invoke(router.RegisterGinRouters),
		golibgin.OnStopHttpServerOpt(),
	)
}
