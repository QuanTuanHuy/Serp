/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/infrastructure/store/model"
)

func ToFailedEventModel(e *entity.FailedEventEntity) *model.FailedEventModel {
	if e == nil {
		return nil
	}
	return &model.FailedEventModel{
		BaseModel:    model.BaseModel{ID: e.ID},
		EventID:      e.EventID,
		EventType:    e.EventType,
		Topic:        e.Topic,
		MessageKey:   e.MessageKey,
		MessageValue: e.MessageValue,
		RetryCount:   e.RetryCount,
		LastError:    e.LastError,
		Status:       e.Status,
	}
}

func ToFailedEventEntity(mo *model.FailedEventModel) *entity.FailedEventEntity {
	if mo == nil {
		return nil
	}
	return &entity.FailedEventEntity{
		BaseEntity:   entity.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		EventID:      mo.EventID,
		EventType:    mo.EventType,
		Topic:        mo.Topic,
		MessageKey:   mo.MessageKey,
		MessageValue: mo.MessageValue,
		RetryCount:   mo.RetryCount,
		LastError:    mo.LastError,
		Status:       mo.Status,
	}
}

func ToFailedEventEntities(list []*model.FailedEventModel) []*entity.FailedEventEntity {
	out := make([]*entity.FailedEventEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToFailedEventEntity(it))
	}
	return out
}

func ToFailedEventModels(list []*entity.FailedEventEntity) []*model.FailedEventModel {
	out := make([]*model.FailedEventModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToFailedEventModel(it))
	}
	return out
}
