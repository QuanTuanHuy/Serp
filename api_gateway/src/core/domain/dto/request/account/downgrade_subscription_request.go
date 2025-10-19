/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type DowngradeSubscriptionRequest struct {
	NewPlanId      int64 `json:"newPlanId"`
	DurationMonths *int  `json:"durationMonths"`
}
