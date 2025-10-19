/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CancelSubscriptionRequest struct {
	Reason           string `json:"reason"`
	CancelImmediately *bool  `json:"cancelImmediately"`
}
