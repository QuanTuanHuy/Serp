/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

func CreateSchedulePlanMapper(userID int64) *entity.SchedulePlanEntity {
	return &entity.SchedulePlanEntity{
		UserID:            userID,
		StartDate:         time.Now().UnixMilli(),
		ActiveStatus:      enum.Active,
		ActiveTaskBatch:   0,
		IsActiveTaskBatch: false,
	}
}
