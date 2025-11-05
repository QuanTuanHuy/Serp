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

type ITaskChangeLogPort interface {
	CreateChangeLog(ctx context.Context, tx *gorm.DB, log *entity.TaskChangeLogEntity) error
	CreateChangeLogs(ctx context.Context, tx *gorm.DB, logs []*entity.TaskChangeLogEntity) error

	GetChangeLogByID(ctx context.Context, id int64) (*entity.TaskChangeLogEntity, error)
	GetChangeLogsByTaskID(ctx context.Context, taskID int64, limit int) ([]*entity.TaskChangeLogEntity, error)
	GetChangeLogsByUserID(ctx context.Context, userID int64, filter *ChangeLogFilter) ([]*entity.TaskChangeLogEntity, error)
	GetRecentChanges(ctx context.Context, userID int64, limit int) ([]*entity.TaskChangeLogEntity, error)

	GetChangesByType(ctx context.Context, userID int64, changeType string, fromTimeMs int64) ([]*entity.TaskChangeLogEntity, error)
	GetChangesBySource(ctx context.Context, userID int64, source string, fromTimeMs int64) ([]*entity.TaskChangeLogEntity, error)
	CountChangesByType(ctx context.Context, userID int64, fromTimeMs int64) (map[string]int64, error)
}

type ChangeLogFilter struct {
	ChangeTypes   []string
	ChangeSources []string

	CreatedFrom *int64
	CreatedTo   *int64

	FieldName *string

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

func NewChangeLogFilter() *ChangeLogFilter {
	return &ChangeLogFilter{
		ChangeTypes:   []string{},
		ChangeSources: []string{},
		SortBy:        "created_at",
		SortOrder:     "DESC",
		Limit:         50,
		Offset:        0,
	}
}
