/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/serp/ptm-schedule/src/core/domain/constant"
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type ICalendarExceptionUseCase interface {
	ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error)
	SaveExceptions(ctx context.Context, userID int64, items []*dom.CalendarExceptionEntity) error
	ReplaceExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64, items []*dom.CalendarExceptionEntity) error
	DeleteExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) error
}

type CalendarExceptionUseCase struct {
	svc       service.ICalendarExceptionService
	txService service.ITransactionService
}

func (u *CalendarExceptionUseCase) ListExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.CalendarExceptionEntity, error) {
	if fromDateMs > toDateMs {
		return nil, errors.New(constant.InvalidDateRange)
	}
	return u.svc.ListExceptions(ctx, userID, fromDateMs, toDateMs)
}

func (u *CalendarExceptionUseCase) SaveExceptions(ctx context.Context, userID int64, items []*dom.CalendarExceptionEntity) error {
	if err := u.svc.ValidateItems(userID, items); err != nil {
		return err
	}
	if err := u.svc.ValidateNoOverlapWithExisting(ctx, userID, items); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		var createItems, updateItems []*dom.CalendarExceptionEntity
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
		return nil
	})
}

func (u *CalendarExceptionUseCase) ReplaceExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64, items []*dom.CalendarExceptionEntity) error {
	if fromDateMs > toDateMs {
		return errors.New(constant.InvalidDateRange)
	}

	if err := u.svc.ValidateItems(userID, items); err != nil {
		return err
	}
	for _, it := range items {
		it.ID = 0
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		if err := u.svc.DeleteByDateRange(ctx, tx, userID, fromDateMs, toDateMs); err != nil {
			return err
		}
		if len(items) > 0 {
			return u.svc.CreateBatch(ctx, tx, items)
		}
		return nil
	})
}

func (u *CalendarExceptionUseCase) DeleteExceptions(ctx context.Context, userID int64, fromDateMs, toDateMs int64) error {
	if fromDateMs > toDateMs {
		return errors.New(constant.InvalidDateRange)
	}
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.svc.DeleteByDateRange(ctx, tx, userID, fromDateMs, toDateMs)
	})
}

func NewCalendarExceptionUseCase(svc service.ICalendarExceptionService, txService service.ITransactionService) ICalendarExceptionUseCase {
	return &CalendarExceptionUseCase{svc: svc, txService: txService}
}
