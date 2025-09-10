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
)

type ITaskUseCase interface {
	GetTaskByID(ctx context.Context, userID, taskID int64) (*entity.TaskEntity, error)
	CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error)
	UpdateTask(ctx context.Context, userID, taskID int64, request *request.UpdateTaskDTO) (*entity.TaskEntity, error)
	DeleteTask(ctx context.Context, userID, taskID int64) error
}

type TaskUseCase struct {
	taskService      service.ITaskService
	groupTaskService service.IGroupTaskService
}

func (t *TaskUseCase) CreateTask(ctx context.Context, userID int64, request *request.CreateTaskDTO) (*entity.TaskEntity, error) {
	groupTask, err := t.groupTaskService.GetGroupTaskByID(ctx, request.GroupTaskID)
	if err != nil {
		log.Error(ctx, "Failed to get group task by ID ", request.GroupTaskID, " error ", err)
		return nil, err
	}
	task, err := t.taskService.CreateTask(ctx, userID, request)
	if err != nil {
		log.Error(ctx, "Failed to create task for user ", userID, " error ", err)
		return nil, err
	}

	_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, groupTask.ID)
	if err != nil {
		return nil, err
	}

	err = t.taskService.PushCreateTaskToKafka(ctx, task)
	if err != nil {
		log.Error(ctx, "Failed to push task creation to Kafka for task ID ", task.ID, " error ", err)
	}

	return task, nil
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
	task, err := t.taskService.UpdateTask(ctx, userID, taskID, request)
	if err != nil {
		log.Error(ctx, "Failed to update task for user ", userID, " error ", err)
		return nil, err
	}

	_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, task.GroupTaskID)
	if err != nil {
		return nil, err
	}

	err = t.taskService.PushUpdateTaskToKafka(ctx, task, request)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to push task update to Kafka for task ID %d: %v", task.ID, err))
	}

	return task, nil
}

func (t *TaskUseCase) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	task, err := t.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	err = t.taskService.DeleteTask(ctx, userID, taskID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to delete task for user %d and task ID %d: %v", userID, taskID, err))
		return err
	}

	_, err = t.groupTaskService.CalculateTasksInGroupTask(ctx, task.GroupTaskID)
	if err != nil {
		return err
	}

	err = t.taskService.PushDeleteTaskToKafka(ctx, taskID)
	if err != nil {
		log.Error(ctx, fmt.Sprintf("Failed to push task deletion to Kafka for task ID %d: %v", taskID, err))
	}

	return err
}

func NewTaskUseCase(taskService service.ITaskService, groupTaskService service.IGroupTaskService) ITaskUseCase {
	return &TaskUseCase{
		taskService:      taskService,
		groupTaskService: groupTaskService,
	}
}
