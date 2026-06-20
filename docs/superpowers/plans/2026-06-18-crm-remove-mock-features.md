# CRM Remove Mock Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove CRM frontend features that are backed only by mock data or nonexistent backend APIs.

**Architecture:** Keep CRM screens wired to existing RTK Query endpoints under `serp_web/src/modules/crm/api`. Hide or remove UI surfaces whose behavior depends on mock data, console-only callbacks, or backend routes that do not exist in `crm/src/main/java/serp/project/crm/ui/controller`.

**Tech Stack:** Next.js App Router, React 19, TypeScript, Redux Toolkit Query, Spring Boot CRM API.

---

### Task 1: Remove Unsupported CRM API Exports

**Files:**

- Modify: `serp_web/src/modules/crm/api/crmApi.ts`
- Modify: `serp_web/src/modules/crm/api/activityApi.ts`
- Modify: `serp_web/src/modules/crm/api/customerApi.ts`
- Modify: `serp_web/src/modules/crm/api/leadApi.ts`
- Modify: `serp_web/src/modules/crm/index.ts`

- [ ] Remove `analyticsApi` from the CRM API barrel because backend CRM has no `/api/v1/analytics/*` controller.
- [ ] Remove bulk delete mutations pointing at nonexistent `/activities/bulk-delete`, `/accounts/bulk-delete`, `/leads/bulk-delete` style endpoints.
- [ ] Stop exporting mock services from the CRM module root.

### Task 2: Replace Dashboard Mock Data With Real API Data

**Files:**

- Modify: `serp_web/src/app/crm/dashboard/page.tsx`

- [ ] Remove imports from `@/modules/crm/mocks`.
- [ ] Use existing account, lead, opportunity, pipeline, and activity RTK Query hooks.
- [ ] Show only metrics and lists that can be derived from returned API data.
- [ ] Remove sales trend and mock upcoming task sections.

### Task 3: Remove Mock-Only Analytics Route

**Files:**

- Modify: `serp_web/src/app/crm/analytics/page.tsx`
- Modify: `serp_web/src/modules/crm/pages/index.ts`

- [ ] Replace the analytics route with a plain unsupported-state page linking back to CRM dashboard.
- [ ] Stop exporting the mock analytics page from the CRM pages barrel.

### Task 4: Clean Customer Detail Mock Features

**Files:**

- Modify: `serp_web/src/modules/crm/pages/customers/CustomerDetailPageEnhanced.tsx`

- [ ] Remove mock orders, documents, notes, primary mock contacts, and mock related opportunities.
- [ ] Remove dropdown actions for send email, create order, and add note.
- [ ] Remove Orders, Opportunities, and Documents tabs when they rely on mock data.
- [ ] Replace the ad hoc log activity dialog with navigation to `/crm/activities` because activity create/list APIs already exist elsewhere.

### Task 5: Verify

**Files:**

- Verify from: `serp_web/`

- [ ] Run `npm run lint`.
- [ ] Run `npm run type-check`.
- [ ] Run `npm run format:check`.
