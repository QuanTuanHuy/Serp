# WebSocket Controller

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Controller Structure

```go
// ui/controller/websocket_controller.go
package controller

import (
    "net/http"
    "strings"
    
    "github.com/gin-gonic/gin"
    "github.com/gorilla/websocket"
    ws "github.com/serp/notification-service/src/infrastructure/websocket"
    "github.com/serp/notification-service/src/kernel/utils"
    "go.uber.org/zap"
)

type WebSocketController struct {
    hub           *ws.Hub
    upgrader      websocket.Upgrader
    jwtUtils      *utils.KeycloakJwksUtils
    logger        *zap.Logger
}

func NewWebSocketController(
    hub *ws.Hub,
    jwtUtils *utils.KeycloakJwksUtils,
    logger *zap.Logger,
) *WebSocketController {
    return &WebSocketController{
        hub:      hub,
        jwtUtils: jwtUtils,
        logger:   logger,
        upgrader: websocket.Upgrader{
            ReadBufferSize:  1024,
            WriteBufferSize: 1024,
            // Allow all origins in dev, restrict in prod
            CheckOrigin: func(r *http.Request) bool {
                return true // Configure properly in production
            },
        },
    }
}
```

---

## 2. WebSocket Upgrade Handler

```go
// HandleWebSocket upgrades HTTP to WebSocket
func (c *WebSocketController) HandleWebSocket(ctx *gin.Context) {
    // Extract and validate JWT
    token := c.extractToken(ctx)
    if token == "" {
        c.logger.Warn("Missing authentication token")
        ctx.JSON(http.StatusUnauthorized, gin.H{
            "code":    401,
            "message": "Missing authentication token",
        })
        return
    }
    
    // Validate token and extract claims
    claims, err := c.jwtUtils.ValidateToken(token)
    if err != nil {
        c.logger.Warn("Invalid token", zap.Error(err))
        ctx.JSON(http.StatusUnauthorized, gin.H{
            "code":    401,
            "message": "Invalid authentication token",
        })
        return
    }
    
    userID := claims.UserID
    tenantID := claims.TenantID
    
    if userID == 0 {
        ctx.JSON(http.StatusUnauthorized, gin.H{
            "code":    401,
            "message": "Invalid user in token",
        })
        return
    }
    
    // Upgrade connection
    conn, err := c.upgrader.Upgrade(ctx.Writer, ctx.Request, nil)
    if err != nil {
        c.logger.Error("Failed to upgrade connection", zap.Error(err))
        return
    }
    
    // Create client
    client := ws.NewClient(
        c.hub,
        conn,
        userID,
        tenantID,
        ctx.GetHeader("User-Agent"),
        ctx.ClientIP(),
        c.logger,
    )
    
    // Register with hub
    c.hub.Register(client)
    
    c.logger.Info("WebSocket client connected",
        zap.Int64("userID", userID),
        zap.Int64("tenantID", tenantID),
        zap.String("clientID", client.ID()),
        zap.String("remoteAddr", ctx.ClientIP()),
    )
    
    // Start client pumps
    go client.WritePump()
    go client.ReadPump()
    
    // Send initial unread count
    c.sendInitialData(client)
}

func (c *WebSocketController) extractToken(ctx *gin.Context) string {
    // Try query parameter first (for WebSocket)
    token := ctx.Query("token")
    if token != "" {
        return token
    }
    
    // Try Authorization header
    authHeader := ctx.GetHeader("Authorization")
    if authHeader != "" {
        parts := strings.SplitN(authHeader, " ", 2)
        if len(parts) == 2 && strings.ToLower(parts[0]) == "bearer" {
            return parts[1]
        }
    }
    
    // Try cookie
    cookie, err := ctx.Cookie("access_token")
    if err == nil && cookie != "" {
        return cookie
    }
    
    return ""
}
```

---

## 3. Initial Data Sender

```go
func (c *WebSocketController) sendInitialData(client *ws.Client) {
    // This runs in a goroutine to not block the connection
    go func() {
        // Get unread count
        unreadCount, err := c.getUnreadCount(client.UserID())
        if err != nil {
            c.logger.Error("Failed to get unread count", zap.Error(err))
            return
        }
        
        // Build message
        builder := ws.NewMessageBuilder()
        msg, err := builder.UnreadCountUpdate(unreadCount)
        if err != nil {
            return
        }
        
        // Send to client
        client.Send(msg)
    }()
}

func (c *WebSocketController) getUnreadCount(userID int64) (*ws.UnreadCountPayload, error) {
    // This should call the notification service
    // Simplified for illustration
    return &ws.UnreadCountPayload{
        TotalUnread: 0,
        ByCategory:  make(map[string]int64),
        HasUrgent:   false,
    }, nil
}
```

---

## 4. Hub Registration Methods

```go
// Add to Hub struct for controller access
func (h *Hub) Register(client *Client) {
    h.register <- client
}

func (h *Hub) Unregister(client *Client) {
    h.unregister <- client
}

// Add to Client for ID access
func (c *Client) ID() string {
    return c.id
}

func (c *Client) UserID() int64 {
    return c.userID
}

func (c *Client) Send(message []byte) {
    select {
    case c.send <- message:
    default:
        // Buffer full
    }
}
```

---

## 5. WebSocket Upgrader Configuration

```go
// config/websocket.go
package config

type WebSocketConfig struct {
    ReadBufferSize    int      `mapstructure:"readBufferSize"`
    WriteBufferSize   int      `mapstructure:"writeBufferSize"`
    AllowedOrigins    []string `mapstructure:"allowedOrigins"`
    MaxMessageSize    int64    `mapstructure:"maxMessageSize"`
    PingPeriodSeconds int      `mapstructure:"pingPeriodSeconds"`
    PongWaitSeconds   int      `mapstructure:"pongWaitSeconds"`
}

func DefaultWebSocketConfig() *WebSocketConfig {
    return &WebSocketConfig{
        ReadBufferSize:    1024,
        WriteBufferSize:   1024,
        AllowedOrigins:    []string{"*"},
        MaxMessageSize:    4096,
        PingPeriodSeconds: 54,
        PongWaitSeconds:   60,
    }
}
```

**application.yml:**
```yaml
websocket:
  readBufferSize: 1024
  writeBufferSize: 1024
  allowedOrigins:
    - "http://localhost:3000"
    - "https://serp.example.com"
  maxMessageSize: 4096
  pingPeriodSeconds: 54
  pongWaitSeconds: 60
```

---

## 6. CORS Configuration for WebSocket

```go
func (c *WebSocketController) checkOrigin(r *http.Request) bool {
    origin := r.Header.Get("Origin")
    if origin == "" {
        return true
    }
    
    // In development, allow all
    if c.config.Env == "development" {
        return true
    }
    
    // Check against allowed origins
    for _, allowed := range c.config.AllowedOrigins {
        if allowed == "*" || allowed == origin {
            return true
        }
    }
    
    c.logger.Warn("Rejected WebSocket origin",
        zap.String("origin", origin),
    )
    return false
}
```

---

## 7. Route Registration

```go
// ui/router/router.go
func RegisterRoutes(
    engine *gin.Engine,
    wsController *controller.WebSocketController,
    // ... other controllers
) {
    // WebSocket endpoint (no middleware - auth handled internally)
    engine.GET("/notification/ws", wsController.HandleWebSocket)
    
    // Alternative path through API
    engine.GET("/notification/api/v1/ws", wsController.HandleWebSocket)
}
```

---

## 8. Health Check Endpoint

```go
// GetWebSocketStatus returns WebSocket hub status
func (c *WebSocketController) GetWebSocketStatus(ctx *gin.Context) {
    metrics := c.hub.GetMetrics()
    
    ctx.JSON(http.StatusOK, gin.H{
        "code":    200,
        "message": "WebSocket hub status",
        "data": gin.H{
            "status":            "running",
            "totalConnections":  metrics.TotalConnections,
            "peakConnections":   metrics.PeakConnections,
            "uniqueUsers":       metrics.UniqueUsers,
            "messagesDelivered": metrics.MessagesDelivered,
        },
    })
}
```
