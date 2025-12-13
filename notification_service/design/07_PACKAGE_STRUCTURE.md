# Package Structure

**Module:** notification_service  
**Ngày tạo:** 2025-12-13

---

## 1. Directory Structure

```
notification_service/
├── Dockerfile
├── docker-compose.yml
├── go.mod
├── go.sum
├── run-dev.sh
├── .env
├── design/                          # Design documents
│   ├── 01_OVERVIEW.md
│   ├── 02_DOMAIN_ENTITIES.md
│   ├── ...
│
└── src/
    ├── main.go                      # Entry point
    │
    ├── cmd/
    │   └── bootstrap/
    │       └── all.go               # FX dependency injection
    │
    ├── config/
    │   ├── database.go              # Database config
    │   ├── redis.go                 # Redis config
    │   ├── kafka.go                 # Kafka config
    │   └── websocket.go             # WebSocket config
    │
    ├── core/
    │   ├── domain/
    │   │   ├── constant/
    │   │   │   └── topics.go        # Kafka topic constants
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── event/
    │   │   │   │   ├── notification_request_event.go
    │   │   │   │   ├── task_event_payload.go
    │   │   │   │   └── email_request_event.go
    │   │   │   ├── request/
    │   │   │   │   ├── create_notification_request.go
    │   │   │   │   ├── create_bulk_notification_request.go
    │   │   │   │   ├── mark_read_request.go
    │   │   │   │   ├── update_preferences_request.go
    │   │   │   │   └── get_notifications_request.go
    │   │   │   └── response/
    │   │   │       ├── notification_response.go
    │   │   │       ├── notification_list_response.go
    │   │   │       └── unread_count_response.go
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── base_entity.go
    │   │   │   ├── notification_entity.go
    │   │   │   ├── notification_preference_entity.go
    │   │   │   └── notification_template_entity.go
    │   │   │
    │   │   ├── enum/
    │   │   │   ├── notification_type.go
    │   │   │   ├── notification_category.go
    │   │   │   ├── notification_priority.go
    │   │   │   ├── delivery_channel.go
    │   │   │   └── active_status.go
    │   │   │
    │   │   └── mapper/
    │   │       ├── notification_mapper.go
    │   │       └── preference_mapper.go
    │   │
    │   ├── port/
    │   │   ├── store/
    │   │   │   ├── notification_port.go
    │   │   │   ├── preference_port.go
    │   │   │   ├── template_port.go
    │   │   │   └── transaction_port.go
    │   │   │
    │   │   └── client/
    │   │       ├── redis_port.go
    │   │       ├── kafka_producer_port.go
    │   │       └── websocket_hub_port.go
    │   │
    │   ├── service/
    │   │   ├── notification_service.go
    │   │   ├── preference_service.go
    │   │   ├── template_service.go
    │   │   ├── delivery_service.go
    │   │   └── transaction_service.go
    │   │
    │   └── usecase/
    │       ├── notification_usecase.go
    │       └── preference_usecase.go
    │
    ├── infrastructure/
    │   ├── client/
    │   │   ├── redis_adapter.go
    │   │   └── kafka_producer_adapter.go
    │   │
    │   ├── store/
    │   │   ├── adapter/
    │   │   │   ├── notification_adapter.go
    │   │   │   ├── preference_adapter.go
    │   │   │   ├── template_adapter.go
    │   │   │   └── transaction_adapter.go
    │   │   │
    │   │   ├── mapper/
    │   │   │   ├── notification_model_mapper.go
    │   │   │   ├── preference_model_mapper.go
    │   │   │   └── template_model_mapper.go
    │   │   │
    │   │   └── model/
    │   │       ├── base_model.go
    │   │       ├── notification_model.go
    │   │       ├── notification_preference_model.go
    │   │       └── notification_template_model.go
    │   │
    │   └── websocket/
    │       ├── hub.go
    │       ├── client.go
    │       └── redis_broadcaster.go
    │
    ├── kernel/
    │   ├── properties/
    │   │   └── app_properties.go
    │   │
    │   └── utils/
    │       ├── auth_utils.go
    │       ├── response_utils.go
    │       ├── time_utils.go
    │       └── keycloak_jwks_utils.go
    │
    └── ui/
        ├── controller/
        │   ├── notification_controller.go
        │   ├── preference_controller.go
        │   └── websocket_controller.go
        │
        ├── kafka/
        │   ├── notification_consumer.go
        │   └── consumer_group.go
        │
        ├── middleware/
        │   ├── jwt_middleware.go
        │   ├── logging_middleware.go
        │   └── service_auth_middleware.go
        │
        └── router/
            └── router.go
```

---

## 2. Key Interfaces (Ports)

### 2.1. Store Ports

```go
// core/port/store/notification_port.go
package store

type INotificationPort interface {
    Create(ctx context.Context, tx *gorm.DB, entity *entity.NotificationEntity) (*entity.NotificationEntity, error)
    CreateBulk(ctx context.Context, tx *gorm.DB, entities []*entity.NotificationEntity) error
    GetByID(ctx context.Context, id, userID int64) (*entity.NotificationEntity, error)
    GetByUserID(ctx context.Context, userID int64, filter *dto.GetNotificationsRequest) ([]*entity.NotificationEntity, int64, error)
    MarkAsRead(ctx context.Context, tx *gorm.DB, ids []int64, userID int64) (int64, error)
    MarkAllAsRead(ctx context.Context, tx *gorm.DB, userID int64) (int64, error)
    Delete(ctx context.Context, tx *gorm.DB, id, userID int64) error
    DeleteBulk(ctx context.Context, tx *gorm.DB, ids []int64, userID int64) error
    CountUnread(ctx context.Context, userID int64) (int64, error)
    CountUnreadByCategory(ctx context.Context, userID int64) (map[string]int64, error)
}

// core/port/store/preference_port.go
type IPreferencePort interface {
    GetByUserID(ctx context.Context, userID int64) (*entity.NotificationPreferenceEntity, error)
    Upsert(ctx context.Context, tx *gorm.DB, entity *entity.NotificationPreferenceEntity) error
}

// core/port/store/transaction_port.go
type ITransactionPort interface {
    ExecuteInTransaction(ctx context.Context, fn func(tx *gorm.DB) error) error
    ExecuteInTransactionWithResult(ctx context.Context, fn func(tx *gorm.DB) (any, error)) (any, error)
}
```

### 2.2. Client Ports

```go
// core/port/client/websocket_hub_port.go
package client

type IWebSocketHubPort interface {
    SendToUser(userID int64, message []byte) error
    BroadcastToTenant(tenantID int64, message []byte) error
    GetConnectedUserCount() int
    IsUserConnected(userID int64) bool
}

// core/port/client/kafka_producer_port.go
type IKafkaProducerPort interface {
    SendEmailRequest(ctx context.Context, event *event.EmailRequestEvent) error
    SendPushRequest(ctx context.Context, event *event.PushRequestEvent) error
    SendDeliveryConfirmation(ctx context.Context, event *event.DeliveryConfirmationEvent) error
}

// core/port/client/redis_port.go
type IRedisPort interface {
    GetUnreadCount(ctx context.Context, userID int64) (int64, error)
    SetUnreadCount(ctx context.Context, userID int64, count int64, ttl time.Duration) error
    IncrUnreadCount(ctx context.Context, userID int64) error
    DecrUnreadCount(ctx context.Context, userID int64, amount int64) error
    InvalidateUnreadCount(ctx context.Context, userID int64) error
    PublishToUser(ctx context.Context, userID int64, message []byte) error
}
```

---

## 3. Bootstrap (Uber FX)

```go
// cmd/bootstrap/all.go
package bootstrap

import (
    "github.com/serp/notification-service/src/core/service"
    "github.com/serp/notification-service/src/core/usecase"
    client "github.com/serp/notification-service/src/infrastructure/client"
    store "github.com/serp/notification-service/src/infrastructure/store/adapter"
    "github.com/serp/notification-service/src/infrastructure/websocket"
    "github.com/serp/notification-service/src/kernel/utils"
    "github.com/serp/notification-service/src/ui/controller"
    "github.com/serp/notification-service/src/ui/kafka"
    "github.com/serp/notification-service/src/ui/middleware"
    "github.com/serp/notification-service/src/ui/router"
    "go.uber.org/fx"
)

func All() fx.Option {
    return fx.Options(
        // Core infrastructure
        fx.Provide(NewLogger),
        fx.Provide(NewConfig),
        fx.Provide(NewAppProperties),
        fx.Provide(NewDatabase),
        fx.Provide(NewRedisClient),
        fx.Provide(NewKafkaConsumer),
        fx.Provide(NewGinEngine),
        
        // WebSocket
        fx.Provide(websocket.NewHub),
        fx.Provide(websocket.NewRedisBroadcaster),
        
        // Middleware
        fx.Provide(middleware.NewJWTMiddleware),
        fx.Provide(middleware.NewServiceAuthMiddleware),
        
        // Utils
        fx.Provide(utils.NewKeycloakJwksUtils),
        
        // Clients
        fx.Provide(client.NewRedisAdapter),
        fx.Provide(client.NewKafkaProducerAdapter),
        
        // Store Adapters
        fx.Provide(store.NewDBTransactionAdapter),
        fx.Provide(store.NewNotificationAdapter),
        fx.Provide(store.NewPreferenceAdapter),
        fx.Provide(store.NewTemplateAdapter),
        
        // Services
        fx.Provide(service.NewTransactionService),
        fx.Provide(service.NewNotificationService),
        fx.Provide(service.NewPreferenceService),
        fx.Provide(service.NewTemplateService),
        fx.Provide(service.NewDeliveryService),
        
        // Use Cases
        fx.Provide(usecase.NewNotificationUseCase),
        fx.Provide(usecase.NewPreferenceUseCase),
        
        // Kafka Consumers
        fx.Provide(kafka.NewNotificationConsumer),
        
        // Controllers
        fx.Provide(controller.NewNotificationController),
        fx.Provide(controller.NewPreferenceController),
        fx.Provide(controller.NewWebSocketController),
        
        // Routes & Lifecycle
        fx.Invoke(router.RegisterRoutes),
        fx.Invoke(StartWebSocketHub),
        fx.Invoke(StartKafkaConsumer),
    )
}
```

---

## 4. Environment Configuration

```bash
# .env
# Server
APP_PORT=8088
APP_ENV=development

# Database
DB_HOST=localhost
DB_PORT=5432
DB_USER=serp
DB_PASSWORD=serp
DB_NAME=serp_notification

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=notification-service
KAFKA_TOPICS=NOTIFICATION_REQUEST_TOPIC,TASK_EVENT_TOPIC,CRM_EVENT_TOPIC

# Keycloak
KEYCLOAK_URL=http://localhost:8180
KEYCLOAK_REALM=serp
KEYCLOAK_CLIENT_ID=notification-service
CLIENT_SECRET=xxx

# WebSocket
WS_READ_BUFFER_SIZE=1024
WS_WRITE_BUFFER_SIZE=1024
WS_PING_PERIOD=30s
```

---

## 5. go.mod

```go
module github.com/serp/notification-service

go 1.21

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/gorilla/websocket v1.5.1
    github.com/redis/go-redis/v9 v9.3.0
    github.com/confluentinc/confluent-kafka-go/v2 v2.3.0
    go.uber.org/fx v1.20.1
    go.uber.org/zap v1.26.0
    gorm.io/gorm v1.25.5
    gorm.io/driver/postgres v1.5.4
    gorm.io/datatypes v1.2.0
)
```
