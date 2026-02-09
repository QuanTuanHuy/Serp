/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type WorkItemModel struct {
	BaseModel

	ProjectID   int64   `gorm:"not null;index:idx_workitem_project"`
	ItemNumber  int     `gorm:"not null"`
	Title       string  `gorm:"type:varchar(500);not null"`
	Description *string `gorm:"type:text"`

	Type     string `gorm:"type:varchar(20);not null"`
	Status   string `gorm:"type:varchar(20);not null;default:'TODO'"`
	Priority string `gorm:"type:varchar(20);not null;default:'MEDIUM'"`

	ParentID      *int64 `gorm:"index:idx_workitem_parent"`
	SprintID      *int64 `gorm:"index:idx_workitem_sprint"`
	MilestoneID   *int64
	BoardColumnID *int64

	ReporterID int64 `gorm:"not null"`

	StoryPoints    *int
	EstimatedHours *float64 `gorm:"type:decimal(10,2)"`

	StartDateMs *int64
	DueDateMs   *int64

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
	Position     int    `gorm:"not null;default:0"`

	// Denormalized
	HasChildren         bool `gorm:"not null;default:false"`
	ChildCount          int  `gorm:"not null;default:0"`
	CompletedChildCount int  `gorm:"not null;default:0"`
	CommentCount        int  `gorm:"not null;default:0"`
}

func (WorkItemModel) TableName() string {
	return "work_items"
}
