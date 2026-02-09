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

	Status       string `gorm:"type:varchar(20);not null;default:'PENDING'"`
	TargetDateMs *int64

	// Denormalized stats
	TotalWorkItems     int `gorm:"not null;default:0"`
	CompletedWorkItems int `gorm:"not null;default:0"`
	ProgressPercentage int `gorm:"not null;default:0"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (MilestoneModel) TableName() string {
	return "milestones"
}
