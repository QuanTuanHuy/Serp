# Second-Mile Bagging Business Guide

Author: Nguyen The Anh
Description: Part of Serp Project - Bagging workflow audit and implementation guide

This document is for AI agents working on the second-mile bag domain. It records
what is currently implemented, what is missing for a complete bag lifecycle, and
the recommended backend sequence before frontend agents expose additional bag
operations.

## Current Business Boundary

The current backend implements bagging up to this boundary:

```text
Orders received at origin hub
  -> create/select bag
  -> add orders to bag
  -> seal bag
  -> order status BAG_SEALED
```

The current backend does not yet implement this later transport boundary:

```text
Sealed bag
  -> dispatch from origin hub
  -> driver/vehicle transport
  -> arrive at destination hub or destination post office
  -> scan bag/orders inbound
  -> transition orders to the next operational status
```

So the system is currently complete for "bagging and sealing", but incomplete
for "bag transport after seal".

## Destination Target Rule

Bag destination must be selected by the next physical handoff point, not only by
the mapped destination hub.

Use this rule when creating bags, suggesting bags, or auto-planning bags:

1. Resolve the current receiving hub from the completed post-office-to-hub
   handover. This is the bag `origin_hub_id`.
2. Resolve the order destination post office from `destination_post_office_code`.
3. If the destination post office maps to a different hub, create or select a
   `HUB` bag for that destination hub.
4. If the destination post office maps to the same hub as `origin_hub_id`, create
   or select a `POST_OFFICE` bag using the destination post-office code.
5. If the destination post office is the same as the receiving post office,
   create or select a `POST_OFFICE` bag using that destination post-office code.

Do not create `HUB -> HUB` bags where `origin_hub_id` equals
`destination_hub_id`. That route has no second-mile transport value and can trap
orders in `BAG_SEALED` without a real downstream movement. Same-hub orders should
continue through local hub-to-post-office bagging.

## Implemented Flow

Upstream handover:

1. Post office dispatches a handover manifest to a hub.
2. Driver performs departure and arrival check-ins.
3. Hub manager or employee scans orders inbound.
4. `HandoverManifestServiceImpl.confirmInbound(...)` sets manifest order
   `scanInTime`.
5. `second-mile` enqueues a `tms-order` transition to
   `INBOUND_AT_ORIGIN_HUB`.

Bagging:

1. Hub operation user creates a bag or chooses an existing `CREATED` bag.
2. User adds orders to the bag.
3. `BagServiceImpl.addOrderToBag(...)` validates:
   - order is `INBOUND_AT_ORIGIN_HUB` or `BAGGING_IN_PROGRESS`
   - order belongs to the bag origin hub
   - order destination matches the bag destination target
   - order is not already assigned to another bag
   - bag capacity is not exceeded
4. Adding an order enqueues a `tms-order` transition to `BAGGED`.
5. User seals the bag.
6. `BagServiceImpl.sealBag(...)` moves bag status to `SEALED`, sets
   `sealedAt`, updates bag-order snapshots to `BAG_SEALED`, and enqueues
   `tms-order` transitions to `BAG_SEALED`.

Important current gap: the backend validates destination existence, but bag
validation does not yet enforce the "no same hub `HUB` bag" rule above.
Backend agents should add that validation before relying on suggestions or
auto-plan for same-hub orders.

## Current Backend Artifacts

Main files:

- `src/main/java/serp/project/second_mile/domain/Bag.java`
- `src/main/java/serp/project/second_mile/domain/BagOrder.java`
- `src/main/java/serp/project/second_mile/enums/BagStatus.java`
- `src/main/java/serp/project/second_mile/enums/BagDestinationType.java`
- `src/main/java/serp/project/second_mile/service/impl/BagServiceImpl.java`
- `src/main/java/serp/project/second_mile/ui/controller/BagController.java`
- `src/main/resources/db.migration/bags.sql`
- `src/main/resources/db.migration/zz_bag_orders.sql`
- `src/main/resources/db.migration/zz_bag_capacity_fields.sql`

Current public endpoints:

| Purpose | Endpoint | Implemented |
| --- | --- | --- |
| List bags | `GET /api/v1/bags` | Yes |
| Bag detail | `GET /api/v1/bags/{id}` | Yes |
| Create bag | `POST /api/v1/bags` | Yes |
| Update editable bag | `PUT /api/v1/bags/{id}` | Yes |
| Delete editable bag | `DELETE /api/v1/bags/{id}` | Yes |
| Add order | `POST /api/v1/bags/{id}/orders` | Yes |
| Remove order | `DELETE /api/v1/bags/{id}/orders/{orderCode}` | Yes |
| Seal bag | `POST /api/v1/bags/{id}/seal` | Yes |
| Reopen sealed bag | `POST /api/v1/bags/{id}/reopen` | Yes |
| Suggest matching bags | `GET /api/v1/bags/suggestions` | Yes |
| Validate bagging batch | `POST /api/v1/bags/validate` | Yes |
| Auto-plan bags | `POST /api/v1/bags/auto-plan` | Yes |
| Bagging KPIs | `GET /api/v1/bags/kpis` | Yes |

## Current Status Model

Bag statuses:

```java
CREATED
SEALED
IN_TRANSIT
ARRIVED
CANCELLED
```

Reachability today:

| Status | Current reachability |
| --- | --- |
| `CREATED` | Created and reopened bags |
| `SEALED` | Sealed bags |
| `IN_TRANSIT` | Enum exists, but no controller/service transition reaches it |
| `ARRIVED` | Enum exists, but no controller/service transition reaches it |
| `CANCELLED` | Enum exists, but no controller/service transition reaches it |

Order statuses in `tms-order` currently stop at `BAG_SEALED` for bagging:

```java
INBOUND_AT_ORIGIN_HUB
BAGGING_IN_PROGRESS
BAGGED
BAG_SEALED
```

There are no current `tms-order` statuses for:

- bag dispatched from origin hub
- bag in transit
- bag arrived at destination hub
- bag arrived at destination post office
- order received from bag at destination

This means backend agents must extend `tms-order` status lifecycle before
second-mile can safely publish post-seal transitions.

## Data Model Gaps

`bags` currently stores:

- `bag_code`
- `origin_hub_id`
- `destination_type`
- `destination_hub_id`
- `destination_post_office_code`
- optional `vehicle_id`
- capacity/current metrics
- `status`
- `sealed_at`
- `note`

Missing for post-seal transport:

- `route_id`
- assigned driver/staff id
- planned departure and planned arrival
- actual dispatched timestamp
- actual arrived timestamp
- dispatch note
- arrival note
- seal code or seal verification record, if required by product
- driver check-in GPS/photo fields, if bags need driver proof like manifests
- destination scan proof
- exception status or reason for lost/damaged/missing bags

`bag_orders` currently stores order identity and snapshots only:

- order id/code
- customer order code
- last known status
- origin/destination post-office codes
- weight/volume snapshots

Missing for destination handling:

- bag outbound scan time per order
- destination inbound scan time per order
- received/missing/damaged per-order result
- discrepancy reason

## Backend Gaps To Complete Bag Lifecycle

### 1. Define Post-Seal Order Lifecycle

Before implementing bag dispatch/arrival, extend `tms-order`:

- Add explicit post-seal statuses.
- Add allowed transitions in `OrderTransitionServiceImpl`.
- Confirm how order timeline should label bag transport events.
- Confirm rollback paths for reopen/cancel after dispatch.

Do not publish new second-mile transitions until `tms-order` accepts them.

Before or alongside post-seal work, fix bag target selection so orders whose
destination post office belongs to the same current hub are bagged to
`POST_OFFICE`, not to `HUB` with the same hub id.

Suggested status shape, subject to product confirmation:

```text
BAG_SEALED
  -> BAG_DISPATCHED_FROM_ORIGIN_HUB
  -> BAG_IN_TRANSIT
  -> INBOUND_AT_DESTINATION_HUB
  -> AT_DESTINATION_POST_OFFICE
```

Use different names if product already has a canonical status vocabulary.

### 2. Add Bag Transport Persistence

Choose one of these designs:

Option A: extend `bags`

- Works only if one bag moves once.
- Add `route_id`, `assigned_driver_id`, planned/actual timestamps and check-in
  proof columns directly to `bags`.

Option B: create `bag_shipments` or `bag_dispatches`

- Preferred if a bag can move through multiple legs or be re-dispatched.
- Keeps bag identity separate from movement attempts.
- Allows each movement to track route, vehicle, driver, planned window,
  driver check-ins, arrival proof, and exceptions.

Do not add frontend route selection until backend has either `route_id` on bags
or a shipment/dispatch resource with `route_id`.

### 3. Add Dispatch Endpoint

Needed endpoint:

```http
POST /api/v1/bags/{id}/dispatch
```

Suggested request:

```json
{
  "route_id": 100,
  "vehicle_id": 20,
  "assigned_driver_id": 30,
  "planned_departure_at": "2026-06-05T09:00:00",
  "planned_arrival_at": "2026-06-05T11:00:00",
  "seal_code": "SEAL-001",
  "note": "Outbound to destination hub"
}
```

Required validations:

- Bag status must be `SEALED`.
- Bag must contain at least one order.
- Route must be active.
- Route origin must match bag origin hub.
- Route destination must match bag destination target.
- Vehicle must be active and compatible with the route.
- Driver must be active and assigned to the hub if driver is required.
- Vehicle capacity should consider bag count, total weight and total volume.
- Vehicle/driver schedule overlap should be prevented.

Expected state transition:

- Bag status: `SEALED -> IN_TRANSIT` or `SEALED -> DISPATCHED` if a new status
  is added.
- Order status: publish the selected post-seal outbound status.

Current enum has no `DISPATCHED`; if product needs a distinct "ready but not
departed" state, add it instead of overloading `IN_TRANSIT`.

### 4. Add Driver Check-In For Bag Transport

If product requires driver proof, mirror the handover manifest approach:

```http
POST /api/v1/bags/{id}/driver-checkin-start
POST /api/v1/bags/{id}/driver-checkin-end
```

Use multipart form fields:

- `latitude`
- `longitude`
- `photo`
- optional `location_label`

Validate distance against origin hub for start and destination hub/post office
for end. If destination is a post office, second-mile may need a reliable
post-office location source from first-mile or tms-order.

### 5. Add Destination Receive Endpoint

Needed endpoint:

```http
POST /api/v1/bags/{id}/receive
```

Suggested request:

```json
{
  "order_codes": ["ORD-001", "ORD-002"],
  "note": "Received without discrepancy"
}
```

Business choices to confirm:

- Is scanning the bag code enough to receive all orders?
- Must destination staff scan every order inside the bag?
- Are partial receives allowed?
- How are missing or damaged orders represented?
- Does destination `HUB` differ from destination `POST_OFFICE` in order
  status?

Expected state transition:

- Bag status: `IN_TRANSIT -> ARRIVED`
- Bag order rows: record inbound scan/results
- Order status: publish destination inbound status

### 6. Add Cancel/Exception Handling

Needed paths depend on product:

- Cancel an empty or open bag.
- Cancel a sealed bag before dispatch.
- Mark lost/damaged in transit.
- Reopen after dispatch should likely be forbidden.
- Partial receive should create discrepancy records rather than silently close.

Do not reuse `delete` for operational cancellation once a bag has orders or has
been sealed. Use explicit cancellation records for auditability.

### 7. Add Eligible Orders Endpoint

Frontend can scan order codes without a candidate list, but a production bagging
screen usually needs eligible order search:

```http
GET /api/v1/bags/eligible-orders
```

Suggested filters:

- `origin_hub_id`
- `destination_type`
- `destination_hub_id`
- `destination_post_office_code`
- `keyword`
- `page`
- `size`

Backend can delegate to `tms-order` lookup with statuses:

- `INBOUND_AT_ORIGIN_HUB`
- `BAGGING_IN_PROGRESS`

## Route Relationship

Today:

- Routes exist as master data.
- Handover manifests store `route_id`.
- Bags do not store `route_id`.
- Bag validation checks origin hub and destination target, not a route.

To complete transport, either:

- add route assignment to bag dispatch/shipment, or
- explicitly document that bag dispatch is not route-bound.

If route-bound dispatch is chosen, validate:

- `route.originType == HUB`
- `route.originHubId == bag.originHubId`
- for `destinationType == HUB`, `route.destinationType == HUB` and
  `route.destinationHubId == bag.destinationHubId`
- for `destinationType == POST_OFFICE`, route destination post-office code
  matches bag destination post-office code
- route vehicle and selected vehicle are consistent

## Frontend Dependency Notes

Frontend can safely implement the current bagging screen only up to:

- list/create/update/delete open bags
- add/remove orders
- validate scans
- suggestions
- auto-plan
- seal/reopen
- KPI

Frontend must not implement these until backend supports them:

- bag route selection
- bag dispatch
- bag driver handover
- bag arrival
- destination order receive
- post-seal order status visualization beyond `BAG_SEALED`

## Recommended Backend Implementation Order

1. Confirm product lifecycle after `BAG_SEALED`.
2. Extend `tms-order` statuses and transition rules.
3. Decide `bags` extension vs `bag_shipments` resource.
4. Add SQL migrations and domain fields/entities.
5. Add request/response DTOs.
6. Add dispatch service method and controller endpoint.
7. Add driver check-in endpoints if required.
8. Add receive endpoint and discrepancy model if partial receive is allowed.
9. Add order transition outbox events for each new status.
10. Add focused unit tests for every status transition.
11. Update frontend guide and API types.

## Backend Test Checklist

Add or update tests for:

- creating a bag with invalid destination target
- rejecting a `HUB` bag whose `origin_hub_id` equals `destination_hub_id`
- auto-plan chooses `POST_OFFICE` when the destination post office belongs to
  the same origin hub
- suggestions return `POST_OFFICE` bag candidates for same-hub destination
  post-office orders
- adding an order not at origin hub
- adding an order already assigned to another bag
- capacity overflow
- sealing an empty bag
- sealing a bag transitions all orders to `BAG_SEALED`
- reopening a sealed bag returns orders to `BAGGED`
- dispatch rejects non-sealed bag
- dispatch rejects route/destination mismatch
- dispatch rejects inactive vehicle/driver
- receive rejects non-in-transit bag
- receive handles partial scans according to product rule
- idempotent transition outbox behavior

## Current Completion Summary

Implemented:

- Bag CRUD
- Bag order add/remove
- Scan validation
- Suggestions
- Auto plan
- Seal/reopen
- KPI over sealed bags
- TMS order transition outbox through `BAG_SEALED`

Missing for full operational bag lifecycle:

- post-seal order statuses in `tms-order`
- route/shipment assignment for bag movement
- dispatch endpoint
- driver check-in/proof endpoints
- destination receive endpoint
- per-order destination scan/discrepancy tracking
- cancellation/exception workflow
- frontend screens for any post-seal transport action
