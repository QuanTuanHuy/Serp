/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

func ToTaggedItemModel(e *entity.TaggedItemEntity) *model.TaggedItemModel {
	if e == nil {
		return nil
	}
	return &model.TaggedItemModel{
		BaseModel:    model.BaseModel{ID: e.ID},
		TagID:        e.TagID,
		ResourceType: string(e.ResourceType),
		ResourceID:   e.ResourceID,
	}
}

func ToTaggedItemEntity(m *model.TaggedItemModel) *entity.TaggedItemEntity {
	if m == nil {
		return nil
	}
	return &entity.TaggedItemEntity{
		BaseEntity: entity.BaseEntity{
			ID:        m.ID,
			CreatedAt: m.CreatedAt.UnixMilli(),
			UpdatedAt: m.UpdatedAt.UnixMilli(),
		},
		TagID:        m.TagID,
		ResourceType: enum.ResourceType(m.ResourceType),
		ResourceID:   m.ResourceID,
	}
}

func ToTaggedItemModelList(es []*entity.TaggedItemEntity) []*model.TaggedItemModel {
	if es == nil {
		return nil
	}
	result := make([]*model.TaggedItemModel, len(es))
	for i, e := range es {
		result[i] = ToTaggedItemModel(e)
	}
	return result
}
