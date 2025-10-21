/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type SubscribeRequest struct {
	PlanId       int64   `json:"planId"`
	BillingCycle string  `json:"billingCycle"`
	IsAutoRenew  *bool   `json:"isAutoRenew"`
	Notes        *string `json:"notes"`
}
