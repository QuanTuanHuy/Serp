/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/entity"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/infrastructure/store/model"
)

func ToSchedulePlanModel(schedulePlan *entity.SchedulePlanEntity) *model.SchedulePlanModel {
	if schedulePlan == nil {
		return nil
	}
	var endDate *time.Time
	if schedulePlan.EndDate != nil {
		temp := time.UnixMilli(*schedulePlan.EndDate)
		endDate = &temp
	}

	return &model.SchedulePlanModel{
		BaseModel: model.BaseModel{
			ID: schedulePlan.ID,
		},
		UserID:            schedulePlan.UserID,
		StartDate:         time.UnixMilli(schedulePlan.StartDate),
		EndDate:           endDate,
		ActiveStatus:      string(schedulePlan.ActiveStatus),
		ActiveTaskBatch:   schedulePlan.ActiveTaskBatch,
		IsActiveTaskBatch: schedulePlan.IsActiveTaskBatch,
	}
}

func ToSchedulePlanEntity(schedulePlanModel *model.SchedulePlanModel) *entity.SchedulePlanEntity {
	if schedulePlanModel == nil {
		return nil
	}
	var endDate *int64
	if schedulePlanModel.EndDate != nil {
		temp := schedulePlanModel.EndDate.UnixMilli()
		endDate = &temp
	}

	return &entity.SchedulePlanEntity{
		BaseEntity: entity.BaseEntity{
			ID:        schedulePlanModel.ID,
			CreatedAt: schedulePlanModel.CreatedAt.UnixMilli(),
			UpdatedAt: schedulePlanModel.UpdatedAt.UnixMilli(),
		},
		UserID:            schedulePlanModel.UserID,
		StartDate:         schedulePlanModel.StartDate.UnixMilli(),
		EndDate:           endDate,
		ActiveStatus:      enum.ActiveStatus(schedulePlanModel.ActiveStatus),
		ActiveTaskBatch:   schedulePlanModel.ActiveTaskBatch,
		IsActiveTaskBatch: schedulePlanModel.IsActiveTaskBatch,
	}
}
