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
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

func ToScheduleGroupEntity(scheduleGroup *model.ScheduleGroupModel) *entity.ScheduleGroupEntity {
	if scheduleGroup == nil {
		return nil
	}
	var priorities []enum.Priority
	err := json.Unmarshal([]byte(scheduleGroup.Priority), &priorities)
	if err != nil {
		priorities = nil
	}
	var repeats []int32
	err = json.Unmarshal([]byte(scheduleGroup.Repeat), &repeats)
	if err != nil {
		repeats = nil
	}
	var rotationDay *int64
	if scheduleGroup.RotationDay != nil {
		rotationDay = utils.Int64Ptr(scheduleGroup.RotationDay.UnixMilli())
	}

	return &entity.ScheduleGroupEntity{
		BaseEntity: entity.BaseEntity{
			ID:        scheduleGroup.ID,
			CreatedAt: scheduleGroup.CreatedAt.UnixMilli(),
			UpdatedAt: scheduleGroup.UpdatedAt.UnixMilli(),
		},
		SchedulePlanID:  scheduleGroup.SchedulePlanID,
		ProjectID:       scheduleGroup.ProjectID,
		GroupTaskID:     scheduleGroup.GroupTaskID,
		Title:           scheduleGroup.Title,
		Priority:        priorities,
		Status:          enum.Status(scheduleGroup.Status),
		StartHour:       scheduleGroup.StartHour,
		StartMinute:     scheduleGroup.StartMinute,
		EndHour:         scheduleGroup.EndHour,
		EndMinute:       scheduleGroup.EndMinute,
		Duration:        scheduleGroup.Duration,
		PreferenceLevel: scheduleGroup.PreferenceLevel,
		Repeat:          repeats,
		IsNotify:        scheduleGroup.IsNotify,
		ActiveStatus:    enum.ActiveStatus(scheduleGroup.ActiveStatus),
		IsFailed:        scheduleGroup.IsFailed,
		RotationDay:     rotationDay,
	}
}

func ToScheduleGroupModel(scheduleGroup *entity.ScheduleGroupEntity) *model.ScheduleGroupModel {
	if scheduleGroup == nil {
		return nil
	}

	priority, _ := json.Marshal(scheduleGroup.Priority)
	repeat, _ := json.Marshal(scheduleGroup.Repeat)
	var rotationDay *time.Time
	if scheduleGroup.RotationDay != nil {
		timestamp := time.UnixMilli(*scheduleGroup.RotationDay)
		rotationDay = &timestamp
	}

	return &model.ScheduleGroupModel{
		BaseModel: model.BaseModel{
			ID: scheduleGroup.ID,
		},
		SchedulePlanID:  scheduleGroup.SchedulePlanID,
		ProjectID:       scheduleGroup.ProjectID,
		GroupTaskID:     scheduleGroup.GroupTaskID,
		Title:           scheduleGroup.Title,
		Priority:        string(priority),
		Status:          string(scheduleGroup.Status),
		StartHour:       scheduleGroup.StartHour,
		StartMinute:     scheduleGroup.StartMinute,
		EndHour:         scheduleGroup.EndHour,
		EndMinute:       scheduleGroup.EndMinute,
		Duration:        scheduleGroup.Duration,
		PreferenceLevel: scheduleGroup.PreferenceLevel,
		Repeat:          string(repeat),
		IsNotify:        scheduleGroup.IsNotify,
		ActiveStatus:    string(scheduleGroup.ActiveStatus),
		IsFailed:        scheduleGroup.IsFailed,
		RotationDay:     rotationDay,
	}
}

func ToScheduleGroupEntities(scheduleGroups []*model.ScheduleGroupModel) []*entity.ScheduleGroupEntity {
	if scheduleGroups == nil {
		return nil
	}

	var entities []*entity.ScheduleGroupEntity
	for _, sg := range scheduleGroups {
		entities = append(entities, ToScheduleGroupEntity(sg))
	}
	return entities
}
