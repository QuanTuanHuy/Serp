/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ActivityAction string

const (
	ActionCreated       ActivityAction = "CREATED"
	ActionUpdated       ActivityAction = "UPDATED"
	ActionDeleted       ActivityAction = "DELETED"
	ActionStatusChanged ActivityAction = "STATUS_CHANGED"
	ActionAssigned      ActivityAction = "ASSIGNED"
	ActionUnassigned    ActivityAction = "UNASSIGNED"
	ActionCommented     ActivityAction = "COMMENTED"
	ActionMovedSprint   ActivityAction = "MOVED_SPRINT"
	ActionMovedBoard    ActivityAction = "MOVED_BOARD"
)

func (a ActivityAction) IsValid() bool {
	switch a {
	case ActionCreated, ActionUpdated, ActionDeleted, ActionStatusChanged,
		ActionAssigned, ActionUnassigned, ActionCommented,
		ActionMovedSprint, ActionMovedBoard:
		return true
	}
	return false
}
