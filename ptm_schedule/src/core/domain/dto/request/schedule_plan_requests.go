/*
Author: QuanTuanHuy
Description: Part of Serp Project - Schedule Plan DTOs
*/

package request

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/optimization"
	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

type CreateProposedPlanRequest struct {
	Algorithm       enum.Algorithm `json:"algorithm"`
	RequireApproval bool           `json:"requireApproval"` // true = create PROPOSED, false = auto ACTIVE
}

type TriggerRescheduleRequest struct {
	Strategy enum.RescheduleStrategy `json:"strategy"`
	Reason   string                  `json:"reason"` // "manual", "scheduled_job", "significant_changes"
}

type GetPlanEventsRequest struct {
	FromDateMs int64 `json:"fromDateMs"`
	ToDateMs   int64 `json:"toDateMs"`
}

type DeepOptimizeRequest struct {
	Strategy   optimization.StrategyType `json:"strategy" binding:"required,oneof=AUTO CP_SAT MILP LOCAL_SEARCH HEURISTIC"`
	MaxTimeSec *int                      `json:"maxTimeSec"` // Solver timeout in seconds (default 30)
	Reason     string                    `json:"reason"`     // "manual", "weekly_planning", "low_quality_detected"
}

type FallbackChainOptimizeRequest struct {
	MaxTimeSec *int   `json:"maxTimeSec"` // Timeout per algorithm in seconds (default 30)
	Reason     string `json:"reason"`     // "critical_scheduling", "must_succeed"
}
