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
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type ITaskUseCase interface {
	CreateTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)
	CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error)
	CreateRecurringTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)

	UpdateTask(ctx context.Context, userID int64, task *entity.TaskEntity) error
	UpdateTaskStatus(ctx context.Context, userID int64, taskID int64, status string) error
	CompleteTask(ctx context.Context, userID int64, taskID int64, actualDurationMin int, quality int) error

	DeleteTask(ctx context.Context, userID int64, taskID int64) error
	BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error

	GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	GetOverdueTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)

	RefreshProjectProgressForTask(ctx context.Context, taskID int64) error
}

type taskUseCase struct {
	logger          *zap.Logger
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
		taskService:     taskService,
		templateService: templateService,
		projectService:  projectService,
		noteService:     noteService,
		txService:       txService,
	}
}

func (u *taskUseCase) CreateTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		task, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
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

		_ = u.templateService.IncrementUsageCount(ctx, tx, templateID)

		return task, nil
	})
	if err != nil {
		return nil, err
	}
	return result.(*entity.TaskEntity), nil
}

func (u *taskUseCase) CreateRecurringTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	if task.RecurrencePattern == nil || *task.RecurrencePattern == "" {
		return nil, errors.New(constant.InvalidRecurrencePattern)
	}
	task.IsRecurring = true
	return u.CreateTask(ctx, userID, task)
}

func (u *taskUseCase) UpdateTask(ctx context.Context, userID int64, task *entity.TaskEntity) error {
	return u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		return u.taskService.UpdateTask(ctx, tx, userID, task)
	})
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
