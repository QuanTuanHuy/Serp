/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkflowTransitionModel struct {
	BaseModel

	WorkflowID  int64  `gorm:"not null;index:idx_transition_workflow"`
	FromStateID int64  `gorm:"not null"`
	ToStateID   int64  `gorm:"not null"`
	Name        string `gorm:"type:varchar(200);not null"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (WorkflowTransitionModel) TableName() string {
	return "workflow_transitions"
}
