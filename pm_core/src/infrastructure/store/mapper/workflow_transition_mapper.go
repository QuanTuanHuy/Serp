/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkflowTransitionMapper struct{}

func NewWorkflowTransitionMapper() *WorkflowTransitionMapper {
	return &WorkflowTransitionMapper{}
}

func (m *WorkflowTransitionMapper) ToEntity(mdl *model.WorkflowTransitionModel) *entity.WorkflowTransitionEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkflowTransitionEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkflowID:   mdl.WorkflowID,
		FromStateID:  mdl.FromStateID,
		ToStateID:    mdl.ToStateID,
		Name:         mdl.Name,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *WorkflowTransitionMapper) ToModel(e *entity.WorkflowTransitionEntity) *model.WorkflowTransitionModel {
	if e == nil {
		return nil
	}

	return &model.WorkflowTransitionModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkflowID:   e.WorkflowID,
		FromStateID:  e.FromStateID,
		ToStateID:    e.ToStateID,
		Name:         e.Name,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *WorkflowTransitionMapper) ToEntities(models []*model.WorkflowTransitionModel) []*entity.WorkflowTransitionEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkflowTransitionEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
