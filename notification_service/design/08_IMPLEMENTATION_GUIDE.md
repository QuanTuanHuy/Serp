# Implementation Guide

**Module:** notification_service  
**Ngày tạo:** 2025-12-13

---

## 1. Implementation Phases

| Phase | Tasks | Priority |
|-------|-------|----------|
| **Phase 1** | Core CRUD, WebSocket, Kafka consumer | High |
| **Phase 2** | Preferences, Templates, Email integration | Medium |
| **Phase 3** | Push notifications, Analytics | Low |

---

## 2. Phase 1: Core Implementation

### Step 1: Project Setup

```bash
# Create project structure
mkdir -p notification_service/src/{cmd/bootstrap,config,core/{domain/{constant,dto,entity,enum,mapper},port/{store,client},service,usecase},infrastructure/{client,store/{adapter,mapper,model},websocket},kernel/{properties,utils},ui/{controller,kafka,middleware,router}}

# Initialize go module
cd notification_service
go mod init github.com/serp/notification-service
```

### Step 2: Base Files

**main.go:**
```go
/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package main

import (
    "github.com/serp/notification-service/src/cmd/bootstrap"
    "go.uber.org/fx"
)

func main() {
    fx.New(bootstrap.All()).Run()
}
```

**BaseEntity:**
```go
// core/domain/entity/base_entity.go
package entity

type BaseEntity struct {
    ID           int64  `json:"id"`
    CreatedAt    int64  `json:"createdAt"`
    UpdatedAt    int64  `json:"updatedAt"`
    ActiveStatus string `json:"activeStatus"`
}
```

**BaseModel:**
```go
// infrastructure/store/model/base_model.go
package model

import "time"

type BaseModel struct {
    ID        int64     `gorm:"primaryKey;autoIncrement"`
    CreatedAt time.Time `gorm:"not null;autoCreateTime"`
    UpdatedAt time.Time `gorm:"not null;autoUpdateTime"`
}
```

### Step 3: Notification Service

```go
// core/service/notification_service.go
package service

type NotificationService struct {
    notificationPort store.INotificationPort
    wsHub           client.IWebSocketHubPort
    redis           client.IRedisPort
    logger          *zap.Logger
}

func NewNotificationService(
    notificationPort store.INotificationPort,
    wsHub client.IWebSocketHubPort,
    redis client.IRedisPort,
    logger *zap.Logger,
) *NotificationService {
    return &NotificationService{
        notificationPort: notificationPort,
        wsHub:           wsHub,
        redis:           redis,
        logger:          logger,
    }
}

func (s *NotificationService) Create(
    ctx context.Context,
    tx *gorm.DB,
    entity *entity.NotificationEntity,
) (*entity.NotificationEntity, error) {
    // Validate
    if entity.Title == "" {
        return nil, errors.New("title is required")
    }
    
    // Set defaults
    if entity.Priority == "" {
        entity.Priority = string(enum.PriorityMedium)
    }
    if entity.Type == "" {
        entity.Type = string(enum.TypeInfo)
    }
    
    // Create in DB
    created, err := s.notificationPort.Create(ctx, tx, entity)
    if err != nil {
        return nil, err
    }
    
    return created, nil
}

func (s *NotificationService) DeliverRealtime(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Convert to WS message
    msg := &WSMessage{
        Type:      "NEW_NOTIFICATION",
        Payload:   mapper.ToNotificationResponse(notification),
        Timestamp: time.Now().UnixMilli(),
        MessageID: uuid.NewString(),
    }
    
    data, err := json.Marshal(msg)
    if err != nil {
        return err
    }
    
    // Send via WebSocket
    return s.wsHub.SendToUser(notification.UserID, data)
}
```

### Step 4: Notification UseCase

```go
// core/usecase/notification_usecase.go
package usecase

type NotificationUseCase struct {
    txService           *service.TransactionService
    notificationService *service.NotificationService
    deliveryService     *service.DeliveryService
    preferenceService   *service.PreferenceService
    kafkaProducer       client.IKafkaProducerPort
    logger              *zap.Logger
}

func (u *NotificationUseCase) CreateNotification(
    ctx context.Context,
    req *request.CreateNotificationRequest,
) (*response.NotificationResponse, error) {
    // Check user preferences
    prefs, _ := u.preferenceService.GetByUserID(ctx, req.UserID)
    if prefs != nil && !u.shouldDeliver(prefs, req.Category, req.Channels) {
        return nil, nil // User opted out
    }
    
    // Create entity
    entity := mapper.ToNotificationEntity(req)
    
    // Execute in transaction
    result, err := u.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
        created, err := u.notificationService.Create(ctx, tx, entity)
        if err != nil {
            return nil, err
        }
        
        // Update unread count in Redis
        u.notificationService.IncrementUnreadCount(ctx, created.UserID)
        
        return created, nil
    })
    
    if err != nil {
        return nil, err
    }
    
    notification := result.(*entity.NotificationEntity)
    
    // Async delivery
    go u.deliveryService.Deliver(ctx, notification)
    
    return mapper.ToNotificationResponse(notification), nil
}

func (u *NotificationUseCase) CreateFromEvent(
    ctx context.Context,
    evt *event.NotificationRequestEvent,
) error {
    // Handle bulk notifications
    if len(evt.UserIDs) > 0 {
        for _, userID := range evt.UserIDs {
            req := u.eventToRequest(evt, userID)
            u.CreateNotification(ctx, req)
        }
        return nil
    }
    
    // Single notification
    req := u.eventToRequest(evt, evt.UserID)
    _, err := u.CreateNotification(ctx, req)
    return err
}
```

### Step 5: Controller

```go
// ui/controller/notification_controller.go
package controller

type NotificationController struct {
    notificationUseCase *usecase.NotificationUseCase
    logger              *zap.Logger
}

func (c *NotificationController) GetNotifications(ctx *gin.Context) {
    userID := utils.GetUserIDFromContext(ctx)
    
    var req request.GetNotificationsRequest
    if err := ctx.ShouldBindQuery(&req); err != nil {
        utils.ResponseError(ctx, http.StatusBadRequest, err.Error())
        return
    }
    
    // Set defaults
    if req.Page == 0 {
        req.Page = 1
    }
    if req.PageSize == 0 {
        req.PageSize = 20
    }
    
    result, err := c.notificationUseCase.GetNotifications(ctx, userID, &req)
    if err != nil {
        utils.ResponseError(ctx, http.StatusInternalServerError, err.Error())
        return
    }
    
    utils.ResponseSuccess(ctx, result)
}

func (c *NotificationController) MarkAsRead(ctx *gin.Context) {
    userID := utils.GetUserIDFromContext(ctx)
    idStr := ctx.Param("id")
    id, _ := strconv.ParseInt(idStr, 10, 64)
    
    err := c.notificationUseCase.MarkAsRead(ctx, []int64{id}, userID)
    if err != nil {
        utils.ResponseError(ctx, http.StatusInternalServerError, err.Error())
        return
    }
    
    utils.ResponseSuccess(ctx, gin.H{"updatedCount": 1})
}

func (c *NotificationController) GetUnreadCount(ctx *gin.Context) {
    userID := utils.GetUserIDFromContext(ctx)
    
    result, err := c.notificationUseCase.GetUnreadCount(ctx, userID)
    if err != nil {
        utils.ResponseError(ctx, http.StatusInternalServerError, err.Error())
        return
    }
    
    utils.ResponseSuccess(ctx, result)
}
```

### Step 6: Router

```go
// ui/router/router.go
package router

func RegisterRoutes(
    engine *gin.Engine,
    jwtMiddleware *middleware.JWTMiddleware,
    serviceAuthMiddleware *middleware.ServiceAuthMiddleware,
    notificationCtrl *controller.NotificationController,
    preferenceCtrl *controller.PreferenceController,
    wsCtrl *controller.WebSocketController,
) {
    api := engine.Group("/notification/api/v1")
    
    // WebSocket (separate auth)
    engine.GET("/notification/ws", wsCtrl.HandleWebSocket)
    
    // Protected routes
    protected := api.Group("")
    protected.Use(jwtMiddleware.Handle())
    {
        // Notifications
        notifications := protected.Group("/notifications")
        {
            notifications.GET("", notificationCtrl.GetNotifications)
            notifications.GET("/:id", notificationCtrl.GetNotificationByID)
            notifications.PUT("/:id/read", notificationCtrl.MarkAsRead)
            notifications.PUT("/read", notificationCtrl.MarkMultipleAsRead)
            notifications.PUT("/read-all", notificationCtrl.MarkAllAsRead)
            notifications.DELETE("/:id", notificationCtrl.DeleteNotification)
            notifications.DELETE("", notificationCtrl.DeleteMultiple)
            notifications.GET("/unread-count", notificationCtrl.GetUnreadCount)
        }
        
        // Preferences
        preferences := protected.Group("/preferences")
        {
            preferences.GET("", preferenceCtrl.GetPreferences)
            preferences.PUT("", preferenceCtrl.UpdatePreferences)
        }
    }
    
    // Internal routes (service-to-service)
    internal := api.Group("/internal")
    internal.Use(serviceAuthMiddleware.Handle())
    {
        internal.POST("/notifications", notificationCtrl.CreateNotification)
        internal.POST("/notifications/bulk", notificationCtrl.CreateBulkNotifications)
    }
}
```

---

## 3. Testing Checklist

| Test | Description |
|------|-------------|
| ✅ Create notification | POST /notifications (internal) |
| ✅ List notifications | GET /notifications với pagination |
| ✅ Mark as read | PUT /notifications/:id/read |
| ✅ Unread count | GET /notifications/unread-count |
| ✅ WebSocket connect | Connect với valid JWT |
| ✅ Real-time delivery | Nhận notification qua WS |
| ✅ Kafka consume | Process NOTIFICATION_REQUEST_TOPIC |

---

## 4. API Gateway Configuration

Thêm vào `api_gateway/src/config/routes.go`:

```go
{
    Path:        "/notification",
    ServiceURL:  "http://notification-service:8088",
    Methods:     []string{"GET", "POST", "PUT", "DELETE"},
    RequireAuth: true,
},
{
    Path:        "/notification/ws",
    ServiceURL:  "ws://notification-service:8088",
    IsWebSocket: true,
    RequireAuth: true,
},
```

---

## 5. Docker Configuration

**Dockerfile:**
```dockerfile
FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o notification-service src/main.go

FROM alpine:3.18
WORKDIR /app
COPY --from=builder /app/notification-service .
EXPOSE 8088
CMD ["./notification-service"]
```

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  notification-service:
    build: .
    ports:
      - "8088:8088"
    environment:
      - APP_PORT=8088
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - postgres
      - redis
      - kafka
```

---

## 6. Next Steps

1. **Phase 2:**
   - Implement notification templates
   - Add email delivery via mailservice
   - User preference management

2. **Phase 3:**
   - Firebase push notifications
   - Notification analytics
   - Scheduled notifications
