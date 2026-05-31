# PM Optimization Batch Decision Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace single-item optimization review updates with a batch decision API and update the review page so users can accept, reject, or override many items without repetitive one-by-one requests.

**Architecture:** Keep review state on optimization run items and move all review writes through one batch command/endpoint. The backend remains the source of truth for validation and persisted warnings, while the frontend only manages selection, mode-specific bulk actions, and refresh after each successful batch update. Remove the live dependency on the single-work-item update route so the contract is simpler and the UI can reuse the same path for one item or many.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript strict mode, Redux Toolkit Query, Shadcn/Radix UI.

---

### Task 1: Add Batch Decision Request And Command Coverage

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/BatchUpdateOptimizationRunItemDecisionsRequest.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommand.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest.java`

- [ ] **Step 1: Define the request shape**

Add a request DTO with one nested item payload list:

```java
@Getter
@Setter
@NoArgsConstructor
public class BatchUpdateOptimizationRunItemDecisionsRequest {
    @NotEmpty
    private List<ItemDecisionRequest> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDecisionRequest {
        @NotNull
        private Long workItemId;
        private OptimizationDecision assignmentDecision;
        private OptimizationDecision scheduleDecision;
        private Long overrideAssigneeId;
        private Long overridePlannedStart;
        private Long overridePlannedEnd;
    }
}
```

Keep the DTO aligned with the current single-item request fields so the UI can send one row or many rows
through the same contract.

- [ ] **Step 2: Add a batch command record**

Create a command that carries tenant, user, project, run, and the list of item decision payloads. Keep
the payload immutable by copying the incoming list in the record constructor.

Example shape:

```java
public record BatchUpdateOptimizationRunItemDecisionsCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long runId,
        List<ItemDecision> items
) implements ICommand<OptimizationRunReviewView> {
    public record ItemDecision(
            Long workItemId,
            OptimizationDecision assignmentDecision,
            OptimizationDecision scheduleDecision,
            Long overrideAssigneeId,
            Long overridePlannedStart,
            Long overridePlannedEnd
    ) {}
}
```

- [ ] **Step 3: Write focused handler tests first**

Cover these cases in `BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest`:

```java
@Test
void handleShouldPersistMultipleAcceptedDecisions() { ... }

@Test
void handleShouldRejectDuplicateWorkItemIds() { ... }

@Test
void handleShouldRejectInvalidOverridePayload() { ... }
```

Use the current optimization run review fixtures as the base so the tests prove the new batch flow still
returns the refreshable run view.

- [ ] **Step 4: Run the focused test class**

Run:

```bash
./mvnw.cmd -Dtest=BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest test
```

Expected: the new test class fails at first because the handler does not exist yet.

---

### Task 2: Implement The Batch Handler And Remove The Single-Item Handler Path

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandler.java`
- Remove: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommand.java`
- Remove: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandler.java`
- Remove: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandlerTest.java`

- [ ] **Step 1: Move the existing validation rules into batch form**

Reuse the current validation rules from the single-item handler, but apply them inside a loop over batch
items. Validate the batch request itself first:

```java
if (command.items() == null || command.items().isEmpty()) {
    throw new IllegalArgumentException("items is required");
}
List<Long> workItemIds = command.items().stream()
        .map(BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision::workItemId)
        .toList();
if (new LinkedHashSet<>(workItemIds).size() != workItemIds.size()) {
    throw new IllegalArgumentException("workItemIds must not contain duplicates");
}
```

Then validate each item exactly as the current single-item handler does for assignment and schedule
decisions.

- [ ] **Step 2: Keep the response contract unchanged**

Return `OptimizationRunReviewView` from the batch handler so the existing `GetOptimizationRun` response
shape can keep driving the page refresh path.

- [ ] **Step 3: Preserve audit warnings**

When a batch item fails override validation, keep the same warning persistence behavior the current
single-item handler uses, then abort the batch with the same exception style the REST layer already maps.

- [ ] **Step 4: Make the handler atomic**

Ensure the batch command runs in a single transaction so either the whole batch review persists or none
of it does.

- [ ] **Step 5: Run the focused handler test**

Run:

```bash
./mvnw.cmd -Dtest=BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest test
```

Expected: the new batch handler test passes.

---

### Task 3: Wire The Batch Endpoint In The REST Controller

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
- Remove: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/UpdateOptimizationRunItemDecisionRequest.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunControllerTest.java`

- [ ] **Step 1: Add the batch endpoint**

Add:

```java
@PatchMapping("/{runId}/items/decisions")
public ResponseEntity<GeneralResponse<OptimizationRunReviewView>> updateOptimizationRunItemDecisions(...)
```

Map each request item into the batch command payload and invoke the new handler.

- [ ] **Step 2: Remove the single-item endpoint**

Delete the `PATCH /{runId}/items/{workItemId}` method and its request DTO once the batch endpoint is wired.

- [ ] **Step 3: Add a controller test**

Add a focused controller test at `pm_core/src/test/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunControllerTest.java`
that proves the new endpoint delegates to the batch command and returns the standard wrapped response.

- [ ] **Step 4: Run compile for the REST layer**

Run:

```bash
./mvnw.cmd clean compile
```

Expected: the controller compiles after the route and DTO switch.

---

### Task 4: Switch The Frontend API Contract To Batch Decisions

**Files:**
- Modify: `serp_web/src/modules/pm/types/optimization.types.ts`
- Modify: `serp_web/src/modules/pm/api/optimizationApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`

- [ ] **Step 1: Replace the single-item request type**

Remove `PMUpdateOptimizationRunItemDecisionRequest` and add a batch request type:

```ts
export interface PMOptimizationRunDecisionItemRequest {
  workItemId: number;
  assignmentDecision?: PMOptimizationDecision | null;
  scheduleDecision?: PMOptimizationDecision | null;
  overrideAssigneeId?: number | null;
  overridePlannedStart?: number | null;
  overridePlannedEnd?: number | null;
}

export interface PMBatchUpdateOptimizationRunItemDecisionsRequest {
  items: PMOptimizationRunDecisionItemRequest[];
}
```

- [ ] **Step 2: Replace the RTK Query mutation**

Replace `updatePmOptimizationRunItemDecision` with `updatePmOptimizationRunItemDecisions` that calls:

```ts
url: `/projects/${projectId}/optimization-runs/${runId}/items/decisions`
method: 'PATCH'
```

Keep `extraOptions: { service: 'pm' }`, `createDataTransform`, and the same run tag invalidation.

- [ ] **Step 3: Export the new contract from the module barrel**

Update `api/index.ts` so the page and component imports continue to resolve from the module boundary.

- [ ] **Step 4: Run type-check**

Run:

```bash
npm run type-check
```

Expected: the frontend compiles once the old request type and hook are removed.

---

### Task 5: Update The Optimization Run Review Page For Bulk Actions

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx`

- [ ] **Step 1: Replace single-item decision calls with a batch helper**

Add one local helper in the page that accepts an array of item payloads and forwards it to the batch
mutation. Use it for:

```tsx
onAccept={(item) => ...}
onReject={(item) => ...}
onOverride={(item) => ...}
```

For row actions, send a one-item batch. For bulk actions, send one payload per selected row.

- [ ] **Step 2: Add explicit bulk buttons for the active tab**

Add `Accept selected`, `Reject selected`, and `Clear selection` near the existing `Apply selected` /
`Discard` actions. Keep the active tab deciding whether the batch updates assignment or schedule
decisions.

- [ ] **Step 3: Keep override modal working**

Keep the existing override dialog UX, but change its save path to the batch endpoint with a one-item
payload so the single-item API is no longer needed.

- [ ] **Step 4: Keep selected ids in sync after refresh**

After a successful batch update, refetch the run and leave selection in a predictable state. Clearing the
selection after a bulk action is acceptable if it simplifies the UI state.

- [ ] **Step 5: Run frontend verification**

Run:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: the page compiles cleanly with the new batch hook and the removed per-item mutation.

---

### Task 6: Final Verification And Diff Review

**Files:**
- No new production files unless verification finds a missing contract or test gap.

- [ ] **Step 1: Run the focused backend tests**

Run:

```bash
./mvnw.cmd -Dtest=BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest,OptimizationRunControllerTest test
```

Expected: the batch handler and controller tests pass.

- [ ] **Step 2: Run frontend verification again**

Run:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all frontend checks pass after the API and page updates.

- [ ] **Step 3: Inspect the final diff**

Run:

```bash
git diff --stat
git diff --check
```

Expected: the diff only includes the batch review feature, endpoint removal, and the related spec/plan
documents.
