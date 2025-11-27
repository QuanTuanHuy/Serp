/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CalendarExceptionItemRequest struct {
	ID       int64  `json:"id"`
	DateMs   int64  `json:"dateMs" binding:"required,gt=0"`
	StartMin int    `json:"startMin" binding:"required,gte=0,lte=1440"`
	EndMin   int    `json:"endMin" binding:"required,gte=0,lte=1440"`
	Type     string `json:"type"`
}

type SaveExceptionsRequest struct {
	Items []*CalendarExceptionItemRequest `json:"items" binding:"required,dive"`
}

type ReplaceExceptionsRequest struct {
	FromDateMs int64                           `json:"fromDateMs" binding:"required,gt=0"`
	ToDateMs   int64                           `json:"toDateMs" binding:"required,gt=0"`
	Items      []*CalendarExceptionItemRequest `json:"items" binding:"required,dive"`
}

type DeleteExceptionsRequest struct {
	FromDateMs int64 `json:"fromDateMs" binding:"required,gt=0"`
	ToDateMs   int64 `json:"toDateMs" binding:"required,gt=0"`
}
