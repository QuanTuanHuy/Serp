package response

type NotificationResponse struct {
	ID       int64  `json:"id"`
	Title    string `json:"title"`
	Message  string `json:"message"`
	Type     string `json:"type"`
	Category string `json:"category"`
	Priority string `json:"priority"`

	SourceService string  `json:"sourceService"`
	ActionURL     *string `json:"actionUrl,omitempty"`
	ActionType    *string `json:"actionType,omitempty"`

	EntityType *string `json:"entityType,omitempty"`
	EntityID   *int64  `json:"entityId,omitempty"`

	IsRead     bool   `json:"isRead"`
	ReadAt     *int64 `json:"readAt,omitempty"`
	IsArchived bool   `json:"isArchived"`

	CreatedAt int64                  `json:"createdAt"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

type NotificationListResponse struct {
	Notifications []NotificationResponse `json:"notifications"`
	TotalCount    int64                  `json:"totalCount"`
	UnreadCount   int64                  `json:"unreadCount"`
	Page          int                    `json:"page"`
	PageSize      int                    `json:"pageSize"`
}

type UnreadCountResponse struct {
	TotalUnread int64            `json:"totalUnread"`
	ByCategory  map[string]int64 `json:"byCategory"`
	HasUrgent   bool             `json:"hasUrgent"`
}
