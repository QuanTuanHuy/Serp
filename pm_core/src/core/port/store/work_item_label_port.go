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

type IWorkItemLabelPort interface {
	CreateWorkItemLabel(ctx context.Context, tx *gorm.DB, wil *entity.WorkItemLabelEntity) (*entity.WorkItemLabelEntity, error)
	GetLabelsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.WorkItemLabelEntity, error)
	DeleteWorkItemLabel(ctx context.Context, tx *gorm.DB, workItemLabelID int64) error
}
