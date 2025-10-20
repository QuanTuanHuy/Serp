/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpgradeSubscriptionRequest struct {
	NewPlanId       int64   `json:"newPlanId"`
	DurationMonths  *int    `json:"durationMonths"`
	PaymentMethodId *string `json:"paymentMethodId"`
}
