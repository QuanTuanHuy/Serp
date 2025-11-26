/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToCalendarExceptionModel(e *dom.CalendarExceptionEntity) *m.CalendarExceptionModel {
	if e == nil {
		return nil
	}
	return &m.CalendarExceptionModel{
		BaseModel: m.BaseModel{ID: e.ID},
		UserID:    e.UserID,
		Date:      DayStartUTC(e.DateMs),
		StartMin:  e.StartMin,
		EndMin:    e.EndMin,
		Type:      e.Type,
	}
}

func ToCalendarExceptionEntity(mo *m.CalendarExceptionModel) *dom.CalendarExceptionEntity {
	if mo == nil {
		return nil
	}
	return &dom.CalendarExceptionEntity{
		BaseEntity: dom.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		UserID:     mo.UserID,
		DateMs:     DateMsFromDate(mo.Date),
		StartMin:   mo.StartMin,
		EndMin:     mo.EndMin,
		Type:       mo.Type,
	}
}

func ToCalendarExceptionEntities(list []*m.CalendarExceptionModel) []*dom.CalendarExceptionEntity {
	out := make([]*dom.CalendarExceptionEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToCalendarExceptionEntity(it))
	}
	return out
}

func ToCalendarExceptionModels(list []*dom.CalendarExceptionEntity) []*m.CalendarExceptionModel {
	out := make([]*m.CalendarExceptionModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToCalendarExceptionModel(it))
	}
	return out
}
