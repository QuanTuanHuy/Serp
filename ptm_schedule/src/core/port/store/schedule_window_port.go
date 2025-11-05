package port

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleWindowStorePort interface {
	ListAvailabilityWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error
}
