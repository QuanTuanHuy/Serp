package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleTaskPort interface {
	CreateScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error)
	GetByTaskBatch(ctx context.Context, schedulePlanID int64, taskBatch int32) ([]*entity.ScheduleTaskEntity, error)
	GetScheduleTaskByID(ctx context.Context, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error)
	GetScheduleTaskByTaskID(ctx context.Context, taskID int64) (*entity.ScheduleTaskEntity, error)
	DeleteScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTaskID int64) error
	UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	GetTopNewestTasks(ctx context.Context, schedulePlanID int64, limit int) ([]*entity.ScheduleTaskEntity, error)
	DeleteByScheduleGroupID(ctx context.Context, tx *gorm.DB, scheduleGroupID int64) error
}
