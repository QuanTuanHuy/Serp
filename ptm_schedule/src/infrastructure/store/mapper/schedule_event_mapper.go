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

func ToScheduleEventModel(e *dom.ScheduleEventEntity) *m.ScheduleEventModel {
	if e == nil {
		return nil
	}
	return &m.ScheduleEventModel{
		BaseModel:      m.BaseModel{ID: e.ID},
		SchedulePlanID: e.SchedulePlanID,
		ScheduleTaskID: e.ScheduleTaskID,
		Date:           DayStartUTC(e.DateMs),
		StartMin:       e.StartMin,
		EndMin:         e.EndMin,
		Title:          e.Title,
		PartIndex:      e.PartIndex,
		TotalParts:     e.TotalParts,
		LinkedEventID:  e.LinkedEventID,
		Status:         string(e.Status),
		IsPinned:       e.IsPinned,
		UtilityScore:   e.UtilityScore,
		ActualStartMin: e.ActualStartMin,
		ActualEndMin:   e.ActualEndMin,
	}
}

func ToScheduleEventEntity(mo *m.ScheduleEventModel) *dom.ScheduleEventEntity {
	if mo == nil {
		return nil
	}
	return &dom.ScheduleEventEntity{
		BaseEntity:     dom.BaseEntity{ID: mo.ID, CreatedAt: TimeMsFromTime(mo.CreatedAt), UpdatedAt: TimeMsFromTime(mo.UpdatedAt)},
		SchedulePlanID: mo.SchedulePlanID,
		ScheduleTaskID: mo.ScheduleTaskID,
		DateMs:         DateMsFromDate(mo.Date),
		StartMin:       mo.StartMin,
		EndMin:         mo.EndMin,
		Title:          mo.Title,
		PartIndex:      mo.PartIndex,
		TotalParts:     mo.TotalParts,
		LinkedEventID:  mo.LinkedEventID,
		Status:         en.ScheduleEventStatus(mo.Status),
		IsPinned:       mo.IsPinned,
		UtilityScore:   mo.UtilityScore,
		ActualStartMin: mo.ActualStartMin,
		ActualEndMin:   mo.ActualEndMin,
	}
}

func ToScheduleEventEntities(list []*m.ScheduleEventModel) []*dom.ScheduleEventEntity {
	out := make([]*dom.ScheduleEventEntity, 0, len(list))
	for _, it := range list {
		out = append(out, ToScheduleEventEntity(it))
	}
	return out
}

func ToScheduleEventModels(list []*dom.ScheduleEventEntity) []*m.ScheduleEventModel {
	out := make([]*m.ScheduleEventModel, 0, len(list))
	for _, it := range list {
		out = append(out, ToScheduleEventModel(it))
	}
	return out
}
