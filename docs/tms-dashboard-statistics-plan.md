# TMS Reporting Dashboard Implementation Plan

## Objective

Build a reporting dashboard for the TMS modules so users can quickly monitor order volume, operational status, delivery success rate, revenue, COD, and detailed performance across the three transportation legs.

The dashboard must support role-based data access:

- `Admin`: can view all statistics within the tenant/organization scope.
- `Hub Manager`: can only view statistics for the hubs they manage.
- `Post Office Manager`: can only view statistics for the post offices they manage.

## Module Scope

- `tms-order`: source of truth for orders, order statuses, sender/receiver locations, related post offices, and COD when COD is stored on the order.
- `first-mile`: first-mile pickup, post office intake, bagging, and handover to the first hub.
- `second-mile`: middle-mile transportation between hubs, including bags, routes, vehicles, manifests, and hub processing time.
- `last-mile`: final delivery from destination hub/post office to recipient. If last-mile logic currently lives inside `first-mile`, separate the reporting domain logic first; a physical service split is not required for phase 1.
- `tms-billing-service`: source of truth for revenue, shipping fees, COD reconciliation, invoices, ledger entries, or transactions if available.
- `serp_web`: dashboard page, API integration, charts, tables, filters, and role-aware UI.
- `api_gateway`: route authenticated dashboard API traffic through the gateway when called from the frontend.

## Design Principles

- Backend must enforce data authorization. Frontend role handling is only for UX and must not be trusted for security.
- Dashboard endpoints are read-heavy. Prefer optimized aggregate queries, projection DTOs, indexes, and short-lived cache where appropriate.
- Do not expose persistence entities directly through dashboard APIs. Create dedicated report DTOs.
- Every report request must include a time context: `fromDate`, `toDate`, `timezone`, and `granularity`.
- Every response should include the resolved `scope` so the frontend can display what data the user is viewing.
- If data lives in multiple services, prefer a backend report facade or aggregation endpoint instead of making the frontend assemble many service responses.

## Shared Filters

All dashboard widgets should use the same filter model:

- Date range: today, last 7 days, last 30 days, current month, custom range.
- Granularity: day, week, month.
- Hub: visible only when the user can access multiple hubs or is an Admin.
- Post Office: visible only when the user can access multiple post offices, or when an Admin/Hub Manager needs drill-down.
- Transportation leg: all, first-mile, middle-mile, last-mile.
- Optional order/service type if available: intra-province, inter-province, COD, non-COD, express, standard.

## Overview KPI Row

### 1. Total Order Volume

Purpose:

- Display total orders in the selected time range.
- Compare against the previous period with the same duration.
- Split by new, in-progress, completed, cancelled, and returned orders.

Recommended data source:

- `tms-order` should be the primary source.
- The dashboard status groups must be mapped consistently from `OrderStatus`.

Metrics:

- `totalOrders`
- `newOrders`
- `inProgressOrders`
- `completedOrders`
- `cancelledOrders`
- `returnedOrders`
- `growthRatePercent`

### 2. Real-Time Order Status

Purpose:

- Display current order distribution by status.
- Support donut/bar charts and a status summary table.

Recommended data source:

- `tms-order` for the current order status.
- If order transition/event logs exist, add a "recently updated" indicator later.

Metrics:

- `statusCode`
- `statusName`
- `count`
- `percentage`
- `lastUpdatedAt`

Real-time strategy:

- Phase 1: poll every 30-60 seconds.
- Phase 2: add WebSocket/SSE only if the notification/event-stream infrastructure is stable.

### 3. Delivery Success Rate

Purpose:

- Measure delivered orders against orders that reached a delivery outcome.
- Break down failed/returned reasons when data is available.

Recommended formula:

```text
successRate = deliveredOrders / (deliveredOrders + failedDeliveryOrders + returnedOrders)
```

Confirm with business whether orders cancelled before delivery should be included in the denominator.

Metrics:

- `deliveredOrders`
- `failedDeliveryOrders`
- `returnedOrders`
- `successRatePercent`
- `failedReasons[]`

### 4. Total Revenue and COD

Purpose:

- Display total shipping revenue and COD amounts.
- Support Admin global view and scoped Hub/Post Office views.

Recommended data source:

- Prefer `tms-billing-service` if ledger, invoice, transaction, or reconciliation data already exists.
- If billing is not ready, phase 1 may calculate provisional revenue/COD from `tms-order`, but the response should clearly treat it as estimated.

Metrics:

- `grossRevenue`
- `netRevenue`
- `codAmount`
- `codCollected`
- `codReconciled`
- `codPending`
- `currency`

## Detailed Statistics Across Three Legs

### Leg 1: First Mile

Scope:

- From order creation/pickup acceptance until the shipment reaches the origin post office or is handed over to the first hub.

Required metrics:

- Total first-mile orders.
- Orders awaiting pickup, picked up, received at post office, bagged, and handed over to hub.
- Pickup success rate.
- Average time from order creation to pickup.
- Average time from pickup to post office/hub intake.
- Top post offices by volume.
- Orders breaching pickup SLA.

Backend must identify:

- Which order/manifest statuses represent each first-mile step.
- Which tables/fields contain `pickupPostOfficeId`, `originPostOfficeId`, `firstHubId`, `bagId`, and `manifestId`.

Frontend should display:

- KPI cards for volume, success rate, and SLA breach count.
- Line chart for daily volume.
- Bar chart for top post offices.
- Alert table for pickup SLA breaches.

### Leg 2: Middle Mile / Second Mile

Scope:

- Hub-to-hub transportation, including bags, routes, vehicles, manifests, and inbound/outbound hub processing.

Required metrics:

- Total bags, manifests, and routes in the selected period.
- Orders currently at hub, on route, and arrived at destination hub.
- On-time route completion rate.
- Average hub dwell time.
- Average hub-to-hub transportation time.
- Route/vehicle capacity utilization if load data exists.
- Top hubs by inbound/outbound volume.
- Delayed bags/routes, scan issues, and order count mismatches.

Backend must identify:

- Mapping from `BagStatus`, `RouteStatus`, and manifest statuses to dashboard groups.
- Relationships between bag-order, route-hub, and vehicle-route.
- Hub scoped query rules: a Hub Manager can see inbound, outbound, and in-hub inventory for their assigned hubs.

Frontend should display:

- Hub performance panel.
- Route completion chart.
- Bag status distribution.
- Table of delayed routes/bags requiring action.

### Leg 3: Last Mile

Scope:

- From arrival at destination hub/post office until successful delivery, failed delivery, return, or final resolution.

Required metrics:

- Total last-mile orders.
- Orders awaiting assignment, out for delivery, delivered, failed, and returned.
- Delivery success rate.
- Average time from arrival at destination station to successful delivery.
- Average delivery attempts per order.
- Top post offices or shippers by volume if shipper data exists.
- Delivery SLA breaches.
- Failed delivery and return reasons.

Backend must identify:

- Which service currently owns last-mile status.
- Fields linking destination hub/post office and delivery assignment.
- Business rule for Post Office Manager scope: by delivering post office, destination post office, or both.

Frontend should display:

- Delivery funnel.
- Success/failure chart.
- SLA breach table.
- Drill-down links from widgets to filtered order lists.

## Dashboard Authorization

### Data Scope

Create a shared backend component/function to resolve dashboard scope from the current user:

```text
DashboardScope {
  userId
  tenantId
  organizationId
  roleCodes[]
  accessLevel: ADMIN | HUB_MANAGER | POST_OFFICE_MANAGER
  hubIds[]
  postOfficeIds[]
}
```

Rules:

- Admin:
  - `accessLevel = ADMIN`
  - Do not restrict `hubIds` or `postOfficeIds` unless the request explicitly applies a filter.
- Hub Manager:
  - `accessLevel = HUB_MANAGER`
  - Query only data related to assigned `hubIds`.
  - Return `403` if the request contains a `hubId` outside the resolved scope.
- Post Office Manager:
  - `accessLevel = POST_OFFICE_MANAGER`
  - Query only data related to assigned `postOfficeIds`.
  - Return `403` if the request contains a `postOfficeId` outside the resolved scope.

Confirm with the account module:

- Exact role codes for Admin, Hub Manager, and Post Office Manager.
- Source of user-to-hub and user-to-post-office assignments.
- Multi-role rule: prefer the broadest valid scope, but still restrict by assigned locations unless the user is Admin.

### Frontend Role Behavior

- Admin:
  - Show Hub and Post Office filters.
  - Allow "All" or a specific Hub/Post Office.
- Hub Manager:
  - Default dashboard scope to assigned hub.
  - If assigned to multiple hubs, allow selecting among those hubs only.
  - Post Office filter should only include post offices under the selected/assigned hub when metadata is available.
- Post Office Manager:
  - Hide the Hub filter when it is not useful.
  - Lock or limit the Post Office filter to assigned post offices.

## Backend Plan

### Phase 1: Data Discovery and Contract Finalization

1. Inspect `tms-order`, `first-mile`, `second-mile`, and `tms-billing-service` to identify the source fields for every metric.
2. Create status mappings:
   - Order status -> dashboard order groups.
   - Bag status -> middle-mile groups.
   - Route/manifest status -> operational groups.
3. Identify role codes and user-to-location mapping from `account`.
4. Finalize formulas:
   - Order volume.
   - Delivery success rate.
   - Revenue and COD.
   - SLA for first-mile, middle-mile, and last-mile.
5. Define API contract and DTOs before implementing aggregate queries.

Deliverables:

- Status mapping table in markdown or DTO documentation.
- Request/response DTOs for dashboard APIs.
- List of indexes required for heavy dashboard queries.

### Phase 2: Implement Overview Report API

Recommended endpoint:

```text
GET /api/v1/tms/dashboard/overview
```

Query params:

```text
fromDate
toDate
timezone
granularity
hubId?
postOfficeId?
serviceType?
```

Recommended response:

```json
{
  "scope": {
    "accessLevel": "HUB_MANAGER",
    "hubIds": ["hub-1"],
    "postOfficeIds": []
  },
  "period": {
    "fromDate": "2026-06-01",
    "toDate": "2026-06-08",
    "timezone": "Asia/Saigon",
    "granularity": "DAY"
  },
  "orderVolume": {
    "totalOrders": 1200,
    "growthRatePercent": 8.5
  },
  "orderStatuses": [
    {
      "statusCode": "DELIVERED",
      "statusName": "Delivered",
      "count": 760,
      "percentage": 63.33
    }
  ],
  "deliverySuccess": {
    "deliveredOrders": 760,
    "failedDeliveryOrders": 80,
    "returnedOrders": 40,
    "successRatePercent": 86.36
  },
  "finance": {
    "grossRevenue": 25000000,
    "netRevenue": 23000000,
    "codAmount": 85000000,
    "codCollected": 70000000,
    "codReconciled": 50000000,
    "codPending": 20000000,
    "currency": "VND"
  },
  "lastUpdatedAt": "2026-06-08T10:15:30+07:00"
}
```

Implementation notes:

- Create a dedicated service/application layer, for example `DashboardReportService`.
- Repositories should return aggregate projections, not full entities.
- Validate maximum date range, for example 90 days for real-time dashboard views.
- Add a 30-60 second cache for expensive overview queries if needed.

### Phase 3: Implement Three-Leg Report API

Recommended endpoint:

```text
GET /api/v1/tms/dashboard/legs
```

Response contains three blocks:

```json
{
  "firstMile": {
    "totalOrders": 420,
    "pickupSuccessRatePercent": 94.2,
    "avgPickupMinutes": 85,
    "slaBreachedOrders": 18,
    "statusBreakdown": [],
    "trend": [],
    "topPostOffices": []
  },
  "middleMile": {
    "totalBags": 180,
    "totalRoutes": 32,
    "onTimeRouteRatePercent": 91.5,
    "avgHubDwellMinutes": 140,
    "statusBreakdown": [],
    "topHubs": []
  },
  "lastMile": {
    "totalOrders": 390,
    "deliverySuccessRatePercent": 88.7,
    "avgDeliveryMinutes": 260,
    "failedReasons": [],
    "slaBreachedOrders": 25,
    "trend": []
  }
}
```

Implementation notes:

- If the three legs are owned by multiple services, use one of these approaches:
  - A report facade calls each service internally and merges the response.
  - Each service exposes its own aggregate endpoint, and a gateway/report endpoint combines them.
- Prefer a unified contract for the frontend: one endpoint for the three-leg dashboard in the first implementation phase.
- Be explicit about timezone behavior when grouping by day.

### Phase 4: Drill-Down and Alerts

Recommended endpoints:

```text
GET /api/v1/tms/dashboard/alerts
GET /api/v1/tms/dashboard/trends
GET /api/v1/tms/dashboard/top-performers
```

Minimum alert types:

- Pickup SLA breached orders.
- Delivery SLA breached orders.
- Delayed bags/routes.
- COD pending reconciliation beyond threshold.
- Orders stuck in an abnormal status or delayed at a leg.

Drill-down requirements:

- Every frontend widget should have matching query params to navigate to a filtered order/bag/route list.
- Backend list APIs used for drill-down must enforce the same scope rules as dashboard APIs.

### Phase 5: Testing and Performance

Backend tests:

- Unit test the dashboard scope resolver.
- Unit test formulas for success rate, COD pending, and growth rate.
- Service/repository tests for Admin, Hub Manager, and Post Office Manager filters.
- Test `403` when a user requests data outside their scope.
- Test timezone and date grouping behavior.

Performance:

- Add indexes for frequently filtered columns: `createdAt`, `status`, `hubId`, `postOfficeId`, `orderId`, `routeId`, `bagId`.
- For large tables, consider daily summary/materialized tables by date, hub, and post office.
- Log query duration and response size for dashboard endpoints.

## Frontend Plan

### Phase 1: Inspect Existing UI Patterns

1. Read `serp_web/AGENTS.md` and `serp_web/src/modules/first-mile/AGENTS.md`.
2. Inspect current TMS layout/navigation.
3. Inspect RTK Query patterns in `serp_web/src/lib/store/api/apiSlice.ts` and existing `api.injectEndpoints()` usage.
4. Check whether a chart library already exists. Reuse existing project libraries before adding a new dependency.

Deliverables:

- Dashboard route decision.
- Component reuse/creation list.
- TypeScript API response types.

### Phase 2: Create API Client and Types

Recommended locations:

```text
serp_web/src/modules/first-mile/api/dashboardApi.ts
serp_web/src/modules/first-mile/types/dashboard.ts
```

If the TMS module already has a different API/types convention, follow the existing convention.

Required hooks/types:

- `useGetTmsDashboardOverviewQuery`
- `useGetTmsDashboardLegsQuery`
- `useGetTmsDashboardAlertsQuery`
- `TmsDashboardFilter`
- `TmsDashboardOverview`
- `TmsDashboardLegs`
- `TmsDashboardAlert`

Rules:

- Use `api.injectEndpoints()`.
- Set `extraOptions: { service: '...' }` according to the existing gateway/service convention.
- Do not introduce `any`; use temporary literal unions or explicit placeholder types with clear TODOs if the backend contract is not final.

### Phase 3: Build the Dashboard Page

Recommended route:

```text
/first-mile/dashboard
```

Alternative route if the project has a broader TMS navigation area:

```text
/tms/dashboard
```

Layout:

- Compact header: title, last updated time, date filter, Hub/Post Office filters.
- Overview KPI row:
  - Total order volume.
  - Real-time order status.
  - Delivery success rate.
  - Total revenue and COD.
- Three-leg statistics area:
  - Tabs or three sections: First Mile, Middle Mile, Last Mile.
  - Each leg has compact KPIs, trend chart, status breakdown, and top performers.
- Alerts area:
  - Table of orders/bags/routes requiring action.
  - Navigation buttons/links to filtered detail pages.

UX requirements:

- This is an operational tool, not a landing page.
- Prioritize scanability, dense but readable data, and predictable controls.
- Add loading skeletons per widget.
- Add empty states for scopes with no data.
- Add error states with retry.
- Display the active scope clearly, for example "Hub: HCM-01" or "Post Office: District 1".

### Phase 4: Role-Aware UI

Read user/role data from the existing auth state.

Behavior:

- Admin:
  - Show Hub/Post Office filters.
  - Default to "All".
- Hub Manager:
  - If assigned to one hub, lock the Hub filter and display the hub name.
  - If assigned to multiple hubs, show a dropdown containing only assigned hubs.
  - If metadata is available, Post Office filter should only list post offices under the selected/assigned hub.
- Post Office Manager:
  - Lock the scope to assigned post offices.
  - Hide widgets that are not meaningful for post-office-only scope, or show read-only summaries when the backend returns scoped data.

Important:

- Frontend must not expand scope by sending `hubId` or `postOfficeId` outside the allowed list.
- If backend returns `403`, show "You do not have permission to view this data" and reset filters to the default resolved scope.

### Phase 5: Drill-Down

Every major widget should support navigation:

- Total orders -> order list filtered by date and scope.
- Order status -> order list filtered by status.
- First-mile SLA breach -> delayed pickup order list.
- Middle-mile delayed route -> delayed route/bag list.
- Last-mile failed delivery -> failed delivery order list.
- COD pending -> billing/COD reconciliation page if available.

Use consistent query params:

```text
fromDate
toDate
hubId
postOfficeId
status
leg
alertType
```

### Phase 6: Frontend Verification

Run:

- `npm run type-check`
- `npx eslint` for new/changed dashboard files.
- `npx prettier --check` for new/changed dashboard files.

Manual checks:

- Desktop, tablet, and mobile responsiveness.
- Text does not overflow KPI cards, chart legends, buttons, or filters.
- Admin, Hub Manager, and Post Office Manager behavior using mock data or test accounts.
- Loading, empty, error, and retry states.

## Shared API Contract

### Request Filter

```ts
export type TmsDashboardGranularity = 'DAY' | 'WEEK' | 'MONTH';

export type TmsDashboardFilter = {
  fromDate: string;
  toDate: string;
  timezone: string;
  granularity: TmsDashboardGranularity;
  hubId?: string;
  postOfficeId?: string;
  serviceType?: string;
};
```

### Shared Chart DTOs

```ts
export type TmsDashboardTrendPoint = {
  label: string;
  date: string;
  value: number;
};

export type TmsDashboardBreakdownItem = {
  code: string;
  name: string;
  count: number;
  percentage: number;
};
```

## AI Agent Checklist

### Backend Agent

- [ ] Read the nearest `AGENTS.md` for every module being changed.
- [ ] Identify source data for orders, first-mile, middle-mile, last-mile, and billing.
- [ ] Finalize status mapping and metric formulas.
- [ ] Create report request/response DTOs.
- [ ] Implement dashboard scope resolver and enforce scope in every query.
- [ ] Implement overview API.
- [ ] Implement three-leg API.
- [ ] Add alert/drill-down APIs if included in the current phase.
- [ ] Add tests for authorization, formulas, filters, and timezone grouping.
- [ ] Run the narrowest relevant compile/test command.
- [ ] Update gateway routes if needed.

### Frontend Agent

- [ ] Read `serp_web/AGENTS.md` and the TMS module guide.
- [ ] Create dashboard API types and RTK Query endpoints.
- [ ] Create the dashboard page and register route/navigation.
- [ ] Build KPI cards, chart sections, and alerts table.
- [ ] Implement shared date/scope filters.
- [ ] Implement role-aware UI.
- [ ] Implement loading, empty, error, and retry states.
- [ ] Implement drill-down links.
- [ ] Run type-check, eslint, and prettier for changed files.
- [ ] Verify responsive behavior and main roles.

## Acceptance Criteria

- Admin can view system-wide statistics and filter by Hub/Post Office.
- Hub Manager can only view data for assigned hubs; out-of-scope requests are rejected by the backend.
- Post Office Manager can only view data for assigned post offices; out-of-scope requests are rejected by the backend.
- Overview KPI row includes all four groups: order volume, real-time order status, delivery success rate, revenue and COD.
- Three-leg statistics are available for First Mile, Middle Mile, and Last Mile.
- All widgets update from the shared filter state.
- Loading, empty, error, and retry states are implemented.
- Backend tests cover formulas and authorization scope.
- Frontend passes type-check and lint/prettier checks for related files.

## Questions To Confirm Before Implementation

- Which service currently owns last-mile data, and which statuses mean delivered, failed delivery, and returned?
- Should official revenue and COD come from `tms-billing-service`, or should phase 1 calculate provisional values from `tms-order`?
- What are the SLA thresholds for first-mile, middle-mile, and last-mile by service type?
- Are user-to-hub and user-to-post-office assignments stored in `account` or in TMS modules?
- If a user has multiple roles, which dashboard scope should take priority?
- Does phase 1 require true real-time updates through WebSocket/SSE, or is polling sufficient?
