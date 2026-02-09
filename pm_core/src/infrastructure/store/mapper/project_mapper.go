/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type ProjectMapper struct{}

func NewProjectMapper() *ProjectMapper {
	return &ProjectMapper{}
}

func (m *ProjectMapper) ToEntity(mdl *model.ProjectModel) *entity.ProjectEntity {
	if mdl == nil {
		return nil
	}

	return &entity.ProjectEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		TenantID:           mdl.TenantID,
		Name:               mdl.Name,
		Key:                mdl.Key,
		Description:        mdl.Description,
		Status:             mdl.Status,
		ActiveStatus:       mdl.ActiveStatus,
		Visibility:         mdl.Visibility,
		MethodologyType:    mdl.MethodologyType,
		Priority:           mdl.Priority,
		StartDateMs:        mdl.StartDateMs,
		TargetEndDateMs:    mdl.TargetEndDateMs,
		Color:              mdl.Color,
		Icon:               mdl.Icon,
		DefaultBoardID:     mdl.DefaultBoardID,
		DefaultWorkflowID:  mdl.DefaultWorkflowID,
		NextItemNumber:     mdl.NextItemNumber,
		TotalWorkItems:     mdl.TotalWorkItems,
		CompletedWorkItems: mdl.CompletedWorkItems,
		ProgressPercentage: mdl.ProgressPercentage,
		TotalMembers:       mdl.TotalMembers,
		CreatedBy:          mdl.CreatedBy,
	}
}

func (m *ProjectMapper) ToModel(e *entity.ProjectEntity) *model.ProjectModel {
	if e == nil {
		return nil
	}

	return &model.ProjectModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		TenantID:           e.TenantID,
		Name:               e.Name,
		Key:                e.Key,
		Description:        e.Description,
		Status:             e.Status,
		ActiveStatus:       e.ActiveStatus,
		Visibility:         e.Visibility,
		MethodologyType:    e.MethodologyType,
		Priority:           e.Priority,
		StartDateMs:        e.StartDateMs,
		TargetEndDateMs:    e.TargetEndDateMs,
		Color:              e.Color,
		Icon:               e.Icon,
		DefaultBoardID:     e.DefaultBoardID,
		DefaultWorkflowID:  e.DefaultWorkflowID,
		NextItemNumber:     e.NextItemNumber,
		TotalWorkItems:     e.TotalWorkItems,
		CompletedWorkItems: e.CompletedWorkItems,
		ProgressPercentage: e.ProgressPercentage,
		TotalMembers:       e.TotalMembers,
		CreatedBy:          e.CreatedBy,
	}
}

func (m *ProjectMapper) ToEntities(models []*model.ProjectModel) []*entity.ProjectEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.ProjectEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
