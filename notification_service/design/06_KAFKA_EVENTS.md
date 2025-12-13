# Kafka Events Design

**Module:** notification_service  
**Ngày tạo:** 2025-12-13

---

## 1. Event Flow Overview

```
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   ptm_task    │    │     crm       │    │    sales      │
│               │    │               │    │               │
│ TaskAssigned  │    │ LeadAssigned  │    │ OrderCreated  │
│ TaskOverdue   │    │ DealWon       │    │ PaymentDue    │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                             ▼
                   ┌─────────────────┐
                   │      Kafka      │
                   │                 │
                   │ NOTIFICATION_   │
                   │ REQUEST_TOPIC   │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  notification   │
                   │    service      │
                   │                 │
                   │  - Create notif │
                   │  - Send WS      │
                   │  - Send email   │
                   └─────────────────┘
```

---

## 2. Kafka Topics

### 2.1. Consumer Topics

| Topic | Description | Producer |
|-------|-------------|----------|
| `NOTIFICATION_REQUEST_TOPIC` | Request tạo notification | All services |
| `TASK_EVENT_TOPIC` | Task events | ptm_task |
| `CRM_EVENT_TOPIC` | CRM events | crm |
| `SALES_EVENT_TOPIC` | Sales events | sales |
| `USER_EVENT_TOPIC` | User events | account |

### 2.2. Producer Topics

| Topic | Description | Consumer |
|-------|-------------|----------|
| `EMAIL_REQUEST_TOPIC` | Request gửi email | mailservice |
| `PUSH_REQUEST_TOPIC` | Push notification request | push_service |
| `NOTIFICATION_DELIVERED_TOPIC` | Delivery confirmation | logging_tracker |

---

## 3. Event Schemas

### 3.1. NotificationRequestEvent

```go
// core/domain/dto/event/notification_request_event.go
package event

type NotificationRequestEvent struct {
    EventID   string `json:"eventId"`
    EventType string `json:"eventType"` // CREATE_NOTIFICATION
    Timestamp int64  `json:"timestamp"`
    
    // Target
    UserID   int64   `json:"userId"`
    UserIDs  []int64 `json:"userIds,omitempty"` // For bulk
    TenantID int64   `json:"tenantId"`
    
    // Content
    Title    string `json:"title"`
    Message  string `json:"message"`
    Type     string `json:"type"`
    Category string `json:"category"`
    Priority string `json:"priority"`
    
    // Source
    SourceService string `json:"sourceService"`
    SourceEventID string `json:"sourceEventId"`
    
    // Action
    ActionURL  string `json:"actionUrl,omitempty"`
    ActionType string `json:"actionType,omitempty"`
    EntityType string `json:"entityType,omitempty"`
    EntityID   int64  `json:"entityId,omitempty"`
    
    // Delivery preferences
    Channels []string `json:"channels"` // ["IN_APP", "EMAIL"]
    
    // Optional
    ExpiresAt int64                  `json:"expiresAt,omitempty"`
    Metadata  map[string]interface{} `json:"metadata,omitempty"`
    
    // Template (optional)
    TemplateCode string                 `json:"templateCode,omitempty"`
    TemplateVars map[string]interface{} `json:"templateVars,omitempty"`
}
```

### 3.2. TaskEventPayload

```go
// Events from ptm_task service
type TaskEventPayload struct {
    EventID   string `json:"eventId"`
    EventType string `json:"eventType"` // TASK_ASSIGNED, TASK_OVERDUE, etc.
    Timestamp int64  `json:"timestamp"`
    
    TaskID      int64  `json:"taskId"`
    TaskTitle   string `json:"taskTitle"`
    UserID      int64  `json:"userId"`      // Task owner
    AssignedTo  int64  `json:"assignedTo"`  // Target user
    AssignedBy  int64  `json:"assignedBy"`
    TenantID    int64  `json:"tenantId"`
    Priority    string `json:"priority"`
    DeadlineMs  *int64 `json:"deadlineMs,omitempty"`
}
```

### 3.3. CRMEventPayload

```go
// Events from crm service  
type CRMEventPayload struct {
    EventID   string `json:"eventId"`
    EventType string `json:"eventType"` // LEAD_ASSIGNED, DEAL_WON, etc.
    Timestamp int64  `json:"timestamp"`
    
    EntityType string `json:"entityType"` // lead, customer, opportunity
    EntityID   int64  `json:"entityId"`
    EntityName string `json:"entityName"`
    
    UserID     int64 `json:"userId"`     // Actor
    AssignedTo int64 `json:"assignedTo"` // Target user
    TenantID   int64 `json:"tenantId"`
    
    // Context
    Value    *float64 `json:"value,omitempty"`    // Deal value
    Stage    *string  `json:"stage,omitempty"`    // Pipeline stage
    Metadata map[string]interface{} `json:"metadata,omitempty"`
}
```

### 3.4. EmailRequestEvent (Outbound)

```go
type EmailRequestEvent struct {
    EventID      string `json:"eventId"`
    Timestamp    int64  `json:"timestamp"`
    
    To           []string `json:"to"`
    Subject      string   `json:"subject"`
    Body         string   `json:"body"`
    BodyHTML     string   `json:"bodyHtml"`
    
    TemplateCode string                 `json:"templateCode,omitempty"`
    TemplateVars map[string]interface{} `json:"templateVars,omitempty"`
    
    // Reference
    NotificationID int64  `json:"notificationId"`
    TenantID       int64  `json:"tenantId"`
    
    // Priority
    Priority string `json:"priority"` // LOW, NORMAL, HIGH
}
```

---

## 4. Kafka Consumer Implementation

### 4.1. Consumer Handler

```go
// ui/kafka/notification_consumer.go
package kafka

import (
    "context"
    "encoding/json"
    "github.com/serp/notification-service/src/core/domain/dto/event"
    "github.com/serp/notification-service/src/core/usecase"
)

type NotificationConsumer struct {
    notificationUseCase usecase.INotificationUseCase
    logger              *zap.Logger
}

func NewNotificationConsumer(
    uc usecase.INotificationUseCase,
    logger *zap.Logger,
) *NotificationConsumer {
    return &NotificationConsumer{
        notificationUseCase: uc,
        logger:              logger,
    }
}

func (c *NotificationConsumer) HandleMessage(
    ctx context.Context,
    topic string,
    key, value []byte,
) error {
    switch topic {
    case "NOTIFICATION_REQUEST_TOPIC":
        return c.handleNotificationRequest(ctx, value)
    case "TASK_EVENT_TOPIC":
        return c.handleTaskEvent(ctx, value)
    case "CRM_EVENT_TOPIC":
        return c.handleCRMEvent(ctx, value)
    default:
        c.logger.Warn("Unknown topic", zap.String("topic", topic))
        return nil
    }
}

func (c *NotificationConsumer) handleNotificationRequest(
    ctx context.Context,
    value []byte,
) error {
    var evt event.NotificationRequestEvent
    if err := json.Unmarshal(value, &evt); err != nil {
        return err
    }
    
    return c.notificationUseCase.CreateFromEvent(ctx, &evt)
}

func (c *NotificationConsumer) handleTaskEvent(
    ctx context.Context,
    value []byte,
) error {
    var evt event.TaskEventPayload
    if err := json.Unmarshal(value, &evt); err != nil {
        return err
    }
    
    // Transform task event to notification
    switch evt.EventType {
    case "TASK_ASSIGNED":
        return c.notificationUseCase.CreateTaskAssignedNotification(ctx, &evt)
    case "TASK_OVERDUE":
        return c.notificationUseCase.CreateTaskOverdueNotification(ctx, &evt)
    }
    
    return nil
}
```

---

## 5. Event Mapping Rules

### 5.1. Task Events → Notifications

| Event | Notification |
|-------|--------------|
| TASK_ASSIGNED | "Task '{TaskTitle}' assigned to you" |
| TASK_OVERDUE | "Task '{TaskTitle}' is overdue!" |
| TASK_COMPLETED | "Task '{TaskTitle}' marked as complete" |
| TASK_COMMENTED | "New comment on task '{TaskTitle}'" |

### 5.2. CRM Events → Notifications

| Event | Notification |
|-------|--------------|
| LEAD_ASSIGNED | "Lead '{LeadName}' assigned to you" |
| DEAL_WON | "🎉 Deal '{DealName}' won! Value: ${Value}" |
| DEAL_LOST | "Deal '{DealName}' marked as lost" |
| ACTIVITY_DUE | "Activity '{ActivityName}' due today" |

### 5.3. Sales Events → Notifications

| Event | Notification |
|-------|--------------|
| ORDER_CREATED | "New order #{OrderID} created" |
| PAYMENT_RECEIVED | "Payment received for order #{OrderID}" |
| ORDER_SHIPPED | "Order #{OrderID} has been shipped" |

---

## 6. Producer Implementation

```go
// infrastructure/client/kafka_producer.go
package client

type KafkaProducerAdapter struct {
    producer *kafka.Producer
    logger   *zap.Logger
}

func (k *KafkaProducerAdapter) SendEmailRequest(
    ctx context.Context,
    event *EmailRequestEvent,
) error {
    data, err := json.Marshal(event)
    if err != nil {
        return err
    }
    
    return k.producer.Produce(&kafka.Message{
        TopicPartition: kafka.TopicPartition{
            Topic: stringPtr("EMAIL_REQUEST_TOPIC"),
        },
        Key:   []byte(event.EventID),
        Value: data,
    }, nil)
}
```

---

## 7. Error Handling & Retry

```go
// Dead Letter Queue for failed messages
const DLQ_TOPIC = "NOTIFICATION_DLQ"

func (c *NotificationConsumer) HandleWithRetry(
    ctx context.Context,
    topic string,
    key, value []byte,
) error {
    var lastErr error
    
    for attempt := 1; attempt <= 3; attempt++ {
        if err := c.HandleMessage(ctx, topic, key, value); err != nil {
            lastErr = err
            time.Sleep(time.Duration(attempt) * time.Second)
            continue
        }
        return nil
    }
    
    // Send to DLQ after max retries
    return c.sendToDLQ(topic, key, value, lastErr)
}
```
