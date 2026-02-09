/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkflowDefinitionMapper struct{}

func NewWorkflowDefinitionMapper() *WorkflowDefinitionMapper {
	return &WorkflowDefinitionMapper{}
}

func (m *WorkflowDefinitionMapper) ToEntity(mdl *model.WorkflowDefinitionModel) *entity.WorkflowDefinitionEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkflowDefinitionEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:    mdl.ProjectID,
		Name:         mdl.Name,
		IsDefault:    mdl.IsDefault,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *WorkflowDefinitionMapper) ToModel(e *entity.WorkflowDefinitionEntity) *model.WorkflowDefinitionModel {
	if e == nil {
		return nil
	}

	return &model.WorkflowDefinitionModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:    e.ProjectID,
		Name:         e.Name,
		IsDefault:    e.IsDefault,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *WorkflowDefinitionMapper) ToEntities(models []*model.WorkflowDefinitionModel) []*entity.WorkflowDefinitionEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkflowDefinitionEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
