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

type AvailabilityCalendarAdapter struct {
	db *gorm.DB
}

func NewAvailabilityCalendarAdapter(db *gorm.DB) p.IAvailabilityCalendarStorePort {
	return &AvailabilityCalendarAdapter{db: db}
}

func (a *AvailabilityCalendarAdapter) ListByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error) {
	var rows []*m.AvailabilityCalendarModel
	if err := a.db.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("day_of_week ASC, start_min ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToAvailabilityCalendarEntities(rows), nil
}

func (a *AvailabilityCalendarAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error {
	if len(items) == 0 {
		return nil
	}
	models := mp.ToAvailabilityCalendarModels(items)
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}
	return dbx.WithContext(ctx).Create(&models).Error
}

func (a *AvailabilityCalendarAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error {
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
		mdl := mp.ToAvailabilityCalendarModel(item)
		if item.IsNew() {
			if err := dbx.WithContext(ctx).Create(mdl).Error; err != nil {
				return err
			}
			continue
		}
		if err := dbx.WithContext(ctx).Model(&m.AvailabilityCalendarModel{}).
			Where("id = ?", mdl.ID).
			Updates(mdl).Error; err != nil {
			return err
		}
	}
	return nil
}

func (a *AvailabilityCalendarAdapter) DeleteByUser(ctx context.Context, tx *gorm.DB, userID int64) error {
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}
	return dbx.WithContext(ctx).Where("user_id = ?", userID).Delete(&m.AvailabilityCalendarModel{}).Error
}

func (a *AvailabilityCalendarAdapter) DeleteByUserAndDays(ctx context.Context, tx *gorm.DB, userID int64, days []int) error {
	if len(days) == 0 {
		return nil
	}
	dbx := tx
	if dbx == nil {
		dbx = a.db
	}
	return dbx.WithContext(ctx).Where("user_id = ? AND day_of_week IN ?", userID, days).Delete(&m.AvailabilityCalendarModel{}).Error
}
