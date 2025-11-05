/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"gorm.io/gorm"
)

type ICalendarExceptionService interface {
	ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error)
	ValidateItems(userID int64, items []*dom.CalendarExceptionEntity) error
	ValidateNoOverlapWithExisting(ctx context.Context, userID int64, items []*dom.CalendarExceptionEntity) error
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error
	DeleteByIDs(ctx context.Context, tx *gorm.DB, ids []int64) error
	DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs, toDateMs int64) error
}

type CalendarExceptionService struct {
	store port.ICalendarExceptionStorePort
}

func (s *CalendarExceptionService) ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error) {
	return s.store.ListExceptions(ctx, userID, fromDateMs, toDateMs)
}

func (s *CalendarExceptionService) ValidateItems(userID int64, items []*dom.CalendarExceptionEntity) error {
	for _, it := range items {
		if !it.BelongsToUser(userID) {
			return errors.New(constant.ExceptionUserIDMismatch)
		}
		if !it.IsValid() {
			return errors.New(constant.ExceptionInvalidItem)
		}
	}

	for i := 0; i < len(items); i++ {
		for j := i + 1; j < len(items); j++ {
			if items[i].OverlapsWith(items[j]) {
				return errors.New(constant.ExceptionItemsOverlap)
			}
		}
	}

	return nil
}

func (s *CalendarExceptionService) ValidateNoOverlapWithExisting(ctx context.Context, userID int64, items []*dom.CalendarExceptionEntity) error {
	if len(items) == 0 {
		return nil
	}

	minDate := items[0].DateMs
	maxDate := items[0].DateMs
	for _, item := range items {
		if item.DateMs < minDate {
			minDate = item.DateMs
		}
		if item.DateMs > maxDate {
			maxDate = item.DateMs
		}
	}

	existing, err := s.ListExceptions(ctx, userID, minDate, maxDate)
	if err != nil {
		return err
	}

	for _, newItem := range items {
		for _, existingItem := range existing {
			if newItem.ID > 0 && newItem.ID == existingItem.ID {
				continue
			}
			if newItem.OverlapsWith(existingItem) {
				return errors.New(constant.ExceptionOverlapWithExisting)
			}
		}
	}

	return nil
}

func (s *CalendarExceptionService) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error {
	return s.store.CreateBatch(ctx, tx, items)
}

func (s *CalendarExceptionService) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.CalendarExceptionEntity) error {
	return s.store.UpdateBatch(ctx, tx, items)
}

func (s *CalendarExceptionService) DeleteByIDs(ctx context.Context, tx *gorm.DB, ids []int64) error {
	return s.store.DeleteByIDs(ctx, tx, ids)
}

func (s *CalendarExceptionService) DeleteByDateRange(ctx context.Context, tx *gorm.DB, userID int64, fromDateMs, toDateMs int64) error {
	return s.store.DeleteByDateRange(ctx, tx, userID, fromDateMs, toDateMs)
}

func NewCalendarExceptionService(store port.ICalendarExceptionStorePort) ICalendarExceptionService {
	return &CalendarExceptionService{store: store}
}
