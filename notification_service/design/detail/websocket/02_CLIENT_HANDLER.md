# WebSocket Client Handler

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Client Structure

```go
// infrastructure/websocket/client.go
package websocket

import (
    "encoding/json"
    "sync"
    "time"
    
    "github.com/google/uuid"
    "github.com/gorilla/websocket"
    "go.uber.org/zap"
)

// Connection timeouts
const (
    // Time allowed to write a message
    writeWait = 10 * time.Second
    
    // Time allowed to read pong from client
    pongWait = 60 * time.Second
    
    // Ping interval (must be less than pongWait)
    pingPeriod = (pongWait * 9) / 10  // 54 seconds
    
    // Maximum message size
    maxMessageSize = 4096  // 4KB
    
    // Send buffer size
    sendBufferSize = 256
)

type Client struct {
    // Unique client ID
    id string
    
    // Reference to hub
    hub *Hub
    
    // WebSocket connection
    conn *websocket.Conn
    
    // User information (from JWT)
    userID   int64
    tenantID int64
    
    // Outbound message buffer
    send chan []byte
    
    // Category subscriptions
    subscriptions map[string]bool
    subMutex      sync.RWMutex
    
    // Connection metadata
    connectedAt time.Time
    lastPingAt  time.Time
    userAgent   string
    remoteAddr  string
    
    // Logger
    logger *zap.Logger
}
```

---

## 2. Client Constructor

```go
func NewClient(
    hub *Hub,
    conn *websocket.Conn,
    userID, tenantID int64,
    userAgent, remoteAddr string,
    logger *zap.Logger,
) *Client {
    return &Client{
        id:            uuid.NewString(),
        hub:           hub,
        conn:          conn,
        userID:        userID,
        tenantID:      tenantID,
        send:          make(chan []byte, sendBufferSize),
        subscriptions: make(map[string]bool),
        connectedAt:   time.Now(),
        userAgent:     userAgent,
        remoteAddr:    remoteAddr,
        logger:        logger,
    }
}
```

---

## 3. Read Pump (Client → Server)

```go
// ReadPump handles incoming messages from client
// Runs in its own goroutine per client
func (c *Client) ReadPump() {
    defer func() {
        c.hub.unregister <- c
        c.conn.Close()
        c.logger.Info("ReadPump closed",
            zap.String("clientID", c.id),
            zap.Int64("userID", c.userID),
        )
    }()
    
    // Configure connection
    c.conn.SetReadLimit(maxMessageSize)
    c.conn.SetReadDeadline(time.Now().Add(pongWait))
    
    // Handle pong messages
    c.conn.SetPongHandler(func(appData string) error {
        c.conn.SetReadDeadline(time.Now().Add(pongWait))
        c.lastPingAt = time.Now()
        return nil
    })
    
    for {
        messageType, message, err := c.conn.ReadMessage()
        if err != nil {
            if websocket.IsUnexpectedCloseError(err,
                websocket.CloseGoingAway,
                websocket.CloseAbnormalClosure,
                websocket.CloseNormalClosure,
            ) {
                c.logger.Error("Unexpected close error",
                    zap.Error(err),
                    zap.String("clientID", c.id),
                )
            }
            break
        }
        
        // Only handle text messages
        if messageType == websocket.TextMessage {
            c.handleMessage(message)
        }
    }
}

func (c *Client) handleMessage(message []byte) {
    var msg WSMessage
    if err := json.Unmarshal(message, &msg); err != nil {
        c.logger.Warn("Invalid message format",
            zap.Error(err),
            zap.String("clientID", c.id),
        )
        return
    }
    
    switch msg.Type {
    case "PING":
        c.handlePing()
        
    case "ACK":
        c.handleAck(&msg)
        
    case "SUBSCRIBE":
        c.handleSubscribe(&msg)
        
    case "UNSUBSCRIBE":
        c.handleUnsubscribe(&msg)
        
    default:
        c.logger.Warn("Unknown message type",
            zap.String("type", msg.Type),
            zap.String("clientID", c.id),
        )
    }
}
```

---

## 4. Write Pump (Server → Client)

```go
// WritePump handles outgoing messages to client
// Runs in its own goroutine per client
func (c *Client) WritePump() {
    ticker := time.NewTicker(pingPeriod)
    
    defer func() {
        ticker.Stop()
        c.conn.Close()
        c.logger.Info("WritePump closed",
            zap.String("clientID", c.id),
            zap.Int64("userID", c.userID),
        )
    }()
    
    for {
        select {
        case message, ok := <-c.send:
            c.conn.SetWriteDeadline(time.Now().Add(writeWait))
            
            if !ok {
                // Hub closed the channel
                c.conn.WriteMessage(websocket.CloseMessage, []byte{})
                return
            }
            
            // Write message
            if err := c.conn.WriteMessage(websocket.TextMessage, message); err != nil {
                c.logger.Error("Write error",
                    zap.Error(err),
                    zap.String("clientID", c.id),
                )
                return
            }
            
            // Batch pending messages if available
            n := len(c.send)
            for i := 0; i < n; i++ {
                if err := c.conn.WriteMessage(websocket.TextMessage, <-c.send); err != nil {
                    return
                }
            }
            
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

## 5. Message Handlers

```go
func (c *Client) handlePing() {
    response := WSMessage{
        Type:      "PONG",
        Timestamp: time.Now().UnixMilli(),
        MessageID: uuid.NewString(),
    }
    
    data, _ := json.Marshal(response)
    
    select {
    case c.send <- data:
    default:
        c.logger.Warn("Send buffer full on PONG",
            zap.String("clientID", c.id),
        )
    }
}

func (c *Client) handleAck(msg *WSMessage) {
    // Handle message acknowledgment
    // Can be used for delivery confirmation
    var payload AckPayload
    if err := json.Unmarshal(msg.Payload, &payload); err != nil {
        return
    }
    
    c.logger.Debug("Message acknowledged",
        zap.String("messageID", payload.MessageID),
        zap.String("clientID", c.id),
    )
}

func (c *Client) handleSubscribe(msg *WSMessage) {
    var payload SubscribePayload
    if err := json.Unmarshal(msg.Payload, &payload); err != nil {
        return
    }
    
    c.subMutex.Lock()
    for _, category := range payload.Categories {
        c.subscriptions[category] = true
    }
    c.subMutex.Unlock()
    
    c.logger.Info("Client subscribed",
        zap.Strings("categories", payload.Categories),
        zap.String("clientID", c.id),
    )
    
    // Send confirmation
    c.sendSubscriptionConfirmation(payload.Categories, true)
}

func (c *Client) handleUnsubscribe(msg *WSMessage) {
    var payload SubscribePayload
    if err := json.Unmarshal(msg.Payload, &payload); err != nil {
        return
    }
    
    c.subMutex.Lock()
    for _, category := range payload.Categories {
        delete(c.subscriptions, category)
    }
    c.subMutex.Unlock()
    
    c.logger.Info("Client unsubscribed",
        zap.Strings("categories", payload.Categories),
        zap.String("clientID", c.id),
    )
    
    c.sendSubscriptionConfirmation(payload.Categories, false)
}

func (c *Client) sendSubscriptionConfirmation(categories []string, subscribed bool) {
    action := "SUBSCRIBED"
    if !subscribed {
        action = "UNSUBSCRIBED"
    }
    
    response := WSMessage{
        Type:      action,
        Timestamp: time.Now().UnixMilli(),
        MessageID: uuid.NewString(),
        Payload:   mustMarshal(SubscribePayload{Categories: categories}),
    }
    
    data, _ := json.Marshal(response)
    c.send <- data
}
```

---

## 6. Subscription Methods

```go
// IsSubscribed checks if client subscribed to category
func (c *Client) IsSubscribed(category string) bool {
    c.subMutex.RLock()
    defer c.subMutex.RUnlock()
    
    // Empty subscriptions means subscribed to all
    if len(c.subscriptions) == 0 {
        return true
    }
    
    return c.subscriptions[category]
}

// GetSubscriptions returns all subscriptions
func (c *Client) GetSubscriptions() []string {
    c.subMutex.RLock()
    defer c.subMutex.RUnlock()
    
    categories := make([]string, 0, len(c.subscriptions))
    for cat := range c.subscriptions {
        categories = append(categories, cat)
    }
    return categories
}
```

---

## 7. Client Info Methods

```go
// GetInfo returns client connection info
func (c *Client) GetInfo() ClientInfo {
    return ClientInfo{
        ID:            c.id,
        UserID:        c.userID,
        TenantID:      c.tenantID,
        ConnectedAt:   c.connectedAt,
        LastPingAt:    c.lastPingAt,
        UserAgent:     c.userAgent,
        RemoteAddr:    c.remoteAddr,
        Subscriptions: c.GetSubscriptions(),
    }
}

type ClientInfo struct {
    ID            string    `json:"id"`
    UserID        int64     `json:"userId"`
    TenantID      int64     `json:"tenantId"`
    ConnectedAt   time.Time `json:"connectedAt"`
    LastPingAt    time.Time `json:"lastPingAt"`
    UserAgent     string    `json:"userAgent"`
    RemoteAddr    string    `json:"remoteAddr"`
    Subscriptions []string  `json:"subscriptions"`
}

// ConnectionDuration returns how long client has been connected
func (c *Client) ConnectionDuration() time.Duration {
    return time.Since(c.connectedAt)
}
```

---

## 8. Payload Types

```go
type AckPayload struct {
    MessageID string `json:"messageId"`
}

type SubscribePayload struct {
    Categories []string `json:"categories"`
}

func mustMarshal(v interface{}) json.RawMessage {
    data, _ := json.Marshal(v)
    return data
}
```
