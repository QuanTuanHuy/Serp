# Discuss Service - WebSocket Protocol

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Real-time Communication Protocol  
**Date:** December 2025  

## 🔌 WebSocket Connection

### **Endpoint**
```
ws://localhost:8092/ws/discuss
wss://api.serp.com/ws/discuss (production)
```

### **Authentication**
WebSocket connection requires JWT token in query parameter:
```
ws://localhost:8092/ws/discuss?token=<JWT_TOKEN>
```

### **Connection Flow**
```
1. Client initiates WebSocket connection with JWT
2. Server validates JWT via Keycloak
3. Server extracts userID, tenantID from JWT
4. Server registers client in WebSocket Hub
5. Server sends CONNECTED event
6. Client can now send/receive messages
```

---

## 📨 Message Format

All WebSocket messages use JSON format:

```json
{
  "type": "MESSAGE_TYPE",
  "payload": { ... },
  "timestamp": 1704067200000,
  "requestId": "uuid-v4" // Optional, for request-response correlation
}
```

---

## 📤 Client → Server Events

### **1. PING**
Keep-alive heartbeat to prevent connection timeout.

```json
{
  "type": "PING",
  "timestamp": 1704067200000
}
```

**Server Response:**
```json
{
  "type": "PONG",
  "timestamp": 1704067200000
}
```

**Frequency:** Send every 30 seconds

---

### **2. SUBSCRIBE_CHANNEL**
Subscribe to a channel to receive real-time messages.

```json
{
  "type": "SUBSCRIBE_CHANNEL",
  "payload": {
    "channelId": 1
  },
  "requestId": "abc-123"
}
```

**Server Response:**
```json
{
  "type": "CHANNEL_SUBSCRIBED",
  "payload": {
    "channelId": 1,
    "success": true
  },
  "requestId": "abc-123"
}
```

---

### **3. UNSUBSCRIBE_CHANNEL**
Unsubscribe from a channel.

```json
{
  "type": "UNSUBSCRIBE_CHANNEL",
  "payload": {
    "channelId": 1
  }
}
```

---

### **4. SEND_MESSAGE**
Send a message to a channel (alternative to REST API).

```json
{
  "type": "SEND_MESSAGE",
  "payload": {
    "channelId": 1,
    "content": "Hello team!",
    "messageType": "TEXT",
    "mentions": [100],
    "parentId": null
  },
  "requestId": "msg-456"
}
```

**Server Response (Success):**
```json
{
  "type": "MESSAGE_SENT",
  "payload": {
    "id": 500,
    "channelId": 1,
    "senderId": 123,
    "content": "Hello team!",
    "messageType": "TEXT",
    "mentions": [100],
    "createdAt": 1704067200000
  },
  "requestId": "msg-456"
}
```

**Server Response (Error):**
```json
{
  "type": "ERROR",
  "payload": {
    "code": "PERMISSION_DENIED",
    "message": "You are not a member of this channel"
  },
  "requestId": "msg-456"
}
```

---

### **5. TYPING_START**
Indicate user is typing in a channel.

```json
{
  "type": "TYPING_START",
  "payload": {
    "channelId": 1
  }
}
```

**Note:** Auto-expires after 10 seconds. Resend every 5s while typing.

---

### **6. TYPING_STOP**
Indicate user stopped typing.

```json
{
  "type": "TYPING_STOP",
  "payload": {
    "channelId": 1
  }
}
```

---

### **7. MARK_READ**
Mark messages as read up to a specific message ID.

```json
{
  "type": "MARK_READ",
  "payload": {
    "channelId": 1,
    "messageId": 500
  }
}
```

---

### **8. ADD_REACTION**
Add emoji reaction to a message.

```json
{
  "type": "ADD_REACTION",
  "payload": {
    "messageId": 500,
    "emoji": "👍"
  }
}
```

---

### **9. REMOVE_REACTION**
Remove emoji reaction from a message.

```json
{
  "type": "REMOVE_REACTION",
  "payload": {
    "messageId": 500,
    "emoji": "👍"
  }
}
```

---

## 📥 Server → Client Events

### **1. CONNECTED**
Sent when WebSocket connection is established.

```json
{
  "type": "CONNECTED",
  "payload": {
    "userId": 123,
    "tenantId": 456,
    "serverTime": 1704067200000,
    "connectionId": "conn-abc-123"
  }
}
```

---

### **2. NEW_MESSAGE**
Broadcast when a new message is sent to a subscribed channel.

```json
{
  "type": "NEW_MESSAGE",
  "payload": {
    "message": {
      "id": 500,
      "channelId": 1,
      "senderId": 123,
      "content": "Hello team!",
      "messageType": "TEXT",
      "mentions": [100],
      "parentId": null,
      "threadCount": 0,
      "reactions": [],
      "sender": {
        "id": 123,
        "name": "John Doe",
        "avatar": "https://..."
      },
      "createdAt": 1704067200000
    }
  }
}
```

**When to receive:**
- User is subscribed to the channel
- Message sent by another user (not echoed back to sender)

---

### **3. MESSAGE_EDITED**
Broadcast when a message is edited.

```json
{
  "type": "MESSAGE_EDITED",
  "payload": {
    "messageId": 500,
    "channelId": 1,
    "content": "Updated message content",
    "isEdited": true,
    "editedAt": 1704067300000
  }
}
```

---

### **4. MESSAGE_DELETED**
Broadcast when a message is deleted.

```json
{
  "type": "MESSAGE_DELETED",
  "payload": {
    "messageId": 500,
    "channelId": 1,
    "deletedBy": 123,
    "deletedAt": 1704067400000
  }
}
```

---

### **5. REACTION_ADDED**
Broadcast when a reaction is added to a message.

```json
{
  "type": "REACTION_ADDED",
  "payload": {
    "messageId": 500,
    "channelId": 1,
    "emoji": "👍",
    "userId": 100,
    "totalCount": 3,
    "userIds": [100, 101, 102]
  }
}
```

---

### **6. REACTION_REMOVED**
Broadcast when a reaction is removed.

```json
{
  "type": "REACTION_REMOVED",
  "payload": {
    "messageId": 500,
    "channelId": 1,
    "emoji": "👍",
    "userId": 100,
    "totalCount": 2,
    "userIds": [101, 102]
  }
}
```

---

### **7. TYPING_INDICATOR**
Broadcast when a user starts/stops typing.

```json
{
  "type": "TYPING_INDICATOR",
  "payload": {
    "channelId": 1,
    "userId": 100,
    "userName": "John Doe",
    "isTyping": true
  }
}
```

**Note:** Only sent to other channel members (not the typer)

---

### **8. USER_ONLINE**
Broadcast when a user comes online.

```json
{
  "type": "USER_ONLINE",
  "payload": {
    "userId": 100,
    "userName": "John Doe",
    "avatar": "https://...",
    "status": "ONLINE",
    "connectedAt": 1704067200000
  }
}
```

**When to receive:**
- User is in one of your channels
- User just connected

---

### **9. USER_OFFLINE**
Broadcast when a user goes offline.

```json
{
  "type": "USER_OFFLINE",
  "payload": {
    "userId": 100,
    "userName": "John Doe",
    "status": "OFFLINE",
    "lastSeen": 1704067500000
  }
}
```

---

### **10. CHANNEL_UPDATED**
Broadcast when channel details are updated.

```json
{
  "type": "CHANNEL_UPDATED",
  "payload": {
    "channelId": 1,
    "name": "Sales Team Q1 2025",
    "description": "Updated description",
    "updatedBy": 123,
    "updatedAt": 1704067600000
  }
}
```

---

### **11. MEMBER_ADDED**
Broadcast when a user is added to a channel.

```json
{
  "type": "MEMBER_ADDED",
  "payload": {
    "channelId": 1,
    "user": {
      "id": 105,
      "name": "Alice Brown",
      "avatar": "https://..."
    },
    "addedBy": 123,
    "role": "MEMBER",
    "addedAt": 1704067700000
  }
}
```

---

### **12. MEMBER_REMOVED**
Broadcast when a user is removed from a channel.

```json
{
  "type": "MEMBER_REMOVED",
  "payload": {
    "channelId": 1,
    "userId": 105,
    "userName": "Alice Brown",
    "removedBy": 123,
    "removedAt": 1704067800000
  }
}
```

---

### **13. MESSAGE_READ**
Broadcast when someone reads messages (read receipt).

```json
{
  "type": "MESSAGE_READ",
  "payload": {
    "channelId": 1,
    "userId": 100,
    "userName": "John Doe",
    "lastReadMessageId": 500,
    "readAt": 1704067900000
  }
}
```

**Note:** Only for DIRECT channels (1-1 chat). GROUP channels don't broadcast read receipts.

---

### **14. UNREAD_COUNT_UPDATED**
Sent to user when their unread count changes.

```json
{
  "type": "UNREAD_COUNT_UPDATED",
  "payload": {
    "channelId": 1,
    "unreadCount": 5,
    "totalUnread": 12
  }
}
```

---

### **15. ERROR**
Sent when an error occurs.

```json
{
  "type": "ERROR",
  "payload": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "Channel with ID 999 does not exist",
    "details": {}
  },
  "requestId": "abc-123"
}
```

**Error Codes:**
| Code | Description |
|------|-------------|
| INVALID_MESSAGE | Malformed message format |
| CHANNEL_NOT_FOUND | Channel doesn't exist |
| PERMISSION_DENIED | No access to channel |
| NOT_A_MEMBER | User not a channel member |
| RATE_LIMIT_EXCEEDED | Too many messages |
| MESSAGE_TOO_LONG | Content exceeds max length |
| INTERNAL_ERROR | Server error |

---

### **16. CHANNEL_ARCHIVED**
Broadcast when a channel is archived.

```json
{
  "type": "CHANNEL_ARCHIVED",
  "payload": {
    "channelId": 1,
    "archivedBy": 123,
    "archivedAt": 1704068000000
  }
}
```

---

## 🔄 Event Flow Examples

### **Example 1: Sending a Message**

**Client A (sender):**
```json
// 1. Send message
{
  "type": "SEND_MESSAGE",
  "payload": {
    "channelId": 1,
    "content": "Hello!"
  },
  "requestId": "msg-1"
}

// 2. Receive confirmation
{
  "type": "MESSAGE_SENT",
  "payload": {
    "id": 500,
    "channelId": 1,
    "content": "Hello!",
    ...
  },
  "requestId": "msg-1"
}
```

**Client B (recipient):**
```json
// Receive broadcast
{
  "type": "NEW_MESSAGE",
  "payload": {
    "message": {
      "id": 500,
      "channelId": 1,
      "senderId": 123,
      "content": "Hello!",
      ...
    }
  }
}

// Update unread count
{
  "type": "UNREAD_COUNT_UPDATED",
  "payload": {
    "channelId": 1,
    "unreadCount": 1
  }
}
```

---

### **Example 2: Typing Indicator**

**Client A (typing):**
```json
// Start typing
{
  "type": "TYPING_START",
  "payload": {
    "channelId": 1
  }
}

// Every 5 seconds while typing
{
  "type": "TYPING_START",
  "payload": {
    "channelId": 1
  }
}

// Stop typing
{
  "type": "TYPING_STOP",
  "payload": {
    "channelId": 1
  }
}
```

**Client B (watching):**
```json
// User A started typing
{
  "type": "TYPING_INDICATOR",
  "payload": {
    "channelId": 1,
    "userId": 123,
    "userName": "John Doe",
    "isTyping": true
  }
}

// User A stopped typing (auto after 10s or explicit STOP)
{
  "type": "TYPING_INDICATOR",
  "payload": {
    "channelId": 1,
    "userId": 123,
    "isTyping": false
  }
}
```

---

### **Example 3: Presence Updates**

**Client A connects:**
```json
// All users in Client A's channels receive:
{
  "type": "USER_ONLINE",
  "payload": {
    "userId": 123,
    "userName": "John Doe",
    "status": "ONLINE"
  }
}
```

**Client A disconnects:**
```json
// All users in Client A's channels receive:
{
  "type": "USER_OFFLINE",
  "payload": {
    "userId": 123,
    "userName": "John Doe",
    "status": "OFFLINE",
    "lastSeen": 1704067500000
  }
}
```

---

## 🔐 Security

### **Authentication**
- JWT validated on connection
- Token expires → connection closed
- Client must reconnect with new token

### **Authorization**
- Users only receive events for channels they're members of
- Server validates channel membership before broadcasting

### **Rate Limiting**
```
Per connection:
- Max 60 messages/minute
- Max 100 events/second receive
- Exceeded → CONNECTION_THROTTLED event
```

---

## 🧪 Client Implementation Example

### **JavaScript (Browser)**

```javascript
class DiscussWebSocket {
  constructor(token) {
    this.token = token;
    this.ws = null;
    this.handlers = new Map();
    this.requestCallbacks = new Map();
  }

  connect() {
    this.ws = new WebSocket(`ws://localhost:8092/ws/discuss?token=${this.token}`);
    
    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.startHeartbeat();
    };
    
    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.handleMessage(message);
    };
    
    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.reconnect();
    };
    
    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  handleMessage(message) {
    // Handle request-response correlation
    if (message.requestId && this.requestCallbacks.has(message.requestId)) {
      const callback = this.requestCallbacks.get(message.requestId);
      callback(message);
      this.requestCallbacks.delete(message.requestId);
      return;
    }
    
    // Handle events
    const handlers = this.handlers.get(message.type) || [];
    handlers.forEach(handler => handler(message.payload));
  }

  on(eventType, handler) {
    if (!this.handlers.has(eventType)) {
      this.handlers.set(eventType, []);
    }
    this.handlers.get(eventType).push(handler);
  }

  send(type, payload, callback) {
    const requestId = this.generateRequestId();
    const message = {
      type,
      payload,
      requestId,
      timestamp: Date.now()
    };
    
    if (callback) {
      this.requestCallbacks.set(requestId, callback);
    }
    
    this.ws.send(JSON.stringify(message));
  }

  subscribeChannel(channelId) {
    this.send('SUBSCRIBE_CHANNEL', { channelId });
  }

  sendMessage(channelId, content) {
    this.send('SEND_MESSAGE', {
      channelId,
      content,
      messageType: 'TEXT'
    }, (response) => {
      if (response.type === 'MESSAGE_SENT') {
        console.log('Message sent:', response.payload);
      } else if (response.type === 'ERROR') {
        console.error('Error:', response.payload.message);
      }
    });
  }

  startTyping(channelId) {
    this.send('TYPING_START', { channelId });
  }

  stopTyping(channelId) {
    this.send('TYPING_STOP', { channelId });
  }

  startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      this.send('PING', {});
    }, 30000); // 30 seconds
  }

  reconnect() {
    setTimeout(() => {
      console.log('Reconnecting...');
      this.connect();
    }, 5000);
  }

  generateRequestId() {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}

// Usage
const ws = new DiscussWebSocket('your-jwt-token');
ws.connect();

// Listen for new messages
ws.on('NEW_MESSAGE', (message) => {
  console.log('New message:', message);
  // Update UI
});

// Listen for typing indicators
ws.on('TYPING_INDICATOR', ({ userId, userName, isTyping }) => {
  if (isTyping) {
    console.log(`${userName} is typing...`);
  } else {
    console.log(`${userName} stopped typing`);
  }
});

// Send message
ws.sendMessage(1, 'Hello team!');

// Subscribe to channel
ws.subscribeChannel(1);
```

---

## 📊 Performance Considerations

### **Connection Management**
- Max 10,000 concurrent connections per server instance
- Use sticky sessions for WebSocket (consistent routing)
- Redis Pub/Sub for cross-instance broadcasting

### **Message Batching**
Server may batch multiple events:
```json
{
  "type": "BATCH",
  "payload": {
    "events": [
      {
        "type": "NEW_MESSAGE",
        "payload": { ... }
      },
      {
        "type": "UNREAD_COUNT_UPDATED",
        "payload": { ... }
      }
    ]
  }
}
```

### **Compression**
- Enable WebSocket compression (permessage-deflate)
- Reduces bandwidth by ~60% for text messages

---

## 🐛 Debugging

### **Enable Debug Mode**
```json
{
  "type": "DEBUG",
  "payload": {
    "enabled": true,
    "logLevel": "verbose"
  }
}
```

Server responds with debug info:
```json
{
  "type": "DEBUG_INFO",
  "payload": {
    "connectionId": "conn-abc-123",
    "subscribedChannels": [1, 2, 3],
    "messagesSent": 42,
    "messagesReceived": 156,
    "uptime": 3600000
  }
}
```

---

## 📚 Summary

This WebSocket protocol provides:
- ✅ **Bidirectional communication** (client ↔ server)
- ✅ **Real-time events** (messages, typing, presence)
- ✅ **Request-response pattern** (with requestId correlation)
- ✅ **Efficient** (single connection for all channels)
- ✅ **Reliable** (auto-reconnect, heartbeat)
- ✅ **Secure** (JWT auth, authorization checks)
- ✅ **Scalable** (Redis Pub/Sub for multi-instance)
