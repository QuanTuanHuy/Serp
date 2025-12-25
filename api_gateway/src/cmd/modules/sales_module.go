/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	"go.uber.org/fx"
)

func SalesModule() fx.Option {
	return fx.Module("sales",
		// Sales module uses generic proxy, no specific controllers needed
	)
}
