/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

type ActivityEventModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;index:idx_activity_event_user"`
	TenantID int64 `gorm:"not null"`

	EventType  string `gorm:"type:varchar(100);not null;index:idx_activity_event_type"`
	EntityType string `gorm:"type:varchar(100);not null;index:idx_activity_event_entity_type"`
	EntityID   int64  `gorm:"not null;index:idx_activity_event_entity_id"`

	Title       string  `gorm:"type:varchar(255);not null"`
	Description *string `gorm:"type:text"`
	Metadata    *string `gorm:"type:text"`

	NavigationURL    *string `gorm:"type:text"`
	NavigationParams *string `gorm:"type:text"`
}

func (ActivityEventModel) TableName() string {
	return "activity_events"
}
