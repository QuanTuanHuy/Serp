package store

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type IActivityEventStorePort interface {
	CreateActivityEvent(ctx context.Context, tx *gorm.DB, activity *entity.ActivityEventEntity) error

	GetActivityEventByID(ctx context.Context, id int64) (*entity.ActivityEventEntity, error)
	GetActivityEventsByUserID(ctx context.Context, userID int64, filter *ActivityEventFilter) ([]*entity.ActivityEventEntity, error)
}

type ActivityEventFilter struct {
	CreatedFrom *int64
	CreatedTo   *int64
	Limit       *int
	Offset      *int
}
