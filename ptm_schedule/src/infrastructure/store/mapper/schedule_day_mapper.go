/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToScheduleDayEntity(scheduleDay *model.ScheduleDayModel) *entity.ScheduleDayEntity {
	if scheduleDay == nil {
		return nil
	}
	var tag *enum.Tag
	if scheduleDay.Tag != nil {
		t := enum.Tag(*scheduleDay.Tag)
		tag = &t
	}
	return &entity.ScheduleDayEntity{
		BaseEntity: entity.BaseEntity{
			ID:        scheduleDay.ID,
			CreatedAt: scheduleDay.CreatedAt.UnixMilli(),
			UpdatedAt: scheduleDay.UpdatedAt.UnixMilli(),
		},
		UserID:           scheduleDay.UserID,
		StartTime:        scheduleDay.StartTime,
		EndTime:          scheduleDay.EndTime,
		PrimaryTaskID:    scheduleDay.PrimaryTaskID,
		PrimaryTaskTitle: scheduleDay.PrimaryTaskTitle,
		BackupTaskID:     scheduleDay.BackupTaskID,
		BackupTaskTitle:  scheduleDay.BackupTaskTitle,
		Tag:              tag,
		WeekDay:          scheduleDay.WeekDay,
		TimeBubbleID:     scheduleDay.TimeBubbleID,
	}
}

func ToScheduleDayModel(scheduleDay *entity.ScheduleDayEntity) *model.ScheduleDayModel {
	if scheduleDay == nil {
		return nil
	}
	var tag *string
	if scheduleDay.Tag != nil {
		t := string(*scheduleDay.Tag)
		tag = &t
	}
	return &model.ScheduleDayModel{
		BaseModel: model.BaseModel{
			ID: scheduleDay.ID,
		},
		UserID:           scheduleDay.UserID,
		StartTime:        scheduleDay.StartTime,
		EndTime:          scheduleDay.EndTime,
		PrimaryTaskID:    scheduleDay.PrimaryTaskID,
		PrimaryTaskTitle: scheduleDay.PrimaryTaskTitle,
		BackupTaskID:     scheduleDay.BackupTaskID,
		BackupTaskTitle:  scheduleDay.BackupTaskTitle,
		Tag:              tag,
		WeekDay:          scheduleDay.WeekDay,
		TimeBubbleID:     scheduleDay.TimeBubbleID,
	}
}

func ToScheduleDayEntities(scheduleDays []*model.ScheduleDayModel) []*entity.ScheduleDayEntity {
	if scheduleDays == nil {
		return nil
	}
	entities := make([]*entity.ScheduleDayEntity, 0, len(scheduleDays))
	for _, scheduleDay := range scheduleDays {
		entities = append(entities, ToScheduleDayEntity(scheduleDay))
	}
	return entities
}

func ToScheduleDayModels(scheduleDays []*entity.ScheduleDayEntity) []*model.ScheduleDayModel {
	if scheduleDays == nil {
		return nil
	}
	models := make([]*model.ScheduleDayModel, 0, len(scheduleDays))
	for _, scheduleDay := range scheduleDays {
		models = append(models, ToScheduleDayModel(scheduleDay))
	}
	return models
}
