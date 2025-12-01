package port

import (
	"context"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleGroupPort interface {
	CreateScheduleGroup(ctx context.Context, tx *gorm.DB, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error)
	UpdateScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64, scheduleGroup *entity.ScheduleGroupEntity) (*entity.ScheduleGroupEntity, error)
	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleGroupEntity, error)
	DeleteScheduleGroup(ctx context.Context, tx *gorm.DB, ID int64) error
	GetScheduleGroupByID(ctx context.Context, ID int64) (*entity.ScheduleGroupEntity, error)
	GetScheduleGroupsToCreateTask(ctx context.Context, date time.Time, limit int32) ([]*entity.ScheduleGroupEntity, error)
}
