/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	en "github.com/serp/ptm-schedule/src/core/domain/enum"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToAvailabilityCalendarModel(e *dom.AvailabilityCalendarEntity) *m.AvailabilityCalendarModel {
	if e == nil {
		return nil
	}
	return &m.AvailabilityCalendarModel{
		BaseModel:    m.BaseModel{ID: e.ID},
		UserID:       e.UserID,
		DayOfWeek:    e.DayOfWeek,
		StartMin:     e.StartMin,
		EndMin:       e.EndMin,
		ActiveStatus: string(e.ActiveStatus),
	}
}

func ToAvailabilityCalendarEntity(mo *m.AvailabilityCalendarModel) *dom.AvailabilityCalendarEntity {
	if mo == nil {
		return nil
	}
	return &dom.AvailabilityCalendarEntity{
		BaseEntity:   dom.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		UserID:       mo.UserID,
		DayOfWeek:    mo.DayOfWeek,
		StartMin:     mo.StartMin,
		EndMin:       mo.EndMin,
		ActiveStatus: en.ActiveStatus(mo.ActiveStatus),
	}
}

func ToAvailabilityCalendarEntities(list []*m.AvailabilityCalendarModel) []*dom.AvailabilityCalendarEntity {
	out := make([]*dom.AvailabilityCalendarEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToAvailabilityCalendarEntity(it))
	}
	return out
}

func ToAvailabilityCalendarModels(list []*dom.AvailabilityCalendarEntity) []*m.AvailabilityCalendarModel {
	out := make([]*m.AvailabilityCalendarModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToAvailabilityCalendarModel(it))
	}
	return out
}
