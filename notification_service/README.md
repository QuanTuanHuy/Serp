# Notification Service

## Overview

The **Notification Service** is a real-time notification system that delivers instant notifications to users through multiple channels. Built with Go and WebSocket technology, it provides in-app notifications with real-time delivery, user preference management, and event-driven architecture for seamless integration with other SERP services.

**Technology Stack:**
- **Language**: Go 1.25.0
- **Framework**: Gin (HTTP/REST framework)
- **Database**: PostgreSQL (persistence), Redis (caching)
- **Messaging**: Apache Kafka (event consumption)
- **Real-time**: WebSocket (Gorilla WebSocket)
- **Dependency Injection**: Uber FX
- **Logging**: Uber Zap
- **Authentication**: JWT via Keycloak (through API Gateway)
- **Architecture**: Clean Architecture / Hexagonal Architecture

**Service Configuration:**
- **Context Path**: `/notification-service`
- **Access**: All requests via API Gateway at `http://localhost:8080/notification-service`

## Architecture

The service follows Clean Architecture principles with clear separation of concerns:

```
notification_service/
├── ui/                          # Presentation Layer
│   ├── controller/              # REST API & WebSocket controllers
│   │   ├── NotificationController
│   │   ├── PreferenceController
│   │   └── WebSocketController
│   ├── kafka/                   # Kafka event consumers
│   │   ├── UserNotificationHandler
│   │   └── MessageProcessingMiddleware
│   ├── middleware/              # HTTP middleware (JWT, CORS)
│   └── router/                  # Route registration
│
├── core/                        # Business Logic Layer
│   ├── domain/                  # Domain models
│   │   ├── entity/              # Business entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── enum/                # Enumerations (NotificationType, Priority, etc.)
│   │   └── constant/            # Domain constants (event types, topics)
│   ├── usecase/                 # Business use cases
│   │   ├── NotificationUseCase
│   │   └── PreferenceUseCase
│   ├── service/                 # Domain services
│   │   ├── NotificationService
│   │   ├── PreferenceService
│   │   ├── DeliveryService
│   │   ├── IdempotencyService
│   │   └── TransactionService
│   ├── websocket/               # WebSocket hub & client management
│   │   ├── Hub (connection management)
│   │   ├── Client (WebSocket client lifecycle)
│   │   └── Message (WebSocket message types)
│   └── port/                    # Interface definitions
│       ├── store/               # Repository interfaces
│       └── client/              # External client interfaces
│
└── infrastructure/              # External Adapters Layer
    ├── store/                   # PostgreSQL repositories
    │   ├── model/               # GORM entities
    │   ├── mapper/              # Entity ↔ Model mappers
    │   └── adapter/             # Repository implementations
    └── client/                  # External clients
        ├── RedisAdapter
        ├── KafkaProducerAdapter
        └── KafkaConsumer
```

**Data Flow:**

1. **REST API Flow**:
   ```
   API Gateway → Controller → UseCase → Service → Repository → PostgreSQL
                                                → Redis (caching)
   ```

2. **Kafka Event Consumption Flow**:
   ```
   Kafka Topic → Consumer Handler → UseCase → Service → Repository → PostgreSQL
                                                                    → WebSocket Hub
   ```

3. **WebSocket Real-Time Delivery Flow**:
   ```
   Client connects → Hub registers client → Notification created → 
   Hub broadcasts → All user's devices receive instantly
   ```

## Core Features

### 1. Notification Management
Comprehensive notification lifecycle management:
- **CRUD Operations**: Create, read, update, and delete notifications
- **Mark as Read**: Individual or bulk mark-all-as-read functionality
- **Unread Count**: Real-time unread notification counter
- **Filtering & Pagination**: Filter by type, category, priority, status, read state
- **Archiving**: Archive old notifications for cleanup
- **Expiration**: Automatic notification expiration support
- **Entity Association**: Link notifications to business entities (leads, opportunities, tasks)

### 2. Real-Time WebSocket Delivery
Instant notification delivery with WebSocket technology:
- **Instant Push**: Notifications delivered in real-time without polling
- **Multi-Device Support**: Multiple WebSocket connections per user (desktop, mobile, tablet)
- **Connection Management**: Automatic client registration/unregistration
- **Broadcast Capabilities**:
  - Send to specific user (all devices)
  - Broadcast to all users in a tenant
  - Broadcast to all connected users
  - Category-based filtering for targeted delivery
- **Initial Data Sync**: Unread count and metadata sent on connection establishment
- **Keep-Alive**: Ping/Pong mechanism for connection health monitoring
- **Graceful Disconnection**: Automatic cleanup on client disconnect

### 3. User Preferences
Per-user notification preferences and settings:
- **Channel Preferences**: Enable/disable notifications per delivery channel:
  - In-app notifications (active)
  - Email notifications (planned)
  - Push notifications (planned)
  - SMS notifications (planned)
- **Quiet Hours**: Configure do-not-disturb time windows
  - Start/end time configuration (minutes from midnight)
  - Automatic suppression during quiet hours
- **Default Preferences**: Automatic preference creation for new users

### 4. Multi-Channel Delivery Design
Flexible delivery channel architecture:
- **In-App Notifications**: Currently active, real-time WebSocket delivery
- **Email Delivery**: Planned for future implementation
- **Push Notifications**: Planned for mobile app integration
- **SMS Delivery**: Planned for urgent notifications
- **Channel Selection**: Specify delivery channels per notification
- **Preference Enforcement**: Respect user channel preferences

### 5. Event-Driven Architecture
Kafka-based event consumption with reliability features:
- **Kafka Consumer**: Consumes notification events from other services
- **Event Types**:
  - `notification.create.requested` - Single notification creation
  - `notification.bulk_create.requested` - Bulk notification creation for multiple users
- **Idempotency Handling**: Prevent duplicate notifications using event ID tracking
- **Failed Event Retry**: Automatic retry mechanism for failed event processing
- **Processed Event Tracking**: Track successfully processed events to ensure exactly-once delivery
- **Transaction Safety**: Atomic event processing with database transactions

### 6. Categorization & Prioritization
Organize and prioritize notifications effectively:
- **Categories**: SYSTEM, EMAIL, CRM, PTM (Project/Task Management)
- **Priorities**: LOW, MEDIUM, HIGH, URGENT
- **Types**: INFO, SUCCESS, WARNING, ERROR
- **Status Tracking**: UNREAD, READ, ARCHIVED
- **Filtering Support**: Filter notifications by any combination of attributes
- **Visual Differentiation**: Frontend can style notifications based on type and priority

## API Routes

All routes are prefixed with `/notification-service/api/v1` when accessed through the API Gateway.

### Notification Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/notifications` | Create a new notification |
| GET | `/notifications` | Get all notifications with filters (type, category, priority, status, isRead, page, size) |
| GET | `/notifications/:id` | Get notification by ID |
| PATCH | `/notifications/:id` | Update notification (mark as read, archive, etc.) |
| DELETE | `/notifications/:id` | Delete notification |
| PATCH | `/notifications/read-all` | Mark all notifications as read for current user |
| GET | `/notifications/unread-count` | Get unread notification count |

**Query Parameters for GET `/notifications`:**
- `type` - Filter by notification type (INFO, SUCCESS, WARNING, ERROR)
- `category` - Filter by category (SYSTEM, EMAIL, CRM, PTM)
- `priority` - Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `status` - Filter by status (UNREAD, READ, ARCHIVED)
- `isRead` - Filter by read status (true/false)
- `page` - Page number (default: 1)
- `size` - Items per page (default: 20)

### Preference Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/preferences` | Get current user's notification preferences |
| PATCH | `/preferences` | Update user's notification preferences |

### WebSocket

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/ws?token={jwt}` | Establish WebSocket connection (requires JWT token in query parameter) |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Service health status |

## Event-Driven Communication

### Kafka Consumer

The service consumes notification events from other SERP services via Kafka.

**Consumed Topic:**
- `serp.notification.user.events` - User notification events from all services

**Consumed Event Types:**

1. **notification.create.requested**
   - Creates a single notification for a user
   - Published by any service (CRM, PTM, Account, etc.)
   - Example use case: "Lead converted to opportunity" notification from CRM service

2. **notification.bulk_create.requested**
   - Creates multiple notifications for different users in one batch
   - Published for bulk operations (e.g., task assignments to team)
   - Example use case: "New task assigned" notification to all team members

**Event Processing Flow:**
```
Kafka Topic → Consumer Handler → Idempotency Check → Create Notification → 
Save to PostgreSQL → Broadcast via WebSocket → Delivered to Connected Clients
```

**Reliability Features:**
- **Idempotency**: Each event has unique `eventId` to prevent duplicate processing
- **Processed Event Tracking**: Successfully processed events stored in `processed_event` table
- **Failed Event Handling**: Failed events logged to `failed_event` table with retry mechanism
- **Automatic Retry**: Failed events automatically retried with exponential backoff
- **Transaction Safety**: Event processing wrapped in database transactions

**Integration with Other Services:**
- **CRM Service**: Lead qualification, opportunity close, customer updates
- **PTM Services**: Task assignments, project updates, deadline reminders
- **Account Service**: User invitations, role changes, subscription updates
- **Discuss Service**: Mentions, channel invitations, message notifications

## WebSocket Integration

### Real-Time Notification Delivery

The service uses WebSocket for instant, bidirectional communication with clients.

**Connection Establishment:**
1. Client requests WebSocket upgrade: `GET /notification-service/ws?token={jwt}`
2. Server validates JWT token and extracts user/tenant context
3. HTTP connection upgraded to WebSocket protocol
4. Client registered in WebSocket Hub
5. Initial data sent to client (unread count, metadata)
6. Bidirectional message pump started

**WebSocket Message Types:**
- **NOTIFICATION_NEW**: New notification created and pushed to client
- **INITIAL_DATA**: Initial sync data sent on connection (unread count, categories)
- **PING/PONG**: Keep-alive messages for connection health monitoring

**Hub Capabilities:**
- **SendToUser**: Send message to all devices of a specific user
- **SendToUserWithCategory**: Send to user with category filtering
- **BroadcastToTenant**: Broadcast message to all users in a tenant
- **BroadcastToAll**: Broadcast to all connected clients (system announcements)
- **Connection Tracking**: Check if user is online, get connection metrics

**Multi-Device Support:**
- Users can have multiple active WebSocket connections simultaneously
- Notifications delivered to all connected devices (desktop, mobile, tablet)
- Each device receives the same notification instantly
- Automatic cleanup when device disconnects

**Connection Management:**
- Automatic client registration on connect
- Automatic cleanup on disconnect
- Buffered message queue per client (256 messages)
- Write timeout for slow clients
- Read timeout with pong handler for keep-alive

## Multi-Tenancy

The notification service enforces strict multi-tenancy isolation:

- **Tenant Identification**: `tenantId` extracted from JWT token in every request
- **Data Isolation**: All database queries filtered by `tenantId`
- **WebSocket Isolation**: WebSocket Hub tracks clients per tenant for broadcast capabilities
- **Security**: Users can only access notifications within their tenant
- **Broadcast Control**: Tenant-wide broadcasts isolated to specific tenant

## Integration Points

### API Gateway
- All external HTTP/REST requests route through API Gateway (port 8080)
- JWT authentication enforced at gateway level
- Context path: `/notification-service` prepended to all routes
- WebSocket upgrade requests also proxied through gateway

### Keycloak
- User authentication and authorization
- JWT token generation and validation
- JWKS endpoint for public key retrieval
- Multi-tenant realm support
- Realm: `serp`, Client: `serp-notification`

### PostgreSQL
- Primary data store for notifications, preferences, and event tracking
- Database: `serp_notification`
- Tables: `notification`, `notification_preference`, `processed_event`, `failed_event`
- Connection pooling via GORM

### Redis
- Caching layer for frequently accessed data
- User preference caching
- Potential session management for WebSocket connections
- Performance optimization for read-heavy operations

### Kafka
- Event consumption from topic: `serp.notification.user.events`
- Consumer group for load balancing and fault tolerance
- Idempotency tracking to prevent duplicate processing
- Failed event logging for debugging and retry

### Other SERP Services (Event Publishers)
- **CRM Service**: Lead/opportunity/customer notification events
- **PTM Services**: Task/project/schedule notification events
- **Account Service**: User/organization/subscription notification events
- **Discuss Service**: Message/mention/channel notification events
- **Any Service**: Can publish notification creation requests to Kafka topic

## License

This project is part of the SERP ERP system and is licensed under the MIT License. See the [LICENSE](../LICENSE) file in the root directory for details.

## Author

**QuanTuanHuy**  
Part of Serp Project
