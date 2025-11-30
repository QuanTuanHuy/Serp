/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type Status string

const (
	ToDo       Status = "TODO"
	Inprogress Status = "IN_PROGRESS"
	Done       Status = "DONE"
	Pending    Status = "PENDING"
	Archived   Status = "ARCHIVED"
)

type ActiveStatus string

const (
	Active   ActiveStatus = "ACTIVE"
	Inactive ActiveStatus = "INACTIVE"
	Draft    ActiveStatus = "DRAFT"
)

type Priority string

const (
	PriorityLow    Priority = "LOW"
	PriorityMedium Priority = "MEDIUM"
	PriorityHigh   Priority = "HIGH"
)

func (p Priority) IsValid() bool {
	switch p {
	case PriorityLow, PriorityMedium, PriorityHigh:
		return true
	default:
		return false
	}
}

type RepeatLevel string

const (
	None    RepeatLevel = "NONE"
	Daily   RepeatLevel = "DAILY"
	Weekly  RepeatLevel = "WEEKLY"
	Monthly RepeatLevel = "MONTHLY"
	Yearly  RepeatLevel = "YEARLY"
)

type ScheduleEventStatus string

const (
	ScheduleEventPlanned ScheduleEventStatus = "PLANNED"
	ScheduleEventDone    ScheduleEventStatus = "DONE"
	ScheduleEventSkipped ScheduleEventStatus = "SKIPPED"
)
