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
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	storePort "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleEventUseCase interface {
	ListEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error)
	GetEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error)
	SaveEvents(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error
	UpdateEventStatus(ctx context.Context, eventID int64, status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error
	ManuallyMoveEvent(ctx context.Context, userID int64, eventID int64, newDateMs int64, newStartMin, newEndMin int) error
	CompleteEvent(ctx context.Context, userID int64, eventID int64, actualStartMin, actualEndMin int) error
	SplitEvent(ctx context.Context, userID int64, eventID int64, splitPointMin int) (*service.SplitEventResult, error)
	SkipEvent(ctx context.Context, userID int64, eventID int64) error
}

type ScheduleEventUseCase struct {
	eventSvc         service.IScheduleEventService
	scheduleTaskPort storePort.IScheduleTaskPort
	rescheduleQueue  service.IRescheduleQueueService
	txService        service.ITransactionService
}

func (u *ScheduleEventUseCase) ListEvents(ctx context.Context, planID int64, fromDateMs, toDateMs int64) ([]*dom.ScheduleEventEntity, error) {
	if planID <= 0 {
		return nil, errors.New(constant.InvalidPlanID)
	}
	if fromDateMs > toDateMs {
		return nil, errors.New(constant.InvalidDateRange)
	}
	return u.eventSvc.ListEventsByPlanAndDateRange(ctx, planID, fromDateMs, toDateMs)
}

func (u *ScheduleEventUseCase) GetEventsByScheduleTaskID(ctx context.Context, scheduleTaskID int64) ([]*dom.ScheduleEventEntity, error) {
	return u.eventSvc.GetEventsByScheduleTaskID(ctx, scheduleTaskID)
}

func (u *ScheduleEventUseCase) SaveEvents(ctx context.Context, planID int64, events []*dom.ScheduleEventEntity) error {
	if planID <= 0 {
		return errors.New(constant.InvalidPlanID)
	}
	if err := u.eventSvc.ValidateEvents(planID, events); err != nil {
		return err
	}
	if err := u.eventSvc.ValidateNoOverlapWithExisting(ctx, planID, events); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		var createItems, updateItems []*dom.ScheduleEventEntity
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

func (u *ScheduleEventUseCase) UpdateEventStatus(ctx context.Context, eventID int64, status enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) error {
	if eventID <= 0 {
		return errors.New(constant.InvalidEventID)
	}
	if err := u.eventSvc.ValidateStatusUpdate(status, actualStartMin, actualEndMin); err != nil {
		return err
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.eventSvc.UpdateEventStatus(ctx, tx, eventID, status, actualStartMin, actualEndMin)
	})
}

func (u *ScheduleEventUseCase) ManuallyMoveEvent(ctx context.Context, userID int64, eventID int64, newDateMs int64, newStartMin, newEndMin int) error {
	if eventID <= 0 {
		return errors.New(constant.InvalidEventID)
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		result, err := u.eventSvc.MoveAndPinEvent(ctx, tx, eventID, newDateMs, newStartMin, newEndMin)
		if err != nil {
			return err
		}

		task, err := u.scheduleTaskPort.GetScheduleTaskByID(ctx, result.Event.ScheduleTaskID)
		if err != nil {
			return err
		}
		if task == nil {
			return errors.New(constant.ScheduleTaskNotFound)
		}

		startMs := newDateMs + int64(newStartMin)*60*1000
		endMs := newDateMs + int64(newEndMin)*60*1000
		task.PinTo(startMs, endMs)
		if _, err := u.scheduleTaskPort.UpdateScheduleTask(ctx, tx, task.ID, task); err != nil {
			return err
		}

		if result.HasConflicts {
			payload := map[string]any{
				"newDateMs":   newDateMs,
				"newStartMin": newStartMin,
				"newEndMin":   newEndMin,
			}
			return u.rescheduleQueue.EnqueueEventMove(ctx, tx, result.Event.SchedulePlanID, userID, eventID, payload)
		}

		return nil
	})
}

func (u *ScheduleEventUseCase) CompleteEvent(ctx context.Context, userID int64, eventID int64, actualStartMin, actualEndMin int) error {
	if eventID <= 0 {
		return errors.New(constant.InvalidEventID)
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		result, err := u.eventSvc.CompleteEvent(ctx, tx, eventID, actualStartMin, actualEndMin)
		if err != nil {
			return err
		}

		task, err := u.scheduleTaskPort.GetScheduleTaskByID(ctx, result.ScheduleTaskID)
		if err != nil {
			return err
		}

		if result.AllPartsCompleted && task != nil {
			task.MarkAsScheduled()
			if _, err := u.scheduleTaskPort.UpdateScheduleTask(ctx, tx, task.ID, task); err != nil {
				return err
			}
		}

		return u.rescheduleQueue.EnqueueEventComplete(ctx, tx, result.Event.SchedulePlanID, userID, eventID)
	})
}

func (u *ScheduleEventUseCase) SplitEvent(ctx context.Context, userID int64, eventID int64, splitPointMin int) (*service.SplitEventResult, error) {
	if eventID <= 0 {
		return nil, errors.New(constant.InvalidEventID)
	}

	event, err := u.eventSvc.GetByID(ctx, eventID)
	if err != nil {
		return nil, err
	}

	task, err := u.scheduleTaskPort.GetScheduleTaskByID(ctx, event.ScheduleTaskID)
	if err != nil {
		return nil, err
	}

	minSplitDuration := dom.DefaultMinSplitDuration
	if task != nil && task.MinSplitDurationMin > 0 {
		minSplitDuration = task.MinSplitDurationMin
	}

	var splitResult *service.SplitEventResult
	err = u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		result, err := u.eventSvc.SplitEvent(ctx, tx, eventID, splitPointMin, minSplitDuration)
		if err != nil {
			return err
		}
		splitResult = result

		payload := map[string]any{
			"splitPointMin":   splitPointMin,
			"originalEventId": eventID,
			"newEventId":      result.NewEvent.ID,
		}
		return u.rescheduleQueue.EnqueueEventSplit(ctx, tx, event.SchedulePlanID, userID, eventID, payload)
	})

	return splitResult, err
}

func (u *ScheduleEventUseCase) SkipEvent(ctx context.Context, userID int64, eventID int64) error {
	if eventID <= 0 {
		return errors.New(constant.InvalidEventID)
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		event, err := u.eventSvc.GetByID(ctx, eventID)
		if err != nil {
			return err
		}

		if err := u.eventSvc.SkipEvent(ctx, tx, eventID); err != nil {
			return err
		}

		return u.rescheduleQueue.EnqueueEventComplete(ctx, tx, event.SchedulePlanID, userID, eventID)
	})
}

func NewScheduleEventUseCase(
	eventSvc service.IScheduleEventService,
	scheduleTaskPort storePort.IScheduleTaskPort,
	rescheduleQueue service.IRescheduleQueueService,
	txService service.ITransactionService,
) IScheduleEventUseCase {
	return &ScheduleEventUseCase{
		eventSvc:         eventSvc,
		scheduleTaskPort: scheduleTaskPort,
		rescheduleQueue:  rescheduleQueue,
		txService:        txService,
	}
}
