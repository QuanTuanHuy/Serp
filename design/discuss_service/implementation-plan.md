# Discuss Service - Implementation Plan

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Development Roadmap  
**Date:** December 2025  
**Estimated Timeline:** 8-10 weeks  

## 🎯 Project Overview

**Goal:** Build a production-ready unified communication module for SERP ERP system.

**Success Metrics:**
- ✅ Support 10,000+ concurrent WebSocket connections
- ✅ <100ms message delivery latency (p95)
- ✅ 99.9% uptime
- ✅ Zero message loss
- ✅ Full integration with existing SERP modules

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
- [ ] Create ChannelList component
  - Display user's channels
  - Group by type (DIRECT, GROUP, TOPIC)
  - Show unread badges
  - Handle channel selection

- [ ] Use Shadcn UI components
  - `ScrollArea` for scrollable list
  - `Badge` for unread counts
  - `Avatar` for user icons

#### **Day 3-4: Chat Window**
- [ ] Create ChatWindow component
  - Display channel details in header
  - Render MessageList
  - Show typing indicators
  - MessageInput at bottom

- [ ] Create MessageList component
  - Infinite scroll (load older messages)
  - Group messages by date
  - Show sender avatars
  - Display reactions

- [ ] Create MessageInput component
  - Rich text editor (markdown support)
  - @mention autocomplete (Command component)
  - File attachment button
  - Send button

#### **Day 5: Polish & Features**
- [ ] Add features:
  - Emoji picker (Popover)
  - Message reactions
  - Thread replies
  - Read receipts (for DIRECT)
  - Online status indicators

- [ ] Add pages:
  - `/discuss` - Main discuss page
  - `/discuss/channels/:id` - Specific channel

---

## 📋 Phase 5: Polish & Launch (Week 9-10)

### **Week 9: Testing & Optimization**

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
  - RTK Query tests (MSW mocking)

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

### **Week 10: Documentation & Deployment**

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
- ✅ **Clear timeline** (10 weeks to MVP)
- ✅ **Phased approach** (incremental delivery)
- ✅ **Detailed tasks** (day-by-day breakdown)
- ✅ **Testing strategy** (unit, integration, load)
- ✅ **Risk mitigation** (identified and addressed)
- ✅ **Resource planning** (team, infra, costs)
- ✅ **Post-launch roadmap** (continuous improvement)

**Next Steps:**
1. Review and approve this plan
2. Allocate team resources
3. Setup development environment
4. Begin Phase 1: Foundation (Week 1)

---

**Document Status:** ✅ Ready for Review  
**Last Updated:** December 30, 2025  
**Prepared By:** QuanTuanHuy
