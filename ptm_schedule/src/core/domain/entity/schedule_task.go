/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"crypto/md5"
	"fmt"
	"io"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
)

type ScheduleTaskEntity struct {
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

// Snapshot & Change Detection

func (e *ScheduleTaskEntity) CalculateSnapshotHash() string {
	h := md5.New()

	io.WriteString(h, e.Title)
	io.WriteString(h, fmt.Sprintf("%d", e.DurationMin))
	io.WriteString(h, string(e.Priority))

	if e.DeadlineMs != nil {
		io.WriteString(h, fmt.Sprintf("%d", *e.DeadlineMs))
	}
	if e.EarliestStartMs != nil {
		io.WriteString(h, fmt.Sprintf("%d", *e.EarliestStartMs))
	}

	io.WriteString(h, fmt.Sprintf("%v-%d-%d", e.AllowSplit, e.MinSplitDurationMin, e.MaxSplitCount))

	io.WriteString(h, fmt.Sprintf("%v", e.DependentTaskIDs))

	return fmt.Sprintf("%x", h.Sum(nil))
}

func (e *ScheduleTaskEntity) HasConstraintsChanged(incomingHash string) bool {
	return e.TaskSnapshotHash != incomingHash
}

func (e *ScheduleTaskEntity) UpdateFromSource(title string, duration int, priority enum.Priority, deadline *int64) {
	e.Title = title
	e.DurationMin = duration
	e.Priority = priority
	e.DeadlineMs = deadline

	e.TaskSnapshotHash = e.CalculateSnapshotHash()
	e.ScheduleStatus = enum.ScheduleTaskPending
}

// Pinning & Locking

func (e *ScheduleTaskEntity) PinTo(startMs, endMs int64) {
	e.IsPinned = true
	e.PinnedStartMs = &startMs
	e.PinnedEndMs = &endMs

	// Khi đã Pin, coi như nó đã được xếp lịch (nhưng là kiểu Manual)
	e.ScheduleStatus = enum.ScheduleTaskScheduled
	e.UpdatedAt = time.Now().UnixMilli()
}

func (e *ScheduleTaskEntity) Unpin() {
	e.IsPinned = false
	e.PinnedStartMs = nil
	e.PinnedEndMs = nil
	e.ScheduleStatus = enum.ScheduleTaskPending
	e.UpdatedAt = time.Now().UnixMilli()
}

// Scoring

func (e *ScheduleTaskEntity) RecalculatePriorityScore(nowMs int64) {
	baseScore := 0.0
	switch e.Priority {
	case enum.PriorityHigh:
		baseScore = 100.0
	case enum.PriorityMedium:
		baseScore = 50.0
	case enum.PriorityLow:
		baseScore = 10.0
	}

	urgencyBoost := 0.0
	if e.DeadlineMs != nil {
		timeRemainingMin := (*e.DeadlineMs - nowMs) / 60000

		if timeRemainingMin <= 0 {
			urgencyBoost = 500.0
		} else if timeRemainingMin < 1440 {
			urgencyBoost = 200.0
		} else if timeRemainingMin < 4320 {
			urgencyBoost = 50.0
		}
	}

	deepWorkBonus := 0.0
	if e.IsDeepWork {
		deepWorkBonus = 20.0
	}

	e.PriorityScore = baseScore + urgencyBoost + deepWorkBonus
}

// business logic

func (e *ScheduleTaskEntity) IsValidChunk(durationMin int) bool {
	if !e.AllowSplit {
		return durationMin >= e.DurationMin
	}
	return durationMin >= e.MinSplitDurationMin
}

func (e *ScheduleTaskEntity) GetTotalDurationWithBuffer() int {
	return e.DurationMin + e.BufferBeforeMin + e.BufferAfterMin
}

func (e *ScheduleTaskEntity) IsOverdue(nowMs int64) bool {
	if e.DeadlineMs == nil {
		return false
	}
	return *e.DeadlineMs < nowMs
}

// Lifecycle Methods

func (e *ScheduleTaskEntity) MarkAsScheduled() {
	e.ScheduleStatus = enum.ScheduleTaskScheduled
	e.UnscheduledReason = nil
}

func (e *ScheduleTaskEntity) MarkAsFailed(reason string) {
	e.ScheduleStatus = enum.ScheduleTaskUnschedulable
	e.UnscheduledReason = &reason
}

func (e *ScheduleTaskEntity) ResetStatus() {
	e.ScheduleStatus = enum.ScheduleTaskPending
	e.UnscheduledReason = nil
}

func (e *ScheduleTaskEntity) Clone() *ScheduleTaskEntity {
	clone := &ScheduleTaskEntity{
		UserID:              e.UserID,
		TenantID:            e.TenantID,
		TaskID:              e.TaskID,
		TaskSnapshotHash:    e.TaskSnapshotHash,
		Title:               e.Title,
		DurationMin:         e.DurationMin,
		Priority:            e.Priority,
		PriorityScore:       e.PriorityScore,
		IsDeepWork:          e.IsDeepWork,
		AllowSplit:          e.AllowSplit,
		MinSplitDurationMin: e.MinSplitDurationMin,
		MaxSplitCount:       e.MaxSplitCount,
		IsPinned:            e.IsPinned,
		BufferBeforeMin:     e.BufferBeforeMin,
		BufferAfterMin:      e.BufferAfterMin,
		ScheduleStatus:      e.ScheduleStatus,
	}

	if e.Category != nil {
		cat := *e.Category
		clone.Category = &cat
	}
	if e.EarliestStartMs != nil {
		val := *e.EarliestStartMs
		clone.EarliestStartMs = &val
	}
	if e.DeadlineMs != nil {
		val := *e.DeadlineMs
		clone.DeadlineMs = &val
	}
	if e.PreferredStartMs != nil {
		val := *e.PreferredStartMs
		clone.PreferredStartMs = &val
	}
	if e.PinnedStartMs != nil {
		val := *e.PinnedStartMs
		clone.PinnedStartMs = &val
	}
	if e.PinnedEndMs != nil {
		val := *e.PinnedEndMs
		clone.PinnedEndMs = &val
	}
	if e.UnscheduledReason != nil {
		val := *e.UnscheduledReason
		clone.UnscheduledReason = &val
	}
	if len(e.DependentTaskIDs) > 0 {
		clone.DependentTaskIDs = make([]int64, len(e.DependentTaskIDs))
		copy(clone.DependentTaskIDs, e.DependentTaskIDs)
	}

	return clone
}
