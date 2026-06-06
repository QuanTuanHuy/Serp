# TMS Bagging Frontend Implementation Guide

Author: Nguyen The Anh
Description: Part of Serp Project - Frontend guide for second-mile bagging

This document is for AI agents implementing the TMS bagging UI in
`serp_web/src/modules/first-mile/`. It summarizes the current backend contract
and the frontend work needed to expose it safely.

Backend companion guide:

- `second-mile/BAGGING_BUSINESS_GUIDE.md`

## Business Position

Bagging starts after post-office-to-hub handover inbound is confirmed.

Current upstream flow:

1. Post office creates a handover manifest and scans orders out.
2. Driver departs the post office and arrives at the target hub.
3. Hub manager or hub employee scans orders in.
4. `second-mile` sets the manifest order `scanInTime`.
5. `second-mile` enqueues an order transition to `tms-order`.
6. `tms-order` moves each received order to `INBOUND_AT_ORIGIN_HUB`.
7. Bagging can start for those orders.

Expected bagging flow:

1. Hub operation user creates or selects an editable bag.
2. User scans or enters order codes.
3. Backend validates that each order is ready for bagging, belongs to the bag
   origin hub, matches the bag destination, is not already in another bag, and
   fits capacity.
4. Accepted orders are added to the bag.
5. User seals the bag.
6. `tms-order` status moves to `BAGGED`, then `BAG_SEALED`.

Destination target rule:

- Use `HUB` only when the order destination post office belongs to a different
  hub from the current receiving hub.
- If the order destination post office belongs to the same current hub, create
  or select a `POST_OFFICE` bag using the destination post-office code.
- If the destination post office is the same as the receiving post office,
  create or select a `POST_OFFICE` bag using that destination post-office code.
- Do not create or suggest a `HUB` bag where `originHubId` equals
  `destinationHubId`.

Do not implement bag dispatch or bag arrival in this UI yet. Backend has
`BagStatus.IN_TRANSIT` and `BagStatus.ARRIVED`, but there are no public bag
dispatch or arrival endpoints in the current `BagController`.

## Current Backend Scope

Backend module: `second-mile`

Controller:

- `second-mile/src/main/java/serp/project/second_mile/ui/controller/BagController.java`
- Base path: `/api/v1/bags`
- Gateway path from frontend: `/second-mile/api/v1/bags`
- Frontend RTK endpoint must use `extraOptions: { service: 'second-mile' }`.

Service:

- `second-mile/src/main/java/serp/project/second_mile/service/impl/BagServiceImpl.java`

Important backend rules:

- Required role: hub operation role. The service calls
  `ensureHubOperationRoleOrThrow()` and
  `ensureCurrentUserHasActiveHubStaffRoleOrThrow()`.
- Editable bag status is only `CREATED`.
- Orders can be bagged only when their order status is
  `INBOUND_AT_ORIGIN_HUB` or `BAGGING_IN_PROGRESS`.
- Adding an order moves order status to `BAGGED` through the TMS order
  transition outbox.
- Sealing a bag moves all bag orders to `BAG_SEALED`.
- Removing an order from an editable bag moves it back to
  `INBOUND_AT_ORIGIN_HUB`.
- Reopening a sealed bag moves orders from `BAG_SEALED` back to `BAGGED`.
- Default capacity when request fields are omitted or non-positive:
  `max_weight = 50.0`, `max_volume = 0.5`, `max_orders = 30`.

Important design constraint:

- Bags do not currently store `route_id`.
- Do not add route selection to the bagging UI unless backend bag contracts and
  persistence are changed first.
- See `serp_web/src/modules/first-mile/pages/routes/AGENTS.md`, section
  "Bags And Bagging".

## Backend Completeness Audit

The current backend is complete enough for this frontend scope:

- Bag CRUD.
- Add/remove orders to an open bag.
- Validate scanned order batches.
- Suggest matching open bags for a scanned order.
- Auto-plan bags.
- Seal and reopen bags.
- KPI for sealed bags.

The current backend is not complete enough for these UI scopes:

- Dispatch sealed bag from origin hub.
- Assign route for a bag movement.
- Assign driver or driver check-in for bag transport.
- Scan bag arrival at destination hub or destination post office.
- Scan individual orders out of or into a transported bag.
- Handle partial receive, missing order, damaged order, or lost bag.
- Show order statuses after `BAG_SEALED`.

Reasons:

- `BagController` has no dispatch, driver check-in, or receive endpoints.
- `BagStatus.IN_TRANSIT` and `BagStatus.ARRIVED` exist but are not reachable
  through public service actions.
- `bags` do not store `route_id`, planned/actual transport timestamps, driver
  proof, or arrival proof.
- `bag_orders` do not store outbound/inbound scan times or discrepancy results.
- `tms-order` currently stops the bag lifecycle at `BAG_SEALED`; it has no
  accepted statuses for bag dispatched, bag in transit, or destination inbound.
- The bag API validates destination existence, but frontend agents should treat
  same-hub `HUB` bags as invalid. Same-hub destination post-office orders must
  be bagged to `POST_OFFICE`.

Frontend agents must therefore implement the first bagging screen only up to
seal/reopen. Do not add disabled or placeholder transport controls unless the
product request explicitly asks for a future-state mock; operational TMS screens
should expose only backed actions.

## Domain Statuses

Bag destination type:

```ts
export type SecondMileBagDestinationType = 'HUB' | 'POST_OFFICE';
```

Bag status:

```ts
export type SecondMileBagStatus =
  | 'CREATED'
  | 'SEALED'
  | 'IN_TRANSIT'
  | 'ARRIVED'
  | 'CANCELLED';
```

Order statuses relevant to bagging already exist in
`serp_web/src/modules/first-mile/types/index.ts`:

```ts
export type SecondMileOrderStatus =
  | 'INBOUND_AT_ORIGIN_HUB'
  | 'BAGGING_IN_PROGRESS'
  | 'BAGGED'
  | 'BAG_SEALED'
  // plus existing statuses...
```

UI status meaning:

| Backend status | UI meaning | Editable |
| --- | --- | --- |
| `CREATED` | Open bag, can add/remove orders and update metadata | Yes |
| `SEALED` | Bag locked and ready for a future transport step | No, can reopen |
| `IN_TRANSIT` | Future state, not currently reachable via public API | No |
| `ARRIVED` | Future state, not currently reachable via public API | No |
| `CANCELLED` | Future/terminal state, not currently exposed by controller action | No |

## Backend Endpoints

All endpoints return `ApiResponse<T>` except delete, which returns
`ApiResponse<Void>`. Use existing unwrap helpers:

- `unwrapFirstMileResult<T>()`
- `unwrapFirstMilePageResult<T>()`
- `unwrapFirstMileResultOrRaw<T>()` only if you find a raw response shape

Endpoint table:

| Purpose | Method and path | Request | Response |
| --- | --- | --- | --- |
| List bags | `GET /bags` | Query filters | `PageResponse<BagResponse>` |
| Bag detail | `GET /bags/{id}` | none | `BagResponse` |
| Create bag | `POST /bags` | `CreateBagRequest` | `BagResponse` |
| Update bag | `PUT /bags/{id}` | `UpdateBagRequest` | `BagResponse` |
| Delete bag | `DELETE /bags/{id}` | none | `Void` |
| Add order | `POST /bags/{id}/orders` | `AddBagOrderRequest` | `BagResponse` |
| Remove order | `DELETE /bags/{id}/orders/{orderCode}` | none | `BagResponse` |
| Seal bag | `POST /bags/{id}/seal` | none | `BagResponse` |
| Reopen bag | `POST /bags/{id}/reopen` | `ReopenBagRequest` | `BagResponse` |
| Suggestions | `GET /bags/suggestions` | `order_code`, optional `origin_hub_id` | `BagSuggestionResponse[]` |
| Validate | `POST /bags/validate` | `ValidateBaggingRequest` | `BaggingValidationResponse` |
| Auto plan | `POST /bags/auto-plan` | `AutoBaggingPlanRequest` | `AutoBaggingPlanResponse` |
| KPI | `GET /bags/kpis` | `origin_hub_id`, `from`, `to` | `BaggingKpiResponse` |

List query params:

```ts
{
  page?: number;
  size?: number;
  keyword?: string;
  bag_code?: string;
  origin_hub_id?: number;
  destination_type?: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  vehicle_id?: number;
  status?: SecondMileBagStatus;
}
```

Use camelCase in frontend filter types and convert to snake_case in
`firstMileApi.ts` query params.

## TypeScript Types To Add

Add these to `serp_web/src/modules/first-mile/types/index.ts`. Keep frontend
domain models camelCase. Request payload interfaces can use snake_case when they
represent exact HTTP bodies, matching existing TMS request types.

```ts
export type SecondMileBagDestinationType = 'HUB' | 'POST_OFFICE';

export type SecondMileBagStatus =
  | 'CREATED'
  | 'SEALED'
  | 'IN_TRANSIT'
  | 'ARRIVED'
  | 'CANCELLED';

export interface SecondMileBagOrder {
  id: number;
  orderId?: number;
  orderCode?: string;
}

export interface SecondMileBag {
  id: number;
  bagCode?: string;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  maxWeight?: number;
  maxVolume?: number;
  maxOrders?: number;
  currentWeight?: number;
  currentVolume?: number;
  currentOrders?: number;
  status?: SecondMileBagStatus;
  sealedAt?: string;
  note?: string;
  orders?: SecondMileBagOrder[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  tenantId?: number;
}

export interface SecondMileBagListFilters {
  keyword?: string;
  bagCode?: string;
  originHubId?: number;
  destinationType?: SecondMileBagDestinationType;
  destinationHubId?: number;
  destinationPostOfficeCode?: string;
  vehicleId?: number;
  status?: SecondMileBagStatus;
}

export interface CreateSecondMileBagRequest {
  bag_code: string;
  origin_hub_id: number;
  destination_type: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  vehicle_id?: number;
  max_weight?: number;
  max_volume?: number;
  max_orders?: number;
  status?: SecondMileBagStatus;
  note?: string;
}

export type UpdateSecondMileBagRequest = CreateSecondMileBagRequest & {
  status: SecondMileBagStatus;
};

export interface AddSecondMileBagOrderRequest {
  order_code: string;
}

export interface ReopenSecondMileBagRequest {
  reason: string;
}

export interface ValidateSecondMileBaggingRequest {
  bag_id: number;
  order_codes: string[];
}

export interface SecondMileBaggingValidationItem {
  orderCode: string;
  accepted: boolean;
  reason?: string;
}

export interface SecondMileBaggingValidation {
  bagId: number;
  acceptedCount: number;
  rejectedCount: number;
  items: SecondMileBaggingValidationItem[];
}

export interface AutoSecondMileBaggingPlanRequest {
  origin_hub_id: number;
  destination_type: SecondMileBagDestinationType;
  destination_hub_id?: number;
  destination_post_office_code?: string;
  order_codes: string[];
  execute?: boolean;
}

export interface AutoSecondMileBaggingPlanItem {
  bagCode: string;
  orderCodes: string[];
  totalWeight?: number;
  totalVolume?: number;
}

export interface AutoSecondMileBaggingPlan {
  executed: boolean;
  bagCount: number;
  items: AutoSecondMileBaggingPlanItem[];
}

export interface SecondMileBagSuggestion {
  bagId: number;
  bagCode?: string;
  remainingWeight?: number;
  remainingVolume?: number;
  remainingOrders?: number;
}

export interface SecondMileBaggingKpi {
  originHubId: number;
  sealedBagCount: number;
  avgFillRateWeight: number;
  avgFillRateVolume: number;
  avgOrdersPerBag: number;
}
```

## Transform Functions To Add

Add normalizers to `serp_web/src/modules/first-mile/api/transforms.ts`:

- `normalizeSecondMileBagOrder`
- `normalizeSecondMileBag`
- `normalizeSecondMileBagPage`
- `normalizeSecondMileBagSuggestion`
- `normalizeSecondMileBaggingValidation`
- `normalizeAutoSecondMileBaggingPlan`
- `normalizeSecondMileBaggingKpi`

Follow existing patterns:

- Use `readField`, `readOptionalNumber`, and `readRecord`.
- Accept both snake_case and camelCase fields.
- Default `orders` to `[]` when absent.
- For numeric metrics, prefer `0` when backend returns `null`.

Critical mappings:

| Backend field | Frontend field |
| --- | --- |
| `bag_code` | `bagCode` |
| `origin_hub_id` | `originHubId` |
| `destination_type` | `destinationType` |
| `destination_hub_id` | `destinationHubId` |
| `destination_post_office_code` | `destinationPostOfficeCode` |
| `vehicle_id` | `vehicleId` |
| `max_weight` | `maxWeight` |
| `max_volume` | `maxVolume` |
| `max_orders` | `maxOrders` |
| `current_weight` | `currentWeight` |
| `current_volume` | `currentVolume` |
| `current_orders` | `currentOrders` |
| `sealed_at` | `sealedAt` |
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |
| `tenant_id` | `tenantId` |

## RTK Query Endpoints To Add

Add imports/types and endpoints in
`serp_web/src/modules/first-mile/api/firstMileApi.ts`.

Also add a `Bag` tag in `src/lib/store/api/apiSlice.ts` tag types. Keep the tag
name simple and local, for example:

```ts
'SecondMileBag'
```

Suggested endpoints:

```ts
getSecondMileBags
getSecondMileBagById
createSecondMileBag
updateSecondMileBag
deleteSecondMileBag
addSecondMileBagOrder
removeSecondMileBagOrder
sealSecondMileBag
reopenSecondMileBag
getSecondMileBagSuggestions
validateSecondMileBagging
autoPlanSecondMileBags
getSecondMileBaggingKpis
```

Endpoint wiring rules:

- `extraOptions: SECOND_MILE_SERVICE` on every bag endpoint.
- List endpoint `providesTags: [{ type: 'SecondMileBag', id: 'LIST' }]`.
- Detail endpoint provides both list and item tags where useful.
- Create/update/delete/add/remove/seal/reopen/auto-plan with `execute: true`
  must invalidate `LIST` and related bag item id.
- `validate` and `suggestions` can avoid invalidation.
- `auto-plan` with `execute: false` should not invalidate. If the endpoint
  cannot inspect request easily in `invalidatesTags`, invalidating list is
  acceptable but noisier.

Example query param conversion:

```ts
query: ({
  page = 0,
  size = 20,
  keyword,
  bagCode,
  originHubId,
  destinationType,
  destinationHubId,
  destinationPostOfficeCode,
  vehicleId,
  status,
}) => ({
  url: '/bags',
  params: {
    page,
    size,
    ...(keyword ? { keyword } : {}),
    ...(bagCode ? { bag_code: bagCode } : {}),
    ...(originHubId !== undefined ? { origin_hub_id: originHubId } : {}),
    ...(destinationType ? { destination_type: destinationType } : {}),
    ...(destinationHubId !== undefined
      ? { destination_hub_id: destinationHubId }
      : {}),
    ...(destinationPostOfficeCode
      ? { destination_post_office_code: destinationPostOfficeCode }
      : {}),
    ...(vehicleId !== undefined ? { vehicle_id: vehicleId } : {}),
    ...(status ? { status } : {}),
  },
}),
extraOptions: SECOND_MILE_SERVICE,
transformResponse: normalizeSecondMileBagPage,
```

## Route And Files To Add

Recommended frontend route:

- URL: `/first-mile/bags`
- App route wrapper:
  `serp_web/src/app/first-mile/bags/page.tsx`
- Module page:
  `serp_web/src/modules/first-mile/pages/bags/BagListPage.tsx`
- Barrel:
  `serp_web/src/modules/first-mile/pages/bags/index.ts`

Suggested page structure:

```text
src/modules/first-mile/pages/bags/
  BagListPage.tsx
  bagPageModels.ts
  components/
    BagResultsTable.tsx
    BagFormDialog.tsx
    BagDetailDialog.tsx
    BagScanOrdersDialog.tsx
    BagValidationDialog.tsx
    AutoBaggingDialog.tsx
    BagKpiPanel.tsx
```

Keep `src/app/first-mile/bags/page.tsx` thin:

```tsx
import { BagListPage } from '@/modules/first-mile/pages/bags';

export default function Page() {
  return <BagListPage />;
}
```

Add `export * from './bags';` to
`serp_web/src/modules/first-mile/pages/index.ts` if needed.

Navigation wiring depends on the current layout/menu system. Reuse the existing
TMS navigation pattern and add a "Bags" entry under hub or second-mile
operations.

## Page UX Requirements

All visible UI copy must be English.

Primary users:

- `TMS_ADMIN`
- `TMS_HUB_MANAGER`
- `TMS_HUB_EMPLOYEE`

Do not expose bag management actions for `TMS_HUB_DRIVER`.

Recommended first screen:

- Dense operational table, not a landing page.
- Header: "Bags"
- Primary action: "New bag"
- Secondary actions: "Auto plan", "Refresh"
- Filters: keyword, origin hub, destination type, destination hub or post
  office, vehicle, status.
- KPI strip for selected origin hub and date range, if an origin hub filter is
  selected.

Recommended table columns:

- Bag code
- Origin hub
- Destination
- Vehicle
- Status
- Orders
- Weight
- Volume
- Sealed at
- Actions

Action availability:

| Action | Status | Notes |
| --- | --- | --- |
| Edit | `CREATED` | Update bag metadata/capacity |
| Delete | `CREATED` | Only if backend accepts it |
| Add orders | `CREATED` | Scan or paste order codes |
| Remove order | `CREATED` | From detail dialog |
| Seal | `CREATED` with `currentOrders > 0` | Backend rejects empty bag |
| Reopen | `SEALED` | Requires reason |
| View detail | All statuses | Read-only for non-editable statuses |

Status labels:

```ts
const BAG_STATUS_LABELS = {
  CREATED: 'Open',
  SEALED: 'Sealed',
  IN_TRANSIT: 'In transit',
  ARRIVED: 'Arrived',
  CANCELLED: 'Cancelled',
} satisfies Record<SecondMileBagStatus, string>;
```

Destination labels:

- `HUB`: show target hub code/name when available, otherwise `Hub #id`.
- `POST_OFFICE`: show destination post office code.
- Avoid displaying raw enum labels to users.

Capacity display:

- Weight: `currentWeight / maxWeight kg`
- Volume: `currentVolume / maxVolume m3`
- Orders: `currentOrders / maxOrders`
- Show progress bars only if compact and readable.

## Bag Creation Rules

Form fields:

- Bag code, required.
- Origin hub, required.
- Destination type, required: `Hub` or `Post office`.
- Destination hub, required when destination type is `HUB`.
- Destination post office code, required when destination type is
  `POST_OFFICE`.
- Vehicle, optional.
- Max weight, optional. Backend default: `50.0`.
- Max volume, optional. Backend default: `0.5`.
- Max orders, optional. Backend default: `30`.
- Note, optional.

Use existing data endpoints:

- Origin/destination hubs: `useGetHubsQuery`.
- Vehicles: `useGetSecondMileVehiclesQuery`, filtered by selected origin hub
  when practical.
- Post office options: if no direct second-mile post-office lookup exists for
  bagging, accept a code input or reuse hub-post-office mapping endpoints when
  scoped to a hub.

Target selection:

- For an inter-hub movement, select `Hub` and a destination hub different from
  the origin hub.
- For same-hub destination post-office orders, select `Post office` and enter
  the destination post-office code.
- For orders where destination post office equals the receiving post office,
  select `Post office` and enter that destination post-office code.
- Do not let users submit `Hub` with the same origin and destination hub ids,
  even if the current backend has not rejected it yet.

Important:

- Send `status: 'CREATED'` only if required by form reuse. Backend forces
  create/update status to `CREATED` after validation.
- Do not allow editing a sealed bag; use reopen first.

## Scan Orders Into Bag

Recommended scan workflow:

1. User opens "Add orders" for an open bag.
2. Dialog focuses an order code input.
3. User scans or types one code at a time.
4. For a single code, optionally call suggestions first when no bag is selected.
5. For selected bag, call validate before adding a batch.
6. Show validation result with accepted/rejected rows.
7. Add accepted orders one by one through `POST /bags/{id}/orders`.
8. Refetch bag detail/list after successful adds.

Why add one by one:

- Backend currently exposes single-order add.
- Bulk validation exists, but bulk add does not.
- `auto-plan` is the only bulk execution endpoint and creates new bags.

Normalize scanned codes:

```ts
const normalizeOrderCode = (value: string) => value.trim().toUpperCase();
```

Prevent duplicate scans in local state before calling backend.

Validation endpoint:

```json
{
  "bag_id": 123,
  "order_codes": ["ORD-001", "ORD-002"]
}
```

Validation response:

```json
{
  "bag_id": 123,
  "accepted_count": 1,
  "rejected_count": 1,
  "items": [
    { "order_code": "ORD-001", "accepted": true, "reason": null },
    { "order_code": "ORD-002", "accepted": false, "reason": "Order is not ready for bagging." }
  ]
}
```

Use `reason` as supporting text in the validation table. Do not rely on exact
backend English wording for client-side branching.

## Suggestions Workflow

Suggestions endpoint:

```http
GET /bags/suggestions?order_code=ORD-001&origin_hub_id=10
```

Use case:

- User scans an order before choosing a bag.
- UI asks backend which open bags match the order destination and capacity.
- Show candidate bags ordered by backend preference.
- If no suggestions, offer "Create bag" with destination inferred only if the
  UI has enough order context. The current suggestions response does not include
  destination target, so do not invent it from suggestions alone.
- If order context shows the destination post office is under the current hub,
  the create-bag shortcut must preselect `Post office`, not `Hub`.

Response fields:

- `bagId`
- `bagCode`
- `remainingWeight`
- `remainingVolume`
- `remainingOrders`

## Auto Bagging

Auto plan endpoint:

```json
{
  "origin_hub_id": 10,
  "destination_type": "HUB",
  "destination_hub_id": 20,
  "order_codes": ["ORD-001", "ORD-002"],
  "execute": false
}
```

Same-hub destination post-office example:

```json
{
  "origin_hub_id": 10,
  "destination_type": "POST_OFFICE",
  "destination_post_office_code": "PO-DST-001",
  "order_codes": ["ORD-003", "ORD-004"],
  "execute": false
}
```

Preview mode:

- Send `execute: false`.
- Backend returns planned bag groups but does not persist bags.
- Show groups with bag code placeholders `PLAN-1`, `PLAN-2`, etc.

Execute mode:

- Send same request with `execute: true`.
- Backend creates bags and assigns orders.
- Response `executed` is `true`.
- Invalidate/refetch bag list and related order views.

Limitations:

- Auto plan uses backend default capacity values only.
- It does not accept vehicle id.
- It creates `CREATED` bags with generated bag codes.
- It does not seal bags automatically.

## KPI Panel

KPI endpoint:

```http
GET /bags/kpis?origin_hub_id=10&from=2026-06-01T00:00:00&to=2026-06-05T23:59:59
```

Only call when:

- `originHubId` is selected.
- `from` and `to` are valid ISO local datetime strings.

Display:

- Sealed bag count.
- Average weight fill rate as percent.
- Average volume fill rate as percent.
- Average orders per bag.

If backend returns zeroes, show zero states rather than errors.

## Data Dependencies

Required existing endpoints:

- Hubs: `useGetHubsQuery`
- Second-mile vehicles: `useGetSecondMileVehiclesQuery`
- Orders: existing TMS order query can be used for read-only reference, but
  scan workflows can work with order code input alone.

Potential missing convenience endpoint:

- There is no dedicated "list orders ready for bagging at hub" endpoint in the
  bag API. If product requires a selectable order list, use the existing order
  list endpoint with status `INBOUND_AT_ORIGIN_HUB` if it supports the needed
  filters, or add a backend endpoint first.

## Role Gating

Use roles from:

```ts
const roles = useAppSelector(
  (state) => state.account.user.profile?.roles ?? []
);
```

Recommended helpers:

```ts
const canManageBags = (roles: string[]) =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE');
```

If access is denied, show an access card with English copy:

> Bagging requires hub operation access.

## Error Handling

Use RTK Query `.unwrap()` in `try/catch`.

Use:

- `useNotification()`
- `getErrorMessage(error)`

Recommended success messages:

- "Bag created successfully."
- "Bag updated successfully."
- "Order added to bag."
- "Order removed from bag."
- "Bag sealed successfully."
- "Bag reopened successfully."
- "Auto bagging plan created."
- "Auto bagging executed successfully."

Recommended error messages:

- "Failed to create bag."
- "Failed to update bag."
- "Failed to add order to bag."
- "Failed to seal bag."
- "Failed to validate bagging."

Keep all UI copy in English.

## Implementation Checklist

1. Add `SecondMileBag` types in `types/index.ts`.
2. Add bag normalizers in `api/transforms.ts`.
3. Add `SecondMileBag` tag type in `src/lib/store/api/apiSlice.ts`.
4. Add bag RTK Query endpoints in `api/firstMileApi.ts`.
5. Export generated hooks from `firstMileApi.ts`.
6. Add `/first-mile/bags` app route.
7. Add `pages/bags` module files.
8. Add TMS navigation entry for "Bags".
9. Implement list/filter/KPI view.
10. Implement create/update dialog.
11. Implement detail dialog with order rows.
12. Implement scan/add orders dialog with validation.
13. Implement seal and reopen flows with confirmation dialogs.
14. Implement auto-plan preview and execute dialog.
15. Run focused ESLint, type-check, and manual QA.

## Manual QA Scenarios

Use a local environment with gateway, `second-mile`, and `tms-order` running.

Happy path:

1. Complete a post-office-to-hub handover.
2. Confirm hub inbound for at least one order.
3. Confirm the order status becomes `INBOUND_AT_ORIGIN_HUB`.
4. Create a bag for the matching origin hub and destination.
5. Validate the order code.
6. Add the order.
7. Confirm bag metrics update.
8. Seal the bag.
9. Confirm order status becomes `BAG_SEALED`.

Validation cases:

- Scan an unknown order code.
- Scan an order not in `INBOUND_AT_ORIGIN_HUB` or `BAGGING_IN_PROGRESS`.
- Scan an order already assigned to another bag.
- Scan an order whose destination does not match the bag target.
- Try to create a `Hub` bag with the same origin hub and destination hub.
- Create a `Post office` bag for an order whose destination post office belongs
  to the same current hub.
- Exceed max weight, max volume, or max order count.
- Try to edit/add/remove after seal.
- Reopen a sealed bag with a reason.

## Verification Commands

From `serp_web/`:

```bash
npx eslint src/modules/first-mile/pages/bags/BagListPage.tsx
npx prettier --check src/modules/first-mile/pages/bags/BagListPage.tsx
npm run type-check
```

If multiple files are added, run:

```bash
npm run lint
npm run format:check
npm run type-check
```

Known current repository note:

- `npm run type-check` may fail because unrelated modules reference
  `react-leaflet` without installed types/modules. Do not treat those existing
  unrelated errors as bagging implementation errors, but report them in handoff.

## Non-Goals For This Frontend Task

Do not implement these unless backend/product scope changes:

- Bag route selection.
- Bag dispatch to linehaul.
- Driver check-in for bags.
- Bag arrival scan.
- Delivery to destination post office.
- New backend migrations or new second-mile endpoints.

## Handoff Expectations

When an agent completes the frontend implementation, the handoff should state:

- Route added.
- Endpoints and tags added.
- Screens/components added.
- Verification commands run.
- Any missing backend support or unrelated type-check failures.
- Manual QA steps performed or not performed.
