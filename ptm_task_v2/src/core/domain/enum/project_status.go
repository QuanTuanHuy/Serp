/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectStatus string

const (
	ProjectActive    ProjectStatus = "ACTIVE"
	ProjectCompleted ProjectStatus = "COMPLETED"
	ProjectArchived  ProjectStatus = "ARCHIVED"
	ProjectOnHold    ProjectStatus = "ON_HOLD"
)

func (s ProjectStatus) IsValid() bool {
	switch s {
	case ProjectActive, ProjectCompleted, ProjectArchived, ProjectOnHold:
		return true
	}
	return false
}

func (s ProjectStatus) CanTransitionTo(newStatus ProjectStatus) bool {
	switch s {
	case ProjectActive:
		return true
	case ProjectOnHold:
		return newStatus == ProjectActive || newStatus == ProjectArchived || newStatus == ProjectCompleted
	case ProjectCompleted:
		return newStatus == ProjectArchived || newStatus == ProjectActive
	case ProjectArchived:
		return newStatus == ProjectActive
	}
	return false
}

func (s ProjectStatus) IsTerminal() bool {
	return s == ProjectCompleted || s == ProjectArchived
}
