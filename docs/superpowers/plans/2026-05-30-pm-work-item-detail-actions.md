# PM Work Item Detail Actions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add subtask creation, linked work item create/delete, and worklog list/create/edit/delete actions to the PM work item detail dialog.

**Architecture:** Keep `PMWorkItemDetailDialog.tsx` as the shell and move each new action surface into focused child components. Reuse existing `pm_core` endpoints through RTK Query and avoid backend changes unless a contract gap appears. Use existing shared UI primitives and RTK Query cache invalidation to keep the detail view refreshed.

**Tech Stack:** Next.js 15, React 19, TypeScript strict mode, Redux Toolkit Query, Shadcn/Radix UI primitives, Spring Boot `pm_core` API contracts.

---

### Task 1: API Types And RTK Query Hooks

**Files:**
- Modify: `serp_web/src/lib/store/api/apiSlice.ts`
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`

- [ ] **Step 1: Add worklog tag**

Add `'pm/WorkItemWorklogs'` next to the existing PM work item tags in `apiSlice.ts`.

- [ ] **Step 2: Add request and response types**

Add these types to `work-item-api.types.ts`:

```ts
export interface PMIssueLinkTypeApi {
  id: number;
  tenantId?: number | null;
  name: string;
  outwardDescription?: string | null;
  inwardDescription?: string | null;
  isSystem?: boolean;
  readOnly?: boolean;
  createdAt?: number | string;
  createdBy?: number | null;
  updatedAt?: number | string;
  updatedBy?: number | null;
}

export interface PMCreateWorkItemLinkRequest {
  targetId: number;
  linkTypeId: number;
}

export interface PMCreateWorkItemLinkResponse {
  id: number;
  sourceId: number;
  targetId: number;
  linkTypeId: number;
  createdAt?: number | string;
  createdBy?: number | null;
  updatedAt?: number | string;
  updatedBy?: number | null;
}

export interface PMDeleteWorkItemLinkResponse {
  id?: number | null;
  sourceId?: number | null;
  targetId?: number | null;
  linkTypeId?: number | null;
  deletedAt?: number | string | null;
  deletedBy?: number | null;
}

export interface PMWorklogApi {
  id: number;
  workItemId: number;
  authorId?: number | null;
  comment?: string | null;
  startDate: number;
  timeSpent: number;
  createdAt?: number | string;
  createdBy?: number | null;
  updatedAt?: number | string;
  updatedBy?: number | null;
}

export interface PMWorklogDetailApi extends PMWorklogApi {
  workItemTimeSpent?: number | null;
  workItemTimeRemainingEstimate?: number | null;
}

export interface PMWorklogListResponse {
  items: PMWorklogApi[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  workItemId: number;
  workItemTimeSpent?: number | null;
  workItemTimeRemainingEstimate?: number | null;
}

export interface PMUpsertWorklogRequest {
  timeSpent: number;
  startDate: number;
  comment?: string | null;
}
```

- [ ] **Step 3: Add RTK Query endpoints**

Add endpoints in `workItemApi.ts`:

```ts
getPmIssueLinkTypes
createPmWorkItemLink
deletePmWorkItemLink
getPmWorkItemWorklogs
createPmWorkItemWorklog
updatePmWorkItemWorklog
deletePmWorkItemWorklog
```

Use `extraOptions: { service: 'pm' }`, `createDataTransform`, and invalidation described in the design spec.

- [ ] **Step 4: Export hooks and types**

Export the new hooks from `workItemApi.ts` and `api/index.ts`. Export the new types from the `api/index.ts` type barrel.

- [ ] **Step 5: Verify types**

Run: `npm run type-check` from `serp_web/`.

Expected: no TypeScript errors from the new API contracts. If unrelated pre-existing errors appear, record them before continuing.

### Task 2: Create Dialog Defaults For Subtasks

**Files:**
- Modify: `serp_web/src/modules/pm/components/work-items/CreateWorkItemDialog.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/createWorkItemForm.ts`

- [ ] **Step 1: Extend default value helper**

Update `getCreateWorkItemDefaultValues` to accept:

```ts
{
  initialProjectId?: number;
  initialParentId?: number;
}
```

Return `parentId: initialParentId ? String(initialParentId) : ''`.

- [ ] **Step 2: Extend dialog props**

Add props:

```ts
initialParentId?: number;
lockProject?: boolean;
lockParent?: boolean;
onCreated?: (item: PMCreateWorkItemResponse) => void;
```

Pass defaults into `getCreateWorkItemDefaultValues`, disable the Project combobox when `lockProject` is true, disable the Parent combobox when `lockParent` is true, and call `onCreated(created)` after a successful create.

- [ ] **Step 3: Preserve existing behavior**

Keep existing usages valid by making all new props optional. Existing create dialog callers should compile unchanged.

- [ ] **Step 4: Verify type safety**

Run: `npm run type-check` from `serp_web/`.

Expected: no TypeScript errors from the dialog prop change.

### Task 3: Subtask Action Component

**Files:**
- Create: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemSubtaskActions.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`

- [ ] **Step 1: Create component**

Create `PMWorkItemSubtaskActions.tsx` with a compact outline button. When clicked, open `CreateWorkItemDialog` with:

```tsx
initialProjectId={projectId}
initialParentId={workItemId}
lockProject
lockParent
onCreated={handleCreated}
```

Use `pmWorkItemApi.util.invalidateTags` with `useAppDispatch` or another existing dispatch hook if present. If no module hook exists, rely on `createPmWorkItem` invalidating the work item list and call `childrenQuery.refetch()` from the parent after `onCreated`.

- [ ] **Step 2: Wire into Subtasks section**

In `PMWorkItemDetailDialog.tsx`, place the button near the Subtasks section heading or immediately above `WorkItemChildrenList`.

- [ ] **Step 3: Verify type safety**

Run: `npm run type-check` from `serp_web/`.

Expected: component props and dialog integration compile.

### Task 4: Link Action Component

**Files:**
- Create: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemLinkActions.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemRelationLists.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`

- [ ] **Step 1: Create link action component**

Build a compact form with:

- link type `Select` from `useGetPmIssueLinkTypesQuery`
- target work item `Combobox` from `useSearchPmWorkItemsQuery`
- submit button calling `useCreatePmWorkItemLinkMutation`

Exclude the current work item from target options.

- [ ] **Step 2: Add delete support to link list**

Extend `WorkItemLinksList` props with optional:

```ts
onDeleteLink?: (linkId: number) => void;
deletingLinkId?: number | null;
```

Render a ghost icon button on each link when `onDeleteLink` is provided.

- [ ] **Step 3: Wire create/delete into detail dialog**

Use `useDeletePmWorkItemLinkMutation` in `PMWorkItemDetailDialog.tsx` or the link action component. Show success/error toasts and let RTK Query invalidation refresh links.

- [ ] **Step 4: Verify type safety**

Run: `npm run type-check` from `serp_web/`.

Expected: link creation and deletion components compile.

### Task 5: Worklog Panel

**Files:**
- Create: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemWorklogPanel.tsx`
- Modify: `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx`

- [ ] **Step 1: Create conversion helpers inside component file**

Use local helpers:

```ts
function secondsToMinutes(value?: number | null): string {
  return value ? String(Math.floor(value / 60)) : '';
}

function minutesToSeconds(value: string): number {
  return Math.max(1, Number(value || 0)) * 60;
}
```

- [ ] **Step 2: Create list and form UI**

Use `useGetPmWorkItemWorklogsQuery` to render worklogs. Add a create form with minutes, start date, and comment. Add edit mode per item with the same fields. Add delete action per item.

- [ ] **Step 3: Wire mutations**

Use:

```ts
useCreatePmWorkItemWorklogMutation
useUpdatePmWorkItemWorklogMutation
useDeletePmWorkItemWorklogMutation
```

Show toasts with `getErrorMessage` on failures. Reset edit/create form on success.

- [ ] **Step 4: Add panel to detail dialog**

Render `PMWorkItemWorklogPanel` between Linked work items and Activity.

- [ ] **Step 5: Verify type safety**

Run: `npm run type-check` from `serp_web/`.

Expected: worklog panel compiles and hook payloads match backend contract.

### Task 6: Final Verification

**Files:**
- No new production edits unless verification finds issues.

- [ ] **Step 1: Format changed frontend files**

Run: `npm run format` from `serp_web/`.

Expected: Prettier formats the changed files.

- [ ] **Step 2: Run lint**

Run: `npm run lint` from `serp_web/`.

Expected: lint exits 0, or any pre-existing unrelated failures are captured with file references.

- [ ] **Step 3: Run type-check**

Run: `npm run type-check` from `serp_web/`.

Expected: TypeScript exits 0, or any pre-existing unrelated failures are captured with file references.

- [ ] **Step 4: Run format check**

Run: `npm run format:check` from `serp_web/`.

Expected: format check exits 0.

- [ ] **Step 5: Inspect diff**

Run: `git diff --stat` and `git diff --check`.

Expected: diff only contains planned frontend and plan changes; no whitespace errors.
