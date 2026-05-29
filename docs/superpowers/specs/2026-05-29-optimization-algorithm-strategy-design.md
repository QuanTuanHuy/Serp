# PM Core Optimization Algorithm Strategy Design

## Context

`pm_core` currently generates optimization runs through a fixed greedy flow:

1. `GenerateOptimizationRunCommandHandler` validates the request.
2. `OptimizationProjectModelBuilder` loads and normalizes project data.
3. `GreedyOptimizationRunGenerator` creates assignment and schedule suggestions.
4. The handler persists runs, run items, and warnings.
5. Review, override, discard, and apply flows operate on persisted run items.

This flow already has useful boundaries around persistence, review, and apply safety, but the optimization algorithm is not yet replaceable. The greedy generator also mixes assignment policy, scheduling policy, summary calculation, and warning generation. That makes it harder to add more heuristics or a real solver later.

The agreed direction is to introduce an algorithm strategy layer first, then evolve toward a fuller optimization pipeline over small refactor steps.

## Goals

- Support multiple internal heuristic algorithms.
- Leave room for a real solver implementation later.
- Keep `pm_core` as the owner of problem modeling, solution validation, persistence, review, and apply safety.
- Preserve current generate/review/apply behavior during the first refactor unless a regression test defines an intentional behavior change.
- Allow API and schema changes where they make the flow cleaner.
- Use direct `mvn` commands for verification instead of the Maven wrapper.

## Non-Goals

- Do not introduce a real solver in the first step.
- Do not rewrite the full optimization pipeline in one change.
- Do not move apply-time permission, stale, or locked-plan checks into algorithms.
- Do not let an algorithm write directly to persistence.

## Recommended Approach

Use a Strategy-based algorithm contract now and evolve toward a Template Method style pipeline later.

The new generation flow should become:

```text
Generate command
  -> build OptimizationProblem
  -> resolve OptimizationAlgorithm by algorithmKey
  -> algorithm.solve(problem, options)
  -> validate OptimizationSolution
  -> persist run/items/warnings
  -> review/update/apply using persisted run items
```

The first implementation should wrap the existing greedy generator behavior as the default algorithm. This keeps risk low while creating a stable contract for future algorithms.

## Domain Contracts

Add a domain-level algorithm contract:

```java
public interface IOptimizationAlgorithm {
    OptimizationAlgorithmDescriptor descriptor();

    OptimizationSolution solve(
            OptimizationProblem problem,
            OptimizationAlgorithmOptions options
    );
}
```

Add descriptor metadata:

```java
public record OptimizationAlgorithmDescriptor(
        String key,
        String version,
        Set<OptimizationCapability> capabilities
) {
}
```

Initial algorithm keys:

- `greedy-balanced`: default behavior, equivalent to the current greedy flow.
- `greedy-minimal-reassignment`: future heuristic that strongly prefers current assignees.
- `greedy-skill-first`: future heuristic that weights required and preferred skill fit more heavily.
- `greedy-deadline-first`: future heuristic that weights due date and late risk more heavily.
- `solver-v1`: future solver-backed implementation using the same solution contract.

`OptimizationProjectModel` can initially be reused as the problem model, but the target naming should be `OptimizationProblem`. The problem model should contain normalized facts only, not algorithm policy.

`OptimizationGenerationResult` can initially be adapted into `OptimizationSolution`, but the target solution should include algorithm metadata:

```java
public record OptimizationSolution(
        Map<Long, OptimizationAssignmentSuggestion> assignmentSuggestions,
        Map<Long, OptimizationScheduleSuggestion> scheduleSuggestions,
        List<OptimizationConstraintViolation> warnings,
        OptimizationRunSummary summary,
        OptimizationAlgorithmDescriptor algorithm,
        String solverStatus,
        BigDecimal objectiveScore
) {
}
```

## API Changes

Extend generate requests with an explicit algorithm key:

```java
public class GenerateOptimizationRunRequest {
    private String scope;
    private String algorithmKey;
    private OptimizationMode mode;
    private Long planningStart;
    private Long planningEnd;
    private Boolean allowReassignment;
    private Boolean allowScheduleChanges;
    private List<Long> selectedWorkItemIds;
}
```

Compatibility rule:

- If `algorithmKey` is provided, use it.
- If `algorithmKey` is missing, map the existing `mode` to `greedy-balanced` behavior.
- Keep `mode` in request and response temporarily for existing clients.

Extend review response with algorithm metadata:

```java
private String algorithmKey;
private String algorithmVersion;
private String solverStatus;
private BigDecimal objectiveScore;
```

## Schema Changes

Add columns to `optimization_runs`:

```sql
ALTER TABLE optimization_runs
    ADD COLUMN algorithm_key VARCHAR(100) NOT NULL DEFAULT 'greedy-balanced',
    ADD COLUMN algorithm_version VARCHAR(50) NOT NULL DEFAULT 'v1',
    ADD COLUMN solver_status VARCHAR(50) NOT NULL DEFAULT 'FEASIBLE',
    ADD COLUMN objective_score NUMERIC(18, 6);
```

Do not add `solution_json` in the first step. Run items, warnings, and summary already persist the useful review data. Raw solution storage can be added later if solver debugging or audit requirements justify the extra storage.

## Solution Validation

Add `OptimizationSolutionValidator` after algorithm execution and before persistence.

The validator should check algorithm-independent correctness:

- Every selected work item has a solution item or an explicit skip warning.
- Suggested assignee is a known candidate, or the current assignee when reassignment is disabled.
- `allowReassignment=false` prevents assignment changes.
- `allowScheduleChanges=false` and assignment-only mode prevent schedule suggestions.
- Schedule ranges satisfy `plannedStart < plannedEnd`.
- Schedule starts no earlier than the planning start.
- Internal dependencies are respected: successor starts after predecessor ends.
- External dependency earliest starts are respected.
- Required skill gaps are recorded as warnings by default.
- Capacity overrun remains a warning and low-confidence signal by default, so heuristic algorithms can still return a least-bad solution.

The validator should initially convert most violations into warnings instead of rejecting the whole run. Hard rejection can be introduced later per policy.

## Apply Flow

Keep the existing apply safety model:

- Apply uses persisted run item decisions.
- Apply does not trust final assignee or schedule details from the request body.
- Apply still checks project status, stale work item snapshots, stale plan snapshots, locked plans, permissions, and issue security.
- Apply remains independent of the algorithm that generated the run.

Default decision behavior:

- Keep actionable but risky suggestions as `PENDING` with warnings for review.
- Use `REJECTED` only for suggestions that are impossible to apply.

## Scheduling Accuracy Fixes

The current scheduling model stores only `plannedStart` and `plannedEnd`. That is not enough to measure effort accurately when a task spans multiple capacity slots or days.

Near-term fixes:

- Add `allocatedEffortMillis` to schedule suggestions, or keep allocation details internally while persisting the resulting planned window.
- Summary workload and overload metrics should use allocated effort or duration, not `plannedEnd - plannedStart`.
- Add regression coverage for a task that starts in the middle of a slot.
- Add regression coverage for a task spanning multiple slots.
- Add regression coverage for chained dependencies.

One known risk in the current capacity allocation is that slot availability can be overcounted when `earliestStart` falls inside a slot. The scheduler should not allocate capacity before `earliestStart` for that item.

## Refactor Steps

### Step 1: Introduce Algorithm Contract

- Add `IOptimizationAlgorithm`, descriptor, registry, options, and solution model.
- Wrap existing greedy behavior as `greedy-balanced`.
- Add `algorithmKey` to the generate request.
- Add algorithm metadata to run entity, model, mapper, migration, and review view.
- Preserve current behavior except for metadata.

### Step 2: Add Solution Validator

- Add validator after algorithm execution and before persistence.
- Convert initial validator findings into warnings unless a suggestion is impossible to persist.
- Cover disabled reassignment, disabled scheduling, invalid schedule ranges, invalid assignees, and dependency violations.

### Step 3: Fix Scheduling Accuracy

- Represent effort separately from calendar window.
- Correct overload and workload summary calculations.
- Add regression tests for mid-slot starts, multi-slot allocation, and dependency chains.

### Step 4: Split Greedy Internals

- Split the greedy implementation into smaller strategies:
  - `AssignmentPolicy`
  - `SchedulingPolicy`
  - `ObjectiveScorer`
  - `OptimizationSummaryBuilder`
- Add the first new heuristic algorithm after the split, preferably `greedy-skill-first` or `greedy-deadline-first`.

## Test Plan

Use focused tests first, then broaden.

Focused commands:

```bash
mvn -Dtest=GreedyOptimizationRunGeneratorTest test
mvn -Dtest=OptimizationProjectModelBuilderTest test
mvn -Dtest=ApplyOptimizationRunCommandHandlerTest test
mvn -Dtest=UpdateOptimizationRunItemDecisionCommandHandlerTest test
```

Add or update tests for:

- Algorithm registry resolves `greedy-balanced`.
- Missing `algorithmKey` falls back to current behavior.
- Run metadata is persisted and appears in review response.
- Solution validator emits warnings for invalid algorithm output.
- Disabled reassignment blocks assignment changes from solution.
- Disabled schedule changes blocks schedule suggestions.
- Schedule calculation handles a mid-slot earliest start.
- Summary overload uses effort, not calendar window length.

Final module check:

```bash
mvn clean test
```

## Rollout Notes

- Keep `mode` in the API until frontend clients switch to `algorithmKey`.
- New DB columns should have defaults so existing rows remain readable.
- The first pull request should avoid behavior changes beyond metadata and registry routing.
- Later pull requests can tighten validator policy and add new algorithms once the contract is stable.

