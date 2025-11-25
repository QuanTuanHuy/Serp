package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

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

func (e *ScheduleEventEntity) IsNew() bool {
	return e.ID == 0
}

func (e *ScheduleEventEntity) IsValid() bool {
	return e.SchedulePlanID > 0 &&
		e.ScheduleTaskID > 0 &&
		e.DateMs > 0 &&
		e.StartMin >= 0 && e.EndMin <= 24*60 &&
		e.StartMin < e.EndMin
}

func (e *ScheduleEventEntity) BelongsToPlan(planID int64) bool {
	return e.SchedulePlanID == planID
}

func (e *ScheduleEventEntity) DurationMinutes() int {
	return e.EndMin - e.StartMin
}

func (e *ScheduleEventEntity) IsPlanned() bool {
	return e.Status == enum.PLANNED
}

func (e *ScheduleEventEntity) IsDone() bool {
	return e.Status == enum.DONE
}

func (e *ScheduleEventEntity) IsSkipped() bool {
	return e.Status == enum.SKIPPED
}

func (e *ScheduleEventEntity) HasActualTimes() bool {
	return e.ActualStartMin != nil && e.ActualEndMin != nil
}

func (e *ScheduleEventEntity) CanTransitionTo(newStatus enum.ScheduleEventStatus) bool {
	if e.Status == enum.PLANNED {
		return newStatus == enum.DONE || newStatus == enum.SKIPPED
	}
	return false
}

func (e *ScheduleEventEntity) SetStatus(newStatus enum.ScheduleEventStatus, actualStartMin, actualEndMin *int) bool {
	if !e.CanTransitionTo(newStatus) {
		return false
	}

	if newStatus == enum.DONE {
		if actualStartMin == nil || actualEndMin == nil {
			return false
		}
		if *actualStartMin < 0 || *actualEndMin > 24*60 || *actualStartMin >= *actualEndMin {
			return false
		}
		e.ActualStartMin = actualStartMin
		e.ActualEndMin = actualEndMin
	}

	e.Status = newStatus
	return true
}

func (e *ScheduleEventEntity) OverlapsWith(other *ScheduleEventEntity) bool {
	if e.DateMs != other.DateMs || e.SchedulePlanID != other.SchedulePlanID {
		return false
	}
	return e.StartMin < other.EndMin && other.StartMin < e.EndMin
}
