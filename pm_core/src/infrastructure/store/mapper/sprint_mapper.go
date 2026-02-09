/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type SprintMapper struct{}

func NewSprintMapper() *SprintMapper {
	return &SprintMapper{}
}

func (m *SprintMapper) ToEntity(mdl *model.SprintModel) *entity.SprintEntity {
	if mdl == nil {
		return nil
	}

	return &entity.SprintEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:    mdl.ProjectID,
		Name:         mdl.Name,
		Goal:         mdl.Goal,
		Status:       mdl.Status,
		StartDateMs:  mdl.StartDateMs,
		EndDateMs:    mdl.EndDateMs,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *SprintMapper) ToModel(e *entity.SprintEntity) *model.SprintModel {
	if e == nil {
		return nil
	}

	return &model.SprintModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:    e.ProjectID,
		Name:         e.Name,
		Goal:         e.Goal,
		Status:       e.Status,
		StartDateMs:  e.StartDateMs,
		EndDateMs:    e.EndDateMs,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *SprintMapper) ToEntities(models []*model.SprintModel) []*entity.SprintEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.SprintEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
