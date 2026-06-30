# PM Optimization Intent and Schedule Override Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PM optimization objective-first and allow schedule overrides to edit and apply detailed allocation chunks.

**Architecture:** Keep the current greedy optimizer and add a small objective-to-algorithm mapper so `objective` drives `algorithmKey` consistently. Persist override allocations separately from generated allocations, derive override planned range from chunks, and have apply choose generated or override chunks based on the schedule decision.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Flyway, Next.js 15, React 19, TypeScript, RTK Query, Tailwind/shadcn UI.

---

## Scope Check

This plan spans backend and frontend, but both parts serve one vertical feature: objective-first optimization runs with reviewable schedule chunk overrides. It does not need separate plans because backend contract and frontend UI must land together to produce a usable workflow.

## File Structure

Backend files:

- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapper.java`
  - Owns the objective-to-greedy-preset mapping used by command defaults and generation.
- Create: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapperTest.java`
  - Covers every objective mapping and null default behavior.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`
  - Defaults `objective` first, then derives `algorithmKey` from it.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
  - Normalizes `algorithmKey` from objective before building run intent and resolving the algorithm.
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`
  - Verifies mismatched request input is normalized.
- Create: `pm_core/src/main/resources/db/migration/V31__add_optimization_run_item_override_allocations.sql`
  - Adds `override_allocation_chunks_json` to keep generated and override chunks separate.
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunItemEntity.java`
  - Adds `overrideAllocationChunksJson`.
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunItemModel.java`
  - Maps the new database column.
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunItemMapper.java`
  - Copies the new JSON field both directions.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunItemView.java`
  - Exposes `overrideAllocationChunks`.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
  - Parses generated and override allocation chunks.
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`
  - Verifies override chunks appear in the API view.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommand.java`
  - Adds override allocation chunks to each item decision.
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/BatchUpdateOptimizationRunItemDecisionsRequest.java`
  - Adds nested request DTO for allocation chunks.
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
  - Maps request chunks into the command.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandler.java`
  - Validates chunks, derives planned range, validates dependencies, validates assignees against project assignable members, and persists override JSON.
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest.java`
  - Covers derived range, invalid chunks, dependency rejection, and project-member validation.
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandler.java`
  - Uses override chunks when schedule decision is `OVERRIDDEN`.
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandlerTest.java`
  - Verifies overridden schedule writes allocations.

Frontend files:

- Modify: `serp_web/src/modules/pm/types/optimization.types.ts`
  - Adds allocation chunk types and request payload fields.
- Modify: `serp_web/src/modules/pm/constants/optimization.ts`
  - Adds the shared objective-to-algorithm mapping helper.
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx`
  - Derives algorithm from objective instead of user-controlled algorithm state.
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunIntentPanel.tsx`
  - Removes the algorithm picker from the normal path and shows derived preset.
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
  - Manages chunk editor state and sends override chunks.
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx`
  - Shows schedule allocation detail.
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx`
  - Turns schedule override into a mini plan editor.

---

### Task 1: Objective-to-Algorithm Mapping

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapper.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapperTest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

- [ ] **Step 1: Write the mapper test**

Create `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapperTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationObjectiveAlgorithmMapperTest {

    @Test
    void algorithmKeyForShouldMapEveryObjective() {
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.BALANCED_WORKLOAD))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.SKILL_FIRST))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.DEADLINE_FIRST))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_DEADLINE_FIRST);
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(OptimizationObjective.MINIMAL_REASSIGNMENT))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_MINIMAL_REASSIGNMENT);
    }

    @Test
    void algorithmKeyForShouldDefaultNullObjectiveToBalanced() {
        assertThat(OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(null))
                .isEqualTo(OptimizationAlgorithmKeys.GREEDY_BALANCED);
    }
}
```

- [ ] **Step 2: Run mapper test to verify it fails**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=OptimizationObjectiveAlgorithmMapperTest test
```

Expected: FAIL because `OptimizationObjectiveAlgorithmMapper` does not exist.

- [ ] **Step 3: Add the mapper**

Create `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapper.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import lombok.experimental.UtilityClass;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

@UtilityClass
public class OptimizationObjectiveAlgorithmMapper {

    public static String algorithmKeyFor(OptimizationObjective objective) {
        if (objective == null) {
            return OptimizationAlgorithmKeys.GREEDY_BALANCED;
        }
        return switch (objective) {
            case BALANCED_WORKLOAD -> OptimizationAlgorithmKeys.GREEDY_BALANCED;
            case SKILL_FIRST -> OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST;
            case DEADLINE_FIRST -> OptimizationAlgorithmKeys.GREEDY_DEADLINE_FIRST;
            case MINIMAL_REASSIGNMENT -> OptimizationAlgorithmKeys.GREEDY_MINIMAL_REASSIGNMENT;
        };
    }
}
```

- [ ] **Step 4: Run mapper test to verify it passes**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=OptimizationObjectiveAlgorithmMapperTest test
```

Expected: PASS.

- [ ] **Step 5: Make generate command objective-first**

Modify the compact constructor in `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`:

```java
    public GenerateOptimizationRunCommand {
        selectedWorkItemIds = selectedWorkItemIds == null ? List.of() : List.copyOf(selectedWorkItemIds);
        objective = objective == null ? OptimizationObjective.BALANCED_WORKLOAD : objective;
        algorithmKey = OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(objective);
        changeScope = changeScope == null ? OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE : changeScope;
    }
```

Add this import:

```java
import serp.project.pmcore.domain.optimization.service.OptimizationObjectiveAlgorithmMapper;
```

Remove the unused import:

```java
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
```

- [ ] **Step 6: Normalize in the handler**

Modify `GenerateOptimizationRunCommandHandler.normalizeAlgorithmKey` to receive the objective:

```java
    private String normalizeAlgorithmKey(OptimizationObjective objective) {
        return OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(objective);
    }
```

Add these imports:

```java
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.service.OptimizationObjectiveAlgorithmMapper;
```

Remove this import when no longer used:

```java
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
```

Change the call in `handle`:

```java
        String algorithmKey = normalizeAlgorithmKey(command.objective());
```

- [ ] **Step 7: Add generate normalization assertion**

In `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`, add a test near the existing handler tests:

```java
    @Test
    void handleShouldNormalizeAlgorithmKeyFromObjective() {
        GenerateOptimizationRunCommand command = new GenerateOptimizationRunCommand(
                1L,
                10L,
                2L,
                "SELECTED_WORK_ITEMS",
                OptimizationAlgorithmKeys.GREEDY_BALANCED,
                OptimizationObjective.SKILL_FIRST,
                OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                1_000L,
                10_000L,
                List.of(100L)
        );

        assertThat(command.algorithmKey()).isEqualTo(OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST);
    }
```

- [ ] **Step 8: Run focused generate tests**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=OptimizationObjectiveAlgorithmMapperTest,GenerateOptimizationRunCommandHandlerTest test
```

Expected: PASS.

- [ ] **Step 9: Commit backend mapping**

Run from repo root:

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapper.java pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationObjectiveAlgorithmMapperTest.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): derive optimization algorithm from objective"
```

---

### Task 2: Persist Override Allocation Chunks Separately

**Files:**
- Create: `pm_core/src/main/resources/db/migration/V31__add_optimization_run_item_override_allocations.sql`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunItemEntity.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunItemModel.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunItemMapper.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunItemView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`

- [ ] **Step 1: Write assembler test for override chunks**

In `OptimizationRunReviewAssemblerTest`, add a test that builds an `OptimizationRunItemEntity` with both generated and override chunk JSON and asserts both are exposed:

```java
    @Test
    void toViewShouldExposeOverrideAllocationChunks() {
        OptimizationRunReviewAssembler assembler = new OptimizationRunReviewAssembler(jsonUtils);
        OptimizationRunEntity run = OptimizationRunEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .summaryJson(null)
                .build();
        OptimizationRunItemEntity item = OptimizationRunItemEntity.builder()
                .id(100L)
                .tenantId(1L)
                .runId(20L)
                .projectId(10L)
                .workItemId(30L)
                .allocationChunksJson("[{\"assigneeId\":100,\"start\":1714876800000,\"end\":1714880400000,\"effortMillis\":3600000}]")
                .overrideAllocationChunksJson("[{\"assigneeId\":101,\"start\":1714880400000,\"end\":1714884000000,\"effortMillis\":3600000}]")
                .build();
        when(jsonUtils.fromJson(eq(item.getAllocationChunksJson()), any(TypeReference.class)))
                .thenReturn(List.of(new OptimizationScheduleAllocation(
                        100L,
                        1_714_876_800_000L,
                        1_714_880_400_000L,
                        3_600_000L
                )));
        when(jsonUtils.fromJson(eq(item.getOverrideAllocationChunksJson()), any(TypeReference.class)))
                .thenReturn(List.of(new OptimizationScheduleAllocation(
                        101L,
                        1_714_880_400_000L,
                        1_714_884_000_000L,
                        3_600_000L
                )));

        OptimizationRunReviewView view = assembler.toView(run, List.of(item), List.of());

        assertEquals(1, view.getItems().getFirst().getAllocationChunks().size());
        assertEquals(100L, view.getItems().getFirst().getAllocationChunks().getFirst().getAssigneeId());
        assertEquals(1, view.getItems().getFirst().getOverrideAllocationChunks().size());
        assertEquals(101L, view.getItems().getFirst().getOverrideAllocationChunks().getFirst().getAssigneeId());
    }
```

Add imports if missing:

```java
import com.fasterxml.jackson.core.type.TypeReference;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
```

- [ ] **Step 2: Run assembler test to verify it fails**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=OptimizationRunReviewAssemblerTest test
```

Expected: FAIL because `overrideAllocationChunksJson` and `overrideAllocationChunks` do not exist.

- [ ] **Step 3: Add migration**

Create `pm_core/src/main/resources/db/migration/V31__add_optimization_run_item_override_allocations.sql`:

```sql
ALTER TABLE optimization_run_items
    ADD COLUMN override_allocation_chunks_json TEXT NULL;
```

- [ ] **Step 4: Add entity and model fields**

In `OptimizationRunItemEntity`, add after `allocationChunksJson`:

```java
    private String overrideAllocationChunksJson;
```

In `OptimizationRunItemModel`, add after `allocationChunksJson`:

```java
    @Column(name = "override_allocation_chunks_json", columnDefinition = "TEXT")
    private String overrideAllocationChunksJson;
```

- [ ] **Step 5: Map the new field**

In `OptimizationRunItemMapper.toModel`, replace the JSON mapping chain with:

```java
                .violationsJson(e.getViolationsJson()).allocationChunksJson(e.getAllocationChunksJson())
                .overrideAllocationChunksJson(e.getOverrideAllocationChunksJson())
```

In `OptimizationRunItemMapper.toEntity`, replace the JSON mapping chain with:

```java
                .violationsJson(m.getViolationsJson()).allocationChunksJson(m.getAllocationChunksJson())
                .overrideAllocationChunksJson(m.getOverrideAllocationChunksJson())
```

- [ ] **Step 6: Expose override chunks in review view**

In `OptimizationRunItemView`, add after `allocationChunks`:

```java
    private List<OptimizationScheduleAllocationView> overrideAllocationChunks;
```

In `OptimizationRunReviewAssembler.toItemView`, add after `.allocationChunks(parseAllocationChunks(item.getAllocationChunksJson()))`:

```java
                .overrideAllocationChunks(parseAllocationChunks(item.getOverrideAllocationChunksJson()))
```

- [ ] **Step 7: Run assembler test**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=OptimizationRunReviewAssemblerTest test
```

Expected: PASS.

- [ ] **Step 8: Compile pm_core**

Run from `pm_core/`:

```bash
./mvnw.cmd clean compile
```

Expected: PASS.

- [ ] **Step 9: Commit persistence and view contract**

Run from repo root:

```bash
git add pm_core/src/main/resources/db/migration/V31__add_optimization_run_item_override_allocations.sql pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunItemEntity.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunItemModel.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunItemMapper.java pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunItemView.java pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java
git commit -m "feat(pm): expose optimization override allocation chunks"
```

---

### Task 3: Update Decision Contract and Validation

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommand.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/BatchUpdateOptimizationRunItemDecisionsRequest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandler.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest.java`

- [ ] **Step 1: Add failing tests for override chunks**

In `BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest`, add mocks:

```java
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectMemberService projectMemberService;
    @Mock
    private JsonUtils jsonUtils;
```

Update `setUp` constructor call to pass the new dependencies after `optimizationRunWarningAuditService`:

```java
                optimizationRunWarningAuditService,
                projectService,
                projectMemberService,
                jsonUtils
```

Add these imports:

```java
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectMemberService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.kernel.utils.JsonUtils;
```

Add this helper:

```java
    private BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride allocation(
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis) {
        return new BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride(
                assigneeId,
                start,
                end,
                effortMillis
        );
    }
```

Add this helper:

```java
    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
    }
```

Add this test:

```java
    @Test
    void handleShouldDeriveOverrideRangeFromAllocationChunks() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem(WORK_ITEM_ID);
        OptimizationRunReviewView view = OptimizationRunReviewView.builder().id(RUN_ID).build();
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project());
        when(projectMemberService.listAssignableMembers(any())).thenReturn(List.of(100L));
        when(jsonUtils.toJson(any())).thenReturn("[{\"assigneeId\":100,\"start\":1714963200000,\"end\":1714966800000,\"effortMillis\":3600000}]");
        when(optimizationRunWarningPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of());
        when(optimizationRunReviewAssembler.toView(any(), any(), any())).thenReturn(view);

        OptimizationRunReviewView result = handler.handle(new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        WORK_ITEM_ID,
                        null,
                        OptimizationDecision.OVERRIDDEN,
                        null,
                        null,
                        null,
                        List.of(allocation(100L, 1_714_963_200_000L, 1_714_966_800_000L, 3_600_000L))
                ))
        ));

        assertEquals(RUN_ID, result.getId());
        assertEquals(OptimizationDecision.OVERRIDDEN, item.getScheduleDecision());
        assertEquals(1_714_963_200_000L, item.getOverridePlannedStart());
        assertEquals(1_714_966_800_000L, item.getOverridePlannedEnd());
        assertEquals("[{\"assigneeId\":100,\"start\":1714963200000,\"end\":1714966800000,\"effortMillis\":3600000}]",
                item.getOverrideAllocationChunksJson());
    }
```

Add this test:

```java
    @Test
    void handleShouldRejectEmptyOverrideAllocationChunks() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem(WORK_ITEM_ID);
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));

        BatchUpdateOptimizationRunItemDecisionsCommand command = new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        WORK_ITEM_ID,
                        null,
                        OptimizationDecision.OVERRIDDEN,
                        null,
                        null,
                        null,
                        List.of()
                ))
        );

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(optimizationRunWarningAuditService).recordInvalidOverrideWarning(
                TENANT_ID,
                USER_ID,
                RUN_ID,
                WORK_ITEM_ID,
                "overrideAllocationChunks is required when scheduleDecision is OVERRIDDEN"
        );
        verify(optimizationRunItemPort, never()).saveAll(any());
    }
```

Add this test:

```java
    @Test
    void handleShouldRejectOverrideAllocationForNonAssignableMember() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem(WORK_ITEM_ID);
        when(optimizationRunGuard.requireRunInProject(TENANT_ID, PROJECT_ID, RUN_ID)).thenReturn(run);
        when(optimizationRunItemPort.listByRunId(TENANT_ID, RUN_ID)).thenReturn(List.of(item));
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project());
        when(projectMemberService.listAssignableMembers(any())).thenReturn(List.of(100L));

        BatchUpdateOptimizationRunItemDecisionsCommand command = new BatchUpdateOptimizationRunItemDecisionsCommand(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                RUN_ID,
                List.of(new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        WORK_ITEM_ID,
                        null,
                        OptimizationDecision.OVERRIDDEN,
                        null,
                        null,
                        null,
                        List.of(allocation(999L, 1_714_963_200_000L, 1_714_966_800_000L, 3_600_000L))
                ))
        );

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(optimizationRunWarningAuditService).recordInvalidOverrideWarning(
                TENANT_ID,
                USER_ID,
                RUN_ID,
                WORK_ITEM_ID,
                "override allocation assigneeId must be an assignable project member"
        );
    }
```

- [ ] **Step 2: Run update tests to verify they fail**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest test
```

Expected: FAIL because command/request fields and handler dependencies do not exist.

- [ ] **Step 3: Extend the command record**

Replace `BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision` with:

```java
    public record ItemDecision(
            Long workItemId,
            OptimizationDecision assignmentDecision,
            OptimizationDecision scheduleDecision,
            Long overrideAssigneeId,
            Long overridePlannedStart,
            Long overridePlannedEnd,
            List<AllocationOverride> overrideAllocationChunks
    ) {
        public ItemDecision {
            overrideAllocationChunks = overrideAllocationChunks == null
                    ? List.of()
                    : List.copyOf(overrideAllocationChunks);
        }
    }

    public record AllocationOverride(
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis
    ) {
    }
```

- [ ] **Step 4: Extend the REST request**

In `BatchUpdateOptimizationRunItemDecisionsRequest.ItemDecisionRequest`, add:

```java
        @Valid
        private List<AllocationRequest> overrideAllocationChunks = List.of();
```

Add the nested request class inside `BatchUpdateOptimizationRunItemDecisionsRequest`:

```java
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AllocationRequest {
        @NotNull
        private Long assigneeId;
        @NotNull
        private Long start;
        @NotNull
        private Long end;
        @NotNull
        private Long effortMillis;

        public BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride toCommand() {
            return new BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride(
                    assigneeId,
                    start,
                    end,
                    effortMillis
            );
        }
    }
```

Add this import:

```java
import serp.project.pmcore.application.optimization.command.update.BatchUpdateOptimizationRunItemDecisionsCommand;
```

- [ ] **Step 5: Map request chunks in controller**

In `OptimizationRunController.updateOptimizationRunItemDecisions`, replace the item mapping with:

```java
        List<BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> items = request.getItems().stream()
                .map(item -> new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
                        item.getWorkItemId(),
                        item.getAssignmentDecision(),
                        item.getScheduleDecision(),
                        item.getOverrideAssigneeId(),
                        item.getOverridePlannedStart(),
                        item.getOverridePlannedEnd(),
                        item.getOverrideAllocationChunks() == null
                                ? List.of()
                                : item.getOverrideAllocationChunks().stream()
                                        .map(BatchUpdateOptimizationRunItemDecisionsRequest.AllocationRequest::toCommand)
                                        .toList()
                ))
                .toList();
```

- [ ] **Step 6: Add handler dependencies**

In `BatchUpdateOptimizationRunItemDecisionsCommandHandler`, add fields:

```java
    private final IProjectService projectService;
    private final IProjectMemberService projectMemberService;
    private final JsonUtils jsonUtils;
```

Add imports:

```java
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectMemberService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.kernel.utils.JsonUtils;
```

- [ ] **Step 7: Validate chunks and derive range**

Add these helper methods to `BatchUpdateOptimizationRunItemDecisionsCommandHandler`:

```java
    private List<OptimizationScheduleAllocation> buildOverrideAllocations(
            BatchUpdateOptimizationRunItemDecisionsCommand command,
            BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
            Set<Long> assignableMemberIds) {
        if (decision.overrideAllocationChunks().isEmpty()) {
            rejectInvalidOverride(command, decision.workItemId(),
                    "overrideAllocationChunks is required when scheduleDecision is OVERRIDDEN");
        }
        return decision.overrideAllocationChunks().stream()
                .map(allocation -> toScheduleAllocation(command, decision, allocation, assignableMemberIds))
                .toList();
    }

    private OptimizationScheduleAllocation toScheduleAllocation(
            BatchUpdateOptimizationRunItemDecisionsCommand command,
            BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
            BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride allocation,
            Set<Long> assignableMemberIds) {
        if (allocation == null
                || allocation.assigneeId() == null
                || allocation.start() == null
                || allocation.end() == null
                || allocation.effortMillis() == null
                || allocation.assigneeId() <= 0
                || allocation.start() <= 0
                || allocation.end() <= 0
                || allocation.effortMillis() <= 0) {
            rejectInvalidOverride(command, decision.workItemId(),
                    "override allocation fields must be positive");
        }
        if (allocation.start() >= allocation.end()) {
            rejectInvalidOverride(command, decision.workItemId(),
                    "override allocation start must be before end");
        }
        if (!assignableMemberIds.contains(allocation.assigneeId())) {
            rejectInvalidOverride(command, decision.workItemId(),
                    "override allocation assigneeId must be an assignable project member");
        }
        return new OptimizationScheduleAllocation(
                allocation.assigneeId(),
                allocation.start(),
                allocation.end(),
                allocation.effortMillis()
        );
    }

    private Long derivedStart(List<OptimizationScheduleAllocation> allocations) {
        return allocations.stream()
                .map(OptimizationScheduleAllocation::start)
                .min(Long::compareTo)
                .orElse(null);
    }

    private Long derivedEnd(List<OptimizationScheduleAllocation> allocations) {
        return allocations.stream()
                .map(OptimizationScheduleAllocation::end)
                .max(Long::compareTo)
                .orElse(null);
    }
```

- [ ] **Step 8: Load assignable members only when needed**

Add this helper:

```java
    private Set<Long> assignableMemberIds(BatchUpdateOptimizationRunItemDecisionsCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        return Set.copyOf(projectMemberService.listAssignableMembers(project));
    }
```

In `handle`, after `projectModel` assignment, add:

```java
        Set<Long> assignableMemberIds = needsScheduleOverride(command)
                ? assignableMemberIds(command)
                : Set.of();
```

Add helper:

```java
    private boolean needsScheduleOverride(BatchUpdateOptimizationRunItemDecisionsCommand command) {
        return command.items().stream()
                .anyMatch(item -> item.scheduleDecision() == OptimizationDecision.OVERRIDDEN);
    }
```

- [ ] **Step 9: Use derived range in schedule validation and apply**

Change `validateDecisionBasics` signature to include `Set<Long> assignableMemberIds`.

Inside the `scheduleDecision == OVERRIDDEN` block, replace planned start/end validation with:

```java
            List<OptimizationScheduleAllocation> allocations = buildOverrideAllocations(command, decision, assignableMemberIds);
            Long overrideStart = derivedStart(allocations);
            Long overrideEnd = derivedEnd(allocations);
            if (overrideStart == null || overrideEnd == null || overrideStart >= overrideEnd) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "override allocation range is invalid");
            }
            if (overrideStart < run.getPlanningStart() || overrideEnd > run.getPlanningEnd()) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "override allocation range must stay within the optimization planning range");
            }
```

Change dependency checks to use helpers that resolve derived override start/end. Add:

```java
    private Long decisionOverrideStart(BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision) {
        if (decision == null || decision.overrideAllocationChunks().isEmpty()) {
            return decision == null ? null : decision.overridePlannedStart();
        }
        return decision.overrideAllocationChunks().stream()
                .map(BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride::start)
                .min(Long::compareTo)
                .orElse(null);
    }

    private Long decisionOverrideEnd(BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision) {
        if (decision == null || decision.overrideAllocationChunks().isEmpty()) {
            return decision == null ? null : decision.overridePlannedEnd();
        }
        return decision.overrideAllocationChunks().stream()
                .map(BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride::end)
                .max(Long::compareTo)
                .orElse(null);
    }
```

In `effectivePlannedStart`, change the overridden branch to:

```java
            return decision != null && decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN
                    ? decisionOverrideStart(decision)
                    : item.getOverridePlannedStart();
```

In `effectivePlannedEnd`, change the overridden branch to:

```java
            return decision != null && decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN
                    ? decisionOverrideEnd(decision)
                    : item.getOverridePlannedEnd();
```

Change `applyDecision` signature to accept `Set<Long> assignableMemberIds` and pass the already loaded set so project members are not loaded again inside the item loop:

```java
    private void applyDecision(BatchUpdateOptimizationRunItemDecisionsCommand command,
                               BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
                               OptimizationRunItemEntity item,
                               Set<Long> assignableMemberIds,
                               long now)
```

The final overridden branch should be:

```java
                List<OptimizationScheduleAllocation> allocations = buildOverrideAllocations(
                        command,
                        decision,
                        assignableMemberIds
                );
                item.setOverridePlannedStart(derivedStart(allocations));
                item.setOverridePlannedEnd(derivedEnd(allocations));
                item.setOverrideAllocationChunksJson(jsonUtils.toJson(allocations));
```

For non-overridden schedule decisions, clear:

```java
                item.setOverrideAllocationChunksJson(null);
```

- [ ] **Step 10: Update old tests with new constructor parameter**

Every existing test-created `ItemDecision` must add the seventh argument:

```java
List.of()
```

For example:

```java
new BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision(
        WORK_ITEM_ID,
        OptimizationDecision.ACCEPTED,
        OptimizationDecision.REJECTED,
        null,
        null,
        null,
        List.of()
)
```

- [ ] **Step 11: Run focused update tests**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest test
```

Expected: PASS.

- [ ] **Step 12: Commit update contract**

Run from repo root:

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommand.java pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/BatchUpdateOptimizationRunItemDecisionsRequest.java pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandler.java pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/BatchUpdateOptimizationRunItemDecisionsCommandHandlerTest.java
git commit -m "feat(pm): validate optimization schedule override chunks"
```

---

### Task 4: Apply Override Allocation Chunks

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandler.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandlerTest.java`

- [ ] **Step 1: Add failing apply test for overridden chunks**

In `ApplyOptimizationRunCommandHandlerTest`, add:

```java
    @Test
    void handleShouldWriteOverrideAllocationsForOverriddenSchedule() {
        OptimizationRunEntity run = run();
        OptimizationRunItemEntity item = runItem();
        item.setScheduleDecision(OptimizationDecision.OVERRIDDEN);
        item.setOverridePlannedStart(1_714_876_800_000L);
        item.setOverridePlannedEnd(1_714_883_200_000L);
        item.setOverrideAllocationChunksJson("[{\"assigneeId\":200,\"start\":1714876800000,\"end\":1714883200000,\"effortMillis\":6400000}]");
        WorkItemEntity workItem = workItem(SNAPSHOT_UPDATED_AT, 100L);
        ProjectEntity project = project();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("pm"))
                .reporterUserId(101L)
                .assigneeUserId(100L)
                .build();

        stubCommon(run, item, project, workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("pm"), 101L, 100L)).thenReturn(actorContext);
        when(workItemPlanPort.upsertActivePlan(any())).thenReturn(WorkItemPlanEntity.builder()
                .id(701L)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .workItemId(WORK_ITEM_ID)
                .plannedStart(1_714_876_800_000L)
                .plannedEnd(1_714_883_200_000L)
                .source(WorkItemPlanSource.OPTIMIZATION)
                .sourceRunId(RUN_ID)
                .locked(false)
                .build());
        when(jsonUtils.fromJsonToList(eq(item.getOverrideAllocationChunksJson()), eq(OptimizationScheduleAllocation.class)))
                .thenReturn(List.of(new OptimizationScheduleAllocation(
                        200L,
                        1_714_876_800_000L,
                        1_714_883_200_000L,
                        6_400_000L
                )));
        when(optimizationRunReviewAssembler.toView(any(), any(), any()))
                .thenReturn(OptimizationRunReviewView.builder().id(RUN_ID).build());

        handler.handle(new ApplyOptimizationRunCommand(
                TENANT_ID, USER_ID, PROJECT_ID, RUN_ID, false, true, List.of(WORK_ITEM_ID), Set.of("pm")));

        ArgumentCaptor<List<WorkItemPlanAllocationEntity>> allocationCaptor = ArgumentCaptor.forClass(List.class);
        verify(workItemPlanAllocationPort).replaceForPlan(eq(TENANT_ID), eq(701L), allocationCaptor.capture());
        assertEquals(1, allocationCaptor.getValue().size());
        assertEquals(200L, allocationCaptor.getValue().getFirst().getAssigneeId());
        assertEquals(6_400_000L, allocationCaptor.getValue().getFirst().getEffortMillis());
    }
```

Add static import if missing:

```java
import static org.mockito.ArgumentMatchers.eq;
```

- [ ] **Step 2: Run apply test to verify it fails**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=ApplyOptimizationRunCommandHandlerTest test
```

Expected: FAIL because overridden schedule currently produces no allocations.

- [ ] **Step 3: Select allocation JSON by decision**

In `ApplyOptimizationRunCommandHandler.buildPlanAllocations`, replace the initial guard and JSON source with:

```java
        String allocationChunksJson = item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN
                ? item.getOverrideAllocationChunksJson()
                : item.getAllocationChunksJson();
        if (allocationChunksJson == null || allocationChunksJson.isBlank()) {
            return List.of();
        }
        List<OptimizationScheduleAllocation> allocations = jsonUtils.fromJsonToList(
                allocationChunksJson, OptimizationScheduleAllocation.class);
```

Remove this old condition:

```java
        if (item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN
                || item.getAllocationChunksJson() == null
                || item.getAllocationChunksJson().isBlank()) {
            return List.of();
        }
```

- [ ] **Step 4: Run apply tests**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=ApplyOptimizationRunCommandHandlerTest test
```

Expected: PASS.

- [ ] **Step 5: Run all optimization backend tests**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=*Optimization*Test test
```

Expected: PASS.

- [ ] **Step 6: Commit apply behavior**

Run from repo root:

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandler.java pm_core/src/test/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): apply overridden optimization allocations"
```

---

### Task 5: Frontend Types and Objective-First Launch

**Files:**
- Modify: `serp_web/src/modules/pm/types/optimization.types.ts`
- Modify: `serp_web/src/modules/pm/constants/optimization.ts`
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx`
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunIntentPanel.tsx`

- [ ] **Step 1: Add allocation types**

In `optimization.types.ts`, add after `PMOptimizationUserSummaryApi`:

```ts
export interface PMOptimizationScheduleAllocationApi {
  assigneeId: number;
  start: number;
  end: number;
  effortMillis: number;
}
```

In `PMOptimizationRunItemApi`, add:

```ts
  allocationChunks?: PMOptimizationScheduleAllocationApi[];
  overrideAllocationChunks?: PMOptimizationScheduleAllocationApi[];
```

In `PMOptimizationRunDecisionItemRequest`, add:

```ts
  overrideAllocationChunks?: PMOptimizationScheduleAllocationApi[];
```

- [ ] **Step 2: Add objective mapping helper**

In `constants/optimization.ts`, add:

```ts
export const PM_OPTIMIZATION_OBJECTIVE_ALGORITHM_MAP: Record<
  PMOptimizationObjective,
  string
> = {
  BALANCED_WORKLOAD: 'greedy-balanced',
  MINIMAL_REASSIGNMENT: 'greedy-minimal-reassignment',
  SKILL_FIRST: 'greedy-skill-first',
  DEADLINE_FIRST: 'greedy-deadline-first',
};

export function getPmOptimizationAlgorithmKeyForObjective(
  objective: PMOptimizationObjective
) {
  return PM_OPTIMIZATION_OBJECTIVE_ALGORITHM_MAP[objective];
}
```

- [ ] **Step 3: Derive algorithm in launch page**

In `PMProjectOptimizationPage.tsx`, remove:

```ts
  const [algorithmKey, setAlgorithmKey] = useState(
    PM_OPTIMIZATION_DEFAULT_ALGORITHM_KEY
  );
```

Add:

```ts
  const algorithmKey = getPmOptimizationAlgorithmKeyForObjective(objective);
```

Update the imports from `../constants/optimization` to include:

```ts
  getPmOptimizationAlgorithmKeyForObjective,
```

Remove the `PM_OPTIMIZATION_DEFAULT_ALGORITHM_KEY` import.

Remove this prop from `PMOptimizationRunIntentPanel` usage:

```tsx
            onAlgorithmKeyChange={setAlgorithmKey}
```

- [ ] **Step 4: Make intent panel read-only for algorithm**

In `PMOptimizationRunIntentPanel.tsx`, remove this prop:

```ts
  onAlgorithmKeyChange: (value: string) => void;
```

Remove it from the function destructuring.

Replace the algorithm `<select>` section with:

```tsx
        <div className='space-y-2'>
          <p className='text-sm font-medium'>Algorithm preset</p>
          <div className='rounded-md border bg-muted/30 px-3 py-2 text-sm'>
            {PM_OPTIMIZATION_ALGORITHM_OPTIONS.find(
              (option) => option.value === algorithmKey
            )?.label || algorithmKey}
          </div>
          <p className='text-xs text-muted-foreground'>
            Derived from the selected objective so the solver intent stays
            consistent.
          </p>
        </div>
```

- [ ] **Step 5: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: PASS.

- [ ] **Step 6: Commit frontend launch changes**

Run from repo root:

```bash
git add serp_web/src/modules/pm/types/optimization.types.ts serp_web/src/modules/pm/constants/optimization.ts serp_web/src/modules/pm/pages/PMProjectOptimizationPage.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunIntentPanel.tsx
git commit -m "feat(pm-web): derive optimization algorithm from objective"
```

---

### Task 6: Schedule Chunk Display and Mini Editor UI

**Files:**
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx`
- Modify: `serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`

- [ ] **Step 1: Add effective chunk helpers in run page**

In `PMProjectOptimizationRunPage.tsx`, add imports:

```ts
import type {
  PMOptimizationScheduleAllocationApi,
  PMProjectPersonApi,
} from '../types/api';
import {
  fromCalendarDateTimeInputValue,
  toCalendarDateTimeInputValue,
} from '../components/projects/calendar/pmProjectCalendar.utils';
```

Add local state:

```ts
  const [overrideAllocationChunks, setOverrideAllocationChunks] = useState<
    PMOptimizationScheduleAllocationApi[]
  >([]);
```

In `openOverride`, after planned date setters, add:

```ts
    const effectiveChunks =
      item.scheduleDecision === 'OVERRIDDEN' &&
      item.overrideAllocationChunks?.length
        ? item.overrideAllocationChunks
        : item.allocationChunks || [];
    setOverrideAllocationChunks(
      effectiveChunks.length
        ? effectiveChunks
        : [
            {
              assigneeId:
                item.overrideAssigneeId ||
                item.suggestedAssigneeId ||
                item.currentAssigneeId ||
                0,
              start:
                item.overridePlannedStart ||
                item.suggestedPlannedStart ||
                item.currentPlannedStart ||
                Date.now(),
              end:
                item.overridePlannedEnd ||
                item.suggestedPlannedEnd ||
                item.currentPlannedEnd ||
                Date.now() + 60 * 60 * 1000,
              effortMillis: 60 * 60 * 1000,
            },
          ]
    );
```

- [ ] **Step 2: Send chunks on save**

In `saveOverride`, replace the schedule range fields in the request body with:

```ts
      overridePlannedStart:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? undefined
          : fromLocalDateInputValue(overridePlannedStart),
      overridePlannedEnd:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? undefined
          : fromLocalDateInputValue(overridePlannedEnd, true),
      overrideAllocationChunks:
        overrideScheduleDecision === 'OVERRIDDEN'
          ? overrideAllocationChunks.filter(
              (chunk) =>
                chunk.assigneeId > 0 &&
                chunk.start > 0 &&
                chunk.end > 0 &&
                chunk.effortMillis > 0
            )
          : undefined,
```

Keep assignment fields unchanged.

- [ ] **Step 3: Pass chunk props into dialog**

In the `PMOptimizationRunOverrideDialog` usage, add:

```tsx
        overrideAllocationChunks={overrideAllocationChunks}
        projectPeople={projectPeople}
        onOverrideAllocationChunksChange={setOverrideAllocationChunks}
```

- [ ] **Step 4: Show allocation detail in table**

In `PMOptimizationRunItemTable.tsx`, import allocation type:

```ts
  PMOptimizationScheduleAllocationApi,
```

In the schedule mode item render, add:

```ts
                const allocationChunks =
                  item.scheduleDecision === 'OVERRIDDEN' &&
                  item.overrideAllocationChunks?.length
                    ? item.overrideAllocationChunks
                    : item.allocationChunks || [];
```

In the detail grid, add a third detail panel for schedule mode:

```tsx
                        {mode === 'schedule' ? (
                          <AllocationList chunks={allocationChunks} />
                        ) : null}
```

Add this component near `DetailList`:

```tsx
function AllocationList({
  chunks,
}: {
  chunks: PMOptimizationScheduleAllocationApi[];
}) {
  return (
    <div className='rounded-md border bg-muted/20 p-3 md:col-span-2'>
      <div className='mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground'>
        Allocations
        <Badge variant='secondary' className='h-5 px-1.5'>
          {chunks.length}
        </Badge>
      </div>
      <div className='space-y-1 text-sm'>
        {chunks.length ? (
          chunks.slice(0, 4).map((chunk, index) => (
            <div
              key={`${chunk.assigneeId}-${chunk.start}-${chunk.end}-${index}`}
              className='text-muted-foreground'
            >
              User #{chunk.assigneeId}: {formatDateTime(chunk.start)} -{' '}
              {formatDateTime(chunk.end)} ({formatEffort(chunk.effortMillis)})
            </div>
          ))
        ) : (
          <div className='text-muted-foreground'>-</div>
        )}
      </div>
    </div>
  );
}
```

Add helpers:

```ts
function formatDateTime(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}

function formatEffort(value?: number | null) {
  if (!value) return '0m';
  const minutes = Math.round(value / 60000);
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h ${remainder}m` : `${hours}h`;
}
```

- [ ] **Step 5: Add dialog props**

In `PMOptimizationRunOverrideDialog.tsx`, add imports:

```ts
import { Trash2, Plus } from 'lucide-react';
import { Input, Checkbox } from '@/shared/components/ui';
import {
  fromCalendarDateTimeInputValue,
  toCalendarDateTimeInputValue,
} from '../projects/calendar/pmProjectCalendar.utils';
import type {
  PMOptimizationScheduleAllocationApi,
  PMProjectPersonApi,
} from '../../types/api';
```

Add props:

```ts
  overrideAllocationChunks: PMOptimizationScheduleAllocationApi[];
  projectPeople: PMProjectPersonApi[];
  onOverrideAllocationChunksChange: (
    value: PMOptimizationScheduleAllocationApi[]
  ) => void;
```

Add state:

```ts
  const [showAllProjectMembers, setShowAllProjectMembers] = useState(false);
```

Add `useState` import from React:

```ts
import { useState } from 'react';
```

- [ ] **Step 6: Add chunk editing handlers**

Inside `PMOptimizationRunOverrideDialog`, add:

```ts
  const visibleUsers = showAllProjectMembers
    ? projectPeople.map((person) => ({
        id: Number(person.userId),
        label: person.name || person.email || `User #${person.userId}`,
      }))
    : users;

  const updateChunk = (
    index: number,
    patch: Partial<PMOptimizationScheduleAllocationApi>
  ) => {
    onOverrideAllocationChunksChange(
      overrideAllocationChunks.map((chunk, chunkIndex) =>
        chunkIndex === index ? { ...chunk, ...patch } : chunk
      )
    );
  };

  const addChunk = () => {
    const lastChunk = overrideAllocationChunks.at(-1);
    const start = lastChunk?.end || Date.now();
    onOverrideAllocationChunksChange([
      ...overrideAllocationChunks,
      {
        assigneeId: Number(overrideAssigneeId) || item?.suggestedAssigneeId || 0,
        start,
        end: start + 60 * 60 * 1000,
        effortMillis: 60 * 60 * 1000,
      },
    ]);
  };

  const removeChunk = (index: number) => {
    onOverrideAllocationChunksChange(
      overrideAllocationChunks.filter((_chunk, chunkIndex) => chunkIndex !== index)
    );
  };

  const derivedStart = overrideAllocationChunks.length
    ? Math.min(...overrideAllocationChunks.map((chunk) => chunk.start))
    : null;
  const derivedEnd = overrideAllocationChunks.length
    ? Math.max(...overrideAllocationChunks.map((chunk) => chunk.end))
    : null;
```

- [ ] **Step 7: Replace schedule date inputs with chunk editor**

In the dialog body, keep the legacy planned start/end date inputs only when `scheduleDecision !== 'OVERRIDDEN'`. Add this block after the schedule decision select:

```tsx
            {scheduleDecision === 'OVERRIDDEN' ? (
              <div className='space-y-3'>
                <div className='flex items-center justify-between gap-3'>
                  <div>
                    <p className='text-sm font-medium'>Schedule allocations</p>
                    <p className='text-xs text-muted-foreground'>
                      Planned range is derived from the earliest start and latest end.
                    </p>
                  </div>
                  <Button type='button' variant='outline' size='sm' onClick={addChunk}>
                    <Plus className='mr-2 h-4 w-4' />
                    Add chunk
                  </Button>
                </div>

                <label className='flex items-center gap-2 text-sm text-muted-foreground'>
                  <Checkbox
                    checked={showAllProjectMembers}
                    onCheckedChange={(checked) =>
                      setShowAllProjectMembers(Boolean(checked))
                    }
                  />
                  Show all project members
                </label>

                <div className='rounded-md border bg-muted/20 px-3 py-2 text-sm'>
                  Derived range: {formatDateTime(derivedStart)} -{' '}
                  {formatDateTime(derivedEnd)}
                </div>

                <div className='space-y-2'>
                  {overrideAllocationChunks.map((chunk, index) => (
                    <div
                      key={`${chunk.assigneeId}-${chunk.start}-${index}`}
                      className='grid gap-2 rounded-md border p-3 md:grid-cols-[1fr_1fr_1fr_120px_40px]'
                    >
                      <select
                        value={chunk.assigneeId || ''}
                        onChange={(event) =>
                          updateChunk(index, {
                            assigneeId: Number(event.target.value),
                          })
                        }
                        className='h-10 rounded-md border bg-background px-3 text-sm'
                      >
                        <option value=''>Assignee</option>
                        {visibleUsers.map((user) => (
                          <option key={user.id} value={user.id}>
                            {user.label}
                          </option>
                        ))}
                      </select>
                      <Input
                        type='datetime-local'
                        value={toCalendarDateTimeInputValue(chunk.start)}
                        onChange={(event) =>
                          updateChunk(index, {
                            start:
                              fromCalendarDateTimeInputValue(event.target.value) ||
                              0,
                          })
                        }
                      />
                      <Input
                        type='datetime-local'
                        value={toCalendarDateTimeInputValue(chunk.end)}
                        onChange={(event) =>
                          updateChunk(index, {
                            end:
                              fromCalendarDateTimeInputValue(event.target.value) ||
                              0,
                          })
                        }
                      />
                      <Input
                        type='number'
                        min={1}
                        value={Math.round(chunk.effortMillis / 60000)}
                        onChange={(event) =>
                          updateChunk(index, {
                            effortMillis: Number(event.target.value) * 60000,
                          })
                        }
                      />
                      <Button
                        type='button'
                        size='icon'
                        variant='ghost'
                        onClick={() => removeChunk(index)}
                      >
                        <Trash2 className='h-4 w-4' />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className='grid gap-3 md:grid-cols-2'>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned start</span>
                  <PMDatePicker
                    value={overridePlannedStart}
                    onChange={(date) =>
                      onOverridePlannedStartChange(
                        date ? toLocalDateInputValue(date) : ''
                      )
                    }
                    className='w-full'
                    buttonClassName='flex-1'
                  />
                </label>
                <label className='space-y-1'>
                  <span className='text-sm font-medium'>Planned end</span>
                  <PMDatePicker
                    value={overridePlannedEnd}
                    onChange={(date) =>
                      onOverridePlannedEndChange(
                        date ? toLocalDateInputValue(date) : ''
                      )
                    }
                    className='w-full'
                    buttonClassName='flex-1'
                  />
                </label>
              </div>
            )}
```

Add helper in dialog file:

```ts
function formatDateTime(value?: number | null) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString();
}
```

- [ ] **Step 8: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: PASS.

- [ ] **Step 9: Run frontend format check**

Run from `serp_web/`:

```bash
npm run format:check
```

Expected: PASS. If it fails only for touched files, run `npm run format` and rerun `npm run format:check`.

- [ ] **Step 10: Commit mini editor**

Run from repo root:

```bash
git add serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx serp_web/src/modules/pm/components/optimization/PMOptimizationRunOverrideDialog.tsx
git commit -m "feat(pm-web): edit optimization schedule allocation chunks"
```

---

### Task 7: Final Verification

**Files:**
- Check all modified backend and frontend files.

- [ ] **Step 1: Run focused backend optimization tests**

Run from `pm_core/`:

```bash
./mvnw.cmd -Dtest=*Optimization*Test test
```

Expected: PASS.

- [ ] **Step 2: Run pm_core compile**

Run from `pm_core/`:

```bash
./mvnw.cmd clean compile
```

Expected: PASS.

- [ ] **Step 3: Run frontend lint**

Run from `serp_web/`:

```bash
npm run lint
```

Expected: PASS.

- [ ] **Step 4: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: PASS.

- [ ] **Step 5: Run frontend format check**

Run from `serp_web/`:

```bash
npm run format:check
```

Expected: PASS.

- [ ] **Step 6: Inspect git status**

Run from repo root:

```bash
git status --short
```

Expected: only intentional changes are present, or clean if every task commit succeeded.

- [ ] **Step 7: Final commit for verification fixes**

If verification required fixes, commit only those fixes:

```bash
git add pm_core serp_web
git commit -m "chore: verify pm optimization schedule overrides"
```

If no fixes were needed, do not create an empty commit.

---

## Self-Review

Spec coverage:

- Objective-first launch: Task 1 and Task 5.
- Backend normalization: Task 1.
- Mini plan editor and chunk request: Task 3 and Task 6.
- Persist override chunks separately from generated chunks: Task 2.
- Apply override chunks to plan allocations: Task 4.
- Error handling for invalid chunks, non-assignable assignees, and dependency checks: Task 3.
- Verification commands: Task 7.

Type consistency:

- Backend allocation command type is `BatchUpdateOptimizationRunItemDecisionsCommand.AllocationOverride`.
- Backend persisted field is `overrideAllocationChunksJson`.
- API view field is `overrideAllocationChunks`.
- Frontend request field is `overrideAllocationChunks`.
- Frontend API type is `PMOptimizationScheduleAllocationApi`.

No broad algorithm refactor is included; the mapper preserves the existing greedy algorithm classes.
