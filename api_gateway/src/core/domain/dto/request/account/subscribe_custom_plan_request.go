/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type SubscribeCustomPlanRequest struct {
	BillingCycle string  `json:"billingCycle" validate:"required"`
	IsAutoRenew  *bool   `json:"isAutoRenew,omitempty"`
	Notes        *string `json:"notes,omitempty"`
	ModuleIds    []int64 `json:"moduleIds,omitempty"`
}
