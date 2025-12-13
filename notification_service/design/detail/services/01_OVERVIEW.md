# Service Layer Overview

**Module:** notification_service/core/service  
**Ngày tạo:** 2025-12-14

---

## 1. Service Layer Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         UseCase Layer                            │
│         (Orchestration, Transaction, Cross-service logic)        │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Service Layer                             │
│              (Business Logic, Validation, Rules)                 │
│                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ Notification    │  │   Preference    │  │    Template     │  │
│  │    Service      │  │    Service      │  │    Service      │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │           │
│  ┌────────┴────────┐  ┌────────┴────────┐                       │
│  │   Delivery      │  │  Transaction    │                       │
│  │    Service      │  │    Service      │                       │
│  └─────────────────┘  └─────────────────┘                       │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Port Layer                               │
│                    (Interfaces/Contracts)                        │
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Store Ports │  │Client Ports │  │ External    │              │
│  │ (DB Access) │  │(Redis,Kafka)│  │   Ports     │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Services Overview

| Service | Responsibility |
|---------|----------------|
| `NotificationService` | CRUD operations, business rules, validation |
| `PreferenceService` | User notification preferences management |
| `TemplateService` | Notification template processing |
| `DeliveryService` | Multi-channel delivery (WS, Email, Push) |
| `TransactionService` | Database transaction management |

---

## 3. Service Design Principles

### 3.1. Single Responsibility
Mỗi service chỉ xử lý một domain concern:
```go
// ✅ Good: Focused responsibility
type NotificationService struct { ... }  // Notification domain only
type DeliveryService struct { ... }      // Delivery logic only

// ❌ Bad: Mixed responsibilities
type NotificationAndEmailService struct { ... }
```

### 3.2. Dependency Injection via Ports
Services depend on interfaces (ports), not implementations:
```go
type NotificationService struct {
    notificationPort store.INotificationPort  // Interface, not adapter
    redisPort        client.IRedisPort        // Interface, not adapter
}
```

### 3.3. No Direct Cross-Service Calls
Services don't call each other directly - UseCase orchestrates:
```go
// ✅ Good: UseCase orchestrates
func (u *NotificationUseCase) Create(...) {
    notification := u.notificationService.Create(...)
    u.deliveryService.Deliver(notification)
}

// ❌ Bad: Service calls another service
func (s *NotificationService) Create(...) {
    // Don't do this
    s.deliveryService.Deliver(...)
}
```

### 3.4. Transaction Awareness
Services receive transaction from UseCase, don't manage their own:
```go
// ✅ Good: Receive tx from outside
func (s *NotificationService) Create(ctx context.Context, tx *gorm.DB, entity *entity.NotificationEntity) error

// ❌ Bad: Manage own transaction
func (s *NotificationService) Create(ctx context.Context, entity *entity.NotificationEntity) error {
    tx := s.db.Begin()  // Don't do this
}
```

---

## 4. File Structure

```
core/
├── service/
│   ├── notification_service.go     # Notification CRUD & business logic
│   ├── preference_service.go       # User preferences
│   ├── template_service.go         # Template processing
│   ├── delivery_service.go         # Multi-channel delivery
│   └── transaction_service.go      # Transaction management
│
├── port/
│   ├── store/
│   │   ├── notification_port.go
│   │   ├── preference_port.go
│   │   ├── template_port.go
│   │   └── transaction_port.go
│   │
│   └── client/
│       ├── redis_port.go
│       ├── kafka_producer_port.go
│       └── websocket_hub_port.go
│
└── usecase/
    ├── notification_usecase.go
    └── preference_usecase.go
```

---

## 5. Danh Sách Files Chi Tiết

| File | Nội dung |
|------|----------|
| [02_NOTIFICATION_SERVICE.md](./02_NOTIFICATION_SERVICE.md) | NotificationService implementation |
| [03_PREFERENCE_SERVICE.md](./03_PREFERENCE_SERVICE.md) | PreferenceService implementation |
| [04_TEMPLATE_SERVICE.md](./04_TEMPLATE_SERVICE.md) | TemplateService implementation |
| [05_DELIVERY_SERVICE.md](./05_DELIVERY_SERVICE.md) | DeliveryService implementation |
| [06_TRANSACTION_SERVICE.md](./06_TRANSACTION_SERVICE.md) | TransactionService implementation |
| [07_NOTIFICATION_USECASE.md](./07_NOTIFICATION_USECASE.md) | NotificationUseCase orchestration |
| [08_PREFERENCE_USECASE.md](./08_PREFERENCE_USECASE.md) | PreferenceUseCase orchestration |
