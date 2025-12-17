/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "time"

type ProcessedEventEntity struct {
	ID          int64     `json:"id"`
	EventID     string    `json:"eventId"`
	EventType   string    `json:"eventType"`
	Topic       string    `json:"topic"`
	ProcessedAt time.Time `json:"processedAt"`
}
