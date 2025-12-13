# DeliveryService - Chi Tiết Thiết Kế

**File:** `core/service/delivery_service.go`  
**Responsibility:** Multi-channel notification delivery

---

## 1. Interface Definition

```go
package service

import (
    "context"
    
    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
)

type IDeliveryService interface {
    // Main delivery
    Deliver(ctx context.Context, notification *entity.NotificationEntity) error
    DeliverToChannel(ctx context.Context, notification *entity.NotificationEntity, channel enum.NotificationChannel) error
    
    // Channel-specific
    DeliverInApp(ctx context.Context, notification *entity.NotificationEntity) error
    DeliverEmail(ctx context.Context, notification *entity.NotificationEntity) error
    DeliverPush(ctx context.Context, notification *entity.NotificationEntity) error
    
    // Batch delivery
    DeliverBulk(ctx context.Context, notifications []*entity.NotificationEntity) error
    
    // Retry failed deliveries
    RetryFailed(ctx context.Context, notificationID int64) error
}
```

---

## 2. Struct Definition

```go
package service

import (
    "context"
    "fmt"
    "sync"

    "notification_service/src/core/domain/entity"
    "notification_service/src/core/domain/enum"
    "notification_service/src/core/port/client"
    "notification_service/src/infrastructure/websocket"
)

type DeliveryService struct {
    preferenceService IPreferenceService
    hub               *websocket.Hub
    kafkaProducer     client.IKafkaProducerPort
    redisPort         client.IRedisPort
}

func NewDeliveryService(
    preferenceService IPreferenceService,
    hub *websocket.Hub,
    kafkaProducer client.IKafkaProducerPort,
    redisPort client.IRedisPort,
) IDeliveryService {
    return &DeliveryService{
        preferenceService: preferenceService,
        hub:               hub,
        kafkaProducer:     kafkaProducer,
        redisPort:         redisPort,
    }
}
```

---

## 3. Main Delivery Logic

```go
func (s *DeliveryService) Deliver(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Get enabled channels for user
    channels, err := s.preferenceService.GetEnabledChannels(ctx, notification.UserID)
    if err != nil {
        return fmt.Errorf("failed to get user channels: %w", err)
    }
    
    var deliveryErrors []error
    
    for _, channel := range channels {
        // Check if should deliver (preferences, quiet hours, etc.)
        shouldDeliver, _ := s.preferenceService.ShouldDeliver(
            ctx, notification.UserID, notification, channel,
        )
        
        if !shouldDeliver {
            continue
        }
        
        if err := s.DeliverToChannel(ctx, notification, channel); err != nil {
            deliveryErrors = append(deliveryErrors, 
                fmt.Errorf("channel %s: %w", channel, err))
        }
    }
    
    if len(deliveryErrors) > 0 {
        return fmt.Errorf("partial delivery failure: %v", deliveryErrors)
    }
    
    return nil
}

func (s *DeliveryService) DeliverToChannel(
    ctx context.Context,
    notification *entity.NotificationEntity,
    channel enum.NotificationChannel,
) error {
    switch channel {
    case enum.ChannelInApp:
        return s.DeliverInApp(ctx, notification)
    case enum.ChannelEmail:
        return s.DeliverEmail(ctx, notification)
    case enum.ChannelPush:
        return s.DeliverPush(ctx, notification)
    default:
        return fmt.Errorf("unknown channel: %s", channel)
    }
}
```

---

## 4. In-App Delivery (WebSocket)

```go
func (s *DeliveryService) DeliverInApp(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Build WebSocket message
    wsMessage := &websocket.WSMessage{
        Type: websocket.MessageTypeNotification,
        Payload: map[string]interface{}{
            "id":        notification.ID,
            "type":      notification.Type,
            "title":     notification.Title,
            "body":      notification.Body,
            "priority":  notification.Priority,
            "category":  notification.Category,
            "icon":      notification.Icon,
            "actionUrl": notification.ActionURL,
            "metadata":  notification.Metadata,
            "createdAt": notification.CreatedAt,
        },
        Timestamp: time.Now().UnixMilli(),
    }
    
    // Serialize message
    data, err := json.Marshal(wsMessage)
    if err != nil {
        return fmt.Errorf("failed to serialize message: %w", err)
    }
    
    // Send via Hub (handles multi-device, scaling)
    s.hub.SendToUser(notification.UserID, data)
    
    return nil
}
```

---

## 5. Email Delivery (via Kafka)

```go
const EmailNotificationTopic = "email.notification"

func (s *DeliveryService) DeliverEmail(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Build email event for mailservice
    emailEvent := map[string]interface{}{
        "type":       "NOTIFICATION_EMAIL",
        "userId":     notification.UserID,
        "templateId": s.mapToEmailTemplate(notification.Type),
        "subject":    notification.Title,
        "data": map[string]interface{}{
            "title":     notification.Title,
            "body":      notification.Body,
            "actionUrl": notification.ActionURL,
            "category":  notification.Category,
        },
        "timestamp": time.Now().UnixMilli(),
    }
    
    // Send to Kafka for mailservice to process
    key := fmt.Sprintf("user-%d", notification.UserID)
    return s.kafkaProducer.SendMessage(ctx, EmailNotificationTopic, key, emailEvent)
}

func (s *DeliveryService) mapToEmailTemplate(notifType enum.NotificationType) string {
    switch notifType {
    case enum.TypeTaskAssigned:
        return "task_assigned_email"
    case enum.TypeMention:
        return "mention_email"
    default:
        return "generic_notification_email"
    }
}
```

---

## 6. Push Delivery (via Kafka)

```go
const PushNotificationTopic = "push.notification"

func (s *DeliveryService) DeliverPush(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Build push event
    pushEvent := map[string]interface{}{
        "type":   "PUSH_NOTIFICATION",
        "userId": notification.UserID,
        "notification": map[string]interface{}{
            "title":    notification.Title,
            "body":     notification.Body,
            "icon":     notification.Icon,
            "badge":    1,
            "sound":    "default",
            "priority": s.mapPushPriority(notification.Priority),
            "data": map[string]interface{}{
                "notificationId": notification.ID,
                "type":           notification.Type,
                "actionUrl":      notification.ActionURL,
            },
        },
        "timestamp": time.Now().UnixMilli(),
    }
    
    key := fmt.Sprintf("user-%d", notification.UserID)
    return s.kafkaProducer.SendMessage(ctx, PushNotificationTopic, key, pushEvent)
}

func (s *DeliveryService) mapPushPriority(priority enum.NotificationPriority) string {
    switch priority {
    case enum.PriorityUrgent:
        return "high"
    case enum.PriorityHigh:
        return "high"
    default:
        return "normal"
    }
}
```

---

## 7. Bulk Delivery

```go
func (s *DeliveryService) DeliverBulk(
    ctx context.Context,
    notifications []*entity.NotificationEntity,
) error {
    var wg sync.WaitGroup
    errChan := make(chan error, len(notifications))
    
    // Limit concurrency
    semaphore := make(chan struct{}, 10)
    
    for _, notification := range notifications {
        wg.Add(1)
        go func(n *entity.NotificationEntity) {
            defer wg.Done()
            
            semaphore <- struct{}{}
            defer func() { <-semaphore }()
            
            if err := s.Deliver(ctx, n); err != nil {
                errChan <- fmt.Errorf("notification %d: %w", n.ID, err)
            }
        }(notification)
    }
    
    wg.Wait()
    close(errChan)
    
    // Collect errors
    var errors []error
    for err := range errChan {
        errors = append(errors, err)
    }
    
    if len(errors) > 0 {
        return fmt.Errorf("bulk delivery errors: %v", errors)
    }
    
    return nil
}
```

---

## 8. Broadcast to All Users

```go
func (s *DeliveryService) Broadcast(
    ctx context.Context,
    notification *entity.NotificationEntity,
) error {
    // Build WebSocket message
    wsMessage := &websocket.WSMessage{
        Type: websocket.MessageTypeBroadcast,
        Payload: map[string]interface{}{
            "type":     notification.Type,
            "title":    notification.Title,
            "body":     notification.Body,
            "priority": notification.Priority,
        },
        Timestamp: time.Now().UnixMilli(),
    }
    
    data, _ := json.Marshal(wsMessage)
    
    // Broadcast to all connected clients
    s.hub.Broadcast(data)
    
    return nil
}
```

---

## 9. Delivery Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    DeliveryService.Deliver()                     │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
                   ┌─────────────────────┐
                   │ Get Enabled Channels│
                   │   (PreferenceService)│
                   └──────────┬──────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
   ┌───────────┐       ┌───────────┐       ┌───────────┐
   │  In-App   │       │   Email   │       │   Push    │
   │ (WebSocket)│       │  (Kafka)  │       │  (Kafka)  │
   └─────┬─────┘       └─────┬─────┘       └─────┬─────┘
         │                   │                   │
         ▼                   ▼                   ▼
   ┌───────────┐       ┌───────────┐       ┌───────────┐
   │    Hub    │       │mailservice│       │push-service│
   │ SendToUser│       │  Consumer │       │  Consumer │
   └───────────┘       └───────────┘       └───────────┘
```
