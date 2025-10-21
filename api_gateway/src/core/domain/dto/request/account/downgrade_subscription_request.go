/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type DowngradeSubscriptionRequest struct {
	NewPlanId int64   `json:"newPlanId"`
	Notes     *string `json:"notes"`
}
