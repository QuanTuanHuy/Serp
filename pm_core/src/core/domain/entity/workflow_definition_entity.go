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
}

func NewWorkflowDefinitionEntity() *WorkflowDefinitionEntity {
	return &WorkflowDefinitionEntity{
		IsDefault:    false,
		ActiveStatus: string(enum.Active),
	}
}
