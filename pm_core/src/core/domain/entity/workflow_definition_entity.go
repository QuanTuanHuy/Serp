/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type WorkflowDefinitionEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	Name      string `json:"name"`
	IsDefault bool   `json:"isDefault"`

	ActiveStatus string `json:"activeStatus"`

	States      []*WorkflowStateEntity      `json:"states,omitempty"`
	Transitions []*WorkflowTransitionEntity `json:"transitions,omitempty"`
}

func NewWorkflowDefinitionEntity() *WorkflowDefinitionEntity {
	return &WorkflowDefinitionEntity{
		IsDefault:    false,
		ActiveStatus: string(enum.Active),
	}
}

func (w *WorkflowDefinitionEntity) GetInitialState() *WorkflowStateEntity {
	if len(w.States) == 0 {
		return nil
	}
	initial := w.States[0]
	for _, s := range w.States[1:] {
		if s.StateOrder < initial.StateOrder {
			initial = s
		}
	}
	return initial
}

func (w *WorkflowDefinitionEntity) FindStateByID(stateID int64) *WorkflowStateEntity {
	for _, s := range w.States {
		if s.ID == stateID {
			return s
		}
	}
	return nil
}

func (w *WorkflowDefinitionEntity) IsTransitionAllowed(fromStateID, toStateID int64) bool {
	for _, t := range w.Transitions {
		if t.MatchesTransition(fromStateID, toStateID) {
			return true
		}
	}
	return false
}
