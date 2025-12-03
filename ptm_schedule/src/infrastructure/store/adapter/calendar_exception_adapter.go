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

type CalendarExceptionAdapter struct {
	BaseStoreAdapter
}

func NewCalendarExceptionAdapter(db *gorm.DB) p.ICalendarExceptionStorePort {
	return &CalendarExceptionAdapter{
		BaseStoreAdapter: BaseStoreAdapter{db: db},
	}
}

func (a *CalendarExceptionAdapter) ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error) {
	var rows []*m.CalendarExceptionModel
	if err := a.db.WithContext(ctx).
		Where("user_id = ? AND date >= ? AND date <= ?", userID, mp.DayStartUTC(fromDateMs), mp.DayStartUTC(toDateMs)).
		Order("date ASC, start_min ASC").
		Find(&rows).Error; err != nil {
		return nil, err
	}
	return mp.ToCalendarExceptionEntities(rows), nil
}

func (a *CalendarExceptionAdapter) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error {
	if len(items) == 0 {
		return nil
	}
	models := mp.ToCalendarExceptionModels(items)
	return a.WithTx(tx).WithContext(ctx).Create(&models).Error
}

func (a *CalendarExceptionAdapter) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error {
	if len(items) == 0 {
		return nil
	}
	dbx := a.WithTx(tx).WithContext(ctx)

	for _, item := range items {
		if item == nil {
			continue
		}
		mdl := mp.ToCalendarExceptionModel(item)
		if item.IsNew() {
			if err := dbx.Create(mdl).Error; err != nil {
				return err
			}
			continue
		}
		if err := dbx.Model(&m.CalendarExceptionModel{}).
			Where("id = ?", mdl.ID).
			Updates(mdl).Error; err != nil {
			return err
		}
	}
	return nil
}

func (a *CalendarExceptionAdapter) DeleteByIDs(ctx context.Context, tx *gorm.DB, ids []int64) error {
	if len(ids) == 0 {
		return nil
	}
	return a.WithTx(tx).WithContext(ctx).Where("id IN ?", ids).Delete(&m.CalendarExceptionModel{}).Error
}

func (a *CalendarExceptionAdapter) DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs, toDateMs int64) error {
	return a.WithTx(tx).WithContext(ctx).
		Where("user_id = ? AND date >= ? AND date <= ?", userID, mp.DayStartUTC(fromDateMs), mp.DayStartUTC(toDateMs)).
		Delete(&m.CalendarExceptionModel{}).Error
}
