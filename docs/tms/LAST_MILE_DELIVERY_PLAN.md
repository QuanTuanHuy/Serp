# Last-Mile Delivery — Implementation Plan

Author: SERP Project  
Status: **PLANNED — awaiting implementation**

Kế hoạch này dành cho AI coding agents triển khai chặng cuối (last-mile): gỡ bag tại bưu cục đích → phân công bưu tá → tối ưu lộ trình → giao đến người nhận → thu tiền COD và phí giao hàng.

> **Kiến trúc quyết định:** Toàn bộ luồng last-mile được tích hợp trực tiếp vào service `first-mile` (port `8093`). Không tạo service riêng.

---

## 1. Hiện trạng và Vấn đề

Luồng đơn hàng hiện tại dừng ở `INBOUND_AT_DESTINATION_POST_OFFICE` sau khi bag đến bưu cục đích.

**Các điểm còn thiếu:**

1. `OrderOperationView` trong `tms-order` **chưa expose** receiver fields và COD fields ra internal API — cần bổ sung để `first-mile` có thể đọc khi tạo delivery manifest.
2. Không có trạng thái đơn sau khi đến bưu cục đích (`READY_FOR_DELIVERY`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNED_TO_SENDER`).
3. `first-mile/enums/OrderStatus.java` thiếu đồng bộ: thiếu `BAG_IN_TRANSIT`, `INBOUND_AT_DESTINATION_HUB`, `INBOUND_AT_DESTINATION_POST_OFFICE` + 5 trạng thái last-mile.
4. `TmsOrderClient` trong first-mile chưa có method lookup theo bưu cục đích.
5. Không có luồng gỡ bag, phân loại đơn, tạo lộ trình giao trong first-mile.
6. Không có ghi nhận thu tiền COD và phí giao khi người nhận trả.

---

## 2. Kiến trúc Mục tiêu

```
[second-mile]
    Bag → ARRIVED
    Order → INBOUND_AT_DESTINATION_POST_OFFICE
           |
           ▼  (scan/confirm tại bưu cục đích — last-mile service)
    READY_FOR_DELIVERY
           |
           ▼  (tạo delivery manifest + tối ưu lộ trình)
    OUT_FOR_DELIVERY
           |
    ┌──────┴──────────┐
    ▼                 ▼
DELIVERED       DELIVERY_FAILED
                      |
               (retry hoặc sau N lần)
                      ▼
              RETURNED_TO_SENDER
```

**Tích hợp vào `first-mile`** — Spring Boot, Java 21, port `8093` (đã tồn tại).  
Thêm last-mile domain vào đúng kiến trúc Clean Architecture đang có của `first-mile`.

```
Serp/
  first-mile/          ← SỬA: thêm last-mile domain (delivery manifest, COD, route opt)
  tms-order/           ← SỬA: thêm receiver fields + new statuses
  serp_web/            ← SỬA: thêm UI last-mile
```

---

## 3. Phase 1 — `tms-order`: Bổ sung trạng thái & mở rộng lookup

### 3.1 Receiver fields — ĐÃ HOÀN THÀNH

> **Không cần làm gì.** `Order.java` đã có đầy đủ: `receiverName`, `receiverPhone`, `receiverWardCode`, `receiverProvinceCode`, `receiverAddressDetail`, `receiverLocation`, `destinationPostOfficeCode`, `codAmount`, `feePayer`, `totalShippingFee`, `paymentStatus`. Không cần migration thêm cột.

**Việc CÒN thiếu:** `OrderOperationView` chưa expose các trường này ra internal API.

**File:** `tms-order/src/main/java/serp/project/tms_order/dto/response/OrderOperationView.java`  
Thêm vào record:

```java
// Receiver info
String receiverName,
String receiverPhone,
String receiverWardCode,
String receiverProvinceCode,
String receiverAddressDetail,
Double receiverLatitude,
Double receiverLongitude,
// COD & fee
Long codAmount,
Long totalShippingFee,
String feePayer,        // FeePayer enum name
String paymentStatus,   // PaymentStatus enum name
// Destination
String destinationPostOfficeCode,
```

**File:** `tms-order/src/main/java/serp/project/tms_order/mapper/OrderOperationMapper.java`  
Map các field mới từ `Order` sang `OrderOperationView` (trích `receiverLatitude/Longitude` từ `receiverLocation` Point bằng `ST_Y`/`ST_X` hoặc getter tương tự sender).

**Migration SQL** — **KHÔNG CẦN** (cột đã tồn tại trong DB).

### 3.2 Bổ sung `OrderStatus` mới

**File:** `tms-order/src/main/java/serp/project/tms_order/enums/OrderStatus.java`

Thêm vào cuối enum (trước `CANCELLED`):

```java
READY_FOR_DELIVERY,          // Đã kiểm hàng tại bưu cục đích, sẵn sàng giao
OUT_FOR_DELIVERY,            // Bưu tá đang trên đường giao
DELIVERED,                   // Giao thành công
DELIVERY_FAILED,             // Giao thất bại (vắng nhà, địa chỉ sai...) — có thể thử lại
RETURNED_TO_SENDER,          // Hoàn hàng người gửi (sau khi hết lần thử)
```

**Lưu ý:** `second-mile/enums/OrderStatus.java` và `first-mile/enums/OrderStatus.java` là **bản copy cục bộ** — cần đồng bộ:

- `second-mile` đang đồng bộ với `tms-order` → chỉ thêm 5 trạng thái trên.
- `first-mile` **hiện thiếu nhiều hơn**: so với `tms-order` nó đang thiếu `BAG_IN_TRANSIT`, `INBOUND_AT_DESTINATION_HUB`, `INBOUND_AT_DESTINATION_POST_OFFICE` và chưa có 5 trạng thái mới. Cần thêm đủ **8 trạng thái** vào `first-mile/enums/OrderStatus.java`:

```java
// Các trạng thái đang thiếu trong first-mile (cần thêm trước CANCELLED):
BAG_IN_TRANSIT,
INBOUND_AT_DESTINATION_HUB,
INBOUND_AT_DESTINATION_POST_OFFICE,
READY_FOR_DELIVERY,
OUT_FOR_DELIVERY,
DELIVERED,
DELIVERY_FAILED,
RETURNED_TO_SENDER,
```

### 3.3 Bổ sung transition rules

**File:** `tms-order/src/main/java/serp/project/tms_order/service/impl/OrderTransitionServiceImpl.java`

Trong block `static { ... }`, thêm sau `INBOUND_AT_DESTINATION_POST_OFFICE`:

```java
ALLOWED_PREVIOUS_STATUSES.put(
    OrderStatus.READY_FOR_DELIVERY,
    EnumSet.of(OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE)
);
ALLOWED_PREVIOUS_STATUSES.put(
    OrderStatus.OUT_FOR_DELIVERY,
    EnumSet.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED)
);
ALLOWED_PREVIOUS_STATUSES.put(
    OrderStatus.DELIVERED,
    EnumSet.of(OrderStatus.OUT_FOR_DELIVERY)
);
ALLOWED_PREVIOUS_STATUSES.put(
    OrderStatus.DELIVERY_FAILED,
    EnumSet.of(OrderStatus.OUT_FOR_DELIVERY)
);
ALLOWED_PREVIOUS_STATUSES.put(
    OrderStatus.RETURNED_TO_SENDER,
    EnumSet.of(OrderStatus.DELIVERY_FAILED)
);
```

### 3.4 Mở rộng `InternalOrderLookupRequest`

**File:** `tms-order/src/main/java/serp/project/tms_order/dto/request/InternalOrderLookupRequest.java`

Đảm bảo có field `destinationPostOfficeCode` (hoặc `destinationPostOfficeCodes`) để last-mile có thể query đơn tại bưu cục đích. Nếu chưa có, bổ sung:

```java
private String destinationPostOfficeCode;
private List<OrderStatus> statuses;  // filter theo trạng thái
```

Và cập nhật `OrderQueryServiceImpl.lookupOrders(...)` tương ứng.

---

## 4. Phase 2 — Thêm last-mile module vào `first-mile`

Không tạo service mới. Toàn bộ code last-mile được đặt trong package `serp.project.first_mile` và DB `first-mile` hiện có.

### 4.1 Các file mới thêm vào `first-mile`

```
first-mile/src/main/java/serp/project/first_mile/
  ui/controller/
    DeliveryManifestController.java        ← NEW
    OrderSortingController.java            ← NEW
    InternalLastMileController.java        ← NEW
  service/
    DeliveryManifestService.java           ← NEW (interface)
    OrderSortingService.java               ← NEW (interface)
    DeliveryRouteOptimizationService.java  ← NEW (interface)
    CodCollectionService.java              ← NEW (interface)
    # TmsOrderTransitionOutboxService.java ĐÃ TỒN TẠI — không cần tạo lại
    impl/
      DeliveryManifestServiceImpl.java     ← NEW
      OrderSortingServiceImpl.java         ← NEW
      DeliveryRouteOptimizationServiceImpl.java  ← NEW
      CodCollectionServiceImpl.java        ← NEW
      # TmsOrderTransitionOutboxServiceImpl.java ĐÃ TỒN TẠI — không tạo lại
  domain/
    DeliveryManifest.java                  ← NEW
    DeliveryManifestOrder.java             ← NEW
    # DeliveryOrderTransitionOutbox.java — KHÔNG CẦN TẠO
    # Reuse entity OrderTransitionOutbox đã có, phân biệt bằng source = "LAST_MILE_DELIVERY"
  dto/
    request/
      CreateDeliveryManifestRequest.java   ← NEW
      ConfirmDeliveryRequest.java          ← NEW
      ConfirmDeliveryFailureRequest.java   ← NEW
      ReturnToSenderRequest.java           ← NEW
      SortInboundOrdersRequest.java        ← NEW
    response/
      DeliveryManifestResponse.java        ← NEW
      DeliveryManifestOrderResponse.java   ← NEW
      InboundOrderResponse.java            ← NEW
  enums/
    DeliveryManifestStatus.java            ← NEW
    DeliveryOrderStatus.java               ← NEW
  repository/
    DeliveryManifestRepository.java        ← NEW
    DeliveryManifestOrderRepository.java   ← NEW
    # DeliveryOrderTransitionOutboxRepository.java — KHÔNG CẦN TẠO
    # Dùng OrderTransitionOutboxRepository đã có
  caller/
    # TmsOrderClient.java đã TỒN TẠI — chỉ thêm method mới vào đây, không tạo file mới
    dto/tms_order/
      # Cập nhật TmsOrderOperationView.java thêm receiver/COD fields  ← SỬA
      # Cập nhật TmsOrderLookupRequest.java thêm destinationPostOfficeCode+statuses  ← SỬA
  kernel/utils/
    DeliveryRouteOptimizationUtils.java    ← NEW (Nearest Neighbor + 2-opt)
    HaversineUtils.java                    ← NEW (nếu chưa có)

first-mile/src/main/resources/
  db/migration/
    delivery-manifests.sql          ← NEW  (không dùng Flyway V<n>__ prefix)
    delivery-manifest-orders.sql    ← NEW
    # Sau đó đăng ký vào application.yaml: spring.sql.init.schema-locations
  i18n/
    messages.properties       ← SỬA: thêm delivery message keys
    messages_vi.properties    ← SỬA: thêm delivery message keys
    messages_en.properties    ← SỬA: thêm delivery message keys
```

> **`TmsOrderClient.java` đã tồn tại** tại `first-mile/caller/TmsOrderClient.java` với `lookupByIds`, `lookupByCodes`, `applyStatusTransitions`. **Không tạo file mới.** Chỉ thêm method mới:
>
> - `lookupAtPostOffice(String postOfficeCode, List<OrderStatus> statuses, Long tenantId)` — tìm đơn theo bưu cục đích + trạng thái.
> - `updatePaymentStatus(String orderCode, Long tenantId, String paymentStatus)` — sau khi giao thành công.

### 4.2 Bổ sung cấu hình vào `application.yaml` hiện có

File: `first-mile/src/main/resources/application.yaml`.

> **Lưu ý cấu trúc config hiện có:**
>
> - Config kết nối tms-order đã nằm ở key `tms-order.service.base-url` (không phải `app.tms-order`).
> - DB migration dùng `spring.sql.init.schema-locations` (plain SQL), **KHÔNG dùng Flyway**.
> - Security config đã có (JWT từ Keycloak).

Chỉ cần **thêm 2 việc**:

**1. Đăng ký migration SQL mới** vào `spring.sql.init.schema-locations`:

```yaml
spring:
  sql:
    init:
      schema-locations:
        # ... (giữ các entry đã có) ...
        - classpath:db/migration/delivery-manifests.sql
        - classpath:db/migration/delivery-manifest-orders.sql
```

**2. Thêm config delivery** vào block `app:` hiện có:

```yaml
app:
  # ... (các config đã có giữ nguyên) ...
  delivery:
    max-attempts: ${MAX_DELIVERY_ATTEMPTS:3}
    manifest-code-prefix: DM
```

> `tms-order.service.base-url` đã có sẵn, không cần thêm.

---

## 5. Phase 3 — Domain Model & DB Schema

### 5.1 Enums

**File:** `first-mile/src/main/java/serp/project/first_mile/enums/DeliveryManifestStatus.java`

```java
public enum DeliveryManifestStatus {
    CREATED,          // Đã tạo, chưa khởi hành
    IN_PROGRESS,      // Bưu tá đang đi giao
    COMPLETED,        // Tất cả đơn đã được xử lý (giao/thất bại)
    CANCELLED
}
```

**File:** `first-mile/src/main/java/serp/project/first_mile/enums/DeliveryOrderStatus.java` (trạng thái đơn _trong_ manifest, tách riêng với `OrderStatus` toàn cục)

```java
public enum DeliveryOrderStatus {
    PENDING,         // Chờ giao
    OUT_FOR_DELIVERY,// Đang được giao trong lượt này
    DELIVERED,       // Giao thành công
    FAILED,          // Giao thất bại lần này
    RESCHEDULED,     // Đã xếp vào manifest khác để giao lại
    RETURNED         // Đã hoàn người gửi
}
```

### 5.2 Entity `DeliveryManifest`

**File:** `first-mile/src/main/java/serp/project/first_mile/domain/DeliveryManifest.java`

```java
@Entity @Table(name = "delivery_manifests")
public class DeliveryManifest extends AbstractAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private String manifestCode;          // Mã phiếu, e.g. DM-20260607-0001
    private String postOfficeCode;        // Bưu cục đích (nguồn giao hàng)
    private Long courierId;               // ID bưu tá (staff từ first-mile)
    private String courierName;           // Cache tên bưu tá
    private String vehicleId;             // Phương tiện

    @Enumerated(EnumType.STRING)
    private DeliveryManifestStatus status;

    private LocalDate plannedDate;
    private LocalDateTime plannedDepartureAt;
    private LocalDateTime actualDepartureAt;
    private LocalDateTime actualReturnAt;

    private Integer totalOrders;
    private Integer deliveredCount;
    private Integer failedCount;

    private Long totalCodAmount;          // Tổng COD cần thu
    private Long collectedCodAmount;      // Tổng COD đã thu
    private Long totalShippingFee;        // Tổng phí giao khi feePayer = RECEIVER
    private Long collectedShippingFee;    // Tổng phí giao đã thu

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private RouteGeoJson routeGeoJson;    // Lộ trình tối ưu (GeoJSON LineString)

    private String note;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryManifestOrder> orders = new ArrayList<>();
}
```

### 5.3 Entity `DeliveryManifestOrder`

**File:** `first-mile/src/main/java/serp/project/first_mile/domain/DeliveryManifestOrder.java`

```java
@Entity @Table(name = "delivery_manifest_orders")
public class DeliveryManifestOrder extends AbstractAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id")
    private DeliveryManifest manifest;

    private Long orderId;                 // FK → tms-order (không có JPA join cross-service)
    private String orderCode;

    private Integer sequence;            // Thứ tự giao trong lộ trình (từ thuật toán)
    private Integer deliveryAttemptCount; // Tổng số lần đã thử giao (across manifests)

    @Enumerated(EnumType.STRING)
    private DeliveryOrderStatus status;

    // Cache thông tin người nhận (lấy từ tms-order khi tạo manifest)
    private String receiverName;
    private String receiverPhone;
    private String receiverAddressDetail;
    private String receiverWardCode;
    private String receiverProvinceCode;
    private Double receiverLat;
    private Double receiverLng;

    // Tài chính
    private Long codAmount;              // Số tiền COD cần thu
    private Long codCollected;           // Số tiền COD thực thu (0 nếu chưa thu)
    private Long shippingFee;            // Phí giao hàng (chỉ áp dụng khi feePayer=RECEIVER)
    private Long shippingFeeCollected;   // Phí giao thực thu

    private String feePayer;             // SENDER / RECEIVER

    // Kết quả
    private String proofPhotoUrl;        // Ảnh chụp bằng chứng giao hàng
    private String failureReason;        // Lý do thất bại
    private LocalDateTime deliveredAt;
    private String note;
}
```

### 5.4 Migration SQL

**`V1__create_delivery_manifests.sql`**

```sql
CREATE TABLE delivery_manifests (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             BIGINT        NOT NULL,
    manifest_code         VARCHAR(50)   NOT NULL,
    post_office_code      VARCHAR(50)   NOT NULL,
    courier_id            BIGINT,
    courier_name          VARCHAR(255),
    vehicle_id            VARCHAR(50),
    status                VARCHAR(50)   NOT NULL DEFAULT 'CREATED',
    planned_date          DATE          NOT NULL,
    planned_departure_at  TIMESTAMP,
    actual_departure_at   TIMESTAMP,
    actual_return_at      TIMESTAMP,
    total_orders          INT           NOT NULL DEFAULT 0,
    delivered_count       INT           NOT NULL DEFAULT 0,
    failed_count          INT           NOT NULL DEFAULT 0,
    total_cod_amount      BIGINT        NOT NULL DEFAULT 0,
    collected_cod_amount  BIGINT        NOT NULL DEFAULT 0,
    total_shipping_fee    BIGINT        NOT NULL DEFAULT 0,
    collected_shipping_fee BIGINT       NOT NULL DEFAULT 0,
    route_geo_json        JSONB,
    note                  TEXT,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    UNIQUE (tenant_id, manifest_code)
);

CREATE INDEX idx_dm_tenant_post_office ON delivery_manifests (tenant_id, post_office_code);
CREATE INDEX idx_dm_tenant_courier     ON delivery_manifests (tenant_id, courier_id);
CREATE INDEX idx_dm_tenant_status      ON delivery_manifests (tenant_id, status);
```

**`V2__create_delivery_manifest_orders.sql`**

```sql
CREATE TABLE delivery_manifest_orders (
    id                     BIGSERIAL PRIMARY KEY,
    tenant_id              BIGINT       NOT NULL,
    manifest_id            BIGINT       NOT NULL REFERENCES delivery_manifests(id),
    order_id               BIGINT       NOT NULL,
    order_code             VARCHAR(100) NOT NULL,
    sequence               INT          NOT NULL DEFAULT 0,
    delivery_attempt_count INT          NOT NULL DEFAULT 0,
    status                 VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    receiver_name          VARCHAR(255),
    receiver_phone         VARCHAR(50),
    receiver_address_detail TEXT,
    receiver_ward_code     VARCHAR(50),
    receiver_province_code VARCHAR(50),
    receiver_lat           DOUBLE PRECISION,
    receiver_lng           DOUBLE PRECISION,
    cod_amount             BIGINT       NOT NULL DEFAULT 0,
    cod_collected          BIGINT       NOT NULL DEFAULT 0,
    shipping_fee           BIGINT       NOT NULL DEFAULT 0,
    shipping_fee_collected BIGINT       NOT NULL DEFAULT 0,
    fee_payer              VARCHAR(20),
    proof_photo_url        TEXT,
    failure_reason         VARCHAR(255),
    delivered_at           TIMESTAMP,
    note                   TEXT,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP
);

CREATE INDEX idx_dmo_manifest_id ON delivery_manifest_orders (manifest_id);
CREATE INDEX idx_dmo_order_code   ON delivery_manifest_orders (tenant_id, order_code);
CREATE INDEX idx_dmo_status       ON delivery_manifest_orders (tenant_id, status);
```

## 5.5 Reuse `OrderTransitionOutbox` (Outbox pattern)

Không cần entity mới. Dùng lại `OrderTransitionOutbox` và `TmsOrderTransitionOutboxService` đã có trong `first-mile`. Phân biệt delivery outbox bằng `source = "LAST_MILE_DELIVERY"` khi enqueue.

---

## 6. Phase 4 — Thuật toán Tối ưu Lộ trình Giao hàng

### 6.1 Lý do chọn thuật toán

Bưu tá thường giao **10–50 đơn/ngày** từ một bưu cục. Với quy mô nhỏ:

- **Nearest Neighbor (NN)** cho kết quả tốt trong < 1ms
- **2-opt improvement** cải thiện thêm ~10-15% chất lượng sau NN
- Không cần bài toán VRP đầy đủ (ALNS, OR-Tools) ở giai đoạn này

### 6.2 `HaversineUtils.java`

```java
// File: first-mile/src/main/java/serp/project/first_mile/kernel/utils/HaversineUtils.java
public final class HaversineUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineUtils() {}

    /**
     * Tính khoảng cách đường chim bay (km) giữa hai tọa độ.
     */
    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.asin(Math.sqrt(a));
    }
}
```

### 6.3 `DeliveryRouteOptimizationUtils.java`

```java
// File: first-mile/src/main/java/serp/project/first_mile/kernel/utils/DeliveryRouteOptimizationUtils.java

/**
 * Tối ưu lộ trình giao hàng last-mile bằng Nearest Neighbor + 2-opt.
 *
 * Input:
 *   - depotLat, depotLng: tọa độ bưu cục
 *   - stops: danh sách điểm giao (có lat/lng)
 *
 * Output:
 *   - Danh sách stops được sắp xếp theo thứ tự giao tối ưu
 *
 * Độ phức tạp:
 *   - Nearest Neighbor construction: O(n²)
 *   - 2-opt improvement: O(n²) mỗi pass, tối đa O(n²) pass → O(n³) worst case
 *   - Với n ≤ 50: < 5ms trên mọi phần cứng hiện đại
 */
public final class DeliveryRouteOptimizationUtils {

    private DeliveryRouteOptimizationUtils() {}

    public static <T extends DeliveryStop> List<T> optimize(
            double depotLat, double depotLng, List<T> stops) {

        if (stops == null || stops.isEmpty()) return List.of();
        if (stops.size() == 1) return List.copyOf(stops);

        List<T> route = nearestNeighbor(depotLat, depotLng, new ArrayList<>(stops));
        twoOpt(depotLat, depotLng, route);
        return route;
    }

    // ── STEP 1: Nearest Neighbor Construction ──────────────────────────────
    // Bắt đầu từ depot, mỗi bước chọn điểm giao chưa thăm gần nhất.
    private static <T extends DeliveryStop> List<T> nearestNeighbor(
            double depotLat, double depotLng, List<T> stops) {

        List<T> route = new ArrayList<>(stops.size());
        Set<T> remaining = new LinkedHashSet<>(stops);
        double curLat = depotLat, curLng = depotLng;

        while (!remaining.isEmpty()) {
            T nearest = null;
            double minDist = Double.MAX_VALUE;
            for (T stop : remaining) {
                double d = HaversineUtils.distanceKm(curLat, curLng,
                                                      stop.getLat(), stop.getLng());
                if (d < minDist) { minDist = d; nearest = stop; }
            }
            route.add(nearest);
            remaining.remove(nearest);
            curLat = nearest.getLat();
            curLng = nearest.getLng();
        }
        return route;
    }

    // ── STEP 2: 2-opt Improvement ──────────────────────────────────────────
    // Thử đảo ngược từng đoạn con [i..j] trong lộ trình.
    // Nếu đảo ngược làm giảm tổng quãng đường → giữ lại và lặp lại.
    private static <T extends DeliveryStop> void twoOpt(
            double depotLat, double depotLng, List<T> route) {

        int n = route.size();
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    double gain = twoOptGain(depotLat, depotLng, route, i, j);
                    if (gain > 1e-9) {  // dùng epsilon tránh floating-point noise
                        reverseSegment(route, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
    }

    /**
     * Tính độ giảm quãng đường nếu đảo đoạn [i+1 .. j].
     * gain > 0 → đáng đổi.
     *
     * Công thức:
     *   old_cost = d(route[i], route[i+1]) + d(route[j], route[j+1])
     *   new_cost = d(route[i], route[j])   + d(route[i+1], route[j+1])
     *   gain     = old_cost - new_cost
     */
    private static <T extends DeliveryStop> double twoOptGain(
            double depotLat, double depotLng, List<T> route, int i, int j) {

        int n = route.size();
        double prevLat = (i == 0) ? depotLat : route.get(i - 1).getLat();
        double prevLng = (i == 0) ? depotLng : route.get(i - 1).getLng();

        double nextLat = (j + 1 < n) ? route.get(j + 1).getLat() : depotLat;
        double nextLng = (j + 1 < n) ? route.get(j + 1).getLng() : depotLng;

        double oldCost = HaversineUtils.distanceKm(prevLat, prevLng,
                             route.get(i).getLat(), route.get(i).getLng())
                       + HaversineUtils.distanceKm(route.get(j).getLat(), route.get(j).getLng(),
                             nextLat, nextLng);
        double newCost = HaversineUtils.distanceKm(prevLat, prevLng,
                             route.get(j).getLat(), route.get(j).getLng())
                       + HaversineUtils.distanceKm(route.get(i).getLat(), route.get(i).getLng(),
                             nextLat, nextLng);
        return oldCost - newCost;
    }

    private static <T> void reverseSegment(List<T> list, int from, int to) {
        while (from < to) {
            T tmp = list.get(from);
            list.set(from, list.get(to));
            list.set(to, tmp);
            from++; to--;
        }
    }
}
```

**Interface `DeliveryStop`:**

```java
// Mọi class muốn được tối ưu phải implement interface này
public interface DeliveryStop {
    double getLat();
    double getLng();
}
```

`DeliveryManifestOrder` implements `DeliveryStop` trả về `receiverLat`, `receiverLng`.

### 6.4 `DeliveryRouteOptimizationServiceImpl.java`

**File:** `first-mile/src/main/java/serp/project/first_mile/service/impl/DeliveryRouteOptimizationServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class DeliveryRouteOptimizationServiceImpl implements DeliveryRouteOptimizationService {

    private final LastMileAccessUtils accessUtils;

    @Override
    public List<DeliveryManifestOrder> optimizeRoute(
            String postOfficeCode, List<DeliveryManifestOrder> orders) {

        // Lấy tọa độ bưu cục trực tiếp từ PostOffice entity
        // PostOffice đã có @Formula fields: locationLatitude, locationLongitude
        PostOffice postOffice = postOfficeRepository.findByCodeAndTenantId(postOfficeCode, accessUtils.getCurrentTenantIdOrThrow())
            .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
        GeoPoint depot = new GeoPoint(postOffice.getLocationLatitude(), postOffice.getLocationLongitude());
        if (depot.getLat() == null || depot.getLng() == null) {
            throw new AppException(ErrorCode.POST_OFFICE_LOCATION_NOT_SET);
        }

        // Lọc ra đơn có tọa độ hợp lệ
        List<DeliveryManifestOrder> withCoords = orders.stream()
            .filter(o -> o.getReceiverLat() != null && o.getReceiverLng() != null)
            .collect(Collectors.toCollection(ArrayList::new));

        // Đơn không có tọa độ → xếp cuối danh sách (sắp xếp thủ công)
        List<DeliveryManifestOrder> noCoords = orders.stream()
            .filter(o -> o.getReceiverLat() == null || o.getReceiverLng() == null)
            .toList();

        List<DeliveryManifestOrder> optimized = DeliveryRouteOptimizationUtils.optimize(
            depot.getLat(), depot.getLng(), withCoords);

        // Gộp lại: optimized route + đơn không có tọa độ ở cuối
        List<DeliveryManifestOrder> result = new ArrayList<>(optimized);
        result.addAll(noCoords);

        // Gán số thứ tự sequence
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setSequence(i + 1);
        }
        return result;
    }
}
```

---

## 7. Phase 5 — Luồng nghiệp vụ & API

### 7.1 Luồng 1: Kiểm hàng tại bưu cục đích

**Khi nào:** Nhân viên bưu cục đích mở bag, quét từng đơn.

**Endpoint:** `POST /api/v1/inbound-orders/confirm`

**Request: `SortInboundOrdersRequest`**

```json
{
  "post_office_code": "PO_HN_001",
  "order_codes": ["TMS-001", "TMS-002"]
}
```

**Logic trong `OrderSortingServiceImpl.confirmInbound(...)`:**

1. Gọi `TmsOrderCaller.lookupOrders(orderCodes, tenantId)` → lấy danh sách `OrderOperationView`.
2. Validate: tất cả đơn phải có `status = INBOUND_AT_DESTINATION_POST_OFFICE` và `destinationPostOfficeCode` khớp.
3. Gọi `TmsOrderCaller.applyTransitions(...)` với `targetStatus = READY_FOR_DELIVERY`.
4. Enqueue vào `OrderTransitionOutbox` để retry nếu call tms-order thất bại.
5. Trả về danh sách đơn đã xác nhận.

**Endpoint:** `GET /api/v1/inbound-orders?post_office_code=X&status=INBOUND_AT_DESTINATION_POST_OFFICE`

Logic: Gọi `TmsOrderCaller.lookupOrders` với filter `destinationPostOfficeCode + status`.

### 7.2 Luồng 2: Tạo Delivery Manifest & Tối ưu lộ trình

**Endpoint:** `POST /api/v1/delivery-manifests`

**Request: `CreateDeliveryManifestRequest`**

```json
{
  "post_office_code": "PO_HN_001",
  "courier_id": 12,
  "vehicle_id": "VH-001",
  "planned_date": "2026-06-07",
  "planned_departure_at": "2026-06-07T08:00:00",
  "order_codes": ["TMS-001", "TMS-002", "TMS-005"],
  "note": "Khu vực Hoàn Kiếm"
}
```

**Logic trong `DeliveryManifestServiceImpl.createManifest(...)`:**

1. Validate: tất cả đơn phải `READY_FOR_DELIVERY` tại đúng bưu cục.
2. Gọi `TmsOrderCaller.lookupOrders(...)` để lấy receiver lat/lng.
3. Build danh sách `DeliveryManifestOrder` từ data trả về.
4. Gọi `RouteOptimizationService.optimizeRoute(postOfficeCode, orders)` → gán `sequence`.
5. Tính `totalCodAmount`, `totalShippingFee` (chỉ khi `feePayer = RECEIVER`).
6. Sinh `manifestCode` (format: `DM-YYYYMMDD-{tenantId}-{seq}`).
7. Lưu `DeliveryManifest` + `DeliveryManifestOrder` list.
8. Transition tất cả đơn sang `OUT_FOR_DELIVERY` qua tms-order internal API.

**Endpoint:** `GET /api/v1/delivery-manifests?post_office_code=X&status=X&date=X`  
**Endpoint:** `GET /api/v1/delivery-manifests/{id}`  
**Endpoint:** `GET /api/v1/delivery-manifests/{id}/route-summary` → trả về ordered list + tổng km ước tính

### 7.3 Luồng 3: Xác nhận giao hàng thành công

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/delivered`

**Request: `ConfirmDeliveryRequest`**

```json
{
  "proof_photo_url": "https://s3.../proof.jpg",
  "cod_collected": 250000,
  "shipping_fee_collected": 0,
  "note": "Giao cho người thân",
  "delivered_at": "2026-06-07T10:30:00"
}
```

**Logic trong `DeliveryManifestServiceImpl.confirmDelivered(...)`:**

1. Load `DeliveryManifestOrder`.
2. Validate `status = OUT_FOR_DELIVERY` (hoặc đang `IN_PROGRESS` trong manifest).
3. Set `status = DELIVERED`, lưu proof photo, COD collected, fee collected, `deliveredAt`.
4. Cập nhật `DeliveryManifest.deliveredCount++`, `collectedCodAmount += codCollected`, etc.
5. Nếu `collectedCodAmount > codAmount`: log warning (thu thừa).
6. Transition đơn sang `DELIVERED` qua tms-order.
7. Nếu tất cả đơn đã xử lý → `DeliveryManifest.status = COMPLETED`.

### 7.4 Luồng 4: Giao thất bại

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/failed`

**Request: `ConfirmDeliveryFailureRequest`**

```json
{
  "failure_reason": "RECIPIENT_NOT_HOME",
  "note": "Đã gọi điện 3 lần không bắt máy",
  "current_lat": 21.028,
  "current_lng": 105.834
}
```

**Logic trong `DeliveryManifestServiceImpl.confirmFailed(...)`:**

1. Set `DeliveryManifestOrder.status = FAILED`, lưu `failureReason`.
2. Tăng `deliveryAttemptCount`.
3. Transition đơn sang `DELIVERY_FAILED` qua tms-order.
4. Nếu `deliveryAttemptCount >= app.delivery.max-attempts`:
   - Tự động chuyển sang `RETURNED_TO_SENDER` (hoặc pending quyết định của quản lý bưu cục).
5. Cập nhật `DeliveryManifest.failedCount++`.

### 7.5 Luồng 5: Hoàn hàng người gửi

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/return`

**Request: `ReturnToSenderRequest`**

```json
{ "note": "Hết 3 lần thử, khách từ chối nhận" }
```

**Logic:** Transition đơn sang `RETURNED_TO_SENDER` qua tms-order.

---

## 8. Phase 6 — Thu tiền COD & Phí giao hàng

### 8.1 Kiến trúc thanh toán

| Trường hợp                    | FeePayer | COD   | Logic                                          |
| ----------------------------- | -------- | ----- | ---------------------------------------------- |
| Người gửi đã thanh toán       | SENDER   | Có    | Bưu tá chỉ thu `codAmount`, không thu phí giao |
| Người nhận trả phí            | RECEIVER | Có    | Bưu tá thu `codAmount` + `shippingFee`         |
| Người nhận trả phí, không COD | RECEIVER | Không | Bưu tá chỉ thu `shippingFee`                   |

`shippingFee` trong `DeliveryManifestOrder` chỉ được set ≠ 0 khi `feePayer = RECEIVER`.  
Lấy giá trị từ `totalShippingFee` của đơn hàng trong tms-order.

### 8.2 `CodCollectionServiceImpl`

Không có entity riêng ở MVP — COD được ghi nhận trực tiếp trong `DeliveryManifestOrder.codCollected` và `DeliveryManifestOrder.shippingFeeCollected`.

Cung cấp endpoint tổng hợp:

**`GET /api/v1/delivery-manifests/{manifestId}/financial-summary`**

```json
{
  "manifest_code": "DM-20260607-001",
  "courier_id": 12,
  "total_cod_amount": 1500000,
  "collected_cod_amount": 1250000,
  "pending_cod_amount": 250000,
  "total_shipping_fee": 85000,
  "collected_shipping_fee": 75000,
  "pending_shipping_fee": 10000,
  "orders": [...]
}
```

**`GET /api/v1/reports/cod?post_office_code=X&date_from=X&date_to=X`**  
Báo cáo COD theo bưu cục và khoảng thời gian.

### 8.3 Cập nhật `PaymentStatus` trong tms-order

Sau khi `confirmDelivered(...)`:

- Nếu `feePayer = RECEIVER` và `shippingFeeCollected >= shippingFee`:
  → Gọi `TmsOrderCaller.updatePaymentStatus(orderCode, PAID)`.
- Nếu `feePayer = SENDER` và `paymentStatus` vẫn `UNPAID`:
  → Không thay đổi (sender thanh toán qua payment gateway riêng).

Mở thêm endpoint internal trong `tms-order`:

**`tms-order`: `POST /api/v1/internal/orders/payment-status`**

```java
// InternalOrderController thêm endpoint:
@PostMapping("/payment-status")
public void updatePaymentStatus(@RequestBody UpdatePaymentStatusRequest request) {
    orderService.updatePaymentStatus(request.getOrderCode(),
                                     request.getTenantId(),
                                     request.getPaymentStatus());
}
```

---

## 9. Phase 7 — Mở rộng `TmsOrderClient` (cross-service HTTP)

`TmsOrderClient` đã tồn tại tại `first-mile/caller/TmsOrderClient.java`, dùng `RestClient` (Spring 6.1+), config bằng `tms-order.service.base-url` (giá trị mặc định `http://localhost:8099`).

**Thêm 2 method mới vào class hiện có:**

```java
/** Lookup orders by destination post office + statuses */
public List<TmsOrderOperationView> lookupAtPostOffice(
        String postOfficeCode, List<OrderStatus> statuses, Long tenantId) {
    return lookup(TmsOrderLookupRequest.builder()
            .destinationPostOfficeCode(postOfficeCode)
            .statuses(statuses)
            .build(), tenantId);
}

/** Update PaymentStatus after delivery */
public void updatePaymentStatus(
        String orderCode, Long tenantId, String paymentStatus) { ... }
```

**Cập nhật local DTOs:**

- `caller/dto/tms_order/TmsOrderLookupRequest.java` — thêm `destinationPostOfficeCode`, `statuses`.
- `caller/dto/tms_order/TmsOrderOperationView.java` — thêm `destinationPostOfficeCode`, `receiverName/Phone/WardCode/ProvinceCode/AddressDetail/Latitude/Longitude`, `codAmount`, `totalShippingFee`, `feePayer`, `paymentStatus`.

**Authentication:** Đã được xử lý trong `TmsOrderClient` hiện có (forward JWT + `X-Internal-API-Key` header).

---

## 10. Phase 8 — Frontend (`serp_web`)

Thêm vào module `serp_web/src/modules/first-mile/` (bưu tá, bưu cục đã có trong first-mile UI). API calls tới gateway path `/first-mile/api/v1/...`.

### 10.1 RTK Query API

Tạo file: `serp_web/src/modules/first-mile/api/lastMileApi.ts`

```typescript
// Sử dụng extraOptions: { service: 'first-mile' }
const lastMileApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getInboundOrders: builder.query<...>({ ... }),
    confirmInboundOrders: builder.mutation<...>({ ... }),
    createDeliveryManifest: builder.mutation<...>({ ... }),
    getDeliveryManifests: builder.query<...>({ ... }),
    getDeliveryManifestDetail: builder.query<...>({ ... }),
    confirmDelivered: builder.mutation<...>({ ... }),
    confirmDeliveryFailed: builder.mutation<...>({ ... }),
    confirmReturn: builder.mutation<...>({ ... }),
    getFinancialSummary: builder.query<...>({ ... }),
  }),
});
```

Cập nhật `serp_web/src/modules/first-mile/api/transforms.ts` thêm transform cho `last-mile`.

### 10.2 Các trang cần tạo

| File                                                      | Vai trò                                                 |
| --------------------------------------------------------- | ------------------------------------------------------- |
| `pages/inbound-sorting/InboundSortingPage.tsx`            | Màn hình quét hàng gỡ bag, confirm `READY_FOR_DELIVERY` |
| `pages/delivery-manifests/DeliveryManifestListPage.tsx`   | Danh sách phiếu giao                                    |
| `pages/delivery-manifests/DeliveryManifestFormPage.tsx`   | Tạo phiếu giao + xem lộ trình tối ưu                    |
| `pages/delivery-manifests/DeliveryManifestDetailPage.tsx` | Chi tiết phiếu (bưu tá dùng trên di động)               |
| `pages/delivery-execution/DeliveryOrderCard.tsx`          | Component xác nhận giao thành công / thất bại           |
| `pages/delivery-reports/CodReportPage.tsx`                | Báo cáo COD theo ngày/bưu cục                           |

### 10.3 Route map cho lộ trình

`DeliveryManifestDetailPage` nhúng bản đồ (dùng Leaflet hoặc Goong Maps SDK) hiển thị:

- Icon bưu cục (điểm xuất phát)
- Icon từng điểm giao theo số thứ tự `sequence`
- Đường nối thể hiện lộ trình

---

## 11. Tích hợp Gateway

Route `/first-mile/**` **đã tồn tại** trong `api_gateway` và trỏ tới `first-mile:8093`. Không cần thêm route mới.

Chỉ cần đảm bảo các controller mới dùng đúng prefix `/api/v1/delivery-manifests`, `/api/v1/inbound-orders` — gateway sẽ tự forward.

**Kiểm tra route hiện có trong `api_gateway`:**

```yaml
# Đã có — không cần thay đổi
- path: /first-mile/**
  service: first-mile
  base_url: http://first-mile:8093
```

---

## 12. Thứ tự triển khai cho Agent

Thực hiện theo đúng thứ tự sau để tránh compile error và dependency cycle:

| Bước | Việc cần làm                                                                             | Service       |
| ---- | ---------------------------------------------------------------------------------------- | ------------- |
| 1    | Thêm receiver/COD fields vào `OrderOperationView` + cập nhật `OrderOperationMapper`      | `tms-order`   |
| 2    | Thêm 5 OrderStatus mới vào enum                                                          | `tms-order`   |
| 3    | Thêm 5 trạng thái last-mile + 3 trạng thái còn thiếu vào `first-mile/enums/OrderStatus`  | `first-mile`  |
| 4    | Thêm 5 trạng thái last-mile vào `second-mile/enums/OrderStatus`                          | `second-mile` |
| 5    | Thêm transition rules trong `OrderTransitionServiceImpl`                                 | `tms-order`   |
| 6    | Mở rộng `InternalOrderLookupRequest` thêm `destinationPostOfficeCode` + `statuses`       | `tms-order`   |
| 7    | Cập nhật `OrderQueryServiceImpl.lookupOrders(...)` xử lý filter mới                      | `tms-order`   |
| 8    | Thêm endpoint `POST /api/v1/internal/orders/payment-status`                              | `tms-order`   |
| 9    | `./mvnw.cmd clean compile` → xác nhận build pass                                         | `tms-order`   |
| 10   | Cập nhật `TmsOrderLookupRequest` + `TmsOrderOperationView` (local DTOs trong first-mile) | `first-mile`  |
| 11   | Thêm `lookupAtPostOffice(...)` + `updatePaymentStatus(...)` vào `TmsOrderClient`         | `first-mile`  |
| 12   | Tạo migration SQL `delivery-manifests.sql` + `delivery-manifest-orders.sql`              | `first-mile`  |
| 13   | Đăng ký 2 file SQL vào `spring.sql.init.schema-locations` trong `application.yaml`       | `first-mile`  |
| 14   | Thêm `app.delivery.*` config vào `application.yaml`                                      | `first-mile`  |
| 15   | Tạo enums, domain entities, repositories (DeliveryManifest, DeliveryManifestOrder)       | `first-mile`  |
| 16   | Implement `HaversineUtils` + `DeliveryRouteOptimizationUtils` + unit tests               | `first-mile`  |
| 17   | Implement `OrderSortingServiceImpl` (Luồng 1)                                            | `first-mile`  |
| 18   | Implement `DeliveryRouteOptimizationServiceImpl` (inject `PostOfficeRepository`)         | `first-mile`  |
| 19   | Implement `DeliveryManifestServiceImpl` (Luồng 2–5)                                      | `first-mile`  |
| 20   | Implement `CodCollectionServiceImpl` + financial summary                                 | `first-mile`  |
| 21   | Implement Controllers (`DeliveryManifestController`, `OrderSortingController`)           | `first-mile`  |
| 22   | `./mvnw.cmd clean compile` → xác nhận build pass                                         | `first-mile`  |
| 23   | Unit test `DeliveryRouteOptimizationUtils` (bắt buộc)                                    | `first-mile`  |
| 24   | Xác nhận gateway route `/first-mile/**` đã đúng (không cần thêm mới)                     | `api_gateway` |
| 25   | `InboundSortingPage` + `DeliveryManifestListPage`                                        | `serp_web`    |
| 26   | `DeliveryManifestFormPage` với route map preview                                         | `serp_web`    |
| 27   | `DeliveryOrderCard` (giao thành công/thất bại) + `CodReportPage`                         | `serp_web`    |
| 28   | `npm run build` + `npm run lint` pass                                                    | `serp_web`    |

---

## 13. Kiểm thử

### 13.1 Unit tests bắt buộc (`first-mile`)

| Class                            | Test cases                                                                                                                                                             |
| -------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DeliveryRouteOptimizationUtils` | route 0 stops; route 1 stop; route 5 stops → verify sequence 1-based; 2-opt không tăng tổng distance; depot không bị lặp trong route; stops không có coords → xếp cuối |
| `HaversineUtils`                 | Hà Nội → TP.HCM ≈ 1490 km (±5 km); cùng điểm → 0.0; điểm đối xứng                                                                                                      |
| `DeliveryManifestServiceImpl`    | Tạo manifest từ đơn valid; lỗi khi đơn sai trạng thái; tính đúng `totalCodAmount` / `totalShippingFee` theo `feePayer`                                                 |

### 13.2 Unit tests bắt buộc (`tms-order`)

| Class                        | Test cases                                                                                                                                                                                                |
| ---------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `OrderTransitionServiceImpl` | `INBOUND_AT_DESTINATION_POST_OFFICE → READY_FOR_DELIVERY` hợp lệ; `DELIVERED → READY_FOR_DELIVERY` bị từ chối; `DELIVERY_FAILED → OUT_FOR_DELIVERY` hợp lệ; `DELIVERY_FAILED → RETURNED_TO_SENDER` hợp lệ |

### 13.3 Integration checklist

- [ ] Đơn INBOUND_AT_DESTINATION_POST_OFFICE tại đúng bưu cục → nhân viên confirm → thành READY_FOR_DELIVERY
- [ ] Tạo manifest với 5 đơn → sequence tối ưu khác với thứ tự nhập vào
- [ ] Xác nhận giao thành công → đơn → DELIVERED, `collectedCodAmount` cộng dồn đúng
- [ ] Giao thất bại 3 lần → đơn → RETURNED_TO_SENDER
- [ ] `feePayer = RECEIVER`: sau giao thành công → `paymentStatus = PAID` (tms-order)
- [ ] `feePayer = SENDER`: sau giao thành công → `paymentStatus` không đổi

---

## 14. Quy tắc code

- Áp dụng `// Author: SERP Project` header cho tất cả file Java mới trong `first-mile`.
- Controller thin: không chứa business logic.
- `@Transactional(readOnly = true)` cho query methods, `@Transactional(rollbackFor = Exception.class)` cho write.
- Mọi thay đổi trạng thái đơn hàng phải đi qua `TmsOrderCaller.applyTransitions(...)` với idempotency key, đồng thời enqueue vào `OrderTransitionOutbox` để retry.
- Manifest code sinh bằng `String.format("DM-%s-%d-%05d", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), tenantId, seq)`.
- Response shape: `ApiResponse<T>` với `message` từ `MessageService`.
- Không lưu secret, credential vào code; dùng biến môi trường qua `application.yaml`.
- Tất cả class mới nằm trong package `serp.project.first_mile.*` — không dùng `last_mile` làm tên package.
