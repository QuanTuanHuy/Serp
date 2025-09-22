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
	Star   Priority = "STAR"
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
	EAT    Tag = "EAT"
	SLEEP  Tag = "SLEEP"
	TRAVEL Tag = "TRAVEL"
	RELAX  Tag = "RELAX"
)
