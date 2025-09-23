/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"gorm.io/gorm"
)

type IScheduleDayPort interface {
	CreateScheduleDayInBatch(ctx context.Context, tx *gorm.DB, scheduleDays []*entity.ScheduleDayEntity) ([]*entity.ScheduleDayEntity, error)
	UpdateScheduleDay(ctx context.Context, tx *gorm.DB, scheduleDayID int64, scheduleDay *entity.ScheduleDayEntity) (*entity.ScheduleDayEntity, error)
	DeleteScheduleDayByWeekDay(ctx context.Context, tx *gorm.DB, userID int64, dayOfWeek int) error
	GetByUserIDAndWeekDay(ctx context.Context, userID int64, dayOfWeek int) ([]*entity.ScheduleDayEntity, error)
	GetAllByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleDayEntity, error)
	GetScheduleDayByID(ctx context.Context, scheduleDayID int64) (*entity.ScheduleDayEntity, error)
}
