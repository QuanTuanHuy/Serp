/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type WorkflowTransitionEntity struct {
	BaseEntity

	WorkflowID  int64  `json:"workflowId"`
	FromStateID int64  `json:"fromStateId"`
	ToStateID   int64  `json:"toStateId"`
	Name        string `json:"name"`

	ActiveStatus string `json:"activeStatus"`
}

func NewWorkflowTransitionEntity() *WorkflowTransitionEntity {
	return &WorkflowTransitionEntity{
		ActiveStatus: string(enum.Active),
	}
}

func (t *WorkflowTransitionEntity) MatchesTransition(fromStateID, toStateID int64) bool {
	return t.FromStateID == fromStateID && t.ToStateID == toStateID
}
