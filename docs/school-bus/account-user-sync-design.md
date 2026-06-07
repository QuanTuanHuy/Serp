# Thiết kế Đồng bộ Người dùng (Account User Sync Design) - Module School Bus

Tài liệu này ghi nhận thiết kế kỹ thuật phục vụ việc đồng bộ tài khoản người dùng từ module Core Account sang bảng shadow của module School Bus.

---

## 1. Kiến trúc Shadow Table

Hệ thống quản lý tài khoản tập trung (Core Account) là Source of Truth. Để đảm bảo hiệu năng truy vấn dữ liệu chéo và giảm tải cho network call đồng thời làm nền tảng kiểm tra quyền (Data Scope), module School Bus duy trì bảng shadow `school_bus_user`.

*   **Bảng shadow**: `school_bus_user`
*   **Mã định danh liên kết**: `account_user_id` (trùng khớp với `users.id` trong Core Account).
*   **Unique Index**: Khóa duy nhất trên trường `account_user_id` và `keycloak_id` để ngăn ngừa trùng lặp dữ liệu.

---

## 2. Liên kết Hồ sơ (Profile Linking)

Các thực thể hồ sơ hiện có trong School Bus:
*   `school_bus_parent_profile.user_id`
*   `school_bus_driver_profile.user_id`
*   `school_bus_attendant_profile.user_id`

### Quy tắc Mapping ở Phase 2:
1.  Trường `user_id` trong các bảng profile này được xác định là lưu **Account User ID** (`users.id` từ Core Account).
2.  Do đó, khi liên kết hồ sơ với bảng shadow user mới tạo:
    ```txt
    profile.user_id = school_bus_user.account_user_id
    ```
3.  **Lưu ý quan trọng**: Ở Phase 2, **KHÔNG** tạo khóa ngoại (Foreign Key) cứng từ các bảng profile sang bảng `school_bus_user` mới để tránh rủi ro dữ liệu lịch sử chưa đồng bộ đầy đủ gây lỗi ràng buộc. Việc thêm FK sẽ được xem xét và đặt TODO cho các phase sau.

---

## 3. Quy trình đồng bộ dữ liệu (Upsert Logic)

Mọi luồng đồng bộ (Kafka Consumer ở Phase 3 và Sync Recovery Job ở Phase 4) đều sẽ gọi đến interface:

```java
ISchoolBusUserService.upsertFromAccountUser(SchoolBusUserUpsertCommand command);
```

### Quy tắc Matching của Service Layer:
1.  Tìm bản ghi shadow user hiện tại theo thứ tự ưu tiên:
    *   **Thứ tự 1**: Tìm theo `accountUserId`
    *   **Thứ tự 2**: Tìm theo `keycloakId` (nếu không khớp ID trước đó)
    *   **Thứ tự 3**: Tìm theo cặp `tenantId` + `email` (nếu không khớp Keycloak ID)
2.  Nếu tìm thấy bản ghi:
    *   Cập nhật thông tin mới từ Command.
    *   Đặt thời gian đồng bộ: `lastSyncedAt = LocalDateTime.now()`.
    *   Ghi nhận nguồn: `syncSource` (ví dụ: `KAFKA`, `ACCOUNT_API`).
    *   Lưu payload thô: `rawPayloadJson`.
3.  Nếu không tìm thấy bản ghi:
    *   Tạo mới một shadow user mới hoàn toàn với `isActive = true` và `isDeleted = false`.

---

## 4. Phase 3: Tích hợp sự kiện qua Kafka (Kafka Consumer)

Trong Phase 3, chúng tôi xây dựng class consumer lắng nghe sự kiện tài khoản từ Core Account để đồng bộ vào bảng shadow.

### 4.1. Kafka Configuration
- **Topic**: `SYNC_USER` (Configurable: `school-bus.kafka.topics.account-user-events: ${SYNC_USER_TOPIC:SYNC_USER}`).
- **GroupId**: `school-bus-sync-user` (Configurable: `spring.kafka.consumer.group-id: ${KAFKA_SCHOOL_BUS_GROUP_ID:school-bus-sync-user}`).
- **Consumer Class**: `AccountUserEventConsumer` (nằm tại package `serp.project.school_bus_service.consumer`).

### 4.2. Rà soát Payload Event thực tế
Sự kiện đồng bộ tài khoản từ Core Account có payload kiểu JSON dạng snapshot đầy đủ (chứ không chia theo event type `CREATED`/`UPDATED`).
Các thuộc tính payload thực tế (tương ứng với `SyncUserEvent` bên Account):
- `userId` (Long)
- `organizationId` (Long)
- `tenantId` (Long, map từ field `"tid"` của JSON)
- `email` (String)
- `phoneNumber` (String)
- `firstName` (String)
- `lastName` (String)
- `fullName` (String)
- `roleNames` (List<String>)

> [!WARNING]
> **Điểm đặc biệt cần lưu ý**:
> - **Không có `keycloakId`**: Payload của sự kiện đồng bộ Account không cung cấp `keycloakId`. Do đó, Command mapping sẽ đặt `keycloakId = null`.
> - **Không có `status`**: Payload không cung cấp trạng thái người dùng. Do đó, Command mapping sẽ mặc định gán `status = "ACTIVE"`.
> - **roleNames**: Mặc dù payload gửi về danh sách vai trò (`roleNames`), Phase 3 chỉ lưu thông tin này trong payload thô (`rawPayloadJson`) mà không tự động tạo các bản ghi profile nghiệp vụ liên quan để tránh thiếu sót dữ liệu chuyên biệt.

### 4.3. Bảng ánh xạ (Mapping Table)

| Trường trong JSON Payload | Trường tương ứng trong `SchoolBusUserUpsertCommand` | Quy tắc & Fallback |
| :--- | :--- | :--- |
| `userId` | `accountUserId` | Map trực tiếp từ `userId` |
| `tenantId` (hoặc `"tid"`) | `tenantId` | Sử dụng `tenantId`, nếu null fallback dùng `organizationId` |
| `email` | `email` | Map trực tiếp |
| `firstName` | `firstName` | Map trực tiếp |
| `lastName` | `lastName` | Map trực tiếp |
| `phoneNumber` | `phoneNumber` | Map trực tiếp |
| `organizationId` | `primaryOrganizationId` | Map trực tiếp |
| *(Không có)* | `keycloakId` | Gán `null` |
| *(Không có)* | `avatarUrl` | Gán `null` |
| *(Không có)* | `status` | Mặc định gán `"ACTIVE"` |
| *(Không có)* | `syncSource` | Gán `"KAFKA"` |
| *Toàn bộ message* | `rawPayloadJson` | Chuỗi JSON thô phục vụ debug và xử lý role/profile ở các phase sau |

### 4.4. Xử lý lỗi (Error Handling)
- **Thiếu trường bắt buộc**: Nếu payload thiếu các trường cốt lõi như `userId`, `email` hoặc cả hai trường định danh tổ chức (`tenantId`/`organizationId`), consumer sẽ log warning và skip tin nhắn.
- **Lỗi Parse JSON**: Consumer try-catch quá trình deserialize của Jackson. Nếu xảy ra lỗi cú pháp JSON, consumer sẽ ghi nhận lỗi qua log kèm chuỗi payload và tiếp tục chạy mà không làm crash hay ngưng trệ vòng lặp của Kafka listener.
- **Lỗi Database/Service**: Toàn bộ quá trình gọi `upsertFromAccountUser` được bọc trong try-catch riêng. Nếu DB gặp lỗi ràng buộc dữ liệu hoặc logic lưu thất bại, consumer sẽ log error chi tiết (kèm userId, email, tenantId) và bỏ qua để tránh nghẽn luồng.

---

## 5. Phase 4: Cơ chế đồng bộ dự phòng (REST API & Scheduled Sync Job)

Để khắc phục việc thiếu hụt các trường thông tin quan trọng (`keycloakId`, `status`) trong sự kiện Kafka, đồng thời làm cơ chế dự phòng phục hồi dữ liệu khi xảy ra sự cố mất tin nhắn Kafka, Phase 4 triển khai cơ chế đồng bộ dự phòng qua REST API.

### 5.1. Cơ chế Checkpoint Incremental Sync
- **Bảng checkpoint**: `school_bus_sync_checkpoint` lưu vết tiến trình đồng bộ thông qua `sync_code = "ACCOUNT_USER_SYNC"`.
- **Cách thức hoạt động**: 
  - Lưu thời điểm đồng bộ thành công gần nhất vào `last_success_sync_at`.
  - Khi Job chạy, hệ thống truy vấn danh sách người dùng từ Core Account API phân trang, sắp xếp theo thời gian cập nhật mới nhất (`updatedAt desc`).
  - Job sẽ duyệt qua các người dùng từ mới nhất. Khi gặp một người dùng có `updatedAt` trước thời điểm `last_success_sync_at`, Job sẽ dừng quá trình đồng bộ (incremental sync) để tối ưu hiệu năng.
  - Nếu chưa có checkpoint (lần đầu chạy), hệ thống sẽ fallback về một khoảng thời gian trước đó (configurable: `school-bus.account-sync.initial-lookback-days`, mặc định 30 ngày) để quét dữ liệu.

### 5.2. Xác thực Service-to-Service
- Các API internal của module Account được bảo vệ bằng quyền `SERP_SERVICES`.
- Class `TokenUtils` được triển khai để tự động lấy Service Token thông qua luồng Client Credentials Flow của Keycloak (`/protocol/openid-connect/token`), sau đó đính kèm token này vào header `Authorization: Bearer <token>` khi gọi API.

### 5.3. API Mapping & Đồng bộ Trạng thái
REST Client (`AccountUserClient`) sẽ gọi API `/internal/api/v1/users` và ánh xạ dữ liệu đầy đủ về `SchoolBusUserUpsertCommand`:
- `keycloakId`, `avatarUrl`, `preferredLanguage`, `timezone`, `userType` được đồng bộ đầy đủ.
- **Quy tắc mapping trạng thái hoạt động (`isActive`)**:
  - `status` là `"ACTIVE"` -> `isActive = true`.
  - `status` là `"INACTIVE"`, `"SUSPENDED"`, `"DELETED"`, hoặc `"INVITED"` -> `isActive = false`.
  - Không thực hiện hard delete dữ liệu shadow user (`isDeleted` giữ nguyên `false`) để bảo toàn lịch sử audit chuyến đi, điểm danh.

### 5.4. Sơ đồ các REST Endpoints hỗ trợ
- `POST /admin/account-users/sync`: Trigger chạy đồng bộ thủ công (incremental sync từ checkpoint gần nhất).
- `POST /admin/account-users/sync/{accountUserId}`: Đồng bộ khẩn cấp thông tin của 1 user cụ thể từ Core Account.
- `GET /admin/account-users/sync/checkpoint`: Xem trạng thái chạy gần nhất của Job (trạng thái, thời gian chạy, số bản ghi đồng bộ thành công/lỗi).

