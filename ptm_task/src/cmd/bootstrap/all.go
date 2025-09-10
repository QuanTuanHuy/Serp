package bootstrap

import (
	"github.com/golibs-starter/golib"
	golibdata "github.com/golibs-starter/golib-data"
	golibgin "github.com/golibs-starter/golib-gin"
	"github.com/serp/ptm-task/src/infrastructure/store/adapter"
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/serp/ptm-task/src/kernel/utils"
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

		fx.Invoke(InitializeDB),

		// Provide adapter
		fx.Provide(adapter.NewDBTransactionAdapter),
		fx.Provide(adapter.NewProjectStoreAdapter),
		fx.Provide(adapter.NewGroupTaskStoreAdapter),
		fx.Provide(adapter.NewTaskStoreAdapter),
		fx.Provide(adapter.NewCommentStoreAdapter),
		fx.Provide(adapter.NewNoteStoreAdapter),

		// Provide service

		// Provide usecase

		// Provide controller

		// Provide JWT components
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(utils.NewJWTUtils),
		fx.Provide(middleware.NewJWTMiddleware),

		golibgin.GinHttpServerOpt(),
		fx.Invoke(router.RegisterGinRouters),
		golibgin.OnStopHttpServerOpt(),
	)
}
