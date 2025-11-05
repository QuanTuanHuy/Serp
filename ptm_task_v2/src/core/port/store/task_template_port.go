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

type ITaskTemplatePort interface {
	CreateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error

	GetTaskTemplateByID(ctx context.Context, id int64) (*entity.TaskTemplateEntity, error)
	GetTaskTemplatesByUserID(ctx context.Context, userID int64, filter *TaskTemplateFilter) ([]*entity.TaskTemplateEntity, error)
	GetFavoriteTemplates(ctx context.Context, userID int64) ([]*entity.TaskTemplateEntity, error)
	CountTaskTemplatesByUserID(ctx context.Context, userID int64) (int64, error)

	UpdateTaskTemplate(ctx context.Context, tx *gorm.DB, template *entity.TaskTemplateEntity) error
	UpdateTemplateUsage(ctx context.Context, tx *gorm.DB, templateID int64, currentTimeMs int64) error
	ToggleFavorite(ctx context.Context, tx *gorm.DB, templateID int64, isFavorite bool) error

	SoftDeleteTaskTemplate(ctx context.Context, tx *gorm.DB, templateID int64) error
}

type TaskTemplateFilter struct {
	Category   *string
	IsFavorite *bool

	NameContains *string

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewTaskTemplateFilter() *TaskTemplateFilter {
	return &TaskTemplateFilter{
		SortBy:    "usage_count",
		SortOrder: "DESC",
		Limit:     20,
		Offset:    0,
	}
}
