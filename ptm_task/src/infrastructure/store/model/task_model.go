/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "gorm.io/datatypes"

type TaskModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;index:idx_task_user_status,priority:1"`
	TenantID int64 `gorm:"not null"`

	Title       string  `gorm:"type:varchar(500);not null"`
	Description *string `gorm:"type:text"`

	Priority      string   `gorm:"type:varchar(20);not null;default:'MEDIUM';index:idx_task_user_status,priority:3"`
	PriorityScore *float64 `gorm:"type:decimal(5,2)"`

	EstimatedDurationMin *int
	ActualDurationMin    *int
	IsDurationLearned    bool `gorm:"not null;default:false"`

	PreferredStartDateMs *int64
	DeadlineMs           *int64
	EarliestStartMs      *int64

	Category *string        `gorm:"type:varchar(100)"`
	Tags     datatypes.JSON `gorm:"type:jsonb;index:idx_task_tags,type:gin"`

	ParentTaskID *int64 `gorm:"index:idx_task_parent"`

	ProjectID *int64 `gorm:"index:idx_task_project_status,priority:1"`

	IsRecurring           bool    `gorm:"not null;default:false"`
	RecurrencePattern     *string `gorm:"type:varchar(50)"`
	RecurrenceConfig      *string `gorm:"type:text"`
	ParentRecurringTaskID *int64

	IsDeepWork bool `gorm:"not null;default:false"`
	IsMeeting  bool `gorm:"not null;default:false"`
	IsFlexible bool `gorm:"not null;default:true"`

	Status       string `gorm:"type:varchar(20);not null;default:'TODO';index:idx_task_user_status,priority:2;index:idx_task_project_status,priority:2"`
	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`

	ExternalID *string `gorm:"type:varchar(100);index:idx_task_external_id,unique"`
	Source     string  `gorm:"type:varchar(50);not null;default:'manual'"`

	CompletedAt *int64
}

func (TaskModel) TableName() string {
	return "tasks"
}
