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

type ScheduleWindowAdapter struct {
	db *gorm.DB
}

func NewScheduleWindowAdapter(db *gorm.DB) p.IScheduleWindowStorePort {
	return &ScheduleWindowAdapter{db: db}
}

func (a *ScheduleWindowAdapter) ListAvailabilityWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error) {
	var rows []*m.ScheduleWindowModel
	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND date >= ? AND date <= ?", userID, mp.DayStartUTC(fromDateMs), mp.DayStartUTC(toDateMs)).
		Order("date ASC, start_min ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToScheduleWindowEntities(rows), nil
}

func (a *ScheduleWindowAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error {
	if len(items) == 0 {
		return nil
	}
	models := mp.ToScheduleWindowModels(items)
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}
	return dbx.WithContext(ctx).Create(&models).Error
}

func (a *ScheduleWindowAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.ScheduleWindowEntity) error {
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

		mdl := mp.ToScheduleWindowModel(item)
		if item.IsNew() {
			if err := dbx.WithContext(ctx).Create(mdl).Error; err != nil {
				return err
			}
			continue
		}
		if err := dbx.WithContext(ctx).Model(&m.ScheduleWindowModel{}).
			Where("id = ?", mdl.ID).
			Updates(mdl).Error; err != nil {
			return err
		}
	}
	return nil
}
