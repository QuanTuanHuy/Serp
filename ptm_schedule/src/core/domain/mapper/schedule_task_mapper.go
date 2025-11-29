/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

func TaskCreatedEventToEntity(event *message.TaskCreatedEvent) *entity.ScheduleTaskEntity {
	return &entity.ScheduleTaskEntity{
		TaskID:           event.TaskID,
		UserID:           event.UserID,
		TenantID:         event.TenantID,
		Title:            event.Title,
		Priority:         enum.Priority(event.Priority),
		Category:         event.Category,
		IsDeepWork:       event.IsDeepWork,
		DurationMin:      *event.EstimatedDurationMin,
		EarliestStartMs:  event.EarliestStartMs,
		DeadlineMs:       event.DeadlineMs,
		PreferredStartMs: event.PreferredStartDateMs,
	}
}

func TaskUpdatedEventToEntity(event *message.TaskUpdatedEvent, scheduleTask *entity.ScheduleTaskEntity) *entity.ScheduleTaskEntity {
	if event.Title != nil {
		scheduleTask.Title = *event.Title
	}
	if event.Priority != nil {
		scheduleTask.Priority = enum.Priority(*event.Priority)
	}
	if event.DeadlineMs != nil {
		scheduleTask.DeadlineMs = event.DeadlineMs
	}
	if event.EstimatedDurationMin != nil {
		scheduleTask.DurationMin = *event.EstimatedDurationMin
	}
	if event.PreferredStartDateMs != nil {
		scheduleTask.PreferredStartMs = event.PreferredStartDateMs
	}
	if event.EarliestStartMs != nil {
		scheduleTask.EarliestStartMs = event.EarliestStartMs
	}
	if event.Category != nil {
		scheduleTask.Category = event.Category
	}
	if event.IsDeepWork != nil {
		scheduleTask.IsDeepWork = *event.IsDeepWork
	}
	return scheduleTask

}
