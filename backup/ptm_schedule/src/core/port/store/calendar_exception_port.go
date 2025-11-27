package port

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type ICalendarExceptionStorePort interface {
	ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error
	DeleteByIDs(ctx context.Context, tx *gorm.DB, ids []int64) error
	DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs, toDateMs int64) error
}
