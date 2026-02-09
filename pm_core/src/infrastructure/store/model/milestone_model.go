/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type MilestoneModel struct {
	BaseModel

	ProjectID   int64   `gorm:"not null;index:idx_milestone_project"`
	Name        string  `gorm:"type:varchar(200);not null"`
	Description *string `gorm:"type:text"`

	DueDateMs *int64

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (MilestoneModel) TableName() string {
	return "milestones"
}
