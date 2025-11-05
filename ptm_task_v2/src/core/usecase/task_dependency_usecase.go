/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/service"
	"gorm.io/gorm"
)

type ITaskDependencyUseCase interface {
	// Create operations
	AddDependency(ctx context.Context, userID int64, taskID int64, dependsOnTaskID int64) (*entity.TaskDependencyGraphEntity, error)

	// Delete operations
	RemoveDependency(ctx context.Context, userID int64, taskID int64, dependsOnTaskID int64) error

	// Query operations
	GetBlockingTasks(ctx context.Context, userID int64, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetBlockedTasks(ctx context.Context, userID int64, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetTaskExecutionOrder(ctx context.Context, userID int64) ([]int64, error)
}

type taskDependencyUseCase struct {
	db                *gorm.DB
	dependencyService service.ITaskDependencyService
	txService         service.ITransactionService
}

func NewTaskDependencyUseCase(
	db *gorm.DB,
	dependencyService service.ITaskDependencyService,
	txService service.ITransactionService,
) ITaskDependencyUseCase {
	return &taskDependencyUseCase{
		db:                db,
		dependencyService: dependencyService,
		txService:         txService,
	}
}

func (u *taskDependencyUseCase) AddDependency(ctx context.Context, userID int64, taskID int64, dependsOnTaskID int64) (*entity.TaskDependencyGraphEntity, error) {
	var createdDependency *entity.TaskDependencyGraphEntity
	err := u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		dependency := entity.NewTaskDependencyGraphEntity(userID, taskID, dependsOnTaskID)
		created, err := u.dependencyService.CreateDependency(ctx, tx, userID, dependency)
		if err != nil {
			return err
		}
		createdDependency = created
		return nil
	})
	if err != nil {
		return nil, err
	}
	return createdDependency, nil
}

func (u *taskDependencyUseCase) RemoveDependency(ctx context.Context, userID int64, taskID int64, dependsOnTaskID int64) error {
	return u.txService.ExecuteInTransaction(ctx, u.db, func(tx *gorm.DB) error {
		return u.dependencyService.RemoveDependency(ctx, tx, userID, taskID, dependsOnTaskID)
	})
}

func (u *taskDependencyUseCase) GetBlockingTasks(ctx context.Context, userID int64, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return u.dependencyService.GetBlockingTasks(ctx, taskID)
}

func (u *taskDependencyUseCase) GetBlockedTasks(ctx context.Context, userID int64, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return u.dependencyService.GetBlockedTasks(ctx, taskID)
}

func (u *taskDependencyUseCase) GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return u.dependencyService.GetInvalidDependencies(ctx, userID)
}

func (u *taskDependencyUseCase) GetTaskExecutionOrder(ctx context.Context, userID int64) ([]int64, error) {
	return u.dependencyService.GetTopologicalOrder(ctx, userID)
}
