/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type WorkflowStateCategory string

const (
	StateCategoryTodo       WorkflowStateCategory = "TODO"
	StateCategoryInProgress WorkflowStateCategory = "IN_PROGRESS"
	StateCategoryDone       WorkflowStateCategory = "DONE"
)

func (w WorkflowStateCategory) IsValid() bool {
	switch w {
	case StateCategoryTodo, StateCategoryInProgress, StateCategoryDone:
		return true
	}
	return false
}
