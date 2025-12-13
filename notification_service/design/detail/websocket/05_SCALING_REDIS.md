# WebSocket Scaling với Redis Pub/Sub

**Module:** notification_service/websocket  
**Ngày tạo:** 2025-12-13

---

## 1. Scaling Problem

Khi chạy nhiều instances của notification service, mỗi instance có Hub riêng. Cần đảm bảo message được gửi đến đúng user dù họ connect vào instance nào.

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Instance 1    │     │   Instance 2    │     │   Instance 3    │
│                 │     │                 │     │                 │
│  Hub:           │     │  Hub:           │     │  Hub:           │
│  User A ──────┐ │     │  User A ──────┐ │     │  User B ───┐    │
│  User C       │ │     │  User D       │ │     │  User E    │    │
│               │ │     │               │ │     │            │    │
└───────────────┼─┘     └───────────────┼─┘     └────────────┼────┘
                │                       │                    │
                └───────────────────────┴────────────────────┘
                                    │
                                    ▼
                          Message for User A
                           (which instance?)
```

---

## 2. Solution: Redis Pub/Sub

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Instance 1    │     │   Instance 2    │     │   Instance 3    │
│                 │     │                 │     │                 │
│  Hub + Redis    │     │  Hub + Redis    │     │  Hub + Redis    │
│  Subscriber     │     │  Subscriber     │     │  Subscriber     │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │      SUBSCRIBE        │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │         Redis           │
                    │                         │
                    │  Channel: notification  │
                    │  Channel: notification:1│
                    │  Channel: notification:2│
                    └─────────────────────────┘
```

---

## 3. Redis Broadcaster Implementation

```go
// infrastructure/websocket/redis_broadcaster.go
package websocket

import (
    "context"
    "encoding/json"
    "fmt"
    "strconv"
    "strings"
    "sync"
    
    "github.com/redis/go-redis/v9"
    "go.uber.org/zap"
)

const (
    // Channel patterns
    channelPrefix       = "notification:"
    channelAll          = "notification:broadcast"
    channelTenantPrefix = "notification:tenant:"
)

type RedisBroadcaster struct {
    redis  *redis.Client
    hub    *Hub
    pubsub *redis.PubSub
    logger *zap.Logger
    
    ctx    context.Context
    cancel context.CancelFunc
    wg     sync.WaitGroup
}

func NewRedisBroadcaster(
    redisClient *redis.Client,
    hub *Hub,
    logger *zap.Logger,
) *RedisBroadcaster {
    ctx, cancel := context.WithCancel(context.Background())
    
    return &RedisBroadcaster{
        redis:  redisClient,
        hub:    hub,
        logger: logger,
        ctx:    ctx,
        cancel: cancel,
    }
}
```

---

## 4. Start/Stop Methods

```go
func (r *RedisBroadcaster) Start() error {
    // Subscribe to patterns
    r.pubsub = r.redis.PSubscribe(r.ctx,
        channelPrefix+"*",      // All user channels
        channelAll,             // Broadcast channel
        channelTenantPrefix+"*", // Tenant channels
    )
    
    // Wait for confirmation
    _, err := r.pubsub.Receive(r.ctx)
    if err != nil {
        return fmt.Errorf("failed to subscribe: %w", err)
    }
    
    r.logger.Info("Redis broadcaster started")
    
    // Start message handler
    r.wg.Add(1)
    go r.handleMessages()
    
    return nil
}

func (r *RedisBroadcaster) Stop() error {
    r.cancel()
    
    if r.pubsub != nil {
        r.pubsub.Close()
    }
    
    r.wg.Wait()
    r.logger.Info("Redis broadcaster stopped")
    
    return nil
}
```

---

## 5. Message Handler

```go
func (r *RedisBroadcaster) handleMessages() {
    defer r.wg.Done()
    
    ch := r.pubsub.Channel()
    
    for {
        select {
        case <-r.ctx.Done():
            return
            
        case msg, ok := <-ch:
            if !ok {
                return
            }
            r.processMessage(msg)
        }
    }
}

func (r *RedisBroadcaster) processMessage(msg *redis.Message) {
    channel := msg.Channel
    payload := []byte(msg.Payload)
    
    // Broadcast to all
    if channel == channelAll {
        r.hub.BroadcastToAll(payload)
        return
    }
    
    // Tenant broadcast
    if strings.HasPrefix(channel, channelTenantPrefix) {
        tenantIDStr := strings.TrimPrefix(channel, channelTenantPrefix)
        tenantID, err := strconv.ParseInt(tenantIDStr, 10, 64)
        if err != nil {
            r.logger.Warn("Invalid tenant channel", zap.String("channel", channel))
            return
        }
        r.hub.sendToTenant(tenantID, payload, "")
        return
    }
    
    // User-specific
    if strings.HasPrefix(channel, channelPrefix) {
        userIDStr := strings.TrimPrefix(channel, channelPrefix)
        userID, err := strconv.ParseInt(userIDStr, 10, 64)
        if err != nil {
            r.logger.Warn("Invalid user channel", zap.String("channel", channel))
            return
        }
        r.hub.sendToUser(userID, payload, "")
        return
    }
}
```

---

## 6. Publish Methods

```go
// PublishToUser sends message to a specific user across all instances
func (r *RedisBroadcaster) PublishToUser(ctx context.Context, userID int64, message []byte) error {
    channel := fmt.Sprintf("%s%d", channelPrefix, userID)
    
    err := r.redis.Publish(ctx, channel, message).Err()
    if err != nil {
        r.logger.Error("Failed to publish to user",
            zap.Int64("userID", userID),
            zap.Error(err),
        )
        return err
    }
    
    return nil
}

// PublishToTenant sends message to all users in a tenant
func (r *RedisBroadcaster) PublishToTenant(ctx context.Context, tenantID int64, message []byte) error {
    channel := fmt.Sprintf("%s%d", channelTenantPrefix, tenantID)
    
    return r.redis.Publish(ctx, channel, message).Err()
}

// PublishToAll broadcasts to all connected clients
func (r *RedisBroadcaster) PublishToAll(ctx context.Context, message []byte) error {
    return r.redis.Publish(ctx, channelAll, message).Err()
}
```

---

## 7. Hybrid Delivery Strategy

Kết hợp local Hub delivery và Redis Pub/Sub:

```go
// core/service/delivery_service.go
package service

type DeliveryService struct {
    hub         *websocket.Hub
    broadcaster *websocket.RedisBroadcaster
    logger      *zap.Logger
}

func (s *DeliveryService) DeliverToUser(
    ctx context.Context,
    userID int64,
    message []byte,
) error {
    // Check if user is connected locally
    if s.hub.IsUserConnected(userID) {
        // Deliver locally (faster)
        s.hub.SendToUser(userID, message)
        s.logger.Debug("Delivered locally", zap.Int64("userID", userID))
        return nil
    }
    
    // User not connected locally, broadcast via Redis
    // Other instances will pick it up
    err := s.broadcaster.PublishToUser(ctx, userID, message)
    if err != nil {
        s.logger.Error("Failed to publish via Redis",
            zap.Int64("userID", userID),
            zap.Error(err),
        )
        return err
    }
    
    s.logger.Debug("Published via Redis", zap.Int64("userID", userID))
    return nil
}

// DeliverToUserAlways always uses Redis (for consistency)
func (s *DeliveryService) DeliverToUserAlways(
    ctx context.Context,
    userID int64,
    message []byte,
) error {
    return s.broadcaster.PublishToUser(ctx, userID, message)
}
```

---

## 8. Connection Tracking với Redis

Track user connections across instances:

```go
// infrastructure/websocket/connection_tracker.go
package websocket

const (
    connectionSetPrefix = "ws:connections:"
    connectionTTL       = 5 * time.Minute
)

type ConnectionTracker struct {
    redis      *redis.Client
    instanceID string
    logger     *zap.Logger
}

func NewConnectionTracker(redis *redis.Client, instanceID string, logger *zap.Logger) *ConnectionTracker {
    return &ConnectionTracker{
        redis:      redis,
        instanceID: instanceID,
        logger:     logger,
    }
}

// TrackConnection adds user connection to Redis set
func (t *ConnectionTracker) TrackConnection(ctx context.Context, userID int64, clientID string) error {
    key := fmt.Sprintf("%s%d", connectionSetPrefix, userID)
    member := fmt.Sprintf("%s:%s", t.instanceID, clientID)
    
    pipe := t.redis.Pipeline()
    pipe.SAdd(ctx, key, member)
    pipe.Expire(ctx, key, connectionTTL)
    _, err := pipe.Exec(ctx)
    
    return err
}

// UntrackConnection removes user connection from Redis set
func (t *ConnectionTracker) UntrackConnection(ctx context.Context, userID int64, clientID string) error {
    key := fmt.Sprintf("%s%d", connectionSetPrefix, userID)
    member := fmt.Sprintf("%s:%s", t.instanceID, clientID)
    
    return t.redis.SRem(ctx, key, member).Err()
}

// IsUserOnline checks if user has any active connections
func (t *ConnectionTracker) IsUserOnline(ctx context.Context, userID int64) (bool, error) {
    key := fmt.Sprintf("%s%d", connectionSetPrefix, userID)
    count, err := t.redis.SCard(ctx, key).Result()
    
    return count > 0, err
}

// GetUserConnections returns all connections for a user
func (t *ConnectionTracker) GetUserConnections(ctx context.Context, userID int64) ([]string, error) {
    key := fmt.Sprintf("%s%d", connectionSetPrefix, userID)
    return t.redis.SMembers(ctx, key).Result()
}
```

---

## 9. Heartbeat Refresh

Refresh connection TTL định kỳ:

```go
func (t *ConnectionTracker) StartHeartbeat(ctx context.Context) {
    ticker := time.NewTicker(connectionTTL / 2)
    defer ticker.Stop()
    
    for {
        select {
        case <-ctx.Done():
            return
        case <-ticker.C:
            t.refreshAllConnections(ctx)
        }
    }
}

func (t *ConnectionTracker) refreshAllConnections(ctx context.Context) {
    // Refresh TTL for all tracked connections
    // This ensures stale connections are cleaned up
}
```

---

## 10. Architecture Summary

```
┌──────────────────────────────────────────────────────────────────┐
│                    Notification Service Instance                  │
│                                                                   │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │ Delivery    │───▶│    Hub      │───▶│   WS Clients        │  │
│  │ Service     │    │  (Local)    │    │   (Connected here)  │  │
│  └──────┬──────┘    └─────────────┘    └─────────────────────┘  │
│         │                                                        │
│         │ (if user not local)                                    │
│         ▼                                                        │
│  ┌─────────────────────────┐                                     │
│  │   Redis Broadcaster     │                                     │
│  │   (Publish/Subscribe)   │                                     │
│  └───────────┬─────────────┘                                     │
│              │                                                   │
└──────────────┼───────────────────────────────────────────────────┘
               │
               ▼
        ┌─────────────┐
        │    Redis    │
        │  Pub/Sub    │
        └──────┬──────┘
               │
    ┌──────────┼──────────┐
    ▼          ▼          ▼
┌───────┐  ┌───────┐  ┌───────┐
│Inst 1 │  │Inst 2 │  │Inst 3 │
└───────┘  └───────┘  └───────┘
```
