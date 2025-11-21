/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/datatypes"
)

type TaskReminderMapper struct {
	*BaseMapper
}

func NewTaskReminderMapper() *TaskReminderMapper {
	return &TaskReminderMapper{
		BaseMapper: NewBaseMapper(),
	}
}

func (m *TaskReminderMapper) ToModel(entity *entity.TaskReminderEntity) *model.TaskReminderModel {
	if entity == nil {
		return nil
	}

	modelData := &model.TaskReminderModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		TaskID:           entity.TaskID,
		UserID:           entity.UserID,
		ReminderType:     entity.ReminderType,
		TriggerTimeMs:    entity.TriggerTimeMs,
		AdvanceNoticeMin: entity.AdvanceNoticeMin,
		IsRecurring:      entity.IsRecurring,
		SnoozeUntilMs:    entity.SnoozeUntilMs,
		MessageTemplate:  entity.MessageTemplate,
		Status:           entity.Status,
		SentAt:           entity.SentAt,
	}

	if len(entity.NotificationChannels) > 0 {
		channelsJSON, _ := json.Marshal(entity.NotificationChannels)
		modelData.NotificationChannels = datatypes.JSON(channelsJSON)
	}

	return modelData
}

func (m *TaskReminderMapper) ToEntity(model *model.TaskReminderModel) *entity.TaskReminderEntity {
	if model == nil {
		return nil
	}

	entity := &entity.TaskReminderEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		TaskID:           model.TaskID,
		UserID:           model.UserID,
		ReminderType:     model.ReminderType,
		TriggerTimeMs:    model.TriggerTimeMs,
		AdvanceNoticeMin: model.AdvanceNoticeMin,
		IsRecurring:      model.IsRecurring,
		SnoozeUntilMs:    model.SnoozeUntilMs,
		MessageTemplate:  model.MessageTemplate,
		Status:           model.Status,
		SentAt:           model.SentAt,
	}

	if len(model.NotificationChannels) > 0 {
		var channels []string
		if err := json.Unmarshal(model.NotificationChannels, &channels); err == nil {
			entity.NotificationChannels = channels
		}
	}

	return entity
}

func (m *TaskReminderMapper) ToEntities(models []*model.TaskReminderModel) []*entity.TaskReminderEntity {
	entities := make([]*entity.TaskReminderEntity, 0, len(models))
	for _, model := range models {
		entities = append(entities, m.ToEntity(model))
	}
	return entities
}

func (m *TaskReminderMapper) ToModels(entities []*entity.TaskReminderEntity) []*model.TaskReminderModel {
	models := make([]*model.TaskReminderModel, 0, len(entities))
	for _, entity := range entities {
		models = append(models, m.ToModel(entity))
	}
	return models
}
