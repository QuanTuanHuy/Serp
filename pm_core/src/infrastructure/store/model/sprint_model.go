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

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (SprintModel) TableName() string {
	return "sprints"
}
