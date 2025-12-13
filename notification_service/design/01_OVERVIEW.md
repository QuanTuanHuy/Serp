# In-App Notification Service - Tổng Quan

**Service:** notification_service  
**Ngôn ngữ:** Golang  
**Port:** 8088  
**Phiên bản:** 1.0  
**Ngày tạo:** 2025-12-13

---

## 1. Mục Tiêu

In-App Notification Service cung cấp khả năng:

- **Real-time Notifications:** Gửi thông báo real-time qua WebSocket
- **Notification Storage:** Lưu trữ và quản lý notifications
- **Read/Unread Status:** Theo dõi trạng thái đã đọc/chưa đọc
- **Notification Preferences:** Cho phép user tùy chỉnh preferences
- **Multi-channel Delivery:** Hỗ trợ in-app, push, email (via mailservice)
- **Bulk Notifications:** Gửi thông báo hàng loạt cho nhiều users

---

## 2. Features Matrix

| Feature | Description | Priority |
|---------|-------------|----------|
| **Create Notification** | Tạo notification từ các services khác | High |
| **WebSocket Delivery** | Push real-time qua WebSocket | High |
| **List Notifications** | Lấy danh sách notifications của user | High |
| **Mark as Read** | Đánh dấu đã đọc (single/bulk) | High |
| **Unread Count** | Đếm số notifications chưa đọc | High |
| **Delete Notification** | Xóa notification | Medium |
| **Notification Types** | Phân loại: info, warning, success, error | High |
| **User Preferences** | Tùy chỉnh loại notifications nhận | Medium |
| **Kafka Integration** | Nhận events từ các services | High |
| **Push Notification** | Firebase/APNs integration | Low |

---

## 3. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (serp_web)                       │
│                    WebSocket Client + REST API                   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (:8080)                       │
│                   JWT Validation + Routing                       │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Notification Service (:8088)                    │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    WebSocket Hub                             ││
│  │              (Real-time connections)                         ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────────┐ │
│  │ REST API    │  │ Kafka       │  │ Notification             │ │
│  │ Controllers │  │ Consumers   │  │ Processor                │ │
│  └─────────────┘  └─────────────┘  └──────────────────────────┘ │
└─────────────────────────┬───────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │PostgreSQL│    │  Redis   │    │  Kafka   │
    │(Storage) │    │ (Cache)  │    │ (Events) │
    └──────────┘    └──────────┘    └──────────┘
```

---

## 4. Integration với Services Khác

### 4.1. Event Sources (Kafka Topics)

| Service | Topic | Events |
|---------|-------|--------|
| account | `USER_EVENT_TOPIC` | User created, role changed |
| crm | `CRM_EVENT_TOPIC` | Lead assigned, deal won |
| ptm_task | `TASK_TOPIC` | Task assigned, deadline reminder |
| sales | `SALES_EVENT_TOPIC` | Order created, payment received |
| mailservice | `EMAIL_EVENT_TOPIC` | Email bounced, delivery failed |

### 4.2. Outbound Integration

| Target | Purpose | Method |
|--------|---------|--------|
| mailservice | Send email notifications | Kafka |
| Firebase | Push notifications (mobile) | REST API |
| serp_web | Real-time updates | WebSocket |

---

## 5. Notification Types

```go
const (
    TypeInfo    = "INFO"     // Thông tin chung
    TypeSuccess = "SUCCESS"  // Hành động thành công
    TypeWarning = "WARNING"  // Cảnh báo
    TypeError   = "ERROR"    // Lỗi/vấn đề
    TypeTask    = "TASK"     // Liên quan đến task
    TypeCRM     = "CRM"      // Liên quan đến CRM
    TypeSales   = "SALES"    // Liên quan đến sales
    TypeSystem  = "SYSTEM"   // Thông báo hệ thống
)
```

---

## 6. Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Go 1.21+ |
| Web Framework | Gin |
| WebSocket | gorilla/websocket |
| ORM | GORM |
| Database | PostgreSQL |
| Cache | Redis |
| Message Queue | Kafka |
| DI Framework | Uber FX |
| Auth | JWT (Keycloak) |

---

## 7. Danh Sách Files Thiết Kế

| File | Nội Dung |
|------|----------|
| [02_DOMAIN_ENTITIES.md](./02_DOMAIN_ENTITIES.md) | Entities, DTOs, Enums |
| [03_DATABASE_SCHEMA.md](./03_DATABASE_SCHEMA.md) | Database tables, indexes |
| [04_API_ENDPOINTS.md](./04_API_ENDPOINTS.md) | REST API endpoints |
| [05_WEBSOCKET_DESIGN.md](./05_WEBSOCKET_DESIGN.md) | WebSocket implementation |
| [06_KAFKA_EVENTS.md](./06_KAFKA_EVENTS.md) | Kafka producers/consumers |
| [07_PACKAGE_STRUCTURE.md](./07_PACKAGE_STRUCTURE.md) | Code structure |
| [08_IMPLEMENTATION_GUIDE.md](./08_IMPLEMENTATION_GUIDE.md) | Step-by-step guide |
