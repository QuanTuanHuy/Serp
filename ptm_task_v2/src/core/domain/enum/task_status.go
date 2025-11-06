/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type TaskStatus string

const (
	StatusTodo       TaskStatus = "TODO"
	StatusInProgress TaskStatus = "IN_PROGRESS"
	StatusDone       TaskStatus = "DONE"
	StatusCancelled  TaskStatus = "CANCELLED"
	StatusArchived   TaskStatus = "ARCHIVED"
)

func (s TaskStatus) IsValid() bool {
	switch s {
	case StatusTodo, StatusInProgress, StatusDone, StatusCancelled, StatusArchived:
		return true
	}
	return false
}

func (s TaskStatus) IsCompleted() bool {
	return s == StatusDone || s == StatusCancelled || s == StatusArchived
}

func (s TaskStatus) CanTransitionTo(newStatus TaskStatus) bool {
	switch s {
	case StatusTodo:
		return newStatus == StatusInProgress || newStatus == StatusDone || newStatus == StatusCancelled
	case StatusInProgress:
		return newStatus == StatusTodo || newStatus == StatusDone || newStatus == StatusCancelled
	case StatusDone:
		return newStatus == StatusTodo || newStatus == StatusInProgress || newStatus == StatusArchived
	case StatusCancelled:
		return newStatus == StatusTodo || newStatus == StatusArchived
	case StatusArchived:
		return newStatus == StatusTodo
	default:
		return false
	}
}
