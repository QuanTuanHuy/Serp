# PM Optimization Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework the PM optimization frontend into a two-page launch/review flow that matches the backend clean input model and is easier for occasional users to understand.

**Architecture:** Keep `PMProjectOptimizationPage` as the launch surface and `PMProjectOptimizationRunPage` as the review surface. Move contract details into shared PM optimization types and a small local constants module so the pages stay focused on state and layout. Split the large review page into focused UI components so the page file becomes orchestration instead of a monolith.

**Tech Stack:** Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS 4, lucide-react, shared UI primitives from `@/shared/components/ui`.

---

### Task 1: Migrate optimization contracts to clean frontend input

**Files:**
- Create: `serp_web/src/modules/pm/constants/optimization.ts`
- Modify: `serp_web/src/modules/pm/types/optimization.types.ts`
- Modify: `serp_web/src/modules/pm/api/optimizationApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`

- [ ] **Step 1: Replace the legacy generate/review contract types**

Update the shared PM optimization contract to expose the backend-aligned intent model:

```ts
export type PMOptimizationObjective =
  | 'BALANCED_WORKLOAD'
  | 'MINIMAL_REASSIGNMENT'
  | 'SKILL_FIRST'
  | 'DEADLINE_FIRST';

export type PMOptimizationChangeScope =
  | 'ASSIGNMENT_ONLY'
  | 'SCHEDULE_ONLY'
  | 'ASSIGNMENT_AND_SCHEDULE';

export interface PMGenerateOptimizationRunRequest {
  scope?: string;
  algorithmKey?: string;
  objective: PMOptimizationObjective;
  changeScope: PMOptimizationChangeScope;
  planningStart: number;
  planningEnd: number;
  selectedWorkItemIds: number[];
}

export interface PMOptimizationRunApi {
  id: number;
  tenantId: number;
  projectId: number;
  scope?: string | null;
  objective?: PMOptimizationObjective | string | null;
  changeScope?: PMOptimizationChangeScope | string | null;
  status?: PMOptimizationRunStatus | null;
  planningStart?: number | null;
  planningEnd?: number | null;
  selectedWorkItemCount?: number | null;
  summary?: PMOptimizationRunSummaryApi | null;
  algorithmKey?: string | null;
  algorithmVersion?: string | null;
  solverStatus?: string | null;
  objectiveScore?: string | null;
  createdAt?: number | null;
  createdBy?: number | null;
  updatedAt?: number | null;
  updatedBy?: number | null;
  items: PMOptimizationRunItemApi[];
  warnings: PMOptimizationRunWarningApi[];
}
```

Remove `PMOptimizationMode`, `allowReassignment`, and `allowScheduleChanges` from the frontend contract.

- [ ] **Step 2: Add a small local constants module for labels and defaults**

Create a dedicated constants file so the two pages do not duplicate label maps:

```ts
export const PM_OPTIMIZATION_DEFAULT_ALGORITHM_KEY = 'greedy-balanced';

export const PM_OPTIMIZATION_OBJECTIVE_OPTIONS = [
  { value: 'BALANCED_WORKLOAD', label: 'Balanced workload', description: 'Spread work evenly.' },
  { value: 'MINIMAL_REASSIGNMENT', label: 'Minimal reassignment', description: 'Prefer current assignees.' },
  { value: 'SKILL_FIRST', label: 'Skill first', description: 'Prioritize skill fit.' },
  { value: 'DEADLINE_FIRST', label: 'Deadline first', description: 'Prefer late-risk reduction.' },
] as const;

export const PM_OPTIMIZATION_CHANGE_SCOPE_OPTIONS = [
  { value: 'ASSIGNMENT_ONLY', label: 'Assignment only', description: 'Only assignees may change.' },
  { value: 'SCHEDULE_ONLY', label: 'Schedule only', description: 'Only planned dates may change.' },
  { value: 'ASSIGNMENT_AND_SCHEDULE', label: 'Assignment and schedule', description: 'Both channels may change.' },
] as const;
```

- [ ] **Step 3: Update the RTK Query layer to consume the clean contract**

Keep the same endpoints, but make the endpoint signatures and generated hooks use the new request/response types.

```ts
generatePmOptimizationRun: builder.mutation<
  PMOptimizationRunApi,
  { projectId: number; body: PMGenerateOptimizationRunRequest }
>({
  query: ({ projectId, body }) => ({
    url: `/projects/${projectId}/optimization-runs`,
    method: 'POST',
    body,
  }),
  extraOptions: { service: 'pm' },
  transformResponse: createDataTransform<PMOptimizationRunApi>(),
  invalidatesTags: (_result, _error, { projectId }) => [
    { type: 'pm/OptimizationRun' as const, id: `project-${projectId}` },
  ],
})
```

Update `serp_web/src/modules/pm/api/index.ts` so any direct exports point to `PMOptimizationObjective` and `PMOptimizationChangeScope` instead of the removed mode type.

- [ ] **Step 4: Run type-check to expose downstream pages that still use legacy fields**

Run:

```bash
npm run type-check
```

Expected: compile errors in the launch/review pages where `mode`, `allowReassignment`, and `allowScheduleChanges` are still referenced.

- [ ] **Step 5: Commit the contract migration**

```bash
git add serp_web/src/modules/pm/constants/optimization.ts serp_web/src/modules/pm/types/optimization.types.ts serp_web/src/modules/pm/api/optimizationApi.ts serp_web/src/modules/pm/api/index.ts
git commit -m "feat(pm): migrate optimization frontend contract"
```

---

### Task 2: Rebuild the launch page around intent, selection, and solver choice

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx`
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationWorkItemPicker.tsx`
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunIntentPanel.tsx`

- [ ] **Step 1: Replace legacy launch state with clean intent state**

Rewrite the page state so the launch flow stores intent explicitly:

```tsx
const [objective, setObjective] = useState<PMOptimizationObjective>('BALANCED_WORKLOAD');
const [changeScope, setChangeScope] = useState<PMOptimizationChangeScope>('ASSIGNMENT_AND_SCHEDULE');
const [algorithmKey, setAlgorithmKey] = useState(PM_OPTIMIZATION_DEFAULT_ALGORITHM_KEY);
```

The payload sent to generate must become:

```tsx
const body: PMGenerateOptimizationRunRequest = {
  scope: 'SELECTED_WORK_ITEMS',
  algorithmKey,
  objective,
  changeScope,
  planningStart,
  planningEnd,
  selectedWorkItemIds: selectedIds,
};
```

- [ ] **Step 2: Extract the work item list into a focused picker component**

Move the search/filter/selection list into `PMOptimizationWorkItemPicker` so the page only manages state and layout.

```tsx
type PMOptimizationWorkItemPickerProps = {
  keyword: string;
  onKeywordChange: (value: string) => void;
  selectedIds: number[];
  items: PMWorkItemSearchApi[];
  onToggleSelected: (workItemId: number) => void;
  onSelectVisible: () => void;
  onClearSelected: () => void;
};
```

The picker should preserve the existing selection when the search filter changes, and it should keep the row density high enough for scanning.

- [ ] **Step 3: Extract run settings into an intent panel**

Move objective, change scope, algorithm key, and planning dates into `PMOptimizationRunIntentPanel`.

```tsx
type PMOptimizationRunIntentPanelProps = {
  objective: PMOptimizationObjective;
  changeScope: PMOptimizationChangeScope;
  algorithmKey: string;
  planningStart: string;
  planningEnd: string;
  onObjectiveChange: (value: PMOptimizationObjective) => void;
  onChangeScopeChange: (value: PMOptimizationChangeScope) => void;
  onAlgorithmKeyChange: (value: string) => void;
  onPlanningStartChange: (value: string) => void;
  onPlanningEndChange: (value: string) => void;
};
```

The panel should render:

- objective radio cards
- change scope radio cards or a segmented control
- algorithm dropdown
- date inputs

- [ ] **Step 4: Restructure the page into a two-column launch layout**

Keep the main page as the orchestration layer:

```tsx
<div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]'>
  <PMOptimizationWorkItemPicker
    keyword={keyword}
    onKeywordChange={setKeyword}
    selectedIds={selectedIds}
    items={searchQuery.data?.data.items || []}
    onToggleSelected={toggleSelected}
    onSelectVisible={selectAllVisible}
    onClearSelected={clearSelected}
  />
  <div className='space-y-5'>
    <PMOptimizationRunIntentPanel
      objective={objective}
      changeScope={changeScope}
      algorithmKey={algorithmKey}
      planningStart={dateState.planningStart}
      planningEnd={dateState.planningEnd}
      onObjectiveChange={setObjective}
      onChangeScopeChange={setChangeScope}
      onAlgorithmKeyChange={setAlgorithmKey}
      onPlanningStartChange={(value) =>
        setDateState((current) => ({ ...current, planningStart: value }))
      }
      onPlanningEndChange={(value) =>
        setDateState((current) => ({ ...current, planningEnd: value }))
      }
    />
    <div className='rounded-md border p-4'>
      <p className='text-sm font-medium'>Selected work items</p>
      <p className='text-sm text-muted-foreground'>
        {selectedIds.length} selected
      </p>
    </div>
  </div>
</div>
```

The launch page should remain one screen, not a wizard, with a clear primary `Generate run` action in the header.

- [ ] **Step 5: Add launch validation and backend error surfacing**

Keep these checks on the client before submit:

- at least one work item selected
- valid planning range
- chosen algorithm key non-empty

Show field-level errors near the relevant control and surface submit failures via toast.

- [ ] **Step 6: Run frontend verification for the launch flow**

Run:

```bash
npm run type-check
npm run lint
```

Expected: the launch page compiles against the new contract and no legacy mode/flag references remain in this flow.

- [ ] **Step 7: Commit the launch page redesign**

```bash
git add serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationWorkItemPicker.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunIntentPanel.tsx
git commit -m "feat(pm): redesign optimization launch flow"
```

---

### Task 3: Rebuild the review page into a decision-oriented working surface

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverview.tsx`
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx`
- Create: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx`

- [ ] **Step 1: Replace legacy metadata fields with clean run metadata**

Rewrite the top of the review page so it reads from:

```tsx
run.objective
run.changeScope
run.algorithmKey
run.algorithmVersion
run.solverStatus
run.objectiveScore
```

and no longer reads:

```tsx
run.mode
run.allowReassignment
run.allowScheduleChanges
```

The header should show run id, status, objective, change scope, and algorithm key.

- [ ] **Step 2: Extract the overview block into its own component**

Move the metrics strip and metadata panel into `PMOptimizationRunOverview`.

```tsx
type PMOptimizationRunOverviewProps = {
  run: PMOptimizationRunApi;
  summary: PMOptimizationRunSummaryApi | null | undefined;
};
```

This component should render:

- scope size
- assignment suggestion count
- scheduled item count
- warnings count
- confidence
- objective score
- run metadata fields

- [ ] **Step 3: Extract assignment/schedule rows into a reusable item table**

Create one table component that can render either channel by mode:

```tsx
type PMOptimizationRunItemTableProps = {
  title: string;
  mode: 'assignment' | 'schedule';
  items: PMOptimizationRunItemApi[];
  selectedIds: number[];
  onToggleApply: (workItemId: number) => void;
  onAccept: (item: PMOptimizationRunItemApi) => void;
  onReject: (item: PMOptimizationRunItemApi) => void;
  onOverride: (item: PMOptimizationRunItemApi) => void;
};
```

The table should keep the row layout dense and show:

- checkbox
- key
- decision badge
- current vs suggested values
- score/cost/confidence
- reasons
- violations
- action buttons

- [ ] **Step 4: Extract override editing into a dedicated dialog**

Move override editing out of the page body and into `PMOptimizationRunOverrideDialog`.

```tsx
type PMOptimizationRunOverrideDialogProps = {
  open: boolean;
  item: PMOptimizationRunItemApi | null;
  users: { id: number; label: string }[];
  assignmentDecision: PMOptimizationDecision;
  scheduleDecision: PMOptimizationDecision;
  overrideAssigneeId: string;
  overridePlannedStart: string;
  overridePlannedEnd: string;
  onAssignmentDecisionChange: (value: PMOptimizationDecision) => void;
  onScheduleDecisionChange: (value: PMOptimizationDecision) => void;
  onOverrideAssigneeIdChange: (value: string) => void;
  onOverridePlannedStartChange: (value: string) => void;
  onOverridePlannedEndChange: (value: string) => void;
  onSave: () => void;
  onClose: () => void;
};
```

The dialog should only surface fields relevant to the selected item and decision state.

- [ ] **Step 5: Rework the page into a header + tabs + action flow**

Keep the page orchestration focused on:

- loading the run
- tracking selected apply ids
- opening the override dialog
- dispatching apply/discard/update mutations
- choosing the active tab

Tabs should remain:

- Summary
- Assignment
- Schedule
- Risks
- History

- [ ] **Step 6: Make apply and discard behavior explicit and scoped**

Keep selected apply ids persistent across refetches.

Apply should:

- default to all items selected on load
- apply only the selected rows
- refresh the run after completion
- keep the user on the same review page

Discard should:

- confirm intent before execution if needed by the existing shared UI pattern
- refresh the run after completion

- [ ] **Step 7: Run verification for the review flow**

Run:

```bash
npm run type-check
npm run lint
```

Expected: the review page compiles cleanly against the new review metadata and the new extracted components.

- [ ] **Step 8: Commit the review page redesign**

```bash
git add serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverview.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx
git commit -m "feat(pm): redesign optimization review flow"
```

---

### Task 4: Remove leftover legacy references and run the final frontend gate

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
- Modify: `serp_web/src/modules/pm/api/index.ts`
- Modify: any optimization UI files still referencing legacy mode/flags after Tasks 2 and 3

- [ ] **Step 1: Search the PM optimization frontend for legacy contract references**

Run:

```bash
rg -n "PMOptimizationMode|allowReassignment|allowScheduleChanges|mode:" serp_web/src/modules/pm/api serp_web/src/modules/pm/pages serp_web/src/modules/pm/components serp_web/src/modules/pm/types -S
```

Expected: no matches in optimization contract code, launch code, or review code. Any hits must be unrelated generic UI state and should stay untouched.

- [ ] **Step 2: Fix any remaining copy or label drift**

Check for stale text such as:

- `Mode`
- `Allow reassignment`
- `Allow schedule changes`
- `Balanced workload` being used where an objective label should be shown

Replace those with the new `objective` / `changeScope` vocabulary and keep the copy short.

- [ ] **Step 3: Run the final frontend verification gate**

Run:

```bash
npm run type-check
npm run lint
npm run format:check
npm run build
```

Expected: all commands pass. `build` is the final guard for routing, layout, and production bundling behavior.

- [ ] **Step 4: Commit the finished frontend redesign**

```bash
git add serp_web/src/modules/pm
git commit -m "feat(pm): complete optimization frontend redesign"
```

## Coverage Check

This plan covers every requirement from the spec:

- shared clean input contract: Task 1
- launch page layout and intent flow: Task 2
- review page summary, metadata, tabs, item tables, override, apply/discard: Task 3
- removal of legacy references and final verification: Task 4
