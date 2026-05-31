# PM Core Optimization Clean Input Design

## Context

The optimization flow now supports algorithm strategies, algorithm metadata, solution validation, and separated greedy assignment/scheduling policies. The remaining input problem is ambiguity:

- `OptimizationMode` mixes objective policy (`BALANCED_WORKLOAD`, `MINIMAL_REASSIGNMENT`) with change scope (`ASSIGNMENT_ONLY`, `SCHEDULE_ONLY`).
- `allowReassignment` and `allowScheduleChanges` also express change scope.
- Some combinations are valid but surprising, for example `ASSIGNMENT_ONLY` with `allowScheduleChanges=true`.
- Some combinations create no meaningful optimization run, for example both flags set to false.

The selected direction is a direct clean break. New generate requests and persisted run metadata should use explicit, non-overlapping input concepts.

## Goals

- Replace ambiguous mode/flag input with one objective and one change scope.
- Make the domain algorithm contract clear enough for additional heuristics and a real solver.
- Remove no-op input combinations by construction.
- Validate algorithm capabilities before solving.
- Keep review, override, and apply flows based on persisted run metadata.
- Use direct `mvn` commands for verification.

## Non-Goals

- Do not introduce a real solver in this change.
- Do not keep dual API compatibility for old generate request fields.
- Do not let algorithms bypass review/apply safety.
- Do not move persistence writes into algorithm implementations.

## Public API

`GenerateOptimizationRunRequest` will remove:

- `mode`
- `allowReassignment`
- `allowScheduleChanges`

It will use:

```java
private String algorithmKey;
private OptimizationObjective objective;
private OptimizationChangeScope changeScope;
private Long planningStart;
private Long planningEnd;
private List<Long> selectedWorkItemIds;
```

`algorithmKey` keeps the existing default behavior: a missing or blank value resolves to `greedy-balanced`.

`objective` and `changeScope` are required.

## Domain Types

Add objective enum:

```java
public enum OptimizationObjective {
    BALANCED_WORKLOAD,
    MINIMAL_REASSIGNMENT,
    SKILL_FIRST,
    DEADLINE_FIRST
}
```

Add change scope enum:

```java
public enum OptimizationChangeScope {
    ASSIGNMENT_ONLY,
    SCHEDULE_ONLY,
    ASSIGNMENT_AND_SCHEDULE
}
```

Add canonical intent:

```java
public record OptimizationRunIntent(
        String algorithmKey,
        OptimizationObjective objective,
        OptimizationChangeScope changeScope
) {
}
```

`OptimizationRunIntent` is the single source of truth for algorithm selection and optimization policy. `OptimizationBuilderInput`, `OptimizationProblem`, and `OptimizationAlgorithmOptions` should reference this intent instead of duplicating `algorithmKey`, `mode`, `allowReassignment`, and `allowScheduleChanges`.

## Behavior

`OptimizationChangeScope` controls which solution dimensions may change:

- `ASSIGNMENT_ONLY`: assignment suggestions may change assignees; schedule suggestions must not be generated.
- `SCHEDULE_ONLY`: schedule suggestions may change planning windows; assignment suggestions must keep current assignees.
- `ASSIGNMENT_AND_SCHEDULE`: both assignment and schedule suggestions may be generated.

`OptimizationObjective` controls preference, not scope:

- `BALANCED_WORKLOAD`: default objective for load/capacity balance.
- `MINIMAL_REASSIGNMENT`: heavily prefers current assignees when assignment is in scope.
- `SKILL_FIRST`: emphasizes required/preferred skill fit.
- `DEADLINE_FIRST`: emphasizes due dates and late-risk reduction.

If an objective has no special implementation yet, the default greedy scorer may treat it like balanced workload except where behavior is already implemented. The important boundary is that objective must never enable or disable assignment/scheduling scope.

## Algorithm Capability Validation

After resolving the algorithm and before building the final solution, validate the requested intent against the algorithm descriptor:

- `ASSIGNMENT_ONLY` requires `OptimizationCapability.ASSIGNMENT`.
- `SCHEDULE_ONLY` requires `OptimizationCapability.SCHEDULING`.
- `ASSIGNMENT_AND_SCHEDULE` requires both capabilities.

Capability mismatch should reject the generate command before persistence. This prevents future solvers or narrow heuristics from silently returning partial solutions for a broader request.

## Persistence

`optimization_runs` will remove:

- `mode`
- `allow_reassignment`
- `allow_schedule_changes`

It will add:

- `objective VARCHAR(50) NOT NULL DEFAULT 'BALANCED_WORKLOAD'`
- `change_scope VARCHAR(50) NOT NULL DEFAULT 'ASSIGNMENT_AND_SCHEDULE'`

Migration should backfill existing rows before dropping legacy columns:

```sql
objective =
    CASE
        WHEN mode = 'MINIMAL_REASSIGNMENT' THEN 'MINIMAL_REASSIGNMENT'
        ELSE 'BALANCED_WORKLOAD'
    END

change_scope =
    CASE
        WHEN mode = 'ASSIGNMENT_ONLY' THEN 'ASSIGNMENT_ONLY'
        WHEN mode = 'SCHEDULE_ONLY' THEN 'SCHEDULE_ONLY'
        WHEN allow_reassignment = TRUE AND allow_schedule_changes = FALSE THEN 'ASSIGNMENT_ONLY'
        WHEN allow_reassignment = FALSE AND allow_schedule_changes = TRUE THEN 'SCHEDULE_ONLY'
        ELSE 'ASSIGNMENT_AND_SCHEDULE'
    END
```

Rows that previously had both flags false are backfilled to `ASSIGNMENT_AND_SCHEDULE` so historical review data remains readable. The new API cannot create that no-op combination.

## Review And Update Flow

Review responses should expose:

- `objective`
- `changeScope`
- existing algorithm metadata: `algorithmKey`, `algorithmVersion`, `solverStatus`, `objectiveScore`

They should no longer expose `mode`, `allowReassignment`, or `allowScheduleChanges`.

`UpdateOptimizationRunItemDecisionCommandHandler` rebuilds the current optimization model from persisted `objective`, `changeScope`, and `algorithmKey`. It must not hardcode `greedy-balanced` when a run was generated by another algorithm.

## Validation

Generate command validation should require:

- positive tenant, user, and project ids
- non-empty selected work item ids with no duplicates
- valid planning range
- non-null objective
- non-null change scope
- supported algorithm key
- algorithm capabilities compatible with requested change scope

Solution validation should use `OptimizationRunIntent`:

- assignment changes are allowed only when `changeScope` includes assignment.
- schedule suggestions are allowed only when `changeScope` includes scheduling.
- objective does not grant permissions to change assignment or schedule.

## Test Plan

Focused tests:

```bash
mvn -Dtest=GenerateOptimizationRunCommandHandlerTest test
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
mvn -Dtest=OptimizationSolutionValidatorTest test
mvn -Dtest=OptimizationRunReviewAssemblerTest test
mvn -Dtest=OptimizationFoundationMapperTest test
mvn -Dtest=UpdateOptimizationRunItemDecisionCommandHandlerTest test
```

Add or update coverage for:

- generate command persists `objective` and `changeScope`.
- API request no longer has legacy input fields.
- `ASSIGNMENT_ONLY` does not generate schedule suggestions.
- `SCHEDULE_ONLY` keeps current assignees while generating schedule suggestions.
- `MINIMAL_REASSIGNMENT` affects assignment scoring only when assignment is in scope.
- capability mismatch rejects the generate command.
- review response includes clean input fields.
- update override model rebuild uses the persisted algorithm key and clean intent.

Final module check:

```bash
mvn clean test
```

## Rollout

This is a clean break. Backend clients must switch to `objective` and `changeScope` when calling generate. Existing persisted rows remain readable through migration backfill. No dual request contract is kept in the generate endpoint.
