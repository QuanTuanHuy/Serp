/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleEventPort interface {
	GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error)
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error

	DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error
	DeleteByID(ctx context.Context, tx *gorm.DB, eventID int64) error
	DeleteFutureEventsByTaskID(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, afterDateMs int64) error

	ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	GetPinnedEvents(ctx context.Context, planID int64) ([]*dom.ScheduleEventEntity, error)
	GetByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error)

	CountPendingEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) (int64, error)

	IncrementPartIndexAfter(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, afterPartIndex int) error
	UpdateTotalPartsForTask(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, totalParts int) error
}
