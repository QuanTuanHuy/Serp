/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type AvailabilityCalendarResponse struct {
	ID           int64             `json:"id"`
	UserID       int64             `json:"userId"`
	DayOfWeek    int               `json:"dayOfWeek"`
	StartMin     int               `json:"startMin"`
	EndMin       int               `json:"endMin"`
	ActiveStatus enum.ActiveStatus `json:"activeStatus"`
	CreatedAt    int64             `json:"createdAt"`
	UpdatedAt    int64             `json:"updatedAt"`
}

type CalendarExceptionResponse struct {
	ID        int64  `json:"id"`
	UserID    int64  `json:"userId"`
	DateMs    int64  `json:"dateMs"`
	StartMin  int    `json:"startMin"`
	EndMin    int    `json:"endMin"`
	Type      string `json:"type"`
	CreatedAt int64  `json:"createdAt"`
	UpdatedAt int64  `json:"updatedAt"`
}
