/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkItemAssignmentMapper struct{}

func NewWorkItemAssignmentMapper() *WorkItemAssignmentMapper {
	return &WorkItemAssignmentMapper{}
}

func (m *WorkItemAssignmentMapper) ToEntity(mdl *model.WorkItemAssignmentModel) *entity.WorkItemAssignmentEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkItemAssignmentEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		WorkItemID: mdl.WorkItemID,
		UserID:     mdl.UserID,
	}
}

func (m *WorkItemAssignmentMapper) ToModel(e *entity.WorkItemAssignmentEntity) *model.WorkItemAssignmentModel {
	if e == nil {
		return nil
	}

	return &model.WorkItemAssignmentModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		WorkItemID: e.WorkItemID,
		UserID:     e.UserID,
	}
}

func (m *WorkItemAssignmentMapper) ToEntities(models []*model.WorkItemAssignmentModel) []*entity.WorkItemAssignmentEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkItemAssignmentEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
