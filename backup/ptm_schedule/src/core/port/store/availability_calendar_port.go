package port

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IAvailabilityCalendarStorePort interface {
	ListByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error
	DeleteByUser(ctx context.Context, tx *gorm.DB, userID int64) error
	DeleteByUserAndDays(ctx context.Context, tx *gorm.DB, userID int64, days []int) error
}
