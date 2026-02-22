/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type PriorityMapper struct {
}

func NewPriorityMapper() *PriorityMapper {
	return &PriorityMapper{}
}

func (m *PriorityMapper) ToEntity(mdl *model.PriorityModel) *entity.PriorityEntity {
	if mdl == nil {
		return nil
	}
	return &entity.PriorityEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			CreatedBy: mdl.CreatedBy,
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
			UpdatedBy: mdl.UpdatedBy,
		},
		TenantID:    mdl.TenantID,
		Name:        mdl.Name,
		Description: mdl.Description,
		IconUrl:     mdl.IconUrl,
		Color:       mdl.Color,
		Sequence:    mdl.Sequence,
		IsSystem:    mdl.IsSystem,
	}
}

func (m *PriorityMapper) ToModel(e *entity.PriorityEntity) *model.PriorityModel {
	if e == nil {
		return nil
	}
	return &model.PriorityModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedBy: e.CreatedBy,
			UpdatedBy: e.UpdatedBy,
		},
		TenantID:    e.TenantID,
		Name:        e.Name,
		Description: e.Description,
		IconUrl:     e.IconUrl,
		Color:       e.Color,
		Sequence:    e.Sequence,
		IsSystem:    e.IsSystem,
	}
}

func (m *PriorityMapper) ToEntities(models []*model.PriorityModel) []*entity.PriorityEntity {
	if models == nil {
		return nil
	}
	entities := make([]*entity.PriorityEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
