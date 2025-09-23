/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type TimeBubblesEntity struct {
	BaseEntity
	UserID       int64             `json:"userId"`
	DayOfWeek    int               `json:"dayOfWeek"`
	DayOfWeekStr string            `json:"dayOfWeekStr"`
	StartTime    string            `json:"startTime"` // Format: HH:MM:SS
	EndTime      string            `json:"endTime"`
	Tag          enum.Tag          `json:"tag"`
	Status       enum.ActiveStatus `json:"status"`
}
