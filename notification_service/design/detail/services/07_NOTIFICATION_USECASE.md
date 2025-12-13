# NotificationUseCase - Chi Tiết Thiết Kế

**File:** `core/usecase/notification_usecase.go`  
**Responsibility:** Orchestration, transaction management, cross-service coordination

---

## 1. Interface Definition

```go
package usecase

import (
    "context"
    
    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/dto/response"
)

type INotificationUseCase interface {
    // User-facing operations
    CreateNotification(ctx context.Context, userID int64, req *request.CreateNotificationRequest) (*response.NotificationResponse, error)
    GetNotification(ctx context.Context, userID int64, id int64) (*response.NotificationResponse, error)
    GetNotifications(ctx context.Context, userID int64, params *request.GetNotificationsParams) (*response.NotificationListResponse, error)
    MarkAsRead(ctx context.Context, userID int64, id int64) error
    MarkAllAsRead(ctx context.Context, userID int64) error
    DeleteNotification(ctx context.Context, userID int64, id int64) error
    GetUnreadCount(ctx context.Context, userID int64) (*response.UnreadCountResponse, error)
    
    // System operations (from Kafka events)
    CreateFromEvent(ctx context.Context, event *request.NotificationEvent) error
    CreateBulkFromEvent(ctx context.Context, event *request.BulkNotificationEvent) error
    
    // Broadcast
    BroadcastSystemNotification(ctx context.Context, req *request.BroadcastRequest) error
}
```

---

## 2. Struct Definition

```go
package usecase

import (
    "context"
    "fmt"

    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/dto/response"
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/mapper"
    "notification_service/src/core/service"
    "gorm.io/gorm"
)

type NotificationUseCase struct {
    txService           service.ITransactionService
    notificationService service.INotificationService
    preferenceService   service.IPreferenceService
    templateService     service.ITemplateService
    deliveryService     service.IDeliveryService
}

func NewNotificationUseCase(
    txService service.ITransactionService,
    notificationService service.INotificationService,
    preferenceService service.IPreferenceService,
    templateService service.ITemplateService,
    deliveryService service.IDeliveryService,
) INotificationUseCase {
    return &NotificationUseCase{
        txService:           txService,
        notificationService: notificationService,
        preferenceService:   preferenceService,
        templateService:     templateService,
        deliveryService:     deliveryService,
    }
}
```

---

## 3. Create Notification (Full Flow)

```go
func (u *NotificationUseCase) CreateNotification(
    ctx context.Context,
    userID int64,
    req *request.CreateNotificationRequest,
) (*response.NotificationResponse, error) {
    var notification *entity.NotificationEntity
    
    // Execute within transaction
    err := u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        var err error
        
        // 1. Process template if provided
        if req.TemplateCode != "" {
            rendered, err := u.templateService.RenderNotification(ctx, req.TemplateCode, req.TemplateData)
            if err != nil {
                return fmt.Errorf("template rendering failed: %w", err)
            }
            req.Title = rendered.Title
            req.Body = rendered.Body
            if req.Type == "" {
                req.Type = rendered.Type
            }
            if req.Priority == "" {
                req.Priority = rendered.Priority
            }
        }
        
        // 2. Create notification
        notification, err = u.notificationService.Create(ctx, tx, userID, req)
        if err != nil {
            return err
        }
        
        return nil
    })
    
    if err != nil {
        return nil, err
    }
    
    // 3. Deliver (async, after transaction commits)
    go func() {
        if deliveryErr := u.deliveryService.Deliver(context.Background(), notification); deliveryErr != nil {
            // Log error but don't fail - notification is saved
            fmt.Printf("delivery failed for notification %d: %v\n", notification.ID, deliveryErr)
        }
    }()
    
    return mapper.NotificationEntityToResponse(notification), nil
}
```

---

## 4. Get Notifications with Pagination

```go
func (u *NotificationUseCase) GetNotifications(
    ctx context.Context,
    userID int64,
    params *request.GetNotificationsParams,
) (*response.NotificationListResponse, error) {
    // Set defaults
    if params.Page <= 0 {
        params.Page = 1
    }
    if params.PageSize <= 0 {
        params.PageSize = 20
    }
    if params.PageSize > 100 {
        params.PageSize = 100
    }
    
    // Get notifications
    notifications, total, err := u.notificationService.GetList(ctx, userID, params)
    if err != nil {
        return nil, err
    }
    
    // Get unread count
    unreadCount, _ := u.notificationService.GetUnreadCount(ctx, userID)
    
    // Map to response
    items := make([]*response.NotificationResponse, len(notifications))
    for i, n := range notifications {
        items[i] = mapper.NotificationEntityToResponse(n)
    }
    
    return &response.NotificationListResponse{
        Items:       items,
        Total:       total,
        Page:        params.Page,
        PageSize:    params.PageSize,
        UnreadCount: unreadCount,
    }, nil
}
```

---

## 5. Mark as Read (with WebSocket update)

```go
func (u *NotificationUseCase) MarkAsRead(
    ctx context.Context,
    userID int64,
    id int64,
) error {
    err := u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        return u.notificationService.MarkAsRead(ctx, tx, id, userID)
    })
    
    if err != nil {
        return err
    }
    
    // Notify client to update unread count
    go u.sendUnreadCountUpdate(ctx, userID)
    
    return nil
}

func (u *NotificationUseCase) MarkAllAsRead(
    ctx context.Context,
    userID int64,
) error {
    err := u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        return u.notificationService.MarkAllAsRead(ctx, tx, userID)
    })
    
    if err != nil {
        return err
    }
    
    // Notify client
    go u.sendUnreadCountUpdate(ctx, userID)
    
    return nil
}

func (u *NotificationUseCase) sendUnreadCountUpdate(ctx context.Context, userID int64) {
    count, _ := u.notificationService.GetUnreadCount(ctx, userID)
    u.deliveryService.SendUnreadCountUpdate(ctx, userID, count)
}
```

---

## 6. Handle Kafka Event

```go
// Called by Kafka consumer when other services emit notification events
func (u *NotificationUseCase) CreateFromEvent(
    ctx context.Context,
    event *request.NotificationEvent,
) error {
    // 1. Check user preferences - should we send this notification?
    pref, _ := u.preferenceService.GetByUserID(ctx, event.UserID)
    if pref != nil && !u.shouldProcessEvent(pref, event) {
        return nil // Skip - user doesn't want this type
    }
    
    // 2. Create notification request from event
    req := &request.CreateNotificationRequest{
        TemplateCode: event.TemplateCode,
        TemplateData: event.Data,
        Type:         event.Type,
        Priority:     event.Priority,
        ReferenceID:  event.ReferenceID,
        ReferenceType: event.ReferenceType,
    }
    
    // 3. Create and deliver
    _, err := u.CreateNotification(ctx, event.UserID, req)
    return err
}

func (u *NotificationUseCase) shouldProcessEvent(
    pref *entity.NotificationPreferenceEntity,
    event *request.NotificationEvent,
) bool {
    switch event.Type {
    case enum.TypeTaskAssigned, enum.TypeTaskUpdated:
        return pref.TaskNotifications
    case enum.TypeComment:
        return pref.CommentNotifications
    case enum.TypeMention:
        return pref.MentionNotifications
    default:
        return true
    }
}
```

---

## 7. Bulk Create from Event

```go
func (u *NotificationUseCase) CreateBulkFromEvent(
    ctx context.Context,
    event *request.BulkNotificationEvent,
) error {
    var notifications []*entity.NotificationEntity
    
    // 1. Render template once
    rendered, err := u.templateService.RenderNotification(ctx, event.TemplateCode, event.Data)
    if err != nil {
        return err
    }
    
    // 2. Filter users by preferences
    for _, userID := range event.UserIDs {
        pref, _ := u.preferenceService.GetByUserID(ctx, userID)
        
        if pref != nil {
            enabled, _ := u.preferenceService.IsTypeEnabled(ctx, userID, rendered.Type)
            if !enabled {
                continue
            }
        }
        
        notifications = append(notifications, &entity.NotificationEntity{
            UserID:        userID,
            Title:         rendered.Title,
            Body:          rendered.Body,
            Type:          rendered.Type,
            Priority:      rendered.Priority,
            Category:      rendered.Category,
            Icon:          rendered.Icon,
            ActionURL:     rendered.ActionURL,
            ReferenceID:   event.ReferenceID,
            ReferenceType: event.ReferenceType,
        })
    }
    
    if len(notifications) == 0 {
        return nil
    }
    
    // 3. Bulk insert
    err = u.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
        return u.notificationService.CreateBulk(ctx, tx, notifications)
    })
    
    if err != nil {
        return err
    }
    
    // 4. Deliver all
    go u.deliveryService.DeliverBulk(context.Background(), notifications)
    
    return nil
}
```

---

## 8. UseCase Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                     Controller / Kafka Handler                    │
└─────────────────────────────┬────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      NotificationUseCase                          │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              TransactionService.ExecuteInTransaction         │ │
│  │                                                              │ │
│  │  1. TemplateService.RenderNotification()                     │ │
│  │  2. PreferenceService.GetByUserID()                          │ │
│  │  3. NotificationService.Create(tx)                           │ │
│  │                                                              │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼ (after commit)                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  4. DeliveryService.Deliver() [async goroutine]              │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

---

## 9. FX Registration

```go
// cmd/bootstrap/all.go
fx.Provide(usecase.NewNotificationUseCase),
```
