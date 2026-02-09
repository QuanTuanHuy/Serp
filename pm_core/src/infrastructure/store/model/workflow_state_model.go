/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkflowStateModel struct {
	BaseModel

	WorkflowID int64   `gorm:"not null;index:idx_state_workflow"`
	Name       string  `gorm:"type:varchar(200);not null"`
	Category   string  `gorm:"type:varchar(20);not null;default:'TODO'"`
	StateOrder int     `gorm:"not null;default:0"`
	Color      *string `gorm:"type:varchar(20)"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (WorkflowStateModel) TableName() string {
	return "workflow_states"
}
