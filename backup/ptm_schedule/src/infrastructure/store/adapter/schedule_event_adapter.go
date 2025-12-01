/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	p "github.com/serp/ptm-schedule/src/core/port/store"
	mp "github.com/serp/ptm-schedule/src/infrastructure/store/mapper"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type ScheduleEventAdapter struct {
	db *gorm.DB
}

func NewScheduleEventAdapter(db *gorm.DB) p.IScheduleEventStorePort {
	return &ScheduleEventAdapter{db: db}
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

func (a *ScheduleEventAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	if len(items) == 0 {
		return nil
	}
	models := mp.ToScheduleEventModels(items)
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}
	return dbx.WithContext(ctx).Create(&models).Error
}

func (a *ScheduleEventAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleEventEntity) error {
	if len(items) == 0 {
		return nil
	}
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}

	for _, item := range items {
		if item == nil {
			continue
		}

		mdl := mp.ToScheduleEventModel(item)
		if item.IsNew() {
			if err := dbx.WithContext(ctx).Create(mdl).Error; err != nil {
				return err
			}
			continue
		}
		if err := dbx.WithContext(ctx).Model(&m.ScheduleEventModel{}).
			Where("id = ?", mdl.ID).
			Updates(mdl).Error; err != nil {
			return err
		}
	}
	return nil
}
