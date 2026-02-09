/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type WorkflowStateEntity struct {
	BaseEntity

	WorkflowID int64   `json:"workflowId"`
	Name       string  `json:"name"`
	Category   string  `json:"category"`
	StateOrder int     `json:"stateOrder"`
	Color      *string `json:"color,omitempty"`

	ActiveStatus string `json:"activeStatus"`
}

func NewWorkflowStateEntity() *WorkflowStateEntity {
	return &WorkflowStateEntity{
		Category:     string(enum.StateCategoryTodo),
		ActiveStatus: string(enum.Active),
	}
}

func (s *WorkflowStateEntity) IsTodo() bool {
	return enum.WorkflowStateCategory(s.Category) == enum.StateCategoryTodo
}

func (s *WorkflowStateEntity) IsInProgress() bool {
	return enum.WorkflowStateCategory(s.Category) == enum.StateCategoryInProgress
}

func (s *WorkflowStateEntity) IsDone() bool {
	return enum.WorkflowStateCategory(s.Category) == enum.StateCategoryDone
}
