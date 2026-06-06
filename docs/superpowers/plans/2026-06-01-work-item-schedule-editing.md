# Work Item Schedule Editing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users edit a work item's manual schedule from the project calendar while keeping the plan and allocations consistent.

**Architecture:** Add a plan-level schedule update command in `pm_core` that validates and replaces the active work item plan in one transaction. Expose it through a thin work-item REST endpoint, then wire `serp_web` to edit the selected calendar allocation and submit the complete plan state.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind/Shadcn primitives.

---

## File Structure

- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanAllocationCommand.java`
  - Immutable request record for one schedule allocation.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommand.java`
  - Immutable command for the application handler.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanResult.java`
  - API-facing result for the saved plan and allocations.
- Create `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommandHandler.java`
  - Transactional orchestration, authorization, validation, persistence.
- Create `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/dto/request/UpdateWorkItemPlanRequest.java`
  - REST request DTO with Jakarta validation.
- Modify `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemController.java`
  - Add `PUT /{workItemId}/schedule`.
- Create `pm_core/src/test/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommandHandlerTest.java`
  - Focused unit tests for the command.
- Modify or create a controller test under `pm_core/src/test/java/serp/project/pmcore/ui/rest/workitem/`.
- Modify `serp_web/src/modules/pm/types/work-item-api.types.ts`
  - Add request/response interfaces and expose locked/plan fields needed by calendar items.
- Modify `serp_web/src/modules/pm/api/workItemApi.ts`
  - Add update mutation and export hook.
- Modify `serp_web/src/modules/pm/components/projects/calendar/PMProjectScheduleAllocationSheet.tsx`
  - Add edit mode form.
- Modify `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`
  - Wire mutation, derive full plan payload from visible allocations, and refresh calendar.

## Task 1: Backend Command and Handler

**Files:**
- Create: `pm_core/src/test/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommandHandlerTest.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanAllocationCommand.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommand.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanResult.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/workitem/command/schedule/UpdateWorkItemPlanCommandHandler.java`

- [ ] **Step 1: Write failing handler tests**

Create tests that prove:

```java
@Test
void handleShouldSaveManualLockedPlanAndReplaceAllocations()
```

asserts:

- `workItemPlanPort.upsertActivePlan(...)` receives `source = MANUAL`, `sourceRunId = null`, `locked = true`.
- `workItemPlanAllocationPort.replaceForPlan(...)` receives allocations with `source = MANUAL`.
- Result returns saved plan and allocations.

```java
@Test
void handleShouldDefaultLockedToTrue()
```

asserts omitted `locked` becomes `true`.

```java
@Test
void handleShouldRejectInvalidPlanRange()
```

asserts `BusinessRuleViolationException` with `DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID`.

```java
@Test
void handleShouldRejectAllocationOutsidePlanRange()
```

asserts `BusinessRuleViolationException` with `DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID`.

- [ ] **Step 2: Run tests and verify RED**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=UpdateWorkItemPlanCommandHandlerTest test
```

Expected: compile failure or test failure because the command classes do not exist yet.

- [ ] **Step 3: Implement command records and result**

Create records:

```java
public record UpdateWorkItemPlanAllocationCommand(
        Long assigneeId,
        Long start,
        Long end,
        Long effortMillis
) {
}
```

```java
public record UpdateWorkItemPlanCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long workItemId,
        Long plannedStart,
        Long plannedEnd,
        Boolean locked,
        List<UpdateWorkItemPlanAllocationCommand> allocations,
        Set<String> groupKeys
) {
    public UpdateWorkItemPlanCommand {
        allocations = allocations == null ? List.of() : List.copyOf(allocations);
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }
}
```

Result shape:

```java
@Builder
public record UpdateWorkItemPlanResult(
        Long id,
        Long workItemId,
        Long projectId,
        Long plannedStart,
        Long plannedEnd,
        String source,
        Boolean locked,
        List<AllocationView> allocations
) {
    @Builder
    public record AllocationView(
            Long id,
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis
    ) {
    }
}
```

- [ ] **Step 4: Implement minimal handler**

The handler should:

- validate scalar inputs and ranges;
- load project with `projectService.getProjectById(projectId, tenantId)`;
- reject archived projects;
- load work item with `workItemReadPort.getWorkItemById(tenantId, workItemId)` or the nearest existing read method;
- ensure work item belongs to project;
- build actor context through `workItemAuthorizationSupportService.buildActorContext(...)`;
- require `BROWSE_PROJECTS` and `SCHEDULE_ISSUES`;
- check issue security;
- upsert `WorkItemPlanEntity` with manual source;
- replace allocations;
- return `UpdateWorkItemPlanResult`.

- [ ] **Step 5: Run tests and verify GREEN**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=UpdateWorkItemPlanCommandHandlerTest test
```

Expected: tests pass.

## Task 2: Backend REST Endpoint

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/dto/request/UpdateWorkItemPlanRequest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/WorkItemController.java`
- Test: controller test under `pm_core/src/test/java/serp/project/pmcore/ui/rest/workitem/`

- [ ] **Step 1: Write failing controller test**

Add a test that calls:

```java
controller.updateWorkItemSchedule(PROJECT_ID, WORK_ITEM_ID, request)
```

and verifies the command contains:

- `tenantId`
- `userId`
- `projectId`
- `workItemId`
- `plannedStart`
- `plannedEnd`
- `locked`
- one allocation
- groups from `authUtils.getCurrentGroups()`

- [ ] **Step 2: Run test and verify RED**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=WorkItemControllerTest test
```

Expected: compile failure because endpoint/request is missing.

- [ ] **Step 3: Add REST request DTO**

Create request with fields:

```java
@NotNull
@Positive
private Long plannedStart;

@NotNull
@Positive
private Long plannedEnd;

private Boolean locked;

@Valid
private List<AllocationRequest> allocations = List.of();
```

Nested allocation fields:

```java
@NotNull
@Positive
private Long assigneeId;

@NotNull
@Positive
private Long start;

@NotNull
@Positive
private Long end;

@NotNull
@Positive
private Long effortMillis;
```

- [ ] **Step 4: Add controller endpoint**

Add method:

```java
@PutMapping("/{workItemId}/schedule")
public ResponseEntity<GeneralResponse<UpdateWorkItemPlanResult>> updateWorkItemSchedule(
        @PathVariable Long projectId,
        @PathVariable Long workItemId,
        @Valid @RequestBody UpdateWorkItemPlanRequest request
)
```

It should resolve auth, map request allocations to command allocations, delegate, and wrap with `responseUtils.success(result)`.

- [ ] **Step 5: Run controller test and handler test**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=WorkItemControllerTest,UpdateWorkItemPlanCommandHandlerTest test
```

Expected: tests pass.

## Task 3: Frontend API Types and Mutation

**Files:**
- Modify: `serp_web/src/modules/pm/types/work-item-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/workItemApi.ts`

- [ ] **Step 1: Add TypeScript contracts**

Add:

```ts
export interface PMUpdateWorkItemScheduleAllocationRequest {
  assigneeId: number;
  start: number;
  end: number;
  effortMillis: number;
}

export interface PMUpdateWorkItemScheduleRequest {
  plannedStart: number;
  plannedEnd: number;
  locked?: boolean | null;
  allocations: PMUpdateWorkItemScheduleAllocationRequest[];
}

export interface PMUpdateWorkItemScheduleAllocationResponse {
  id?: number | null;
  assigneeId?: number | null;
  start?: number | null;
  end?: number | null;
  effortMillis?: number | null;
}

export interface PMUpdateWorkItemScheduleResponse {
  id: number;
  workItemId: number;
  projectId: number;
  plannedStart?: number | null;
  plannedEnd?: number | null;
  source?: string | null;
  locked?: boolean | null;
  allocations: PMUpdateWorkItemScheduleAllocationResponse[];
}
```

Also add optional `locked?: boolean | null`, `plannedStart?: number | null`, and `plannedEnd?: number | null` to `PMWorkItemScheduleAllocationCalendarItemApi` if the backend query is extended to return those fields.

- [ ] **Step 2: Add RTK Query mutation**

Add:

```ts
updatePmWorkItemSchedule: builder.mutation<
  PMUpdateWorkItemScheduleResponse,
  {
    projectId: number;
    workItemId: number;
    body: PMUpdateWorkItemScheduleRequest;
  }
>({
  query: ({ projectId, workItemId, body }) => ({
    url: `/projects/${projectId}/work-items/${workItemId}/schedule`,
    method: 'PUT',
    body,
  }),
  extraOptions: { service: 'pm' },
  transformResponse: createDataTransform<PMUpdateWorkItemScheduleResponse>(),
  invalidatesTags: (_result, _error, { workItemId }) => [
    { type: 'pm/WorkItem', id: workItemId },
    { type: 'pm/WorkItem', id: 'LIST' },
  ],
})
```

Export `useUpdatePmWorkItemScheduleMutation`.

- [ ] **Step 3: Run frontend type check**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: no new type errors from the API addition.

## Task 4: Calendar Sheet Edit UI

**Files:**
- Modify: `serp_web/src/modules/pm/components/projects/calendar/PMProjectScheduleAllocationSheet.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectCalendarPage.tsx`

- [ ] **Step 1: Add sheet props for saving**

Add props:

```ts
isSaving?: boolean;
onSaveSchedule?: (input: {
  allocationId: number;
  start: number;
  end: number;
  effortMillis: number;
  assigneeId: number;
  locked: boolean;
}) => Promise<void> | void;
```

- [ ] **Step 2: Add local edit state**

When `allocation` changes, initialize local values from:

- `allocation.start`
- `allocation.end`
- `allocation.effortMillis`
- `allocation.assigneeId`
- `true` for locked by default

Use `datetime-local` inputs with helper conversion through the Vietnam timezone helpers already in `pmProjectCalendar.utils.ts` or local moment conversion.

- [ ] **Step 3: Add validation before save**

Reject in UI before mutation when:

- start or end is missing;
- start >= end;
- assignee is missing;
- effort <= 0.

Show a compact inline error in the sheet.

- [ ] **Step 4: Wire mutation in page**

In `PMProjectCalendarPage.tsx`:

- import `toast`, `getErrorMessage`, and `useUpdatePmWorkItemScheduleMutation`;
- create `handleSaveSchedule`;
- gather all `scheduleItems` with same `workItemId`;
- replace the selected allocation values;
- derive `plannedStart = min(allocation.start)`;
- derive `plannedEnd = max(allocation.end)`;
- submit body;
- call `scheduleQuery.refetch()` after success;
- update `selectedAllocation` from the edited response or clear it if necessary.

- [ ] **Step 5: Run frontend checks**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
npm run format:check
```

Expected: commands pass or report only pre-existing unrelated issues.

## Task 5: Backend Verification

**Files:**
- All backend files touched in previous tasks.

- [ ] **Step 1: Run focused backend tests**

Run:

```powershell
cd pm_core
.\mvnw.cmd -Dtest=UpdateWorkItemPlanCommandHandlerTest,WorkItemControllerTest test
```

Expected: tests pass.

- [ ] **Step 2: Run compile**

Run:

```powershell
cd pm_core
.\mvnw.cmd clean compile
```

Expected: compile succeeds.

## Task 6: Final Review

**Files:**
- Review `git diff`.

- [ ] **Step 1: Check changed files**

Run:

```powershell
git diff --stat
git diff --check
```

Expected: only schedule editing files and no whitespace errors.

- [ ] **Step 2: Summarize verification**

Report:

- backend focused tests command and result;
- backend compile command and result;
- frontend lint/type/format command results;
- any checks that could not be run.
