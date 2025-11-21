/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "gorm.io/datatypes"

type TaskTemplateModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;index:idx_task_template_user"`
	TenantID int64 `gorm:"not null"`

	TemplateName string  `gorm:"type:varchar(255);not null;index:idx_task_template_name"`
	Description  *string `gorm:"type:text"`

	TitleTemplate        string            `gorm:"type:varchar(500);not null"`
	EstimatedDurationMin int               `gorm:"not null"`
	Priority             string            `gorm:"type:varchar(20);not null;default:'MEDIUM'"`
	Category             *string           `gorm:"type:varchar(100)"`
	Tags                 datatypes.JSONMap `gorm:"type:jsonb"`
	IsDeepWork           bool              `gorm:"not null;default:false"`

	PreferredTimeOfDay *string           `gorm:"type:varchar(20)"`
	PreferredDays      datatypes.JSONMap `gorm:"type:jsonb"`

	RecurrencePattern *string `gorm:"type:varchar(50)"`
	RecurrenceConfig  *string `gorm:"type:text"`

	UsageCount int `gorm:"not null;default:0"`
	LastUsedAt *int64
	IsFavorite bool `gorm:"not null;default:false"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (TaskTemplateModel) TableName() string {
	return "task_templates"
}
