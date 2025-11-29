/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/service"
)

type IScheduleTaskUseCase interface {
	HandleTaskCreated(ctx context.Context, event *message.TaskCreatedEvent) error
	HandleTaskUpdated(ctx context.Context, event *message.TaskUpdatedEvent) error
	HandleTaskDeleted(ctx context.Context, event *message.TaskDeletedEvent) error
}

type ScheduleTaskUseCase struct {
	scheduleTaskService service.IScheduleTaskService
	schedulePlanService service.ISchedulePlanService
	txService           service.ITransactionService
}

func (s *ScheduleTaskUseCase) HandleTaskCreated(ctx context.Context, event *message.TaskCreatedEvent) error {
	panic("unimplemented")
}

func (s *ScheduleTaskUseCase) HandleTaskDeleted(ctx context.Context, event *message.TaskDeletedEvent) error {
	panic("unimplemented")
}

func (s *ScheduleTaskUseCase) HandleTaskUpdated(ctx context.Context, event *message.TaskUpdatedEvent) error {
	panic("unimplemented")
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
