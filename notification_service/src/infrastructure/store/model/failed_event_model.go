/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type FailedEventModel struct {
	BaseModel
	EventID      string `gorm:"type:varchar(50);uniqueIndex;not null"`
	EventType    string `gorm:"type:varchar(100);not null"`
	Topic        string `gorm:"type:varchar(100);not null"`
	MessageKey   string `gorm:"type:varchar(100)"`
	MessageValue string `gorm:"type:text"`
	RetryCount   int    `gorm:"default:0;not null"`
	LastError    string `gorm:"type:text"`
	Status       string `gorm:"type:varchar(20);default:'pending';index"`
}

func (FailedEventModel) TableName() string {
	return "failed_events"
}

const (
	FailedEventStatusPending   = "pending"
	FailedEventStatusSentToDLQ = "sent_to_dlq"
	FailedEventStatusResolved  = "resolved"
)
