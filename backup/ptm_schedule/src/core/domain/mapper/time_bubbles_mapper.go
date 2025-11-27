/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

func CreateTimeBubblesMapper(activity request.CreateTimeBubblesDTO, userID int64, dayOfWeek int, status string) *entity.TimeBubblesEntity {
	return &entity.TimeBubblesEntity{
		UserID:       userID,
		DayOfWeek:    dayOfWeek,
		DayOfWeekStr: utils.ToDayOfWeekString(dayOfWeek),
		StartTime:    activity.StartTime,
		EndTime:      activity.EndTime,
		Tag:          enum.Tag(activity.Tag),
		Status:       enum.ActiveStatus(status),
	}
}
