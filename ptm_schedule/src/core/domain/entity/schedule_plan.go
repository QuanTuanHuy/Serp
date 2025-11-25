package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type SchedulePlanEntity struct {
	BaseEntity
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	StartDateMs int64  `json:"startDateMs"`
	EndDateMs   *int64 `json:"endDateMs"`

	PlanName string `json:"planName"`
	PlanType string `json:"planType"`

	AlgorithmUsed          enum.Algorithm `json:"algorithmUsed"`
	OptimizationScore      float64        `json:"optimizationScore"`
	OptimizationTimestamp  int64          `json:"optimizationTimestamp"`
	OptimizationDurationMs int64          `json:"optimizationDurationMs"`

	Version      int32  `json:"version"`
	ParentPlanID *int64 `json:"parentPlanId"`

	Status enum.PlanStatus `json:"status"`
}
