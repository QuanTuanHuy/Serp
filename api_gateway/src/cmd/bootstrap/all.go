/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/golibs-starter/golib"
	golibdata "github.com/golibs-starter/golib-data"
	golibgin "github.com/golibs-starter/golib-gin"
	"github.com/serp/api-gateway/src/cmd/modules"
	"github.com/serp/api-gateway/src/kernel/properties"
	"github.com/serp/api-gateway/src/kernel/utils"
	"github.com/serp/api-gateway/src/ui/middleware"
	"github.com/serp/api-gateway/src/ui/router"
	"go.uber.org/fx"
)

func All() fx.Option {
	return fx.Options(
		CoreInfrastructure(),

		// Business modules
		modules.AccountModule(),
		modules.PtmModule(),
		modules.PurchaseModule(),

		// HTTP server and routing
		HttpServerModule(),
	)
}

func CoreInfrastructure() fx.Option {
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

		// Provide app properties
		golib.ProvideProps(properties.NewExternalServicePropeties),
		golib.ProvideProps(properties.NewKeycloakProperties),
		golib.ProvideProps(properties.NewCorsProperties),

		// Provide utilities
		fx.Provide(utils.NewJWTUtils),
		fx.Provide(utils.NewKeycloakJwksUtils),
		fx.Provide(middleware.NewJWTMiddleware),
		fx.Provide(middleware.NewCorsMiddleware),
	)
}

func HttpServerModule() fx.Option {
	return fx.Options(
		golibgin.GinHttpServerOpt(),
		fx.Invoke(router.RegisterGinRouters),
		golibgin.OnStopHttpServerOpt(),
	)
}
