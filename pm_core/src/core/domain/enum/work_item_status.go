/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type WorkItemStatus string

const (
	WorkItemTodo       WorkItemStatus = "TODO"
	WorkItemInProgress WorkItemStatus = "IN_PROGRESS"
	WorkItemInReview   WorkItemStatus = "IN_REVIEW"
	WorkItemDone       WorkItemStatus = "DONE"
	WorkItemCancelled  WorkItemStatus = "CANCELLED"
)

func (s WorkItemStatus) IsValid() bool {
	switch s {
	case WorkItemTodo, WorkItemInProgress, WorkItemInReview, WorkItemDone, WorkItemCancelled:
		return true
	}
	return false
}

func (s WorkItemStatus) IsCompleted() bool {
	return s == WorkItemDone
}

func (s WorkItemStatus) IsCancelled() bool {
	return s == WorkItemCancelled
}

func (s WorkItemStatus) IsTerminal() bool {
	return s == WorkItemDone || s == WorkItemCancelled
}
