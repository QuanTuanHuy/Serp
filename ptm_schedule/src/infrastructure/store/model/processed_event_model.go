/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type ProcessedEventModel struct {
	ID          int64     `gorm:"primaryKey;autoIncrement"`
	EventID     string    `gorm:"type:varchar(50);uniqueIndex;not null"`
	EventType   string    `gorm:"type:varchar(100);not null"`
	Topic       string    `gorm:"type:varchar(100);not null"`
	ProcessedAt time.Time `gorm:"autoCreateTime;index"`
}

func (ProcessedEventModel) TableName() string {
	return "processed_events"
}
