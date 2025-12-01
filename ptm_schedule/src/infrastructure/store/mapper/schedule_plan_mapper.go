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
	if schedulePlan.EndDateMs != nil {
		temp := time.UnixMilli(*schedulePlan.EndDateMs)
		endDate = &temp
	}

	return &model.SchedulePlanModel{
		BaseModel: model.BaseModel{
			ID: schedulePlan.ID,
		},
		UserID:                 schedulePlan.UserID,
		TenantID:               schedulePlan.TenantID,
		StartDateMs:            time.UnixMilli(schedulePlan.StartDateMs),
		EndDateMs:              endDate,
		PlanName:               schedulePlan.PlanName,
		PlanType:               schedulePlan.PlanType,
		AlgorithmUsed:          string(schedulePlan.AlgorithmUsed),
		OptimizationScore:      schedulePlan.OptimizationScore,
		OptimizationTimestamp:  schedulePlan.OptimizationTimestamp,
		OptimizationDurationMs: schedulePlan.OptimizationDurationMs,
		Version:                schedulePlan.Version,
		ParentPlanID:           schedulePlan.ParentPlanID,
		Status:                 string(schedulePlan.Status),
	}
}

func ToSchedulePlanEntity(schedulePlanModel *model.SchedulePlanModel) *entity.SchedulePlanEntity {
	if schedulePlanModel == nil {
		return nil
	}
	var endDate *int64
	if schedulePlanModel.EndDateMs != nil {
		temp := schedulePlanModel.EndDateMs.UnixMilli()
		endDate = &temp
	}

	return &entity.SchedulePlanEntity{
		BaseEntity: entity.BaseEntity{
			ID:        schedulePlanModel.ID,
			CreatedAt: schedulePlanModel.CreatedAt.UnixMilli(),
			UpdatedAt: schedulePlanModel.UpdatedAt.UnixMilli(),
		},
		UserID:                 schedulePlanModel.UserID,
		StartDateMs:            schedulePlanModel.StartDateMs.UnixMilli(),
		EndDateMs:              endDate,
		PlanName:               schedulePlanModel.PlanName,
		PlanType:               schedulePlanModel.PlanType,
		TenantID:               schedulePlanModel.TenantID,
		AlgorithmUsed:          enum.Algorithm(schedulePlanModel.AlgorithmUsed),
		OptimizationScore:      schedulePlanModel.OptimizationScore,
		OptimizationTimestamp:  schedulePlanModel.OptimizationTimestamp,
		OptimizationDurationMs: schedulePlanModel.OptimizationDurationMs,
		Version:                schedulePlanModel.Version,
		ParentPlanID:           schedulePlanModel.ParentPlanID,
		Status:                 enum.PlanStatus(schedulePlanModel.Status),
	}
}

func ToSchedulePlanEntities(schedulePlanModels []model.SchedulePlanModel) []*entity.SchedulePlanEntity {
	schedulePlans := make([]*entity.SchedulePlanEntity, 0, len(schedulePlanModels))
	for _, model := range schedulePlanModels {
		schedulePlans = append(schedulePlans, ToSchedulePlanEntity(&model))
	}
	return schedulePlans
}
