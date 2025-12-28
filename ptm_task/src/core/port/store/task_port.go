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

type ITaskPort interface {
	CreateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error)
	CreateTasks(ctx context.Context, tx *gorm.DB, tasks []*entity.TaskEntity) error

	GetTaskByID(ctx context.Context, id int64) (*entity.TaskEntity, error)
	GetTasksByIDs(ctx context.Context, ids []int64) ([]*entity.TaskEntity, error)
	GetTasksByUserID(ctx context.Context, userID int64, filter *TaskFilter) ([]*entity.TaskEntity, error)
	GetTaskByExternalID(ctx context.Context, externalID string) (*entity.TaskEntity, error)
	CountTasksByUserID(ctx context.Context, userID int64, filter *TaskFilter) (int64, error)

	UpdateTask(ctx context.Context, tx *gorm.DB, task *entity.TaskEntity) (*entity.TaskEntity, error)
	UpdateTaskStatus(ctx context.Context, tx *gorm.DB, taskID int64, status string) error
	UpdateTaskPriority(ctx context.Context, tx *gorm.DB, taskID int64, priority string, priorityScore float64) error
	UpdateTaskDuration(ctx context.Context, tx *gorm.DB, taskID int64, actualDurationMin int) error

	SoftDeleteTask(ctx context.Context, tx *gorm.DB, taskID int64) error
	SoftDeleteTasks(ctx context.Context, tx *gorm.DB, taskIDs []int64) error

	GetOverdueTasks(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.TaskEntity, error)
	GetTasksByDeadline(ctx context.Context, userID int64, fromMs, toMs int64) ([]*entity.TaskEntity, error)
	GetTasksByCategory(ctx context.Context, userID int64, category string) ([]*entity.TaskEntity, error)
	GetDeepWorkTasks(ctx context.Context, userID int64) ([]*entity.TaskEntity, error)
	GetTasksByParentID(ctx context.Context, parentTaskID int64) ([]*entity.TaskEntity, error)
	GetTasksByParentIDWithTx(ctx context.Context, tx *gorm.DB, parentTaskID int64) ([]*entity.TaskEntity, error)
	GetTasksByRootID(ctx context.Context, rootTaskID int64) ([]*entity.TaskEntity, error)
	GetTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.TaskEntity, error)
}

type TaskFilter struct {
	Statuses     []string
	ActiveStatus *string

	Priorities       []string
	MinPriorityScore *float64

	DeadlineFrom *int64
	DeadlineTo   *int64
	CreatedFrom  *int64
	CreatedTo    *int64

	Categories []string
	Tags       []string
	ProjectID  *int64

	IsDeepWork  *bool
	IsMeeting   *bool
	IsRecurring *bool

	HasDependencies *bool
	ParentTaskID    *int64

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewTaskFilter() *TaskFilter {
	return &TaskFilter{
		Statuses:   []string{},
		Priorities: []string{},
		Categories: []string{},
		Tags:       []string{},
		SortBy:     "id",
		SortOrder:  "DESC",
		Limit:      50,
		Offset:     0,
	}
}
