# Discuss Service - Tổng Quan

**Authors:** QuanTuanHuy  
**Description:** Part of Serp Project - Unified Communication Module  
**Date:** December 2025  
**Version:** 1.0  

## 📋 Mục Đích

Discuss Service là module giao tiếp tập trung cho hệ thống SERP ERP, cung cấp khả năng:
- **Real-time messaging** giữa các users
- **Contextual discussions** gắn với business entities (customers, tasks, orders)
- **Unified activity feed** tổng hợp hoạt động từ tất cả modules
- **Collaboration tools** hỗ trợ teamwork (mentions, threads, reactions)

Tương tự như **Odoo Discuss** nhưng được thiết kế theo kiến trúc microservices với khả năng mở rộng cao.

---

## 🎯 Vấn Đề Cần Giải Quyết

### **Hiện trạng:**
1. Mỗi module tự implement comment/activity riêng → Duplicate code
2. Không có communication channel xuyên suốt qua nhiều modules
3. Users phải chuyển đổi giữa nhiều nơi để theo dõi conversations
4. Thiếu real-time collaboration features (typing indicators, presence)
5. Khó tìm kiếm và theo dõi lịch sử trao đổi

### **Giải pháp:**
Xây dựng **centralized discuss service** với:
- ✅ Unified message storage
- ✅ WebSocket-based real-time communication
- ✅ Entity-based channels (auto-created when entity created)
- ✅ Rich collaboration features (@mentions, threads, reactions)
- ✅ Integration với notification system
- ✅ Cross-module activity aggregation

---

## 🏗️ Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────────────────┐
│                       SERP Web (Frontend)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Channel List │  │  Chat Window │  │ Activity Feed│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────┬────────────────────────────────────────────────┘
             │ WebSocket + REST API
             ↓
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (Port 8080)                 │
│              JWT Validation & Routing                        │
└────────────┬────────────────────────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────────────────────────┐
│              Discuss Service (Go - Port 8092)                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  REST API          WebSocket Hub      Kafka Consumer │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  ChannelUseCase    MessageUseCase    ActivityUseCase│   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  ChannelService    MessageService    SearchService  │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  PostgreSQL        Redis Cache       S3 Storage      │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────┬───────────────────────┬────────────────────────┘
             │                       │
             │ Kafka Events          │ Notifications
             ↓                       ↓
┌────────────────────┐    ┌─────────────────────────┐
│  Other Services    │    │  Notification Service   │
│  (CRM, PTM, etc)   │    │  (Email, Push, WS)      │
└────────────────────┘    └─────────────────────────┘
```

---

## 🔑 Core Concepts

### **1. Channel (Kênh Giao Tiếp)**
Channel là nơi chứa messages, có 3 loại:

#### **DIRECT Channel**
- 1-1 private chat giữa 2 users
- Tự động tạo khi user nhắn tin lần đầu
- Luôn private

#### **GROUP Channel**
- Group chat cho team/department
- Có thể public hoặc private
- Members được mời tham gia
- Ví dụ: "Sales Team", "Marketing Department"

#### **TOPIC Channel**
- Gắn với một business entity cụ thể
- Tự động tạo khi entity được tạo
- Ví dụ: 
  - Channel cho Customer #123
  - Channel cho Task #456
  - Channel cho Purchase Order #789

### **2. Message**
Message là đơn vị giao tiếp cơ bản:
- **Text content** với markdown support
- **Attachments** (images, files)
- **Mentions** (@user)
- **Thread replies** (nested discussions)
- **Reactions** (emoji 👍❤️😂)
- **Edit/Delete** capabilities

### **3. Activity Feed**
Tổng hợp activities từ tất cả modules:
- Messages sent
- Tasks completed
- Orders created
- Customer interactions
- Approval workflows

---

## 📊 Use Cases

### **UC-1: Direct Messaging**
```
User A muốn nhắn tin với User B:
1. Frontend gọi API create/get direct channel
2. Service tìm hoặc tạo DIRECT channel
3. User A gửi message qua WebSocket
4. Service broadcast message đến User B (real-time)
5. Notification service gửi push notification nếu User B offline
```

### **UC-2: Team Discussion**
```
Team leader tạo group channel cho team:
1. Tạo GROUP channel "Sales Q1 Planning"
2. Mời members vào channel
3. Members discuss, share files, @mention colleagues
4. Typing indicators hiển thị khi ai đó đang gõ
5. Thread replies cho deep discussions
```

### **UC-3: Entity-Based Discussion**
```
CRM user tạo customer mới:
1. CRM service tạo Customer #123
2. CRM publish Kafka event "CUSTOMER_CREATED"
3. Discuss service consume event
4. Tự động tạo TOPIC channel gắn với Customer #123
5. Sales team discuss về customer trong channel này
6. Tất cả discussions được link với customer record
```

### **UC-4: Cross-Module Communication**
```
Workflow: Lead → Opportunity → Order → Delivery
1. CRM creates TOPIC channel for Lead #100
2. Lead converts to Opportunity → Same channel persists
3. Opportunity converts to Order → Channel ID passed to Sales
4. Sales creates Order → Link same channel
5. Logistics uses same channel for delivery updates
→ Toàn bộ conversation history xuyên suốt process
```

### **UC-5: Activity Feed**
```
User login vào SERP:
1. Activity Feed hiển thị:
   - New messages in channels
   - Tasks assigned to user
   - Customers interacted with
   - Orders requiring approval
2. Click vào activity → Navigate to entity + channel
```

---

## 🎨 User Experience

### **Sidebar (Channel List)**
```
┌─────────────────────────────┐
│ 🔍 Search channels...        │
├─────────────────────────────┤
│ ⭐ Pinned                    │
│   • Sales Team         [3]  │
│   • Customer #123      [1]  │
├─────────────────────────────┤
│ 💬 Direct Messages          │
│   🟢 John Doe          [2]  │
│   🟢 Jane Smith             │
│   ⚪ Mike Johnson           │
├─────────────────────────────┤
│ 👥 Groups                   │
│   • Marketing Team     [5]  │
│   • Dev Team                │
├─────────────────────────────┤
│ 📋 Topics (Entities)        │
│   • Task: Q1 Planning       │
│   • Order #PO-2024-001      │
└─────────────────────────────┘
[3] = Unread count
🟢 = Online, ⚪ = Offline
```

### **Chat Window**
```
┌─────────────────────────────────────────────────────────┐
│ # Sales Team                                      [⚙️]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ John Doe          10:30 AM                             │
│ Hey @Jane, can you review the Q1 targets?              │
│   👍 2  ❤️ 1                                           │
│   └─ 2 replies                                         │
│                                                         │
│ You               10:32 AM                             │
│ Sure! I'll check it now                                │
│   [attachment: Q1_Report.pdf]                          │
│                                                         │
│ Jane Smith is typing...                                │
│                                                         │
├─────────────────────────────────────────────────────────┤
│ 📎  |  Type a message...                       [Send]  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔗 Integration Points

### **Với CRM Service**
- CUSTOMER_CREATED → Create TOPIC channel
- OPPORTUNITY_CREATED → Create TOPIC channel
- CONTACT_ADDED → Add to customer channel

### **Với PTM Services**
- TASK_CREATED → Create TOPIC channel
- TASK_ASSIGNED → Notify via message
- TASK_COMPLETED → Post system message

### **Với Purchase Service**
- PURCHASE_ORDER_CREATED → Create TOPIC channel
- APPROVAL_REQUIRED → Send @mention to approvers
- ORDER_DELIVERED → Post update message

### **Với Notification Service**
- New message → Push notification
- @Mention → Email + Push
- Missed messages → Daily digest email

### **Với Storage Service**
- File upload → Store in S3/MinIO
- Image preview → Generate thumbnail
- File sharing → Access control check

---

## 📈 Performance Requirements

### **Scalability Targets**
- **Users:** Support 10,000+ concurrent users
- **Messages:** Handle 1M+ messages/day
- **Channels:** 100,000+ active channels
- **Latency:** <100ms for message delivery
- **WebSocket connections:** 10,000+ concurrent

### **Storage Estimates**
```
Average message size: 500 bytes
Messages per day: 1M
Monthly storage: ~15GB
Yearly storage: ~180GB

With attachments (assume 20% have files, avg 2MB):
Additional yearly: ~150TB
→ S3 storage required
```

### **Caching Strategy**
- **Redis:** Recent messages (last 100 per channel)
- **Redis:** User presence (online/offline status)
- **Redis:** Typing indicators (TTL 10s)
- **PostgreSQL:** Full message history

---

## 🔐 Security & Privacy

### **Authentication**
- JWT-based auth (inherit from API Gateway)
- WebSocket connection authenticated via token

### **Authorization**
- **DIRECT channels:** Only 2 members can read/write
- **GROUP channels:** Only members can read/write
- **TOPIC channels:** Access based on entity permissions
  - Example: Can access Customer #123 channel if can view customer

### **Data Privacy**
- Messages encrypted at rest (PostgreSQL encryption)
- File uploads scanned for malware
- GDPR compliance: User can delete all messages
- Tenant isolation: Strict tenantID filtering

---

## 🚀 Deployment

### **Service Configuration**
```yaml
Service: discuss_service
Language: Go 1.21+
Port: 8092
Database: PostgreSQL 15+
Cache: Redis 7+
Message Queue: Kafka 3.x
Storage: S3/MinIO
```

### **Resource Requirements**
```
CPU: 2-4 cores
Memory: 4-8GB RAM
Storage: 100GB (initial), auto-scale
Network: 1Gbps
```

### **Monitoring**
- Prometheus metrics:
  - Message throughput (msg/sec)
  - WebSocket connections (count)
  - API latency (ms)
  - Error rate (%)
- Grafana dashboards
- Alert on high latency or error rates

---

## 📚 Related Documents

1. [Architecture Design](./architecture.md) - Detailed architecture
2. [Database Schema](./database-schema.md) - Table structures
3. [API Specification](./api-specification.md) - REST endpoints
4. [WebSocket Protocol](./websocket-protocol.md) - Real-time protocol
5. [Implementation Plan](./implementation-plan.md) - Development roadmap

---

## 🔄 Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-30 | QuanTuanHuy | Initial design document |

---

## ✅ Success Criteria

Discuss Service được coi là thành công khi:

1. ✅ **95% messages delivered < 100ms**
2. ✅ **Support 10,000+ concurrent WebSocket connections**
3. ✅ **Zero message loss** (all messages persisted)
4. ✅ **99.9% uptime** (excluding planned maintenance)
5. ✅ **Users spend 30%+ more time** collaborating in SERP
6. ✅ **50% reduction** in external chat tools usage (Slack, Teams)
7. ✅ **Positive user feedback** (>4/5 satisfaction score)
