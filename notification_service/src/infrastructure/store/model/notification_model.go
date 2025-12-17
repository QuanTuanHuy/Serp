/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import (
	"time"

	"gorm.io/datatypes"
)

type NotificationModel struct {
	BaseModel

	UserID   int64 `gorm:"not null;index:idx_notification_user"`
	TenantID int64 `gorm:"not null;index:idx_notification_tenant"`

	Title   string `gorm:"type:varchar(200);not null"`
	Message string `gorm:"type:text;not null"`
	Type    string `gorm:"type:varchar(20);not null"`

	Category string `gorm:"type:varchar(50);not null;index:idx_notification_category"`
	Priority string `gorm:"type:varchar(20);not null;default:'MEDIUM'"`

	SourceService string  `gorm:"type:varchar(100);not null"`
	SourceEventID *string `gorm:"type:varchar(100)"`

	ActionURL  *string `gorm:"type:text"`
	ActionType *string `gorm:"type:varchar(50)"`

	EntityType *string `gorm:"type:varchar(50)"`
	EntityID   *int64  `gorm:""`

	IsRead     bool       `gorm:"not null;default:false;index:idx_notification_is_read"`
	ReadAt     *time.Time `gorm:""`
	IsArchived bool       `gorm:"not null;default:false"`
	Status     string     `gorm:"type:varchar(30);not null;default:'PENDING';index:idx_notification_status"`

	DeliveryChannels datatypes.JSON `gorm:"type:jsonb"`
	DeliveryAt       *time.Time

	ExpireAt *time.Time

	Metadata datatypes.JSONMap `gorm:"type:jsonb"`
}

func (NotificationModel) TableName() string {
	return "notifications"
}
