package port

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"gorm.io/gorm"
)

type ITaggedItemPort interface {
	CreateInBatch(ctx context.Context, tx *gorm.DB, taggedItems []*entity.TaggedItemEntity) error
	DeleteByTagAndResource(ctx context.Context, tx *gorm.DB, tagID int64, resourceType enum.ResourceType, resourceID int64) error
	GetTagsByResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error)
}
