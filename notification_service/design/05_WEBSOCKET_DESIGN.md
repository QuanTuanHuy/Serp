# WebSocket Design

**Module:** notification_service  
**Ngày tạo:** 2025-12-13

---

## 1. WebSocket Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Frontend (serp_web)                    │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              WebSocket Client (useNotificationWS)        │ │
│  │                                                          │ │
│  │  - Auto reconnect with exponential backoff               │ │
│  │  - Heartbeat ping/pong                                   │ │
│  │  - Message queue during disconnect                       │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────┬───────────────────────┘
                                       │
                                       │ wss://
                                       ▼
┌──────────────────────────────────────────────────────────────┐
│                 Notification Service                          │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                     WebSocket Hub                        │ │
│  │                                                          │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │ Connection  │  │  Broadcast  │  │   Message   │      │ │
│  │  │  Manager    │  │   Manager   │  │   Router    │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  │                                                          │ │
│  │  clients map[userID][]*WebSocketClient                   │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  Kafka Consumer                          │ │
│  │              (Receives notification events)              │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. WebSocket Endpoint

```
ws(s)://gateway.serp.com/notification/ws?token=<jwt_token>
```

Hoặc qua header:
```
Authorization: Bearer <jwt_token>
```

---

## 3. Message Protocol

### 3.1. Message Format

```go
type WSMessage struct {
    Type      string          `json:"type"`
    Payload   json.RawMessage `json:"payload"`
    Timestamp int64           `json:"timestamp"`
    MessageID string          `json:"messageId"`
}
```

### 3.2. Server → Client Messages

**NEW_NOTIFICATION:**
```json
{
  "type": "NEW_NOTIFICATION",
  "payload": {
    "id": 123,
    "title": "Task Assigned",
    "message": "You have been assigned task: Setup API",
    "type": "INFO",
    "category": "TASK",
    "priority": "HIGH",
    "actionUrl": "/ptm/tasks/123",
    "createdAt": 1702454400000
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-123"
}
```

**UNREAD_COUNT_UPDATE:**
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
    }
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-124"
}
```

**NOTIFICATION_READ:**
```json
{
  "type": "NOTIFICATION_READ",
  "payload": {
    "notificationIds": [1, 2, 3],
    "remainingUnread": 12
  },
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-125"
}
```

**PONG:**
```json
{
  "type": "PONG",
  "payload": {},
  "timestamp": 1702454400000,
  "messageId": "msg-uuid-126"
}
```

### 3.3. Client → Server Messages

**PING:**
```json
{
  "type": "PING",
  "payload": {},
  "timestamp": 1702454400000
}
```

**ACK:**
```json
{
  "type": "ACK",
  "payload": {
    "messageId": "msg-uuid-123"
  },
  "timestamp": 1702454400000
}
```

**SUBSCRIBE:**
```json
{
  "type": "SUBSCRIBE",
  "payload": {
    "categories": ["TASK", "CRM"]
  },
  "timestamp": 1702454400000
}
```

---

## 4. Hub Implementation

### 4.1. WebSocket Hub

```go
// infrastructure/websocket/hub.go
package websocket

import (
    "sync"
    "github.com/gorilla/websocket"
)

type Hub struct {
    // Map userID -> connections (multi-device support)
    clients    map[int64]map[*Client]bool
    
    // Channels
    broadcast  chan *BroadcastMessage
    register   chan *Client
    unregister chan *Client
    
    mutex sync.RWMutex
}

type Client struct {
    hub      *Hub
    conn     *websocket.Conn
    userID   int64
    tenantID int64
    send     chan []byte
    
    // Subscriptions
    categories map[string]bool
}

type BroadcastMessage struct {
    UserID  int64
    Message []byte
}

func NewHub() *Hub {
    return &Hub{
        clients:    make(map[int64]map[*Client]bool),
        broadcast:  make(chan *BroadcastMessage, 256),
        register:   make(chan *Client),
        unregister: make(chan *Client),
    }
}

func (h *Hub) Run() {
    for {
        select {
        case client := <-h.register:
            h.registerClient(client)
            
        case client := <-h.unregister:
            h.unregisterClient(client)
            
        case msg := <-h.broadcast:
            h.broadcastToUser(msg.UserID, msg.Message)
        }
    }
}

func (h *Hub) registerClient(client *Client) {
    h.mutex.Lock()
    defer h.mutex.Unlock()
    
    if h.clients[client.userID] == nil {
        h.clients[client.userID] = make(map[*Client]bool)
    }
    h.clients[client.userID][client] = true
}

func (h *Hub) broadcastToUser(userID int64, message []byte) {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    if clients, ok := h.clients[userID]; ok {
        for client := range clients {
            select {
            case client.send <- message:
            default:
                close(client.send)
                delete(clients, client)
            }
        }
    }
}

func (h *Hub) SendToUser(userID int64, message []byte) {
    h.broadcast <- &BroadcastMessage{
        UserID:  userID,
        Message: message,
    }
}
```

### 4.2. Client Handler

```go
// infrastructure/websocket/client.go
package websocket

import (
    "time"
    "github.com/gorilla/websocket"
)

const (
    writeWait      = 10 * time.Second
    pongWait       = 60 * time.Second
    pingPeriod     = (pongWait * 9) / 10
    maxMessageSize = 4096
)

func (c *Client) ReadPump() {
    defer func() {
        c.hub.unregister <- c
        c.conn.Close()
    }()
    
    c.conn.SetReadLimit(maxMessageSize)
    c.conn.SetReadDeadline(time.Now().Add(pongWait))
    c.conn.SetPongHandler(func(string) error {
        c.conn.SetReadDeadline(time.Now().Add(pongWait))
        return nil
    })
    
    for {
        _, message, err := c.conn.ReadMessage()
        if err != nil {
            break
        }
        c.handleMessage(message)
    }
}

func (c *Client) WritePump() {
    ticker := time.NewTicker(pingPeriod)
    defer func() {
        ticker.Stop()
        c.conn.Close()
    }()
    
    for {
        select {
        case message, ok := <-c.send:
            c.conn.SetWriteDeadline(time.Now().Add(writeWait))
            if !ok {
                c.conn.WriteMessage(websocket.CloseMessage, []byte{})
                return
            }
            c.conn.WriteMessage(websocket.TextMessage, message)
            
        case <-ticker.C:
            c.conn.SetWriteDeadline(time.Now().Add(writeWait))
            if err := c.conn.WriteMessage(websocket.PingMessage, nil); err != nil {
                return
            }
        }
    }
}
```

---

## 5. Scaling với Redis Pub/Sub

Để hỗ trợ multiple instances:

```go
// infrastructure/websocket/redis_broadcaster.go
package websocket

type RedisBroadcaster struct {
    redis  *redis.Client
    hub    *Hub
    pubsub *redis.PubSub
}

func (r *RedisBroadcaster) Publish(userID int64, message []byte) error {
    channel := fmt.Sprintf("notification:%d", userID)
    return r.redis.Publish(ctx, channel, message).Err()
}

func (r *RedisBroadcaster) Subscribe() {
    for msg := range r.pubsub.Channel() {
        userID := extractUserID(msg.Channel)
        r.hub.SendToUser(userID, []byte(msg.Payload))
    }
}
```

```
┌─────────────────┐     ┌─────────────────┐
│  Instance 1     │     │  Instance 2     │
│  (User A conn)  │     │  (User A conn)  │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │    Redis Pub/Sub      │
         └───────────┬───────────┘
                     │
              ┌──────┴──────┐
              │    Redis    │
              │  Channel:   │
              │ notification│
              └─────────────┘
```

---

## 6. Frontend Integration

```typescript
// serp_web/src/shared/hooks/useNotificationWS.ts
export function useNotificationWS() {
  const [connected, setConnected] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  
  useEffect(() => {
    const token = getAccessToken();
    const ws = new WebSocket(
      `${WS_URL}/notification/ws?token=${token}`
    );
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      
      switch (message.type) {
        case 'NEW_NOTIFICATION':
          dispatch(addNotification(message.payload));
          toast.info(message.payload.title);
          break;
        case 'UNREAD_COUNT_UPDATE':
          setUnreadCount(message.payload.totalUnread);
          break;
      }
    };
    
    // Reconnect logic...
  }, []);
  
  return { connected, unreadCount };
}
```
