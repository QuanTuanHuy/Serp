# Domain Entities & DTOs

**Module:** notification_service  
**Ngày tạo:** 2025-12-13

---

## 1. Entities

### 1.1. NotificationEntity

```go
// core/domain/entity/notification_entity.go
package entity

type NotificationEntity struct {
    BaseEntity
    
    // Target user
    UserID   int64 `json:"userId"`
    TenantID int64 `json:"tenantId"`
    
    // Content
    Title   string  `json:"title"`
    Message string  `json:"message"`
    Type    string  `json:"type"`    // INFO, SUCCESS, WARNING, ERROR
    
    // Categorization
    Category  string  `json:"category"`  // TASK, CRM, SALES, SYSTEM
    Priority  string  `json:"priority"`  // LOW, MEDIUM, HIGH, URGENT
    
    // Source info
    SourceService string  `json:"sourceService"` // ptm_task, crm, sales
    SourceEventID *string `json:"sourceEventId,omitempty"`
    
    // Action link
    ActionURL  *string `json:"actionUrl,omitempty"`
    ActionType *string `json:"actionType,omitempty"` // VIEW, APPROVE, DISMISS
    
    // Entity reference (for deep linking)
    EntityType *string `json:"entityType,omitempty"` // task, lead, order
    EntityID   *int64  `json:"entityId,omitempty"`
    
    // Status
    IsRead     bool   `json:"isRead"`
    ReadAt     *int64 `json:"readAt,omitempty"`
    IsArchived bool   `json:"isArchived"`
    
    // Delivery
    DeliveryChannels []string `json:"deliveryChannels"` // ["in_app", "email", "push"]
    DeliveredAt      *int64   `json:"deliveredAt,omitempty"`
    
    // Expiration
    ExpiresAt *int64 `json:"expiresAt,omitempty"`
    
    // Metadata
    Metadata map[string]interface{} `json:"metadata,omitempty"`
}
```

### 1.2. NotificationPreferenceEntity

```go
// core/domain/entity/notification_preference_entity.go
package entity

type NotificationPreferenceEntity struct {
    BaseEntity
    
    UserID   int64 `json:"userId"`
    TenantID int64 `json:"tenantId"`
    
    // Channel preferences
    EnableInApp  bool `json:"enableInApp"`
    EnableEmail  bool `json:"enableEmail"`
    EnablePush   bool `json:"enablePush"`
    
    // Category preferences (JSON stored)
    CategorySettings map[string]CategoryPreference `json:"categorySettings"`
    
    // Quiet hours
    QuietHoursEnabled bool    `json:"quietHoursEnabled"`
    QuietHoursStart   *string `json:"quietHoursStart,omitempty"` // "22:00"
    QuietHoursEnd     *string `json:"quietHoursEnd,omitempty"`   // "08:00"
    Timezone          string  `json:"timezone"`
}

type CategoryPreference struct {
    Enabled  bool     `json:"enabled"`
    Channels []string `json:"channels"` // ["in_app", "email"]
}
```

### 1.3. NotificationTemplateEntity

```go
// core/domain/entity/notification_template_entity.go
package entity

type NotificationTemplateEntity struct {
    BaseEntity
    
    TenantID int64 `json:"tenantId"`
    
    // Template info
    Code        string  `json:"code"`        // TASK_ASSIGNED, DEAL_WON
    Name        string  `json:"name"`
    Description *string `json:"description,omitempty"`
    
    // Content templates
    TitleTemplate   string `json:"titleTemplate"`   // "Task {{.TaskName}} assigned"
    MessageTemplate string `json:"messageTemplate"` // "You have been assigned..."
    
    // Defaults
    Type      string   `json:"type"`
    Category  string   `json:"category"`
    Priority  string   `json:"priority"`
    Channels  []string `json:"channels"`
    
    // Variables schema (for validation)
    VariablesSchema *string `json:"variablesSchema,omitempty"` // JSON Schema
    
    IsActive     bool   `json:"isActive"`
    ActiveStatus string `json:"activeStatus"`
}
```

---

## 2. Enums

### 2.1. NotificationType

```go
// core/domain/enum/notification_type.go
package enum

type NotificationType string

const (
    TypeInfo    NotificationType = "INFO"
    TypeSuccess NotificationType = "SUCCESS"
    TypeWarning NotificationType = "WARNING"
    TypeError   NotificationType = "ERROR"
)
```

### 2.2. NotificationCategory

```go
// core/domain/enum/notification_category.go
package enum

type NotificationCategory string

const (
    CategoryTask   NotificationCategory = "TASK"
    CategoryCRM    NotificationCategory = "CRM"
    CategorySales  NotificationCategory = "SALES"
    CategorySystem NotificationCategory = "SYSTEM"
    CategoryEmail  NotificationCategory = "EMAIL"
)
```

### 2.3. NotificationPriority

```go
// core/domain/enum/notification_priority.go
package enum

type NotificationPriority string

const (
    PriorityLow    NotificationPriority = "LOW"
    PriorityMedium NotificationPriority = "MEDIUM"
    PriorityHigh   NotificationPriority = "HIGH"
    PriorityUrgent NotificationPriority = "URGENT"
)
```

### 2.4. DeliveryChannel

```go
// core/domain/enum/delivery_channel.go
package enum

type DeliveryChannel string

const (
    ChannelInApp DeliveryChannel = "IN_APP"
    ChannelEmail DeliveryChannel = "EMAIL"
    ChannelPush  DeliveryChannel = "PUSH"
    ChannelSMS   DeliveryChannel = "SMS"
)
```

---

## 3. DTOs

### 3.1. Request DTOs

```go
// core/domain/dto/request/create_notification_request.go
package request

type CreateNotificationRequest struct {
    UserID   int64  `json:"userId" binding:"required"`
    TenantID int64  `json:"tenantId" binding:"required"`
    Title    string `json:"title" binding:"required,max=200"`
    Message  string `json:"message" binding:"required,max=1000"`
    Type     string `json:"type" binding:"required,oneof=INFO SUCCESS WARNING ERROR"`
    
    Category  string `json:"category" binding:"required"`
    Priority  string `json:"priority" binding:"omitempty,oneof=LOW MEDIUM HIGH URGENT"`
    
    SourceService string  `json:"sourceService" binding:"required"`
    SourceEventID *string `json:"sourceEventId,omitempty"`
    
    ActionURL  *string `json:"actionUrl,omitempty"`
    ActionType *string `json:"actionType,omitempty"`
    
    EntityType *string `json:"entityType,omitempty"`
    EntityID   *int64  `json:"entityId,omitempty"`
    
    DeliveryChannels []string               `json:"deliveryChannels"`
    ExpiresAt        *int64                 `json:"expiresAt,omitempty"`
    Metadata         map[string]interface{} `json:"metadata,omitempty"`
}

// core/domain/dto/request/create_bulk_notification_request.go
type CreateBulkNotificationRequest struct {
    UserIDs  []int64 `json:"userIds" binding:"required,min=1"`
    TenantID int64   `json:"tenantId" binding:"required"`
    Title    string  `json:"title" binding:"required,max=200"`
    Message  string  `json:"message" binding:"required,max=1000"`
    Type     string  `json:"type" binding:"required"`
    Category string  `json:"category" binding:"required"`
    Priority string  `json:"priority"`
    
    SourceService string                 `json:"sourceService" binding:"required"`
    ActionURL     *string                `json:"actionUrl,omitempty"`
    Metadata      map[string]interface{} `json:"metadata,omitempty"`
}

// core/domain/dto/request/mark_read_request.go
type MarkReadRequest struct {
    NotificationIDs []int64 `json:"notificationIds" binding:"required,min=1"`
}

// core/domain/dto/request/get_notifications_request.go
type GetNotificationsRequest struct {
    Page     int    `form:"page" binding:"omitempty,min=1"`
    PageSize int    `form:"pageSize" binding:"omitempty,min=1,max=100"`
    IsRead   *bool  `form:"isRead"`
    Category string `form:"category"`
    Type     string `form:"type"`
}
```

### 3.2. Response DTOs

```go
// core/domain/dto/response/notification_response.go
package response

type NotificationResponse struct {
    ID        int64  `json:"id"`
    Title     string `json:"title"`
    Message   string `json:"message"`
    Type      string `json:"type"`
    Category  string `json:"category"`
    Priority  string `json:"priority"`
    
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

// core/domain/dto/response/notification_list_response.go
type NotificationListResponse struct {
    Notifications []NotificationResponse `json:"notifications"`
    TotalCount    int64                  `json:"totalCount"`
    UnreadCount   int64                  `json:"unreadCount"`
    Page          int                    `json:"page"`
    PageSize      int                    `json:"pageSize"`
}

// core/domain/dto/response/unread_count_response.go
type UnreadCountResponse struct {
    TotalUnread   int64            `json:"totalUnread"`
    ByCategory    map[string]int64 `json:"byCategory"`
    HasUrgent     bool             `json:"hasUrgent"`
}
```
