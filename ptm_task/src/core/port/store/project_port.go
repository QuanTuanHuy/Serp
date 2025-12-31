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

type IProjectPort interface {
	CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) (*entity.ProjectEntity, error)
	CreateProjects(ctx context.Context, tx *gorm.DB, projects []*entity.ProjectEntity) error

	GetProjectByID(ctx context.Context, id int64) (*entity.ProjectEntity, error)
	GetProjectsByIDs(ctx context.Context, ids []int64) ([]*entity.ProjectEntity, error)
	GetProjectsByUserID(ctx context.Context, userID int64, filter *ProjectFilter) ([]*entity.ProjectEntity, error)
	CountProjectsByUserID(ctx context.Context, userID int64, filter *ProjectFilter) (int64, error)

	UpdateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) error
	UpdateProjectStatus(ctx context.Context, tx *gorm.DB, projectID int64, status string) error
	UpdateProjectProgress(ctx context.Context, tx *gorm.DB, projectID int64, totalTasks, completedTasks, progressPercentage int) error

	SoftDeleteProject(ctx context.Context, tx *gorm.DB, projectID int64) error
	SoftDeleteProjects(ctx context.Context, tx *gorm.DB, projectIDs []int64) error

	// Statistics queries
	GetProjectStats(ctx context.Context, projectID int64) (*ProjectStats, error)
	GetProjectsWithStats(ctx context.Context, userID int64, filter *ProjectFilter) ([]*entity.ProjectEntity, error)
	GetFavoriteProjects(ctx context.Context, userID int64) ([]*entity.ProjectEntity, error)
	GetOverdueProjects(ctx context.Context, userID int64, currentTimeMs int64) ([]*entity.ProjectEntity, error)
}

type ProjectFilter struct {
	Statuses     []string
	ActiveStatus *string
	Priorities   []string

	DeadlineFrom *int64
	DeadlineTo   *int64
	CreatedFrom  *int64
	CreatedTo    *int64

	IsFavorite *bool

	MinProgress *int
	MaxProgress *int

	HasTasks *bool

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewProjectFilter() *ProjectFilter {
	return &ProjectFilter{
		Statuses:   []string{},
		Priorities: []string{},
		SortBy:     "created_at",
		SortOrder:  "DESC",
		Limit:      10,
		Offset:     0,
	}
}

type ProjectStats struct {
	ProjectID int64 `json:"projectId"`

	TotalTasks      int `json:"totalTasks"`
	CompletedTasks  int `json:"completedTasks"`
	TodoTasks       int `json:"todoTasks"`
	InProgressTasks int `json:"inProgressTasks"`

	EstimatedDurationMin int `json:"estimatedDurationMin"`
	ActualDurationMin    int `json:"actualDurationMin"`

	OverdueTasks int `json:"overdueTasks"`

	ProgressPercentage int `json:"progressPercentage"`
}
