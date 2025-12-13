# REST API Endpoints

**Module:** notification_service  
**Base Path:** `/notification/api/v1`  
**Ngày tạo:** 2025-12-13

---

## 1. API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | Lấy danh sách notifications |
| GET | `/notifications/:id` | Lấy chi tiết notification |
| POST | `/notifications` | Tạo notification (internal) |
| POST | `/notifications/bulk` | Tạo bulk notifications |
| PUT | `/notifications/:id/read` | Đánh dấu đã đọc |
| PUT | `/notifications/read` | Đánh dấu nhiều đã đọc |
| PUT | `/notifications/read-all` | Đánh dấu tất cả đã đọc |
| DELETE | `/notifications/:id` | Xóa notification |
| DELETE | `/notifications` | Xóa nhiều notifications |
| GET | `/notifications/unread-count` | Đếm chưa đọc |
| GET | `/preferences` | Lấy preferences |
| PUT | `/preferences` | Cập nhật preferences |

---

## 2. Notification Endpoints

### 2.1. Get Notifications

```
GET /notification/api/v1/notifications
```

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| page | int | No | Page number (default: 1) |
| pageSize | int | No | Items per page (default: 20, max: 100) |
| isRead | bool | No | Filter by read status |
| category | string | No | Filter by category |
| type | string | No | Filter by type |
| priority | string | No | Filter by priority |
| fromDate | int64 | No | Start date (Unix ms) |
| toDate | int64 | No | End date (Unix ms) |

**Response (200 OK):**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "notifications": [
      {
        "id": 1,
        "title": "Task Assigned",
        "message": "You have been assigned task: Setup API",
        "type": "INFO",
        "category": "TASK",
        "priority": "HIGH",
        "sourceService": "ptm_task",
        "actionUrl": "/ptm/tasks/123",
        "actionType": "VIEW",
        "entityType": "task",
        "entityId": 123,
        "isRead": false,
        "isArchived": false,
        "createdAt": 1702454400000,
        "metadata": {
          "taskName": "Setup API",
          "assignedBy": "John Doe"
        }
      }
    ],
    "totalCount": 50,
    "unreadCount": 10,
    "page": 1,
    "pageSize": 20
  }
}
```

---

### 2.2. Get Notification Detail

```
GET /notification/api/v1/notifications/:id
```

**Response (200 OK):**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "title": "Task Assigned",
    "message": "You have been assigned task: Setup API",
    "type": "INFO",
    "category": "TASK",
    "priority": "HIGH",
    "sourceService": "ptm_task",
    "actionUrl": "/ptm/tasks/123",
    "entityType": "task",
    "entityId": 123,
    "isRead": true,
    "readAt": 1702458000000,
    "deliveryChannels": ["IN_APP", "EMAIL"],
    "deliveredAt": 1702454400000,
    "createdAt": 1702454400000,
    "metadata": {}
  }
}
```

---

### 2.3. Create Notification (Internal API)

```
POST /notification/api/v1/notifications
```

**Headers:**
- `X-Service-Key`: Internal service authentication

**Request Body:**

```json
{
  "userId": 1,
  "tenantId": 1,
  "title": "New Lead Assigned",
  "message": "Lead 'John Smith' has been assigned to you",
  "type": "INFO",
  "category": "CRM",
  "priority": "MEDIUM",
  "sourceService": "crm",
  "sourceEventId": "lead-assigned-12345",
  "actionUrl": "/crm/leads/456",
  "actionType": "VIEW",
  "entityType": "lead",
  "entityId": 456,
  "deliveryChannels": ["IN_APP", "EMAIL"],
  "expiresAt": 1704067200000,
  "metadata": {
    "leadName": "John Smith",
    "assignedBy": "Jane Doe"
  }
}
```

**Response (201 Created):**

```json
{
  "code": 201,
  "message": "Notification created",
  "data": {
    "id": 123,
    "delivered": true
  }
}
```

---

### 2.4. Create Bulk Notifications

```
POST /notification/api/v1/notifications/bulk
```

**Request Body:**

```json
{
  "userIds": [1, 2, 3, 4, 5],
  "tenantId": 1,
  "title": "System Maintenance",
  "message": "System will be under maintenance from 2AM to 4AM",
  "type": "WARNING",
  "category": "SYSTEM",
  "priority": "HIGH",
  "sourceService": "system",
  "deliveryChannels": ["IN_APP", "EMAIL"]
}
```

**Response (201 Created):**

```json
{
  "code": 201,
  "message": "Bulk notifications created",
  "data": {
    "totalRequested": 5,
    "totalCreated": 5,
    "totalDelivered": 5
  }
}
```

---

### 2.5. Mark as Read

**Single:**
```
PUT /notification/api/v1/notifications/:id/read
```

**Multiple:**
```
PUT /notification/api/v1/notifications/read
```

```json
{
  "notificationIds": [1, 2, 3]
}
```

**All:**
```
PUT /notification/api/v1/notifications/read-all
```

**Response (200 OK):**

```json
{
  "code": 200,
  "message": "Marked as read",
  "data": {
    "updatedCount": 3
  }
}
```

---

### 2.6. Delete Notifications

**Single:**
```
DELETE /notification/api/v1/notifications/:id
```

**Multiple:**
```
DELETE /notification/api/v1/notifications
```

```json
{
  "notificationIds": [1, 2, 3]
}
```

---

### 2.7. Get Unread Count

```
GET /notification/api/v1/notifications/unread-count
```

**Response (200 OK):**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalUnread": 15,
    "byCategory": {
      "TASK": 8,
      "CRM": 4,
      "SALES": 2,
      "SYSTEM": 1
    },
    "hasUrgent": true
  }
}
```

---

## 3. Preferences Endpoints

### 3.1. Get Preferences

```
GET /notification/api/v1/preferences
```

**Response (200 OK):**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "enableInApp": true,
    "enableEmail": true,
    "enablePush": false,
    "categorySettings": {
      "TASK": {
        "enabled": true,
        "channels": ["IN_APP", "EMAIL"]
      },
      "CRM": {
        "enabled": true,
        "channels": ["IN_APP"]
      },
      "SYSTEM": {
        "enabled": true,
        "channels": ["IN_APP", "EMAIL", "PUSH"]
      }
    },
    "quietHoursEnabled": true,
    "quietHoursStart": "22:00",
    "quietHoursEnd": "08:00",
    "timezone": "Asia/Ho_Chi_Minh"
  }
}
```

### 3.2. Update Preferences

```
PUT /notification/api/v1/preferences
```

**Request Body:**

```json
{
  "enableInApp": true,
  "enableEmail": false,
  "enablePush": true,
  "categorySettings": {
    "TASK": {
      "enabled": true,
      "channels": ["IN_APP", "PUSH"]
    }
  },
  "quietHoursEnabled": true,
  "quietHoursStart": "23:00",
  "quietHoursEnd": "07:00",
  "timezone": "Asia/Ho_Chi_Minh"
}
```

---

## 4. Error Responses

| Code | Message | Description |
|------|---------|-------------|
| 400 | Invalid request | Validation failed |
| 401 | Unauthorized | Missing/invalid JWT |
| 403 | Forbidden | No permission |
| 404 | Notification not found | ID không tồn tại |
| 500 | Internal server error | Server error |

```json
{
  "code": 404,
  "message": "Notification not found",
  "data": null
}
```
