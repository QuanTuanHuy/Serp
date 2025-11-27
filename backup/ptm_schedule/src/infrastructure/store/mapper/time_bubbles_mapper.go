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

func ToTimeBubblesModel(timeBubble *entity.TimeBubblesEntity) *model.TimeBubblesModel {
	if timeBubble == nil {
		return nil
	}
	return &model.TimeBubblesModel{
		BaseModel: model.BaseModel{
			ID: timeBubble.ID,
		},
		UserID:       timeBubble.UserID,
		DayOfWeek:    timeBubble.DayOfWeek,
		DayOfWeekStr: timeBubble.DayOfWeekStr,
		StartTime:    timeBubble.StartTime,
		EndTime:      timeBubble.EndTime,
		Tag:          string(timeBubble.Tag),
		Status:       string(timeBubble.Status),
	}
}

func ToTimeBubblesEntity(timeBubbleModel *model.TimeBubblesModel) *entity.TimeBubblesEntity {
	if timeBubbleModel == nil {
		return nil
	}
	return &entity.TimeBubblesEntity{
		BaseEntity: entity.BaseEntity{
			ID:        timeBubbleModel.ID,
			CreatedAt: timeBubbleModel.CreatedAt.UnixMilli(),
			UpdatedAt: timeBubbleModel.UpdatedAt.UnixMilli(),
		},
		UserID:       timeBubbleModel.UserID,
		DayOfWeek:    timeBubbleModel.DayOfWeek,
		DayOfWeekStr: timeBubbleModel.DayOfWeekStr,
		StartTime:    timeBubbleModel.StartTime,
		EndTime:      timeBubbleModel.EndTime,
		Tag:          enum.Tag(timeBubbleModel.Tag),
		Status:       enum.ActiveStatus(timeBubbleModel.Status),
	}
}

func ToTimeBubblesEntities(timeBubbleModels []*model.TimeBubblesModel) []*entity.TimeBubblesEntity {
	if timeBubbleModels == nil {
		return nil
	}
	timeBubbles := make([]*entity.TimeBubblesEntity, len(timeBubbleModels))
	for i, timeBubbleModel := range timeBubbleModels {
		timeBubbles[i] = ToTimeBubblesEntity(timeBubbleModel)
	}
	return timeBubbles
}

func ToTimeBubblesModels(timeBubbles []*entity.TimeBubblesEntity) []*model.TimeBubblesModel {
	if timeBubbles == nil {
		return nil
	}
	timeBubbleModels := make([]*model.TimeBubblesModel, len(timeBubbles))
	for i, timeBubble := range timeBubbles {
		timeBubbleModels[i] = ToTimeBubblesModel(timeBubble)
	}
	return timeBubbleModels
}
