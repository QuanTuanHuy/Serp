/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func ToTagModel(tag *entity.TagEntity) *model.TagModel {
	if tag == nil {
		return nil
	}

	return &model.TagModel{
		BaseModel: model.BaseModel{
			ID: tag.ID,
		},
		Name:         tag.Name,
		Color:        tag.Color,
		Weight:       utils.Float64Value(tag.Weight),
		ActiveStatus: string(tag.ActiveStatus),
		UserID:       tag.UserID,
	}
}

func ToTagEntity(m *model.TagModel) *entity.TagEntity {
	if m == nil {
		return nil
	}

	return &entity.TagEntity{
		BaseEntity: entity.BaseEntity{
			ID:        m.ID,
			CreatedAt: m.CreatedAt.UnixMilli(),
			UpdatedAt: m.UpdatedAt.UnixMilli(),
		},
		Name:         m.Name,
		Color:        m.Color,
		Weight:       utils.Float64Ptr(m.Weight),
		ActiveStatus: enum.ActiveStatus(m.ActiveStatus),
		UserID:       m.UserID,
	}
}

func ToTagEntityList(ms []*model.TagModel) []*entity.TagEntity {
	if ms == nil {
		return nil
	}
	tags := make([]*entity.TagEntity, len(ms))
	for i, m := range ms {
		tags[i] = ToTagEntity(m)
	}
	return tags
}

func ToTagModelList(tags []*entity.TagEntity) []*model.TagModel {
	if tags == nil {
		return nil
	}
	ms := make([]*model.TagModel, len(tags))
	for i, tag := range tags {
		ms[i] = ToTagModel(tag)
	}
	return ms
}
