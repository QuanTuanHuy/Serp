/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/message"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func ToTaskEntity(createTask *request.CreateTaskDTO) *entity.TaskEntity {
	priorities := utils.StringSliceFromPointers(createTask.Priority)
	return &entity.TaskEntity{
		Title:        createTask.Title,
		Description:  utils.StringValue(createTask.Description),
		Priority:     utils.ToPriorityEnum(priorities),
		Status:       enum.Status(utils.StringValueWithDefault(createTask.Status, string(enum.ToDo))),
		StartDate:    createTask.StartDate,
		Deadline:     createTask.Deadline,
		Duration:     utils.Float64ValueWithDefault(createTask.Duration, 1),
		ActiveStatus: enum.Active,
		GroupTaskID:  createTask.GroupTaskID,
	}
}

func UpdateTaskMapper(task *entity.TaskEntity, updateTask *request.UpdateTaskDTO) *entity.TaskEntity {
	task.Title = updateTask.Title
	task.Description = updateTask.Description
	task.StartDate = &updateTask.StartDate
	task.Deadline = &updateTask.Deadline
	task.Duration = updateTask.Duration
	task.Status = enum.Status(updateTask.Status)
	task.Priority = utils.ToPriorityEnum(updateTask.Priority)
	if updateTask.ActiveStatus != nil {
		task.ActiveStatus = enum.ActiveStatus(*updateTask.ActiveStatus)
	}
	return task
}

func ToKafkaCreateTaskMessage(task *entity.TaskEntity) *message.KafkaCreateTaskMessage {
	return &message.KafkaCreateTaskMessage{
		GroupTaskID:  task.GroupTaskID,
		TaskID:       task.ID,
		UserID:       task.UserID,
		Title:        task.Title,
		Description:  task.Description,
		Priority:     task.Priority,
		Status:       task.Status,
		StartDate:    task.StartDate,
		Deadline:     task.Deadline,
		Duration:     task.Duration,
		ActiveStatus: task.ActiveStatus,
	}
}

func ToKafkaUpdateTaskMessage(task *entity.TaskEntity, updateTask *request.UpdateTaskDTO) *message.KafkaUpdateTaskMessage {
	return &message.KafkaUpdateTaskMessage{
		TaskID:         task.ID,
		UserID:         task.UserID,
		Title:          task.Title,
		Description:    task.Description,
		Priority:       utils.ToPriorityString(task.Priority),
		Status:         string(task.Status),
		StartDate:      *task.StartDate,
		Deadline:       *task.Deadline,
		Duration:       task.Duration,
		ActiveStatus:   string(task.ActiveStatus),
		TaskOrder:      updateTask.TaskOrder,
		StopTime:       updateTask.StopTime,
		ScheduleTaskID: updateTask.ScheduleTaskID,
	}
}
