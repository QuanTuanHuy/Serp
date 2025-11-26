/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/entity"
)

func CreateSchedulePlanMapper(userID int64) *entity.SchedulePlanEntity {
	return &entity.SchedulePlanEntity{
		UserID: userID,
	}
}
