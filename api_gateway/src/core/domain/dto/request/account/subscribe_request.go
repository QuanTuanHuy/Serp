/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type SubscribeRequest struct {
	PlanId          int64   `json:"planId"`
	DurationMonths  *int    `json:"durationMonths"`
	PaymentMethodId *string `json:"paymentMethodId"`
	AutoRenew       *bool   `json:"autoRenew"`
}
