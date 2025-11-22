/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "gorm.io/datatypes"

type TaskReminderModel struct {
	BaseModel

	TaskID int64 `gorm:"not null;index:idx_task_reminder_task"`
	UserID int64 `gorm:"not null;index:idx_task_reminder_user"`

	ReminderType  string `gorm:"type:varchar(50);not null;index:idx_task_reminder_type"`
	TriggerTimeMs int64  `gorm:"not null;index:idx_task_reminder_trigger_time"`

	AdvanceNoticeMin *int
	IsRecurring      bool   `gorm:"not null;default:false"`
	SnoozeUntilMs    *int64 `gorm:"index:idx_task_reminder_snooze"`

	NotificationChannels datatypes.JSON `gorm:"type:jsonb"`
	MessageTemplate      *string        `gorm:"type:text"`

	Status string `gorm:"type:varchar(20);not null;default:'pending';index:idx_task_reminder_status"`
	SentAt *int64 `gorm:"index:idx_task_reminder_sent_at"`
}

func (TaskReminderModel) TableName() string {
	return "task_reminders"
}
