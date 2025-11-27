/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	dom "github.com/serp/ptm-schedule/src/core/domain/entity"
	m "github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToScheduleWindowModel(e *dom.ScheduleWindowEntity) *m.ScheduleWindowModel {
	if e == nil {
		return nil
	}
	return &m.ScheduleWindowModel{
		BaseModel: m.BaseModel{ID: e.ID},
		UserID:    e.UserID,
		Date:      DayStartUTC(e.DateMs),
		StartMin:  e.StartMin,
		EndMin:    e.EndMin,
	}
}

func ToScheduleWindowEntity(mo *m.ScheduleWindowModel) *dom.ScheduleWindowEntity {
	if mo == nil {
		return nil
	}
	return &dom.ScheduleWindowEntity{
		BaseEntity: dom.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		UserID:     mo.UserID,
		DateMs:     DateMsFromDate(mo.Date),
		StartMin:   mo.StartMin,
		EndMin:     mo.EndMin,
	}
}

func ToScheduleWindowEntities(list []*m.ScheduleWindowModel) []*dom.ScheduleWindowEntity {
	out := make([]*dom.ScheduleWindowEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToScheduleWindowEntity(it))
	}
	return out
}

func ToScheduleWindowModels(list []*dom.ScheduleWindowEntity) []*m.ScheduleWindowModel {
	out := make([]*m.ScheduleWindowModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToScheduleWindowModel(it))
	}
	return out
}
