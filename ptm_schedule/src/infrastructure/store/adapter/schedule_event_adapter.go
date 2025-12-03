/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	p "github.com/serp/ptm-schedule/src/core/port/store"
	mp "github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ScheduleEventAdapter struct {
	BaseStoreAdapter
}

func (a *ScheduleEventAdapter) GetByID(ctx context.Context, id int64) (*dom.ScheduleEventEntity, error) {
	var row m.ScheduleEventModel
	if err := a.db.WithContext(ctx).Where("id = ?", id).First(&row).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mp.ToScheduleEventEntity(&row), nil
}

func (a *ScheduleEventAdapter) ListEventsByPlanAndDateRange(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error) {
	var rows []*m.ScheduleEventModel
	if err := a.db.WithContext(ctx).
		Where("schedule_plan_id = ? AND date >= ? AND date <= ?", planID, mp.DayStartUTC(fromDateMs), mp.DayStartUTC(toDateMs)).
		Order("date ASC, start_min ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToScheduleEventEntities(rows), nil
}

func (a *ScheduleEventAdapter) GetByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error) {
	var rows []*m.ScheduleEventModel
	if err := a.db.WithContext(ctx).
		Where("schedule_task_id = ?", scheduleTaskID).
		Order("date ASC, start_min ASC, part_index ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToScheduleEventEntities(rows), nil
}

func (a *ScheduleEventAdapter) GetPinnedEvents(ctx context.Context, planID int64) ([]*dom.ScheduleEventEntity, error) {
	var rows []*m.ScheduleEventModel
	if err := a.db.WithContext(ctx).
		Where("schedule_plan_id = ? AND is_pinned = ?", planID, true).
		Order("date ASC, start_min ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToScheduleEventEntities(rows), nil
}

func (a *ScheduleEventAdapter) CountPendingEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) (int64, error) {
	var count int64
	if err := a.db.WithContext(ctx).
		Model(&m.ScheduleEventModel{}).
		Where("schedule_task_id = ? AND status = ?", scheduleTaskID, string(enum.ScheduleEventPlanned)).
		Count(&count).Error; err != nil {
		return 0, err
	}
	return count, nil
}

func (a *ScheduleEventAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	if len(items) == 0 {
		return nil
	}
	models := mp.ToScheduleEventModels(items)
	if err := a.WithTx(tx).WithContext(ctx).Create(&models).Error; err != nil {
		return err
	}
	for i, model := range models {
		items[i].ID = model.ID
	}
	return nil
}

func (a *ScheduleEventAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	if len(items) == 0 {
		return nil
	}

	for _, item := range items {
		if item == nil {
			continue
		}
		mdl := mp.ToScheduleEventModel(item)
		if err := a.WithTx(tx).WithContext(ctx).Model(&m.ScheduleEventModel{}).
			Where("id = ?", mdl.ID).
			Updates(mdl).Error; err != nil {
			return err
		}
	}
	return nil
}

func (a *ScheduleEventAdapter) DeleteByID(ctx context.Context, tx *gorm.DB, eventID int64) error {
	return a.WithTx(tx).WithContext(ctx).Delete(&m.ScheduleEventModel{}, eventID).Error
}

func (a *ScheduleEventAdapter) DeleteByPlanID(ctx context.Context, tx *gorm.DB, planID int64) error {
	return a.WithTx(tx).WithContext(ctx).
		Where("schedule_plan_id = ?", planID).
		Delete(&m.ScheduleEventModel{}).Error
}

func (a *ScheduleEventAdapter) DeleteFutureEventsByTaskID(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, afterDateMs int64) error {
	return a.WithTx(tx).WithContext(ctx).
		Where("schedule_task_id = ? AND date > ? AND status = ?",
			scheduleTaskID, mp.DayStartUTC(afterDateMs), string(enum.ScheduleEventPlanned)).
		Delete(&m.ScheduleEventModel{}).Error
}

func (a *ScheduleEventAdapter) IncrementPartIndexAfter(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, afterPartIndex int) error {
	return a.WithTx(tx).WithContext(ctx).
		Model(&m.ScheduleEventModel{}).
		Where("schedule_task_id = ? AND part_index > ?", scheduleTaskID, afterPartIndex).
		UpdateColumn("part_index", gorm.Expr("part_index + 1")).Error
}

func (a *ScheduleEventAdapter) UpdateTotalPartsForTask(ctx context.Context, tx *gorm.DB, scheduleTaskID int64, totalParts int) error {
	return a.WithTx(tx).WithContext(ctx).
		Model(&m.ScheduleEventModel{}).
		Where("schedule_task_id = ?", scheduleTaskID).
		Update("total_parts", totalParts).Error
}

func NewScheduleEventAdapter(db *gorm.DB) p.IScheduleEventPort {
	return &ScheduleEventAdapter{
		BaseStoreAdapter: BaseStoreAdapter{db: db},
	}
}
