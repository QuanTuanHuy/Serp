/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package store

import (
	"context"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"gorm.io/gorm"
)

type ITaskReminderPort interface {
	CreateReminder(ctx context.Context, tx *gorm.DB, reminder *entity.TaskReminderEntity) error
	CreateReminders(ctx context.Context, tx *gorm.DB, reminders []*entity.TaskReminderEntity) error

	GetReminderByID(ctx context.Context, id int64) (*entity.TaskReminderEntity, error)
	GetRemindersByTaskID(ctx context.Context, taskID int64) ([]*entity.TaskReminderEntity, error)
	GetRemindersByUserID(ctx context.Context, userID int64, filter *ReminderFilter) ([]*entity.TaskReminderEntity, error)
	GetPendingReminders(ctx context.Context, currentTimeMs int64, limit int) ([]*entity.TaskReminderEntity, error)
	GetDueReminders(ctx context.Context, currentTimeMs int64, limit int) ([]*entity.TaskReminderEntity, error)

	UpdateReminder(ctx context.Context, tx *gorm.DB, reminder *entity.TaskReminderEntity) error
	MarkAsSent(ctx context.Context, tx *gorm.DB, reminderID int64, sentAt int64) error
	MarkAsRead(ctx context.Context, tx *gorm.DB, reminderID int64, readAt int64) error
	SnoozeReminder(ctx context.Context, tx *gorm.DB, reminderID int64, snoozeUntil int64) error
	DismissReminder(ctx context.Context, tx *gorm.DB, reminderID int64) error

	SoftDeleteReminder(ctx context.Context, tx *gorm.DB, id int64) error
	DeleteRemindersByTaskID(ctx context.Context, tx *gorm.DB, taskID int64) error

	CountUnreadReminders(ctx context.Context, userID int64) (int64, error)
	GetReminderDeliveryStats(ctx context.Context, userID int64, fromTimeMs int64) (*ReminderDeliveryStats, error)
}

type ReminderFilter struct {
	ReminderTypes    []string
	ReminderStatuses []string

	ScheduledFrom *int64
	ScheduledTo   *int64

	IsRead      *bool
	IsSnoozed   *bool
	IsDismissed *bool

	Channels []string

	SortBy    string
	SortOrder string

	Limit  int
	Offset int
}

type ReminderDeliveryStats struct {
	TotalReminders     int64   `json:"total_reminders"`
	SentReminders      int64   `json:"sent_reminders"`
	ReadReminders      int64   `json:"read_reminders"`
	SnoozedReminders   int64   `json:"snoozed_reminders"`
	DismissedReminders int64   `json:"dismissed_reminders"`
	AvgReadTimeSeconds int64   `json:"avg_read_time_seconds"`
	DeliveryRate       float64 `json:"delivery_rate"`
	ReadRate           float64 `json:"read_rate"`
}

func NewReminderFilter() *ReminderFilter {
	return &ReminderFilter{
		ReminderTypes:    []string{},
		ReminderStatuses: []string{},
		Channels:         []string{},
		SortBy:           "scheduled_time_ms",
		SortOrder:        "ASC",
		Limit:            50,
		Offset:           0,
	}
}
