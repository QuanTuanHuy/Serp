/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ActivityLogModel struct {
	BaseModel

	ProjectID  int64  `gorm:"not null;index:idx_actlog_project"`
	WorkItemID *int64 `gorm:"index:idx_actlog_workitem"`
	UserID     int64  `gorm:"not null"`

	Action   string  `gorm:"type:varchar(50);not null"`
	Field    *string `gorm:"type:varchar(100)"`
	OldValue *string `gorm:"type:text"`
	NewValue *string `gorm:"type:text"`
}

func (ActivityLogModel) TableName() string {
	return "activity_logs"
}
