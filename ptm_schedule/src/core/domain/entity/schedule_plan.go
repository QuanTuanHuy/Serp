package entity

import (
	"fmt"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

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

// Factory methods

func NewRollingPlan(userId, tenantId int64, durationDays int) *SchedulePlanEntity {
	now := time.Now()
	startMs := now.UnixMilli()
	endMs := now.AddDate(0, 0, durationDays).UnixMilli()

	return &SchedulePlanEntity{
		UserID:        userId,
		TenantID:      tenantId,
		StartDateMs:   startMs,
		EndDateMs:     &endMs,
		PlanType:      "rolling",
		PlanName:      fmt.Sprintf("Plan %s", now.Format("2006-01-02")),
		Status:        enum.PlanDraft,
		Version:       1,
		AlgorithmUsed: enum.HybridAlgorithm,
	}
}

// Lifecycle management

func (e *SchedulePlanEntity) TransitionTo(targetStatus enum.PlanStatus) error {
	if !e.Status.CanTransitionTo(targetStatus) {
		return fmt.Errorf("invalid transition from %s to %s", e.Status, targetStatus)
	}
	e.Status = targetStatus
	e.UpdatedAt = time.Now().UnixMilli()
	return nil
}

func (e *SchedulePlanEntity) StartOptimization(algo enum.Algorithm) error {
	if err := e.TransitionTo(enum.PlanProcessing); err != nil {
		return err
	}
	e.AlgorithmUsed = algo
	e.OptimizationTimestamp = time.Now().UnixMilli()
	return nil
}

func (e *SchedulePlanEntity) CompleteOptimization(score float64, durationMs int64) error {
	if err := e.TransitionTo(enum.PlanProposed); err != nil {
		return err
	}
	e.OptimizationScore = score
	e.OptimizationDurationMs = durationMs
	return nil
}

func (e *SchedulePlanEntity) FailOptimization(reason string) {
	_ = e.TransitionTo(enum.PlanFailed)
}

func (e *SchedulePlanEntity) Activate() error {
	return e.TransitionTo(enum.PlanActive)
}

func (e *SchedulePlanEntity) Archive() {
	_ = e.TransitionTo(enum.PlanArchived)
}

// Versioning

// CreateNextVersion tạo một bản sao mới (Draft) để Re-schedule mà không ảnh hưởng bản hiện tại
func (e *SchedulePlanEntity) CreateNextVersion() *SchedulePlanEntity {
	nextVersion := &SchedulePlanEntity{
		UserID:        e.UserID,
		TenantID:      e.TenantID,
		StartDateMs:   e.StartDateMs,
		EndDateMs:     e.EndDateMs,
		PlanName:      e.PlanName,
		PlanType:      e.PlanType,
		AlgorithmUsed: e.AlgorithmUsed,

		Version:      e.Version + 1,
		ParentPlanID: &e.ID,

		Status:                 enum.PlanDraft,
		OptimizationScore:      0,
		OptimizationDurationMs: 0,
		OptimizationTimestamp:  0,
	}
	return nextVersion
}

// Domian logic

func (e *SchedulePlanEntity) IsExpired() bool {
	if e.EndDateMs == nil {
		return false
	}
	return *e.EndDateMs < time.Now().UnixMilli()
}

func (e *SchedulePlanEntity) IsBetterThan(other *SchedulePlanEntity) bool {
	if other == nil {
		return true
	}
	return e.OptimizationScore > other.OptimizationScore
}

func (e *SchedulePlanEntity) ShouldReOptimize() bool {
	isStale := (time.Now().UnixMilli() - e.OptimizationTimestamp) > 24*60*60*1000
	isLowScore := e.OptimizationScore < 50.0
	return isStale || isLowScore
}
