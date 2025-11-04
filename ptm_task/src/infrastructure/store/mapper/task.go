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
	"gorm.io/datatypes"
)

func ToTaskModel(task *entity.TaskEntity) *model.TaskModel {
	if task == nil {
		return nil
	}

	priorityBytes, _ := json.Marshal(task.Priority)
	priorityStr := string(priorityBytes)

	var startDate, deadline *time.Time
	if task.StartDate != nil {
		startTime := time.UnixMilli(*task.StartDate)
		startDate = &startTime
	}
	if task.Deadline != nil {
		deadlineTime := time.UnixMilli(*task.Deadline)
		deadline = &deadlineTime
	}

	var dimsBytes []byte
	if task.PriorityDims != nil {
		dimsBytes, _ = json.Marshal(task.PriorityDims)
	}

	return &model.TaskModel{
		BaseModel: model.BaseModel{
			ID: task.ID,
		},
		Title:         task.Title,
		Description:   task.Description,
		Priority:      priorityStr,
		Status:        string(task.Status),
		StartDate:     startDate,
		PriorityScore: task.PriorityScore,
		PriorityDims:  datatypes.JSON(dimsBytes),
		Deadline:      deadline,
		Duration:      task.Duration,
		ActiveStatus:  string(task.ActiveStatus),
		GroupTaskID:   task.GroupTaskID,
		UserID:        task.UserID,
		ParentTaskID:  task.ParentTaskID,
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
		startUnix := taskModel.StartDate.UnixMilli()
		startDate = &startUnix
	}
	if taskModel.Deadline != nil {
		deadlineUnix := taskModel.Deadline.UnixMilli()
		deadline = &deadlineUnix
	}

	var dims []entity.PriorityDimension
	if len(taskModel.PriorityDims) > 0 {
		_ = json.Unmarshal([]byte(taskModel.PriorityDims), &dims)
	}

	return &entity.TaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        taskModel.ID,
			CreatedAt: taskModel.CreatedAt.UnixMilli(),
			UpdatedAt: taskModel.UpdatedAt.UnixMilli(),
		},
		Title:         taskModel.Title,
		Description:   taskModel.Description,
		Priority:      utils.ToPriorityEnum(priority),
		Status:        enum.Status(taskModel.Status),
		StartDate:     startDate,
		PriorityScore: taskModel.PriorityScore,
		PriorityDims:  dims,
		Deadline:      deadline,
		Duration:      taskModel.Duration,
		ActiveStatus:  enum.ActiveStatus(taskModel.ActiveStatus),
		GroupTaskID:   taskModel.GroupTaskID,
		UserID:        taskModel.UserID,
		ParentTaskID:  taskModel.ParentTaskID,
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
