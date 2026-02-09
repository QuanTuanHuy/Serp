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

type IMilestonePort interface {
	CreateMilestone(ctx context.Context, tx *gorm.DB, milestone *entity.MilestoneEntity) (*entity.MilestoneEntity, error)
	GetMilestoneByID(ctx context.Context, id int64) (*entity.MilestoneEntity, error)
	GetMilestonesByProjectID(ctx context.Context, projectID int64) ([]*entity.MilestoneEntity, error)
	UpdateMilestone(ctx context.Context, tx *gorm.DB, milestone *entity.MilestoneEntity) error
	SoftDeleteMilestone(ctx context.Context, tx *gorm.DB, milestoneID int64) error
}
