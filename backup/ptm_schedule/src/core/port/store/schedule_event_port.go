package port

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleEventStorePort interface {
	GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error)
	ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
}
