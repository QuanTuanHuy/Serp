# Last-Mile Delivery — Implementation Plan

Author: SERP Project  
Status: **PLANNED — awaiting implementation**  
Validated against codebase: **2026-06-07**

This plan is for AI coding agents to implement the last-mile leg: unbag at destination post office → assign courier → optimize route → deliver to recipient → collect COD and shipping fee.

> **Architecture decision:** All last-mile logic is integrated directly into the `first-mile` service (port `8093`). No separate service is created.

---

## 1. Current State & Gaps

The order flow currently stops at `INBOUND_AT_DESTINATION_POST_OFFICE` after the bag arrives at the destination post office.

**What's missing:**

1. `OrderOperationView` in `tms-order` does **not** expose receiver fields or COD fields to the internal API — must be added so `first-mile` can read them when building a delivery manifest. _(Validated: confirmed missing)_
2. No order statuses after destination post office arrival (`READY_FOR_DELIVERY`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNED_TO_SENDER`). _(Validated: confirmed missing)_
3. `first-mile/enums/OrderStatus.java` is out of sync: missing `BAG_IN_TRANSIT`, `INBOUND_AT_DESTINATION_HUB`, `INBOUND_AT_DESTINATION_POST_OFFICE` + 5 last-mile statuses. _(Validated: confirmed missing)_
4. `TmsOrderClient` in first-mile has no method to lookup orders by destination post office. _(Validated: confirmed missing)_
5. No flow for unbagging, sorting orders, or creating delivery routes in first-mile. _(Validated: confirmed)_
6. No COD/shipping fee collection recording when recipient pays. _(Validated: confirmed)_

---

## 2. Target Architecture

```
[second-mile]
    Bag → ARRIVED
    Order → INBOUND_AT_DESTINATION_POST_OFFICE
           |
           ▼  (scan/confirm at destination post office — last-mile in first-mile service)
    READY_FOR_DELIVERY
           |
           ▼  (create delivery manifest + optimize route)
    OUT_FOR_DELIVERY
           |
    ┌──────┴──────────┐
    ▼                 ▼
DELIVERED       DELIVERY_FAILED
                      |
               (retry or after N attempts)
                      ▼
              RETURNED_TO_SENDER
```

**Integrated into `first-mile`** — Spring Boot, Java 21, port `8093` (already exists).  
Last-mile domain is added within the existing Clean Architecture of `first-mile`.

```
Serp/
  first-mile/          ← MODIFY: add last-mile domain (delivery manifest, COD, route opt)
  tms-order/           ← MODIFY: add receiver fields + new statuses
  serp_web/            ← MODIFY: add last-mile UI
```

---

## 3. Phase 1 — `tms-order`: Add Statuses & Extend Lookup

### 3.1 Receiver Fields — Data Already Exists

> **No DB migration needed.** `Order.java` already has: `receiverName`, `receiverPhone`, `receiverWardCode`, `receiverProvinceCode`, `receiverAddressDetail`, `receiverLocation`, `destinationPostOfficeCode`, `codAmount`, `feePayer`, `totalShippingFee`, `paymentStatus`.

**Still missing:** `OrderOperationView` does not expose these fields to the internal API.

**File:** `tms-order/src/main/java/serp/project/tms_order/dto/response/OrderOperationView.java`  
Add to the record parameters:

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
```

**File:** `tms-order/src/main/java/serp/project/tms_order/mapper/OrderOperationMapper.java`  
Map new fields from `Order` to `OrderOperationView` (extract `receiverLatitude/Longitude` from `receiverLocation` Point using existing `toLatitude()`/`toLongitude()` helpers — same pattern as sender).

> **IMPORTANT (validated):** `OrderOperationView` is a Java **record** and `OrderOperationMapper.toView()` uses **positional constructor arguments**. Both must be updated simultaneously — add new parameters to the record AND add corresponding values to the mapper's constructor call.

**Migration SQL** — **NOT NEEDED** (columns already exist in DB).

### 3.2 Add New `OrderStatus` Values

**File:** `tms-order/src/main/java/serp/project/tms_order/enums/OrderStatus.java`

Add before `CANCELLED` (after `INBOUND_AT_DESTINATION_POST_OFFICE`):

```java
READY_FOR_DELIVERY,          // Inspected at destination PO, ready for courier
OUT_FOR_DELIVERY,            // Courier is on delivery route
DELIVERED,                   // Delivered successfully
DELIVERY_FAILED,             // Delivery failed (not home, wrong address...) — can retry
RETURNED_TO_SENDER,          // Returned to sender (after max attempts exhausted)
```

**Sync to local copies:**

- `second-mile/src/main/java/serp/project/second_mile/enums/OrderStatus.java` — add 5 statuses above (currently synced with tms-order).
- `first-mile/src/main/java/serp/project/first_mile/enums/OrderStatus.java` — currently **more out of sync**: missing `BAG_IN_TRANSIT`, `INBOUND_AT_DESTINATION_HUB`, `INBOUND_AT_DESTINATION_POST_OFFICE` and all 5 new statuses. Must add **8 statuses total** before `CANCELLED`:

```java
// Statuses currently missing in first-mile (add before CANCELLED):
BAG_IN_TRANSIT,
INBOUND_AT_DESTINATION_HUB,
INBOUND_AT_DESTINATION_POST_OFFICE,
READY_FOR_DELIVERY,
OUT_FOR_DELIVERY,
DELIVERED,
DELIVERY_FAILED,
RETURNED_TO_SENDER,
```

### 3.3 Add Transition Rules

**File:** `tms-order/src/main/java/serp/project/tms_order/service/impl/OrderTransitionServiceImpl.java`

In the `static { ... }` block, add after `INBOUND_AT_DESTINATION_POST_OFFICE`:

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

### 3.4 Extend `InternalOrderLookupRequest`

**File:** `tms-order/src/main/java/serp/project/tms_order/dto/request/InternalOrderLookupRequest.java`

_(Validated: currently only has `orderIds` and `orderCodes`)._

Add fields to enable last-mile queries:

```java
private String destinationPostOfficeCode;
private List<OrderStatus> statuses;  // filter by status
```

And update `OrderQueryServiceImpl.lookupOrders(...)` to apply these filters.

---

## 4. Phase 2 — Add Last-Mile Module to `first-mile`

No new microservice. All last-mile code lives in package `serp.project.first_mile` and the existing `first-mile` database.

### 4.1 New Files to Add in `first-mile`

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
    # TmsOrderTransitionOutboxService.java ALREADY EXISTS — do not recreate
    impl/
      DeliveryManifestServiceImpl.java     ← NEW
      OrderSortingServiceImpl.java         ← NEW
      DeliveryRouteOptimizationServiceImpl.java  ← NEW
      CodCollectionServiceImpl.java        ← NEW
      # TmsOrderTransitionOutboxServiceImpl.java ALREADY EXISTS — do not recreate
  domain/
    DeliveryManifest.java                  ← NEW
    DeliveryManifestOrder.java             ← NEW
    # Reuse existing OrderTransitionOutbox entity, differentiate by source = "LAST_MILE_DELIVERY"
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
    # Use existing OrderTransitionOutboxRepository
  caller/
    # TmsOrderClient.java ALREADY EXISTS — only add new methods, do not recreate
    dto/tms_order/
      # Update TmsOrderOperationView.java to add receiver/COD fields  ← MODIFY
      # Update TmsOrderLookupRequest.java to add destinationPostOfficeCode+statuses  ← MODIFY
  kernel/utils/
    DeliveryRouteOptimizationUtils.java    ← NEW (Nearest Neighbor + 2-opt)
    HaversineUtils.java                    ← NEW (validated: does not exist yet)

first-mile/src/main/resources/
  db/migration/
    delivery-manifests.sql          ← NEW  (plain SQL, not Flyway V<n>__ prefix)
    delivery-manifest-orders.sql    ← NEW
    # Register in application.yaml: spring.sql.init.schema-locations
  i18n/
    messages.properties       ← MODIFY: add delivery message keys
    messages_vi.properties    ← MODIFY: add delivery message keys
    messages_en.properties    ← MODIFY: add delivery message keys
```

> **`TmsOrderClient.java` already exists** at `first-mile/caller/TmsOrderClient.java` with `lookupByIds`, `lookupByCodes`, `findPickupCandidates`, `applyTransitions`. **Do not recreate.** Only add new methods:
>
> - `lookupAtPostOffice(String postOfficeCode, List<OrderStatus> statuses, Long tenantId)` — find orders by destination post office + status.
> - `updatePaymentStatus(String orderCode, Long tenantId, String paymentStatus)` — after successful delivery.

### 4.2 Configuration Updates to Existing `application.yaml`

File: `first-mile/src/main/resources/application.yaml`.

> **Validated existing config structure:**
>
> - tms-order connection: `tms-order.service.base-url` (default `http://localhost:8099`)
> - DB migration: `spring.sql.init.schema-locations` (plain SQL), **NOT Flyway**
> - Security: JWT from Keycloak already configured
> - Outbox: `tms-order.transition-outbox.retry-interval-ms` already exists

Only **2 additions** needed:

**1. Register new migration SQL** in `spring.sql.init.schema-locations`:

```yaml
spring:
  sql:
    init:
      schema-locations:
        # ... (keep all existing entries) ...
        - classpath:db/migration/delivery-manifests.sql
        - classpath:db/migration/delivery-manifest-orders.sql
```

**2. Add delivery config** to the existing `app:` block:

```yaml
app:
  # ... (keep all existing config) ...
  delivery:
    max-attempts: ${MAX_DELIVERY_ATTEMPTS:3}
    manifest-code-prefix: DM
```

> `tms-order.service.base-url` already exists, no changes needed.

**3. Add new tms-order path config:**

```yaml
tms-order:
  service:
    # ... (keep existing paths) ...
    payment-status-path: ${TMS_ORDER_PAYMENT_STATUS_PATH:/api/v1/internal/orders/payment-status}
```

---

## 5. Phase 3 — Domain Model & DB Schema

### 5.1 Enums

**File:** `first-mile/src/main/java/serp/project/first_mile/enums/DeliveryManifestStatus.java`

```java
public enum DeliveryManifestStatus {
    CREATED,          // Created, not yet departed
    IN_PROGRESS,      // Courier is delivering
    COMPLETED,        // All orders processed (delivered/failed)
    CANCELLED
}
```

**File:** `first-mile/src/main/java/serp/project/first_mile/enums/DeliveryOrderStatus.java` (order status _within_ manifest, separate from global `OrderStatus`)

```java
public enum DeliveryOrderStatus {
    PENDING,         // Awaiting delivery
    OUT_FOR_DELIVERY,// Being delivered in this batch
    DELIVERED,       // Delivered successfully
    FAILED,          // Failed this attempt
    RESCHEDULED,     // Assigned to another manifest for retry
    RETURNED         // Returned to sender
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
    private String manifestCode;          // e.g. DM-20260607-0001
    private String postOfficeCode;        // Source post office for delivery
    private Long courierId;               // Staff ID (from first-mile staff)
    private String courierName;           // Cached courier name
    private String vehicleId;             // Vehicle identifier

    @Enumerated(EnumType.STRING)
    private DeliveryManifestStatus status;

    private LocalDate plannedDate;
    private LocalDateTime plannedDepartureAt;
    private LocalDateTime actualDepartureAt;
    private LocalDateTime actualReturnAt;

    private Integer totalOrders;
    private Integer deliveredCount;
    private Integer failedCount;

    private Long totalCodAmount;          // Total COD to collect
    private Long collectedCodAmount;      // Total COD collected so far
    private Long totalShippingFee;        // Total shipping fee (when feePayer = RECEIVER)
    private Long collectedShippingFee;    // Total shipping fee collected

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private RouteGeoJson routeGeoJson;    // Optimized route (GeoJSON LineString)

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

    private Long orderId;                 // FK → tms-order (no JPA join cross-service)
    private String orderCode;

    private Integer sequence;            // Delivery order in route (from algorithm)
    private Integer deliveryAttemptCount; // Total attempts across manifests

    @Enumerated(EnumType.STRING)
    private DeliveryOrderStatus status;

    // Cached receiver info (from tms-order at manifest creation time)
    private String receiverName;
    private String receiverPhone;
    private String receiverAddressDetail;
    private String receiverWardCode;
    private String receiverProvinceCode;
    private Double receiverLat;
    private Double receiverLng;

    // Financial
    private Long codAmount;              // COD amount to collect
    private Long codCollected;           // Actual COD collected (0 if not yet)
    private Long shippingFee;            // Shipping fee (only when feePayer=RECEIVER)
    private Long shippingFeeCollected;   // Actual shipping fee collected

    private String feePayer;             // SENDER / RECEIVER

    // Result
    private String proofPhotoUrl;        // Proof of delivery photo
    private String failureReason;        // Failure reason
    private LocalDateTime deliveredAt;
    private String note;
}
```

### 5.4 Migration SQL

**`delivery-manifests.sql`** (plain SQL, NOT Flyway-prefixed):

```sql
CREATE TABLE IF NOT EXISTS delivery_manifests (
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

CREATE INDEX IF NOT EXISTS idx_dm_tenant_post_office ON delivery_manifests (tenant_id, post_office_code);
CREATE INDEX IF NOT EXISTS idx_dm_tenant_courier     ON delivery_manifests (tenant_id, courier_id);
CREATE INDEX IF NOT EXISTS idx_dm_tenant_status      ON delivery_manifests (tenant_id, status);
```

**`delivery-manifest-orders.sql`**:

```sql
CREATE TABLE IF NOT EXISTS delivery_manifest_orders (
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

CREATE INDEX IF NOT EXISTS idx_dmo_manifest_id ON delivery_manifest_orders (manifest_id);
CREATE INDEX IF NOT EXISTS idx_dmo_order_code   ON delivery_manifest_orders (tenant_id, order_code);
CREATE INDEX IF NOT EXISTS idx_dmo_status       ON delivery_manifest_orders (tenant_id, status);
```

### 5.5 Reuse `OrderTransitionOutbox` (Outbox Pattern)

_(Validated: entity exists with `source` field.)_ No new entity needed. Reuse existing `OrderTransitionOutbox` and `TmsOrderTransitionOutboxService`. Differentiate delivery outbox entries by `source = "LAST_MILE_DELIVERY"` when enqueuing.

---

## 6. Phase 4 — Route Optimization Algorithm

### 6.1 Algorithm Selection Rationale

Couriers typically deliver **10–50 orders/day** from one post office. At this scale:

- **Nearest Neighbor (NN)** produces good results in < 1ms
- **2-opt improvement** improves quality by ~10–15% after NN
- Full VRP solvers (ALNS, OR-Tools) are unnecessary at this stage

### 6.2 `HaversineUtils.java`

_(Validated: does not exist yet in first-mile)_

```java
// File: first-mile/src/main/java/serp/project/first_mile/kernel/utils/HaversineUtils.java
public final class HaversineUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineUtils() {}

    /**
     * Calculate great-circle distance (km) between two coordinates.
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
 * Optimize last-mile delivery route using Nearest Neighbor + 2-opt.
 *
 * Input:
 *   - depotLat, depotLng: post office coordinates
 *   - stops: list of delivery points (with lat/lng)
 *
 * Output:
 *   - List of stops sorted in optimal delivery order
 *
 * Complexity:
 *   - Nearest Neighbor construction: O(n²)
 *   - 2-opt improvement: O(n²) per pass, max O(n²) passes → O(n³) worst case
 *   - With n ≤ 50: < 5ms on any modern hardware
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
    // Start from depot, each step picks the nearest unvisited delivery point.
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
    // Try reversing each sub-segment [i..j] in the route.
    // If reversing reduces total distance → keep it and repeat.
    private static <T extends DeliveryStop> void twoOpt(
            double depotLat, double depotLng, List<T> route) {

        int n = route.size();
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    double gain = twoOptGain(depotLat, depotLng, route, i, j);
                    if (gain > 1e-9) {  // epsilon to avoid floating-point noise
                        reverseSegment(route, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
    }

    /**
     * Calculate distance reduction if segment [i+1 .. j] is reversed.
     * gain > 0 → worth swapping.
     *
     * Formula:
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
public interface DeliveryStop {
    double getLat();
    double getLng();
}
```

`DeliveryManifestOrder` implements `DeliveryStop` returning `receiverLat`, `receiverLng`.

### 6.4 `DeliveryRouteOptimizationServiceImpl.java`

**File:** `first-mile/src/main/java/serp/project/first_mile/service/impl/DeliveryRouteOptimizationServiceImpl.java`

> **CORRECTION (validated):** Plan originally referenced `postOfficeRepository.findByCodeAndTenantId(...)` — actual method is `findByCodeIgnoreCaseAndTenantId(String code, Long tenantId)`.

```java
@Service
@RequiredArgsConstructor
public class DeliveryRouteOptimizationServiceImpl implements DeliveryRouteOptimizationService {

    private final PostOfficeRepository postOfficeRepository;
    private final LastMileAccessUtils accessUtils;

    @Override
    public List<DeliveryManifestOrder> optimizeRoute(
            String postOfficeCode, List<DeliveryManifestOrder> orders) {

        // Get post office coordinates directly from PostOffice entity
        // PostOffice already has @Formula fields: locationLatitude, locationLongitude
        PostOffice postOffice = postOfficeRepository.findByCodeIgnoreCaseAndTenantId(
                postOfficeCode, accessUtils.getCurrentTenantIdOrThrow())
            .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        Double depotLat = postOffice.getLocationLatitude();
        Double depotLng = postOffice.getLocationLongitude();
        if (depotLat == null || depotLng == null) {
            throw new AppException(ErrorCode.POST_OFFICE_LOCATION_NOT_SET);
        }

        // Filter orders with valid coordinates
        List<DeliveryManifestOrder> withCoords = orders.stream()
            .filter(o -> o.getReceiverLat() != null && o.getReceiverLng() != null)
            .collect(Collectors.toCollection(ArrayList::new));

        // Orders without coordinates → placed at end (manual sorting)
        List<DeliveryManifestOrder> noCoords = orders.stream()
            .filter(o -> o.getReceiverLat() == null || o.getReceiverLng() == null)
            .toList();

        List<DeliveryManifestOrder> optimized = DeliveryRouteOptimizationUtils.optimize(
            depotLat, depotLng, withCoords);

        // Merge: optimized route + orders without coords at end
        List<DeliveryManifestOrder> result = new ArrayList<>(optimized);
        result.addAll(noCoords);

        // Assign 1-based sequence numbers
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setSequence(i + 1);
        }
        return result;
    }
}
```

---

## 7. Phase 5 — Business Flows & API

### 7.1 Flow 1: Inbound Sorting at Destination Post Office

**When:** Destination PO staff opens bag, scans each order.

**Endpoint:** `POST /api/v1/inbound-orders/confirm`

**Request: `SortInboundOrdersRequest`**

```json
{
  "post_office_code": "PO_HN_001",
  "order_codes": ["TMS-001", "TMS-002"]
}
```

**Logic in `OrderSortingServiceImpl.confirmInbound(...)`:**

1. Call `TmsOrderClient.lookupByCodes(orderCodes, tenantId)` → get list of `TmsOrderOperationView`.
2. Validate: all orders must have `status = INBOUND_AT_DESTINATION_POST_OFFICE` and `destinationPostOfficeCode` matches.
3. Call `TmsOrderClient.applyTransitions(...)` with `targetStatus = READY_FOR_DELIVERY`.
4. Enqueue to `OrderTransitionOutbox` (source = `"LAST_MILE_DELIVERY"`) for retry if tms-order call fails.
5. Return list of confirmed orders.

**Endpoint:** `GET /api/v1/inbound-orders?post_office_code=X&status=INBOUND_AT_DESTINATION_POST_OFFICE`

Logic: Call `TmsOrderClient.lookupAtPostOffice(...)` with filter `destinationPostOfficeCode + status`.

### 7.2 Flow 2: Create Delivery Manifest & Optimize Route

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
  "note": "Hoan Kiem district"
}
```

**Logic in `DeliveryManifestServiceImpl.createManifest(...)`:**

1. Validate: all orders must be `READY_FOR_DELIVERY` at the correct post office.
2. Call `TmsOrderClient.lookupByCodes(...)` to get receiver lat/lng.
3. Build `DeliveryManifestOrder` list from returned data.
4. Call `DeliveryRouteOptimizationService.optimizeRoute(postOfficeCode, orders)` → assign `sequence`.
5. Calculate `totalCodAmount`, `totalShippingFee` (only when `feePayer = RECEIVER`).
6. Generate `manifestCode` (format: `DM-YYYYMMDD-{tenantId}-{seq}`).
7. Save `DeliveryManifest` + `DeliveryManifestOrder` list.
8. Transition all orders to `OUT_FOR_DELIVERY` via tms-order internal API.

**Endpoint:** `GET /api/v1/delivery-manifests?post_office_code=X&status=X&date=X`  
**Endpoint:** `GET /api/v1/delivery-manifests/{id}`  
**Endpoint:** `GET /api/v1/delivery-manifests/{id}/route-summary` → returns ordered list + estimated total km

### 7.3 Flow 3: Confirm Successful Delivery

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/delivered`

**Request: `ConfirmDeliveryRequest`**

```json
{
  "proof_photo_url": "https://s3.../proof.jpg",
  "cod_collected": 250000,
  "shipping_fee_collected": 0,
  "note": "Delivered to family member",
  "delivered_at": "2026-06-07T10:30:00"
}
```

**Logic in `DeliveryManifestServiceImpl.confirmDelivered(...)`:**

1. Load `DeliveryManifestOrder`.
2. Validate `status = OUT_FOR_DELIVERY` (or manifest is `IN_PROGRESS`).
3. Set `status = DELIVERED`, save proof photo, COD collected, fee collected, `deliveredAt`.
4. Update `DeliveryManifest.deliveredCount++`, `collectedCodAmount += codCollected`, etc.
5. If `collectedCodAmount > codAmount`: log warning (over-collected).
6. Transition order to `DELIVERED` via tms-order.
7. If all orders are processed → set `DeliveryManifest.status = COMPLETED`.

### 7.4 Flow 4: Delivery Failed

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/failed`

**Request: `ConfirmDeliveryFailureRequest`**

```json
{
  "failure_reason": "RECIPIENT_NOT_HOME",
  "note": "Called 3 times, no answer",
  "current_lat": 21.028,
  "current_lng": 105.834
}
```

**Logic in `DeliveryManifestServiceImpl.confirmFailed(...)`:**

1. Set `DeliveryManifestOrder.status = FAILED`, save `failureReason`.
2. Increment `deliveryAttemptCount`.
3. Transition order to `DELIVERY_FAILED` via tms-order.
4. If `deliveryAttemptCount >= app.delivery.max-attempts`:
   - Auto-transition to `RETURNED_TO_SENDER` (or pending PO manager decision).
5. Update `DeliveryManifest.failedCount++`.

### 7.5 Flow 5: Return to Sender

**Endpoint:** `POST /api/v1/delivery-manifests/{manifestId}/orders/{orderCode}/return`

**Request: `ReturnToSenderRequest`**

```json
{ "note": "Max attempts reached, recipient refused" }
```

**Logic:** Transition order to `RETURNED_TO_SENDER` via tms-order.

---

## 8. Phase 6 — COD & Shipping Fee Collection

### 8.1 Payment Architecture

| Scenario                  | FeePayer | COD | Logic                                              |
| ------------------------- | -------- | --- | -------------------------------------------------- |
| Sender already paid       | SENDER   | Yes | Courier only collects `codAmount`, no shipping fee |
| Receiver pays fee         | RECEIVER | Yes | Courier collects `codAmount` + `shippingFee`       |
| Receiver pays fee, no COD | RECEIVER | No  | Courier only collects `shippingFee`                |

`shippingFee` in `DeliveryManifestOrder` is only set ≠ 0 when `feePayer = RECEIVER`.  
Value comes from `totalShippingFee` of the order in tms-order.

### 8.2 `CodCollectionServiceImpl`

No separate entity at MVP — COD is recorded directly in `DeliveryManifestOrder.codCollected` and `DeliveryManifestOrder.shippingFeeCollected`.

Provides summary endpoint:

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
COD report by post office and date range.

### 8.3 Update `PaymentStatus` in tms-order

After `confirmDelivered(...)`:

- If `feePayer = RECEIVER` and `shippingFeeCollected >= shippingFee`:
  → Call `TmsOrderClient.updatePaymentStatus(orderCode, PAID)`.
- If `feePayer = SENDER` and `paymentStatus` is still `UNPAID`:
  → No change (sender pays via separate payment gateway).

Add internal endpoint in `tms-order`:

**`tms-order`: `POST /api/v1/internal/orders/payment-status`**

```java
// Add to InternalOrderController:
@PostMapping("/payment-status")
public void updatePaymentStatus(@RequestBody UpdatePaymentStatusRequest request) {
    orderService.updatePaymentStatus(request.getOrderCode(),
                                     request.getTenantId(),
                                     request.getPaymentStatus());
}
```

---

## 9. Phase 7 — Extend `TmsOrderClient` (Cross-Service HTTP)

_(Validated: `TmsOrderClient` already exists with `lookupByIds`, `lookupByCodes`, `findPickupCandidates`, `applyTransitions`. Uses `RestClient`, auth headers include `X-Internal-Api-Key` + `X-Tenant-Id` + `X-Internal-Service=first-mile`.)_

**Add 2 new methods to the existing class:**

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

**Update local DTOs:**

- `caller/dto/tms_order/TmsOrderLookupRequest.java` — add `destinationPostOfficeCode`, `statuses`.
- `caller/dto/tms_order/TmsOrderOperationView.java` — add `destinationPostOfficeCode`, `receiverName`, `receiverPhone`, `receiverWardCode`, `receiverProvinceCode`, `receiverAddressDetail`, `receiverLatitude`, `receiverLongitude`, `codAmount`, `totalShippingFee`, `feePayer`, `paymentStatus`.

**Authentication:** Already handled in existing `TmsOrderClient` (forwards JWT + `X-Internal-Api-Key` header).

---

## 10. Phase 8 — Frontend (`serp_web`)

Add to module `serp_web/src/modules/first-mile/` (courier and post office UI already exists). API calls go through gateway path `/first-mile/api/v1/...`.

### 10.1 RTK Query API

Create file: `serp_web/src/modules/first-mile/api/lastMileApi.ts`

```typescript
// Use extraOptions: { service: 'first-mile' }
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

Update `serp_web/src/modules/first-mile/api/transforms.ts` — add transforms for last-mile responses.

### 10.2 Pages to Create

| File                                                      | Purpose                                            |
| --------------------------------------------------------- | -------------------------------------------------- |
| `pages/inbound-sorting/InboundSortingPage.tsx`            | Scan orders from bag, confirm `READY_FOR_DELIVERY` |
| `pages/delivery-manifests/DeliveryManifestListPage.tsx`   | List of delivery manifests                         |
| `pages/delivery-manifests/DeliveryManifestFormPage.tsx`   | Create manifest + view optimized route             |
| `pages/delivery-manifests/DeliveryManifestDetailPage.tsx` | Manifest detail (courier mobile view)              |
| `pages/delivery-execution/DeliveryOrderCard.tsx`          | Component for confirm delivery / failure           |
| `pages/delivery-reports/CodReportPage.tsx`                | COD report by date/post office                     |

### 10.3 Route Map for Delivery Route

`DeliveryManifestDetailPage` embeds a map (using Leaflet or Goong Maps SDK) displaying:

- Post office icon (departure point)
- Icon for each delivery point with `sequence` number
- Connecting line showing the route

---

## 11. Gateway Integration

_(Validated: route `/first-mile/api/v1/**` already exists in `api_gateway` via `first_mile_router.go` using generic proxy to `:8093`.)_

The generic proxy in `api_gateway` already handles **all** paths under `/first-mile/api/v1/` → forwards to `first-mile:8093/api/v1/`. No new gateway routes are needed.

New controllers using paths like `/api/v1/delivery-manifests` and `/api/v1/inbound-orders` will be automatically proxied.

---

## 12. Implementation Order for Agent

Execute in this exact order to avoid compile errors and dependency cycles:

| Step | Task                                                                                   | Service       |
| ---- | -------------------------------------------------------------------------------------- | ------------- |
| 1    | Add receiver/COD fields to `OrderOperationView` record + update `OrderOperationMapper` | `tms-order`   |
| 2    | Add 5 new OrderStatus values to enum                                                   | `tms-order`   |
| 3    | Add 5 last-mile + 3 missing statuses to `first-mile/enums/OrderStatus`                 | `first-mile`  |
| 4    | Add 5 last-mile statuses to `second-mile/enums/OrderStatus`                            | `second-mile` |
| 5    | Add transition rules in `OrderTransitionServiceImpl`                                   | `tms-order`   |
| 6    | Extend `InternalOrderLookupRequest` with `destinationPostOfficeCode` + `statuses`      | `tms-order`   |
| 7    | Update `OrderQueryServiceImpl.lookupOrders(...)` to handle new filters                 | `tms-order`   |
| 8    | Add endpoint `POST /api/v1/internal/orders/payment-status`                             | `tms-order`   |
| 9    | `./mvnw.cmd clean compile` → confirm build passes                                      | `tms-order`   |
| 10   | Update `TmsOrderLookupRequest` + `TmsOrderOperationView` (local DTOs in first-mile)    | `first-mile`  |
| 11   | Add `lookupAtPostOffice(...)` + `updatePaymentStatus(...)` to `TmsOrderClient`         | `first-mile`  |
| 12   | Create migration SQL `delivery-manifests.sql` + `delivery-manifest-orders.sql`         | `first-mile`  |
| 13   | Register 2 SQL files in `spring.sql.init.schema-locations` in `application.yaml`       | `first-mile`  |
| 14   | Add `app.delivery.*` config to `application.yaml`                                      | `first-mile`  |
| 15   | Create enums, domain entities, repositories (DeliveryManifest, DeliveryManifestOrder)  | `first-mile`  |
| 16   | Implement `HaversineUtils` + `DeliveryRouteOptimizationUtils` + unit tests             | `first-mile`  |
| 17   | Implement `OrderSortingServiceImpl` (Flow 1)                                           | `first-mile`  |
| 18   | Implement `DeliveryRouteOptimizationServiceImpl` (inject `PostOfficeRepository`)       | `first-mile`  |
| 19   | Implement `DeliveryManifestServiceImpl` (Flows 2–5)                                    | `first-mile`  |
| 20   | Implement `CodCollectionServiceImpl` + financial summary                               | `first-mile`  |
| 21   | Implement Controllers (`DeliveryManifestController`, `OrderSortingController`)         | `first-mile`  |
| 22   | `./mvnw.cmd clean compile` → confirm build passes                                      | `first-mile`  |
| 23   | Unit test `DeliveryRouteOptimizationUtils` (mandatory)                                 | `first-mile`  |
| 24   | Confirm gateway route `/first-mile/**` works (no new routes needed)                    | `api_gateway` |
| 25   | `InboundSortingPage` + `DeliveryManifestListPage`                                      | `serp_web`    |
| 26   | `DeliveryManifestFormPage` with route map preview                                      | `serp_web`    |
| 27   | `DeliveryOrderCard` (success/failure) + `CodReportPage`                                | `serp_web`    |
| 28   | `npm run build` + `npm run lint` pass                                                  | `serp_web`    |

---

## 13. Testing

### 13.1 Mandatory Unit Tests (`first-mile`)

| Class                            | Test Cases                                                                                                                                                      |
| -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DeliveryRouteOptimizationUtils` | 0 stops; 1 stop; 5 stops → verify 1-based sequence; 2-opt does not increase total distance; depot not duplicated in route; stops without coords → placed at end |
| `HaversineUtils`                 | Hanoi → HCMC ≈ 1490 km (±5 km); same point → 0.0; symmetric points                                                                                              |
| `DeliveryManifestServiceImpl`    | Create manifest from valid orders; error when order has wrong status; correct `totalCodAmount` / `totalShippingFee` calculation by `feePayer`                   |

### 13.2 Mandatory Unit Tests (`tms-order`)

| Class                        | Test Cases                                                                                                                                                                                           |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `OrderTransitionServiceImpl` | `INBOUND_AT_DESTINATION_POST_OFFICE → READY_FOR_DELIVERY` valid; `DELIVERED → READY_FOR_DELIVERY` rejected; `DELIVERY_FAILED → OUT_FOR_DELIVERY` valid; `DELIVERY_FAILED → RETURNED_TO_SENDER` valid |

### 13.3 Integration Checklist

- [ ] Order at `INBOUND_AT_DESTINATION_POST_OFFICE` at correct PO → staff confirms → becomes `READY_FOR_DELIVERY`
- [ ] Create manifest with 5 orders → optimized sequence differs from input order
- [ ] Confirm successful delivery → order → `DELIVERED`, `collectedCodAmount` accumulates correctly
- [ ] Delivery fails 3 times → order → `RETURNED_TO_SENDER`
- [ ] `feePayer = RECEIVER`: after successful delivery → `paymentStatus = PAID` (tms-order)
- [ ] `feePayer = SENDER`: after successful delivery → `paymentStatus` unchanged

---

## 14. Code Rules

- Apply `// Author: SERP Project` header to all new Java files in `first-mile`.
- Controllers thin: no business logic.
- `@Transactional(readOnly = true)` for query methods, `@Transactional(rollbackFor = Exception.class)` for writes.
- All order status changes must go through `TmsOrderClient.applyTransitions(...)` with idempotency key, and enqueue to `OrderTransitionOutbox` for retry.
- Manifest code generated via `String.format("DM-%s-%d-%05d", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), tenantId, seq)`.
- Response shape: `ApiResponse<T>` with `message` from `MessageService`.
- No secrets or credentials in code; use environment variables via `application.yaml`.
- All new classes live in package `serp.project.first_mile.*` — do NOT use `last_mile` as package name.

---

## Appendix: Validation Summary (2026-06-07)

### Confirmed Claims

| Plan Claim                                              | Status       | Notes                                              |
| ------------------------------------------------------- | ------------ | -------------------------------------------------- |
| `OrderOperationView` missing receiver/COD fields        | ✅ Confirmed | Record has only sender fields + basic order info   |
| `tms-order/OrderStatus` needs 5 new statuses            | ✅ Confirmed | Enum stops at `INBOUND_AT_DESTINATION_POST_OFFICE` |
| `first-mile/OrderStatus` missing 8 statuses             | ✅ Confirmed | Missing 3 transit + 5 last-mile statuses           |
| `second-mile/OrderStatus` needs 5 new statuses          | ✅ Confirmed | Same enum as tms-order minus last-mile             |
| `OrderTransitionServiceImpl` needs new rules            | ✅ Confirmed | Rules stop at `INBOUND_AT_DESTINATION_POST_OFFICE` |
| `InternalOrderLookupRequest` only has ids/codes         | ✅ Confirmed | No destination/status filters                      |
| `TmsOrderClient` exists, needs new methods              | ✅ Confirmed | Has lookup/transition, missing delivery-specific   |
| `OrderTransitionOutbox` has `source` field              | ✅ Confirmed | Can reuse with `LAST_MILE_DELIVERY`                |
| Gateway proxies `/first-mile/api/v1/**`                 | ✅ Confirmed | Generic proxy in `first_mile_router.go`            |
| `application.yaml` uses `spring.sql.init` (not Flyway)  | ✅ Confirmed | Schema-locations list pattern                      |
| `PostOffice` has `locationLatitude`/`locationLongitude` | ✅ Confirmed | `@Formula` fields using ST_Y/ST_X                  |
| `HaversineUtils` does not exist yet                     | ✅ Confirmed | No matches in first-mile                           |
| `OrderOperationMapper` uses positional record args      | ✅ Confirmed | Both record and mapper must update together        |
| `TmsOrderTransitionOutboxService` exists                | ✅ Confirmed | Full implementation with scheduled retry           |

### Corrections Applied in This Version

| Issue                                                       | Original (Vietnamese version) | Corrected                                                                          |
| ----------------------------------------------------------- | ----------------------------- | ---------------------------------------------------------------------------------- |
| PostOffice lookup method name                               | `findByCodeAndTenantId()`     | `findByCodeIgnoreCaseAndTenantId()`                                                |
| Migration SQL style                                         | `CREATE TABLE`                | `CREATE TABLE IF NOT EXISTS` (matches existing migrations)                         |
| tms-order path config for payment-status                    | Not mentioned                 | Added `payment-status-path` to `application.yaml` config section                   |
| `destinationPostOfficeCode` already in `OrderOperationView` | Plan implied it was missing   | Field IS in the record already (validated). Only receiver/COD fields truly missing |
