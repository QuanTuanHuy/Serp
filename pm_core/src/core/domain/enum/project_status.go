/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectStatus string

const (
	ProjectPlanning  ProjectStatus = "PLANNING"
	ProjectActive    ProjectStatus = "ACTIVE"
	ProjectOnHold    ProjectStatus = "ON_HOLD"
	ProjectCompleted ProjectStatus = "COMPLETED"
	ProjectArchived  ProjectStatus = "ARCHIVED"
)

func (s ProjectStatus) IsValid() bool {
	switch s {
	case ProjectPlanning, ProjectActive, ProjectOnHold, ProjectCompleted, ProjectArchived:
		return true
	}
	return false
}

var validProjectTransitions = map[ProjectStatus][]ProjectStatus{
	ProjectPlanning:  {ProjectActive},
	ProjectActive:    {ProjectOnHold, ProjectCompleted, ProjectArchived},
	ProjectOnHold:    {ProjectActive, ProjectArchived},
	ProjectCompleted: {ProjectArchived},
	ProjectArchived:  {},
}

func (s ProjectStatus) CanTransitionTo(target ProjectStatus) bool {
	allowed, ok := validProjectTransitions[s]
	if !ok {
		return false
	}
	for _, a := range allowed {
		if a == target {
			return true
		}
	}
	return false
}

func (s ProjectStatus) IsTerminal() bool {
	return s == ProjectArchived
}

func (s ProjectStatus) IsEditable() bool {
	return s == ProjectPlanning || s == ProjectActive || s == ProjectOnHold
}

func (s ProjectStatus) IsActive() bool {
	return s == ProjectActive
}
