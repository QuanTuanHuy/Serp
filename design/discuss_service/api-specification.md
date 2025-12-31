# Discuss Service - API Specification

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - REST API Endpoints  
**Date:** December 2025  

## 🌐 Base Configuration

```
Service: discuss_service
Port: 8092
Base Path: /discuss/api/v1
Protocol: HTTP/1.1, WebSocket
Authentication: JWT (via API Gateway)
Content-Type: application/json
```

## 🔐 Authentication

All requests must include JWT token in header:
```
Authorization: Bearer <JWT_TOKEN>
```

API Gateway extracts and forwards:
```
X-User-ID: 123
X-Tenant-ID: 456
```

---

## 📋 API Endpoints Overview

### **Channels**
- `POST /channels` - Create channel
- `GET /channels` - List user's channels
- `GET /channels/:id` - Get channel details
- `PATCH /channels/:id` - Update channel
- `DELETE /channels/:id` - Archive channel
- `POST /channels/:id/members` - Add members
- `DELETE /channels/:id/members/:userId` - Remove member
- `GET /channels/:id/members` - List channel members

### **Messages**
- `POST /channels/:channelId/messages` - Send message
- `GET /channels/:channelId/messages` - Get messages (paginated)
- `GET /messages/:id` - Get single message
- `PATCH /messages/:id` - Edit message
- `DELETE /messages/:id` - Delete message
- `POST /messages/:id/reactions` - Add reaction
- `DELETE /messages/:id/reactions/:emoji` - Remove reaction
- `POST /messages/:id/read` - Mark as read

### **Search**
- `GET /search/messages` - Search messages
- `GET /search/channels` - Search channels

### **Activity Feed**
- `GET /activity-feed` - Get user's activity feed
- `PATCH /activity-feed/:id/read` - Mark activity as read
- `POST /activity-feed/read-all` - Mark all as read

### **Attachments**
- `POST /attachments/upload` - Upload file
- `GET /attachments/:id/download` - Download file

### **Presence**
- `GET /presence/online-users` - Get online users
- `POST /presence/typing` - Start typing indicator
- `DELETE /presence/typing` - Stop typing indicator

---

## 📝 Detailed API Specifications

### **1. Create Channel**

Creates a new channel (DIRECT, GROUP, or TOPIC).

**Endpoint:** `POST /channels`

**Request Body:**
```json
{
  "name": "Sales Team Q1",
  "description": "Q1 sales planning and coordination",
  "type": "GROUP",
  "isPrivate": false,
  "memberIds": [100, 101, 102],
  "entityType": null,
  "entityId": null,
  "metadata": {
    "color": "#FF5733",
    "icon": "👥"
  }
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Channel name (max 255 chars) |
| description | string | No | Channel description |
| type | string | Yes | "DIRECT", "GROUP", or "TOPIC" |
| isPrivate | boolean | No | Default: false for GROUP, true for DIRECT |
| memberIds | array | Yes | User IDs to add as members |
| entityType | string | Conditional | Required if type="TOPIC" |
| entityId | number | Conditional | Required if type="TOPIC" |
| metadata | object | No | Custom metadata (color, icon, etc.) |

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "Channel created successfully",
  "data": {
    "id": 1,
    "tenantId": 456,
    "name": "Sales Team Q1",
    "description": "Q1 sales planning and coordination",
    "type": "GROUP",
    "entityType": null,
    "entityId": null,
    "isPrivate": false,
    "isArchived": false,
    "memberCount": 3,
    "messageCount": 0,
    "lastMessageAt": null,
    "createdBy": 123,
    "createdAt": 1704067200000,
    "updatedAt": 1704067200000,
    "metadata": {
      "color": "#FF5733",
      "icon": "👥"
    }
  }
}
```

**Validation Rules:**
- For DIRECT: memberIds must have exactly 2 users
- For GROUP/TOPIC: memberIds must have at least 1 user
- For TOPIC: entityType and entityId are required
- Duplicate DIRECT channel returns existing channel

**Errors:**
- `400 Bad Request` - Invalid input
- `403 Forbidden` - Cannot access entity (for TOPIC)
- `404 Not Found` - Entity not found (for TOPIC)

---

### **2. List User's Channels**

Get all channels user is a member of.

**Endpoint:** `GET /channels`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| type | string | all | Filter by type: "DIRECT", "GROUP", "TOPIC", "all" |
| isPinned | boolean | - | Filter pinned channels |
| hasUnread | boolean | - | Filter channels with unread messages |
| page | number | 1 | Page number |
| pageSize | number | 50 | Items per page (max 100) |
| sortBy | string | lastMessage | "lastMessage", "name", "createdAt" |
| sortOrder | string | desc | "asc" or "desc" |

**Example Request:**
```
GET /channels?type=GROUP&hasUnread=true&page=1&pageSize=20
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "id": 1,
        "name": "Sales Team Q1",
        "type": "GROUP",
        "isPrivate": false,
        "memberCount": 5,
        "unreadCount": 3,
        "isPinned": true,
        "isMuted": false,
        "lastMessageAt": 1704067200000,
        "lastMessagePreview": "Great work on the proposal!",
        "lastMessageSender": {
          "id": 100,
          "name": "John Doe"
        },
        "members": [
          {"id": 100, "name": "John Doe", "avatar": "..."},
          {"id": 101, "name": "Jane Smith", "avatar": "..."}
        ]
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "totalItems": 45,
      "totalPages": 3
    }
  }
}
```

---

### **3. Get Channel Details**

**Endpoint:** `GET /channels/:id`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | number | Channel ID |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "tenantId": 456,
    "name": "Sales Team Q1",
    "description": "Q1 sales planning and coordination",
    "type": "GROUP",
    "isPrivate": false,
    "isArchived": false,
    "memberCount": 5,
    "messageCount": 142,
    "lastMessageAt": 1704067200000,
    "createdBy": 123,
    "createdAt": 1704000000000,
    "members": [
      {
        "userId": 100,
        "name": "John Doe",
        "avatar": "https://...",
        "role": "OWNER",
        "joinedAt": 1704000000000,
        "isOnline": true,
        "lastSeen": 1704067200000
      }
    ],
    "myMembership": {
      "role": "MEMBER",
      "unreadCount": 3,
      "isPinned": true,
      "isMuted": false,
      "notificationLevel": "ALL"
    }
  }
}
```

**Errors:**
- `403 Forbidden` - Not a member of this channel
- `404 Not Found` - Channel does not exist

---

### **4. Update Channel**

**Endpoint:** `PATCH /channels/:id`

**Request Body:**
```json
{
  "name": "Sales Team Q1 2025",
  "description": "Updated description",
  "isPrivate": true,
  "metadata": {
    "color": "#0066CC"
  }
}
```

**Authorization:**
- Only channel OWNER or ADMIN can update

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Channel updated successfully",
  "data": {
    "id": 1,
    "name": "Sales Team Q1 2025",
    ...
  }
}
```

---

### **5. Archive Channel**

**Endpoint:** `DELETE /channels/:id`

Archives a channel (soft delete).

**Authorization:**
- Only channel OWNER can archive

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Channel archived successfully"
}
```

**Note:** Archived channels are hidden but data is preserved.

---

### **6. Add Channel Members**

**Endpoint:** `POST /channels/:id/members`

**Request Body:**
```json
{
  "userIds": [105, 106],
  "role": "MEMBER",
  "notifyMembers": true
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| userIds | array | Yes | User IDs to add |
| role | string | No | "MEMBER" (default), "ADMIN" |
| notifyMembers | boolean | No | Send notification (default: true) |

**Authorization:**
- Channel OWNER/ADMIN can add members
- For private channels: only OWNER

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "Members added successfully",
  "data": {
    "addedCount": 2,
    "members": [
      {"userId": 105, "name": "Alice Brown", "role": "MEMBER"},
      {"userId": 106, "name": "Bob Wilson", "role": "MEMBER"}
    ]
  }
}
```

---

### **7. Remove Channel Member**

**Endpoint:** `DELETE /channels/:channelId/members/:userId`

**Authorization:**
- Channel OWNER/ADMIN can remove others
- Any member can remove themselves (leave)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Member removed successfully"
}
```

---

### **8. Send Message**

**Endpoint:** `POST /channels/:channelId/messages`

**Request Body:**
```json
{
  "content": "Hey @John, can you review the Q1 report?",
  "messageType": "TEXT",
  "parentId": null,
  "mentions": [100],
  "attachments": [
    {
      "fileName": "Q1_Report.pdf",
      "fileSize": 1048576,
      "fileType": "application/pdf",
      "s3Key": "attachments/abc123.pdf"
    }
  ],
  "metadata": {
    "formatting": "markdown"
  }
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| content | string | Yes | Message content (max 10,000 chars) |
| messageType | string | No | "TEXT" (default), "IMAGE", "FILE", "CODE" |
| parentId | number | No | Reply to message ID (for threads) |
| mentions | array | No | User IDs mentioned (@user) |
| attachments | array | No | Uploaded attachments |
| metadata | object | No | Custom metadata |

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "Message sent successfully",
  "data": {
    "id": 500,
    "channelId": 1,
    "senderId": 123,
    "content": "Hey @John, can you review the Q1 report?",
    "messageType": "TEXT",
    "mentions": [100],
    "parentId": null,
    "threadCount": 0,
    "isEdited": false,
    "isDeleted": false,
    "reactions": [],
    "readBy": [123],
    "attachments": [
      {
        "id": 10,
        "fileName": "Q1_Report.pdf",
        "fileSize": 1048576,
        "fileType": "application/pdf",
        "s3Url": "https://s3.../Q1_Report.pdf",
        "thumbnailUrl": null
      }
    ],
    "sender": {
      "id": 123,
      "name": "Current User",
      "avatar": "https://..."
    },
    "createdAt": 1704067200000,
    "updatedAt": 1704067200000
  }
}
```

**Validation:**
- User must be active channel member
- Content cannot be empty for TEXT type
- Mentions must be valid user IDs

**Side Effects:**
- Increments unread count for other members
- Triggers WebSocket broadcast to online members
- Sends push notifications to offline members
- Creates activity feed entries for @mentions

---

### **9. Get Messages**

**Endpoint:** `GET /channels/:channelId/messages`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| beforeId | number | - | Get messages before this ID (pagination) |
| afterId | number | - | Get messages after this ID (live updates) |
| limit | number | 50 | Number of messages (max 100) |
| includeThreads | boolean | false | Include thread replies |

**Example Request:**
```
GET /channels/1/messages?beforeId=500&limit=20
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "messages": [
      {
        "id": 500,
        "channelId": 1,
        "senderId": 123,
        "content": "Hello team!",
        "messageType": "TEXT",
        "mentions": [],
        "parentId": null,
        "threadCount": 2,
        "isEdited": false,
        "reactions": [
          {"emoji": "👍", "count": 3, "userIds": [100, 101, 102]},
          {"emoji": "❤️", "count": 1, "userIds": [103]}
        ],
        "sender": {
          "id": 123,
          "name": "John Doe",
          "avatar": "https://..."
        },
        "createdAt": 1704067200000
      }
    ],
    "hasMore": true,
    "oldestMessageId": 480
  }
}
```

---

### **10. Edit Message**

**Endpoint:** `PATCH /messages/:id`

**Request Body:**
```json
{
  "content": "Updated message content"
}
```

**Authorization:**
- Only message sender can edit
- Cannot edit after 24 hours (configurable)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Message updated successfully",
  "data": {
    "id": 500,
    "content": "Updated message content",
    "isEdited": true,
    "editedAt": 1704067300000,
    ...
  }
}
```

---

### **11. Delete Message**

**Endpoint:** `DELETE /messages/:id`

**Authorization:**
- Message sender can delete
- Channel OWNER/ADMIN can delete any message

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Message deleted successfully"
}
```

**Note:** Soft delete - message marked as deleted but kept in DB

---

### **12. Add Reaction**

**Endpoint:** `POST /messages/:id/reactions`

**Request Body:**
```json
{
  "emoji": "👍"
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "Reaction added",
  "data": {
    "messageId": 500,
    "emoji": "👍",
    "count": 4,
    "userIds": [100, 101, 102, 123]
  }
}
```

---

### **13. Remove Reaction**

**Endpoint:** `DELETE /messages/:id/reactions/:emoji`

**Path Parameters:**
- `emoji`: URL-encoded emoji (e.g., `%F0%9F%91%8D` for 👍)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Reaction removed"
}
```

---

### **14. Mark Message as Read**

**Endpoint:** `POST /messages/:id/read`

Updates last read message for user.

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Message marked as read",
  "data": {
    "channelId": 1,
    "lastReadMessageId": 500,
    "unreadCount": 0
  }
}
```

**Side Effects:**
- Updates `channel_members.last_read_msg_id`
- Resets `channel_members.unread_count` to 0
- Broadcasts read receipt via WebSocket

---

### **15. Search Messages**

**Endpoint:** `GET /search/messages`

Full-text search across user's channels.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| query | string | Yes | Search term (min 3 chars) |
| channelIds | array | No | Filter by channels |
| senderIds | array | No | Filter by senders |
| dateFrom | number | No | Unix timestamp (ms) |
| dateTo | number | No | Unix timestamp (ms) |
| hasAttachment | boolean | No | Only messages with files |
| page | number | No | Page number (default: 1) |
| pageSize | number | No | Items per page (default: 20, max: 50) |

**Example Request:**
```
GET /search/messages?query=report&channelIds=1,2&dateFrom=1704000000000
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "results": [
      {
        "message": {
          "id": 500,
          "content": "Q1 **report** is ready for review",
          "channelId": 1,
          "senderId": 123,
          "createdAt": 1704067200000
        },
        "channel": {
          "id": 1,
          "name": "Sales Team Q1",
          "type": "GROUP"
        },
        "sender": {
          "id": 123,
          "name": "John Doe"
        },
        "highlights": ["Q1 <em>report</em> is ready"]
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "totalItems": 15,
      "totalPages": 1
    }
  }
}
```

---

### **16. Get Activity Feed**

**Endpoint:** `GET /activity-feed`

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| isRead | boolean | - | Filter by read status |
| actionTypes | array | - | Filter by action types |
| page | number | 1 | Page number |
| pageSize | number | 20 | Items per page |

**Example Request:**
```
GET /activity-feed?isRead=false&page=1&pageSize=10
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "activities": [
      {
        "id": 1000,
        "actionType": "MENTION_RECEIVED",
        "title": "Mentioned in Sales Team Q1",
        "description": "John mentioned you: Can you review the report?",
        "actor": {
          "id": 123,
          "name": "John Doe",
          "avatar": "https://..."
        },
        "entityType": "channel",
        "entityId": 1,
        "channelId": 1,
        "messageId": 500,
        "isRead": false,
        "timestamp": 1704067200000,
        "metadata": {
          "messagePreview": "Can you review the report?"
        }
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "totalItems": 23,
      "totalPages": 3
    },
    "unreadCount": 15
  }
}
```

---

### **17. Upload Attachment**

**Endpoint:** `POST /attachments/upload`

**Content-Type:** `multipart/form-data`

**Request:**
```
POST /attachments/upload
Content-Type: multipart/form-data

------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="report.pdf"
Content-Type: application/pdf

<file_content>
------WebKitFormBoundary
Content-Disposition: form-data; name="channelId"

1
------WebKitFormBoundary--
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "File uploaded successfully",
  "data": {
    "id": 10,
    "fileName": "report.pdf",
    "fileSize": 1048576,
    "fileType": "application/pdf",
    "s3Key": "attachments/456/abc123.pdf",
    "s3Url": "https://s3.../abc123.pdf",
    "thumbnailUrl": null,
    "scanStatus": "PENDING",
    "createdAt": 1704067200000
  }
}
```

**Validation:**
- Max file size: 100MB (configurable)
- Allowed types: images, PDFs, documents, archives
- Virus scanning before allowing download

---

### **18. Get Online Users**

**Endpoint:** `GET /presence/online-users`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| channelId | number | Filter by channel |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "onlineUsers": [
      {
        "userId": 100,
        "name": "John Doe",
        "avatar": "https://...",
        "status": "ONLINE",
        "lastSeen": 1704067200000,
        "currentDevice": "web"
      }
    ],
    "totalOnline": 1
  }
}
```

---

### **19. Typing Indicator**

**Endpoint:** `POST /presence/typing`

**Request Body:**
```json
{
  "channelId": 1
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Typing started"
}
```

**Note:** Auto-expires after 10 seconds. Frontend should send every 5s while user is typing.

---

## 📊 Common Response Formats

### **Success Response**
```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

### **Error Response**
```json
{
  "code": 400,
  "message": "Validation error",
  "errors": [
    {
      "field": "content",
      "message": "Content cannot be empty"
    }
  ]
}
```

### **Pagination**
```json
{
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalItems": 100,
    "totalPages": 5
  }
}
```

---

## 🔒 HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PATCH, DELETE |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE (no body) |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid JWT |
| 403 | Forbidden | No permission for resource |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service down/maintenance |

---

## ⏱️ Rate Limiting

```
Per User Limits:
- Create message: 60 requests/minute
- Get messages: 300 requests/minute
- Search: 30 requests/minute
- Upload file: 10 requests/minute

Per Tenant Limits:
- API calls: 10,000 requests/minute
```

**Rate Limit Headers:**
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1704067260
```

---

## 🧪 Example cURL Requests

### **Create Channel**
```bash
curl -X POST https://api.serp.com/discuss/api/v1/channels \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sales Team",
    "type": "GROUP",
    "memberIds": [100, 101]
  }'
```

### **Send Message**
```bash
curl -X POST https://api.serp.com/discuss/api/v1/channels/1/messages \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello team!",
    "messageType": "TEXT"
  }'
```

### **Search Messages**
```bash
curl -X GET "https://api.serp.com/discuss/api/v1/search/messages?query=report&page=1" \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 📚 Summary

This API provides:
- ✅ **Complete CRUD** for channels and messages
- ✅ **Real-time features** (typing, presence)
- ✅ **Rich messaging** (threads, reactions, mentions)
- ✅ **Full-text search** across conversations
- ✅ **Activity feed** for unified notifications
- ✅ **RESTful conventions** (HTTP methods, status codes)
- ✅ **Pagination** for large datasets
- ✅ **Rate limiting** for fair usage
