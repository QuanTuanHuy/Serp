/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleWindowUseCase interface {
	MaterializeWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) error
	ListWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error)
}

type ScheduleWindowUseCase struct {
	windowSvc    service.IScheduleWindowService
	availSvc     service.IAvailabilityCalendarService
	exceptionSvc service.ICalendarExceptionService
	txService    service.ITransactionService
}

func (u *ScheduleWindowUseCase) ListWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleWindowEntity, error) {
	if fromDateMs > toDateMs {
		return nil, errors.New("invalid date range")
	}
	return u.windowSvc.ListAvailabilityWindows(ctx, userID, fromDateMs, toDateMs)
}

func (u *ScheduleWindowUseCase) MaterializeWindows(ctx context.Context, userID int64, fromDateMs, toDateMs int64) error {
	if fromDateMs > toDateMs {
		return errors.New("invalid date range")
	}

	availCalendar, err := u.availSvc.GetByUser(ctx, userID)
	if err != nil {
		return err
	}
	if len(availCalendar) == 0 {
		return errors.New("no availability calendar found for user")
	}

	exceptions, err := u.exceptionSvc.ListExceptions(ctx, userID, fromDateMs, toDateMs)
	if err != nil {
		return err
	}

	windows := u.windowSvc.ExpandAvailabilityToWindows(availCalendar, fromDateMs, toDateMs)
	windows = u.windowSvc.SubtractExceptions(windows, exceptions)

	if len(windows) == 0 {
		return nil
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.windowSvc.CreateBatch(ctx, tx, windows)
	})
}

func NewScheduleWindowUseCase(
	windowSvc service.IScheduleWindowService,
	availSvc service.IAvailabilityCalendarService,
	exceptionSvc service.ICalendarExceptionService,
	txService service.ITransactionService,
) IScheduleWindowUseCase {
	return &ScheduleWindowUseCase{
		windowSvc:    windowSvc,
		availSvc:     availSvc,
		exceptionSvc: exceptionSvc,
		txService:    txService,
	}
}
