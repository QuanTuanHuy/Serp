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
