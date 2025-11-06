/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITaskDependencyGraphPort interface {
	CreateDependencyGraph(ctx context.Context, tx *gorm.DB, graph *entity.TaskDependencyGraphEntity) error
	CreateDependencyGraphs(ctx context.Context, tx *gorm.DB, graphs []*entity.TaskDependencyGraphEntity) error

	GetDependencyGraphByID(ctx context.Context, id int64) (*entity.TaskDependencyGraphEntity, error)
	GetDependencyGraphsByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetBlockingTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetBlockedTasks(ctx context.Context, taskID int64) ([]*entity.TaskDependencyGraphEntity, error)
	GetInvalidDependencies(ctx context.Context, userID int64) ([]*entity.TaskDependencyGraphEntity, error)

	UpdateDependencyGraph(ctx context.Context, tx *gorm.DB, graph *entity.TaskDependencyGraphEntity) error
	MarkAsInvalid(ctx context.Context, tx *gorm.DB, graphID int64, reason string, circularPath []int64) error
	MarkAsValid(ctx context.Context, tx *gorm.DB, graphID int64) error

	SoftDeleteDependencyGraph(ctx context.Context, tx *gorm.DB, id int64) error
	DeleteDependenciesByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error
	DeleteDependencyBetweenTasks(ctx context.Context, tx *gorm.DB, taskID int64, dependsOnTaskID int64) error

	ValidateDependency(ctx context.Context, taskID int64, dependsOnTaskID int64) (*DependencyValidationResult, error)
	DetectCircularDependencies(ctx context.Context, taskID int64) ([][]int64, error)
	GetDependencyDepth(ctx context.Context, taskID int64) (int, error)
	GetTopologicalOrder(ctx context.Context, userID int64) ([]int64, error)

	GetDependencyStats(ctx context.Context, userID int64) (*DependencyStats, error)
	GetMostBlockingTasks(ctx context.Context, userID int64, limit int) ([]*BlockingTaskInfo, error)
	GetMostBlockedTasks(ctx context.Context, userID int64, limit int) ([]*BlockedTaskInfo, error)
}

type DependencyValidationResult struct {
	IsValid          bool      `json:"is_valid"`
	Reason           string    `json:"reason,omitempty"`
	CircularPaths    [][]int64 `json:"circular_paths,omitempty"`
	MaxDepth         int       `json:"max_depth"`
	WouldExceedDepth bool      `json:"would_exceed_depth"`
}

type DependencyStats struct {
	TotalDependencies     int64   `json:"total_dependencies"`
	ValidDependencies     int64   `json:"valid_dependencies"`
	InvalidDependencies   int64   `json:"invalid_dependencies"`
	CircularDependencies  int64   `json:"circular_dependencies"`
	AvgDependencyDepth    float64 `json:"avg_dependency_depth"`
	MaxDependencyDepth    int     `json:"max_dependency_depth"`
	TasksWithDependencies int64   `json:"tasks_with_dependencies"`
}

type BlockingTaskInfo struct {
	TaskID             int64   `json:"task_id"`
	TaskTitle          string  `json:"task_title"`
	BlockedCount       int64   `json:"blocked_count"`
	AvgDependencyDepth float64 `json:"avg_dependency_depth"`
}

type BlockedTaskInfo struct {
	TaskID          int64  `json:"task_id"`
	TaskTitle       string `json:"task_title"`
	BlockingCount   int64  `json:"blocking_count"`
	DependencyDepth int    `json:"dependency_depth"`
}
