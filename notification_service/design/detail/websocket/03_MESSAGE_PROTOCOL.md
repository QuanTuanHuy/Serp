# WebSocket Message Protocol

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Message Format

Tất cả messages đều sử dụng JSON format với cấu trúc chuẩn.

```go
// infrastructure/websocket/message.go
package websocket

import "encoding/json"

type WSMessage struct {
    // Message type identifier
    Type string `json:"type"`
    
    // Message payload (varies by type)
    Payload json.RawMessage `json:"payload,omitempty"`
    
    // Unix timestamp in milliseconds
    Timestamp int64 `json:"timestamp"`
    
    // Unique message ID for acknowledgment
    MessageID string `json:"messageId,omitempty"`
}
```

---

## 2. Server → Client Messages

### 2.1. NEW_NOTIFICATION

Gửi khi có notification mới.

```json
{
  "type": "NEW_NOTIFICATION",
  "payload": {
    "id": 12345,
    "title": "Task Assigned",
    "message": "You have been assigned task: Setup API Gateway",
    "type": "INFO",
    "category": "TASK",
    "priority": "HIGH",
    "sourceService": "ptm_task",
    "actionUrl": "/ptm/tasks/456",
    "actionType": "VIEW",
    "entityType": "task",
    "entityId": 456,
    "isRead": false,
    "createdAt": 1702454400000,
    "metadata": {
      "taskName": "Setup API Gateway",
      "assignedBy": "John Doe",
      "projectName": "SERP Project"
    }
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12345"
}
```

**Payload Structure:**
```go
type NotificationPayload struct {
    ID            int64                  `json:"id"`
    Title         string                 `json:"title"`
    Message       string                 `json:"message"`
    Type          string                 `json:"type"`
    Category      string                 `json:"category"`
    Priority      string                 `json:"priority"`
    SourceService string                 `json:"sourceService"`
    ActionURL     *string                `json:"actionUrl,omitempty"`
    ActionType    *string                `json:"actionType,omitempty"`
    EntityType    *string                `json:"entityType,omitempty"`
    EntityID      *int64                 `json:"entityId,omitempty"`
    IsRead        bool                   `json:"isRead"`
    CreatedAt     int64                  `json:"createdAt"`
    Metadata      map[string]interface{} `json:"metadata,omitempty"`
}
```

---

### 2.2. UNREAD_COUNT_UPDATE

Cập nhật số lượng chưa đọc.

```json
{
  "type": "UNREAD_COUNT_UPDATE",
  "payload": {
    "totalUnread": 15,
    "byCategory": {
      "TASK": 8,
      "CRM": 4,
      "SALES": 2,
      "SYSTEM": 1
    },
    "hasUrgent": true
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12346"
}
```

**Payload Structure:**
```go
type UnreadCountPayload struct {
    TotalUnread int64            `json:"totalUnread"`
    ByCategory  map[string]int64 `json:"byCategory"`
    HasUrgent   bool             `json:"hasUrgent"`
}
```

---

### 2.3. NOTIFICATION_READ

Thông báo notification đã được đọc (sync across devices).

```json
{
  "type": "NOTIFICATION_READ",
  "payload": {
    "notificationIds": [1, 2, 3],
    "remainingUnread": 12
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12347"
}
```

---

### 2.4. NOTIFICATION_DELETED

Thông báo notification đã bị xóa.

```json
{
  "type": "NOTIFICATION_DELETED",
  "payload": {
    "notificationIds": [1, 2],
    "remainingUnread": 10
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12348"
}
```

---

### 2.5. PONG

Phản hồi cho PING từ client.

```json
{
  "type": "PONG",
  "payload": {},
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12349"
}
```

---

### 2.6. SUBSCRIBED / UNSUBSCRIBED

Xác nhận subscription thay đổi.

```json
{
  "type": "SUBSCRIBED",
  "payload": {
    "categories": ["TASK", "CRM"]
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12350"
}
```

---

### 2.7. ERROR

Thông báo lỗi.

```json
{
  "type": "ERROR",
  "payload": {
    "code": "INVALID_MESSAGE",
    "message": "Invalid message format"
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12351"
}
```

---

### 2.8. SYSTEM_ANNOUNCEMENT

Thông báo hệ thống (maintenance, updates).

```json
{
  "type": "SYSTEM_ANNOUNCEMENT",
  "payload": {
    "title": "Scheduled Maintenance",
    "message": "System will be under maintenance from 2AM to 4AM",
    "severity": "warning",
    "actionUrl": "/announcements/123"
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-12352"
}
```

---

## 3. Client → Server Messages

### 3.1. PING

Client heartbeat.

```json
{
  "type": "PING",
  "timestamp": 1702454400000
}
```

---

### 3.2. ACK

Xác nhận đã nhận message.

```json
{
  "type": "ACK",
  "payload": {
    "messageId": "msg-uuid-12345"
  },
  "timestamp": 1702454400000
}
```

---

### 3.3. SUBSCRIBE

Đăng ký nhận notifications theo category.

```json
{
  "type": "SUBSCRIBE",
  "payload": {
    "categories": ["TASK", "CRM", "SYSTEM"]
  },
  "timestamp": 1702454400000
}
```

**Valid Categories:**
- `TASK` - Task notifications
- `CRM` - CRM notifications
- `SALES` - Sales notifications
- `SYSTEM` - System notifications
- `EMAIL` - Email notifications

---

### 3.4. UNSUBSCRIBE

Hủy đăng ký category.

```json
{
  "type": "UNSUBSCRIBE",
  "payload": {
    "categories": ["SALES"]
  },
  "timestamp": 1702454400000
}
```

---

## 4. Message Builder Helper

```go
// infrastructure/websocket/message_builder.go
package websocket

import (
    "encoding/json"
    "time"
    
    "github.com/google/uuid"
)

type MessageBuilder struct{}

func NewMessageBuilder() *MessageBuilder {
    return &MessageBuilder{}
}

func (b *MessageBuilder) NewNotification(notif *NotificationPayload) ([]byte, error) {
    return b.build("NEW_NOTIFICATION", notif)
}

func (b *MessageBuilder) UnreadCountUpdate(payload *UnreadCountPayload) ([]byte, error) {
    return b.build("UNREAD_COUNT_UPDATE", payload)
}

func (b *MessageBuilder) NotificationRead(ids []int64, remaining int64) ([]byte, error) {
    return b.build("NOTIFICATION_READ", map[string]interface{}{
        "notificationIds":  ids,
        "remainingUnread": remaining,
    })
}

func (b *MessageBuilder) NotificationDeleted(ids []int64, remaining int64) ([]byte, error) {
    return b.build("NOTIFICATION_DELETED", map[string]interface{}{
        "notificationIds":  ids,
        "remainingUnread": remaining,
    })
}

func (b *MessageBuilder) SystemAnnouncement(title, message, severity string) ([]byte, error) {
    return b.build("SYSTEM_ANNOUNCEMENT", map[string]interface{}{
        "title":    title,
        "message":  message,
        "severity": severity,
    })
}

func (b *MessageBuilder) Error(code, message string) ([]byte, error) {
    return b.build("ERROR", map[string]interface{}{
        "code":    code,
        "message": message,
    })
}

func (b *MessageBuilder) build(msgType string, payload interface{}) ([]byte, error) {
    payloadBytes, err := json.Marshal(payload)
    if err != nil {
        return nil, err
    }
    
    msg := WSMessage{
        Type:      msgType,
        Payload:   payloadBytes,
        Timestamp: time.Now().UnixMilli(),
        MessageID: uuid.NewString(),
    }
    
    return json.Marshal(msg)
}
```

---

## 5. Message Type Constants

```go
// infrastructure/websocket/constants.go
package websocket

const (
    // Server → Client
    MsgTypeNewNotification    = "NEW_NOTIFICATION"
    MsgTypeUnreadCountUpdate  = "UNREAD_COUNT_UPDATE"
    MsgTypeNotificationRead   = "NOTIFICATION_READ"
    MsgTypeNotificationDeleted = "NOTIFICATION_DELETED"
    MsgTypePong               = "PONG"
    MsgTypeSubscribed         = "SUBSCRIBED"
    MsgTypeUnsubscribed       = "UNSUBSCRIBED"
    MsgTypeError              = "ERROR"
    MsgTypeSystemAnnouncement = "SYSTEM_ANNOUNCEMENT"
    
    // Client → Server
    MsgTypePing        = "PING"
    MsgTypeAck         = "ACK"
    MsgTypeSubscribe   = "SUBSCRIBE"
    MsgTypeUnsubscribe = "UNSUBSCRIBE"
)

// Error codes
const (
    ErrCodeInvalidMessage   = "INVALID_MESSAGE"
    ErrCodeUnauthorized     = "UNAUTHORIZED"
    ErrCodeRateLimited      = "RATE_LIMITED"
    ErrCodeInternalError    = "INTERNAL_ERROR"
)
```
