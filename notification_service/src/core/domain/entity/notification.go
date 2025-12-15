/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/notification-service/src/core/domain/enum"

type NotificationEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Titile  string                `json:"title"`
	Message string                `json:"message"`
	Type    enum.NotificationType `json:"type"`

	Category enum.NotificationCategory `json:"category"`
	Priority enum.NotificationPriority `json:"priority"`

	SourceService string `json:"sourceService"`
	SourceEventID *int64 `json:"sourceEventId,omitempty"`

	ActionURL  *string `json:"actionUrl,omitempty"`
	ActionType *string `json:"actionType,omitempty"`

	EntityType *string `json:"entityType,omitempty"`
	EntityID   *int64  `json:"entityId,omitempty"`

	IsRead     bool   `json:"isRead"`
	ReadAt     *int64 `json:"readAt,omitempty"`
	IsArchived bool   `json:"isArchived"`

	DeliveryChannels []enum.DeliveryChannel `json:"deliveryChannels"`
	DeliveryAt       *int64                 `json:"deliveryAt,omitempty"`

	ExpireAt *int64 `json:"expireAt,omitempty"`

	Metadata map[string]any `json:"metadata,omitempty"`
}
