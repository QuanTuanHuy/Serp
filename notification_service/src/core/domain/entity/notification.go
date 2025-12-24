/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import (
	"time"

	"github.com/serp/notification-service/src/core/domain/enum"
)

type NotificationEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	Title   string                `json:"title"`
	Message string                `json:"message"`
	Type    enum.NotificationType `json:"type"`

	Category enum.NotificationCategory `json:"category"`
	Priority enum.NotificationPriority `json:"priority"`

	SourceService string  `json:"sourceService"`
	SourceEventID *string `json:"sourceEventId,omitempty"`

	ActionURL  *string `json:"actionUrl,omitempty"`
	ActionType *string `json:"actionType,omitempty"`

	EntityType *string `json:"entityType,omitempty"`
	EntityID   *int64  `json:"entityId,omitempty"`

	IsRead     bool                    `json:"isRead"`
	ReadAt     *int64                  `json:"readAt,omitempty"`
	IsArchived bool                    `json:"isArchived"`
	Status     enum.NotificationStatus `json:"status"`

	DeliveryChannels []enum.DeliveryChannel `json:"deliveryChannels"`
	DeliveryAt       *int64                 `json:"deliveryAt,omitempty"`

	ExpireAt *int64 `json:"expireAt,omitempty"`

	Metadata map[string]any `json:"metadata,omitempty"`
}

func (n *NotificationEntity) MarkAsRead() {
	now := time.Now().UnixMilli()

	n.IsRead = true
	n.ReadAt = &now
	n.Status = enum.NotificationRead
	n.UpdatedAt = now
}
