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

type IActivityLogPort interface {
	CreateLog(ctx context.Context, tx *gorm.DB, log *entity.ActivityLogEntity) (*entity.ActivityLogEntity, error)
	GetLogsByProjectID(ctx context.Context, projectID int64) ([]*entity.ActivityLogEntity, error)
	GetLogsByWorkItemID(ctx context.Context, workItemID int64) ([]*entity.ActivityLogEntity, error)
}
