# TTCRS Service — Kiến trúc & Luồng Request-Response

## 1. Tổng Quan Service

**TTCRS** (Truck & Trailer Container Route Scheduling) là một Java Spring Boot microservice chịu trách nhiệm quản lý lịch trình vận chuyển hàng hóa bằng xe tải & rơ-moóc. Service chạy trên **port 8093** và xác thực thông qua **Keycloak JWT**.

---

## 2. Cấu Trúc Thư Mục

```
ttcrs/src/main/java/com/example/ttcrs/
├── Application.java              ← Entry point (main)
│
├── config/                       ← Cấu hình toàn cục
│   ├── GlobalConfig.java
│   ├── JpaDataSourceConfig.java
│   ├── KeycloakConfig.java
│   ├── SecurityConfig.java
│   └── UrlProperties.java
│
├── constant/                     ← Enum values
│   ├── ContainerSize.java
│   ├── DriverStatus.java
│   ├── LocationType.java
│   ├── RequestStatus.java
│   ├── RequestType.java
│   ├── StopAction.java
│   ├── TransportPlanStatus.java
│   └── VehicleStatus.java
│
├── controller/                   ← Tầng HTTP — nhận và trả response
│   └── RequestController.java
│
├── dto/
│   ├── request/                  ← Input từ client
│   │   └── RequestFilterDTO.java
│   └── response/                 ← Output trả về client
│       ├── ApiResponse.java
│       ├── PageResponse.java
│       └── RequestResponseDTO.java
│
├── entity/                       ← Ánh xạ bảng database
│   ├── ContainerEntity.java
│   ├── DistanceEntity.java
│   ├── DriverEntity.java
│   ├── LocationEntity.java
│   ├── RequestEntity.java
│   ├── TrailerEntity.java
│   ├── TransportPlanEntity.java
│   ├── TransportPlanStopEntity.java
│   └── TruckEntity.java
│
├── repository/                   ← Tầng database queries
│   └── RequestRepository.java
│
├── service/                      ← Business logic
│   └── RequestService.java
│
└── util/
    └── AuthUtils.java            ← Helper đọc JWT claims
```

---

## 3. Luồng Request-Response Chi Tiết

Lấy ví dụ với API: **`GET /ttcrs/api/v1/requests?statuses=PENDING&type=OF&page=0&size=10`**

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          CLIENT (Frontend / Postman)                     │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │  HTTP GET /ttcrs/api/v1/requests
                                 │  Header: Authorization: Bearer <JWT Token>
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY (port 8080)                         │
│   - Kiểm tra JWT hợp lệ (Keycloak)                                      │
│   - Forward request đến ttcrs service (port 8093)                       │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │  Forward với JWT header nguyên vẹn
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY FILTER CHAIN                          │
│            (SecurityConfig.java — publicApiFilterChain)                  │
│                                                                          │
│  1. Match URL: /ttcrs/api/** → dùng publicApiFilterChain                │
│  2. JwtDecoder giải mã Bearer token qua Keycloak JWKS endpoint          │
│  3. JwtAuthenticationConverter trích xuất roles từ JWT:                 │
│     - realm_access.roles → ví dụ: [TTCRS_DISPATCHER, TTCRS_ADMIN]      │
│     - resource_access.<client>.roles (nếu có)                           │
│     - Thêm prefix "ROLE_" → [ROLE_TTCRS_DISPATCHER, ...]               │
│  4. Kiểm tra URL /ttcrs/api/v1/requests/** với UrlProperties:           │
│     - Yêu cầu TTCRS_ADMIN | TTCRS_PLANNER | TTCRS_DISPATCHER | ...     │
│  ✓ Nếu hợp lệ → tiếp tục | ✗ Nếu thiếu role → 403 Forbidden          │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │  SecurityContext được thiết lập
                                 │  (chứa JWT và danh sách authorities)
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│              RequestController.java — @GetMapping                        │
│                                                                          │
│  @ModelAttribute RequestFilterDTO filter                                 │
│       └─ Spring tự bind query params vào DTO:                           │
│          filter.statuses  = [PENDING]                                    │
│          filter.type      = OF                                           │
│          filter.page      = 0                                            │
│          filter.size      = 10                                           │
│          filter.sortBy    = "createdAt" (default)                        │
│          filter.sortDirection = "desc" (default)                         │
│                                                                          │
│  → Gọi: requestService.getRequests(filter)                               │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                  RequestService.java — getRequests()                     │
│                                                                          │
│  Bước 1: Lấy tenantId từ JWT                                            │
│    AuthUtils.getCurrentTenantId()                                        │
│       └─ SecurityContextHolder → JWT → claim "tid" → Long tenantId      │
│    Nếu không có "tid" → throw IllegalStateException (401 logic)          │
│                                                                          │
│  Bước 2: Build Specification (dynamic WHERE clause)                      │
│    Specification.where(withTenantId(tenantId))     ← BẮT BUỘC          │
│               .and(withStatuses([PENDING]))         ← IN clause         │
│               .and(withType(OF))                    ← = clause          │
│               .and(withSrcLocationCode(null))       ← bỏ qua           │
│               .and(withDestLocationCode(null))      ← bỏ qua           │
│               .and(withCreatedBetween(null, null))  ← bỏ qua           │
│                                                                          │
│  Bước 3: Build Pageable                                                  │
│    Sort.Direction.DESC, field = "createdAt"                              │
│    PageRequest.of(0, 10, sort)                                           │
│                                                                          │
│  Bước 4: Query database                                                  │
│    requestRepository.findAll(spec, pageable)                             │
│       └─ Page<RequestEntity>                                             │
│    .map(RequestResponseDTO::fromEntity)                                  │
│       └─ Page<RequestResponseDTO>                                        │
│                                                                          │
│  Bước 5: Wrap vào PageResponse                                           │
│    PageResponse.from(resultPage)                                         │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│              RequestRepository.java — findAll(spec, pageable)            │
│                                                                          │
│  Kế thừa JpaSpecificationExecutor                                        │
│  Hibernate dịch Specification → SQL:                                     │
│                                                                          │
│  SELECT r.*                                                              │
│  FROM   requests r                                                       │
│  WHERE  r.tenant_id = ?              ← tenantId từ JWT                  │
│    AND  r.status IN ('PENDING')      ← statuses filter                  │
│    AND  r.type = 'OF'               ← type filter                       │
│  ORDER BY r.created_stamp DESC                                           │
│  LIMIT 10 OFFSET 0                                                       │
│                                                                          │
│  Kết quả: List<RequestEntity>                                            │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     Response Assembly (trở về Controller)                │
│                                                                          │
│  RequestResponseDTO.fromEntity(entity)  ← map từng entity sang DTO     │
│  PageResponse.from(page)                ← wrap metadata phân trang      │
│  ApiResponse.ok(pageResponse)           ← wrap envelope chung           │
│                                                                          │
│  HTTP 200 OK                                                             │
│  Content-Type: application/json                                          │
│  Body:                                                                   │
│  {                                                                       │
│    "success": true,                                                      │
│    "message": "OK",                                                      │
│    "data": {                                                             │
│      "items": [ { "id": 1, "status": "PENDING", "type": "OF", ... } ], │
│      "page": 0,                                                          │
│      "size": 10,                                                         │
│      "totalElements": 42,                                                │
│      "totalPages": 5,                                                    │
│      "last": false                                                       │
│    }                                                                     │
│  }                                                                       │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 ▼
                          CLIENT nhận response
```

---

## 4. Vai Trò Từng File

### 📁 config/

#### `GlobalConfig.java`
**Mục đích**: Cấu hình bảo mật toàn cục, đọc từ `application.yaml`.

| Property | Ý nghĩa |
|----------|---------|
| `serpServiceRole` | Role cho internal service calls (e.g. `SERP_SERVICES`) |
| `rolePrefix` | Tiền tố Spring Security thêm vào role (`ROLE_`) |

---

#### `KeycloakConfig.java`
**Mục đích**: Chứa tất cả cấu hình Keycloak, đọc từ `keycloak.properties` (file tách biệt để không commit secret).

| Property | Ý nghĩa |
|----------|---------|
| `jwkSetUri` | URL endpoint lấy public key từ Keycloak để verify JWT signature |
| `expectedIssuer` | Kiểm tra JWT có được cấp bởi đúng realm không |
| `realmAccess` | Tên claim trong JWT chứa realm roles (`realm_access`) |
| `resourceAccess` | Tên claim trong JWT chứa client roles (`resource_access`) |
| `rolesAttribute` | Tên key trong map roles (`roles`) |

---

#### `JpaDataSourceConfig.java`
**Mục đích**: Cấu hình toàn bộ database stack thủ công thay vì dùng Spring Boot auto-config.

**Tại sao cấu hình thủ công?** → Để có thể chỉ định chính xác schema, connection pool, và đảm bảo Flyway chạy trước khi EntityManager khởi tạo.

| Bean | Vai trò |
|------|---------|
| `DataSource` | HikariCP connection pool, đọc config từ `database.properties` |
| `flyway` | Chạy SQL migration từ `classpath:db/migration` khi startup |
| `entityManagerFactory` | Scan entity từ package `com.example.ttcrs.entity` |
| `transactionManager` | Quản lý transaction JPA |

> **Quan trọng**: `entityManagerFactory` có `@DependsOn("flyway")` — đảm bảo migration bảng chạy xong **trước** khi Hibernate validate schema.

---

#### `SecurityConfig.java`
**Mục đích**: Định nghĩa 2 chuỗi filter bảo mật song song.

**Filter Chain 1 — `internalApiFilterChain` (`@Order(1)`)**
- Match: `/internal/**`
- Dùng cho: Service-to-service calls (không có user context)
- Yêu cầu: role `SERP_SERVICES` (client credential token)
- Converter: `serviceJwtAuthenticationConverter` — đọc `azp` / `client_id`

**Filter Chain 2 — `publicApiFilterChain` (`@Order(2)`)**
- Match: `/ttcrs/api/**`
- Dùng cho: Request từ user qua API Gateway
- Yêu cầu: role theo từng URL, được config trong `UrlProperties`
- Converter: `jwtAuthenticationConverter` — đọc `realm_access.roles` và `resource_access.<client>.roles`

---

#### `UrlProperties.java`
**Mục đích**: Bind cấu hình URL phân quyền từ `application.yaml` sang Java object.

```yaml
# application.yaml
security:
  urls:
    protected-urls:
      - url-pattern: /ttcrs/api/v1/requests/**
        roles: [SUPER_ADMIN, TTCRS_ADMIN, TTCRS_PLANNER, TTCRS_DISPATCHER, TTCRS_DRIVER]
```

`SecurityConfig` đọc list này và gọi `.hasAnyRole(...)` tương ứng — không hardcode role trong code Java.

---

### 📁 constant/

Tất cả đều là `enum` — đại diện cho tập giá trị cố định trong domain.

| File | Enum values | Dùng ở đâu |
|------|------------|------------|
| `RequestStatus.java` | `PENDING`, `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` | Trạng thái của Request vận chuyển |
| `RequestType.java` | `OF` (Outbound Full), `IF` (Inbound Full), `OE` (Outbound Empty), `IE` (Inbound Empty) | Loại lệnh vận chuyển |
| `TransportPlanStatus.java` | `CREATED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` | Trạng thái của Transport Plan |
| `StopAction.java` | `PICKUP`, `DELIVER`, `PICKUP_AND_DELIVER` | Hành động tại mỗi điểm dừng trong route |
| `ContainerSize.java` | `CONTAINER_20`, `CONTAINER_40` | Kích thước container |
| `DriverStatus.java` | `AVAILABLE`, `ON_DUTY`, `OFF_DUTY` | Trạng thái tài xế |
| `VehicleStatus.java` | `AVAILABLE`, `IN_USE`, `MAINTENANCE` | Trạng thái xe |
| `LocationType.java` | `PORT`, `DEPOT`, `CUSTOMER`, `FACTORY` | Phân loại địa điểm |

---

### 📁 entity/

Mỗi entity ánh xạ 1:1 với một bảng trong database (schema `ttcrs`).

#### `RequestEntity.java` — Bảng `requests`
Lệnh vận chuyển cốt lõi. Một Request đại diện cho yêu cầu vận chuyển hàng từ nguồn (`srcLocationCode`) đến đích (`destLocationCode`).

| Field | Ý nghĩa |
|-------|---------|
| `tenantId` | Multi-tenant isolation — mọi query đều lọc theo field này |
| `customerId` | Khách hàng đặt lệnh (có thể null nếu nội bộ) |
| `srcLocationCode` / `destLocationCode` | Mã địa điểm nguồn/đích |
| `earlyAtSrc` / `lateAtSrc` | Khung giờ có thể nhận hàng tại nguồn (time window) |
| `earlyAtDest` / `lateAtDest` | Khung giờ giao hàng tại đích |
| `weight` | Trọng lượng hàng |
| `containerSize` | Kích thước container (20ft / 40ft) |
| `dropTrailerRequired` | Có cần dừng lại để đổi rơ-moóc không |
| `status` | Trạng thái hiện tại (mặc định: `PENDING`) |
| `type` | Loại lệnh (OF/IF/OE/IE) |
| `transportPlanId` | FK đến Transport Plan đã xếp lệnh này (null nếu chưa plan) |
| `createdBy` | User ID tạo lệnh |

---

#### `TransportPlanEntity.java` — Bảng `transport_plans`
Một chuyến vận chuyển được lên kế hoạch, gắn với 1 xe + 1 tài xế.

| Field | Ý nghĩa |
|-------|---------|
| `truckId` | Xe tải được phân công |
| `driverId` | Tài xế được phân công |
| `startTime` / `endTime` | Thời gian bắt đầu/kết thúc chuyến |
| `status` | Trạng thái (mặc định: `CREATED`) |

---

#### `TransportPlanStopEntity.java` — Bảng `transport_plan_stops`
Danh sách các điểm dừng theo thứ tự trong một chuyến.

| Field | Ý nghĩa |
|-------|---------|
| `transportPlanId` | FK đến TransportPlan |
| `sequence` | Thứ tự dừng (unique cùng transportPlanId) |
| `locationCode` | Mã địa điểm dừng |
| `requestId` | Request liên quan tại điểm này (null nếu depot) |
| `trailerId` | Rơ-moóc dùng tại điểm này (drop trailer scenario) |
| `action` | Hành động: PICKUP / DELIVER / PICKUP_AND_DELIVER |
| `plannedArrivalTime` | Giờ dự kiến đến |
| `actualArrivalTime` | Giờ thực tế đến (tài xế check-in) |

---

#### Các entity tham chiếu (dữ liệu master)

| Entity | Bảng | Mô tả |
|--------|------|-------|
| `LocationEntity` | `locations` | Địa điểm (cảng, kho, khách hàng) |
| `TruckEntity` | `trucks` | Xe tải trong đội |
| `TrailerEntity` | `trailers` | Rơ-moóc trong đội |
| `DriverEntity` | `drivers` | Tài xế |
| `ContainerEntity` | `containers` | Container đang quản lý |
| `DistanceEntity` | `distances` | Ma trận khoảng cách giữa các location |

---

### 📁 dto/

#### `RequestFilterDTO.java` (Input)
DTO nhận query params từ HTTP request. `@ModelAttribute` trong controller cho phép Spring tự động bind từng param vào đúng field.

| Field | Nguồn | Default |
|-------|-------|---------|
| `statuses` | `?statuses=PENDING&statuses=PLANNED` | null (lấy tất cả) |
| `type` | `?type=OF` | null |
| `srcLocationCode` | `?srcLocationCode=PORT_HCM` | null |
| `createdFrom` / `createdTo` | `?createdFrom=2025-01-01T00:00:00` | null |
| `page` / `size` | `?page=0&size=20` | 0 / 20 |
| `sortBy` / `sortDirection` | `?sortBy=createdAt&sortDirection=desc` | `createdAt` / `desc` |

---

#### `ApiResponse<T>.java` (Output — envelope)
Wrapper chuẩn cho **mọi** API response. Client luôn nhận cấu trúc nhất quán:
```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

---

#### `PageResponse<T>.java` (Output — phân trang)
Wrapper cho kết quả có phân trang:
```json
{
  "items": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

---

#### `RequestResponseDTO.java` (Output — data)
Chuyển đổi `RequestEntity` sang JSON-safe DTO. Dùng static method `fromEntity()` thay vì MapStruct để tránh thêm dependency. Chỉ expose các field cần thiết (ví dụ: ẩn `evidenceAtSrc`, `evidenceAtDest`).

---

### 📁 repository/

#### `RequestRepository.java`
Interface kế thừa:
- `JpaRepository<RequestEntity, Long>` → CRUD cơ bản
- `JpaSpecificationExecutor<RequestEntity>` → Dynamic query với Specification

**Inner class `RequestSpecs`** chứa các static method tạo `Specification`:

| Method | SQL tương đương |
|--------|----------------|
| `withTenantId(id)` | `WHERE tenant_id = ?` |
| `withStatuses(list)` | `AND status IN (...)` |
| `withType(type)` | `AND type = ?` |
| `withSrcLocationCode(code)` | `AND LOWER(src_location_code) = LOWER(?)` |
| `withDestLocationCode(code)` | `AND LOWER(dest_location_code) = LOWER(?)` |
| `withCreatedBetween(from, to)` | `AND created_stamp BETWEEN ? AND ?` |

Mỗi Spec **tự xử lý null** — trả về `cb.conjunction()` (TRUE) nếu không có filter → không tạo WHERE clause.

---

### 📁 service/

#### `RequestService.java`
Tầng Business Logic. Không truy cập database trực tiếp, chỉ qua Repository.

**Trách nhiệm:**
1. **Tenant isolation**: Luôn lấy `tenantId` từ JWT trước khi query
2. **Build specification**: Compose các Spec từ filter của client
3. **Validate sorting**: Whitelist các field được phép sort — tránh HQL injection
4. **Map kết quả**: `RequestEntity` → `RequestResponseDTO` → `PageResponse`

---

### 📁 util/

#### `AuthUtils.java`
Helper duy nhất để đọc thông tin từ JWT trong `SecurityContext`.

| Method | JWT Claim | Trả về |
|--------|-----------|--------|
| `getCurrentJwt()` | — | `Optional<Jwt>` |
| `getCurrentUserId()` | `uid` | `Optional<Long>` |
| `getCurrentTenantId()` | `tid` | `Optional<Long>` — **quan trọng nhất** |
| `getCurrentUserEmail()` | `email` | `Optional<String>` |
| `getRealmRoles()` | `realm_access.roles` | `List<String>` |
| `getClientRoles(clientId)` | `resource_access.<id>.roles` | `List<String>` |
| `hasRealmRole(role)` | — | `boolean` |
| `canAccessOrganization(orgId)` | — | `boolean` (bypass nếu SUPER_ADMIN) |

---

## 5. Security Flow Chi Tiết

```
JWT Token (ví dụ payload):
{
  "sub": "keycloak-user-uuid",
  "uid": 42,                          ← user ID trong hệ thống
  "tid": 7,                           ← tenant ID
  "realm_access": {
    "roles": ["TTCRS_DISPATCHER", "default-roles-serp"]
  },
  "resource_access": {
    "ttcrs-client": {
      "roles": ["TTCRS_DISPATCHER"]
    }
  }
}

                    ↓  JwtDecoder verify signature với JWKS
                    ↓  JwtAuthenticationConverter extract roles
                    ↓  Thêm prefix "ROLE_"

Spring SecurityContext:
  Principal: JwtAuthenticationToken
  Authorities: [ROLE_TTCRS_DISPATCHER, ROLE_default-roles-serp, ...]

                    ↓  SecurityConfig kiểm tra URL

URL: /ttcrs/api/v1/requests/**
Required roles: [TTCRS_ADMIN | TTCRS_PLANNER | TTCRS_DISPATCHER | ...]
User has: TTCRS_DISPATCHER → ✓ PASS

                    ↓  Request vào Controller

AuthUtils.getCurrentTenantId() = 7   (từ claim "tid")
→ Query: WHERE tenant_id = 7 ...
```

---

## 6. Cơ Chế Tenant Isolation

Đây là tính năng bảo mật quan trọng nhất trong hệ thống multi-tenant:

```
Tenant A (id=1) ─── chỉ thấy requests của Tenant A
Tenant B (id=2) ─── chỉ thấy requests của Tenant B
         │
         └── Đảm bảo bởi: withTenantId(tenantId) trong mọi Specification
             → Không thể bypass vì tenantId lấy từ JWT (server kiểm soát)
             → Không nhận tenantId từ client (query param, body)
```

---

## 7. Database Migration

Flyway tự động chạy khi startup, theo thứ tự file:
```
src/main/resources/db/migration/
  V1__Initial_schema.sql   ← Tạo tất cả bảng lần đầu
  V2__...sql               ← Migration tiếp theo (nếu có)
```

`JpaDataSourceConfig` đảm bảo Flyway chạy xong **trước** khi Hibernate khởi tạo EntityManager.

---

## 8. Tóm Tắt Tầng Kiến Trúc

```
┌────────────────────────────────────────────────┐
│              HTTP Layer                        │
│  Controller — nhận request, trả response       │
│  DTO (request) — bind query params             │
│  DTO (response) — format output JSON           │
├────────────────────────────────────────────────┤
│              Security Layer                    │
│  SecurityConfig — filter chain, role check     │
│  KeycloakConfig — JWT verify config            │
│  AuthUtils — đọc claims từ SecurityContext     │
├────────────────────────────────────────────────┤
│              Business Layer                    │
│  Service — business logic, tenant isolation    │
│  Specification — dynamic query building        │
├────────────────────────────────────────────────┤
│              Data Layer                        │
│  Repository — Spring Data JPA                  │
│  Entity — ánh xạ bảng database                │
│  Flyway — migration schema                     │
├────────────────────────────────────────────────┤
│              Config Layer                      │
│  JpaDataSourceConfig — DataSource + Flyway     │
│  GlobalConfig — security constants             │
│  UrlProperties — URL role mapping từ YAML      │
└────────────────────────────────────────────────┘
```
