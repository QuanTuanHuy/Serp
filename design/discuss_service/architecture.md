# Discuss Service - Architecture Design

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Detailed Architecture Specification  
**Date:** December 2025  

## 🏛️ Clean Architecture Layers

Discuss Service tuân thủ **Clean Architecture** pattern của SERP:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                             │
│  ├── controller/      (HTTP handlers)                   │
│  ├── router/          (Route definitions)               │
│  ├── middleware/      (Auth, logging)                   │
│  ├── kafka/           (Event consumers)                 │
│  └── websocket/       (WebSocket handlers)              │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                   Core Layer                            │
│  ├── usecase/         (Orchestration, transactions)     │
│  ├── service/         (Business rules, validation)      │
│  ├── domain/entity/   (Business entities)               │
│  ├── domain/dto/      (Request/Response DTOs)           │
│  ├── domain/enum/     (Constants, enums)                │
│  ├── mapper/          (Entity ↔ DTO mappers)            │
│  └── port/            (Interfaces for adapters)         │
│      ├── store/       (Repository interfaces)           │
│      └── client/      (External client interfaces)      │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│              Infrastructure Layer                       │
│  ├── store/adapter/   (PostgreSQL implementations)      │
│  ├── store/model/     (DB models with GORM tags)        │
│  ├── store/mapper/    (Model ↔ Entity mappers)          │
│  └── client/          (Redis, Kafka, S3 clients)        │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Kernel Layer                           │
│  ├── properties/      (Config from .env)                │
│  └── utils/           (AuthUtils, ResponseUtils)        │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Directory Structure

```
discuss_service/
├── Dockerfile
├── go.mod
├── go.sum
├── run-dev.sh
├── run-prod.sh
├── .env.example
│
├── src/
│   ├── main.go
│   │
│   ├── cmd/
│   │   └── bootstrap/
│   │       └── all.go              # Uber FX module registration
│   │
│   ├── config/
│   │   └── config.yaml             # Service configuration
│   │
│   ├── core/
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   ├── base.go
│   │   │   │   ├── channel.go
│   │   │   │   ├── channel_member.go
│   │   │   │   ├── message.go
│   │   │   │   ├── attachment.go
│   │   │   │   ├── reaction.go
│   │   │   │   ├── activity_feed.go
│   │   │   │   └── user_presence.go
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── channel_request.go
│   │   │   │   │   ├── message_request.go
│   │   │   │   │   └── search_request.go
│   │   │   │   └── response/
│   │   │   │       ├── channel_response.go
│   │   │   │       ├── message_response.go
│   │   │   │       └── activity_response.go
│   │   │   │
│   │   │   ├── enum/
│   │   │   │   ├── channel_type.go
│   │   │   │   ├── channel_role.go
│   │   │   │   ├── message_type.go
│   │   │   │   └── presence_status.go
│   │   │   │
│   │   │   └── event/
│   │   │       ├── entity_event.go      # Events from other services
│   │   │       └── message_event.go     # Internal events
│   │   │
│   │   ├── mapper/
│   │   │   ├── channel_mapper.go
│   │   │   ├── message_mapper.go
│   │   │   └── activity_mapper.go
│   │   │
│   │   ├── port/
│   │   │   ├── store/
│   │   │   │   ├── channel_port.go
│   │   │   │   ├── message_port.go
│   │   │   │   ├── channel_member_port.go
│   │   │   │   └── activity_port.go
│   │   │   │
│   │   │   └── client/
│   │   │       ├── kafka_producer_port.go
│   │   │       ├── redis_cache_port.go
│   │   │       ├── s3_storage_port.go
│   │   │       └── notification_client_port.go
│   │   │
│   │   ├── service/
│   │   │   ├── transaction_service.go
│   │   │   ├── channel_service.go
│   │   │   ├── message_service.go
│   │   │   ├── channel_member_service.go
│   │   │   ├── search_service.go
│   │   │   ├── presence_service.go
│   │   │   └── activity_service.go
│   │   │
│   │   ├── usecase/
│   │   │   ├── channel_usecase.go
│   │   │   ├── message_usecase.go
│   │   │   ├── search_usecase.go
│   │   │   └── activity_usecase.go
│   │   │
│   │   └── websocket/
│   │       ├── hub.go                  # WebSocket hub (reuse from notification)
│   │       ├── client.go               # WebSocket client
│   │       └── message_handler.go      # Message routing
│   │
│   ├── infrastructure/
│   │   ├── store/
│   │   │   ├── adapter/
│   │   │   │   ├── channel_adapter.go
│   │   │   │   ├── message_adapter.go
│   │   │   │   ├── channel_member_adapter.go
│   │   │   │   └── activity_adapter.go
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── channel_model.go
│   │   │   │   ├── message_model.go
│   │   │   │   ├── channel_member_model.go
│   │   │   │   └── activity_model.go
│   │   │   │
│   │   │   └── mapper/
│   │   │       ├── channel_model_mapper.go
│   │   │       └── message_model_mapper.go
│   │   │
│   │   └── client/
│   │       ├── kafka/
│   │       │   ├── producer.go
│   │       │   └── consumer.go
│   │       │
│   │       ├── redis/
│   │       │   ├── cache.go
│   │       │   └── presence.go
│   │       │
│   │       ├── s3/
│   │       │   └── storage.go
│   │       │
│   │       └── notification/
│   │           └── notification_client.go
│   │
│   ├── ui/
│   │   ├── controller/
│   │   │   ├── channel_controller.go
│   │   │   ├── message_controller.go
│   │   │   ├── websocket_controller.go
│   │   │   └── activity_controller.go
│   │   │
│   │   ├── router/
│   │   │   └── router.go
│   │   │
│   │   ├── middleware/
│   │   │   ├── auth.go
│   │   │   ├── tenant.go
│   │   │   └── logging.go
│   │   │
│   │   └── kafka/
│   │       ├── entity_event_consumer.go  # Consume from CRM, PTM, etc
│   │       └── handler.go
│   │
│   └── kernel/
│       ├── properties/
│       │   └── app_properties.go
│       │
│       └── utils/
│           ├── auth_utils.go
│           ├── response_utils.go
│           ├── time_utils.go
│           └── validation_utils.go
│
└── migrations/
    ├── 001_create_channels_table.sql
    ├── 002_create_messages_table.sql
    ├── 003_create_channel_members_table.sql
    ├── 004_create_activities_table.sql
    └── 005_create_indexes.sql
```

---

## 🔄 Data Flow

### **1. REST API Flow (Create Message)**

```
Client Request
     │
     ▼
┌─────────────────┐
│  API Gateway    │  JWT validation, extract userID, tenantID
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessageController│  HTTP handler
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessageUseCase  │  Orchestration:
│                 │  1. Start transaction
│                 │  2. Call MessageService
│                 │  3. Publish Kafka event
│                 │  4. Broadcast via WebSocket
│                 │  5. Commit transaction
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessageService  │  Business logic:
│                 │  - Validate permissions
│                 │  - Check channel exists
│                 │  - Process @mentions
│                 │  - Extract attachments
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessagePort     │  Interface
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessageAdapter  │  Implementation:
│                 │  - Convert Entity → Model
│                 │  - Execute SQL (GORM)
│                 │  - Convert Model → Entity
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  PostgreSQL     │  Data persistence
└─────────────────┘
```

### **2. WebSocket Flow (Real-time Message)**

```
User sends message via WebSocket
     │
     ▼
┌─────────────────┐
│ WebSocket Client│  Authenticated connection
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ WebSocket Hub   │  Routing:
│                 │  - Identify channel members
│                 │  - Find active connections
│                 │  - Broadcast to recipients
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ MessageHandler  │  Process:
│                 │  1. Persist to DB
│                 │  2. Cache in Redis
│                 │  3. Update unread counts
│                 │  4. Trigger notifications
└─────────────────┘
```

### **3. Kafka Event Flow (Entity Created)**

```
CRM Service: Customer created
     │
     ▼
┌─────────────────┐
│ Kafka Topic     │  CUSTOMER_CREATED event
│ "ENTITY_EVENTS" │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Kafka Consumer  │  Discuss service listens
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ EntityEventHandler│ Process event:
│                 │  1. Extract entity info
│                 │  2. Create TOPIC channel
│                 │  3. Add relevant members
│                 │  4. Post system message
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ChannelService  │  Auto-create channel
└─────────────────┘
```

---

## 🔌 Component Interactions

### **Channel Management**

```go
type ChannelService struct {
    channelPort       IChannelPort
    memberPort        IChannelMemberPort
    kafkaProducer     IKafkaProducerPort
    cache             IRedisCachePort
}

func (s *ChannelService) CreateChannel(
    ctx context.Context,
    channel *entity.ChannelEntity,
    memberIDs []int64,
) (*entity.ChannelEntity, error) {
    // 1. Validate business rules
    if err := s.validateChannel(channel); err != nil {
        return nil, err
    }
    
    // 2. Check for existing DIRECT channel
    if channel.Type == enum.ChannelTypeDirect {
        existing, _ := s.channelPort.FindDirectChannel(
            ctx, memberIDs[0], memberIDs[1],
        )
        if existing != nil {
            return existing, nil
        }
    }
    
    // 3. Persist channel
    channel, err := s.channelPort.Save(ctx, channel)
    if err != nil {
        return nil, err
    }
    
    // 4. Add members
    for _, userID := range memberIDs {
        member := &entity.ChannelMemberEntity{
            ChannelID: channel.ID,
            UserID:    userID,
            TenantID:  channel.TenantID,
            Role:      enum.RoleMember,
        }
        if userID == channel.CreatedBy {
            member.Role = enum.RoleOwner
        }
        s.memberPort.Save(ctx, member)
    }
    
    // 5. Cache channel info
    s.cache.SetChannel(ctx, channel)
    
    return channel, nil
}
```

### **Message Handling**

```go
type MessageService struct {
    messagePort       IMessagePort
    channelService    IChannelService
    memberService     IChannelMemberService
    presenceService   IPresenceService
    cache             IRedisCachePort
}

func (s *MessageService) CreateMessage(
    ctx context.Context,
    tx *gorm.DB,
    message *entity.MessageEntity,
) (*entity.MessageEntity, error) {
    // 1. Validate sender is channel member
    isMember, err := s.memberService.IsMember(
        ctx, message.ChannelID, message.SenderID,
    )
    if !isMember {
        return nil, errors.New("sender not in channel")
    }
    
    // 2. Process @mentions
    mentions := s.extractMentions(message.Content)
    message.Mentions = mentions
    
    // 3. Persist message
    message, err = s.messagePort.Save(ctx, tx, message)
    if err != nil {
        return nil, err
    }
    
    // 4. Update unread counts
    members, _ := s.memberService.GetChannelMembers(
        ctx, message.ChannelID,
    )
    for _, member := range members {
        if member.UserID != message.SenderID {
            s.memberService.IncrementUnread(
                ctx, member.ID,
            )
        }
    }
    
    // 5. Cache recent messages
    s.cache.AddRecentMessage(ctx, message)
    
    return message, nil
}

func (s *MessageService) extractMentions(content string) []int64 {
    // Regex to find @userID or @username
    // Convert to userIDs
    // Return list
}
```

### **WebSocket Hub**

```go
type Hub struct {
    // Connections: userID → map of clients
    clients map[int64]map[*Client]bool
    
    // Channels operations
    register   chan *Client
    unregister chan *Client
    broadcast  chan *BroadcastMessage
    
    mutex sync.RWMutex
    logger *zap.Logger
}

type BroadcastMessage struct {
    ChannelID  int64
    Message    *entity.MessageEntity
    ExcludeIDs []int64  // Don't send to sender
}

func (h *Hub) BroadcastToChannel(
    channelID int64,
    message *entity.MessageEntity,
    excludeIDs []int64,
) {
    // 1. Get channel members
    members := h.getMembersFromCache(channelID)
    
    // 2. For each member (except excluded)
    for _, userID := range members {
        if contains(excludeIDs, userID) {
            continue
        }
        
        // 3. Get user's active connections
        if clients, ok := h.clients[userID]; ok {
            messageBytes := json.Marshal(message)
            
            for client := range clients {
                select {
                case client.send <- messageBytes:
                    // Sent successfully
                default:
                    // Buffer full, close client
                    h.unregisterClient(client)
                }
            }
        }
    }
}
```

### **Presence Tracking**

```go
type PresenceService struct {
    cache  IRedisCachePort
    logger *zap.Logger
}

type UserPresence struct {
    UserID       int64
    Status       enum.PresenceStatus
    LastSeen     int64
    ConnectedAt  int64
}

func (s *PresenceService) SetOnline(
    ctx context.Context,
    userID int64,
) error {
    presence := &UserPresence{
        UserID:      userID,
        Status:      enum.StatusOnline,
        LastSeen:    time.Now().UnixMilli(),
        ConnectedAt: time.Now().UnixMilli(),
    }
    
    // Store in Redis with TTL
    key := fmt.Sprintf("presence:%d", userID)
    return s.cache.Set(ctx, key, presence, 5*time.Minute)
}

func (s *PresenceService) UpdateLastSeen(
    ctx context.Context,
    userID int64,
) {
    key := fmt.Sprintf("presence:%d", userID)
    s.cache.Update(ctx, key, map[string]any{
        "lastSeen": time.Now().UnixMilli(),
    })
}

func (s *PresenceService) GetOnlineUsers(
    ctx context.Context,
    userIDs []int64,
) map[int64]*UserPresence {
    result := make(map[int64]*UserPresence)
    
    for _, userID := range userIDs {
        key := fmt.Sprintf("presence:%d", userID)
        presence, err := s.cache.Get(ctx, key)
        if err == nil {
            result[userID] = presence
        }
    }
    
    return result
}
```

---

## 🔐 Security Architecture

### **Authentication Flow**

```
1. User logs in → Account Service issues JWT
2. Frontend stores JWT in localStorage
3. API requests include: Authorization: Bearer <JWT>
4. API Gateway validates JWT via Keycloak JWKS
5. Gateway extracts userID, tenantID → Forward to discuss_service
6. Discuss service extracts from headers:
   - X-User-ID
   - X-Tenant-ID
```

### **Authorization Checks**

```go
type ChannelMemberService struct {
    memberPort IChannelMemberPort
}

func (s *ChannelMemberService) CanAccessChannel(
    ctx context.Context,
    channelID int64,
    userID int64,
) (bool, error) {
    // 1. Check if user is member
    member, err := s.memberPort.FindByChannelAndUser(
        ctx, channelID, userID,
    )
    
    if err != nil || member == nil {
        return false, nil
    }
    
    // 2. Check if member is active (not removed)
    if member.LeftAt != nil {
        return false, nil
    }
    
    return true, nil
}

func (s *ChannelMemberService) CanSendMessage(
    ctx context.Context,
    channelID int64,
    userID int64,
) (bool, error) {
    member, _ := s.memberPort.FindByChannelAndUser(
        ctx, channelID, userID,
    )
    
    if member == nil {
        return false, errors.New("not a member")
    }
    
    // Check if muted or restricted
    if member.IsMuted {
        return false, errors.New("muted in channel")
    }
    
    return true, nil
}
```

### **Entity-based Permissions**

```go
// For TOPIC channels linked to entities
func (s *ChannelService) CanAccessTopicChannel(
    ctx context.Context,
    channel *entity.ChannelEntity,
    userID int64,
) (bool, error) {
    if channel.Type != enum.ChannelTypeTopic {
        return true, nil
    }
    
    // Check entity permissions via appropriate service
    switch channel.EntityType {
    case "customer":
        return s.crmClient.CanViewCustomer(
            ctx, *channel.EntityID, userID,
        )
    case "task":
        return s.ptmClient.CanViewTask(
            ctx, *channel.EntityID, userID,
        )
    case "order":
        return s.purchaseClient.CanViewOrder(
            ctx, *channel.EntityID, userID,
        )
    default:
        return false, nil
    }
}
```

---

## 📊 Performance Optimizations

### **1. Message Caching**

```go
// Redis structure for recent messages
Key: "channel:{channelID}:messages"
Type: Sorted Set (ZSET)
Score: Unix timestamp
Value: JSON(MessageEntity)
TTL: 1 hour

// Only cache last 100 messages per channel
func (c *RedisCache) AddRecentMessage(
    ctx context.Context,
    message *entity.MessageEntity,
) error {
    key := fmt.Sprintf("channel:%d:messages", message.ChannelID)
    
    // Add to sorted set
    c.client.ZAdd(ctx, key, redis.Z{
        Score:  float64(message.CreatedAt),
        Member: json.Marshal(message),
    })
    
    // Keep only last 100
    c.client.ZRemRangeByRank(ctx, key, 0, -101)
    
    // Set expiry
    c.client.Expire(ctx, key, 1*time.Hour)
}
```

### **2. Unread Count Optimization**

```go
// Instead of COUNT(*), maintain counter in channel_members
type ChannelMemberModel struct {
    ID              int64
    ChannelID       int64
    UserID          int64
    LastReadMsgID   *int64
    UnreadCount     int    // Incremented on new message
    // ...
}

// On message read
func (s *ChannelMemberService) MarkAsRead(
    ctx context.Context,
    channelID int64,
    userID int64,
    messageID int64,
) error {
    return s.memberPort.Update(ctx, map[string]any{
        "last_read_msg_id": messageID,
        "unread_count":     0,
    })
}
```

### **3. Typing Indicators (Ephemeral)**

```go
// Don't store in DB, use Redis with short TTL
Key: "typing:{channelID}:{userID}"
Value: Unix timestamp
TTL: 10 seconds

func (s *PresenceService) SetTyping(
    ctx context.Context,
    channelID int64,
    userID int64,
) {
    key := fmt.Sprintf("typing:%d:%d", channelID, userID)
    s.cache.Set(ctx, key, time.Now().UnixMilli(), 10*time.Second)
    
    // Broadcast to channel via WebSocket
    s.hub.BroadcastTyping(channelID, userID, true)
}

func (s *PresenceService) StopTyping(
    ctx context.Context,
    channelID int64,
    userID int64,
) {
    key := fmt.Sprintf("typing:%d:%d", channelID, userID)
    s.cache.Delete(ctx, key)
    
    s.hub.BroadcastTyping(channelID, userID, false)
}
```

### **4. Connection Pooling**

```go
// PostgreSQL connection pool
func NewDatabase(config *Config) *gorm.DB {
    db, err := gorm.Open(postgres.Open(config.DSN), &gorm.Config{})
    
    sqlDB, _ := db.DB()
    sqlDB.SetMaxOpenConns(100)        // Max connections
    sqlDB.SetMaxIdleConns(10)         // Idle pool size
    sqlDB.SetConnMaxLifetime(1*time.Hour)
    
    return db
}

// Redis connection pool
func NewRedis(config *Config) *redis.Client {
    return redis.NewClient(&redis.Options{
        Addr:         config.RedisAddr,
        PoolSize:     50,
        MinIdleConns: 10,
    })
}
```

---

## 🔄 Event-Driven Architecture

### **Kafka Topics**

```
1. ENTITY_EVENTS (consume)
   - CUSTOMER_CREATED
   - TASK_CREATED
   - ORDER_CREATED
   - etc.

2. MESSAGE_EVENTS (produce)
   - MESSAGE_SENT
   - MESSAGE_EDITED
   - MESSAGE_DELETED

3. NOTIFICATION_EVENTS (produce)
   - MENTION_NOTIFICATION
   - DM_NOTIFICATION
   - MISSED_MESSAGE_NOTIFICATION
```

### **Event Handlers**

```go
type EntityEventConsumer struct {
    channelUseCase IChannelUseCase
    logger         *zap.Logger
}

func (c *EntityEventConsumer) HandleEvent(
    ctx context.Context,
    event *event.EntityEvent,
) error {
    switch event.Type {
    case "CUSTOMER_CREATED":
        return c.handleCustomerCreated(ctx, event)
    case "TASK_CREATED":
        return c.handleTaskCreated(ctx, event)
    default:
        c.logger.Warn("Unknown event type", zap.String("type", event.Type))
    }
    return nil
}

func (c *EntityEventConsumer) handleCustomerCreated(
    ctx context.Context,
    event *event.EntityEvent,
) error {
    channel := &entity.ChannelEntity{
        TenantID:    event.TenantID,
        Name:        fmt.Sprintf("Customer: %s", event.EntityName),
        Type:        enum.ChannelTypeTopic,
        EntityType:  ptr("customer"),
        EntityID:    &event.EntityID,
        CreatedBy:   event.CreatedBy,
        IsPrivate:   false,
    }
    
    // Add creator + assigned sales rep as members
    memberIDs := []int64{event.CreatedBy}
    if event.AssignedTo != nil {
        memberIDs = append(memberIDs, *event.AssignedTo)
    }
    
    return c.channelUseCase.CreateChannel(ctx, channel, memberIDs)
}
```

---

## 🧪 Testing Strategy

### **Unit Tests**
```go
// Service layer tests with mocks
func TestMessageService_CreateMessage(t *testing.T) {
    mockPort := new(MockMessagePort)
    mockMemberService := new(MockChannelMemberService)
    
    service := &MessageService{
        messagePort:    mockPort,
        memberService:  mockMemberService,
    }
    
    // Setup mocks
    mockMemberService.On("IsMember", 1, 100).Return(true, nil)
    mockPort.On("Save", mock.Anything).Return(message, nil)
    
    // Test
    result, err := service.CreateMessage(ctx, tx, message)
    
    assert.NoError(t, err)
    assert.NotNil(t, result)
    mockPort.AssertExpectations(t)
}
```

### **Integration Tests**
```go
// Test with real database (testcontainers)
func TestChannelAdapter_Save(t *testing.T) {
    db := setupTestDB(t)
    defer teardownTestDB(t, db)
    
    adapter := NewChannelAdapter(db)
    
    channel := &entity.ChannelEntity{
        Name: "Test Channel",
        Type: enum.ChannelTypeGroup,
    }
    
    saved, err := adapter.Save(context.Background(), channel)
    
    assert.NoError(t, err)
    assert.NotZero(t, saved.ID)
}
```

### **WebSocket Tests**
```go
func TestWebSocketHub_BroadcastToChannel(t *testing.T) {
    hub := NewHub(logger)
    
    // Create mock clients
    client1 := &Client{userID: 1, send: make(chan []byte)}
    client2 := &Client{userID: 2, send: make(chan []byte)}
    
    hub.RegisterClient(client1)
    hub.RegisterClient(client2)
    
    // Broadcast message
    message := &entity.MessageEntity{ChannelID: 100}
    hub.BroadcastToChannel(100, message, []int64{1})
    
    // Verify only client2 receives
    select {
    case <-client2.send:
        // OK
    case <-time.After(1 * time.Second):
        t.Error("client2 did not receive message")
    }
}
```

---

## 📈 Scalability Considerations

### **Horizontal Scaling**
- Multiple discuss_service instances behind load balancer
- WebSocket sticky sessions (user always connects to same instance)
- Redis Pub/Sub for cross-instance WebSocket broadcasting

### **Database Sharding**
```
// Shard by tenantID for multi-tenancy
Shard 1: tenantID % 4 == 0
Shard 2: tenantID % 4 == 1
Shard 3: tenantID % 4 == 2
Shard 4: tenantID % 4 == 3
```

### **Read Replicas**
```
Master DB: Writes (INSERT, UPDATE, DELETE)
Replica 1: Read queries (message history)
Replica 2: Analytics queries
```

---

## 🚨 Error Handling

### **Graceful Degradation**
```go
func (s *MessageService) CreateMessage(...) error {
    // 1. Try to save to DB
    if err := s.messagePort.Save(ctx, tx, message); err != nil {
        return err
    }
    
    // 2. Try to cache (non-critical)
    if err := s.cache.AddMessage(ctx, message); err != nil {
        s.logger.Warn("Failed to cache message", zap.Error(err))
        // Continue, don't fail the request
    }
    
    // 3. Try to broadcast (non-critical)
    if err := s.hub.Broadcast(message); err != nil {
        s.logger.Warn("Failed to broadcast", zap.Error(err))
        // Users will get it via polling
    }
    
    return nil
}
```

### **Retry Logic**
```go
// Kafka event processing with retries
func (c *EntityEventConsumer) ProcessWithRetry(
    event *Event,
) error {
    maxRetries := 3
    for i := 0; i < maxRetries; i++ {
        if err := c.HandleEvent(ctx, event); err == nil {
            return nil
        }
        time.Sleep(time.Duration(i+1) * time.Second)
    }
    
    // Move to dead letter queue after max retries
    c.sendToDeadLetter(event)
    return errors.New("max retries exceeded")
}
```

---

## 📝 Summary

This architecture provides:
- ✅ **Clean separation of concerns** (UI → Core → Infrastructure)
- ✅ **Testable** (interfaces allow mocking)
- ✅ **Scalable** (stateless services, Redis caching)
- ✅ **Real-time** (WebSocket Hub)
- ✅ **Event-driven** (Kafka integration)
- ✅ **Secure** (JWT auth, tenant isolation)
- ✅ **Maintainable** (consistent with SERP patterns)
