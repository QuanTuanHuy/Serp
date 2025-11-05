/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type ITaskUseCase interface {
	// Create operations
	CreateTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)
	CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error)
	CreateRecurringTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error)

	// Update operations
	UpdateTask(ctx context.Context, userID int64, task *entity.TaskEntity) error
	UpdateTaskStatus(ctx context.Context, userID int64, taskID int64, status string) error
	CompleteTask(ctx context.Context, userID int64, taskID int64, actualDurationMin int, quality int) error

	// Delete operations
	DeleteTask(ctx context.Context, userID int64, taskID int64) error
	BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error

	// Query operations
	GetTaskByID(ctx context.Context, userID int64, taskID int64) (*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *store.TaskFilter) ([]*entity.TaskEntity, error)
	GetOverdueTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
}

type taskUseCase struct {
	db              *gorm.DB
	taskService     service.ITaskService
	templateService service.ITaskTemplateService
	txService       service.ITransactionService
}

func NewTaskUseCase(
	db *gorm.DB,
	taskService service.ITaskService,
	templateService service.ITaskTemplateService,
	txService service.ITransactionService,
) ITaskUseCase {
	return &taskUseCase{
		db:              db,
		taskService:     taskService,
		templateService: templateService,
		txService:       txService,
	}
}

func (u *taskUseCase) CreateTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	var createdTask *entity.TaskEntity
	err := u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		created, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
			return err
		}
		createdTask = created
		return nil
	})
	if err != nil {
		return nil, err
	}
	return createdTask, nil
}

func (u *taskUseCase) CreateTaskFromTemplate(ctx context.Context, userID int64, templateID int64, variables map[string]string) (*entity.TaskEntity, error) {
	template, err := u.templateService.GetTemplateByID(ctx, templateID)
	if err != nil {
		return nil, err
	}
	if template.UserID != userID {
		return nil, errors.New(constant.TemplateDoesNotBelongToUser)
	}

	var createdTask *entity.TaskEntity
	err = u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
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

		created, err := u.taskService.CreateTask(ctx, tx, userID, task)
		if err != nil {
			return err
		}
		if err := u.templateService.IncrementUsageCount(ctx, tx, templateID); err != nil {
			return err
		}
		createdTask = created
		return nil
	})
	if err != nil {
		return nil, err
	}
	return createdTask, nil
}

func (u *taskUseCase) CreateRecurringTask(ctx context.Context, userID int64, task *entity.TaskEntity) (*entity.TaskEntity, error) {
	if task.RecurrencePattern == nil || *task.RecurrencePattern == "" {
		return nil, errors.New(constant.InvalidRecurrencePattern)
	}
	task.IsRecurring = true
	return u.CreateTask(ctx, userID, task)
}

func (u *taskUseCase) UpdateTask(ctx context.Context, userID int64, task *entity.TaskEntity) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.taskService.UpdateTask(ctx, tx, userID, task)
	})
}

func (u *taskUseCase) UpdateTaskStatus(ctx context.Context, userID int64, taskID int64, status string) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.taskService.UpdateTaskStatus(ctx, tx, userID, taskID, status)
	})
}

func (u *taskUseCase) CompleteTask(ctx context.Context, userID int64, taskID int64, actualDurationMin int, quality int) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.taskService.CompleteTask(ctx, tx, userID, taskID, actualDurationMin, quality)
	})
}

func (u *taskUseCase) DeleteTask(ctx context.Context, userID int64, taskID int64) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.taskService.DeleteTask(ctx, tx, userID, taskID)
	})
}

func (u *taskUseCase) BulkDeleteTasks(ctx context.Context, userID int64, taskIDs []int64) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		for _, taskID := range taskIDs {
			if err := u.taskService.DeleteTask(ctx, tx, userID, taskID); err != nil {
				return err
			}
		}
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
