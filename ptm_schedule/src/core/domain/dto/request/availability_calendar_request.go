/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type AvailabilityCalendarItemRequest struct {
	ID           int64             `json:"id"`
	DayOfWeek    int               `json:"dayOfWeek" binding:"required,gte=0,lte=6"`
	StartMin     int               `json:"startMin" binding:"required,gte=0,lte=1440"`
	EndMin       int               `json:"endMin" binding:"required,gte=0,lte=1440"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
}

type SetAvailabilityRequest struct {
	Items []*AvailabilityCalendarItemRequest `json:"items" binding:"required,dive"`
}

type ReplaceAvailabilityRequest struct {
	Items []*AvailabilityCalendarItemRequest `json:"items" binding:"required,dive"`
}
