/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	port "github.com/serp/ptm-task/src/core/port/store"
	"gorm.io/gorm"
)

type ITaggedItemService interface {
	CreateTaggedItem(ctx context.Context, tx *gorm.DB, item *entity.TaggedItemEntity) error
	CreateTaggedItems(ctx context.Context, tx *gorm.DB, items []*entity.TaggedItemEntity) error
	DeleteTagFromResource(ctx context.Context, tx *gorm.DB, tagID int64, resourceType enum.ResourceType, resourceID int64) error
	GetTagsForResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error)
}

type TaggedItemService struct {
	port port.ITaggedItemPort
}

func (s *TaggedItemService) CreateTaggedItem(ctx context.Context, tx *gorm.DB, item *entity.TaggedItemEntity) error {
	if item == nil {
		return nil
	}
	return s.CreateTaggedItems(ctx, tx, []*entity.TaggedItemEntity{item})
}

func (s *TaggedItemService) CreateTaggedItems(ctx context.Context, tx *gorm.DB, items []*entity.TaggedItemEntity) error {
	if len(items) == 0 {
		return nil
	}
	if err := s.port.CreateInBatch(ctx, tx, items); err != nil {
		log.Error(ctx, "Failed to create tagged items: ", err)
		return err
	}
	return nil
}

func (s *TaggedItemService) DeleteTagFromResource(ctx context.Context, tx *gorm.DB, tagID int64, resourceType enum.ResourceType, resourceID int64) error {
	if err := s.port.DeleteByTagAndResource(ctx, tx, tagID, resourceType, resourceID); err != nil {
		log.Error(ctx, "Failed to delete tag from resource: ", err)
		return err
	}
	return nil
}

func (s *TaggedItemService) GetTagsForResource(ctx context.Context, userID int64, resourceType enum.ResourceType, resourceID int64) ([]*entity.TagEntity, error) {
	tags, err := s.port.GetTagsByResource(ctx, userID, resourceType, resourceID)
	if err != nil {
		log.Error(ctx, "Failed to get tags for resource: ", err)
		return nil, err
	}
	return tags, nil
}

func NewTaggedItemService(p port.ITaggedItemPort) ITaggedItemService {
	return &TaggedItemService{port: p}
}
