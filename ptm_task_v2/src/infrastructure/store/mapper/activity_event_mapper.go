/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

type ActivityEventMapper struct {
	*BaseMapper
}

func NewActivityEventMapper() *ActivityEventMapper {
	return &ActivityEventMapper{
		BaseMapper: NewBaseMapper(),
	}
}

func (m *ActivityEventMapper) ToModel(entity *entity.ActivityEventEntity) *model.ActivityEventModel {
	if entity == nil {
		return nil
	}

	return &model.ActivityEventModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		UserID:           entity.UserID,
		TenantID:         entity.TenantID,
		EventType:        entity.EventType,
		EntityType:       entity.EntityType,
		EntityID:         entity.EntityID,
		Title:            entity.Title,
		Description:      entity.Description,
		Metadata:         entity.Metadata,
		NavigationURL:    entity.NavigationURL,
		NavigationParams: entity.NavigationParams,
	}
}

func (m *ActivityEventMapper) ToEntity(model *model.ActivityEventModel) *entity.ActivityEventEntity {
	if model == nil {
		return nil
	}

	return &entity.ActivityEventEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		UserID:           model.UserID,
		TenantID:         model.TenantID,
		EventType:        model.EventType,
		EntityType:       model.EntityType,
		EntityID:         model.EntityID,
		Title:            model.Title,
		Description:      model.Description,
		Metadata:         model.Metadata,
		NavigationURL:    model.NavigationURL,
		NavigationParams: model.NavigationParams,
	}
}

func (m *ActivityEventMapper) ToEntities(models []*model.ActivityEventModel) []*entity.ActivityEventEntity {
	entities := make([]*entity.ActivityEventEntity, 0, len(models))
	for _, model := range models {
		entities = append(entities, m.ToEntity(model))
	}
	return entities
}

func (m *ActivityEventMapper) ToModels(entities []*entity.ActivityEventEntity) []*model.ActivityEventModel {
	models := make([]*model.ActivityEventModel, 0, len(entities))
	for _, entity := range entities {
		models = append(models, m.ToModel(entity))
	}
	return models
}
