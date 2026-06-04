# SERP Admin Dashboard Design

## Scope

Build a system operations dashboard for SERP admin using only the `account` service as the data source.

The dashboard replaces the hard-coded `/admin` overview with live operational data for organizations, users, subscriptions, plans, modules, and menu displays. It keeps the existing admin shell, sidebar, auth guard, and route structure.

This spec does not include gateway health, cross-service health, CRM/PM/logistics/sales metrics, billing payment reconciliation, or real-time websocket updates.

## Goals

- Give a SERP administrator a fast account-service overview from one screen.
- Surface operational work that needs attention, especially pending subscriptions, expiring access, suspended organizations, and configuration coverage.
- Replace hard-coded frontend dashboard values with a single backend-backed API response.
- Keep account controllers thin and place aggregation in a use case.
- Avoid schema changes by deriving all metrics from existing account tables.

## Non-Goals

- Do not redesign the full admin module navigation.
- Do not add new infrastructure health checks.
- Do not create charts that require historical snapshots unless the data already exists.
- Do not add a new frontend test framework.
- Do not add database migrations for this MVP.

## Recommended Approach

Add one aggregate endpoint:

```http
GET /api/v1/admin/dashboard
```

The endpoint returns one response containing KPI cards, operational queues, recent organizations, subscription status breakdown, and configuration coverage.

This approach is preferred over client-side aggregation because existing list endpoints are paginated and filtered. A server-side aggregate endpoint avoids multiple frontend requests, avoids pagination mistakes, and keeps authorization and data contracts centralized in `account`.

## Backend Design

### Controller

Add `AdminDashboardController` under `account/src/main/java/serp/project/account/ui/controller`.

Responsibilities:

- Map `GET /api/v1/admin/dashboard`.
- Call `AdminDashboardUseCase`.
- Return the existing `GeneralResponse<?>` shape through `ResponseEntity.status(response.getCode()).body(response)`.
- Keep auth policy consistent with other `/api/v1/admin/**` routes.

### Use Case

Add `AdminDashboardUseCase` under `core/usecase`.

Responsibilities:

- Coordinate account-service aggregation.
- Return `AdminDashboardResponse`.
- Catch expected domain failures as `AppException` where applicable.
- Return `ResponseUtils.success(...)` for successful reads.
- Log and return `internalServerError(...)` for unexpected aggregation errors.

This use case should not access repositories directly. It should depend on services or ports already exposed through the core layer. If a count or recent query is missing, add it to the nearest port/service contract and implement it in the infrastructure adapter.

### Response Shape

Add DTOs under `core/domain/dto/response`.

Primary response:

```java
AdminDashboardResponse {
    AdminDashboardMetrics metrics;
    AdminDashboardActionQueue actionQueue;
    List<AdminDashboardRecentOrganization> recentOrganizations;
    List<AdminDashboardStatusCount> organizationStatuses;
    List<AdminDashboardStatusCount> subscriptionStatuses;
    AdminDashboardConfigurationCoverage configurationCoverage;
    Long generatedAt;
}
```

Metrics:

- `totalOrganizations`
- `activeOrganizations`
- `suspendedOrganizations`
- `expiredOrganizations`
- `totalUsers`
- `activeUsers`
- `suspendedUsers`
- `totalSubscriptions`
- `activeSubscriptions`
- `trialSubscriptions`
- `pendingSubscriptions`
- `expiredSubscriptions`

Action queue:

- `pendingSubscriptions`
- `subscriptionsEndingSoon`
- `trialsEndingSoon`
- `suspendedOrganizations`
- `expiredOrganizations`

Recent organizations:

- `id`
- `name`
- `code`
- `status`
- `userCount`
- `subscriptionStatus`
- `createdAt`

Configuration coverage:

- `totalPlans`
- `activePlans`
- `inactivePlans`
- `totalModules`
- `availableModules`
- `unavailableModules`
- `totalMenuDisplays`
- `visibleMenuDisplays`
- `hiddenMenuDisplays`

### Query Requirements

Prefer repository-level count queries instead of loading full tables.

Organizations:

- Count all organizations.
- Count by `OrganizationStatus`.
- Fetch recent organizations sorted by `createdAt DESC`, limited to 5.

Users:

- Count all users.
- Count by `UserStatus`.
- Count users for a list of recent organization IDs.

Subscriptions:

- Count all subscriptions.
- Count by `SubscriptionStatus`.
- Count active subscriptions ending in the next 7 days.
- Count trial subscriptions ending in the next 7 days.
- Fetch subscription status by organization ID for recent organizations.

Plans, modules, menu displays:

- Count total and active/available/visible records.

### Data Rules

- `endingSoon` means a non-null end or trial timestamp is greater than or equal to now and less than or equal to 7 days from now.
- `pendingSubscriptions` includes `PENDING` and `PENDING_UPGRADE` where shown as action workload, but status breakdown keeps statuses separate.
- If a recent organization has no subscription, use `NO_SUBSCRIPTION` in the frontend-facing DTO field.
- Dashboard data is eventually consistent with current account database state. No snapshot table is introduced.

## Frontend Design

### API

Add `dashboardApi.ts` under `serp_web/src/modules/admin/services/dashboard`.

RTK Query endpoint:

```ts
getAdminDashboard: builder.query<AdminDashboard, void>({
  query: () => ({
    url: '/admin/dashboard',
    method: 'GET',
  }),
  transformResponse: createDataTransform<AdminDashboard>(),
  providesTags: [{ type: 'admin/Dashboard', id: 'OVERVIEW' }],
})
```

Export `useGetAdminDashboardQuery` through `serp_web/src/modules/admin/services/adminApi.ts` and the module barrel if needed.

### Types

Extend `serp_web/src/modules/admin/types/stats.types.ts` or add a dashboard-specific type file.

Keep types explicit and avoid `any`.

### Page

Replace `serp_web/src/app/admin/page.tsx` hard-coded data with a live dashboard.

The page should include:

- Header: `System Operations` with compact generated timestamp if available.
- KPI grid: organizations, users, subscriptions, pending workload.
- Action queue: pending subscriptions, ending soon, trials ending soon, suspended/expired organizations.
- Recent organizations table/list with status badges and quick links to organization detail pages.
- Subscription status breakdown using compact cards or a simple chart via `recharts`.
- Configuration coverage for plans/modules/menu displays.
- Loading skeleton state.
- Empty/fallback state for no data.
- Error state with a retry action.

The UI should be operational and dense rather than marketing-like. Reuse existing shared cards, badges, buttons, and admin status badge styles where they fit.

## Error Handling

Backend:

- Return `GeneralResponse<?>` consistently.
- Use existing exception handling for unexpected failures.
- Do not expose raw SQL or repository details in response payloads.

Frontend:

- Use RTK Query loading/error states.
- Use a retry button that calls `refetch`.
- Display numeric metrics as `0` only when the response is successful and the backend returns zero. During loading, use skeletons.

## Testing And Verification

Backend:

- Add focused tests for `AdminDashboardUseCase` aggregation rules if local patterns make this practical.
- At minimum run `mvnw.cmd -Dtest=AdminDashboardUseCaseTest test` if a focused test is added.
- Run `mvnw.cmd clean compile` for account after backend changes.

Frontend:

- No frontend test framework is configured.
- Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

- Run `npm run build` if route or bundle behavior changes enough to warrant it.

## Rollout

This can ship as one vertical slice:

1. Backend aggregate endpoint and DTOs.
2. Frontend RTK Query dashboard API.
3. `/admin` page replacement with live states.
4. Focused verification for account and frontend.

## Open Decisions

- Use the 7-day ending-soon window for MVP.
- Keep revenue metrics out of this MVP because account subscription records contain amount data, but reliable business revenue reporting requires billing/payment semantics that are outside the approved account-service-only operations scope.
- Keep gateway and cross-service health out of this MVP.
