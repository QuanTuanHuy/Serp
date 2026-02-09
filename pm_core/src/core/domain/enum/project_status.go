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
