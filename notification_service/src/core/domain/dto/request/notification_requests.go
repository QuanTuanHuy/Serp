/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateNotificationRequest struct {
	UserID   int64  `json:"userId" binding:"required"`
	TenantID int64  `json:"tenantId" binding:"required"`
	Title    string `json:"title" binding:"required,max=200"`
	Message  string `json:"message" binding:"required,max=1000"`
	Type     string `json:"type" binding:"required,oneof=INFO SUCCESS WARNING ERROR"`

	Category string `json:"category" binding:"required"`
	Priority string `json:"priority" binding:"omitempty,oneof=LOW MEDIUM HIGH URGENT"`

	SourceService string  `json:"sourceService" binding:"required"`
	SourceEventID *string `json:"sourceEventId,omitempty"`

	ActionURL  *string `json:"actionUrl,omitempty"`
	ActionType *string `json:"actionType,omitempty"`

	EntityType *string `json:"entityType,omitempty"`
	EntityID   *int64  `json:"entityId,omitempty"`

	DeliveryChannels []string       `json:"deliveryChannels"`
	ExpiresAt        *int64         `json:"expiresAt,omitempty"`
	Metadata         map[string]any `json:"metadata,omitempty"`
}

type CreateBulkNotificationRequest struct {
	UserIDs  []int64 `json:"userIds" binding:"required,min=1"`
	TenantID int64   `json:"tenantId" binding:"required"`
	Title    string  `json:"title" binding:"required,max=200"`
	Message  string  `json:"message" binding:"required,max=1000"`
	Type     string  `json:"type" binding:"required"`
	Category string  `json:"category" binding:"required"`
	Priority string  `json:"priority"`

	SourceService string         `json:"sourceService" binding:"required"`
	ActionURL     *string        `json:"actionUrl,omitempty"`
	Metadata      map[string]any `json:"metadata,omitempty"`
}

type UpdateNotificationRequest struct {
	IsRead     *bool `json:"isRead,omitempty"`
	IsArchived *bool `json:"isArchived,omitempty"`
}

type MarkReadRequest struct {
	NotificationIDs []int64 `json:"notificationIds" binding:"required,min=1"`
}

type GetNotificationParams struct {
	BaseParams

	UserID   *int64  `form:"userId,omitempty"`
	TenantID *int64  `form:"tenantId,omitempty"`
	Type     *string `form:"type,omitempty"`
	Category *string `form:"category,omitempty"`
	Priority *string `form:"priority,omitempty"`
	IsRead   *bool   `form:"isRead,omitempty"`
}
