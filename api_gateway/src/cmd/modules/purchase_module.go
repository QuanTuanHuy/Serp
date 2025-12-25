/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	"go.uber.org/fx"
)

func PurchaseModule() fx.Option {
	return fx.Module("purchase") // Purchase module uses generic proxy, no specific controllers needed

}
