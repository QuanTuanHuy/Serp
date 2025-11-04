package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITagPort interface {
	CreateTag(ctx context.Context, tx *gorm.DB, tag *entity.TagEntity) (*entity.TagEntity, error)
	UpdateTag(ctx context.Context, tx *gorm.DB, tagID int64, tag *entity.TagEntity) (*entity.TagEntity, error)
	GetTagByID(ctx context.Context, ID int64) (*entity.TagEntity, error)
	GetTagsByUserID(ctx context.Context, userID int64) ([]*entity.TagEntity, error)
	DeleteTag(ctx context.Context, tx *gorm.DB, ID int64) error
	GetTagByUserIDAndName(ctx context.Context, userID int64, name string) (*entity.TagEntity, error)
}
