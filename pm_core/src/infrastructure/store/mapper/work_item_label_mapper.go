/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkItemLabelMapper struct{}

func NewWorkItemLabelMapper() *WorkItemLabelMapper {
	return &WorkItemLabelMapper{}
}

func (m *WorkItemLabelMapper) ToEntity(mdl *model.WorkItemLabelModel) *entity.WorkItemLabelEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkItemLabelEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkItemID: mdl.WorkItemID,
		LabelID:    mdl.LabelID,
	}
}

func (m *WorkItemLabelMapper) ToModel(e *entity.WorkItemLabelEntity) *model.WorkItemLabelModel {
	if e == nil {
		return nil
	}

	return &model.WorkItemLabelModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkItemID: e.WorkItemID,
		LabelID:    e.LabelID,
	}
}

func (m *WorkItemLabelMapper) ToEntities(models []*model.WorkItemLabelModel) []*entity.WorkItemLabelEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkItemLabelEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
