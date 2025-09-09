/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/account"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/account"
	controller "github.com/serp/api-gateway/src/ui/controller/account"
	"go.uber.org/fx"
)

func AccountModule() fx.Option {
	return fx.Module("account",
		// Provide adapter
		fx.Provide(adapter.NewAuthClientAdapter),
		fx.Provide(adapter.NewUserClientAdapter),

		// Provide service
		fx.Provide(service.NewAuthService),
		fx.Provide(service.NewUserService),

		// Provide controller
		fx.Provide(controller.NewAuthController),
		fx.Provide(controller.NewUserController),
	)
}
