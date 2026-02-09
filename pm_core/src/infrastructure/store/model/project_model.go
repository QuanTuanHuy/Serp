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

	Status          string `gorm:"type:varchar(20);not null;default:'PLANNING'"`
	ActiveStatus    string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
	Visibility      string `gorm:"type:varchar(20);not null;default:'PRIVATE'"`
	MethodologyType string `gorm:"type:varchar(20);not null;default:'KANBAN'"`
	Priority        string `gorm:"type:varchar(20);not null;default:'MEDIUM'"`

	StartDateMs     *int64
	TargetEndDateMs *int64

	Color *string `gorm:"type:varchar(20)"`
	Icon  *string `gorm:"type:varchar(50)"`

	DefaultBoardID    *int64
	DefaultWorkflowID *int64

	NextItemNumber int `gorm:"not null;default:1"`

	// Denormalized stats
	TotalWorkItems     int `gorm:"not null;default:0"`
	CompletedWorkItems int `gorm:"not null;default:0"`
	ProgressPercentage int `gorm:"not null;default:0"`
	TotalMembers       int `gorm:"not null;default:0"`

	CreatedBy int64 `gorm:"not null"`
}

func (ProjectModel) TableName() string {
	return "projects"
}
