/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleTaskUseCase interface {
	CreateScheduleTask(ctx context.Context, userID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)

	SyncTaskFromSource(ctx context.Context, event *message.TaskCreatedEvent) error
	UpdateTaskFromSource(ctx context.Context, event *message.TaskUpdatedEvent) error
	DeleteTaskFromSource(ctx context.Context, event *message.TaskDeletedEvent) error

	UpdateTaskConstraints(ctx context.Context, scheduleTaskID int64, constraints map[string]any) error
}

type ScheduleTaskUseCase struct {
	scheduleTaskService service.IScheduleTaskService
	schedulePlanService service.ISchedulePlanService
	txService           service.ITransactionService
}

func (s *ScheduleTaskUseCase) SyncTaskFromSource(ctx context.Context, event *message.TaskCreatedEvent) error {
	log.Info(ctx, "Implement later")
	return nil
}

func (s *ScheduleTaskUseCase) UpdateTaskConstraints(ctx context.Context, scheduleTaskID int64, constraints map[string]any) error {
	log.Info(ctx, "Implement later")
	return nil
}

func (s *ScheduleTaskUseCase) UpdateTaskFromSource(ctx context.Context, event *message.TaskUpdatedEvent) error {
	log.Info(ctx, "Implement later")
	return nil
}

func (s *ScheduleTaskUseCase) DeleteTaskFromSource(ctx context.Context, event *message.TaskDeletedEvent) error {
	log.Info(ctx, "Implement later")
	return nil
}

func (s *ScheduleTaskUseCase) CreateScheduleTask(ctx context.Context, userID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		schedulePlan, err := s.schedulePlanService.GetActivePlanByUserID(ctx, userID)
		if err != nil {
			return nil, err
		}
		scheduleTask.SchedulePlanID = schedulePlan.ID
		scheduleTask, err = s.scheduleTaskService.CreateScheduleTask(ctx, tx, scheduleTask)
		if err != nil {
			return nil, err
		}
		return scheduleTask, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ScheduleTaskEntity), nil
}

func NewScheduleTaskUseCase(scheduleTaskService service.IScheduleTaskService,
	schedulePlanService service.ISchedulePlanService,
	txService service.ITransactionService) IScheduleTaskUseCase {
	return &ScheduleTaskUseCase{
		scheduleTaskService: scheduleTaskService,
		schedulePlanService: schedulePlanService,
		txService:           txService,
	}
}
