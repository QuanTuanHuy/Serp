/*
Author: QuanTuanHuy
Description: Part of Serp Project - Schedule Task Request DTOs
*/

package request

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type UpdateScheduleTaskRequest struct {
	DurationMin         *int           `json:"durationMin" binding:"omitempty,min=1"`
	Priority            *enum.Priority `json:"priority" binding:"omitempty"`
	DeadlineMs          *int64         `json:"deadlineMs" binding:"omitempty"`
	EarliestStartMs     *int64         `json:"earliestStartMs" binding:"omitempty"`
	PreferredStartMs    *int64         `json:"preferredStartMs" binding:"omitempty"`
	IsDeepWork          *bool          `json:"isDeepWork" binding:"omitempty"`
	AllowSplit          *bool          `json:"allowSplit" binding:"omitempty"`
	MinSplitDurationMin *int           `json:"minSplitDurationMin" binding:"omitempty,min=15"`
	MaxSplitCount       *int           `json:"maxSplitCount" binding:"omitempty,min=1,max=10"`
	BufferBeforeMin     *int           `json:"bufferBeforeMin" binding:"omitempty,min=0"`
	BufferAfterMin      *int           `json:"bufferAfterMin" binding:"omitempty,min=0"`
}

type TaskFilterRequest struct {
	BaseFilterRequest

	PlanID *int64  `form:"planId,omitempty"`
	Status *string `form:"status,omitempty" validate:"omitempty,oneof=PENDING SCHEDULED UNSCHEDULABLE PARTIAL COMPLETED COMPLETED EXCLUDED"`
}
