/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	port "github.com/serp/ptm-schedule/src/core/port/store"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleTaskUseCase interface {
	HandleTaskCreated(ctx context.Context, event *message.TaskCreatedEvent) error
	HandleTaskUpdated(ctx context.Context, event *message.TaskUpdatedEvent) error
	HandleTaskDeleted(ctx context.Context, event *message.TaskDeletedEvent) error
	HandleTaskBulkDeleted(ctx context.Context, event *message.TaskBulkDeletedEvent) error

	ListScheduleTasks(ctx context.Context, userID int64, filter *port.ScheduleTaskFilter) (
		[]*entity.ScheduleTaskEntity, int64, error)
}

type ScheduleTaskUseCase struct {
	scheduleTaskService    service.IScheduleTaskService
	schedulePlanService    service.ISchedulePlanService
	rescheduleQueueService service.IRescheduleQueueService
	txService              service.ITransactionService
}

func NewScheduleTaskUseCase(
	scheduleTaskService service.IScheduleTaskService,
	schedulePlanService service.ISchedulePlanService,
	rescheduleQueueService service.IRescheduleQueueService,
	txService service.ITransactionService,
) IScheduleTaskUseCase {
	return &ScheduleTaskUseCase{
		scheduleTaskService:    scheduleTaskService,
		schedulePlanService:    schedulePlanService,
		rescheduleQueueService: rescheduleQueueService,
		txService:              txService,
	}
}

func (u *ScheduleTaskUseCase) HandleTaskCreated(ctx context.Context, event *message.TaskCreatedEvent) error {
	_, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		activePlan, err := u.schedulePlanService.GetOrCreateActivePlan(ctx, tx, event.UserID, event.TenantID)
		if err != nil {
			log.Error(ctx, "Failed to get or create active plan: ", err)
			return nil, err
		}
		existed, _ := u.scheduleTaskService.GetByPlanIDAndTaskID(ctx, activePlan.ID, event.TaskID)
		if existed != nil {
			log.Warn(ctx, "Schedule task snapshot already exists for task ID: ", event.TaskID)
			return nil, nil
		}

		createdTask, err := u.scheduleTaskService.CreateSnapshot(ctx, tx, activePlan.ID, event)
		if err != nil {
			return nil, err
		}

		err = u.rescheduleQueueService.EnqueueTaskChange(ctx, tx, activePlan.ID, event.UserID, createdTask.ID, enum.TriggerTaskAdded)
		if err != nil {
			log.Error(ctx, "Failed to enqueue task change: ", err)
			return nil, err
		}

		if err := u.schedulePlanService.MarkProposedPlanAsStale(ctx, tx, event.UserID); err != nil {
			log.Warn(ctx, "Failed to mark proposed plan as stale: ", err)
		}

		return nil, nil
	})

	return err
}

func (u *ScheduleTaskUseCase) HandleTaskUpdated(ctx context.Context, event *message.TaskUpdatedEvent) error {
	_, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		activePlan, err := u.schedulePlanService.GetActivePlanByUserID(ctx, event.UserID)
		if err != nil {
			log.Error(ctx, "Failed to get active plan: ", err)
			return nil, err
		}

		changeType, err := u.scheduleTaskService.SyncSnapshot(ctx, tx, activePlan.ID, event)
		if err != nil {
			return nil, err
		}

		if changeType == enum.ChangeConstraint || changeType == enum.ChangeStructural {
			scheduleTask, err := u.scheduleTaskService.GetByPlanIDAndTaskID(ctx, activePlan.ID, event.TaskID)
			if err != nil {
				log.Warn(ctx, "Schedule task not found after sync: ", err)
				return nil, nil
			}

			err = u.rescheduleQueueService.EnqueueTaskChange(ctx, tx, activePlan.ID, event.UserID, scheduleTask.ID, enum.TriggerConstraintChange)
			if err != nil {
				log.Error(ctx, "Failed to enqueue task change: ", err)
				return nil, err
			}

			if err := u.schedulePlanService.MarkProposedPlanAsStale(ctx, tx, event.UserID); err != nil {
				log.Warn(ctx, "Failed to mark proposed plan as stale: ", err)
			}
		}

		return nil, nil
	})

	return err
}

func (u *ScheduleTaskUseCase) HandleTaskDeleted(ctx context.Context, event *message.TaskDeletedEvent) error {
	_, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		activePlan, err := u.schedulePlanService.GetActivePlanByUserID(ctx, event.UserID)
		if err != nil {
			log.Error(ctx, "Failed to get active plan: ", err)
			return nil, err
		}

		scheduleTask, err := u.scheduleTaskService.GetByPlanIDAndTaskID(ctx, activePlan.ID, event.TaskID)
		if err != nil {
			return nil, err
		}

		scheduleTaskID := scheduleTask.ID

		err = u.scheduleTaskService.DeleteSnapshot(ctx, tx, activePlan.ID, event.TaskID)
		if err != nil {
			return nil, err
		}

		err = u.rescheduleQueueService.EnqueueTaskChange(ctx, tx, activePlan.ID, event.UserID, scheduleTaskID, enum.TriggerTaskDeleted)
		if err != nil {
			log.Error(ctx, "Failed to enqueue task deletion: ", err)
			return nil, err
		}

		if err := u.schedulePlanService.MarkProposedPlanAsStale(ctx, tx, event.UserID); err != nil {
			log.Warn(ctx, "Failed to mark proposed plan as stale: ", err)
		}

		return nil, nil
	})

	return err
}

func (u *ScheduleTaskUseCase) HandleTaskBulkDeleted(ctx context.Context, event *message.TaskBulkDeletedEvent) error {
	_, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		activePlan, err := u.schedulePlanService.GetActivePlanByUserID(ctx, event.UserID)
		if err != nil {
			log.Error(ctx, "Failed to get active plan: ", err)
			return nil, err
		}

		for _, taskID := range event.TaskIDs {
			scheduleTask, err := u.scheduleTaskService.GetByPlanIDAndTaskID(ctx, activePlan.ID, taskID)
			if err != nil {
				return nil, err
			}
			scheduleTaskID := scheduleTask.ID
			err = u.scheduleTaskService.DeleteSnapshot(ctx, tx, activePlan.ID, taskID)
			if err != nil {
				return nil, err
			}
			err = u.rescheduleQueueService.EnqueueTaskChange(ctx, tx, activePlan.ID, event.UserID, scheduleTaskID, enum.TriggerTaskDeleted)
			if err != nil {
				log.Error(ctx, "Failed to enqueue task deletion: ", err)
				return nil, err
			}
		}

		if err := u.schedulePlanService.MarkProposedPlanAsStale(ctx, tx, event.UserID); err != nil {
			log.Warn(ctx, "Failed to mark proposed plan as stale: ", err)
		}
		return nil, nil
	})

	return err
}

func (u *ScheduleTaskUseCase) ListScheduleTasks(
	ctx context.Context,
	userID int64,
	filter *port.ScheduleTaskFilter,
) ([]*entity.ScheduleTaskEntity, int64, error) {
	var targetPlanID int64
	if filter.PlanID != nil && *filter.PlanID > 0 {
		targetPlanID = *filter.PlanID
		targetPlan, err := u.schedulePlanService.GetPlanByID(ctx, targetPlanID)
		if err != nil {
			return nil, 0, err
		}
		if targetPlan.UserID != userID {
			return nil, 0, errors.New(constant.ForbiddenAccess)
		}
	} else {
		activePlan, err := u.schedulePlanService.GetActivePlanByUserID(ctx, userID)
		if err != nil {
			return nil, 0, err
		}
		targetPlanID = activePlan.ID
	}
	filter.PlanID = &targetPlanID
	return u.scheduleTaskService.ListScheduleTasks(ctx, filter)
}
