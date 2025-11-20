/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

type TaskDependencyGraphMapper struct {
	*BaseMapper
}

func NewTaskDependencyGraphMapper() *TaskDependencyGraphMapper {
	return &TaskDependencyGraphMapper{
		BaseMapper: NewBaseMapper(),
	}
}

func (m *TaskDependencyGraphMapper) ToModel(entity *entity.TaskDependencyGraphEntity) *model.TaskDependencyGraphModel {
	if entity == nil {
		return nil
	}

	modelData := &model.TaskDependencyGraphModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		UserID:          entity.UserID,
		TaskID:          entity.TaskID,
		DependsOnTaskID: entity.DependsOnTaskID,
		IsValid:         entity.IsValid,
		ValidationError: entity.ValidationError,
		DependencyDepth: entity.DependencyDepth,
	}

	if entity.CreatedAt > 0 {
		modelData.CreatedAt = m.UnixMilliToTime(entity.CreatedAt)
	}
	if entity.UpdatedAt > 0 {
		modelData.UpdatedAt = m.UnixMilliToTime(entity.UpdatedAt)
	}

	return modelData
}

func (m *TaskDependencyGraphMapper) ToEntity(model *model.TaskDependencyGraphModel) *entity.TaskDependencyGraphEntity {
	if model == nil {
		return nil
	}

	return &entity.TaskDependencyGraphEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		UserID:          model.UserID,
		TaskID:          model.TaskID,
		DependsOnTaskID: model.DependsOnTaskID,
		IsValid:         model.IsValid,
		ValidationError: model.ValidationError,
		DependencyDepth: model.DependencyDepth,
	}
}

func (m *TaskDependencyGraphMapper) ToEntities(models []*model.TaskDependencyGraphModel) []*entity.TaskDependencyGraphEntity {
	entities := make([]*entity.TaskDependencyGraphEntity, 0, len(models))
	for _, model := range models {
		entities = append(entities, m.ToEntity(model))
	}
	return entities
}

func (m *TaskDependencyGraphMapper) ToModels(entities []*entity.TaskDependencyGraphEntity) []*model.TaskDependencyGraphModel {
	models := make([]*model.TaskDependencyGraphModel, 0, len(entities))
	for _, entity := range entities {
		models = append(models, m.ToModel(entity))
	}
	return models
}
