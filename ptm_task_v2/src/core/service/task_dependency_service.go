/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"
	"time"

	"github.com/serp/ptm-task/src/core/domain/constant"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITaskDependencyService interface {
	// Validation
	ValidateDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) error
	DetectCircularDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) (bool, [][]int64, error)

	// Dependency operations
	CreateDependency(ctx context.Context, tx *gorm.DB, userID int64, dependency *entity.TaskDependencyGraphEntity) (*entity.TaskDependencyGraphEntity, error)
	RemoveDependency(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, dependsOnTaskID int64) error

	// Query operations
	GetBlockingTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetBlockedTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetTopologicalOrder(ctx context.Context, userID int64) ([]int64, error)
}

type taskDependencyService struct {
	dependencyPort store.ITaskDependencyGraphPort
	taskPort       store.ITaskPort
}

func NewTaskDependencyService(
	dependencyPort store.ITaskDependencyGraphPort,
	taskPort store.ITaskPort,
) ITaskDependencyService {
	return &taskDependencyService{
		dependencyPort: dependencyPort,
		taskPort:       taskPort,
	}
}

func (s *taskDependencyService) ValidateDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) error {
	if taskID == dependsOnTaskID {
		return errors.New(constant.DependencyCannotSelfDepend)
	}
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return errors.New(constant.TaskNotFound)
	}
	dependsOnTask, err := s.taskPort.GetTaskByID(ctx, dependsOnTaskID)
	if err != nil {
		return errors.New(constant.DependencyTaskNotFound)
	}
	if task.UserID != dependsOnTask.UserID {
		return errors.New(constant.DependencyDifferentUsers)
	}
	if enum.TaskStatus(dependsOnTask.Status).IsCompleted() {
		return errors.New(constant.DependencyOnCompletedTask)
	}
	hasCircular, _, err := s.DetectCircularDependency(ctx, taskID, dependsOnTaskID)
	if err != nil {
		return err
	}
	if hasCircular {
		return errors.New(constant.DependencyCircularDetected)
	}
	return nil
}

func (s *taskDependencyService) DetectCircularDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) (bool, [][]int64, error) {
	visited := make(map[int64]bool)
	recStack := make(map[int64]bool)
	var circularPaths [][]int64
	var currentPath []int64

	var dfs func(int64) bool
	dfs = func(currentTaskID int64) bool {
		visited[currentTaskID] = true
		recStack[currentTaskID] = true
		currentPath = append(currentPath, currentTaskID)

		dependencies, err := s.dependencyPort.GetBlockingTasks(ctx, currentTaskID)
		if err != nil {
			return false
		}

		for _, dep := range dependencies {
			if !visited[dep.DependsOnTaskID] {
				if dfs(dep.DependsOnTaskID) {
					return true
				}
			} else if recStack[dep.DependsOnTaskID] {
				cyclePath := make([]int64, len(currentPath))
				copy(cyclePath, currentPath)
				cyclePath = append(cyclePath, dep.DependsOnTaskID)
				circularPaths = append(circularPaths, cyclePath)
				return true
			}
		}

		recStack[currentTaskID] = false
		currentPath = currentPath[:len(currentPath)-1]
		return false
	}

	tempPath := currentPath
	currentPath = append(currentPath, taskID, dependsOnTaskID)
	hasCircular := dfs(dependsOnTaskID)
	currentPath = tempPath

	return hasCircular, circularPaths, nil
}

func (s *taskDependencyService) CreateDependency(ctx context.Context, tx *gorm.DB, userID int64, dependency *entity.TaskDependencyGraphEntity) (*entity.TaskDependencyGraphEntity, error) {
	if err := s.ValidateDependency(ctx, dependency.TaskID, dependency.DependsOnTaskID); err != nil {
		return nil, err
	}
	now := time.Now().UnixMilli()
	dependency.CreatedAt = now
	dependency.UpdatedAt = now
	dependency.IsValid = true

	depth, err := s.dependencyPort.GetDependencyDepth(ctx, dependency.TaskID)
	if err == nil {
		dependency.DependencyDepth = depth
	}
	if err := s.dependencyPort.CreateDependencyGraph(ctx, tx, dependency); err != nil {
		return nil, err
	}
	return dependency, nil
}

func (s *taskDependencyService) RemoveDependency(ctx context.Context, tx *gorm.DB, userID int64, taskID int64, dependsOnTaskID int64) error {
	task, err := s.taskPort.GetTaskByID(ctx, taskID)
	if err != nil {
		return err
	}
	if task.UserID != userID {
		return errors.New(constant.DependencyTaskNotBelongToUser)
	}
	return s.dependencyPort.DeleteDependencyBetweenTasks(ctx, tx, taskID, dependsOnTaskID)
}

func (s *taskDependencyService) GetBlockingTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return s.dependencyPort.GetBlockingTasks(ctx, taskID)
}

func (s *taskDependencyService) GetBlockedTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return s.dependencyPort.GetBlockedTasks(ctx, taskID)
}

func (s *taskDependencyService) GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error) {
	return s.dependencyPort.GetInvalidDependencies(ctx, userID)
}

func (s *taskDependencyService) GetTopologicalOrder(ctx context.Context, userID int64) ([]int64, error) {
	return s.dependencyPort.GetTopologicalOrder(ctx, userID)
}
