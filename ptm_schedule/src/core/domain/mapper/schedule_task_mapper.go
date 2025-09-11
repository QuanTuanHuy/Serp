package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

func ToScheduleTaskEntity(createTaskMsg *message.KafkaCreateTaskMessage) *entity.ScheduleTaskEntity {
	return &entity.ScheduleTaskEntity{
		TaskID:          createTaskMsg.TaskID,
		Title:           createTaskMsg.Title,
		Priority:        createTaskMsg.Priority,
		Status:          createTaskMsg.Status,
		StartDate:       utils.Int64Value(createTaskMsg.StartDate),
		Deadline:        utils.Int64Value(createTaskMsg.Deadline),
		Duration:        createTaskMsg.Duration,
		ActiveStatus:    createTaskMsg.ActiveStatus,
		PreferenceLevel: utils.ConvertPriority(createTaskMsg.Priority),
		Repeat:          enum.None,
	}
}

func UpdateTaskMapper(updateTaskMsg *message.KafkaUpdateTaskMessage, scheduleTask *entity.ScheduleTaskEntity) *entity.ScheduleTaskEntity {
	priorities := utils.ToPriorityEnum(updateTaskMsg.Priority)

	scheduleTask.TaskID = updateTaskMsg.TaskID
	scheduleTask.Title = updateTaskMsg.Title
	scheduleTask.StartDate = updateTaskMsg.StartDate
	scheduleTask.Deadline = updateTaskMsg.Deadline
	scheduleTask.Duration = updateTaskMsg.Duration
	scheduleTask.Status = enum.Status(updateTaskMsg.Status)
	scheduleTask.Priority = priorities
	scheduleTask.TaskOrder = updateTaskMsg.TaskOrder
	scheduleTask.StopTime = updateTaskMsg.StopTime
	scheduleTask.ActiveStatus = enum.ActiveStatus(updateTaskMsg.ActiveStatus)
	scheduleTask.PreferenceLevel = utils.ConvertPriority(priorities)

	return scheduleTask
}

func ToCreateScheduleTaskMessage(scheduleTask *entity.ScheduleTaskEntity) *message.KafkaCreateScheduleTaskMessage {
	return &message.KafkaCreateScheduleTaskMessage{
		TaskID:           scheduleTask.TaskID,
		ScheduleTaskID:   scheduleTask.ID,
		ScheduleTaskName: scheduleTask.Title,
	}
}
