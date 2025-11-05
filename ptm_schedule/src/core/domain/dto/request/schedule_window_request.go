/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type MaterializeWindowsRequest struct {
	FromDateMs int64 `json:"fromDateMs" binding:"required,gt=0"`
	ToDateMs   int64 `json:"toDateMs" binding:"required,gt=0"`
}
