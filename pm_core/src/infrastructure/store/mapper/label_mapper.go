/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type LabelMapper struct{}

func NewLabelMapper() *LabelMapper {
	return &LabelMapper{}
}

func (m *LabelMapper) ToEntity(mdl *model.LabelModel) *entity.LabelEntity {
	if mdl == nil {
		return nil
	}

	return &entity.LabelEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:    mdl.ProjectID,
		Name:         mdl.Name,
		Color:        mdl.Color,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *LabelMapper) ToModel(e *entity.LabelEntity) *model.LabelModel {
	if e == nil {
		return nil
	}

	return &model.LabelModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:    e.ProjectID,
		Name:         e.Name,
		Color:        e.Color,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *LabelMapper) ToEntities(models []*model.LabelModel) []*entity.LabelEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.LabelEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
