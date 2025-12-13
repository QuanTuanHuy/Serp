# WebSocket Hub Architecture

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Hub Overview

Hub là central component quản lý tất cả WebSocket connections.

```
                    ┌─────────────────────────────────────────┐
                    │              WebSocket Hub               │
                    │                                         │
                    │  ┌─────────────────────────────────────┐│
                    │  │     clients map[userID][]*Client    ││
                    │  │                                     ││
                    │  │  User 1 ──► [Device A, Device B]    ││
                    │  │  User 2 ──► [Device C]              ││
                    │  │  User 3 ──► [Device D, Device E, F] ││
                    │  └─────────────────────────────────────┘│
                    │                                         │
                    │  Channels:                              │
                    │  ├── register   chan *Client            │
                    │  ├── unregister chan *Client            │
                    │  └── broadcast  chan *BroadcastMessage  │
                    └─────────────────────────────────────────┘
```

---

## 2. Hub Structure

```go
// infrastructure/websocket/hub.go
package websocket

import (
    "sync"
    "time"
    
    "go.uber.org/zap"
)

type Hub struct {
    // Client connections: userID -> set of clients
    // Supports multiple devices per user
    clients map[int64]map[*Client]bool
    
    // Tenant grouping for broadcast
    tenants map[int64]map[*Client]bool
    
    // Channel operations
    register   chan *Client
    unregister chan *Client
    broadcast  chan *BroadcastMessage
    
    // Thread safety
    mutex sync.RWMutex
    
    // Dependencies
    logger *zap.Logger
    
    // Metrics
    totalConnections   int64
    peakConnections    int64
    messagesDelivered  int64
}

type BroadcastMessage struct {
    TargetType string      // "user", "tenant", "all"
    TargetID   int64       // userID or tenantID
    Message    []byte
    Category   string      // Optional: filter by category subscription
}
```

---

## 3. Hub Constructor

```go
func NewHub(logger *zap.Logger) *Hub {
    return &Hub{
        clients:    make(map[int64]map[*Client]bool),
        tenants:    make(map[int64]map[*Client]bool),
        register:   make(chan *Client, 256),
        unregister: make(chan *Client, 256),
        broadcast:  make(chan *BroadcastMessage, 1024),
        logger:     logger,
    }
}
```

---

## 4. Hub Run Loop

```go
func (h *Hub) Run() {
    h.logger.Info("WebSocket Hub started")
    
    for {
        select {
        case client := <-h.register:
            h.registerClient(client)
            
        case client := <-h.unregister:
            h.unregisterClient(client)
            
        case msg := <-h.broadcast:
            h.handleBroadcast(msg)
        }
    }
}

func (h *Hub) registerClient(client *Client) {
    h.mutex.Lock()
    defer h.mutex.Unlock()
    
    // Add to user's client set
    if h.clients[client.userID] == nil {
        h.clients[client.userID] = make(map[*Client]bool)
    }
    h.clients[client.userID][client] = true
    
    // Add to tenant's client set
    if h.tenants[client.tenantID] == nil {
        h.tenants[client.tenantID] = make(map[*Client]bool)
    }
    h.tenants[client.tenantID][client] = true
    
    // Update metrics
    h.totalConnections++
    if h.totalConnections > h.peakConnections {
        h.peakConnections = h.totalConnections
    }
    
    h.logger.Info("Client registered",
        zap.Int64("userID", client.userID),
        zap.Int64("tenantID", client.tenantID),
        zap.String("clientID", client.id),
        zap.Int64("totalConnections", h.totalConnections),
    )
}

func (h *Hub) unregisterClient(client *Client) {
    h.mutex.Lock()
    defer h.mutex.Unlock()
    
    // Remove from user's client set
    if clients, ok := h.clients[client.userID]; ok {
        if _, exists := clients[client]; exists {
            delete(clients, client)
            close(client.send)
            
            // Clean up empty user entry
            if len(clients) == 0 {
                delete(h.clients, client.userID)
            }
        }
    }
    
    // Remove from tenant's client set
    if clients, ok := h.tenants[client.tenantID]; ok {
        delete(clients, client)
        if len(clients) == 0 {
            delete(h.tenants, client.tenantID)
        }
    }
    
    h.totalConnections--
    
    h.logger.Info("Client unregistered",
        zap.Int64("userID", client.userID),
        zap.String("clientID", client.id),
    )
}
```

---

## 5. Broadcast Methods

```go
func (h *Hub) handleBroadcast(msg *BroadcastMessage) {
    switch msg.TargetType {
    case "user":
        h.sendToUser(msg.TargetID, msg.Message, msg.Category)
    case "tenant":
        h.sendToTenant(msg.TargetID, msg.Message, msg.Category)
    case "all":
        h.sendToAll(msg.Message)
    }
}

func (h *Hub) sendToUser(userID int64, message []byte, category string) {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    clients, ok := h.clients[userID]
    if !ok {
        return
    }
    
    for client := range clients {
        // Check category subscription if specified
        if category != "" && !client.IsSubscribed(category) {
            continue
        }
        
        select {
        case client.send <- message:
            h.messagesDelivered++
        default:
            // Client buffer full, mark for cleanup
            h.logger.Warn("Client buffer full",
                zap.Int64("userID", userID),
                zap.String("clientID", client.id),
            )
        }
    }
}

func (h *Hub) sendToTenant(tenantID int64, message []byte, category string) {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    clients, ok := h.tenants[tenantID]
    if !ok {
        return
    }
    
    for client := range clients {
        if category != "" && !client.IsSubscribed(category) {
            continue
        }
        
        select {
        case client.send <- message:
            h.messagesDelivered++
        default:
            // Skip slow clients
        }
    }
}

func (h *Hub) sendToAll(message []byte) {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    for _, clients := range h.clients {
        for client := range clients {
            select {
            case client.send <- message:
                h.messagesDelivered++
            default:
                // Skip slow clients
            }
        }
    }
}
```

---

## 6. Public API Methods

```go
// SendToUser sends message to all devices of a user
func (h *Hub) SendToUser(userID int64, message []byte) error {
    h.broadcast <- &BroadcastMessage{
        TargetType: "user",
        TargetID:   userID,
        Message:    message,
    }
    return nil
}

// SendToUserWithCategory sends to user with category filter
func (h *Hub) SendToUserWithCategory(userID int64, message []byte, category string) error {
    h.broadcast <- &BroadcastMessage{
        TargetType: "user",
        TargetID:   userID,
        Message:    message,
        Category:   category,
    }
    return nil
}

// BroadcastToTenant sends to all users in a tenant
func (h *Hub) BroadcastToTenant(tenantID int64, message []byte) error {
    h.broadcast <- &BroadcastMessage{
        TargetType: "tenant",
        TargetID:   tenantID,
        Message:    message,
    }
    return nil
}

// BroadcastToAll sends to all connected clients
func (h *Hub) BroadcastToAll(message []byte) error {
    h.broadcast <- &BroadcastMessage{
        TargetType: "all",
        Message:    message,
    }
    return nil
}

// IsUserConnected checks if user has active connections
func (h *Hub) IsUserConnected(userID int64) bool {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    clients, ok := h.clients[userID]
    return ok && len(clients) > 0
}

// GetConnectedUserCount returns number of connected users
func (h *Hub) GetConnectedUserCount() int {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    return len(h.clients)
}

// GetTotalConnections returns total active connections
func (h *Hub) GetTotalConnections() int64 {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    return h.totalConnections
}

// GetMetrics returns hub metrics
func (h *Hub) GetMetrics() HubMetrics {
    h.mutex.RLock()
    defer h.mutex.RUnlock()
    
    return HubMetrics{
        TotalConnections:  h.totalConnections,
        PeakConnections:   h.peakConnections,
        UniqueUsers:       int64(len(h.clients)),
        UniqueTenants:     int64(len(h.tenants)),
        MessagesDelivered: h.messagesDelivered,
    }
}

type HubMetrics struct {
    TotalConnections  int64 `json:"totalConnections"`
    PeakConnections   int64 `json:"peakConnections"`
    UniqueUsers       int64 `json:"uniqueUsers"`
    UniqueTenants     int64 `json:"uniqueTenants"`
    MessagesDelivered int64 `json:"messagesDelivered"`
}
```

---

## 7. Hub Port Interface

```go
// core/port/client/websocket_hub_port.go
package client

type IWebSocketHubPort interface {
    // Send to specific user (all devices)
    SendToUser(userID int64, message []byte) error
    
    // Send with category filter
    SendToUserWithCategory(userID int64, message []byte, category string) error
    
    // Broadcast to tenant
    BroadcastToTenant(tenantID int64, message []byte) error
    
    // Broadcast to all
    BroadcastToAll(message []byte) error
    
    // Connection status
    IsUserConnected(userID int64) bool
    GetConnectedUserCount() int
    GetTotalConnections() int64
    
    // Metrics
    GetMetrics() HubMetrics
}
```
