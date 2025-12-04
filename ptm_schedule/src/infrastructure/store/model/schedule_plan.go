/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type SchedulePlanModel struct {
	BaseModel
	UserID   int64 `json:"userId" gorm:"not null"`
	TenantID int64 `json:"tenantId" gorm:"not null"`

	StartDateMs time.Time  `json:"startDateMs" gorm:"not null"`
	EndDateMs   *time.Time `json:"endDateMs" gorm:""`

	PlanName string `json:"planName" gorm:"size:255;not null"`
	PlanType string `json:"planType" gorm:"size:100"`

	AlgorithmUsed          string  `json:"algorithmUsed" gorm:"size:100;not null"`
	OptimizationScore      float64 `json:"optimizationScore" gorm:"not null"`
	OptimizationTimestamp  int64   `json:"optimizationTimestamp" gorm:"not null"`
	OptimizationDurationMs int64   `json:"optimizationDurationMs" gorm:"not null"`

	Version      int32  `json:"version" gorm:"not null"`
	ParentPlanID *int64 `json:"parentPlanId" gorm:""`

	Status  string `json:"status" gorm:"size:100;not null"`
	IsStale bool   `json:"isStale" gorm:"not null;default:false"`
}

func (SchedulePlanModel) TableName() string {
	return "schedule_plans"
}
