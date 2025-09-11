/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToScheduleTaskEntity(model *model.ScheduleTaskModel) *entity.ScheduleTaskEntity {
	if model == nil {
		return nil
	}
	var priorities []enum.Priority
	err := json.Unmarshal([]byte(model.Priority), &priorities)
	if err != nil {
		priorities = []enum.Priority{}
	}

	return &entity.ScheduleTaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        model.ID,
			CreatedAt: model.CreatedAt.UnixMilli(),
			UpdatedAt: model.UpdatedAt.UnixMilli(),
		},
		TaskID:               model.TaskID,
		Title:                model.Title,
		Priority:             priorities,
		Status:               enum.Status(model.Status),
		StartDate:            model.StartDate.UnixMilli(),
		Deadline:             model.Deadline.UnixMilli(),
		Duration:             model.Duration,
		ActiveStatus:         enum.ActiveStatus(model.ActiveStatus),
		PreferenceLevel:      model.PreferenceLevel,
		IsSynchronizedWithWO: model.IsSynchronizedWithWO,
		TaskOrder:            model.TaskOrder,
		Weight:               model.Weight,
		StopTime:             model.StopTime,
		TaskBatch:            model.TaskBatch,
		SchedulePlanID:       model.SchedulePlanID,
		Repeat:               enum.RepeatLevel(model.Repeat),
		ScheduleGroupID:      model.ScheduleGroupID,
	}
}

func ToScheduleTaskModel(entity *entity.ScheduleTaskEntity) *model.ScheduleTaskModel {
	if entity == nil {
		return nil
	}
	b, err := json.Marshal(entity.Priority)
	var priorityStr string
	if err != nil {
		priorityStr = "[]"
	} else {
		priorityStr = string(b)
	}

	return &model.ScheduleTaskModel{
		BaseModel: model.BaseModel{
			ID: entity.ID,
		},
		TaskID:               entity.TaskID,
		Title:                entity.Title,
		Priority:             priorityStr,
		Status:               string(entity.Status),
		StartDate:            time.UnixMilli(entity.StartDate),
		Deadline:             time.UnixMilli(entity.Deadline),
		Duration:             entity.Duration,
		ActiveStatus:         string(entity.ActiveStatus),
		PreferenceLevel:      entity.PreferenceLevel,
		IsSynchronizedWithWO: entity.IsSynchronizedWithWO,
		TaskOrder:            entity.TaskOrder,
		Weight:               entity.Weight,
		StopTime:             entity.StopTime,
		TaskBatch:            entity.TaskBatch,
		SchedulePlanID:       entity.SchedulePlanID,
		Repeat:               string(entity.Repeat),
		ScheduleGroupID:      entity.ScheduleGroupID,
	}
}

func ToScheduleTaskEntities(models []*model.ScheduleTaskModel) []*entity.ScheduleTaskEntity {
	var entities []*entity.ScheduleTaskEntity
	for _, m := range models {
		entities = append(entities, ToScheduleTaskEntity(m))
	}
	return entities
}

func ToScheduleTaskModels(entities []*entity.ScheduleTaskEntity) []*model.ScheduleTaskModel {
	var models []*model.ScheduleTaskModel
	for _, e := range entities {
		models = append(models, ToScheduleTaskModel(e))
	}
	return models
}
