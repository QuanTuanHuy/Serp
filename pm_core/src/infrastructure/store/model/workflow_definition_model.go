/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkflowDefinitionModel struct {
	BaseModel

	ProjectID int64  `gorm:"not null;index:idx_workflow_project"`
	Name      string `gorm:"type:varchar(200);not null"`
	IsDefault bool   `gorm:"not null;default:false"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (WorkflowDefinitionModel) TableName() string {
	return "workflow_definitions"
}
