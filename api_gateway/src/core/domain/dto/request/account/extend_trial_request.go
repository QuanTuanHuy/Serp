/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type ExtendTrialRequest struct {
	ExtendDays int    `json:"extendDays"`
	Reason     string `json:"reason"`
}
