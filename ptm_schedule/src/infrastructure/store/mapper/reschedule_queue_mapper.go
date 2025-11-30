/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

type rescheduleQueueMapper struct{}

var RescheduleQueueMapper = &rescheduleQueueMapper{}

func (m *rescheduleQueueMapper) ToEntity(mo *model.RescheduleQueueModel) *entity.RescheduleQueueItem {
	if mo == nil {
		return nil
	}
	e := &entity.RescheduleQueueItem{
		BaseEntity: entity.BaseEntity{
			ID:        mo.ID,
			CreatedAt: mo.CreatedAt.UnixMilli(),
			UpdatedAt: mo.UpdatedAt.UnixMilli(),
		},
		UserID:               mo.UserID,
		SchedulePlanID:       mo.SchedulePlanID,
		TriggerType:          enum.TriggerType(mo.TriggerType),
		EntityID:             mo.EntityID,
		EntityType:           mo.EntityType,
		ChangePayload:        mo.ChangePayload,
		Status:               enum.QueueStatus(mo.Status),
		Priority:             mo.Priority,
		DebounceUntil:        mo.DebounceUntil.UnixMilli(),
		FirstCreatedAt:       mo.FirstCreatedAt.UnixMilli(),
		ProcessingDurationMs: mo.ProcessingDurationMs,
		ErrorMessage:         mo.ErrorMessage,
		RetryCount:           mo.RetryCount,
	}
	if mo.ProcessedAt != nil {
		processedAt := mo.ProcessedAt.UnixMilli()
		e.ProcessedAt = &processedAt
	}
	return e
}

func (m *rescheduleQueueMapper) ToModel(e *entity.RescheduleQueueItem) *model.RescheduleQueueModel {
	if e == nil {
		return nil
	}
	mo := &model.RescheduleQueueModel{
		BaseModel: model.BaseModel{
			ID:        e.ID,
			CreatedAt: time.UnixMilli(e.CreatedAt),
			UpdatedAt: time.UnixMilli(e.UpdatedAt),
		},
		UserID:               e.UserID,
		SchedulePlanID:       e.SchedulePlanID,
		TriggerType:          string(e.TriggerType),
		EntityID:             e.EntityID,
		EntityType:           e.EntityType,
		ChangePayload:        e.ChangePayload,
		Status:               string(e.Status),
		Priority:             e.Priority,
		DebounceUntil:        time.UnixMilli(e.DebounceUntil),
		FirstCreatedAt:       time.UnixMilli(e.FirstCreatedAt),
		ProcessingDurationMs: e.ProcessingDurationMs,
		ErrorMessage:         e.ErrorMessage,
		RetryCount:           e.RetryCount,
	}
	if e.ProcessedAt != nil {
		processedAt := time.UnixMilli(*e.ProcessedAt)
		mo.ProcessedAt = &processedAt
	}
	return mo
}

func (m *rescheduleQueueMapper) ToEntities(models []*model.RescheduleQueueModel) []*entity.RescheduleQueueItem {
	entities := make([]*entity.RescheduleQueueItem, len(models))
	for i, mo := range models {
		entities[i] = m.ToEntity(mo)
	}
	return entities
}
