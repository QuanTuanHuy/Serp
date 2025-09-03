/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type IGroupTaskPort interface {
	CreateGroupTask(ctx context.Context, tx *gorm.DB, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error)
	UpdateGroupTask(ctx context.Context, tx *gorm.DB, groupTaskID int64, groupTask *entity.GroupTaskEntity) (*entity.GroupTaskEntity, error)
	GetDefaultGroupTaskByProjectID(ctx context.Context, projectID int64) (*entity.GroupTaskEntity, error)
	GetGroupTasksByProjectID(ctx context.Context, projectID int64) ([]*entity.GroupTaskEntity, error)
	GetGroupTaskByID(ctx context.Context, groupTaskID int64) (*entity.GroupTaskEntity, error)
}
