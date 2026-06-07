# AGENTS.md - TMS Routes UI Guide

This guide applies to coding work in `serp_web/src/modules/first-mile/pages/routes/`.
Read it together with:

- `AGENTS.md`
- `serp_web/AGENTS.md`
- `serp_web/src/modules/first-mile/AGENTS.md`
- `second-mile/AGENTS.md`

The web module is historically named `first-mile`, but the Routes screen is a
second-mile feature. All user-visible UI copy on this screen must stay in English.

## Business Scope

`Route` is second-mile transport master data for a fixed operating line. It is
not:

- a pickup optimization route from first-mile dispatch planning;
- a billing `RouteType` such as `NOI_TINH_NOI_CUM`;
- an order timeline itself.

Operationally, a route connects one origin hub to either:

- another hub (`destination_type = HUB`), or
- a post office assigned to that origin hub (`destination_type = POST_OFFICE`).

Routes are stored in the `second-mile` service and exposed through
`/second-mile/api/v1/routes`. They become important when dispatching post-office
handover manifests because the selected route determines the origin hub,
destination post office, assigned vehicle, driver context, and route information
sent into the TMS order timeline.

## Source Of Truth

Backend route model and validation:

- `second-mile/src/main/java/serp/project/second_mile/domain/Route.java`
- `second-mile/src/main/java/serp/project/second_mile/ui/controller/RouteController.java`
- `second-mile/src/main/java/serp/project/second_mile/service/impl/RouteServiceImpl.java`
- `second-mile/src/main/java/serp/project/second_mile/dto/request/CreateRouteRequest.java`
- `second-mile/src/main/java/serp/project/second_mile/dto/request/UpdateRouteRequest.java`
- `second-mile/src/main/java/serp/project/second_mile/dto/request/RouteFilterRequest.java`
- `second-mile/src/main/java/serp/project/second_mile/dto/response/RouteResponse.java`
- `second-mile/src/main/resources/db.migration/zzz_routes.sql`

Frontend screen and API wiring:

- `serp_web/src/app/first-mile/routes/page.tsx`
- `serp_web/src/modules/first-mile/pages/routes/RouteListPage.tsx`
- `serp_web/src/modules/first-mile/pages/routes/components/SecondMileRoutesMap.tsx`
- `serp_web/src/modules/first-mile/api/firstMileApi.ts`
- `serp_web/src/modules/first-mile/types/index.ts`

Related workflows:

- Hub to post-office mapping:
  `second-mile/src/main/java/serp/project/second_mile/ui/controller/HubController.java`
  and `useGetHubPostOfficesQuery`.
- First-mile post office dispatch:
  `first-mile/src/main/java/serp/project/first_mile/service/impl/PostOfficeHandoverManifestServiceImpl.java`.
- Second-mile handover manifests:
  `second-mile/src/main/java/serp/project/second_mile/service/impl/HandoverManifestServiceImpl.java`.
- Order timeline context:
  `tms-order/src/main/java/serp/project/tms_order/dto/request/InternalOrderStatusTransitionRequest.java`.
- Billing route classification:
  `tms-billing-service/src/main/java/serp/project/tms_billing_service/core/service/support/RouteClassificationService.java`.

## API Contract

Use RTK Query through `firstMileApi.ts`; do not call `fetch` directly.

Routes endpoints must use:

```ts
extraOptions: { service: 'second-mile' }
```

Current endpoint contracts:

- `GET /routes`
  - query params: `page`, `size`, `keyword`, `route_code`, `origin_hub_id`,
    `destination_type`, `destination_hub_id`, `destination_post_office_code`,
    `vehicle_id`, `status`
  - response: `ApiResponse<PageResponse<RouteResponse>>`
  - frontend transform: `unwrapFirstMilePageResult<SecondMileRoute>`
- `GET /routes/{id}`
  - response: `ApiResponse<RouteResponse>`
- `POST /routes`
  - body: `SecondMileCreateRouteRequest`
  - backend role: `TMS_ADMIN`
- `PUT /routes/{id}`
  - body: `SecondMileUpdateRouteRequest`
  - backend role: `TMS_ADMIN`
- `DELETE /routes/{id}`
  - backend role: `TMS_ADMIN`

Use TypeScript camelCase for models and snake_case for request bodies/query
params, matching `types/index.ts`.

## Core Route Fields

`SecondMileRoute` fields:

- `id`
- `routeCode`
- `routeName`
- `originHubId`
- `destinationType`: `HUB` or `POST_OFFICE`
- `destinationHubId`
- `destinationPostOfficeCode`
- `vehicleId`
- `estimatedDistanceKm`
- `estimatedDurationMinutes`
- `fixedDepartureTime`
- `status`: `ACTIVE` or `INACTIVE`
- `note`

`fixedDepartureTime` is a local time, not a date-time. Do not add timezone or
date data in the UI.

## Validation Rules To Mirror In UI

The backend remains the authority, but the screen should prevent obvious invalid
submissions.

For all routes:

- `route_code` and `route_name` are required and trimmed.
- `route_code` is unique per tenant, case-insensitive.
- `origin_hub_id` and `destination_type` are required.
- `estimated_distance_km` and `estimated_duration_minutes` must be non-negative
  when provided.
- Create defaults to `ACTIVE` if status is omitted; update requires `status`.
- All reads/writes are tenant-scoped from the current JWT context.

For `destination_type = HUB`:

- `destination_hub_id` is required.
- `destination_hub_id` must belong to the same tenant.
- origin hub and destination hub must be different.
- `destination_post_office_code` must be omitted.
- `vehicle_id` is optional, but if present it must be an active second-mile
  vehicle from the origin hub and its assigned driver staff must be active.

For `destination_type = POST_OFFICE`:

- `destination_post_office_code` is required.
- the post office must be mapped to the selected origin hub in second-mile
  `HubPostOfficeMapping`.
- `destination_hub_id` must be omitted.
- `vehicle_id` is required.
- the vehicle must be active, belong to the origin hub, and have an active
  assigned driver staff.

When the user changes `originHubId`, reset dependent fields:

- `destinationHubId`
- `destinationPostOfficeCode`
- `vehicleId`

When the user changes `destinationType`, clear the inactive destination field.

## Data Dependencies For The Screen

Use these sources:

- hubs: `useGetHubsQuery`, service `second-mile`
- route CRUD: `useGetSecondMileRoutesQuery`,
  `useCreateSecondMileRouteMutation`, `useUpdateSecondMileRouteMutation`,
  `useDeleteSecondMileRouteMutation`
- second-mile vehicles: `useGetSecondMileVehiclesQuery`
- post office labels and coordinates: `useGetPostOfficesQuery`, service
  `first-mile`
- hub-to-post-office mappings: `useGetHubPostOfficesQuery`, service
  `second-mile`

Do not rely only on the full post office list for `POST_OFFICE` route
destinations. Backend validation requires the post office to be mapped to the
origin hub; filter destination choices from `useGetHubPostOfficesQuery` and join
with post office data only for display names and coordinates.

Vehicle selection should be filtered by `hubId`, `status: 'ACTIVE'`, and
preferably `assignedStaffId` when available. A post-office route without a valid
driver-backed vehicle cannot be used in handover manifests.

## Roles And Access

Backend currently allows route list/detail reads without `@PreAuthorize`, but
create/update/delete require `TMS_ADMIN`.

The current UI gates the whole Routes screen to `TMS_ADMIN`. If product wants
read-only access for hub users, keep mutation buttons and destructive actions
hidden/disabled and leave backend authorization unchanged unless the backend
scope is explicitly changed.

## Related Workflows

### Post Office To Hub Handover

First-mile dispatch sends `vehicle_id`, `route_id`, `planned_departure_at`, and
`planned_arrival_at` in `DispatchPostOfficeHandoverManifestRequest`.

Second-mile validates the synced manifest with these route rules:

- route is required and exists;
- route is `ACTIVE`;
- route tenant matches current tenant;
- route origin hub equals the manifest target hub;
- route destination type is `POST_OFFICE`;
- route destination post office code equals the manifest origin post office;
- route has a dedicated vehicle;
- selected vehicle equals route vehicle.

The handover UI should therefore query selectable routes with:

- `originHubId = targetHubId`
- `destinationType = POST_OFFICE`
- `destinationPostOfficeCode = originPostOfficeCode`
- `status = ACTIVE`

After route selection, auto-fill or lock the vehicle to `route.vehicleId` where
the workflow requires route consistency.

### Second-Mile Handover Manifests

The second-mile manifest service stores `routeId`, `routeCode`, `vehicleId`, and
driver context in the order transition context. `tms-order` only records this
context in order timeline/status transitions; it does not own route master data.

### Bags And Bagging

Bags use origin hub, destination type, destination hub/post office, vehicle, and
order destination validation. They do not currently store `route_id`. Do not add
route selection to bagging UI unless backend bag contracts and persistence are
changed first.

### Billing Route Types

Billing `RouteType` is calculated from sender/receiver ward and province data
for pricing. It is not the same as a second-mile `Route`. Do not mix billing
route type filters, labels, or enums into this screen.

## UI Implementation Guidance

Keep the first screen as an operational work surface:

- dense filter row for keyword, status, origin hub, destination type, destination
  hub/post office, and vehicle when needed;
- table with route code/name, origin, destination, assigned vehicle, fixed
  departure time, estimated distance/duration, and status;
- map preview for routes with known hub/post-office coordinates;
- create/edit dialog or side panel using existing shared UI primitives;
- clear empty states for no routes and no mappable coordinates.

Map behavior:

- `SecondMileRoutesMap` only renders lines where both endpoints have
  coordinates.
- Missing coordinates should not block list usage.
- Use hub coordinates from second-mile hubs and post office coordinates from
  first-mile post offices.

Form behavior:

- make `Vehicle` optional for hub-to-hub routes and required for hub-to-post
  office routes;
- block origin hub equal to destination hub;
- use `getErrorMessage(...)` and `useNotification()` for API errors;
- call RTK mutation `.unwrap()` inside `try/catch`;
- refresh route list after successful mutations if route tags are not added.

Deletion:

- backend deletes the route directly and does not check historical manifest
  references in service code.
- prefer setting a used route to `INACTIVE` over deleting it when the route may
  have been used in manifests or order timelines.
- confirm deletes with route code and route name.

## Coding Checklist

When changing the Routes screen:

- keep route pages under `src/modules/first-mile/pages/routes/`;
- keep `src/app/first-mile/routes/page.tsx` as a thin wrapper;
- keep API additions in `api/firstMileApi.ts` with `SECOND_MILE_SERVICE`;
- keep types in `types/index.ts` with `SecondMile*` names;
- preserve `Author: Nguyen The Anh` headers in new or touched TMS files;
- keep all UI copy in English;
- do not introduce direct cross-module imports outside established TMS module
  boundaries;
- run `npm run type-check` from `serp_web/` after TypeScript changes.

