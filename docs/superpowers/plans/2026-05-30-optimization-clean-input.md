# Optimization Clean Input Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace ambiguous optimization mode/boolean input with explicit objective and change scope across API, domain, persistence, and tests.

**Architecture:** Introduce `OptimizationRunIntent` as the single canonical policy object for algorithm key, objective, and change scope. Generate flow builds an intent once, validates it against algorithm capabilities, passes it through project model building, algorithms, policies, and solution validation, then persists `objective` and `changeScope` on the run.

**Tech Stack:** Java 21, Spring Boot 3.5, Jakarta Validation, JUnit 5, Mockito, Flyway, Maven via direct `mvn`.

---

## File Structure

- Create `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationObjective.java`: objective enum.
- Create `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationChangeScope.java`: change scope enum with helper methods.
- Create `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationRunIntent.java`: canonical algorithm/policy input.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationBuilderInput.java`: replace legacy fields with intent.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmOptions.java`: replace duplicated fields with intent.
- Modify `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java`: new request contract.
- Modify `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`: new command contract and defaults.
- Modify `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`: pass new request fields.
- Modify `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`: build intent, validate capabilities, persist clean metadata.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment/GreedyAssignmentPolicy.java`: use intent scope/objective.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/GreedySchedulingPolicy.java`: use intent scope.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java`: validate against change scope.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`: pass intent into options.
- Modify `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java`: replace mode/flags with objective/changeScope.
- Modify `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java`: replace columns.
- Modify `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java`: map new fields.
- Create `pm_core/src/main/resources/db/migration/V28__replace_optimization_mode_with_intent.sql`: backfill and drop legacy columns.
- Modify `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java`: expose clean fields.
- Modify `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`: map clean fields.
- Modify `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandler.java`: rebuild model from clean intent and persisted algorithm key.
- Update tests under `pm_core/src/test/java/serp/project/pmcore/...` for new API/domain contracts.

---

### Task 1: Add Canonical Intent Types

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationObjective.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationChangeScope.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationRunIntent.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationBuilderInput.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmOptions.java`

- [ ] **Step 1: Write compile-facing domain test updates**

Update test helpers that construct `OptimizationBuilderInput` to use this shape:

```java
private OptimizationRunIntent intent(OptimizationObjective objective, OptimizationChangeScope changeScope) {
    return new OptimizationRunIntent(OptimizationAlgorithmKeys.GREEDY_BALANCED, objective, changeScope);
}

return new OptimizationBuilderInput(
        1L,
        100L,
        List.of(10L, 20L),
        1_000L,
        10_000L,
        intent(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE)
);
```

- [ ] **Step 2: Run focused compile test and confirm it fails on missing types**

Run from `pm_core`:

```bash
mvn -Dtest=OptimizationSolutionValidatorTest test
```

Expected: FAIL with compile errors for `OptimizationRunIntent`, `OptimizationObjective`, or `OptimizationChangeScope`.

- [ ] **Step 3: Add new enum and intent types**

Create `OptimizationObjective.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.enums;

public enum OptimizationObjective {
    BALANCED_WORKLOAD,
    MINIMAL_REASSIGNMENT,
    SKILL_FIRST,
    DEADLINE_FIRST
}
```

Create `OptimizationChangeScope.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.enums;

public enum OptimizationChangeScope {
    ASSIGNMENT_ONLY,
    SCHEDULE_ONLY,
    ASSIGNMENT_AND_SCHEDULE;

    public boolean includesAssignment() {
        return this == ASSIGNMENT_ONLY || this == ASSIGNMENT_AND_SCHEDULE;
    }

    public boolean includesScheduling() {
        return this == SCHEDULE_ONLY || this == ASSIGNMENT_AND_SCHEDULE;
    }
}
```

Create `OptimizationRunIntent.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;

public record OptimizationRunIntent(
        String algorithmKey,
        OptimizationObjective objective,
        OptimizationChangeScope changeScope
) {
}
```

- [ ] **Step 4: Replace legacy builder/options fields with intent**

`OptimizationBuilderInput` should become:

```java
public record OptimizationBuilderInput(
        Long tenantId,
        Long projectId,
        List<Long> selectedWorkItemIds,
        Long planningStart,
        Long planningEnd,
        OptimizationRunIntent intent
) {
}
```

`OptimizationAlgorithmOptions` should become:

```java
public record OptimizationAlgorithmOptions(
        OptimizationRunIntent intent
) {
}
```

- [ ] **Step 5: Commit task 1**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationObjective.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationChangeScope.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationRunIntent.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationBuilderInput.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationAlgorithmOptions.java \
        pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java
git commit -m "feat(pm): add optimization run intent"
```

---

### Task 2: Switch Generate API And Command

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

- [ ] **Step 1: Update command-handler test to construct the clean command**

Use:

```java
new GenerateOptimizationRunCommand(
        1L,
        10L,
        2L,
        "SELECTED_WORK_ITEMS",
        null,
        OptimizationObjective.BALANCED_WORKLOAD,
        OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
        1_000L,
        10_000L,
        List.of(100L)
)
```

- [ ] **Step 2: Run focused test and confirm it fails on command signature**

Run from `pm_core`:

```bash
mvn -Dtest=GenerateOptimizationRunCommandHandlerTest test
```

Expected: FAIL with compile errors for the old command constructor.

- [ ] **Step 3: Update request DTO**

`GenerateOptimizationRunRequest` should import the new enums and remove legacy fields:

```java
@Builder.Default
private String algorithmKey = OptimizationAlgorithmKeys.GREEDY_BALANCED;

@NotNull(message = "Optimization objective is required")
private OptimizationObjective objective;

@NotNull(message = "Optimization change scope is required")
private OptimizationChangeScope changeScope;
```

Keep `scope`, `planningStart`, `planningEnd`, and `selectedWorkItemIds` unchanged.

- [ ] **Step 4: Update command record**

`GenerateOptimizationRunCommand` should use:

```java
public record GenerateOptimizationRunCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        String scope,
        String algorithmKey,
        OptimizationObjective objective,
        OptimizationChangeScope changeScope,
        Long planningStart,
        Long planningEnd,
        List<Long> selectedWorkItemIds
) implements ICommand<OptimizationRunReviewView> {
    public GenerateOptimizationRunCommand {
        selectedWorkItemIds = selectedWorkItemIds == null ? List.of() : List.copyOf(selectedWorkItemIds);
        algorithmKey = algorithmKey == null || algorithmKey.isBlank()
                ? OptimizationAlgorithmKeys.GREEDY_BALANCED
                : algorithmKey;
        objective = objective == null ? OptimizationObjective.BALANCED_WORKLOAD : objective;
        changeScope = changeScope == null ? OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE : changeScope;
    }
}
```

- [ ] **Step 5: Update controller**

Controller command construction should pass:

```java
request.getObjective(),
request.getChangeScope(),
request.getPlanningStart(),
request.getPlanningEnd(),
request.getSelectedWorkItemIds()
```

- [ ] **Step 6: Commit task 2**

```bash
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/GenerateOptimizationRunRequest.java \
        pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommand.java \
        pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java \
        pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): switch optimization generate input"
```

---

### Task 3: Route Domain Logic Through Intent

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment/GreedyAssignmentPolicy.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/GreedySchedulingPolicy.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

- [ ] **Step 1: Add capability mismatch test**

In `GenerateOptimizationRunCommandHandlerTest`, add a test that stubs an assignment-only algorithm and requests scheduling:

```java
@Test
void handleShouldRejectWhenAlgorithmCapabilitiesDoNotSupportRequestedScope() {
    IOptimizationAlgorithm algorithm = stubAlgorithmWithCapabilities(Set.of(OptimizationCapability.ASSIGNMENT));
    when(optimizationAlgorithmRegistry.resolve(OptimizationAlgorithmKeys.GREEDY_BALANCED)).thenReturn(algorithm);

    assertThatThrownBy(() -> handler.handle(new GenerateOptimizationRunCommand(
            1L,
            10L,
            2L,
            "SELECTED_WORK_ITEMS",
            OptimizationAlgorithmKeys.GREEDY_BALANCED,
            OptimizationObjective.BALANCED_WORKLOAD,
            OptimizationChangeScope.SCHEDULE_ONLY,
            1_000L,
            10_000L,
            List.of(100L)
    ))).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not support scheduling");
}
```

- [ ] **Step 2: Update greedy behavior tests**

Use these helper calls:

```java
input(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_ONLY)
input(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.SCHEDULE_ONLY)
input(OptimizationObjective.MINIMAL_REASSIGNMENT, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE)
```

- [ ] **Step 3: Run focused tests and confirm failures**

Run from `pm_core`:

```bash
mvn -Dtest=GenerateOptimizationRunCommandHandlerTest,GreedyOptimizationRunGeneratorTest,OptimizationSolutionValidatorTest test
```

Expected: FAIL with compile errors and the new capability mismatch assertion failing until implemented.

- [ ] **Step 4: Build intent and options in handler**

Handler generation should create:

```java
String algorithmKey = normalizeAlgorithmKey(command.algorithmKey());
OptimizationRunIntent intent = new OptimizationRunIntent(
        algorithmKey,
        command.objective(),
        command.changeScope()
);
OptimizationBuilderInput input = new OptimizationBuilderInput(
        command.tenantId(),
        command.projectId(),
        command.selectedWorkItemIds(),
        command.planningStart(),
        command.planningEnd(),
        intent
);
```

Call algorithm with:

```java
validateCapabilities(intent, algorithm.descriptor());
OptimizationSolution solution = algorithm.solve(
        new OptimizationProblem(projectModel, input),
        new OptimizationAlgorithmOptions(intent)
);
```

- [ ] **Step 5: Implement capability validation**

Add private helper:

```java
private void validateCapabilities(OptimizationRunIntent intent, OptimizationAlgorithmDescriptor descriptor) {
    if (intent.changeScope().includesAssignment()
            && !descriptor.capabilities().contains(OptimizationCapability.ASSIGNMENT)) {
        throw new IllegalArgumentException("Optimization algorithm does not support assignment: " + descriptor.key());
    }
    if (intent.changeScope().includesScheduling()
            && !descriptor.capabilities().contains(OptimizationCapability.SCHEDULING)) {
        throw new IllegalArgumentException("Optimization algorithm does not support scheduling: " + descriptor.key());
    }
}
```

- [ ] **Step 6: Update policies**

Assignment enabled:

```java
boolean assignmentEnabled = options.intent().changeScope().includesAssignment();
```

Minimal reassignment checks:

```java
options.intent().objective() == OptimizationObjective.MINIMAL_REASSIGNMENT
```

Skill-first checks:

```java
options.intent().objective() == OptimizationObjective.SKILL_FIRST
        || OptimizationAlgorithmKeys.GREEDY_SKILL_FIRST.equals(options.intent().algorithmKey())
```

Scheduling enabled:

```java
if (!options.intent().changeScope().includesScheduling()) {
    return Map.of();
}
```

- [ ] **Step 7: Update validator**

Use:

```java
boolean allowReassignment = problem.input().intent().changeScope().includesAssignment();
boolean allowScheduleChanges = problem.input().intent().changeScope().includesScheduling();
```

- [ ] **Step 8: Run focused tests and commit task 3**

Run from `pm_core`:

```bash
mvn -Dtest=GenerateOptimizationRunCommandHandlerTest,GreedyOptimizationRunGeneratorTest,OptimizationSolutionValidatorTest test
```

Expected: PASS.

Commit:

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGenerator.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/assignment/GreedyAssignmentPolicy.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/GreedySchedulingPolicy.java \
        pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidator.java \
        pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java \
        pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/OptimizationSolutionValidatorTest.java \
        pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java
git commit -m "feat(pm): route optimization policies through intent"
```

---

### Task 4: Replace Persisted Run Metadata

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java`
- Create: `pm_core/src/main/resources/db/migration/V28__replace_optimization_mode_with_intent.sql`
- Modify: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java`
- Modify: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`

- [ ] **Step 1: Update mapper/review tests to expect clean metadata**

Use builder fields:

```java
.objective(OptimizationObjective.BALANCED_WORKLOAD.name())
.changeScope(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE.name())
```

Remove assertions for `mode`, `allowReassignment`, and `allowScheduleChanges`.

- [ ] **Step 2: Run mapper/review tests and confirm failures**

Run from `pm_core`:

```bash
mvn -Dtest=OptimizationFoundationMapperTest,OptimizationRunReviewAssemblerTest test
```

Expected: FAIL with missing fields until entity/model/view are updated.

- [ ] **Step 3: Update entity and JPA model**

`OptimizationRunEntity` fields:

```java
private String objective;
private String changeScope;
```

`OptimizationRunModel` columns:

```java
@Column(name = "objective", nullable = false, length = 50)
private String objective;

@Column(name = "change_scope", nullable = false, length = 50)
private String changeScope;
```

- [ ] **Step 4: Update mapper**

Mapper chain should use:

```java
.objective(entity.getObjective()).changeScope(entity.getChangeScope())
```

and:

```java
.objective(model.getObjective()).changeScope(model.getChangeScope())
```

- [ ] **Step 5: Add Flyway migration**

Create `V28__replace_optimization_mode_with_intent.sql`:

```sql
ALTER TABLE optimization_runs
    ADD COLUMN objective VARCHAR(50) NOT NULL DEFAULT 'BALANCED_WORKLOAD',
    ADD COLUMN change_scope VARCHAR(50) NOT NULL DEFAULT 'ASSIGNMENT_AND_SCHEDULE';

UPDATE optimization_runs
SET objective = CASE
        WHEN mode = 'MINIMAL_REASSIGNMENT' THEN 'MINIMAL_REASSIGNMENT'
        ELSE 'BALANCED_WORKLOAD'
    END,
    change_scope = CASE
        WHEN mode = 'ASSIGNMENT_ONLY' THEN 'ASSIGNMENT_ONLY'
        WHEN mode = 'SCHEDULE_ONLY' THEN 'SCHEDULE_ONLY'
        WHEN allow_reassignment = TRUE AND allow_schedule_changes = FALSE THEN 'ASSIGNMENT_ONLY'
        WHEN allow_reassignment = FALSE AND allow_schedule_changes = TRUE THEN 'SCHEDULE_ONLY'
        ELSE 'ASSIGNMENT_AND_SCHEDULE'
    END;

ALTER TABLE optimization_runs
    DROP COLUMN mode,
    DROP COLUMN allow_reassignment,
    DROP COLUMN allow_schedule_changes;
```

- [ ] **Step 6: Commit task 4**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunEntity.java \
        pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunModel.java \
        pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunMapper.java \
        pm_core/src/main/resources/db/migration/V28__replace_optimization_mode_with_intent.sql \
        pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java \
        pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java
git commit -m "feat(pm): persist optimization run intent"
```

---

### Task 5: Update Review And Override Rebuild Flow

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandlerTest.java`

- [ ] **Step 1: Update review view**

Replace:

```java
private String mode;
private Boolean allowReassignment;
private Boolean allowScheduleChanges;
```

with:

```java
private String objective;
private String changeScope;
```

- [ ] **Step 2: Update assembler**

Use:

```java
.objective(run.getObjective())
.changeScope(run.getChangeScope())
```

- [ ] **Step 3: Update override rebuild**

In `buildCurrentProjectModel`, use:

```java
OptimizationRunIntent intent = new OptimizationRunIntent(
        run.getAlgorithmKey(),
        OptimizationObjective.valueOf(run.getObjective()),
        OptimizationChangeScope.valueOf(run.getChangeScope())
);
return optimizationProjectModelBuilder.build(new OptimizationBuilderInput(
        run.getTenantId(),
        run.getProjectId(),
        selectedWorkItemIds,
        run.getPlanningStart(),
        run.getPlanningEnd(),
        intent
));
```

- [ ] **Step 4: Run focused tests and commit**

Run from `pm_core`:

```bash
mvn -Dtest=OptimizationRunReviewAssemblerTest,UpdateOptimizationRunItemDecisionCommandHandlerTest test
```

Expected: PASS.

Commit:

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewView.java \
        pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java \
        pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandler.java \
        pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java \
        pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandlerTest.java
git commit -m "feat(pm): expose clean optimization intent"
```

---

### Task 6: Clean Legacy References And Run Full Verification

**Files:**
- Modify tests and source files found by `rg`.
- Optionally delete `pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationMode.java` if no references remain.

- [ ] **Step 1: Search legacy references**

Run:

```bash
rg -n "OptimizationMode|allowReassignment|allowScheduleChanges|getMode|getAllowReassignment|getAllowScheduleChanges|\\.mode\\(|\\.allowReassignment\\(|\\.allowScheduleChanges\\(" pm_core/src/main/java pm_core/src/test/java
```

Expected: no production references. Test references should only remain if they intentionally verify legacy absence; otherwise remove them.

- [ ] **Step 2: Delete unused `OptimizationMode`**

If the search shows no references:

```bash
git rm pm_core/src/main/java/serp/project/pmcore/domain/optimization/enums/OptimizationMode.java
```

- [ ] **Step 3: Run focused optimization suite**

Run from `pm_core`:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest,OptimizationProjectModelBuilderTest,OptimizationAlgorithmRegistryTest,OptimizationSolutionValidatorTest,GenerateOptimizationRunCommandHandlerTest,OptimizationRunReviewAssemblerTest,OptimizationFoundationMapperTest,ApplyOptimizationRunCommandHandlerTest,UpdateOptimizationRunItemDecisionCommandHandlerTest test
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Run full module tests**

Run from `pm_core`:

```bash
mvn clean test
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit cleanup**

```bash
git add pm_core/src/main/java pm_core/src/test/java pm_core/src/main/resources/db/migration
git commit -m "test(pm): verify optimization clean input flow"
```
