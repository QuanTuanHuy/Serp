# AGENTS.md - TMS (First-Mile & Second-Mile) Frontend Guide for Coding Agents

This guide is for coding agents working on **Transport Management (TMS)** UI in `serp_web/src/modules/first-mile/`.
It covers both **first-mile** and **second-mile** features. Use with `serp_web/AGENTS.md` (global frontend rules) and the root `AGENTS.md`.

Backend references:

- `first-mile/AGENTS.md` — post offices, orders, pickup, dispatch, first-mile vehicles
- `second-mile/AGENTS.md` — hubs, routes, bags, second-mile vehicles

## UI Language Policy

**All user-visible copy must be in English.**

Includes: page titles, labels, placeholders, button text, table headers, empty states, dialog titles, toast/notification messages, validation messages shown in the UI, and `CardDescription` text.

- Do **not** add Vietnamese (or other) strings to React components for TMS pages.
- Backend `i18n` message files may contain multiple locales for API `message` fields; the web app should still present English UX copy in components.
- Prefer clear, concise labels (`Province`, `Ward`, `Post office`, `Courier`, `Refresh`) consistent with existing pages.

## Module Snapshot

- **Code home:** `src/modules/first-mile/` (historical name; includes second-mile screens).
- **Routes:** `src/app/first-mile/**` — thin wrappers that import module pages.
- **Layout:** `FirstMileLayout` + `FirstMileAuthGuard` + `RouteGuard moduleCode='TMS'`.
- **API:** RTK Query in `api/firstMileApi.ts`, `api/billingApi.ts`, `api/transforms.ts`.
- **Types:** `types/index.ts` (large shared file; use `SecondMile*` prefix for second-mile entities).

## Route Map

| Path | Page / scope |
|------|----------------|
| `/first-mile/post-offices` | Post offices (first-mile API) |
| `/first-mile/orders` | Orders |
| `/first-mile/pickup` | Pickup tracking & check-in |
| `/first-mile/dispatchers/first-mile` | Pickup dispatch (auto/manual) |
| `/first-mile/hubs` | Hubs (second-mile API) |
| `/first-mile/routes` | Routes (second-mile API) |
| `/first-mile/vehicles/first-mile` | Post-office vehicles |
| `/first-mile/vehicles/second-mile` | Hub vehicles |
| `/first-mile/billing` | Billing calculator & rules |
| `/first-mile/product-types` | Product types |
| `/first-mile/import-history` | Import jobs |

Legacy redirects (e.g. `/billing` → `/first-mile/billing`) live under `src/app/`; prefer new links under `/first-mile/`.

## Directory Conventions

```text
src/modules/first-mile/
  api/                    # RTK Query endpoints + transforms
  types/                  # Request/response TypeScript types
  components/             # Layout, auth guard
  pages/
    post-offices/         # first-mile
    orders/
    pickup/
    dispatchers/first-mile/
    hubs/                 # second-mile
    routes/
    vehicles/
      first-mile/         # first-mile service
      second-mile/        # second-mile service
    billing/
    ...
src/app/first-mile/       # Route entries only — no business logic
```

- **Pages:** `*Page.tsx` or `*ListPage.tsx`.
- **Dialogs/cards:** `*Dialog.tsx`, `*Card.tsx`.
- **Shared page logic:** `*PageModels.ts`, `*Form.ts` (e.g. `orderPageModels.ts`, `hubForm.ts`).
- **Barrel exports:** `index.ts` at folder boundaries.

## API Wiring (Critical)

All TMS HTTP calls go through the gateway:

- First-mile: `extraOptions: { service: 'first-mile' }` → `/first-mile/api/v1/...`
- Second-mile: `extraOptions: { service: 'second-mile' }` → `/second-mile/api/v1/...`

Defined in `api/firstMileApi.ts` as `FIRST_MILE_SERVICE` / `SECOND_MILE_SERVICE`.

```typescript
// Query params: camelCase in TS → snake_case in HTTP
...(provinceCode ? { province_code: provinceCode } : {}),
...(wardCode ? { ward_code: wardCode } : {}),
```

- Use `transformResponse` with `unwrapFirstMileResult`, `unwrapFirstMilePageResult`, or `unwrapFirstMileResultOrRaw` from `api/transforms.ts`.
- Second-mile hub import may need `normalizeSecondMileHubImportHistory` — copy an existing endpoint pattern.
- Mutations: `.unwrap()` inside `try/catch` + `useNotification()` for errors (`getErrorMessage`).

Do not call `fetch` directly from TMS pages.

## UX Patterns to Reuse

### Location cascades

For post offices, hubs, and similar master data:

1. **Province** → `useGetProvincesQuery`
2. **Ward** (optional “All wards in province”) → `useGetWardsByProvinceCodeQuery`
3. **Entity** (post office / hub) → filtered `useGetPostOfficesQuery` / `useGetHubsQuery`

Reference: `DispatchersPage` + `ManualDispatchCard`, `PickupPage`, `PostOfficeFormDialog`, `HubListPage`.

Reset dependent selections when parent location changes (`useEffect` on `provinceCode` / `wardCode`).

### Role-based UI

- Read roles from `useAppSelector(state => state.account.user.profile)`.
- Gate sections with `TMS_ADMIN`, `TMS_POSTOFFICER_MANAGER`, `TMS_POSTOFFICER`, `TMS_CUSTOMER` as in `PickupPage` / `DispatchersPage`.
- Hide or disable controls the user cannot use; show an access card when scope is `NO_ACCESS`.

### Lists and imports

- Pagination: `page`, `size` (often 20 or 200 for dropdowns).
- Excel import: validate → preview → confirm; reuse `ValidateImportFileResponse` types.
- Keep tables and filters in English; format dates with `toLocaleString('en-US')` unless product asks otherwise.

## Adding a Feature (Checklist)

1. **Backend** endpoint in `first-mile` or `second-mile` (correct service).
2. **Types** in `types/index.ts` (`*Request`, `*Response`, filters).
3. **RTK endpoint** in `firstMileApi.ts` (or `billingApi.ts`) with correct `extraOptions.service`.
4. **Page/components** under `pages/{feature}/`; wire route in `src/app/first-mile/.../page.tsx`.
5. **English copy** for all new labels and messages.
6. Run from `serp_web/`: `npm run type-check`, `npm run lint` on touched files.

## Styling and Components

- Use `@/shared/components` / `@/shared/components/ui` (Shadcn-style).
- Tailwind + `cn()` for layout; semantic tokens from `globals.css`.
- Prefer `Card`, `Select`, `Dialog`, `Badge`, `Button` like neighboring TMS pages.
- Maps: `PickupOrdersMap`, `OrderRoutePreviewMap` — follow existing Leaflet usage in module.

## Extensibility and Maintainability

- Keep `src/app/first-mile/*/page.tsx` as one-line re-exports.
- Split pages over ~500 lines into `components/` and `*PageModels.ts`.
- Put pure functions (risk scoring, filter builders, option mappers) in dedicated `.ts` files (e.g. `manualAssignRisks.ts`).
- Do not import from other business modules (`crm`, `sales`); lift shared TMS helpers to `pages/...` or `shared/` only if truly cross-app.
- When adding second-mile UI, place under `pages/.../second-mile/` or next to hubs/routes, not mixed into first-mile vehicle pages.

## Verification

From `serp_web/`:

```bash
npm run type-check
npm run lint
npx eslint src/modules/first-mile/pages/your-page/YourPage.tsx
```

There is no module-level Jest/Vitest suite today; rely on type-check and manual QA through gateway + both backends running.

## Debugging Checklist

1. Wrong service prefix → 404 on gateway (check `extraOptions.service`).
2. Empty dropdown → `skip` on RTK query, missing `provinceCode`, or zero `size`.
3. Stale cache → `providesTags` / `invalidatesTags` on mutations (match existing endpoints).
4. Auth → token in Redux; `FirstMileAuthGuard` and TMS module subscription.
5. Shape mismatch → compare Network tab JSON with `transforms.ts` unwrap helpers.

## Before You Finish

- English-only UI strings on all new/changed screens.
- Correct `first-mile` vs `second-mile` service on every endpoint.
- Thin app routes; logic in `pages/` or `api/`.
- `npm run type-check` for files you touched.
