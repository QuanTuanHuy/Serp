/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	"github.com/serp/api-gateway/src/ui/controller/discuss"
	"go.uber.org/fx"
)

func DiscussModule() fx.Option {
	return fx.Module("discuss",
		fx.Provide(discuss.NewDiscussProxyController),
	)
}
