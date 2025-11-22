/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectStatus string

const (
	ProjectNew        ProjectStatus = "NEW"
	ProjectInProgress ProjectStatus = "IN_PROGRESS"
	ProjectCompleted  ProjectStatus = "COMPLETED"
	ProjectArchived   ProjectStatus = "ARCHIVED"
	ProjectOnHold     ProjectStatus = "ON_HOLD"
)

func (s ProjectStatus) IsValid() bool {
	switch s {
	case ProjectNew, ProjectInProgress, ProjectCompleted, ProjectArchived, ProjectOnHold:
		return true
	}
	return false
}

func (s ProjectStatus) CanTransitionTo(newStatus ProjectStatus) bool {
	if !newStatus.IsValid() {
		return false
	}
	if s == newStatus {
		return false
	}
	if s.IsTerminal() {
		return false
	}

	switch s {
	case ProjectNew:
		return newStatus == ProjectInProgress || newStatus == ProjectOnHold || newStatus == ProjectArchived
	case ProjectInProgress:
		return newStatus == ProjectCompleted || newStatus == ProjectOnHold || newStatus == ProjectArchived
	case ProjectOnHold:
		return newStatus == ProjectInProgress || newStatus == ProjectArchived
	default:
		return false
	}
}

func (s ProjectStatus) IsTerminal() bool {
	return s == ProjectCompleted || s == ProjectArchived
}
