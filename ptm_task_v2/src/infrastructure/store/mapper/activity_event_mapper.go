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

// ToModel converts ActivityEventEntity to ActivityEventModel
func (m *ActivityEventMapper) ToModel(entity *entity.ActivityEventEntity) *model.ActivityEventModel {
	if entity == nil {
		return nil
	}

	modelData := &model.ActivityEventModel{
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

	if entity.CreatedAt > 0 {
		modelData.CreatedAt = m.UnixMilliToTime(entity.CreatedAt)
	}
	if entity.UpdatedAt > 0 {
		modelData.UpdatedAt = m.UnixMilliToTime(entity.UpdatedAt)
	}

	return modelData
}

// ToEntity converts ActivityEventModel to ActivityEventEntity
func (m *ActivityEventMapper) ToEntity(model *model.ActivityEventModel) *entity.ActivityEventEntity {
	if model == nil {
		return nil
	}

	entity := &entity.ActivityEventEntity{
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

	entity.ID = model.ID
	entity.CreatedAt = m.TimeToUnixMilli(model.CreatedAt)
	entity.UpdatedAt = m.TimeToUnixMilli(model.UpdatedAt)

	return entity
}

// ToEntities converts slice of ActivityEventModel to slice of ActivityEventEntity
func (m *ActivityEventMapper) ToEntities(models []*model.ActivityEventModel) []*entity.ActivityEventEntity {
	entities := make([]*entity.ActivityEventEntity, 0, len(models))
	for _, model := range models {
		entities = append(entities, m.ToEntity(model))
	}
	return entities
}

// ToModels converts slice of ActivityEventEntity to slice of ActivityEventModel
func (m *ActivityEventMapper) ToModels(entities []*entity.ActivityEventEntity) []*model.ActivityEventModel {
	models := make([]*model.ActivityEventModel, 0, len(entities))
	for _, entity := range entities {
		models = append(models, m.ToModel(entity))
	}
	return models
}
