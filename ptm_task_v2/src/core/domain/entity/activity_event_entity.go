package entity

type ActivityEventEntity struct {
	BaseEntity

	UserID   int64 `json:"userId"`
	TenantID int64 `json:"tenantId"`

	EventType  string `json:"eventType"`
	EntityType string `json:"entityType"`
	EntityID   int64  `json:"entityId"`

	Title       string  `json:"title"`
	Description *string `json:"description,omitempty"`
	Metadata    *string `json:"metadata,omitempty"`

	NavigationURL    *string `json:"navigationUrl,omitempty"`
	NavigationParams *string `json:"navigationParams,omitempty"`
}
