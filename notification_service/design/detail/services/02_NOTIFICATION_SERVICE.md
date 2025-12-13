# NotificationService - Chi Tiết Thiết Kế

**File:** `core/service/notification_service.go`  
**Responsibility:** Notification CRUD, business logic, validation

---

## 1. Interface Definition

```go
package service

import (
    "context"
    
    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/dto/response"
    "notification_service/src/core/domain/entity"
    "gorm.io/gorm"
)

type INotificationService interface {
    // CRUD Operations
    Create(ctx context.Context, tx *gorm.DB, userID int64, req *request.CreateNotificationRequest) (*entity.NotificationEntity, error)
    GetByID(ctx context.Context, id int64, userID int64) (*entity.NotificationEntity, error)
    GetList(ctx context.Context, userID int64, params *request.GetNotificationsParams) ([]*entity.NotificationEntity, int64, error)
    
    // Status Updates
    MarkAsRead(ctx context.Context, tx *gorm.DB, id int64, userID int64) error
    MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) error
    Delete(ctx context.Context, tx *gorm.DB, id int64, userID int64) error
    
    // Counts & Stats
    GetUnreadCount(ctx context.Context, userID int64) (int64, error)
    
    // Bulk Operations
    CreateBulk(ctx context.Context, tx *gorm.DB, notifications []*entity.NotificationEntity) error
    
    // Business Logic
    ValidateNotification(notification *entity.NotificationEntity) error
    EnrichNotification(ctx context.Context, notification *entity.NotificationEntity) (*entity.NotificationEntity, error)
}
```

---

## 2. Struct Definition

```go
package service

import (
    "context"
    "errors"
    "time"

    "notification_service/src/core/domain/dto/request"
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
    "notification_service/src/core/mapper"
    "notification_service/src/core/port/client"
    "notification_service/src/core/port/store"
    "gorm.io/gorm"
)

type NotificationService struct {
    notificationPort store.INotificationPort
    templatePort     store.ITemplatePort
    redisPort        client.IRedisPort
}

func NewNotificationService(
    notificationPort store.INotificationPort,
    templatePort store.ITemplatePort,
    redisPort client.IRedisPort,
) INotificationService {
    return &NotificationService{
        notificationPort: notificationPort,
        templatePort:     templatePort,
        redisPort:        redisPort,
    }
}
```

---

## 3. CRUD Operations

### 3.1. Create Notification

```go
func (s *NotificationService) Create(
    ctx context.Context,
    tx *gorm.DB,
    userID int64,
    req *request.CreateNotificationRequest,
) (*entity.NotificationEntity, error) {
    // 1. Map request to entity
    notification := mapper.NotificationRequestToEntity(req)
    notification.UserID = userID
    notification.Status = enum.NotificationStatusUnread
    
    // 2. Process template if templateCode provided
    if req.TemplateCode != "" {
        enriched, err := s.processTemplate(ctx, notification, req.TemplateCode, req.TemplateData)
        if err != nil {
            return nil, err
        }
        notification = enriched
    }
    
    // 3. Validate notification
    if err := s.ValidateNotification(notification); err != nil {
        return nil, err
    }
    
    // 4. Set timestamps
    now := time.Now().UnixMilli()
    notification.CreatedAt = &now
    notification.UpdatedAt = &now
    
    // 5. Persist to database
    created, err := s.notificationPort.Create(ctx, tx, notification)
    if err != nil {
        return nil, err
    }
    
    // 6. Invalidate cache
    s.invalidateUserCache(ctx, userID)
    
    return created, nil
}
```

### 3.2. Get By ID with Authorization

```go
func (s *NotificationService) GetByID(
    ctx context.Context,
    id int64,
    userID int64,
) (*entity.NotificationEntity, error) {
    notification, err := s.notificationPort.GetByID(ctx, id)
    if err != nil {
        return nil, err
    }
    
    if notification == nil {
        return nil, errors.New("notification not found")
    }
    
    // Authorization: User can only access their own notifications
    if notification.UserID != userID {
        return nil, errors.New("access denied")
    }
    
    return notification, nil
}
```

### 3.3. Get List with Pagination & Filters

```go
func (s *NotificationService) GetList(
    ctx context.Context,
    userID int64,
    params *request.GetNotificationsParams,
) ([]*entity.NotificationEntity, int64, error) {
    // Build filter criteria
    filter := &store.NotificationFilter{
        UserID:   userID,
        Status:   params.Status,
        Type:     params.Type,
        Category: params.Category,
        Page:     params.Page,
        PageSize: params.PageSize,
    }
    
    // Try cache first for unread count
    if params.Status == enum.NotificationStatusUnread {
        cached, found := s.getCachedNotifications(ctx, userID, params)
        if found {
            return cached.Notifications, cached.Total, nil
        }
    }
    
    // Query database
    notifications, total, err := s.notificationPort.GetList(ctx, filter)
    if err != nil {
        return nil, 0, err
    }
    
    return notifications, total, nil
}
```

---

## 4. Status Update Operations

### 4.1. Mark as Read

```go
func (s *NotificationService) MarkAsRead(
    ctx context.Context,
    tx *gorm.DB,
    id int64,
    userID int64,
) error {
    // 1. Verify ownership
    notification, err := s.GetByID(ctx, id, userID)
    if err != nil {
        return err
    }
    
    // 2. Skip if already read
    if notification.Status == enum.NotificationStatusRead {
        return nil
    }
    
    // 3. Update status
    now := time.Now().UnixMilli()
    updates := &entity.NotificationEntity{
        Status:   enum.NotificationStatusRead,
        ReadAt:   &now,
        UpdatedAt: &now,
    }
    
    if err := s.notificationPort.Update(ctx, tx, id, updates); err != nil {
        return err
    }
    
    // 4. Decrement unread count in cache
    s.decrementUnreadCount(ctx, userID)
    
    return nil
}
```

### 4.2. Mark All as Read

```go
func (s *NotificationService) MarkAllAsRead(
    ctx context.Context,
    tx *gorm.DB,
    userID int64,
) error {
    now := time.Now().UnixMilli()
    
    // Batch update all unread notifications
    if err := s.notificationPort.UpdateAllUnread(ctx, tx, userID, &entity.NotificationEntity{
        Status:    enum.NotificationStatusRead,
        ReadAt:    &now,
        UpdatedAt: &now,
    }); err != nil {
        return err
    }
    
    // Reset unread count cache
    s.resetUnreadCount(ctx, userID)
    
    return nil
}
```

---

## 5. Count & Statistics

### 5.1. Get Unread Count (with Cache)

```go
const (
    UnreadCountCacheKey = "notification:unread:%d"
    UnreadCountCacheTTL = 5 * time.Minute
)

func (s *NotificationService) GetUnreadCount(ctx context.Context, userID int64) (int64, error) {
    cacheKey := fmt.Sprintf(UnreadCountCacheKey, userID)
    
    // Try cache first
    cached, err := s.redisPort.Get(ctx, cacheKey)
    if err == nil && cached != "" {
        count, _ := strconv.ParseInt(cached, 10, 64)
        return count, nil
    }
    
    // Query database
    count, err := s.notificationPort.CountUnread(ctx, userID)
    if err != nil {
        return 0, err
    }
    
    // Cache result
    s.redisPort.Set(ctx, cacheKey, strconv.FormatInt(count, 10), UnreadCountCacheTTL)
    
    return count, nil
}

// Helper: Decrement count in cache
func (s *NotificationService) decrementUnreadCount(ctx context.Context, userID int64) {
    cacheKey := fmt.Sprintf(UnreadCountCacheKey, userID)
    s.redisPort.Decr(ctx, cacheKey)
}

// Helper: Increment count in cache
func (s *NotificationService) incrementUnreadCount(ctx context.Context, userID int64) {
    cacheKey := fmt.Sprintf(UnreadCountCacheKey, userID)
    s.redisPort.Incr(ctx, cacheKey)
}

// Helper: Reset cache (after mark all as read)
func (s *NotificationService) resetUnreadCount(ctx context.Context, userID int64) {
    cacheKey := fmt.Sprintf(UnreadCountCacheKey, userID)
    s.redisPort.Set(ctx, cacheKey, "0", UnreadCountCacheTTL)
}
```

---

## 6. Validation Logic

```go
var (
    ErrEmptyTitle       = errors.New("notification title cannot be empty")
    ErrTitleTooLong     = errors.New("notification title exceeds 255 characters")
    ErrInvalidType      = errors.New("invalid notification type")
    ErrInvalidPriority  = errors.New("invalid notification priority")
)

func (s *NotificationService) ValidateNotification(notification *entity.NotificationEntity) error {
    // Title validation
    if notification.Title == "" {
        return ErrEmptyTitle
    }
    if len(notification.Title) > 255 {
        return ErrTitleTooLong
    }
    
    // Type validation
    if !enum.IsValidNotificationType(notification.Type) {
        return ErrInvalidType
    }
    
    // Priority validation
    if !enum.IsValidPriority(notification.Priority) {
        return ErrInvalidPriority
    }
    
    return nil
}
```

---

## 7. Template Processing

```go
func (s *NotificationService) processTemplate(
    ctx context.Context,
    notification *entity.NotificationEntity,
    templateCode string,
    data map[string]interface{},
) (*entity.NotificationEntity, error) {
    // 1. Get template
    template, err := s.templatePort.GetByCode(ctx, templateCode)
    if err != nil {
        return nil, fmt.Errorf("template not found: %s", templateCode)
    }
    
    // 2. Process title template
    title, err := s.renderTemplate(template.TitleTemplate, data)
    if err != nil {
        return nil, fmt.Errorf("failed to render title: %w", err)
    }
    notification.Title = title
    
    // 3. Process body template
    body, err := s.renderTemplate(template.BodyTemplate, data)
    if err != nil {
        return nil, fmt.Errorf("failed to render body: %w", err)
    }
    notification.Body = body
    
    // 4. Apply template defaults
    if notification.Type == "" {
        notification.Type = template.DefaultType
    }
    if notification.Priority == "" {
        notification.Priority = template.DefaultPriority
    }
    if notification.Category == "" {
        notification.Category = template.Category
    }
    
    return notification, nil
}

func (s *NotificationService) renderTemplate(tmpl string, data map[string]interface{}) (string, error) {
    t, err := template.New("notification").Parse(tmpl)
    if err != nil {
        return "", err
    }
    
    var buf bytes.Buffer
    if err := t.Execute(&buf, data); err != nil {
        return "", err
    }
    
    return buf.String(), nil
}
```

---

## 8. Bulk Operations

```go
func (s *NotificationService) CreateBulk(
    ctx context.Context,
    tx *gorm.DB,
    notifications []*entity.NotificationEntity,
) error {
    if len(notifications) == 0 {
        return nil
    }
    
    // Validate all notifications
    for _, n := range notifications {
        if err := s.ValidateNotification(n); err != nil {
            return fmt.Errorf("validation failed for notification to user %d: %w", n.UserID, err)
        }
    }
    
    // Set timestamps
    now := time.Now().UnixMilli()
    for _, n := range notifications {
        n.CreatedAt = &now
        n.UpdatedAt = &now
        n.Status = enum.NotificationStatusUnread
    }
    
    // Batch insert
    if err := s.notificationPort.CreateBatch(ctx, tx, notifications); err != nil {
        return err
    }
    
    // Invalidate cache for all affected users
    userIDs := s.extractUniqueUserIDs(notifications)
    for _, userID := range userIDs {
        s.invalidateUserCache(ctx, userID)
    }
    
    return nil
}

func (s *NotificationService) extractUniqueUserIDs(notifications []*entity.NotificationEntity) []int64 {
    seen := make(map[int64]bool)
    var userIDs []int64
    
    for _, n := range notifications {
        if !seen[n.UserID] {
            seen[n.UserID] = true
            userIDs = append(userIDs, n.UserID)
        }
    }
    
    return userIDs
}
```

---

## 9. Cache Management

```go
func (s *NotificationService) invalidateUserCache(ctx context.Context, userID int64) {
    // Invalidate unread count
    unreadKey := fmt.Sprintf(UnreadCountCacheKey, userID)
    s.redisPort.Del(ctx, unreadKey)
    
    // Invalidate notification list cache (if implemented)
    listKey := fmt.Sprintf("notification:list:%d:*", userID)
    s.redisPort.DelByPattern(ctx, listKey)
}
```

---

## 10. FX Provider Registration

```go
// cmd/bootstrap/all.go
fx.Provide(service.NewNotificationService),
```
