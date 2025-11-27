/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/golibs-starter/golib/log"
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IAvailabilityCalendarUseCase interface {
	GetAvailabilityByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error)
	SetAvailabilityForUser(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error
	ReplaceAvailabilityForUser(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error
}

type AvailabilityCalendarUseCase struct {
	svc       service.IAvailabilityCalendarService
	txService service.ITransactionService
}

func (u *AvailabilityCalendarUseCase) GetAvailabilityByUser(ctx context.Context, userID int64) ([]*dom.AvailabilityCalendarEntity, error) {
	return u.svc.GetByUser(ctx, userID)
}

func (u *AvailabilityCalendarUseCase) SetAvailabilityForUser(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error {
	if err := u.svc.ValidateItems(userID, items); err != nil {
		return err
	}
	if err := u.svc.ValidateNoOverlapWithExisting(ctx, userID, items); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		var createItems []*dom.AvailabilityCalendarEntity
		var updateItems []*dom.AvailabilityCalendarEntity
		for _, it := range items {
			if it.ID > 0 {
				updateItems = append(updateItems, it)
			} else {
				createItems = append(createItems, it)
			}
		}
		if len(createItems) > 0 {
			if err := u.svc.CreateBatch(ctx, tx, createItems); err != nil {
				return err
			}
		}
		if len(updateItems) > 0 {
			if err := u.svc.UpdateBatch(ctx, tx, updateItems); err != nil {
				return err
			}
		}
		log.Info(ctx, "Availability calendar saved for user", userID)
		return nil
	})
}

func (u *AvailabilityCalendarUseCase) ReplaceAvailabilityForUser(ctx context.Context, userID int64, items []*dom.AvailabilityCalendarEntity) error {
	if err := u.svc.ValidateItems(userID, items); err != nil {
		return err
	}

	daySet := map[int]struct{}{}
	for _, it := range items {
		daySet[it.DayOfWeek] = struct{}{}
	}
	days := make([]int, 0, len(daySet))
	for d := range daySet {
		days = append(days, d)
	}
	for _, it := range items {
		it.ID = 0
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		if err := u.svc.DeleteByUserAndDays(ctx, tx, userID, days); err != nil {
			return err
		}
		if len(items) > 0 {
			if err := u.svc.CreateBatch(ctx, tx, items); err != nil {
				return err
			}
		}
		log.Info(ctx, "Availability calendar replaced for user", userID)
		return nil
	})
}

func NewAvailabilityCalendarUseCase(svc service.IAvailabilityCalendarService, txService service.ITransactionService) IAvailabilityCalendarUseCase {
	return &AvailabilityCalendarUseCase{svc: svc, txService: txService}
}
