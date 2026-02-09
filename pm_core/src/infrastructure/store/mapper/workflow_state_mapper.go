/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkflowStateMapper struct{}

func NewWorkflowStateMapper() *WorkflowStateMapper {
	return &WorkflowStateMapper{}
}

func (m *WorkflowStateMapper) ToEntity(mdl *model.WorkflowStateModel) *entity.WorkflowStateEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkflowStateEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkflowID:   mdl.WorkflowID,
		Name:         mdl.Name,
		Category:     mdl.Category,
		StateOrder:   mdl.StateOrder,
		Color:        mdl.Color,
		ActiveStatus: mdl.ActiveStatus,
	}
}

func (m *WorkflowStateMapper) ToModel(e *entity.WorkflowStateEntity) *model.WorkflowStateModel {
	if e == nil {
		return nil
	}

	return &model.WorkflowStateModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkflowID:   e.WorkflowID,
		Name:         e.Name,
		Category:     e.Category,
		StateOrder:   e.StateOrder,
		Color:        e.Color,
		ActiveStatus: e.ActiveStatus,
	}
}

func (m *WorkflowStateMapper) ToEntities(models []*model.WorkflowStateModel) []*entity.WorkflowStateEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkflowStateEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
