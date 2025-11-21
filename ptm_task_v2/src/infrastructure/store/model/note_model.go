/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "gorm.io/datatypes"

type NoteModel struct {
	BaseModel
	UserID   int64 `gorm:"not null;index:idx_note_user"`
	TenantID int64 `gorm:"not null"`

	TaskID    *int64 `gorm:"index:idx_note_task"`
	ProjectID *int64 `gorm:"index:idx_note_project"`

	Content      string  `gorm:"type:text;not null"`
	ContentPlain *string `gorm:"type:text"`

	Attachments datatypes.JSON `gorm:"type:jsonb"`

	IsPinned bool `gorm:"not null;default:false"`

	ActiveStatus string `gorm:"type:varchar(20);not null;default:'ACTIVE'"`
}

func (NoteModel) TableName() string {
	return "notes"
}
