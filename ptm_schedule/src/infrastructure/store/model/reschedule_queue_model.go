/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type RescheduleQueueModel struct {
	BaseModel
	UserID         int64 `gorm:"not null;index"`
	SchedulePlanID int64 `gorm:"not null;index:idx_queue_ready;uniqueIndex:idx_queue_upsert"`

	TriggerType string `gorm:"size:50;not null;uniqueIndex:idx_queue_upsert"`
	EntityID    int64  `gorm:"index:idx_queue_coalesce;uniqueIndex:idx_queue_upsert"`
	EntityType  string `gorm:"size:20"`

	ChangePayload string `gorm:"type:jsonb"`

	Status   string `gorm:"size:20;default:PENDING;index:idx_queue_ready"`
	Priority int    `gorm:"default:5"`

	DebounceUntil  time.Time `gorm:"not null;index:idx_queue_ready"`
	FirstCreatedAt time.Time `gorm:"not null"`

	ProcessedAt          *time.Time
	ProcessingDurationMs *int
	ErrorMessage         *string `gorm:"type:text"`
	RetryCount           int     `gorm:"default:0"`
}

func (RescheduleQueueModel) TableName() string {
	return "reschedule_queues"
}
