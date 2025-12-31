package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleTaskPort interface {
	CreateScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, tasks []*entity.ScheduleTaskEntity) error
	UpdateScheduleTask(ctx context.Context, tx *gorm.DB, ID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	DeleteScheduleTask(ctx context.Context, tx *gorm.DB, scheduleTaskID int64) error
	DeleteByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error
	DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error

	GetScheduleTaskByID(ctx context.Context, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error)
	GetScheduleTasksByIDs(ctx context.Context, scheduleTaskIDs []int64) ([]*entity.ScheduleTaskEntity, error)
	GetScheduleTaskByTaskID(ctx context.Context, taskID int64) ([]*entity.ScheduleTaskEntity, error)
	GetByPlanIDAndTaskID(ctx context.Context, planID, taskID int64) (*entity.ScheduleTaskEntity, error)
	GetBySchedulePlanID(ctx context.Context, schedulePlanID int64) ([]*entity.ScheduleTaskEntity, error)
	ListScheduleTasks(ctx context.Context, filter *ScheduleTaskFilter) ([]*entity.ScheduleTaskEntity, int64, error)
}

type ScheduleTaskFilter struct {
	UserID *int64
	PlanID *int64

	Statuses []string

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewScheduleTaskFilter() *ScheduleTaskFilter {
	return &ScheduleTaskFilter{
		SortBy:    "id",
		SortOrder: "DESC",
		Limit:     50,
		Offset:    0,
	}
}
