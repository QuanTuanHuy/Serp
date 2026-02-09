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

type IWorkItemDependencyPort interface {
	CreateDependency(ctx context.Context, tx *gorm.DB, dependency *entity.WorkItemDependencyEntity) (*entity.WorkItemDependencyEntity, error)
	GetDependenciesByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemDependencyEntity, error)
	DeleteDependency(ctx context.Context, tx *gorm.DB, dependencyID int64) error
}
