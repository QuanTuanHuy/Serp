/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

type ProjectMapper struct{}

func NewProjectMapper() *ProjectMapper {
	return &ProjectMapper{}
}

func (m *ProjectMapper) ToEntity(model *model.ProjectModel) *entity.ProjectEntity {
	if model == nil {
		return nil
	}

	return &entity.ProjectEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		UserID:   model.UserID,
		TenantID: model.TenantID,

		Title:       model.Title,
		Description: model.Description,

		Status:   model.Status,
		Priority: model.Priority,

		StartDateMs: model.StartDateMs,
		DeadlineMs:  model.DeadlineMs,

		ProgressPercentage: model.ProgressPercentage,

		Color:      model.Color,
		Icon:       model.Icon,
		IsFavorite: model.IsFavorite,

		TotalTasks:     model.TotalTasks,
		CompletedTasks: model.CompletedTasks,
		EstimatedHours: model.EstimatedHours,
		ActualHours:    model.ActualHours,
	}
}

func (m *ProjectMapper) ToModel(entity *entity.ProjectEntity) *model.ProjectModel {
	if entity == nil {
		return nil
	}

	projectModel := &model.ProjectModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		UserID:   entity.UserID,
		TenantID: entity.TenantID,

		Title:       entity.Title,
		Description: entity.Description,

		Status:   entity.Status,
		Priority: entity.Priority,

		StartDateMs: entity.StartDateMs,
		DeadlineMs:  entity.DeadlineMs,

		ProgressPercentage: entity.ProgressPercentage,

		Color:      entity.Color,
		Icon:       entity.Icon,
		IsFavorite: entity.IsFavorite,

		TotalTasks:     entity.TotalTasks,
		CompletedTasks: entity.CompletedTasks,
		EstimatedHours: entity.EstimatedHours,
		ActualHours:    entity.ActualHours,
	}

	return projectModel
}

func (m *ProjectMapper) ToEntities(models []*model.ProjectModel) []*entity.ProjectEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.ProjectEntity, 0, len(models))
	for _, model := range models {
		if entity := m.ToEntity(model); entity != nil {
			entities = append(entities, entity)
		}
	}

	return entities
}

func (m *ProjectMapper) ToModels(entities []*entity.ProjectEntity) []*model.ProjectModel {
	if entities == nil {
		return nil
	}

	models := make([]*model.ProjectModel, 0, len(entities))
	for _, entity := range entities {
		if model := m.ToModel(entity); model != nil {
			models = append(models, model)
		}
	}

	return models
}
