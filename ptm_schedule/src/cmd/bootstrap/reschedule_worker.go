/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/ui/worker"
	"go.uber.org/fx"
)

func InitializeRescheduleWorker(
	lc fx.Lifecycle,
	rescheduleWorker *worker.RescheduleWorker,
) {
	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			log.Info(ctx, "Starting reschedule worker ...")
			rescheduleWorker.Start()
			log.Info(ctx, "Reschedule worker started successfully")
			return nil
		},
		OnStop: func(ctx context.Context) error {
			log.Info(ctx, "Stopping reschedule worker ...")
			rescheduleWorker.Stop()
			log.Info(ctx, "Reschedule worker stopped successfully")
			return nil
		},
	})
}
