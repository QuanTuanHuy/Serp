# Discuss Service - Implementation Plan

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Development Roadmap  
**Date:** December 2025  
**Last Updated:** December 30, 2025  
**Estimated Timeline:** 11 weeks  

## 🎯 Project Overview

**Goal:** Build a production-ready unified communication module for SERP ERP system.

**Success Metrics:**
- ✅ Support 10,000+ concurrent WebSocket connections
- ✅ <100ms message delivery latency (p95)
- ✅ 99.9% uptime
- ✅ Zero message loss
- ✅ Full integration with existing SERP modules

---

## 📊 Feature Analysis & Prioritization

### **Core Features (Must-Have)**

#### **1. Channel Management**
**Description:** Organization unit for conversations
- **Types:** 
  - DIRECT (1-1 private chat)
  - GROUP (team channels with members)
  - TOPIC (auto-created for business entities)
- **Features:**
  - Create/update/archive channels
  - Add/remove members with role management (OWNER, ADMIN, MEMBER)
  - Private/public visibility control
  - Channel metadata (name, description, color, icon)
- **API Endpoints:** 8 endpoints (POST, GET, PATCH, DELETE channels + members)
- **Status:** ✅ Designed, ⏳ Backend pending, ✅ Frontend UI ready

#### **2. Real-time Messaging**
**Description:** Core communication feature with instant delivery
- **Message Types:**
  - TEXT (plain text with markdown support)
  - IMAGE (with thumbnail generation)
  - FILE (documents, archives)
  - CODE (syntax highlighted)
  - SYSTEM (automated notifications)
- **Features:**
  - Send/receive messages with <100ms latency
  - Edit messages (24h limit)
  - Delete messages (soft delete)
  - Read receipts & delivery status
  - @mentions with notifications
- **Technology:** WebSocket + Kafka + PostgreSQL
- **API Endpoints:** 7 endpoints for message CRUD
- **Status:** ✅ Designed, ⏳ Backend pending, ✅ Frontend components ready

#### **3. Message Reactions**
**Description:** Quick emotional responses to messages
- **Features:**
  - 300+ emojis across 6 categories
  - Multiple reactions per message
  - User list showing who reacted
  - Real-time reaction updates via WebSocket
- **Storage:** JSONB column in messages table
- **API Endpoints:** 2 endpoints (add/remove reactions)
- **Status:** ✅ Designed, ⏳ Backend pending, ✅ Frontend implemented with bug fixes

#### **4. Thread Replies**
**Description:** Organized sub-conversations within messages
- **Features:**
  - Reply to any message
  - Thread count indicator
  - Nested reply view
  - Thread participants tracking
  - Notifications for thread activity
- **Database:** `parent_id` foreign key + `thread_count` denormalized
- **API Endpoints:** 1 endpoint (get thread replies)
- **Status:** ✅ Designed, ⏳ Backend pending, ✅ Frontend ThreadIndicator ready

### **Advanced Features (Should-Have)**

#### **5. File Attachments**
**Description:** Share files within conversations
- **Supported Types:**
  - Images (JPEG, PNG, GIF, WebP) - with thumbnails
  - Documents (PDF, DOCX, XLSX)
  - Archives (ZIP, RAR)
  - Code files (JS, PY, GO, etc.)
- **Features:**
  - Drag & drop upload
  - Progress indicator
  - Virus scanning (ClamAV integration)
  - Presigned URLs for download
  - 100MB size limit
- **Storage:** S3/MinIO
- **API Endpoints:** 2 endpoints (upload, download)
- **Status:** ✅ Designed, ⏳ Backend pending, ⏳ Frontend pending (Week 9)

#### **6. Full-text Search**
**Description:** Find messages and channels quickly
- **Search Scope:**
  - Message content
  - Channel names
  - File names
- **Features:**
  - Advanced filters (channels, date range, sender, file type)
  - Highlighted search results
  - Search history
  - Keyboard shortcuts (Ctrl+K)
  - Pagination
- **Technology:** PostgreSQL tsvector + GIN index
- **API Endpoints:** 2 endpoints (search messages, search channels)
- **Status:** ✅ Designed, ⏳ Backend pending, ⏳ Frontend pending (Week 9)

#### **7. Presence & Online Status**
**Description:** Show who's online and available
- **Features:**
  - Online/away/busy/offline states
  - Last seen timestamp
  - Automatic away detection (5 min idle)
  - Device tracking (web, mobile, desktop)
  - Pulse animation for online users
- **Technology:** Redis with TTL keys
- **API Endpoints:** 1 endpoint (get online users)
- **Status:** ✅ Designed, ⏳ Backend pending, ✅ Frontend OnlineStatusIndicator ready

#### **8. Typing Indicators**
**Description:** Show when someone is typing
- **Features:**
  - Real-time typing broadcast via WebSocket
  - Auto-expire after 10 seconds
  - Multiple users typing display
  - Channel-specific indicators
- **Technology:** Redis TTL keys + WebSocket events
- **API Endpoints:** 2 endpoints (start/stop typing)
- **Status:** ✅ Designed, ⏳ Backend pending, ⏳ Frontend pending

#### **9. Activity Feed**
**Description:** Unified notification center across all modules
- **Activity Types:**
  - MENTION_RECEIVED - Someone mentioned you
  - CHANNEL_CREATED - New channel created
  - MEMBER_ADDED - Added to channel
  - THREAD_REPLY - Reply to your message
  - ENTITY_UPDATED - Linked entity changed
- **Features:**
  - Cross-module aggregation
  - Read/unread tracking
  - Action buttons (view, dismiss)
  - Filtering by type
  - Pagination
- **Integration:** Consumes Kafka events from all services
- **API Endpoints:** 3 endpoints (get feed, mark read, read all)
- **Status:** ✅ Designed, ⏳ Backend pending, ⏳ Frontend pending

### **Integration Features (Must-Have)**

#### **10. Auto-Channel Creation for Entities**
**Description:** Automatically create TOPIC channels when business entities are created
- **Supported Entities:**
  - **CRM:** Customer, Lead, Opportunity
  - **PTM:** Task, Project
  - **Purchase:** Order, Supplier
  - **Logistics:** Shipment, Inventory Item
- **Workflow:**
  1. Service emits ENTITY_CREATED Kafka event
  2. Discuss service consumes event
  3. Creates TOPIC channel with entity context
  4. Adds relevant members based on entity permissions
- **Technology:** Kafka consumer + event handlers
- **Status:** ✅ Designed, ⏳ Backend pending (Week 5)

#### **11. Permission-Based Access Control**
**Description:** Ensure users can only access channels for entities they have permission to view
- **Checks:**
  - CRM entities: Check via crm_client
  - PTM entities: Check via ptm_client
  - Purchase entities: Check via purchase_client
- **Implementation:**
  - Service-to-service HTTP calls
  - Cached permissions in Redis
  - Fallback to deny on service unavailability
- **Status:** ✅ Designed, ⏳ Backend pending (Week 6)

#### **12. Notification Integration**
**Description:** Send notifications when users are mentioned or receive messages
- **Notification Channels:**
  - **In-app:** WebSocket push notifications
  - **Email:** For offline users (digest emails)
  - **Push:** Mobile app notifications (future)
- **Triggers:**
  - @mention in message
  - Direct message received
  - Reply to thread
  - Added to channel
- **Technology:** Kafka events to notification_service
- **Status:** ✅ Designed, ⏳ Backend pending (Week 6)

### **Nice-to-Have Features (Future)**

#### **13. Voice & Video Calls**
- WebRTC integration
- Screen sharing
- Call recording
- **Timeline:** Post-MVP (Month 2-3)

#### **14. AI Features**
- Message summarization (via serp_llm)
- Smart replies
- Sentiment analysis
- Auto-translation
- **Timeline:** Post-MVP (Month 4-6)

#### **15. Mobile App**
- React Native app
- Offline support
- Push notifications
- **Timeline:** Post-MVP (Month 6-12)

---

## 🗂️ Feature Implementation Matrix

| Feature | Priority | Backend Status | Frontend Status | Week | Dependencies |
|---------|----------|----------------|-----------------|------|--------------|
| Channel Management | P0 | ⏳ Pending | ✅ Complete | 1-2 | PostgreSQL |
| Real-time Messaging | P0 | ⏳ Pending | ✅ Complete | 2-3 | WebSocket, Kafka |
| Message Reactions | P0 | ⏳ Pending | ✅ Complete | 4 | JSONB support |
| Thread Replies | P1 | ⏳ Pending | ✅ Partial | 4, 9 | Message parent_id |
| Presence & Online Status | P1 | ⏳ Pending | ✅ Complete | 3 | Redis |
| Typing Indicators | P1 | ⏳ Pending | ⏳ Pending | 3 | Redis, WebSocket |
| File Attachments | P1 | ⏳ Pending | ✅ Complete | 4, 9 | S3/MinIO |
| Full-text Search | P1 | ⏳ Pending | ⏳ Pending | 4, 9 | PostgreSQL tsvector |
| Activity Feed | P1 | ⏳ Pending | ⏳ Pending | 3 | Kafka consumer |
| Auto-Channel Creation | P0 | ⏳ Pending | N/A | 5 | Kafka integration |
| Permission Checks | P0 | ⏳ Pending | N/A | 6 | Service clients |
| Notification Integration | P0 | ⏳ Pending | N/A | 6 | Kafka producer |

**Legend:**
- P0: Critical (must-have for MVP)
- P1: High (should-have for launch)
- P2: Medium (nice-to-have)
- ✅ Complete, ⏳ Pending, 🚧 In Progress

---

## 📅 Development Phases

### **Phase 1: Foundation (Week 1-2)**
Core infrastructure and basic messaging.

### **Phase 2: Enhanced Features (Week 3-4)**
Rich messaging features and search.

### **Phase 3: Integration (Week 5-6)**
Connect with other SERP services.

### **Phase 4: Frontend (Week 7-8)**
Build user interface in serp_web.

### **Phase 5: Polish & Launch (Week 9-10)**
Testing, optimization, documentation, deployment.

---

## 📋 Phase 1: Foundation (Week 1-2)

### **Week 1: Project Setup & Database**

#### **Day 1-2: Project Scaffolding**
- [ ] Create service directory structure
  ```bash
  mkdir -p discuss_service/src/{cmd,config,core,infrastructure,ui,kernel}
  cd discuss_service
  go mod init github.com/serp/discuss-service
  ```

- [ ] Setup dependencies (go.mod)
  ```go
  require (
      github.com/gin-gonic/gin v1.9.1
      gorm.io/gorm v1.25.5
      gorm.io/driver/postgres v1.5.4
      github.com/redis/go-redis/v9 v9.3.0
      github.com/segmentio/kafka-go v0.4.45
      github.com/gorilla/websocket v1.5.1
      go.uber.org/fx v1.20.1
      go.uber.org/zap v1.26.0
      github.com/spf13/viper v1.17.0
  )
  ```

- [ ] Create .env.example
  ```env
  # Server
  SERVER_PORT=8092
  GIN_MODE=debug
  
  # Database
  DB_HOST=localhost
  DB_PORT=5432
  DB_USER=serp
  DB_PASSWORD=serp
  DB_NAME=discuss_service
  DB_SSL_MODE=disable
  
  # Redis
  REDIS_HOST=localhost:6379
  REDIS_PASSWORD=
  REDIS_DB=0
  
  # Kafka
  KAFKA_BROKERS=localhost:9092
  KAFKA_GROUP_ID=discuss_service
  
  # S3/MinIO
  S3_ENDPOINT=localhost:9000
  S3_ACCESS_KEY=minioadmin
  S3_SECRET_KEY=minioadmin
  S3_BUCKET=discuss-attachments
  
  # JWT
  JWT_SECRET=your-secret-key
  KEYCLOAK_URL=http://localhost:8180
  KEYCLOAK_REALM=serp
  ```

- [ ] Create Dockerfile
  ```dockerfile
  FROM golang:1.21-alpine AS builder
  WORKDIR /app
  COPY go.mod go.sum ./
  RUN go mod download
  COPY . .
  RUN CGO_ENABLED=0 GOOS=linux go build -o main src/main.go
  
  FROM alpine:latest
  RUN apk --no-cache add ca-certificates
  WORKDIR /root/
  COPY --from=builder /app/main .
  COPY --from=builder /app/.env .
  EXPOSE 8092
  CMD ["./main"]
  ```

- [ ] Create run-dev.sh and run-prod.sh

#### **Day 3-4: Database Schema**
- [ ] Create migration files (migrations/ directory)
- [ ] Implement migrations:
  - `001_create_channels_table.sql`
  - `002_create_messages_table.sql`
  - `003_create_channel_members_table.sql`
  - `004_create_attachments_table.sql`
  - `005_create_activity_feed_table.sql`
  - `006_create_indexes.sql`
  - `007_create_triggers.sql`

- [ ] Create GORM models (infrastructure/store/model/)
  - `channel_model.go`
  - `message_model.go`
  - `channel_member_model.go`
  - `attachment_model.go`
  - `activity_feed_model.go`

- [ ] Run migrations and verify schema
  ```bash
  psql -U serp -d discuss_service -f migrations/001_create_channels_table.sql
  # ... etc
  ```

#### **Day 5: Domain Entities & DTOs**
- [ ] Create entities (core/domain/entity/)
  - `base.go` - BaseEntity with common fields
  - `channel.go` - ChannelEntity
  - `message.go` - MessageEntity
  - `channel_member.go` - ChannelMemberEntity

- [ ] Create enums (core/domain/enum/)
  - `channel_type.go` - DIRECT, GROUP, TOPIC
  - `channel_role.go` - OWNER, ADMIN, MEMBER
  - `message_type.go` - TEXT, IMAGE, FILE, SYSTEM

- [ ] Create DTOs (core/domain/dto/)
  - Request DTOs (request/)
    - `create_channel_request.go`
    - `send_message_request.go`
  - Response DTOs (response/)
    - `channel_response.go`
    - `message_response.go`

### **Week 2: Core Services & APIs**

#### **Day 1-2: Repository Layer**
- [ ] Create port interfaces (core/port/store/)
  - `channel_port.go`
  - `message_port.go`
  - `channel_member_port.go`

- [ ] Implement adapters (infrastructure/store/adapter/)
  - `channel_adapter.go`
    - `Save(channel) -> channel`
    - `FindById(id, tenantID) -> channel`
    - `FindDirectChannel(user1, user2) -> channel`
  - `message_adapter.go`
    - `Save(message) -> message`
    - `FindByChannel(channelID, pagination) -> messages`
  - `channel_member_adapter.go`
    - `Save(member) -> member`
    - `FindByChannelAndUser(channelID, userID) -> member`
    - `IncrementUnread(memberID)`

- [ ] Create model ↔ entity mappers (infrastructure/store/mapper/)

#### **Day 3: Service Layer**
- [ ] Create services (core/service/)
  - `transaction_service.go`
    - `ExecuteInTransaction(func)`
  - `channel_service.go`
    - `CreateChannel(channel, memberIDs)`
    - `GetChannelsByUser(userID, filters)`
    - `ValidateChannelAccess(channelID, userID)`
  - `message_service.go`
    - `CreateMessage(message)`
    - `GetMessages(channelID, pagination)`
    - `ExtractMentions(content)`
  - `channel_member_service.go`
    - `AddMember(channelID, userID, role)`
    - `RemoveMember(channelID, userID)`
    - `IsMember(channelID, userID)`

- [ ] Write unit tests for services
  ```bash
  go test ./core/service/... -v
  ```

#### **Day 4: Use Case Layer**
- [ ] Create use cases (core/usecase/)
  - `channel_usecase.go`
    - `CreateChannel(request, userID, tenantID)`
    - `GetUserChannels(userID, tenantID, filters)`
    - `AddMembers(channelID, userIDs)`
  - `message_usecase.go`
    - `SendMessage(request, userID, tenantID)`
    - `GetMessages(channelID, userID, pagination)`
    - `EditMessage(messageID, content, userID)`

- [ ] Implement transaction orchestration
  ```go
  func (u *MessageUseCase) SendMessage(...) {
      result, err := u.txService.ExecuteInTransaction(func(tx *gorm.DB) {
          // 1. Validate permissions
          // 2. Create message
          // 3. Update unread counts
          // 4. Publish Kafka event
          // 5. Broadcast WebSocket
      })
  }
  ```

#### **Day 5: REST API Controllers**
- [ ] Create controllers (ui/controller/)
  - `channel_controller.go`
    - `CreateChannel(c *gin.Context)`
    - `GetChannels(c *gin.Context)`
    - `GetChannelById(c *gin.Context)`
  - `message_controller.go`
    - `SendMessage(c *gin.Context)`
    - `GetMessages(c *gin.Context)`
    - `EditMessage(c *gin.Context)`

- [ ] Create router (ui/router/router.go)
  ```go
  apiV1 := r.Group("/discuss/api/v1")
  {
      channels := apiV1.Group("/channels")
      {
          channels.POST("", channelController.CreateChannel)
          channels.GET("", channelController.GetChannels)
          channels.GET("/:id", channelController.GetChannelById)
      }
      
      messages := apiV1.Group("/channels/:channelId/messages")
      {
          messages.POST("", messageController.SendMessage)
          messages.GET("", messageController.GetMessages)
      }
  }
  ```

- [ ] Create middleware (ui/middleware/)
  - `auth.go` - Extract userID, tenantID from headers
  - `logging.go` - Request/response logging

- [ ] Register all modules in cmd/bootstrap/all.go (Uber FX)

- [ ] Test APIs with Postman/cURL
  ```bash
  # Create channel
  curl -X POST http://localhost:8092/discuss/api/v1/channels \
    -H "X-User-ID: 1" -H "X-Tenant-ID: 1" \
    -d '{"name":"Test","type":"GROUP","memberIds":[1,2]}'
  ```

---

## 📋 Phase 2: Enhanced Features (Week 3-4)

### **Week 3: WebSocket & Real-time**

#### **Day 1-2: WebSocket Hub**
- [ ] Copy WebSocket hub from notification_service
  - `core/websocket/hub.go`
  - `core/websocket/client.go`
  - `core/websocket/message.go`

- [ ] Adapt for discuss service:
  - Channel-based broadcasting
  - User presence tracking
  - Typing indicators

- [ ] Create WebSocket controller (ui/controller/websocket_controller.go)
  ```go
  func (c *WebSocketController) HandleConnection(ctx *gin.Context) {
      // 1. Upgrade HTTP to WebSocket
      // 2. Validate JWT token
      // 3. Register client in Hub
      // 4. Handle messages
  }
  ```

- [ ] Add WebSocket route
  ```go
  r.GET("/ws/discuss", wsController.HandleConnection)
  ```

#### **Day 3: Real-time Broadcasting**
- [ ] Implement message broadcasting
  ```go
  // In MessageUseCase.SendMessage
  message, err := messageService.CreateMessage(...)
  if err == nil {
      hub.BroadcastToChannel(message.ChannelID, message, []int64{userID})
  }
  ```

- [ ] Implement typing indicators
  - Redis TTL keys for typing state
  - Broadcast to channel members

- [ ] Test WebSocket connections
  ```bash
  # Use wscat or browser WebSocket
  wscat -c "ws://localhost:8092/ws/discuss?token=<JWT>"
  ```

#### **Day 4-5: Presence & Activity Feed**
- [ ] Create presence service (core/service/presence_service.go)
  - Redis-based presence tracking
  - `SetOnline(userID)`
  - `SetOffline(userID)`
  - `GetOnlineUsers(userIDs)`

- [ ] Create activity feed service
  - Aggregate activities from all modules
  - `CreateActivity(actionType, entityType, entityID)`
  - `GetActivityFeed(userID, filters)`

- [ ] Add activity feed endpoints
  ```go
  apiV1.GET("/activity-feed", activityController.GetActivityFeed)
  ```

### **Week 4: Search & Attachments**

#### **Day 1-2: Full-text Search**
- [ ] Create search service (core/service/search_service.go)
  - PostgreSQL full-text search using tsvector
  - `SearchMessages(query, filters)`
  - `SearchChannels(query, filters)`

- [ ] Implement search endpoints
  ```go
  apiV1.GET("/search/messages", searchController.SearchMessages)
  apiV1.GET("/search/channels", searchController.SearchChannels)
  ```

- [ ] Test search queries
  ```sql
  SELECT * FROM messages 
  WHERE search_vector @@ to_tsquery('report & q1');
  ```

#### **Day 3-4: File Attachments**
- [ ] Create S3 client (infrastructure/client/s3/storage.go)
  - `UploadFile(file, bucket, key)`
  - `GeneratePresignedURL(key, expiry)`
  - `DeleteFile(key)`

- [ ] Create attachment service
  - Virus scanning integration (optional)
  - Thumbnail generation for images
  - File metadata extraction

- [ ] Add upload endpoint
  ```go
  apiV1.POST("/attachments/upload", attachmentController.Upload)
  ```

- [ ] Test file upload with multipart/form-data

#### **Day 5: Reactions & Threads**
- [ ] Implement message reactions
  - JSONB column for reactions
  - Add/remove reaction endpoints
  - WebSocket broadcast on reaction changes

- [ ] Implement threaded replies
  - `parent_id` field in messages
  - `thread_count` denormalized counter
  - Get thread replies endpoint

---

## 📋 Phase 3: Integration (Week 5-6)

### **Week 5: Kafka Integration**

#### **Day 1-2: Kafka Producer**
- [ ] Create Kafka client (infrastructure/client/kafka/producer.go)
  ```go
  type KafkaProducer struct {
      writer *kafka.Writer
  }
  
  func (p *KafkaProducer) SendMessage(topic, key, value) error
  ```

- [ ] Publish events on message actions
  - `MESSAGE_SENT` → notification_service
  - `MENTION_RECEIVED` → notification_service
  - `CHANNEL_CREATED` → logging_tracker

#### **Day 3-5: Kafka Consumer**
- [ ] Create Kafka consumer (infrastructure/client/kafka/consumer.go)
  ```go
  type KafkaConsumer struct {
      reader *kafka.Reader
  }
  
  func (c *KafkaConsumer) StartConsuming(handler)
  ```

- [ ] Create event handlers (ui/kafka/entity_event_consumer.go)
  - Listen to `ENTITY_EVENTS` topic
  - Handle events from CRM, PTM, Purchase services:
    - `CUSTOMER_CREATED` → Create TOPIC channel
    - `TASK_CREATED` → Create TOPIC channel
    - `ORDER_CREATED` → Create TOPIC channel

- [ ] Implement auto-channel creation
  ```go
  func handleCustomerCreated(event) {
      channel := &ChannelEntity{
          Type: TOPIC,
          EntityType: "customer",
          EntityID: event.CustomerID,
          Name: fmt.Sprintf("Customer: %s", event.CustomerName),
      }
      channelUseCase.CreateChannel(channel, event.MemberIDs)
  }
  ```

### **Week 6: Service-to-Service Integration**

#### **Day 1-2: Notification Service Integration**
- [ ] Create notification client (infrastructure/client/notification/)
  - Send push notifications for mentions
  - Send email notifications for missed messages

- [ ] Update API Gateway routes
  - Add discuss service routes to api_gateway/src/config/config.yaml

#### **Day 3: Permission Checks for TOPIC Channels**
- [ ] Create entity permission clients
  - `crm_client.go` - Check customer/lead access
  - `ptm_client.go` - Check task access
  - `purchase_client.go` - Check order access

- [ ] Implement permission validation
  ```go
  func (s *ChannelService) CanAccessTopicChannel(channel, userID) {
      switch channel.EntityType {
      case "customer":
          return s.crmClient.CanViewCustomer(channel.EntityID, userID)
      }
  }
  ```

#### **Day 4-5: Testing Integration**
- [ ] End-to-end integration tests
  - Create customer in CRM → Verify TOPIC channel created
  - Send message with @mention → Verify notification sent
  - User joins channel → Verify presence broadcast

- [ ] Test with docker-compose
  ```bash
  docker-compose -f docker-compose.dev.yml up -d
  # Start all services
  ```

---

## 📋 Phase 4: Frontend (Week 7-8)

### **Week 7: Frontend Module Setup**

#### **Day 1: Module Structure**
- [x] Create module directory
  ```
  serp_web/src/modules/discuss/
  ├── api/
  │   └── discussApi.ts
  ├── components/
  │   ├── ChannelList.tsx
  │   ├── ChatWindow.tsx
  │   ├── MessageInput.tsx
  │   └── MessageList.tsx
  ├── store/
  │   └── discussSlice.ts
  └── types/
      └── discuss.types.ts
  ```

#### **Day 2-3: RTK Query API**
- [x] Create API endpoints (api/discussApi.ts)
  ```typescript
  export const discussApi = api.injectEndpoints({
    endpoints: (builder) => ({
      getChannels: builder.query<...>({
        query: (filters) => ({
          url: '/channels',
          params: filters,
        }),
        extraOptions: { service: 'discuss' },
      }),
      sendMessage: builder.mutation<...>({
        query: ({ channelId, content }) => ({
          url: `/channels/${channelId}/messages`,
          method: 'POST',
          body: { content },
        }),
      }),
    }),
  });
  ```

#### **Day 4-5: WebSocket Integration**
- [ ] Update WebSocket middleware
  - Connect to discuss WebSocket
  - Handle discuss events (NEW_MESSAGE, TYPING_INDICATOR, etc.)

- [ ] Create WebSocket hook
  ```typescript
  export function useDiscussWebSocket() {
    const dispatch = useDispatch();
    
    useEffect(() => {
      dispatch(wsConnect());
      return () => dispatch(wsDisconnect());
    }, []);
    
    useDiscussEvent('NEW_MESSAGE', (message) => {
      dispatch(discussApi.util.updateQueryData(...));
    });
  }
  ```

### **Week 8: UI Components**

#### **Day 1-2: Channel List Sidebar**
- [x] Create ChannelList component ✅
  - Display user's channels
  - Group by type (DIRECT, GROUP, TOPIC)
  - Show unread badges
  - Handle channel selection

- [x] Use Shadcn UI components ✅
  - `ScrollArea` for scrollable list
  - `Badge` for unread counts
  - `Avatar` for user icons

- [x] Created supporting components ✅
  - `ChannelItem.tsx` - Individual channel item with hover effects, unread badges, avatars
  - `ChannelGroupHeader.tsx` - Collapsible group headers with counts
  - Component exports in `index.ts`

- [x] Code quality checks ✅
  - TypeScript: No errors
  - ESLint: Passed
  - Prettier: Formatted

#### **Day 3-4: Chat Window**
- [x] Create ChatWindow component ✅
  - Display channel details in header (avatar, name, member count, online status)
  - Render MessageList with infinite scroll
  - Show typing indicators (placeholder)
  - MessageInput at bottom with reply/edit state management
  - Action buttons: Phone, Video, Search, Info
  - RTK Query integration for data fetching

- [x] Create MessageList component ✅
  - Infinite scroll (load older messages using IntersectionObserver)
  - Group messages by date with visual dividers
  - Group consecutive messages from same sender
  - Auto-scroll to bottom on new messages
  - Loading states (initial + top loading)
  - Empty and error states
  - ScrollArea integration

- [x] Create MessageInput component ✅
  - Auto-resize textarea (max 200px height)
  - Formatting toolbar (Bold **, Italic *, Code `)
  - @mention, emoji, file attachment buttons
  - Reply indicator with cancel
  - Edit indicator with cancel
  - Keyboard shortcuts (Enter to send, Shift+Enter newline, Esc cancel)
  - Gradient send button with disabled state

- [x] Create MessageItem component ✅
  - Rich message bubbles with gradient styling (own) / bordered (others)
  - Avatar display with fallback initials
  - Timestamp formatting (relative time)
  - Hover actions: Reply, Edit (own), Delete (own), More
  - Reactions display (emoji + count badges)
  - Reply indicator for threaded messages
  - Edited indicator, read receipts
  - Message grouping support

- [x] Integration and quality ✅
  - Updated demo page with ChatWindow
  - Component exports in `index.ts`
  - TypeScript: No errors (fixed 16 type mismatches)
  - ESLint: Passed
  - Prettier: Formatted

**Components created:**
- `ChatWindow.tsx` (230 lines) - Main container with header, list, input
- `MessageList.tsx` (270 lines) - Scrollable message container with infinite scroll
- `MessageInput.tsx` (240 lines) - Rich text input with formatting toolbar
- `MessageItem.tsx` (220 lines) - Individual message bubble with interactions

**Total code:** 960 lines of React/TypeScript
**Design:** Bold gradient aesthetic (violet→fuchsia), smooth transitions, rich interactions

#### **Day 5: Polish & Features**
- [x] Fixed message order (oldest first) ✅
- [x] Add features ✅:
  - [x] Emoji picker (Popover) - EmojiPicker component with 6 categories, 300+ emojis
  - [x] Message reactions - Interactive reaction buttons with gradient styling, add/remove functionality
  - [x] Thread replies - ThreadIndicator component for reply counts
  - [x] Online status indicators - OnlineStatusIndicator with pulse animation for online status
  - [x] Enhanced MessageItem with reaction picker and clickable reactions

- [x] Code quality checks ✅
  - TypeScript: No errors
  - ESLint: Passed
  - Prettier: Formatted

- [x] Bug fixes ✅ (December 30, 2025):
  - Fixed reaction mutation errors (immutable object updates)
  - Fixed EmojiPicker dark mode theme (missing dark: classes)
  - Both issues resolved in discussApi.ts and EmojiPicker.tsx

**Components created:**
- `EmojiPicker.tsx` (394 lines) - Tabbed emoji picker with 6 categories, 300+ emojis, full dark mode support
- `ReactionPicker.tsx` (60 lines) - Quick reaction picker with 10 common emojis
- `ThreadIndicator.tsx` (50 lines) - Thread reply counter badge
- `OnlineStatusIndicator.tsx` (100 lines) - Online/away/busy/offline indicators with pulse animation
- Updated `MessageItem.tsx` - Added reaction handling, interactive reaction badges
- Updated `MessageInput.tsx` - Integrated EmojiPicker into toolbar
- Updated `ChatWindow.tsx` - Added reaction mutations and online status display
- Updated `MessageList.tsx` - Pass reaction handlers to MessageItem
- Fixed `discussApi.ts` - Immutable updates for addReaction/removeReaction mutations

**Total code:** ~1,350 lines (Day 3-5 combined)
**Design highlights:** 
- Gradient-based UI (violet→fuchsia) maintained
- Interactive reactions with scale animations
- Pulse animation for online status
- Distinctive emoji picker with category tabs
- Full dark mode support across all components
- Immutable state management following Redux best practices

---

## 📋 Phase 4.5: Frontend Advanced Features (Week 9)

### **Week 9: File Attachments & Search**

#### **Day 1-2: File Upload Integration**
- [x] Create attachment upload mutation (discussApi.ts) ✅
  - Added `uploadAttachment` mutation with mock file upload
  - Returns Attachment object with blob URLs
  - Simulates 1-second upload delay with progress tracking
  - Automatic virus scan status (mocked as CLEAN)
  - Thumbnail generation for images

- [x] Create AttachmentUploader component ✅
  - Drag & drop zone with visual feedback (scale animation, gradient highlight)
  - File type validation (images, PDFs, docs, archives)
  - Size validation (100MB max, configurable)
  - Multiple file upload support
  - Real-time upload progress bars with gradient animation
  - File preview thumbnails (images) and icons (documents)
  - Success/error states with color-coded UI
  - Auto-remove successful uploads after 2 seconds
  - Manual remove button for all files
  - **Design highlights:**
    - Gradient-based upload zone (violet→fuchsia on drag)
    - Smooth scale transitions on drag enter/hover
    - Color-coded status: violet (uploading), green (success), red (error)
    - File size formatter utility
    - File icon detection (Image, FileText, Archive, File)
  - **Code:** 340 lines of React/TypeScript

- [x] Create AttachmentPreview component ✅
  - Image preview with hover overlay
  - Lightbox dialog for full-screen image viewing
  - Gallery navigation (prev/next) for multiple images
  - Download button for all file types
  - PDF preview with "Open PDF" button
  - Document preview with file icon and metadata
  - File size display in all previews
  - Image counter in lightbox (1/3 format)
  - **Design highlights:**
    - Gradient overlay on image hover with zoom/download buttons
    - Full-screen black backdrop lightbox with blur effects
    - Navigation buttons with backdrop-blur-sm effect
    - Bottom info bar with transparent background
    - Smooth transitions and hover effects
    - Responsive layout for all file types
  - **Code:** 260 lines of React/TypeScript

- [x] Update MessageInput with file attachment ✅
  - File picker button in toolbar (Paperclip icon)
  - Hidden file input with multiple file support
  - Accept filter: images, PDFs, docs, zip files
  - Upload on file select with progress tracking
  - Attachment preview chips before sending
    - Image thumbnails (10x10 rounded preview)
    - Document icons with violet background
    - File name truncation (max 120px)
    - File size display
    - Remove button (X icon)
  - Updated send logic to include attachments
  - Disabled attach button during upload (opacity 50%)
  - Visual attachment list with violet-themed chips
  - Auto-clear attachments after send
  - **Code additions:** ~100 lines added to MessageInput

- [x] Update MessageItem to display attachments ✅
  - Integrated AttachmentPreview in message bubble
  - Attachment gallery support (all images in message)
  - Proper spacing (mt-3) after message content
  - Passes all attachments for gallery navigation
  - Works with both own (gradient) and other (bordered) message styles
  - **Code additions:** ~15 lines added to MessageItem

- [x] Code quality checks ✅
  - TypeScript: No errors ✓
  - ESLint: 2 warnings (next/image - acceptable for blob URLs)
  - Prettier: Formatted ✓
  - Component exports updated in index.ts

**Components created (Day 1-2):**
- `AttachmentUploader.tsx` (340 lines) - Drag & drop file upload with progress
- `AttachmentPreview.tsx` (260 lines) - Image lightbox, PDF viewer, document preview
- Updated `MessageInput.tsx` (+100 lines) - File attachment support
- Updated `MessageItem.tsx` (+15 lines) - Display attachments in messages
- Updated `discussApi.ts` - Upload mutation with mock implementation

**Total code (Day 1-2):** ~715 lines of production-ready React/TypeScript

**Design philosophy:**
- Bold gradient aesthetic (violet→fuchsia) maintained throughout
- Smooth transitions and hover effects (200-300ms)
- Color-coded states for instant visual feedback
- Micro-interactions: scale on hover, pulse on drag
- Accessible file size formatting
- Mobile-friendly touch targets (48x48px minimum)

**Features implemented:**
- ✅ Drag & drop upload zone
- ✅ File validation (type + size)
- ✅ Upload progress tracking
- ✅ Image thumbnails in previews
- ✅ Lightbox gallery navigation
- ✅ Download functionality
- ✅ PDF preview support
- ✅ Multiple file upload
- ✅ Error handling with visual feedback
- ✅ Attachment display in messages
- ✅ File metadata display (name, size, type)

**Next steps:**
- [ ] Integrate real S3/MinIO backend for file storage
- [ ] Add virus scanning integration (ClamAV)
- [ ] Implement file compression for large images
- [ ] Add video preview support
- [ ] Implement attachment search in messages

#### **Day 3-4: Message Search**
- [x] Create search mutation (discussApi.ts) ✅
  - Added `searchMessages` query with full-text search
  - Filter by channel, user, date range, attachment, message type
  - Group results by channel with relevance scoring
  - Pagination support (10 results per page)
  - Highlight matching text in search results
  - Mock implementation with MOCK_MESSAGES

- [x] Create SearchBar component ✅
  - Debounced search input (500ms delay)
  - Advanced filter popover with:
    - Date range picker (from/to dates)
    - Message type filter (TEXT, IMAGE, FILE)
    - Has attachments toggle
  - Recent searches history (localStorage, max 10)
  - Keyboard shortcut hint (Cmd+K / Ctrl+K)
  - Clear button
  - Active filter count badge
  - **Design:** Gradient filter button with badge, subtle hover effects

- [x] Create SearchResults component ✅
  - Grouped by channel with sticky headers
  - Channel type icons (Hash, Users, FolderKanban)
  - Message preview with highlighted search terms (yellow mark)
  - Avatar with gradient fallback
  - Message metadata: timestamp, type icon, attachment count
  - Reaction previews (show first 3 + count)
  - Infinite scroll support with IntersectionObserver
  - Empty state with gradient background
  - Navigate to message on click (ChevronRight icon)
  - **Code:** 265 lines of React/TypeScript

- [x] Create SearchDialog component (Shadcn Dialog) ✅
  - Full-screen modal experience (85vh height)
  - Cmd+K / Ctrl+K keyboard shortcut to open
  - Escape to close
  - Search analytics display:
    - Total results count
    - Search time (mock 0.3s)
  - Integrated SearchBar + SearchResults
  - Recent searches persistence (localStorage)
  - Infinite scroll pagination
  - Footer gradient accent bar
  - Auto-close on result click
  - **Code:** 195 lines of React/TypeScript

- [x] Update ChatWindow with search integration ✅
  - Added SearchDialog component
  - Search button in header (opens dialog)
  - OnResultClick handler (TODO: scroll to message)
  - **Code additions:** ~15 lines

- [x] Update component exports (index.ts) ✅
  - Export SearchBar, SearchResults, SearchDialog

- [x] Code quality checks ✅
  - TypeScript: No errors ✓
  - ESLint: No errors ✓
  - Prettier: Formatted ✓

**Components created (Day 3-4):**
- `SearchBar.tsx` (265 lines) - Debounced input with advanced filters
- `SearchResults.tsx` (265 lines) - Grouped results with infinite scroll
- `SearchDialog.tsx` (195 lines) - Full-screen search modal with Cmd+K
- Updated `ChatWindow.tsx` (+15 lines) - Search button integration
- Updated `discussApi.ts` (+140 lines) - searchMessages query
- Updated `types/index.ts` (+25 lines) - Search types

**Total code (Day 3-4):** ~905 lines of production-ready React/TypeScript

**Design highlights:**
- **Gradient aesthetic** maintained (violet→fuchsia accents)
- **Advanced filtering** with popover UI (date range, type, attachments)
- **Smart search** with text highlighting (yellow marks)
- **Keyboard-first** interaction (Cmd+K, Escape, debounce)
- **Recent searches** with localStorage persistence
- **Search analytics** (result count, search time)
- **Infinite scroll** for performance with large result sets
- **Responsive layout** for all screen sizes

**Features implemented:**
- ✅ Full-text search across all channels
- ✅ Advanced filters (channel, user, date, type, attachments)
- ✅ Debounced input (500ms) to reduce API calls
- ✅ Text highlighting in search results
- ✅ Grouped results by channel
- ✅ Recent searches history (max 10)
- ✅ Keyboard shortcuts (Cmd+K to open, Escape to close)
- ✅ Search analytics display
- ✅ Infinite scroll pagination
- ✅ Empty state handling
- ✅ Loading states

**Next steps:**
- [x] Implement scroll to message in context ✅ (COMPLETED)
  - Added `MessageListRef` with `scrollToMessage` method
  - Exposed via `useImperativeHandle` in MessageList
  - Wrap each MessageItem with ref div for scrolling
  - Highlight message briefly (2s violet background)
  - Smooth scroll to center of viewport
  - Channel validation in ChatWindow (alert if different channel)
- [ ] Add search result caching
- [ ] Consider adding search suggestions/autocomplete
- [ ] Add search history clear button

#### **Day 5: Thread Replies Enhancement**
- [ ] Create ThreadView component
  - Slide-in panel from right
  - Parent message at top
  - Threaded replies below
  - Separate MessageInput for thread
  - Thread reply count badge

- [ ] Update MessageItem with thread actions
  - "View thread" button when threadCount > 0
  - Reply count indicator
  - Open ThreadView on click

- [ ] Thread mutations (discussApi.ts)
  - `getThreadReplies` query
  - `sendThreadReply` mutation
  - Optimistic updates for thread replies

- [ ] Thread notifications
  - Notify parent message author on reply
  - Notify thread participants on new replies

---

## 📋 Phase 5: Polish & Launch (Week 10-11)

### **Week 10: Testing & Optimization**

#### **Day 1-2: Unit Tests**
- [ ] Backend tests
  ```bash
  go test ./... -v -cover
  # Target: >80% coverage
  ```
  - Service layer tests (mocked repos)
  - UseCase tests (mocked services)
  - Adapter tests (integration with DB)

- [ ] Frontend tests
  ```bash
  npm run test
  ```
  - Component tests (React Testing Library)
    - ChannelList rendering & interactions
    - MessageItem reactions & hover actions
    - MessageInput toolbar & emoji picker
    - ChatWindow integration tests
  - RTK Query tests (MSW mocking)
    - Mutation tests (sendMessage, addReaction)
    - Query tests (getChannels, getMessages)
    - Cache invalidation tests
  - E2E tests (Playwright)
    - Full conversation flow
    - File upload flow
    - Search functionality

#### **Day 3: Load Testing**
- [ ] WebSocket load test
  ```go
  // hub_load_test.go
  func TestConcurrentConnections(t *testing.T) {
      for i := 0; i < 10000; i++ {
          go connectClient()
      }
  }
  ```

- [ ] Message throughput test
  - Target: 1000 messages/second
  - Measure: Latency p50, p95, p99

- [ ] Use tools: k6, Artillery, or custom Go scripts

#### **Day 4-5: Performance Optimization**
- [ ] Database optimization
  - Add missing indexes
  - Optimize N+1 queries
  - Enable query caching

- [ ] Redis caching
  - Cache hot channels
  - Cache recent messages (last 100 per channel)
  - Cache user presence

- [ ] WebSocket optimization
  - Connection pooling
  - Message batching
  - Compression enabled

- [ ] Frontend optimization
  - Virtual scrolling for long message lists (react-window)
  - Lazy loading for images and attachments
  - Code splitting for emoji picker
  - React.memo for MessageItem to prevent re-renders
  - Debounce typing indicators
  - Image optimization (WebP format, lazy loading)

### **Week 11: Documentation & Deployment**

#### **Day 1-2: Documentation**
- [ ] API documentation (Swagger/OpenAPI)
  ```yaml
  openapi: 3.0.0
  paths:
    /channels:
      post:
        summary: Create channel
        requestBody: ...
  ```

- [ ] README.md for discuss_service
  - Setup instructions
  - Environment variables
  - Running locally
  - Running tests

- [ ] Developer guide
  - Architecture overview
  - Adding new features
  - Troubleshooting

#### **Day 3: Monitoring & Alerts**
- [ ] Add Prometheus metrics
  ```go
  var (
      messagesTotal = prometheus.NewCounterVec(...)
      messageLatency = prometheus.NewHistogramVec(...)
      wsConnections = prometheus.NewGauge(...)
  )
  ```

- [ ] Create Grafana dashboard
  - Message throughput
  - WebSocket connections
  - API latency
  - Error rates

- [ ] Setup alerts
  - High error rate (>5%)
  - Slow responses (>500ms p95)
  - WebSocket connection drops

#### **Day 4: Deployment**
- [ ] Deploy to staging
  ```bash
  docker build -t discuss_service:latest .
  docker push registry/discuss_service:latest
  kubectl apply -f k8s/discuss-service.yaml
  ```

- [ ] Run smoke tests on staging
  - Create channel
  - Send message
  - WebSocket connection
  - File upload

- [ ] Update API Gateway to route to discuss service

#### **Day 5: Production Launch**
- [ ] Deploy to production
  - Blue-green deployment
  - Monitor metrics
  - Gradual rollout (10% → 50% → 100%)

- [ ] User acceptance testing
  - Internal team testing
  - Gather feedback
  - Fix critical bugs

- [ ] Launch announcement
  - Update SERP documentation
  - Send email to users
  - Provide user guide

---

## ✅ Completion Checklist

### **Infrastructure**
- [ ] PostgreSQL database setup
- [ ] Redis cache configured
- [ ] Kafka topics created
- [ ] S3/MinIO for file storage
- [ ] API Gateway routes added

### **Backend (discuss_service)**
- [ ] Database schema migrated
- [ ] All entities & models created
- [ ] Repository layer (adapters)
- [ ] Service layer (business logic)
- [ ] UseCase layer (orchestration)
- [ ] REST API endpoints
- [ ] WebSocket server
- [ ] Kafka producer/consumer
- [ ] Integration with other services
- [ ] Unit tests (>80% coverage)
- [ ] Integration tests
- [ ] Load tests

### **Frontend (serp_web)**
- [ ] Discuss module structure
- [ ] RTK Query API
- [ ] WebSocket integration
- [ ] ChannelList component
- [ ] ChatWindow component
- [ ] MessageList component
- [ ] MessageInput component
- [ ] UI components (Shadcn)
- [ ] Pages & routing
- [ ] Unit tests

### **DevOps**
- [ ] Dockerfile
- [ ] docker-compose.yml (dev)
- [ ] Kubernetes manifests (prod)
- [ ] CI/CD pipeline
- [ ] Prometheus metrics
- [ ] Grafana dashboard
- [ ] Logging (structured logs)
- [ ] Error tracking (Sentry)

### **Documentation**
- [ ] API specification
- [ ] WebSocket protocol
- [ ] Database schema
- [ ] Architecture design
- [ ] README.md
- [ ] User guide
- [ ] Developer guide

### **Launch**
- [ ] Staging deployment
- [ ] Production deployment
- [ ] User training
- [ ] Launch announcement

---

## 🚨 Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| WebSocket scaling issues | High | Use Redis Pub/Sub for cross-instance messaging |
| Database performance | High | Indexes, caching, connection pooling |
| Message loss during failures | Critical | Kafka for reliability, DB persistence |
| Security vulnerabilities | Critical | JWT auth, input validation, SQL injection prevention |
| Integration breaking changes | Medium | Versioned APIs, backward compatibility |
| File storage costs | Medium | Compression, retention policies, cleanup jobs |

---

## 📊 Resource Requirements

### **Development Team**
- 2 Backend engineers (Go)
- 1 Frontend engineer (React/TypeScript)
- 1 DevOps engineer (part-time)
- 1 QA engineer (part-time)

### **Infrastructure**
- Development: 4 CPU, 8GB RAM, 100GB storage
- Staging: 8 CPU, 16GB RAM, 500GB storage
- Production: 16 CPU, 32GB RAM, 2TB storage (initial)

### **Costs (estimated monthly)**
- EC2/Compute: $500
- RDS PostgreSQL: $200
- ElastiCache Redis: $150
- S3 storage: $100
- Load balancer: $50
- Total: ~$1000/month (initial)

---

## 🎯 Post-Launch Roadmap

### **Phase 6: Advanced Features (Month 2-3)**
- [ ] Voice calls (WebRTC)
- [ ] Video calls
- [ ] Screen sharing
- [ ] Voice messages
- [ ] Message translation
- [ ] Code syntax highlighting
- [ ] Polls & surveys

### **Phase 7: AI Integration (Month 4-6)**
- [ ] AI-powered search (serp_llm)
- [ ] Message summarization
- [ ] Smart replies
- [ ] Sentiment analysis
- [ ] Auto-categorization

### **Phase 8: Mobile App (Month 6-12)**
- [ ] React Native app
- [ ] Push notifications
- [ ] Offline support
- [ ] File sharing

---

## 📝 Success Metrics (After 3 months)

- ✅ **10,000+ messages/day** sent
- ✅ **500+ active users** daily
- ✅ **95% user satisfaction** (survey score >4/5)
- ✅ **30% reduction** in external chat tools usage (Slack, Teams)
- ✅ **50% faster** cross-module communication
- ✅ **99.9% uptime** maintained
- ✅ **<100ms** message delivery (p95)

---

## 📚 Summary

This implementation plan provides:
- ✅ **Comprehensive feature analysis** - 15 features analyzed with priorities
- ✅ **Clear timeline** - 11 weeks to production-ready MVP
- ✅ **Phased approach** - Incremental delivery with milestones
- ✅ **Detailed tasks** - Day-by-day breakdown for each phase
- ✅ **Feature matrix** - Status tracking for backend & frontend
- ✅ **Testing strategy** - Unit, integration, load, E2E tests
- ✅ **Risk mitigation** - Identified risks with solutions
- ✅ **Resource planning** - Team, infrastructure, costs
- ✅ **Post-launch roadmap** - Continuous improvement plan

### **Key Achievements (As of Week 9 Day 1-2):**
- ✅ **Frontend foundation complete** - Types, API, mock data
- ✅ **Channel List implemented** - 3 components, 465 lines
- ✅ **Chat Window implemented** - 4 components, 960 lines
- ✅ **Polish features complete** - Reactions, emoji picker, threads, presence
- ✅ **File attachments implemented** - Upload, preview, lightbox (Day 1-2 ✅)
- ✅ **Bug fixes completed** - Immutable updates, dark mode theme
- ✅ **Total frontend code** - ~2,500 lines of production-ready React/TypeScript

### **Next Immediate Steps:**
1. **Week 9 Day 3-4:** Implement message search (frontend)
2. **Week 9 Day 5:** Implement thread view enhancement
3. **Week 1-6:** Begin backend development (Go service)
4. **Week 10-11:** Testing, optimization, and deployment

### **Technical Highlights:**
- **Architecture:** Clean Architecture with Uber FX dependency injection
- **Real-time:** WebSocket Hub with Redis Pub/Sub for scaling
- **Storage:** PostgreSQL + Redis caching + S3 for files
- **Integration:** Kafka for event-driven communication
- **Frontend:** Next.js 15 + RTK Query + Shadcn UI
- **Design:** Bold gradient aesthetic (violet→fuchsia) with dark mode

**Next Steps:**
1. ✅ Review and approve feature analysis
2. ⏳ Allocate team resources for backend development
3. ⏳ Setup development environment (PostgreSQL, Redis, Kafka)
4. ⏳ Begin Phase 1: Foundation (Week 1-2)
5. 🚧 Continue Phase 4.5: Frontend Advanced Features (Week 9)

---

**Document Status:** ✅ Updated with Feature Analysis  
**Last Updated:** December 30, 2025  
**Prepared By:** QuanTuanHuy
