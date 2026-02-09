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

type ILabelPort interface {
	CreateLabel(ctx context.Context, tx *gorm.DB, label *entity.LabelEntity) (*entity.LabelEntity, error)
	GetLabelByID(ctx context.Context, id int64) (*entity.LabelEntity, error)
	GetLabelsByProjectID(ctx context.Context, projectID int64) ([]*entity.LabelEntity, error)
	UpdateLabel(ctx context.Context, tx *gorm.DB, label *entity.LabelEntity) error
	SoftDeleteLabel(ctx context.Context, tx *gorm.DB, labelID int64) error
}
