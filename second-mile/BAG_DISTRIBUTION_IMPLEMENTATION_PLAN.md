# Bag Distribution Implementation Plan

Author: Nguyen The Anh  
Description: Part of Serp Project - AI agent implementation plan for sealed bag distribution

## Current State

Bagging currently stops at sealed bags:

- `BagServiceImpl.sealBag(...)` changes `Bag.status` from `CREATED` to `SEALED`.
- `BagServiceImpl.sealBag(...)` transitions every order in the bag to `BAG_SEALED`.
- `Bag` stores `originHubId`, `destinationType`, `destinationHubId` or `destinationPostOfficeCode`, and an optional `vehicleId`.
- Existing `Route` data can model fixed lanes from a hub to another hub or post office.
- Existing `HandoverManifest` is order-level and post-office-to-hub oriented: `origin_post_office_code`, `target_hub_id`, `order_codes`, `vehicle_id`, `route_id`.

There is no dedicated bag-level distribution workflow yet. Missing pieces:

- No trip/manifest entity links sealed bags to a route, vehicle, and driver.
- No bag-level scan out/in lifecycle.
- No endpoint to auto-plan sealed bags onto route runs.
- No status transition from `BAG_SEALED` to in-transit/arrived order states after bag dispatch.
- No UI page for sealed bag distribution planning and driver execution.

## Target Business Flow

Implement distribution as a bag-level route run:

1. Hub operator views sealed bags waiting at an origin hub.
2. System groups eligible bags by destination target:
   - hub-to-hub: `destinationType = HUB`, target is `destinationHubId`
   - hub-to-post-office: `destinationType = POST_OFFICE`, target is `destinationPostOfficeCode`
3. System suggests active routes and available vehicles/drivers for each group.
4. Operator previews a distribution plan, adjusts assignments, then creates one or more distribution manifests.
5. Driver checks out from origin hub with photo/location.
6. Bags move to `IN_TRANSIT`; contained orders move to a matching in-transit status.
7. Driver checks in at destination hub or post office with photo/location.
8. Bags move to `ARRIVED`; contained orders move to the destination inbound status.
9. Destination staff scans bags and can inspect contained orders.

## Recommended Domain Model

Add a new bag-level manifest instead of overloading `HandoverManifest`.

### Tables

Create `bag_distribution_manifests`.

Required columns:

- `id`
- `manifest_code`
- `origin_hub_id`
- `destination_type`
- `destination_hub_id`
- `destination_post_office_code`
- `route_id`
- `vehicle_id`
- `assigned_driver_id`
- `planned_departure_at`
- `planned_arrival_at`
- `actual_departure_at`
- `actual_arrival_at`
- `driver_start_latitude`
- `driver_start_longitude`
- `driver_start_distance_m`
- `driver_start_photo_url`
- `driver_end_latitude`
- `driver_end_longitude`
- `driver_end_distance_m`
- `driver_end_photo_url`
- `status`
- `note`
- audit fields from `AbstractAudit`

Create `bag_distribution_manifest_bags`.

Required columns:

- `id`
- `manifest_id`
- `bag_id`
- `bag_code`
- `origin_hub_id`
- `destination_type`
- `destination_hub_id`
- `destination_post_office_code`
- `total_weight_snapshot`
- `total_volume_snapshot`
- `total_orders_snapshot`
- `scan_out_time`
- `scan_in_time`
- audit fields from `AbstractAudit`

Indexes and constraints:

- unique `(tenant_id, lower(manifest_code))`
- unique `(manifest_id, bag_id)`
- index `(tenant_id, status, origin_hub_id)`
- index `(tenant_id, route_id)`
- index `(tenant_id, vehicle_id)`
- index `(tenant_id, assigned_driver_id)`
- index `(tenant_id, bag_id)`

Migration file:

- `second-mile/src/main/resources/db.migration/zzzz_bag_distribution_manifests.sql`

### Enums

Add `BagDistributionManifestStatus`:

- `CREATED`
- `OUTBOUND_CONFIRMED`
- `INBOUND_CONFIRMED`
- `CANCELLED`

Reuse `BagDestinationType` for manifest destination.

Keep `BagStatus` as:

- `SEALED` means ready to dispatch
- `IN_TRANSIT` means assigned and departed
- `ARRIVED` means reached destination

Do not add another bag status unless a separate destination scan workflow requires it.

## Backend Implementation

### Package Layout

Add files under `second-mile/src/main/java/serp/project/second_mile/`:

- `domain/BagDistributionManifest.java`
- `domain/BagDistributionManifestBag.java`
- `enums/BagDistributionManifestStatus.java`
- `dto/request/CreateBagDistributionManifestRequest.java`
- `dto/request/AutoPlanBagDistributionRequest.java`
- `dto/request/ConfirmBagDistributionInboundRequest.java`
- `dto/request/BagDistributionManifestFilterRequest.java`
- `dto/response/BagDistributionManifestResponse.java`
- `dto/response/BagDistributionManifestBagResponse.java`
- `dto/response/BagDistributionPlanResponse.java`
- `dto/response/BagDistributionPlanItemResponse.java`
- `repository/BagDistributionManifestRepository.java`
- `repository/BagDistributionManifestBagRepository.java`
- `repository/specification/BagDistributionManifestSpecification.java`
- `service/BagDistributionManifestService.java`
- `service/impl/BagDistributionManifestServiceImpl.java`
- `ui/controller/BagDistributionManifestController.java`

Follow existing `HandoverManifestServiceImpl` patterns for:

- tenant validation
- role validation
- driver-scoped list access
- route validation
- vehicle and driver assignment checks
- schedule overlap checks
- driver check-in photo upload
- distance-to-endpoint validation
- order transition outbox idempotency

### API Endpoints

Base path:

```text
/api/v1/bag-distribution-manifests
```

Endpoints:

```text
GET    /api/v1/bag-distribution-manifests
GET    /api/v1/bag-distribution-manifests/{id}
POST   /api/v1/bag-distribution-manifests
POST   /api/v1/bag-distribution-manifests/auto-plan
POST   /api/v1/bag-distribution-manifests/{id}/confirm-outbound
POST   /api/v1/bag-distribution-manifests/{id}/confirm-inbound
POST   /api/v1/bag-distribution-manifests/{id}/driver-checkin-start
POST   /api/v1/bag-distribution-manifests/{id}/driver-checkin-end
POST   /api/v1/bag-distribution-manifests/{id}/cancel
```

Security:

- list/detail: `TMS_ADMIN`, `TMS_HUB_MANAGER`, `TMS_HUB_EMPLOYEE`, driver role if present in `SecondMileAccessUtils`
- create/auto-plan/cancel: `TMS_ADMIN`, `TMS_HUB_MANAGER`
- confirm outbound/inbound: `TMS_ADMIN`, `TMS_HUB_MANAGER`, `TMS_HUB_EMPLOYEE`
- driver check-in/out: hub roles plus driver role, matching existing handover manifest behavior

### Create Request

```json
{
  "origin_hub_id": 1,
  "destination_type": "HUB",
  "destination_hub_id": 2,
  "destination_post_office_code": null,
  "route_id": 10,
  "vehicle_id": 20,
  "planned_departure_at": "2026-06-06T09:00:00",
  "planned_arrival_at": "2026-06-06T12:00:00",
  "bag_ids": [100, 101],
  "note": "Morning distribution"
}
```

Validation rules:

- every bag belongs to current tenant
- every bag is `SEALED`
- no bag is already in an active distribution manifest
- every bag origin matches `origin_hub_id`
- every bag destination matches request destination
- route is active and matches origin/destination
- selected vehicle equals route vehicle when route has dedicated vehicle
- vehicle is active, at the origin hub, and has active assigned driver
- driver is active at the origin hub
- total bag weight/volume/order count fits vehicle capacity
- planned time window is valid and does not overlap active assignments for vehicle or driver

### Route Validation

For hub-to-post-office distribution:

- `Route.originType = HUB`
- `Route.originHubId = request.originHubId`
- `Route.destinationType = POST_OFFICE`
- `Route.destinationPostOfficeCode = request.destinationPostOfficeCode`

For hub-to-hub distribution:

- `Route.originType = HUB`
- `Route.originHubId = request.originHubId`
- `Route.destinationType = HUB`
- `Route.destinationHubId = request.destinationHubId`

Reject same-hub route for `destinationType = HUB`.

### Lifecycle

Create:

- manifest status: `CREATED`
- bags remain `SEALED`
- no order status transition yet

Confirm outbound or driver check-in start:

- manifest status: `OUTBOUND_CONFIRMED`
- each manifest bag `scan_out_time = now`
- each bag status: `IN_TRANSIT`
- contained orders transition from `BAG_SEALED` to the chosen in-transit status

Confirm inbound or driver check-in end:

- manifest status: `INBOUND_CONFIRMED`
- each manifest bag `scan_in_time = now`
- each bag status: `ARRIVED`
- contained orders transition from in-transit status to destination inbound status

Cancel:

- allowed only from `CREATED`
- manifest status: `CANCELLED`
- bags remain `SEALED`

### Order Status Contract

Check `tms-order` status enum before implementation. If no better statuses exist, add explicit second-mile distribution statuses there and mirror them here:

- `BAG_IN_TRANSIT`
- `INBOUND_AT_DESTINATION_HUB`
- `INBOUND_AT_DESTINATION_POST_OFFICE`

Transition mapping:

- outbound: `BAG_SEALED -> BAG_IN_TRANSIT`
- inbound hub destination: `BAG_IN_TRANSIT -> INBOUND_AT_DESTINATION_HUB`
- inbound post office destination: `BAG_IN_TRANSIT -> INBOUND_AT_DESTINATION_POST_OFFICE`

If adding statuses is too broad for the first slice, keep bag status updates and emit order transition outbox with existing compatible statuses only after confirming the current tms-order lifecycle.

## Simple Smart Suggestion Algorithms

Keep algorithms deterministic and explainable. Do not introduce an optimization library in the first implementation.

### Candidate Bag Filter

Input:

- origin hub
- destination type and destination id/code
- optional planned departure window

Filter:

- `Bag.status = SEALED`
- `Bag.originHubId = originHubId`
- destination exactly matches target
- bag is not in an active manifest

Sort:

1. oldest `sealedAt`
2. larger `currentOrders`
3. larger `currentWeight`

Reason: move old sealed bags first and prefer fuller bags.

### Route Score

For each active route matching origin/destination:

```text
routeScore =
  routeMatchScore
  + scheduleScore
  + vehicleFitScore
  + driverAvailabilityScore
  - delayPenalty
```

Suggested weights:

- `routeMatchScore`: 100 for exact route match
- `scheduleScore`: 0 to 30, higher when fixed departure is near requested departure
- `vehicleFitScore`: 0 to 30, based on remaining capacity after assigning selected bags
- `driverAvailabilityScore`: 0 or 20
- `delayPenalty`: sealed bag age beyond SLA, capped at 20

Use simple formulas:

```text
weightUsage = totalBagWeight / vehicle.maxWeight
volumeUsage = totalBagVolume / vehicle.maxVolume
capacityUsage = max(weightUsage, volumeUsage)
vehicleFitScore = 30 * (1 - abs(0.75 - capacityUsage))
```

Clamp `vehicleFitScore` to `[0, 30]`.

### Bag Grouping

For auto-plan, group by:

```text
originHubId + destinationType + destinationHubId/destinationPostOfficeCode
```

Within each group, use first-fit decreasing:

1. sort sealed bags by descending `max(weightRatio, volumeRatio)` and old `sealedAt`
2. iterate active matching routes by score
3. place each bag into the first vehicle bin that can fit
4. create new suggested manifest bin when needed

This is enough for stable and predictable planning.

### Driver/Vehicle Availability

Reuse the existing active assignment check pattern from `HandoverManifestServiceImpl`:

- active statuses: `CREATED`, `OUTBOUND_CONFIRMED`
- overlap when requested time intersects another active manifest for same `vehicleId` or `assignedDriverId`

Extend this check to include both:

- existing `handover_manifests`
- new `bag_distribution_manifests`

For first implementation, duplicate the repository query in the new repository. Extract a shared availability service only after both flows are stable.

### SLA Hint

Expose lightweight hints in auto-plan response:

- `HIGH_PRIORITY`: sealed longer than configured SLA hours
- `CAPACITY_RISK`: selected bags use more than 90% vehicle capacity
- `LOW_UTILIZATION`: selected bags use less than 40% vehicle capacity
- `NO_ROUTE`: no active route exactly matches target
- `NO_DRIVER`: route vehicle has no active assigned driver
- `SCHEDULE_CONFLICT`: vehicle/driver is busy in requested window

Do not block on hints except `NO_ROUTE`, `NO_DRIVER`, and `SCHEDULE_CONFLICT`.

## Frontend Implementation

TMS frontend location:

```text
serp_web/src/modules/first-mile/
```

Add a page:

```text
pages/bag-distribution/BagDistributionListPage.tsx
```

Add route entry:

```text
src/app/first-mile/bag-distribution/page.tsx
```

API/types:

- add `SecondMileBagDistributionManifest*` types in `types/index.ts`
- add endpoints in `api/firstMileApi.ts` with `extraOptions: { service: 'second-mile' }`
- add normalizers in `api/transforms.ts`

UI workflows:

- tab 1: `Ready bags`
  - filters: origin hub, destination type, destination hub, destination PO, sealed date
  - table: bag code, target, weight, volume, orders, sealed at
- tab 2: `Planning`
  - select target and time window
  - preview auto-plan suggestions with score and hints
  - allow manual route/vehicle override
  - create manifests
- tab 3: `Manifests`
  - list status, route, vehicle, driver, planned times
  - detail drawer with bags and scan status
- driver view:
  - show assigned manifests
  - check-in start/end with location and photo

All user-visible copy must be English per TMS frontend guide.

## Backend Acceptance Criteria

- A sealed bag can be assigned to exactly one active bag distribution manifest.
- A bag cannot be dispatched if it is not `SEALED`.
- A manifest cannot mix destinations.
- Route origin/destination must match every bag.
- Vehicle and driver cannot be double-booked in overlapping active windows.
- Outbound confirmation changes bags to `IN_TRANSIT`.
- Inbound confirmation changes bags to `ARRIVED`.
- Cancel from `CREATED` releases bags without changing bag status.
- Driver-scoped list returns only manifests assigned to that driver.
- All mutations are tenant-scoped and transactional.
- Focused tests cover create, auto-plan, outbound, inbound, cancel, and double-booking.

## Suggested Implementation Slices

### Slice 1: Persistence and Read APIs

Implement:

- migrations
- entities
- repositories
- response mapper
- list/detail endpoints

Verify:

```bash
cd second-mile
.\mvnw.cmd clean compile
```

### Slice 2: Manual Create and Cancel

Implement:

- create endpoint
- cancel endpoint
- validation for bags, route, vehicle, driver, schedule

Tests:

- create rejects mixed destinations
- create rejects non-sealed bags
- create rejects active manifest duplicates
- cancel releases created manifest

### Slice 3: Auto Plan

Implement:

- candidate bag query
- route scoring
- first-fit decreasing bin packing
- preview mode and execute mode

Tests:

- groups bags by exact destination
- prefers active matching route
- respects vehicle capacity
- marks no-route and schedule-conflict hints

### Slice 4: Driver and Hub Lifecycle

Implement:

- confirm outbound
- confirm inbound
- driver check-in start/end
- photo upload and 100m location validation
- bag status updates
- order transition outbox

Tests:

- outbound only from `CREATED`
- inbound only from `OUTBOUND_CONFIRMED`
- driver cannot act on another driver's manifest
- check-in rejects distance over 100m

### Slice 5: Frontend

Implement:

- RTK Query endpoints and transforms
- route page
- ready bag filters
- auto-plan preview
- manifest detail and lifecycle buttons
- driver check-in actions

Verify:

```bash
cd serp_web
npx eslint src/modules/first-mile/pages/bag-distribution/BagDistributionListPage.tsx
npm run type-check
```

If `npm run type-check` fails due unrelated modules, record the existing errors and still run file-level ESLint.

## Files Agents Should Inspect First

Backend:

- `second-mile/AGENTS.md`
- `second-mile/src/main/java/serp/project/second_mile/domain/Bag.java`
- `second-mile/src/main/java/serp/project/second_mile/domain/BagOrder.java`
- `second-mile/src/main/java/serp/project/second_mile/domain/Route.java`
- `second-mile/src/main/java/serp/project/second_mile/domain/Vehicle.java`
- `second-mile/src/main/java/serp/project/second_mile/service/impl/BagServiceImpl.java`
- `second-mile/src/main/java/serp/project/second_mile/service/impl/HandoverManifestServiceImpl.java`
- `second-mile/src/main/java/serp/project/second_mile/service/impl/RouteServiceImpl.java`
- `second-mile/src/main/java/serp/project/second_mile/service/impl/VehicleServiceImpl.java`
- `second-mile/src/main/java/serp/project/second_mile/caller/dto/tms_order/TmsOrderStatusTransitionRequest.java`
- `second-mile/src/main/resources/db.migration/zz_bag_orders.sql`
- `second-mile/src/main/resources/db.migration/zz_handover_manifests.sql`
- `second-mile/src/main/resources/db.migration/zzz_routes.sql`

Frontend:

- `serp_web/src/modules/first-mile/AGENTS.md`
- `serp_web/src/modules/first-mile/pages/bags/BagListPage.tsx`
- `serp_web/src/modules/first-mile/pages/routes/RouteListPage.tsx`
- `serp_web/src/modules/first-mile/api/firstMileApi.ts`
- `serp_web/src/modules/first-mile/api/transforms.ts`
- `serp_web/src/modules/first-mile/types/index.ts`

Cross-service:

- `tms-order/src/main/java/serp/project/tms_order/enums/OrderStatus.java`
- `tms-order/src/main/java/serp/project/tms_order/service/impl/OrderServiceImpl.java`
- `tms-order/src/main/java/serp/project/tms_order/ui/controller/OrderController.java`

## Open Decisions Before Coding

- Confirm exact order statuses for bag distribution in `tms-order`.
- Decide whether hub-to-hub arrival means another bagging step at destination hub or direct PO distribution.
- Decide if destination post office staff will scan bags or individual orders after arrival.
- Decide SLA hours for sealed bags awaiting dispatch.
- Decide whether vehicle capacity should use bag snapshots only or recalculate from contained orders at dispatch time.
