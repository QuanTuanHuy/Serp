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
		// Provide adapter
		fx.Provide(adapter.NewProjectClientAdapter),
		fx.Provide(adapter.NewGroupTaskClientAdapter),
		fx.Provide(adapter.NewTaskClientAdapter),
		fx.Provide(adapter.NewCommentClientAdapter),
		fx.Provide(adapter.NewNoteClientAdapter),

		// Provide service
		fx.Provide(service.NewProjectService),
		fx.Provide(service.NewGroupTaskService),
		fx.Provide(service.NewTaskService),
		fx.Provide(service.NewCommentService),
		fx.Provide(service.NewNoteService),

		// Provide controller
		fx.Provide(controller.NewProjectController),
		fx.Provide(controller.NewGroupTaskController),
		fx.Provide(controller.NewTaskController),
		fx.Provide(controller.NewCommentController),
		fx.Provide(controller.NewNoteController),
	)
}
