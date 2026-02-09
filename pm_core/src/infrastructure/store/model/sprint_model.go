/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type SprintModel struct {
	BaseModel

	ProjectID int64   `gorm:"not null;index:idx_sprint_project"`
	Name      string  `gorm:"type:varchar(200);not null"`
	Goal      *string `gorm:"type:text"`

	Status string `gorm:"type:varchar(20);not null;default:'PLANNING'"`

	StartDateMs *int64
	EndDateMs   *int64

	SprintOrder int `gorm:"not null;default:0"`

	// Denormalized stats
	TotalWorkItems     int `gorm:"not null;default:0"`
	TotalPoints        int `gorm:"not null;default:0"`
	CompletedWorkItems int `gorm:"not null;default:0"`
	CompletedPoints    int `gorm:"not null;default:0"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (SprintModel) TableName() string {
	return "sprints"
}
