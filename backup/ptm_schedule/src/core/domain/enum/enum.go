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
	Low    Priority = "LOW"
	Medium Priority = "MEDIUM"
	High   Priority = "HIGH"
)

type RepeatLevel string

const (
	None    RepeatLevel = "NONE"
	Daily   RepeatLevel = "DAILY"
	Weekly  RepeatLevel = "WEEKLY"
	Monthly RepeatLevel = "MONTHLY"
	Yearly  RepeatLevel = "YEARLY"
)

type Tag string

const (
	Work   Tag = "WORK"
	Eat    Tag = "EAT"
	Sleep  Tag = "SLEEP"
	Travel Tag = "TRAVEL"
	Relax  Tag = "RELAX"
	Other  Tag = "OTHER"
)

type ScheduleEventStatus string

const (
	PLANNED ScheduleEventStatus = "PLANNED"
	DONE    ScheduleEventStatus = "DONE"
	SKIPPED ScheduleEventStatus = "SKIPPED"
)
