/*
Author: QuanTuanHuy
Description: Part of Serp Project - Schedule Plan DTOs
*/

package request

import "github.com/serp/ptm-schedule/src/core/domain/enum"

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
