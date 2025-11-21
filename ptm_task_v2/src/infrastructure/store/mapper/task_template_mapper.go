/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/datatypes"
)

type TaskTemplateMapper struct {
	*BaseMapper
}

func NewTaskTemplateMapper() *TaskTemplateMapper {
	return &TaskTemplateMapper{
		BaseMapper: NewBaseMapper(),
	}
}

func (m *TaskTemplateMapper) ToModel(entity *entity.TaskTemplateEntity) *model.TaskTemplateModel {
	if entity == nil {
		return nil
	}

	modelData := &model.TaskTemplateModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		UserID:               entity.UserID,
		TenantID:             entity.TenantID,
		TemplateName:         entity.TemplateName,
		Description:          entity.Description,
		TitleTemplate:        entity.TitleTemplate,
		EstimatedDurationMin: entity.EstimatedDurationMin,
		Priority:             entity.Priority,
		Category:             entity.Category,
		IsDeepWork:           entity.IsDeepWork,
		PreferredTimeOfDay:   entity.PreferredTimeOfDay,
		RecurrencePattern:    entity.RecurrencePattern,
		RecurrenceConfig:     entity.RecurrenceConfig,
		UsageCount:           entity.UsageCount,
		LastUsedAt:           entity.LastUsedAt,
		IsFavorite:           entity.IsFavorite,
		ActiveStatus:         entity.ActiveStatus,
	}

	if len(entity.Tags) > 0 {
		tagsMap := make(datatypes.JSONMap)
		for _, tag := range entity.Tags {
			tagsMap[tag] = true
		}
		modelData.Tags = tagsMap
	}

	if len(entity.PreferredDays) > 0 {
		daysMap := make(datatypes.JSONMap)
		for i, day := range entity.PreferredDays {
			daysMap[string(rune('0'+i))] = day
		}
		modelData.PreferredDays = daysMap
	}

	return modelData
}

func (m *TaskTemplateMapper) ToEntity(model *model.TaskTemplateModel) *entity.TaskTemplateEntity {
	if model == nil {
		return nil
	}

	entity := &entity.TaskTemplateEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		UserID:               model.UserID,
		TenantID:             model.TenantID,
		TemplateName:         model.TemplateName,
		Description:          model.Description,
		TitleTemplate:        model.TitleTemplate,
		EstimatedDurationMin: model.EstimatedDurationMin,
		Priority:             model.Priority,
		Category:             model.Category,
		IsDeepWork:           model.IsDeepWork,
		PreferredTimeOfDay:   model.PreferredTimeOfDay,
		RecurrencePattern:    model.RecurrencePattern,
		RecurrenceConfig:     model.RecurrenceConfig,
		UsageCount:           model.UsageCount,
		LastUsedAt:           model.LastUsedAt,
		IsFavorite:           model.IsFavorite,
		ActiveStatus:         model.ActiveStatus,
	}

	if len(model.Tags) > 0 {
		tags := make([]string, 0, len(model.Tags))
		for tag := range model.Tags {
			tags = append(tags, tag)
		}
		entity.Tags = tags
	}

	if len(model.PreferredDays) > 0 {
		days := make([]int, 0, len(model.PreferredDays))
		for _, day := range model.PreferredDays {
			if dayInt, ok := day.(int); ok {
				days = append(days, dayInt)
			} else if dayFloat, ok := day.(float64); ok {
				days = append(days, int(dayFloat))
			}
		}
		entity.PreferredDays = days
	}

	return entity
}

func (m *TaskTemplateMapper) ToEntities(models []*model.TaskTemplateModel) []*entity.TaskTemplateEntity {
	entities := make([]*entity.TaskTemplateEntity, 0, len(models))
	for _, model := range models {
		entities = append(entities, m.ToEntity(model))
	}
	return entities
}

func (m *TaskTemplateMapper) ToModels(entities []*entity.TaskTemplateEntity) []*model.TaskTemplateModel {
	models := make([]*model.TaskTemplateModel, 0, len(entities))
	for _, entity := range entities {
		models = append(models, m.ToModel(entity))
	}
	return models
}
