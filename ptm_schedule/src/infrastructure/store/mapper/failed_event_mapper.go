/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToFailedEventModel(e *dom.FailedEventEntity) *m.FailedEventModel {
	if e == nil {
		return nil
	}
	return &m.FailedEventModel{
		BaseModel:    m.BaseModel{ID: e.ID},
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

func ToFailedEventEntity(mo *m.FailedEventModel) *dom.FailedEventEntity {
	if mo == nil {
		return nil
	}
	return &dom.FailedEventEntity{
		BaseEntity:   dom.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
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

func ToFailedEventEntities(list []*m.FailedEventModel) []*dom.FailedEventEntity {
	out := make([]*dom.FailedEventEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToFailedEventEntity(it))
	}
	return out
}

func ToFailedEventModels(list []*dom.FailedEventEntity) []*m.FailedEventModel {
	out := make([]*m.FailedEventModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToFailedEventModel(it))
	}
	return out
}
