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
		StartDateMs:        mdl.StartDateMs,
		DeadlineMs:         mdl.DeadlineMs,
		NextItemNumber:     mdl.NextItemNumber,
		TotalWorkItems:     mdl.TotalWorkItems,
		CompletedWorkItems: mdl.CompletedWorkItems,
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
		StartDateMs:        e.StartDateMs,
		DeadlineMs:         e.DeadlineMs,
		NextItemNumber:     e.NextItemNumber,
		TotalWorkItems:     e.TotalWorkItems,
		CompletedWorkItems: e.CompletedWorkItems,
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
