/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	"github.com/serp/ptm-schedule/src/core/service"
	"gorm.io/gorm"
)

type IScheduleTaskUseCase interface {
	GetListTaskByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleTaskEntity, error)
	CreateScheduleTask(ctx context.Context, userID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error)
	UpdateScheduleTask(ctx context.Context, userID, taskID int64, updateTaskMsg *message.KafkaUpdateTaskMessage) (*entity.ScheduleTaskEntity, error)
	DeleteScheduleTask(ctx context.Context, taskID int64) error
	GetBatchTasks(ctx context.Context, userID int64) (map[int32][]*entity.ScheduleTaskEntity, error)
	ChooseTaskBatch(ctx context.Context, userID int64, taskBatch int32) ([]*entity.ScheduleTaskEntity, error)
	GetScheduleTaskDetail(ctx context.Context, userID, taskID, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error)
}

type ScheduleTaskUseCase struct {
	scheduleTaskService service.IScheduleTaskService
	schedulePlanService service.ISchedulePlanService
	txService           service.ITransactionService
}

func (s *ScheduleTaskUseCase) GetListTaskByUserID(ctx context.Context, userID int64) ([]*entity.ScheduleTaskEntity, error) {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	schedulePlanID, activeTaskBatch, isActiveTaskBatch := schedulePlan.ID, schedulePlan.ActiveTaskBatch, schedulePlan.IsActiveTaskBatch

	if isActiveTaskBatch && activeTaskBatch > 0 {
		tasks, err := s.scheduleTaskService.GetByTaskBatch(ctx, schedulePlanID, activeTaskBatch)
		if err != nil {
			return nil, err
		}
		if len(tasks) > 0 {
			return tasks, nil
		}
		err = s.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
			_, err := s.schedulePlanService.UpdateTaskBatch(ctx, tx, schedulePlan, 0, false)
			return err
		})
		if err != nil {
			return nil, err
		}

		log.Info(ctx,
			fmt.Sprintf("No tasks found for userID: %d in active task batch: %d, fetching top 10 newest tasks", userID, activeTaskBatch))
		return s.scheduleTaskService.GetTopNewestTasks(ctx, schedulePlanID, 10)
	}

	if isActiveTaskBatch && activeTaskBatch == 0 {
		return []*entity.ScheduleTaskEntity{}, nil
	}

	if activeTaskBatch == 0 {
		return s.scheduleTaskService.GetTopNewestTasks(ctx, schedulePlanID, 10)
	}
	return []*entity.ScheduleTaskEntity{}, nil
}

func (s *ScheduleTaskUseCase) CreateScheduleTask(ctx context.Context, userID int64, scheduleTask *entity.ScheduleTaskEntity) (*entity.ScheduleTaskEntity, error) {
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		schedulePlan, err := s.schedulePlanService.CreateSchedulePlan(ctx, tx, userID)
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

func (s *ScheduleTaskUseCase) UpdateScheduleTask(ctx context.Context, userID int64, taskID int64, updateTaskMsg *message.KafkaUpdateTaskMessage) (*entity.ScheduleTaskEntity, error) {
	scheduleTask, err := s.scheduleTaskService.GetScheduleTaskByTaskID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, updateTaskMsg.UserID)
	if err != nil {
		return nil, err
	}
	if schedulePlan.UserID != userID {
		log.Error(ctx, fmt.Sprintf("User does not own the schedule plan, userID: %d, schedulePlanID: %d", userID, schedulePlan.ID))
		return nil, errors.New(constant.SchedulePlanNotFound)
	}

	scheduleTask = mapper.UpdateTaskMapper(updateTaskMsg, scheduleTask)
	result, err := s.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		return s.scheduleTaskService.UpdateScheduleTask(ctx, tx, scheduleTask.ID, scheduleTask)
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.ScheduleTaskEntity), nil
}

func (s *ScheduleTaskUseCase) DeleteScheduleTask(ctx context.Context, taskID int64) error {
	scheduleTask, err := s.scheduleTaskService.GetScheduleTaskByTaskID(ctx, taskID)
	if err != nil {
		return err
	}
	return s.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return s.scheduleTaskService.DeleteScheduleTask(ctx, tx, scheduleTask.ID)
	})
}

func (s *ScheduleTaskUseCase) GetBatchTasks(ctx context.Context, userID int64) (map[int32][]*entity.ScheduleTaskEntity, error) {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	scheduleTasks, err := s.scheduleTaskService.GetBySchedulePlanID(ctx, schedulePlan.ID)
	if err != nil {
		return nil, err
	}
	taskMap := make(map[int32][]*entity.ScheduleTaskEntity)
	for _, task := range scheduleTasks {
		if task.TaskBatch > 0 {
			taskMap[task.TaskBatch] = append(taskMap[task.TaskBatch], task)
		}
	}
	return taskMap, nil
}

func (s *ScheduleTaskUseCase) ChooseTaskBatch(ctx context.Context, userID int64, taskBatch int32) ([]*entity.ScheduleTaskEntity, error) {
	schedulePlan, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}
	err = s.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		_, err := s.schedulePlanService.UpdateTaskBatch(ctx, tx, schedulePlan, taskBatch, true)
		return err
	})
	if err != nil {
		return nil, err
	}
	return s.scheduleTaskService.GetByTaskBatch(ctx, schedulePlan.ID, taskBatch)
}

func (s *ScheduleTaskUseCase) GetScheduleTaskDetail(ctx context.Context, userID int64, taskID int64, scheduleTaskID int64) (*entity.ScheduleTaskEntity, error) {
	_, err := s.schedulePlanService.GetSchedulePlanByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	if taskID > 0 {
		return s.scheduleTaskService.GetScheduleTaskByTaskID(ctx, taskID)
	}
	if scheduleTaskID > 0 {
		return s.scheduleTaskService.GetScheduleTaskByID(ctx, scheduleTaskID)
	}

	return nil, nil
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
