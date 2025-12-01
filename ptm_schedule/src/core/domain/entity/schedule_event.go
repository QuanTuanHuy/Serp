/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"fmt"
	"slices"
	"time"

	"github.com/serp/ptm-schedule/src/core/domain/enum"
	"github.com/serp/ptm-schedule/src/kernel/utils/mathutils"
)

const DefaultMinSplitDuration = 30

type ScheduleEventEntity struct {
	BaseEntity
	SchedulePlanID int64 `json:"schedulePlanId"`
	ScheduleTaskID int64 `json:"scheduleTaskId"`

	DateMs   int64 `json:"dateMs"`
	StartMin int   `json:"startMin"`
	EndMin   int   `json:"endMin"`

	Title string `json:"title"`

	PartIndex     int    `json:"partIndex"`
	TotalParts    int    `json:"totalParts"`
	LinkedEventID *int64 `json:"linkedEventId"`

	Status   enum.ScheduleEventStatus `json:"status"`
	IsPinned bool                     `json:"isPinned"`

	UtilityScore *float64 `json:"utilityScore"`

	ActualStartMin *int `json:"actualStartMin"`
	ActualEndMin   *int `json:"actualEndMin"`
}

// validStatusTransitions defines allowed status transitions
var validStatusTransitions = map[enum.ScheduleEventStatus][]enum.ScheduleEventStatus{
	enum.ScheduleEventPlanned: {enum.ScheduleEventDone, enum.ScheduleEventSkipped},
	enum.ScheduleEventDone:    {},
	enum.ScheduleEventSkipped: {enum.ScheduleEventPlanned},
}

// Factory methods

func NewScheduleEvent(planID, taskID int64, dateMs int64, startMin, endMin int, title string) *ScheduleEventEntity {
	return &ScheduleEventEntity{
		SchedulePlanID: planID,
		ScheduleTaskID: taskID,
		DateMs:         dateMs,
		StartMin:       startMin,
		EndMin:         endMin,
		Title:          title,
		PartIndex:      1,
		TotalParts:     1,
		Status:         enum.ScheduleEventPlanned,
		IsPinned:       false,
	}
}

// Basic checks

func (e *ScheduleEventEntity) IsNew() bool {
	return e.ID == 0
}

func (e *ScheduleEventEntity) IsValid() bool {
	return e.SchedulePlanID > 0 &&
		e.ScheduleTaskID > 0 &&
		e.DateMs > 0 &&
		e.StartMin >= 0 && e.EndMin <= 24*60 &&
		e.StartMin < e.EndMin &&
		e.PartIndex >= 1 &&
		e.TotalParts >= 1 &&
		e.PartIndex <= e.TotalParts
}

func (e *ScheduleEventEntity) BelongsToPlan(planID int64) bool {
	return e.SchedulePlanID == planID
}

func (e *ScheduleEventEntity) DurationMinutes() int {
	return e.EndMin - e.StartMin
}

// Status checks

func (e *ScheduleEventEntity) IsPlanned() bool {
	return e.Status == enum.ScheduleEventPlanned
}

func (e *ScheduleEventEntity) IsDone() bool {
	return e.Status == enum.ScheduleEventDone
}

func (e *ScheduleEventEntity) IsSkipped() bool {
	return e.Status == enum.ScheduleEventSkipped
}

func (e *ScheduleEventEntity) IsTerminal() bool {
	return e.Status == enum.ScheduleEventDone
}

func (e *ScheduleEventEntity) CanBeModified() bool {
	return e.Status == enum.ScheduleEventPlanned
}

func (e *ScheduleEventEntity) HasActualTimes() bool {
	return e.ActualStartMin != nil && e.ActualEndMin != nil
}

// Status transitions

func (e *ScheduleEventEntity) CanTransitionTo(newStatus enum.ScheduleEventStatus) bool {
	allowedStatuses, exists := validStatusTransitions[e.Status]
	if !exists {
		return false
	}
	return slices.Contains(allowedStatuses, newStatus)
}

func (e *ScheduleEventEntity) SetStatus(newStatus enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) bool {
	if !e.CanTransitionTo(newStatus) {
		return false
	}

	e.Status = newStatus
	e.ActualStartMin = actualStartMin
	e.ActualEndMin = actualEndMin
	e.UpdatedAt = time.Now().UnixMilli()
	return true
}

// Multi-part checks

func (e *ScheduleEventEntity) IsMultiPart() bool {
	return e.TotalParts > 1
}

func (e *ScheduleEventEntity) IsFirstPart() bool {
	return e.PartIndex == 1
}

func (e *ScheduleEventEntity) IsLastPart() bool {
	return e.PartIndex == e.TotalParts
}

func (e *ScheduleEventEntity) HasLinkedEvents() bool {
	return e.LinkedEventID != nil || e.TotalParts > 1
}

// Overlap detection

func (e *ScheduleEventEntity) OverlapsWith(other *ScheduleEventEntity) bool {
	if e.DateMs != other.DateMs || e.SchedulePlanID != other.SchedulePlanID {
		return false
	}
	return mathutils.Max(e.StartMin, other.StartMin) < mathutils.Min(e.EndMin, other.EndMin)
}

func (e *ScheduleEventEntity) ContainsTime(dateMs int64, timeMin int) bool {
	return e.DateMs == dateMs && e.StartMin <= timeMin && timeMin < e.EndMin
}

// Movement operations

func (e *ScheduleEventEntity) MoveAndPin(newDateMs int64, newStart, newEnd int) error {
	if newStart >= newEnd || newStart < 0 || newEnd > 1440 {
		return fmt.Errorf("invalid time range: %d-%d", newStart, newEnd)
	}

	if !e.CanBeModified() {
		return fmt.Errorf("cannot move event with status: %s", e.Status)
	}

	e.DateMs = newDateMs
	e.StartMin = newStart
	e.EndMin = newEnd
	e.IsPinned = true
	e.UpdatedAt = time.Now().UnixMilli()
	return nil
}

func (e *ScheduleEventEntity) Unpin() {
	e.IsPinned = false
	e.UpdatedAt = time.Now().UnixMilli()
}

func (e *ScheduleEventEntity) Resize(newStart, newEnd int) error {
	return e.MoveAndPin(e.DateMs, newStart, newEnd)
}

// Completion

func (e *ScheduleEventEntity) MarkDone(actualStart, actualEnd int) error {
	if !e.CanTransitionTo(enum.ScheduleEventDone) {
		return fmt.Errorf("cannot mark as done from status: %s", e.Status)
	}

	if actualStart < 0 || actualEnd > 1440 || actualStart >= actualEnd {
		return fmt.Errorf("invalid actual time range: %d-%d", actualStart, actualEnd)
	}

	e.Status = enum.ScheduleEventDone
	e.ActualStartMin = &actualStart
	e.ActualEndMin = &actualEnd
	e.UpdatedAt = time.Now().UnixMilli()
	return nil
}

func (e *ScheduleEventEntity) MarkSkipped() error {
	if !e.CanTransitionTo(enum.ScheduleEventSkipped) {
		return fmt.Errorf("cannot skip from status: %s", e.Status)
	}

	e.Status = enum.ScheduleEventSkipped
	e.UpdatedAt = time.Now().UnixMilli()
	return nil
}

func (e *ScheduleEventEntity) Reschedule() error {
	if e.Status != enum.ScheduleEventSkipped {
		return fmt.Errorf("can only reschedule skipped events, current status: %s", e.Status)
	}

	e.Status = enum.ScheduleEventPlanned
	e.UpdatedAt = time.Now().UnixMilli()
	return nil
}

// Split operation (basic - Service handles re-indexing)

func (e *ScheduleEventEntity) CanSplit(minSplitDuration int) bool {
	if minSplitDuration <= 0 {
		minSplitDuration = DefaultMinSplitDuration
	}
	return e.CanBeModified() && e.DurationMinutes() >= 2*minSplitDuration
}

func (e *ScheduleEventEntity) Split(splitPointMin int, minSplitDuration int) (*ScheduleEventEntity, error) {
	if minSplitDuration <= 0 {
		minSplitDuration = DefaultMinSplitDuration
	}

	if !e.CanBeModified() {
		return nil, fmt.Errorf("cannot split event with status: %s", e.Status)
	}

	durationBefore := splitPointMin - e.StartMin
	durationAfter := e.EndMin - splitPointMin

	if durationBefore < minSplitDuration || durationAfter < minSplitDuration {
		return nil, fmt.Errorf("split would create parts smaller than minimum %d minutes", minSplitDuration)
	}

	originalEnd := e.EndMin

	e.EndMin = splitPointMin
	e.UpdatedAt = time.Now().UnixMilli()

	// Note: PartIndex and TotalParts will be handled by Service layer
	newPart := &ScheduleEventEntity{
		SchedulePlanID: e.SchedulePlanID,
		ScheduleTaskID: e.ScheduleTaskID,
		DateMs:         e.DateMs,
		StartMin:       splitPointMin,
		EndMin:         originalEnd,
		Title:          e.Title,
		PartIndex:      e.PartIndex + 1,
		TotalParts:     e.TotalParts + 1,
		Status:         enum.ScheduleEventPlanned,
		IsPinned:       e.IsPinned,
		LinkedEventID:  &e.ID,
	}

	return newPart, nil
}

// Utility calculation helpers

func (e *ScheduleEventEntity) GetActualDuration() int {
	if e.ActualStartMin != nil && e.ActualEndMin != nil {
		return *e.ActualEndMin - *e.ActualStartMin
	}
	return 0
}

func (e *ScheduleEventEntity) GetDurationVariance() int {
	if !e.HasActualTimes() {
		return 0
	}
	return e.GetActualDuration() - e.DurationMinutes()
}
