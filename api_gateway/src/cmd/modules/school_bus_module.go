/*
Author: Codex
Description: Part of Serp Project
*/

package modules

import (
	"go.uber.org/fx"
)

func SchoolBusModule() fx.Option {
	return fx.Module("school-bus")
}
