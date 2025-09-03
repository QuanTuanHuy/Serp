/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"
	"time"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func ToTaskModel(task *entity.TaskEntity) *model.TaskModel {
	if task == nil {
		return nil
	}

	priorityBytes, _ := json.Marshal(task.Priority)
	priorityStr := string(priorityBytes)

	var startDate, deadline *time.Time
	if task.StartDate != nil {
		startTime := time.Unix(*task.StartDate, 0)
		startDate = &startTime
	}
	if task.Deadline != nil {
		deadlineTime := time.Unix(*task.Deadline, 0)
		deadline = &deadlineTime
	}

	return &model.TaskModel{
		BaseModel: model.BaseModel{
			ID: task.ID,
		},
		Title:        task.Title,
		Description:  task.Description,
		Priority:     priorityStr,
		Status:       string(task.Status),
		StartDate:    startDate,
		Deadline:     deadline,
		Duration:     task.Duration,
		ActiveStatus: string(task.ActiveStatus),
		GroupTaskID:  task.GroupTaskID,
		UserID:       task.UserID,
	}
}

func ToTaskEntity(taskModel *model.TaskModel) *entity.TaskEntity {
	if taskModel == nil {
		return nil
	}

	var priority []string
	if taskModel.Priority != "" {
		json.Unmarshal([]byte(taskModel.Priority), &priority)
	}

	var startDate, deadline *int64
	if taskModel.StartDate != nil {
		startUnix := taskModel.StartDate.Unix()
		startDate = &startUnix
	}
	if taskModel.Deadline != nil {
		deadlineUnix := taskModel.Deadline.Unix()
		deadline = &deadlineUnix
	}

	return &entity.TaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        taskModel.ID,
			CreatedAt: taskModel.CreatedAt.Unix(),
			UpdatedAt: taskModel.UpdatedAt.Unix(),
		},
		Title:        taskModel.Title,
		Description:  taskModel.Description,
		Priority:     utils.ToPriorityEnum(priority),
		Status:       enum.Status(taskModel.Status),
		StartDate:    startDate,
		Deadline:     deadline,
		Duration:     taskModel.Duration,
		ActiveStatus: enum.ActiveStatus(taskModel.ActiveStatus),
		GroupTaskID:  taskModel.GroupTaskID,
		UserID:       taskModel.UserID,
	}
}

func ToTaskEntityList(taskModels []*model.TaskModel) []*entity.TaskEntity {
	if taskModels == nil {
		return nil
	}
	tasks := make([]*entity.TaskEntity, len(taskModels))
	for i, taskModel := range taskModels {
		tasks[i] = ToTaskEntity(taskModel)
	}
	return tasks
}

func ToTaskModelList(tasks []*entity.TaskEntity) []*model.TaskModel {
	if tasks == nil {
		return nil
	}
	taskModels := make([]*model.TaskModel, len(tasks))
	for i, task := range tasks {
		taskModels[i] = ToTaskModel(task)
	}
	return taskModels
}
