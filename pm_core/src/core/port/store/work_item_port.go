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

type IWorkItemPort interface {
	CreateWorkItem(ctx context.Context, tx *gorm.DB, workItem *entity.WorkItemEntity) (*entity.WorkItemEntity, error)
	GetWorkItemByID(ctx context.Context, id int64) (*entity.WorkItemEntity, error)
	UpdateWorkItem(ctx context.Context, tx *gorm.DB, workItem *entity.WorkItemEntity) error
	SoftDeleteWorkItem(ctx context.Context, tx *gorm.DB, workItemID int64) error
}
