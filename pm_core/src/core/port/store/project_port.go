/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/pm-core/src/core/domain/entity"
	"gorm.io/gorm"
)

type IProjectPort interface {
	CreateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) (*entity.ProjectEntity, error)
	GetProjectByID(ctx context.Context, id int64) (*entity.ProjectEntity, error)
	GetProjectByKey(ctx context.Context, tenantID int64, key string) (*entity.ProjectEntity, error)
	GetProjectsByTenantID(ctx context.Context, tenantID int64, filter *ProjectFilter) ([]*entity.ProjectEntity, error)
	CountProjectsByTenantID(ctx context.Context, tenantID int64, filter *ProjectFilter) (int64, error)
	UpdateProject(ctx context.Context, tx *gorm.DB, project *entity.ProjectEntity) error
	SoftDeleteProject(ctx context.Context, tx *gorm.DB, projectID int64) error
}

type ProjectFilter struct {
	Statuses     []string
	ActiveStatus *string
	Visibility   *string

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewProjectFilter() *ProjectFilter {
	return &ProjectFilter{
		Statuses:  []string{},
		SortBy:    "id",
		SortOrder: "DESC",
		Limit:     10,
		Offset:    0,
	}
}
