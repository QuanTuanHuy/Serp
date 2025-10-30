# SERP Web (Next.js 15) — Frontend Agent Guide

Purpose: Make productive edits in `serp_web/` fast. This app uses Next.js App Router, TypeScript, Tailwind, Shadcn UI, Redux Toolkit (RTK) + RTK Query, and a modular domain layout.

## Big picture
- App Router in `src/app/**`; domain code in `src/modules/**`; cross-cutting UI/utilities in `src/shared/**`.
- Global providers are wired in `src/app/layout.tsx` via `StoreProvider`, `ThemeProvider`, `NotificationProvider`.
- Redux store with persistence lives in `src/lib/store/store.ts` (redux-persist). Feature reducers are combined here.
- API layer: single base slice in `src/lib/store/api/apiSlice.ts` using RTK Query. It:
  - Targets `${NEXT_PUBLIC_API_BASE_URL}/api/v1` (see `.env`).
  - Injects JWT from `state.account.auth.token` on requests and auto-refreshes via `POST /auth/refresh-token` using `state.account.auth.refreshToken`.
  - Shares types/utilities in `src/lib/store/api/{types.ts,utils.ts}` including timestamp transforms.

## Conventions that matter
- Module isolation: no cross-imports between modules; use each module’s barrel exports (`src/modules/*/index.ts`).
- Per-module layout: `components/`, `hooks/`, `services/`, `store/`, `types/`, optional `contexts/`. Good examples:
  - Admin: `src/modules/admin/**` (rich store + services under domain folders like `services/plans/plansApi.ts`).
  - Settings: `src/modules/settings/**` (org-scoped services, guards, sidebar context).
- API endpoints are added with `api.injectEndpoints` and grouped by domain inside `services/<domain>/*Api.ts` (e.g., `admin/services/plans/plansApi.ts`, `settings/services/users/usersApi.ts`). Use tag types already declared in `apiSlice.ts` (namespaced like `admin/*`, `settings/*`); add new tags there if introducing new entities.
- Response contract: `{ code, status, message, data }`. Prefer helpers from `src/lib/store/api/utils.ts`: `createDataTransform`, `createPaginatedTransform`, `getErrorMessage`. Timestamps are epoch; transforms output ISO strings.
- Path alias: `@/*` → `src/*` (see `tsconfig.json`).
- UI: Shadcn components under `src/shared/components/*`; styling with Tailwind 4. Use module guards (`AdminAuthGuard`, `SettingsAuthGuard`) and layouts found in each module’s `components/layout/*`.

## Typical workflows
- Run dev: `npm run dev` (Turbopack). Build: `npm run build`; Prod serve: `npm run start`.
- Quality gates: `npm run type-check`, `npm run lint`, `npm run format`.
- Env: set `NEXT_PUBLIC_API_BASE_URL` to the API Gateway (defaults to `http://localhost:8080`).

## Adding a new feature (examples based on Admin/Settings)
1) Endpoints (Admin – Plans): create `src/modules/admin/services/plans/plansApi.ts` and inject endpoints:
  - Import `{ api }` from `@/lib/store/api` then:
    `export const plansApi = api.injectEndpoints({ endpoints: (b) => ({ getSubscriptionPlans: b.query({ url: '/subscription-plans' }) }) })`.
  - Use `transformResponse: createDataTransform<SubscriptionPlan[]>()` and tag with `{ type: 'admin/Plan', id }` and `'LIST'` (see existing file).
  - For nested resources (plan modules), invalidate both `admin/PlanModule` and `admin/Plan` as shown in `plansApi.ts`.
2) State (Admin UI): add UI slice under `src/modules/admin/store/<feature>/*Slice.ts` (e.g., `plans/plansSlice.ts` with `setViewMode`, dialogs, selected IDs). Export in `src/modules/admin/store/index.ts`; central `src/lib/store/store.ts` already mounts `admin: adminReducer`.
3) Endpoints (Settings – Users): create `src/modules/settings/services/users/usersApi.ts` with org-scoped filters and `transformResponse: createPaginatedTransform<UserProfile>()`. Namespace tags as `{ type: 'settings/User', id }`.
4) UI: build pages under `src/app/admin/*` or `src/app/settings/*`, and use module guards/layouts (`AdminAuthGuard`, `AdminLayout`, `SettingsLayout`). Consume generated hooks from your `*Api.ts`.

## Auth and routing notes
- Auth state slices live under `src/modules/account/store/*`. The API layer relies on `setTokens` and `clearAuth` from there—don’t rename without updating `apiSlice.ts`.
- On 401, baseQuery auto-tries `/auth/refresh-token`; if it fails, it dispatches `clearAuth()`.

## Useful references
- Store: `src/lib/store/store.ts` (reducers, persistence, middleware).
- API base: `src/lib/store/api/apiSlice.ts`; helpers: `src/lib/store/api/utils.ts` and `types.ts`.
- Admin module: `src/modules/admin/services/plans/plansApi.ts`, `store/plans/plansSlice.ts`, `components/layout/*`.
- Providers: `src/shared/providers/*`; Date utils: `src/shared/utils/dateTransformer.ts`.

Keep it concrete: add endpoints via `injectEndpoints`, keep state in module slices, wire reducers in the central store, and put shared UI in `src/shared/components`. If you add a new entity type, also add a cache tag in `apiSlice.ts`.
