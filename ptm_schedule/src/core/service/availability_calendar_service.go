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

type IAvailabilityCalendarService interface {
	GetByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error)
	ValidateItems(userID int64, items []*dom.AvailabilityCalendarEntity) error
	ValidateNoOverlapWithExisting(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error
	CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error
	UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error
	DeleteByUser(ctx context.Context, tx *gorm.DB, userID int64) error
	DeleteByUserAndDays(ctx context.Context, tx *gorm.DB, userID int64, days []int) error
}

type AvailabilityCalendarService struct {
	store port.IAvailabilityCalendarStorePort
}

func (s *AvailabilityCalendarService) GetByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error) {
	return s.store.ListByUser(ctx, userID)
}

func (s *AvailabilityCalendarService) ValidateItems(userID int64, items []*dom.AvailabilityCalendarEntity) error {
	for _, it := range items {
		if !it.BelongsToUser(userID) {
			return errors.New(constant.AvailabilityUserIDMismatch)
		}
		if !it.IsValid() {
			return errors.New(constant.AvailabilityInvalidItem)
		}
	}

	for i := 0; i < len(items); i++ {
		for j := i + 1; j < len(items); j++ {
			if items[i].OverlapsWith(items[j]) {
				return errors.New(constant.AvailabilityItemsOverlap)
			}
		}
	}

	return nil
}

func (s *AvailabilityCalendarService) ValidateNoOverlapWithExisting(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error {
	existing, err := s.GetByUser(ctx, userID)
	if err != nil {
		return err
	}

	for _, newItem := range items {
		for _, existingItem := range existing {
			if newItem.ID > 0 && newItem.ID == existingItem.ID {
				continue
			}
			if newItem.OverlapsWith(existingItem) {
				return errors.New(constant.AvailabilityOverlapWithExisting)
			}
		}
	}

	return nil
}

func (s *AvailabilityCalendarService) CreateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error {
	return s.store.CreateBatch(ctx, tx, items)
}

func (s *AvailabilityCalendarService) UpdateBatch(ctx context.Context, tx *gorm.DB, items []*dom.AvailabilityCalendarEntity) error {
	return s.store.UpdateBatch(ctx, tx, items)
}

func (s *AvailabilityCalendarService) DeleteByUser(ctx context.Context, tx *gorm.DB, userID int64) error {
	return s.store.DeleteByUser(ctx, tx, userID)
}

func (s *AvailabilityCalendarService) DeleteByUserAndDays(ctx context.Context, tx *gorm.DB, userID int64, days []int) error {
	return s.store.DeleteByUserAndDays(ctx, tx, userID, days)
}

func NewAvailabilityCalendarService(store port.IAvailabilityCalendarStorePort) IAvailabilityCalendarService {
	return &AvailabilityCalendarService{store: store}
}
