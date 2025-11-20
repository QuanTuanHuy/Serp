/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"gorm.io/datatypes"
)

type TaskMapper struct{}

func NewTaskMapper() *TaskMapper {
	return &TaskMapper{}
}

func (m *TaskMapper) ToEntity(taskModel *model.TaskModel) *entity.TaskEntity {
	if taskModel == nil {
		return nil
	}

	task := &entity.TaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        taskModel.ID,
			CreatedAt: taskModel.CreatedAt.UnixMilli(),
			UpdatedAt: taskModel.UpdatedAt.UnixMilli(),
		},
		UserID:   taskModel.UserID,
		TenantID: taskModel.TenantID,

		Title:       taskModel.Title,
		Description: taskModel.Description,

		Priority:      taskModel.Priority,
		PriorityScore: taskModel.PriorityScore,

		EstimatedDurationMin: taskModel.EstimatedDurationMin,
		ActualDurationMin:    taskModel.ActualDurationMin,
		IsDurationLearned:    taskModel.IsDurationLearned,

		PreferredStartDateMs: taskModel.PreferredStartDateMs,
		DeadlineMs:           taskModel.DeadlineMs,
		EarliestStartMs:      taskModel.EarliestStartMs,

		Category: taskModel.Category,

		ParentTaskID: taskModel.ParentTaskID,
		ProjectID:    taskModel.ProjectID,

		IsRecurring:           taskModel.IsRecurring,
		RecurrencePattern:     taskModel.RecurrencePattern,
		RecurrenceConfig:      taskModel.RecurrenceConfig,
		ParentRecurringTaskID: taskModel.ParentRecurringTaskID,

		IsDeepWork: taskModel.IsDeepWork,
		IsMeeting:  taskModel.IsMeeting,
		IsFlexible: taskModel.IsFlexible,

		Status:       taskModel.Status,
		ActiveStatus: taskModel.ActiveStatus,

		Notes:      taskModel.Notes,
		ExternalID: taskModel.ExternalID,
		Source:     taskModel.Source,

		CompletedAt: taskModel.CompletedAt,
	}

	task.Tags = m.jsonMapToStringSlice(taskModel.Tags)

	task.DependentTaskIDs = m.jsonMapToInt64Slice(taskModel.DependentTaskIDs)

	return task
}

func (m *TaskMapper) ToModel(task *entity.TaskEntity) *model.TaskModel {
	if task == nil {
		return nil
	}

	taskModel := &model.TaskModel{
		BaseModel: model.BaseModel{
			ID: task.ID,
		},
		UserID:   task.UserID,
		TenantID: task.TenantID,

		Title:       task.Title,
		Description: task.Description,

		Priority:      task.Priority,
		PriorityScore: task.PriorityScore,

		EstimatedDurationMin: task.EstimatedDurationMin,
		ActualDurationMin:    task.ActualDurationMin,
		IsDurationLearned:    task.IsDurationLearned,

		PreferredStartDateMs: task.PreferredStartDateMs,
		DeadlineMs:           task.DeadlineMs,
		EarliestStartMs:      task.EarliestStartMs,

		Category: task.Category,

		ParentTaskID: task.ParentTaskID,
		ProjectID:    task.ProjectID,

		IsRecurring:           task.IsRecurring,
		RecurrencePattern:     task.RecurrencePattern,
		RecurrenceConfig:      task.RecurrenceConfig,
		ParentRecurringTaskID: task.ParentRecurringTaskID,

		IsDeepWork: task.IsDeepWork,
		IsMeeting:  task.IsMeeting,
		IsFlexible: task.IsFlexible,

		Status:       task.Status,
		ActiveStatus: task.ActiveStatus,

		Notes:      task.Notes,
		ExternalID: task.ExternalID,
		Source:     task.Source,

		CompletedAt: task.CompletedAt,
	}

	taskModel.Tags = m.stringSliceToJSONMap(task.Tags)

	taskModel.DependentTaskIDs = m.int64SliceToJSONMap(task.DependentTaskIDs)

	return taskModel
}

func (m *TaskMapper) ToEntities(models []*model.TaskModel) []*entity.TaskEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.TaskEntity, 0, len(models))
	for _, model := range models {
		if entity := m.ToEntity(model); entity != nil {
			entities = append(entities, entity)
		}
	}

	return entities
}

func (m *TaskMapper) ToModels(entities []*entity.TaskEntity) []*model.TaskModel {
	if entities == nil {
		return nil
	}

	models := make([]*model.TaskModel, 0, len(entities))
	for _, entity := range entities {
		if model := m.ToModel(entity); model != nil {
			models = append(models, model)
		}
	}

	return models
}

func (m *TaskMapper) stringSliceToJSONMap(slice []string) datatypes.JSONMap {
	if slice == nil {
		return datatypes.JSONMap{}
	}

	result := make(map[string]interface{})
	for i, val := range slice {
		result[string(rune('0'+i))] = val
	}

	return datatypes.JSONMap(result)
}

func (m *TaskMapper) jsonMapToStringSlice(jsonMap datatypes.JSONMap) []string {
	if len(jsonMap) == 0 {
		return []string{}
	}

	var arr []string
	bytes, _ := json.Marshal(jsonMap)
	if err := json.Unmarshal(bytes, &arr); err == nil {
		return arr
	}

	result := make([]string, 0, len(jsonMap))
	for _, val := range jsonMap {
		if str, ok := val.(string); ok {
			result = append(result, str)
		}
	}

	return result
}

func (m *TaskMapper) int64SliceToJSONMap(slice []int64) datatypes.JSONMap {
	if slice == nil {
		return datatypes.JSONMap{}
	}

	result := make(map[string]interface{})
	for i, val := range slice {
		result[string(rune('0'+i))] = val
	}

	return datatypes.JSONMap(result)
}

func (m *TaskMapper) jsonMapToInt64Slice(jsonMap datatypes.JSONMap) []int64 {
	if len(jsonMap) == 0 {
		return []int64{}
	}

	var arr []int64
	bytes, _ := json.Marshal(jsonMap)
	if err := json.Unmarshal(bytes, &arr); err == nil {
		return arr
	}

	result := make([]int64, 0, len(jsonMap))
	for _, val := range jsonMap {
		switch v := val.(type) {
		case int64:
			result = append(result, v)
		case float64:
			result = append(result, int64(v))
		case int:
			result = append(result, int64(v))
		}
	}

	return result
}
