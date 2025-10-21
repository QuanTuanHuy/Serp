/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpgradeSubscriptionRequest struct {
	NewPlanId    int64   `json:"newPlanId"`
	BillingCycle string  `json:"billingCycle"`
	IsAutoRenew  *bool   `json:"isAutoRenew"`
	Notes        *string `json:"notes"`
}
