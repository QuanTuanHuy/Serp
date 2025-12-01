/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import (
	"time"
)

type ScheduleTaskModel struct {
	BaseModel
	UserID   int64 `json:"userId" gorm:"column:user_id;not null;index"`
	TenantID int64 `json:"tenantId" gorm:"column:tenant_id"`

	SchedulePlanID int64 `json:"schedulePlanId" gorm:"column:schedule_plan_id;not null;index"`

	TaskID           int64  `json:"taskId" gorm:"column:task_id;not null;index"`
	TaskSnapshotHash string `json:"taskSnapshotHash" gorm:"column:task_snapshot_hash"`
	Title            string `json:"title" gorm:"column:title;not null"`

	DurationMin   int     `json:"durationMin" gorm:"column:duration_min;not null"`
	Priority      string  `json:"priority" gorm:"column:priority;not null"`
	PriorityScore float64 `json:"priorityScore" gorm:"column:priority_score"`
	Category      *string `json:"category" gorm:"column:category"`
	IsDeepWork    bool    `json:"isDeepWork" gorm:"column:is_deep_work;not null"`

	EarliestStartMs  *time.Time `json:"earliestStartMs" gorm:"column:earliest_start_ms"`
	DeadlineMs       *time.Time `json:"deadlineMs" gorm:"column:deadline_ms"`
	PreferredStartMs *time.Time `json:"preferredStartMs" gorm:"column:preferred_start_ms"`

	AllowSplit          bool `json:"allowSplit" gorm:"column:allow_split;not null"`
	MinSplitDurationMin int  `json:"minSplitDurationMin" gorm:"column:min_split_duration_min"`
	MaxSplitCount       int  `json:"maxSplitCount" gorm:"column:max_split_count"`

	IsPinned      bool       `json:"isPinned" gorm:"column:is_pinned;not null"`
	PinnedStartMs *time.Time `json:"pinnedStartMs" gorm:"column:pinned_start_ms"`
	PinnedEndMs   *time.Time `json:"pinnedEndMs" gorm:"column:pinned_end_ms"`

	DependentTaskIDs string `json:"dependentTaskIds" gorm:"column:dependent_task_ids"`
	BufferBeforeMin  int    `json:"bufferBeforeMin" gorm:"column:buffer_before_min"`
	BufferAfterMin   int    `json:"bufferAfterMin" gorm:"column:buffer_after_min"`

	ScheduleStatus    string  `json:"scheduleStatus" gorm:"column:schedule_status;not null"`
	UnscheduledReason *string `json:"unscheduledReason" gorm:"column:unscheduled_reason"`
}

func (ScheduleTaskModel) TableName() string {
	return "schedule_tasks"
}
