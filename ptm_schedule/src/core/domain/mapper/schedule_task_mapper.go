/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
)

func ToScheduleTaskEntity(createTaskMsg *message.KafkaCreateTaskMessage) *entity.ScheduleTaskEntity {
	return &entity.ScheduleTaskEntity{
		TaskID:           createTaskMsg.TaskID,
		UserID:           createTaskMsg.UserID,
		TenantID:         createTaskMsg.TenantID,
		Title:            createTaskMsg.Title,
		Priority:         createTaskMsg.Priority,
		Category:         createTaskMsg.Category,
		IsDeepWork:       createTaskMsg.IsDeepWork,
		DurationMin:      createTaskMsg.EstimatedDurationMin,
		EarliestStartMs:  createTaskMsg.EarliestStartMs,
		DeadlineMs:       createTaskMsg.DeadlineMs,
		PreferredStartMs: createTaskMsg.PreferredStartMs,
		DependentTaskIDs: createTaskMsg.DependentTaskIDs,
	}
}

func UpdateTaskMapper(updateTaskMsg *message.KafkaUpdateTaskMessage, scheduleTask *entity.ScheduleTaskEntity) *entity.ScheduleTaskEntity {

	scheduleTask.TaskID = updateTaskMsg.TaskID
	scheduleTask.Title = updateTaskMsg.Title
	scheduleTask.Priority = updateTaskMsg.Priority
	scheduleTask.DeadlineMs = updateTaskMsg.DeadlineMs
	scheduleTask.DurationMin = updateTaskMsg.DurationMin

	return scheduleTask
}

func ToCreateScheduleTaskMessage(scheduleTask *entity.ScheduleTaskEntity) *message.KafkaCreateScheduleTaskMessage {
	return &message.KafkaCreateScheduleTaskMessage{
		TaskID:           scheduleTask.TaskID,
		ScheduleTaskID:   scheduleTask.ID,
		ScheduleTaskName: scheduleTask.Title,
	}
}
