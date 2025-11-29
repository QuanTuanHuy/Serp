/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"context"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/service"
	"go.uber.org/fx"
)

const (
	CleanupInterval = 1 * time.Hour
)

func InitializeEventCleanupJob(
	lc fx.Lifecycle,
	idempotencyService service.IIdempotencyService,
) {
	var cleanupCancel context.CancelFunc

	lc.Append(fx.Hook{
		OnStart: func(ctx context.Context) error {
			log.Info(ctx, "Starting processed events cleanup job ...")

			cleanupCtx, cancel := context.WithCancel(context.Background())
			cleanupCancel = cancel

			go runCleanupLoop(cleanupCtx, idempotencyService)

			log.Info(ctx, "Processed events cleanup job started successfully")
			return nil
		},
		OnStop: func(ctx context.Context) error {
			log.Info(ctx, "Stopping processed events cleanup job ...")

			if cleanupCancel != nil {
				cleanupCancel()
			}

			log.Info(ctx, "Processed events cleanup job stopped successfully")
			return nil
		},
	})
}

func runCleanupLoop(ctx context.Context, idempotencyService service.IIdempotencyService) {
	ticker := time.NewTicker(CleanupInterval)
	defer ticker.Stop()

	runCleanup(ctx, idempotencyService)

	for {
		select {
		case <-ctx.Done():
			log.Info(ctx, "Cleanup loop stopped")
			return
		case <-ticker.C:
			runCleanup(ctx, idempotencyService)
		}
	}
}

func runCleanup(ctx context.Context, idempotencyService service.IIdempotencyService) {
	count, err := idempotencyService.CleanupExpiredEvents(ctx)
	if err != nil {
		log.Error(ctx, "Failed to cleanup expired events: ", err)
		return
	}
	if count > 0 {
		log.Info(ctx, "Cleaned up ", count, " expired processed events")
	}
}
