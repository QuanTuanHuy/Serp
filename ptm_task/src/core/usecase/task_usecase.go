/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"
	"fmt"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/domain/mapper"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type ITaskUseCase interface {
	CreateTask(ctx context.Context, userID, tenantID int64, req *request.CreateTaskRequest) (*entity.TaskEntity, error)
	CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error)

	UpdateTask(ctx context.Context, userID, taskID int64, req *request.UpdateTaskRequest) (*entity.TaskEntity, error)
	UpdateTaskStatus(ctx context.Context, userID int64, taskID int64, status string) error
	CompleteTask(ctx context.Context, userID int64, taskID int64, actualDurationMin int, quality int) error

	DeleteTask(ctx context.Context, userID int64, taskID int64) error
	BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error

	GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	GetTasksByProjectID(ctx context.Context, userID int64, projectID int64) ([]*entity.TaskEntity, error)
	CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error)
	GetOverdueTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)

	RefreshProjectProgressForTask(ctx context.Context, taskID int64) error
}

type taskUseCase struct {
	logger          *zap.Logger
	mapper          *mapper.TaskMapper
	taskService     service.ITaskService
	templateService service.ITaskTemplateService
	projectService  service.IProjectService
	noteService     service.INoteService
	txService       service.ITransactionService
}

func NewTaskUseCase(
	logger *zap.Logger,
	taskService service.ITaskService,
	templateService service.ITaskTemplateService,
	projectService service.IProjectService,
	noteService service.INoteService,
	txService service.ITransactionService,
) ITaskUseCase {
	return &taskUseCase{
		logger:          logger,
		mapper:          mapper.NewTaskMapper(),
		taskService:     taskService,
		templateService: templateService,
		projectService:  projectService,
		noteService:     noteService,
		txService:       txService,
	}
}

func (u *taskUseCase) CreateTask(ctx context.Context, userID, tenantID int64, req *request.CreateTaskRequest) (*entity.TaskEntity, error) {
	task := u.mapper.CreateRequestToEntity(req, userID, tenantID)
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
			return nil, err
		}

		err = u.taskService.PushTaskCreatedEvent(ctx, task)
		if err != nil {
			u.logger.Error("failed to push task created event", zap.Error(err))
			return nil, err
		}

		return task, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error) {
	template, err := u.templateService.GetTemplateByID(ctx, templateID)
	if err != nil {
		return nil, err
	}
	if template.UserID != userID {
		return nil, errors.New(constant.TemplateDoesNotBelongToUser)
	}

	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task := &entity.TaskEntity{
			UserID:               userID,
			TenantID:             template.TenantID,
			Title:                template.SubstituteVariables(variables),
			Description:          template.Description,
			Priority:             template.Priority,
			EstimatedDurationMin: &template.EstimatedDurationMin,
			Category:             template.Category,
			Tags:                 template.Tags,
			IsDeepWork:           template.IsDeepWork,
			RecurrencePattern:    template.RecurrencePattern,
			RecurrenceConfig:     template.RecurrenceConfig,
		}

		task, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
			return nil, err
		}

		err = u.taskService.PushTaskCreatedEvent(ctx, task)
		if err != nil {
			u.logger.Error("failed to push task created event", zap.Error(err))
			return nil, err
		}

		_ = u.templateService.IncrementUsageCount(ctx, tx, templateID)

		return task, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) UpdateTask(ctx context.Context, userID, taskID int64, req *request.UpdateTaskRequest) (*entity.TaskEntity, error) {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	task = u.mapper.UpdateRequestToEntity(req, task)
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := u.taskService.UpdateTask(ctx, tx, userID, task)
		if err != nil {
			return nil, err
		}
		err = u.taskService.PushTaskUpdatedEvent(ctx, task)
		if err != nil {
			u.logger.Error("failed to push task updated event", zap.Error(err))
			return nil, err
		}
		return task, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) UpdateTaskStatus(ctx context.Context, userID int64, taskID int64, status string) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.taskService.UpdateTaskStatus(ctx, tx, userID, taskID, status)
	})
}

func (u *taskUseCase) CompleteTask(ctx context.Context, userID int64, taskID int64, actualDurationMin int, quality int) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		if err := u.taskService.CompleteTask(ctx, tx, userID, taskID, actualDurationMin, quality); err != nil {
			return err
		}

		go func() {
			if err := u.RefreshProjectProgressForTask(ctx, taskID); err != nil {
				u.logger.Error("failed to refresh project progress", zap.Error(err))
			}
		}()

		return nil
	})
}

func (u *taskUseCase) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		if err := u.taskService.DeleteTask(ctx, tx, userID, taskID); err != nil {
			return err
		}
		err := u.taskService.PushTaskDeletedEvent(ctx, taskID)
		if err != nil {
			u.logger.Error("failed to push task deleted event", zap.Error(err))
			return err
		}

		go func() {
			if err := u.RefreshProjectProgressForTask(ctx, taskID); err != nil {
				u.logger.Error("failed to refresh project progress", zap.Error(err))
			}
		}()

		return nil
	})
}

func (u *taskUseCase) BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		for _, taskID := range taskIDs {
			if err := u.taskService.DeleteTask(ctx, tx, userID, taskID); err != nil {
				return err
			}
			err := u.taskService.PushTaskDeletedEvent(ctx, taskID)
			if err != nil {
				u.logger.Error("failed to push task deleted event", zap.Error(err))
				return err
			}
		}

		go func() {
			for _, taskID := range taskIDs {
				if err := u.RefreshProjectProgressForTask(ctx, taskID); err != nil {
					u.logger.Error(fmt.Sprintf("failed to refresh project progress for task %d", taskID))
				}
			}
		}()

		return nil
	})
}

func (u *taskUseCase) GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error) {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if task.UserID != userID {
		return nil, errors.New(constant.GetTaskForbidden)
	}
	return task, nil
}

func (u *taskUseCase) GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error) {
	return u.taskService.GetTasksByUserID(ctx, userID, filter)
}

func (u *taskUseCase) GetTasksByProjectID(ctx context.Context, userID int64, projectID int64) ([]*entity.TaskEntity, error) {
	project, err := u.projectService.GetProjectByID(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if project.UserID != userID {
		return nil, errors.New(constant.GetTaskForbidden)
	}

	filter := store.NewTaskFilter()
	filter.ProjectID = &projectID
	return u.taskService.GetTasksByUserID(ctx, userID, filter)
}

func (u *taskUseCase) CountTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) (int64, error) {
	return u.taskService.CountTasksByUserID(ctx, userID, filter)
}

func (u *taskUseCase) GetOverdueTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error) {
	return u.taskService.GetOverdueTasks(ctx, userID, 0)
}

func (u *taskUseCase) GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error) {
	return u.taskService.GetDeepWorkTasks(ctx, userID)
}

func (u *taskUseCase) RefreshProjectProgressForTask(ctx context.Context, taskID int64) error {
	task, err := u.taskService.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if task.ProjectID == nil {
		return nil
	}

	tasks, err := u.taskService.GetTaskByProjectID(ctx, *task.ProjectID)
	if err != nil {
		return err
	}
	totalTasks := len(tasks)
	completedTasks := 0
	for _, t := range tasks {
		if enum.TaskStatus(t.Status).IsCompleted() {
			completedTasks++
		}
	}

	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.projectService.UpdateProjectProgress(ctx, tx, *task.ProjectID, totalTasks, completedTasks)
	})
}
