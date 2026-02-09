/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ProjectModel struct {
	BaseModel

	TenantID int64 `gorm:"not null;index:idx_project_tenant_key,priority:1"`

	Name        string  `gorm:"type:varchar(200);not null"`
	Key         string  `gorm:"type:varchar(10);not null;index:idx_project_tenant_key,priority:2"`
	Description *string `gorm:"type:text"`

	Status       string `gorm:"type:varchar(20);not null;default:'PLANNING'"`
	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
	Visibility   string `gorm:"type:varchar(20);not null;default:'PRIVATE'"`

	StartDateMs *int64
	DeadlineMs  *int64

	NextItemNumber int `gorm:"not null;default:1"`

	TotalWorkItems     int `gorm:"not null;default:0"`
	CompletedWorkItems int `gorm:"not null;default:0"`
}

func (ProjectModel) TableName() string {
	return "projects"
}
