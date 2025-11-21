/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ProjectModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;index:idx_project_user_status,priority:1"`
	TenantID int64 `gorm:"not null"`

	Title       string  `gorm:"type:varchar(255);not null"`
	Description *string `gorm:"type:text"`

	Status       string `gorm:"type:varchar(20);not null;default:'ACTIVE';index:idx_project_user_status,priority:2"`
	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
	Priority     string `gorm:"type:varchar(20);not null;default:'MEDIUM';index:idx_project_user_status,priority:3"`

	StartDateMs *int64
	DeadlineMs  *int64

	ProgressPercentage int `gorm:"not null;default:0"`

	Color      *string `gorm:"type:varchar(20)"`
	Icon       *string `gorm:"type:varchar(50)"`
	IsFavorite bool    `gorm:"not null;default:false"`

	TotalTasks     int     `gorm:"not null;default:0"`
	CompletedTasks int     `gorm:"not null;default:0"`
	EstimatedHours float64 `gorm:"type:decimal(10,2);not null;default:0"`
	ActualHours    float64 `gorm:"type:decimal(10,2);not null;default:0"`
}

func (ProjectModel) TableName() string {
	return "projects"
}
