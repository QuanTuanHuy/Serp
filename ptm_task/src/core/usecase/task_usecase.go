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
	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type ITaskUseCase interface {
	GetTaskByID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error)
	CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, userID, taskID int64, request *request.UpdateTaskDTO) (*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, userID, taskID int64) error
	SetParentTask(ctx context.Context, userID, taskID int64, req *request.SetParentTaskDTO) (*entity.TaskEntity, error)
}

type TaskUseCase struct {
	taskService      service.ITaskService
	groupTaskService service.IGroupTaskService
	txService        service.ITransactionService
}

func (t *TaskUseCase) CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error) {
	groupTask, err := t.groupTaskService.GetGroupTaskByID(ctx, request.GroupTaskID)
	if err != nil {
		return nil, err
	}
	result, err := t.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := t.taskService.CreateTask(ctx, tx, userID, request)
		if err != nil {
			return nil, err
		}

		_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, tx, groupTask.ID)
		if err != nil {
			return nil, err
		}

		err = t.taskService.PushCreateTaskToKafka(ctx, task)
		if err != nil {
			log.Error(ctx, "Failed to push task creation to Kafka for task ID ", task.ID, " error ", err)
			return nil, err
		}
		return task, nil

	})
	if err != nil {
		return nil, err
	}

	return result.(*entity.TaskEntity), nil
}

func (t *TaskUseCase) GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error) {
	task, err := t.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		log.Error(ctx, "User ", userID, " does not have permission to access task ", taskID)
		return nil, errors.New(constant.GetTaskForbidden)
	}
	return task, nil
}

func (t *TaskUseCase) UpdateTask(ctx context.Context, userID, taskID int64, request *request.UpdateTaskDTO) (*entity.TaskEntity, error) {
	result, err := t.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := t.taskService.UpdateTask(ctx, tx, userID, taskID, request)
		if err != nil {
			return nil, err
		}

		_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, tx, task.GroupTaskID)
		if err != nil {
			return nil, err
		}

		err = t.taskService.PushUpdateTaskToKafka(ctx, task, request)
		if err != nil {
			log.Error(ctx, fmt.Sprintf("Failed to push task update to Kafka for task ID %d: %v", task.ID, err))
			return nil, err
		}
		return task, nil
	})
	if err != nil {
		return nil, err
	}

	return result.(*entity.TaskEntity), nil
}

func (t *TaskUseCase) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	task, err := t.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}

	return t.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		err = t.taskService.DeleteTask(ctx, tx, userID, taskID)
		if err != nil {
			return err
		}

		_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, tx, task.GroupTaskID)
		if err != nil {
			return err
		}

		err = t.taskService.PushDeleteTaskToKafka(ctx, taskID)
		if err != nil {
			log.Error(ctx, fmt.Sprintf("Failed to push task deletion to Kafka for task ID %d: %v", taskID, err))
			return err
		}
		return nil
	})
}

func (t *TaskUseCase) SetParentTask(ctx context.Context, userID, taskID int64, req *request.SetParentTaskDTO) (*entity.TaskEntity, error) {
	result, err := t.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updated, err := t.taskService.SetParentTask(ctx, tx, userID, taskID, req.ParentTaskID)
		if err != nil {
			return nil, err
		}
		return updated, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func NewTaskUseCase(taskService service.ITaskService,
	groupTaskService service.IGroupTaskService,
	txService service.ITransactionService) ITaskUseCase {
	return &TaskUseCase{
		taskService:      taskService,
		groupTaskService: groupTaskService,
		txService:        txService,
	}
}
