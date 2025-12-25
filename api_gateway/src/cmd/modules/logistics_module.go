/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	"go.uber.org/fx"
)

func LogisticsModule() fx.Option {
	return fx.Module("logistics") // Logistics module uses generic proxy, no specific controllers needed

}
