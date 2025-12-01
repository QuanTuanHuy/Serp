package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type ISchedulePlanPort interface {
	CreateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	UpdateSchedulePlan(ctx context.Context, tx *gorm.DB, schedulePlan *entity.SchedulePlanEntity) (*entity.SchedulePlanEntity, error)
	DeleteSchedulePlan(ctx context.Context, tx *gorm.DB, ID int64) error
	GetSchedulePlanByID(ctx context.Context, ID int64) (*entity.SchedulePlanEntity, error)
	GetSchedulePlanByUserID(ctx context.Context, userID int64) (*entity.SchedulePlanEntity, error)
}
