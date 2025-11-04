/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	port "github.com/serp/ptm-task/src/core/port/store"
	"github.com/serp/ptm-task/src/infrastructure/store/mapper"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type TaggedItemStoreAdapter struct {
	db *gorm.DB
}

func (a *TaggedItemStoreAdapter) CreateInBatch(ctx context.Context, tx *gorm.DB, taggedItems []*entity.TaggedItemEntity) error {
	models := mapper.ToTaggedItemModelList(taggedItems)
	if len(models) == 0 {
		return nil
	}
	return tx.WithContext(ctx).Create(&models).Error
}

func (a *TaggedItemStoreAdapter) DeleteByTagAndResource(ctx context.Context, tx *gorm.DB, tagID int64, resourceType enum.ResourceType, resourceID int64) error {
	return tx.WithContext(ctx).
		Where("tag_id = ? AND resource_type = ? AND resource_id = ?", tagID, string(resourceType), resourceID).
		Delete(&model.TaggedItemModel{}).Error
}

func (a *TaggedItemStoreAdapter) GetTagsByResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error) {
	var tagModels []*model.TagModel
	if err := a.db.WithContext(ctx).
		Table("tags AS t").
		Joins("JOIN tagged_items ti ON ti.tag_id = t.id").
		Where("ti.resource_type = ? AND ti.resource_id = ? AND t.user_id = ?", string(resourceType), resourceID, userID).
		Find(&tagModels).Error; err != nil {
		return nil, err
	}
	return mapper.ToTagEntityList(tagModels), nil
}

func NewTaggedItemStoreAdapter(db *gorm.DB) port.ITaggedItemPort {
	return &TaggedItemStoreAdapter{db: db}
}
