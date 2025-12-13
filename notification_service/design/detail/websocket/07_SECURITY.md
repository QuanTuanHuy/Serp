# WebSocket Security & Authentication

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Authentication Flow

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │     │  API Gateway │     │ Notification │
│              │     │              │     │   Service    │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       │  1. Login          │                    │
       │───────────────────▶│                    │
       │                    │                    │
       │  2. JWT Token      │                    │
       │◀───────────────────│                    │
       │                    │                    │
       │  3. WS Connect     │                    │
       │  ?token=<jwt>      │                    │
       │───────────────────▶│                    │
       │                    │                    │
       │                    │  4. Forward WS     │
       │                    │  (with JWT)        │
       │                    │───────────────────▶│
       │                    │                    │
       │                    │                    │ 5. Validate JWT
       │                    │                    │    via Keycloak
       │                    │                    │
       │  6. WS Established │                    │
       │◀═══════════════════════════════════════│
       │                    │                    │
```

---

## 2. JWT Validation

```go
// kernel/utils/keycloak_jwks_utils.go
package utils

import (
    "context"
    "crypto/rsa"
    "encoding/base64"
    "encoding/json"
    "fmt"
    "math/big"
    "net/http"
    "sync"
    "time"
    
    "github.com/golang-jwt/jwt/v5"
)

type KeycloakJwksUtils struct {
    jwksURL     string
    keys        map[string]*rsa.PublicKey
    keysMutex   sync.RWMutex
    lastRefresh time.Time
    refreshTTL  time.Duration
    httpClient  *http.Client
}

type JWKSResponse struct {
    Keys []JWK `json:"keys"`
}

type JWK struct {
    Kid string `json:"kid"`
    Kty string `json:"kty"`
    Alg string `json:"alg"`
    N   string `json:"n"`
    E   string `json:"e"`
}

type TokenClaims struct {
    UserID    int64  `json:"userId"`
    TenantID  int64  `json:"tenantId"`
    Email     string `json:"email"`
    Username  string `json:"preferred_username"`
    Roles     []string
    ExpiresAt time.Time
}

func NewKeycloakJwksUtils(keycloakURL, realm string) *KeycloakJwksUtils {
    return &KeycloakJwksUtils{
        jwksURL:    fmt.Sprintf("%s/realms/%s/protocol/openid-connect/certs", keycloakURL, realm),
        keys:       make(map[string]*rsa.PublicKey),
        refreshTTL: 1 * time.Hour,
        httpClient: &http.Client{Timeout: 10 * time.Second},
    }
}

func (k *KeycloakJwksUtils) ValidateToken(tokenString string) (*TokenClaims, error) {
    // Parse token without validation first to get kid
    token, _, err := new(jwt.Parser).ParseUnverified(tokenString, jwt.MapClaims{})
    if err != nil {
        return nil, fmt.Errorf("failed to parse token: %w", err)
    }
    
    // Get key ID from header
    kid, ok := token.Header["kid"].(string)
    if !ok {
        return nil, fmt.Errorf("missing kid in token header")
    }
    
    // Get public key
    pubKey, err := k.getPublicKey(kid)
    if err != nil {
        return nil, err
    }
    
    // Parse and validate token
    claims := jwt.MapClaims{}
    token, err = jwt.ParseWithClaims(tokenString, claims, func(t *jwt.Token) (interface{}, error) {
        if _, ok := t.Method.(*jwt.SigningMethodRSA); !ok {
            return nil, fmt.Errorf("unexpected signing method: %v", t.Header["alg"])
        }
        return pubKey, nil
    })
    
    if err != nil {
        return nil, fmt.Errorf("invalid token: %w", err)
    }
    
    if !token.Valid {
        return nil, fmt.Errorf("token is not valid")
    }
    
    // Extract claims
    return k.extractClaims(claims)
}

func (k *KeycloakJwksUtils) extractClaims(claims jwt.MapClaims) (*TokenClaims, error) {
    result := &TokenClaims{}
    
    // Extract user ID (custom claim)
    if userID, ok := claims["user_id"].(float64); ok {
        result.UserID = int64(userID)
    }
    
    // Extract tenant ID (custom claim)
    if tenantID, ok := claims["tenant_id"].(float64); ok {
        result.TenantID = int64(tenantID)
    }
    
    // Extract standard claims
    if email, ok := claims["email"].(string); ok {
        result.Email = email
    }
    
    if username, ok := claims["preferred_username"].(string); ok {
        result.Username = username
    }
    
    // Extract expiration
    if exp, ok := claims["exp"].(float64); ok {
        result.ExpiresAt = time.Unix(int64(exp), 0)
    }
    
    // Extract roles from realm_access
    if realmAccess, ok := claims["realm_access"].(map[string]interface{}); ok {
        if roles, ok := realmAccess["roles"].([]interface{}); ok {
            for _, role := range roles {
                if roleStr, ok := role.(string); ok {
                    result.Roles = append(result.Roles, roleStr)
                }
            }
        }
    }
    
    return result, nil
}
```

---

## 3. Token Refresh Handling

```go
// Handle token expiration during WebSocket connection
func (c *Client) checkTokenExpiration() {
    if c.tokenExpiresAt.Before(time.Now().Add(5 * time.Minute)) {
        // Token expiring soon, notify client
        msg := WSMessage{
            Type:      "TOKEN_EXPIRING",
            Timestamp: time.Now().UnixMilli(),
            Payload:   mustMarshal(map[string]int64{
                "expiresIn": int64(time.Until(c.tokenExpiresAt).Seconds()),
            }),
        }
        data, _ := json.Marshal(msg)
        c.send <- data
    }
}

// Handle token refresh from client
func (c *Client) handleTokenRefresh(msg *WSMessage) {
    var payload struct {
        Token string `json:"token"`
    }
    
    if err := json.Unmarshal(msg.Payload, &payload); err != nil {
        return
    }
    
    // Validate new token
    claims, err := c.jwtUtils.ValidateToken(payload.Token)
    if err != nil {
        c.sendError("INVALID_TOKEN", "Token refresh failed")
        return
    }
    
    // Verify same user
    if claims.UserID != c.userID {
        c.sendError("USER_MISMATCH", "Token user mismatch")
        return
    }
    
    // Update expiration
    c.tokenExpiresAt = claims.ExpiresAt
    
    // Confirm refresh
    c.sendMessage("TOKEN_REFRESHED", map[string]interface{}{
        "expiresAt": claims.ExpiresAt.UnixMilli(),
    })
}
```

---

## 4. Rate Limiting

```go
// infrastructure/websocket/rate_limiter.go
package websocket

import (
    "sync"
    "time"
)

type RateLimiter struct {
    // Messages per second per client
    messagesPerSecond int
    
    // Track message counts
    counts map[string]*clientCount
    mutex  sync.Mutex
}

type clientCount struct {
    count     int
    resetTime time.Time
}

func NewRateLimiter(messagesPerSecond int) *RateLimiter {
    return &RateLimiter{
        messagesPerSecond: messagesPerSecond,
        counts:            make(map[string]*clientCount),
    }
}

func (r *RateLimiter) Allow(clientID string) bool {
    r.mutex.Lock()
    defer r.mutex.Unlock()
    
    now := time.Now()
    
    c, exists := r.counts[clientID]
    if !exists || now.After(c.resetTime) {
        // New window
        r.counts[clientID] = &clientCount{
            count:     1,
            resetTime: now.Add(time.Second),
        }
        return true
    }
    
    if c.count >= r.messagesPerSecond {
        return false
    }
    
    c.count++
    return true
}

// Cleanup old entries periodically
func (r *RateLimiter) Cleanup() {
    r.mutex.Lock()
    defer r.mutex.Unlock()
    
    now := time.Now()
    for id, c := range r.counts {
        if now.After(c.resetTime.Add(time.Minute)) {
            delete(r.counts, id)
        }
    }
}
```

**Usage in Client:**
```go
func (c *Client) handleMessage(message []byte) {
    // Rate limit check
    if !c.rateLimiter.Allow(c.id) {
        c.sendError(ErrCodeRateLimited, "Too many messages")
        return
    }
    
    // Process message...
}
```

---

## 5. Connection Limits

```go
// Limit connections per user
const maxConnectionsPerUser = 5

func (h *Hub) registerClient(client *Client) {
    h.mutex.Lock()
    defer h.mutex.Unlock()
    
    // Check connection limit
    if clients, ok := h.clients[client.userID]; ok {
        if len(clients) >= maxConnectionsPerUser {
            // Close oldest connection
            var oldest *Client
            var oldestTime time.Time
            
            for c := range clients {
                if oldest == nil || c.connectedAt.Before(oldestTime) {
                    oldest = c
                    oldestTime = c.connectedAt
                }
            }
            
            if oldest != nil {
                h.logger.Info("Closing oldest connection due to limit",
                    zap.Int64("userID", client.userID),
                    zap.String("closedClientID", oldest.id),
                )
                
                // Notify client before closing
                oldest.sendMessage("CONNECTION_REPLACED", map[string]string{
                    "reason": "New connection from another device",
                })
                
                delete(clients, oldest)
                close(oldest.send)
            }
        }
    }
    
    // Register new client
    if h.clients[client.userID] == nil {
        h.clients[client.userID] = make(map[*Client]bool)
    }
    h.clients[client.userID][client] = true
}
```

---

## 6. Message Validation

```go
// Validate incoming messages
func (c *Client) validateMessage(msg *WSMessage) error {
    // Check message type
    validTypes := map[string]bool{
        "PING":        true,
        "ACK":         true,
        "SUBSCRIBE":   true,
        "UNSUBSCRIBE": true,
    }
    
    if !validTypes[msg.Type] {
        return fmt.Errorf("invalid message type: %s", msg.Type)
    }
    
    // Check timestamp (prevent replay attacks)
    msgTime := time.UnixMilli(msg.Timestamp)
    if time.Since(msgTime) > 5*time.Minute {
        return fmt.Errorf("message too old")
    }
    
    // Validate payload size
    if len(msg.Payload) > maxMessageSize {
        return fmt.Errorf("payload too large")
    }
    
    return nil
}
```

---

## 7. Security Headers

```go
// Configure WebSocket upgrader with security checks
func (c *WebSocketController) setupUpgrader() {
    c.upgrader = websocket.Upgrader{
        ReadBufferSize:  1024,
        WriteBufferSize: 1024,
        
        // Origin check
        CheckOrigin: func(r *http.Request) bool {
            origin := r.Header.Get("Origin")
            
            // Allow configured origins only
            for _, allowed := range c.config.AllowedOrigins {
                if allowed == origin {
                    return true
                }
            }
            
            c.logger.Warn("Rejected origin", zap.String("origin", origin))
            return false
        },
        
        // Subprotocol selection
        Subprotocols: []string{"notification.serp.v1"},
        
        // Custom error handler
        Error: func(w http.ResponseWriter, r *http.Request, status int, reason error) {
            c.logger.Error("WebSocket upgrade error",
                zap.Int("status", status),
                zap.Error(reason),
            )
        },
    }
}
```

---

## 8. Audit Logging

```go
// Log security-relevant events
type WSSecurityLog struct {
    Timestamp  time.Time `json:"timestamp"`
    Event      string    `json:"event"`
    ClientID   string    `json:"clientId"`
    UserID     int64     `json:"userId"`
    RemoteAddr string    `json:"remoteAddr"`
    UserAgent  string    `json:"userAgent"`
    Details    string    `json:"details,omitempty"`
}

func (h *Hub) logSecurityEvent(event string, client *Client, details string) {
    log := WSSecurityLog{
        Timestamp:  time.Now(),
        Event:      event,
        ClientID:   client.id,
        UserID:     client.userID,
        RemoteAddr: client.remoteAddr,
        UserAgent:  client.userAgent,
        Details:    details,
    }
    
    h.logger.Info("WebSocket security event",
        zap.String("event", log.Event),
        zap.Int64("userID", log.UserID),
        zap.String("details", log.Details),
    )
    
    // Send to logging_tracker service via Kafka
    // h.kafkaProducer.SendSecurityLog(log)
}
```

**Security Events:**
- `CONNECTION_ESTABLISHED`
- `CONNECTION_CLOSED`
- `AUTHENTICATION_FAILED`
- `RATE_LIMITED`
- `INVALID_MESSAGE`
- `CONNECTION_REPLACED`
- `TOKEN_REFRESHED`
