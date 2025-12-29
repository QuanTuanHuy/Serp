/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package modules

import (
	service "github.com/serp/api-gateway/src/core/service/ptm"
	adapter "github.com/serp/api-gateway/src/infrastructure/client/ptm"
	controller "github.com/serp/api-gateway/src/ui/controller/ptm"
	"go.uber.org/fx"
)

func PtmModule() fx.Option {
	return fx.Module("ptm",
		// Provide adapter - PTM Task
		fx.Provide(adapter.NewProjectClientAdapter),
		fx.Provide(adapter.NewTaskClientAdapter),
		fx.Provide(adapter.NewNoteClientAdapter),

		// Provide adapter - PTM Schedule
		fx.Provide(adapter.NewSchedulePlanClientAdapter),
		fx.Provide(adapter.NewAvailabilityCalendarClientAdapter),
		fx.Provide(adapter.NewScheduleWindowClientAdapter),
		fx.Provide(adapter.NewScheduleEventClientAdapter),
		fx.Provide(adapter.NewScheduleTaskClientAdapter),

		// Provide service - PTM Task
		fx.Provide(service.NewProjectService),
		fx.Provide(service.NewTaskService),
		fx.Provide(service.NewNoteService),

		// Provide service - PTM Schedule
		fx.Provide(service.NewSchedulePlanService),
		fx.Provide(service.NewAvailabilityCalendarService),
		fx.Provide(service.NewScheduleWindowService),
		fx.Provide(service.NewScheduleEventService),
		fx.Provide(service.NewScheduleTaskService),

		// Provide controller - PTM Task
		fx.Provide(controller.NewProjectController),
		fx.Provide(controller.NewTaskController),
		fx.Provide(controller.NewNoteController),

		// Provide controller - PTM Schedule
		fx.Provide(controller.NewSchedulePlanController),
		fx.Provide(controller.NewAvailabilityCalendarController),
		fx.Provide(controller.NewScheduleWindowController),
		fx.Provide(controller.NewScheduleEventController),
		fx.Provide(controller.NewScheduleTaskController),
	)
}
