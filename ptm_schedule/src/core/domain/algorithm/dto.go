/*
Author: QuanTuanHuy
Description: Part of Serp Project - Algorithm DTOs
*/

package algorithm

import "github.com/serp/ptm-schedule/src/core/domain/enum"

// TaskInput represents a task to be scheduled
type TaskInput struct {
	TaskID          int64
	ScheduleTaskID  int64
	Title           string
	DurationMin     int
	Priority        enum.Priority
	PriorityScore   float64
	IsDeepWork      bool
	EarliestStartMs *int64
	DeadlineMs      *int64
	AllowSplit      bool
	MinSplitMin     int
	MaxSplitCount   int
	BufferBeforeMin int
	BufferAfterMin  int
	IsPinned        bool
	IsCompleted     bool
	PinnedDateMs    *int64
	PinnedStartMin  *int
	PinnedEndMin    *int
}

func (t *TaskInput) GetEffectiveDuration() int {
	return t.DurationMin + t.BufferBeforeMin + t.BufferAfterMin
}

func (t *TaskInput) CanSplit() bool {
	return t.AllowSplit && t.DurationMin >= 2*t.MinSplitMin
}

// Window represents an available time slot
type Window struct {
	DateMs      int64
	StartMin    int
	EndMin      int
	IsDeepWork  bool
	WindowScore float64
}

func (w *Window) Duration() int {
	return w.EndMin - w.StartMin
}

func (w *Window) Contains(startMin, endMin int) bool {
	return w.StartMin <= startMin && endMin <= w.EndMin
}

func (w *Window) Overlaps(other *Window) bool {
	if w.DateMs != other.DateMs {
		return false
	}
	return max(w.StartMin, other.StartMin) < min(w.EndMin, other.EndMin)
}

// Assignment represents a scheduled time block
type Assignment struct {
	EventID        *int64 // nil for new assignments
	TaskID         int64
	ScheduleTaskID int64
	DateMs         int64
	StartMin       int
	EndMin         int
	PartIndex      int
	TotalParts     int
	IsPinned       bool
	Status         *string
	UtilityScore   float64
	Title          string
}

func (a *Assignment) IsCompleted() bool {
	if a.Status != nil && *a.Status == "DONE" {
		return true
	}
	return false
}

func (a *Assignment) Duration() int {
	return a.EndMin - a.StartMin
}

func (a *Assignment) Overlaps(other *Assignment) bool {
	if a.DateMs != other.DateMs {
		return false
	}
	return max(a.StartMin, other.StartMin) < min(a.EndMin, other.EndMin)
}

func (a *Assignment) OverlapsTimeRange(dateMs int64, startMin, endMin int) bool {
	if a.DateMs != dateMs {
		return false
	}
	return max(a.StartMin, startMin) < min(a.EndMin, endMin)
}

// ScheduleInput represents input for the scheduler
type ScheduleInput struct {
	PlanID         int64
	UserID         int64
	Tasks          []*TaskInput
	Windows        []*Window
	ExistingEvents []*Assignment
}

// UnscheduledTask represents a task that couldn't be scheduled
type UnscheduledTask struct {
	TaskID         int64
	ScheduleTaskID int64
	Reason         string
}

// ScheduleMetrics contains scheduling statistics
type ScheduleMetrics struct {
	TotalTasks       int
	ScheduledTasks   int
	UnscheduledTasks int
	TotalDurationMin int
	UsedDurationMin  int
	UtilizationPct   float64
}

// ScheduleOutput represents output from the scheduler
type ScheduleOutput struct {
	Assignments      []*Assignment
	UnscheduledTasks []*UnscheduledTask
	Metrics          *ScheduleMetrics
}

func NewScheduleOutput() *ScheduleOutput {
	return &ScheduleOutput{
		Assignments:      make([]*Assignment, 0),
		UnscheduledTasks: make([]*UnscheduledTask, 0),
		Metrics:          &ScheduleMetrics{},
	}
}
