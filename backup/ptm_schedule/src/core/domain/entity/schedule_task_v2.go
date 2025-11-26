/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleTaskV2Entity struct {
	BaseEntity
	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	SchedulePlanID int64 `json:"schedulePlanId"`

	TaskID           int64  `json:"taskId"`
	TaskSnapshotHash string `json:"taskSnapshotHash"`
	Title            string `json:"title"`

	DurationMin   int           `json:"durationMin"`
	Priority      enum.Priority `json:"priority"`
	PriorityScore float64       `json:"priorityScore"`
	Category      *string       `json:"category"`
	IsDeepWork    bool          `json:"isDeepWork"`

	EarliestStartMs  *int64 `json:"earliestStartMs,omitempty"`
	DeadlineMs       *int64 `json:"deadlineMs,omitempty"`
	PreferredStartMs *int64 `json:"preferredStartMs,omitempty"`

	AllowSplit          bool `json:"allowSplit"`
	MinSplitDurationMin int  `json:"minSplitDurationMin"`
	MaxSplitCount       int  `json:"maxSplitCount"`

	IsPinned      bool   `json:"isPinned"`
	PinnedStartMs *int64 `json:"pinnedStartMs,omitempty"`
	PinnedEndMs   *int64 `json:"pinnedEndMs,omitempty"`

	DependentTaskIDs []int64 `json:"dependentTaskIds"`
	BufferBeforeMin  int     `json:"bufferBeforeMin"`
	BufferAfterMin   int     `json:"bufferAfterMin"`

	ScheduleStatus    enum.ScheduleTaskStatus `json:"scheduleStatus"`
	UnscheduledReason *string                 `json:"unscheduledReason,omitempty"`
}
