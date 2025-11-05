/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"
	"fmt"

	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleEventUseCase interface {
	ListEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	SaveEvents(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error
	UpdateEventStatus(ctx context.Context, eventID int64, status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
}

type ScheduleEventUseCase struct {
	eventSvc  service.IScheduleEventService
	txService service.ITransactionService
}

func (u *ScheduleEventUseCase) ListEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error) {
	if planID <= 0 {
		return nil, errors.New("invalid planID")
	}
	if fromDateMs > toDateMs {
		return nil, errors.New("invalid date range")
	}
	return u.eventSvc.ListEventsByPlanAndDateRange(ctx, planID, fromDateMs, toDateMs)
}

func (u *ScheduleEventUseCase) SaveEvents(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error {
	if planID <= 0 {
		return errors.New("invalid planID")
	}

	if err := u.eventSvc.ValidateEvents(planID, events); err != nil {
		return err
	}
	if err := u.eventSvc.ValidateNoOverlapWithExisting(ctx, planID, events); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		var createItems []*dom.ScheduleEventEntity
		var updateItems []*dom.ScheduleEventEntity

		for _, ev := range events {
			if ev.ID > 0 {
				updateItems = append(updateItems, ev)
			} else {
				createItems = append(createItems, ev)
			}
		}

		if len(createItems) > 0 {
			if err := u.eventSvc.CreateBatch(ctx, tx, createItems); err != nil {
				return err
			}
		}
		if len(updateItems) > 0 {
			if err := u.eventSvc.UpdateBatch(ctx, tx, updateItems); err != nil {
				return err
			}
		}
		return nil
	})
}

func (u *ScheduleEventUseCase) UpdateEventStatus(
	ctx context.Context,
	eventID int64,
	status enum.ScheduleEventStatus,
	actualStartMin, actualEndMin *int,
) error {
	if eventID <= 0 {
		return errors.New("invalid eventID")
	}

	if err := u.eventSvc.ValidateStatusUpdate(status, actualStartMin, actualEndMin); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		event, err := u.eventSvc.GetByID(ctx, eventID)
		if err != nil {
			return err
		}
		if event == nil {
			return fmt.Errorf("event not found: %d", eventID)
		}

		event.Status = status
		event.ActualStartMin = actualStartMin
		event.ActualEndMin = actualEndMin

		return u.eventSvc.UpdateBatch(ctx, tx, []*dom.ScheduleEventEntity{event})
	})
}

func NewScheduleEventUseCase(
	eventSvc service.IScheduleEventService,
	txService service.ITransactionService,
) IScheduleEventUseCase {
	return &ScheduleEventUseCase{
		eventSvc:  eventSvc,
		txService: txService,
	}
}
