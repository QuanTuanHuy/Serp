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
)

type Priority string

const (
	Low    Priority = "LOW"
	Medium Priority = "MEDIUM"
	High   Priority = "HIGH"
	Star   Priority = "STAR"
)

type ResourceType string

const (
	ResourceProject ResourceType = "PROJECT"
	ResourceTask    ResourceType = "TASK"
	ResourceNote    ResourceType = "NOTE"
)
