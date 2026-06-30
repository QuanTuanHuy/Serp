# PM Optimization Intent and Schedule Override Design

## Context

PM optimization currently exposes both `objective` and `algorithmKey` on the run launch screen. The backend stores both, but the actual greedy behavior is selected by `algorithmKey`; `objective` mostly acts as intent metadata. This makes mismatched combinations possible, such as `SKILL_FIRST` with `greedy-deadline-first`.

Schedule review has a second gap. Generated run items expose `allocationChunks`, but `BatchUpdateOptimizationRunItemDecisionsCommandHandler` can only override `overridePlannedStart` and `overridePlannedEnd`. During apply, overridden schedules intentionally drop allocation chunks, so a user cannot review and apply detailed schedule allocations for one work item.

## Goals

- Make `objective` the primary optimization choice for normal users.
- Derive the greedy algorithm preset from the selected objective on both frontend and backend.
- Replace range-only schedule override with a mini plan editor based on allocation chunks.
- Persist override chunks on the optimization run item and apply them to `work_item_plan_allocations`.
- Keep the change focused on the current greedy optimization flow without a broad algorithm registry refactor.

## Non-Goals

- Do not replace the greedy algorithms with a new solver architecture.
- Do not introduce a new frontend test framework.
- Do not change the existing apply behavior for stale items, locked plans, or permission failures.
- Do not add schema changes unless implementation discovers the existing run item JSON field cannot safely carry override chunks.

## Decisions

### Objective-First Intent

The launch flow treats `objective` as the main input. The frontend derives the algorithm key:

- `BALANCED_WORKLOAD` maps to `greedy-balanced`.
- `SKILL_FIRST` maps to `greedy-skill-first`.
- `DEADLINE_FIRST` maps to `greedy-deadline-first`.
- `MINIMAL_REASSIGNMENT` maps to `greedy-minimal-reassignment`.

The frontend removes algorithm selection from the normal path or shows it as a read-only derived preset. The backend also normalizes the pair so direct API callers cannot persist a mismatched run.

### Schedule Override as Mini Plan Editor

Schedule override is represented by allocation chunks rather than manually entered planned start and end values. Each chunk contains:

- `assigneeId`
- `start`
- `end`
- `effortMillis`

When a schedule decision is `OVERRIDDEN`, the backend derives:

- `overridePlannedStart = min(chunk.start)`
- `overridePlannedEnd = max(chunk.end)`

The derived range is used for dependency checks and apply validation. The chunks are persisted in the run item allocation JSON so the apply handler can create plan allocations.

### Assignee Selection Scope

The frontend defaults the schedule chunk assignee list to candidate or relevant users first, including suggested and current assignees where available. A toggle allows showing all project members. Backend validation must reject malformed assignee ids, while final authorization and security checks remain in the apply flow.

## Backend Contract

`BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision` gains an `overrideAllocationChunks` collection. The REST request gains a matching nested allocation request.

For `scheduleDecision = OVERRIDDEN`:

- `overrideAllocationChunks` is required and cannot be empty.
- Each allocation must have positive `assigneeId`, `start`, `end`, and `effortMillis`.
- Each allocation must satisfy `start < end`.
- Each allocation must stay inside the optimization run planning window.
- The derived planned range must stay inside the optimization run planning window.
- Dependency validation uses the derived planned range.

For non-overridden schedule decisions, override planned fields and override chunks are cleared from the final state.

`ApplyOptimizationRunCommandHandler` changes allocation handling:

- Accepted schedule uses generated persisted chunks.
- Overridden schedule uses override chunks persisted on the run item.
- Apply no longer returns empty plan allocations only because the schedule decision is `OVERRIDDEN`.

## Frontend UX

### Launch Page

The run settings panel keeps `Objective`, `Change scope`, and planning dates. The algorithm picker leaves the normal flow. The selected objective controls the derived algorithm. The summary shows both objective and derived algorithm preset for transparency.

### Review Schedule Tab

The schedule table shows the current and suggested planned range plus allocation chunk detail. This makes split schedule plans visible before the user opens override editing.

### Override Dialog

The override dialog keeps assignment controls and upgrades the schedule area into a mini plan editor:

- Add allocation chunk.
- Remove allocation chunk.
- Edit assignee, start, end, and effort.
- Preview derived planned range from all chunks.
- Prefer candidate/relevant users in assignee selects.
- Toggle to show all project members.

Saving a schedule override sends `scheduleDecision: OVERRIDDEN` and the chunk list. The user no longer has to manually enter `overridePlannedStart` and `overridePlannedEnd` for the schedule path.

## Data Flow

1. User selects objective on the launch page.
2. Frontend derives `algorithmKey` from objective and sends both values.
3. Backend normalizes the objective and algorithm pair before building the run intent.
4. Backend generates and stores schedule allocation chunks as it does today.
5. Review page reads `allocationChunks` and renders schedule detail.
6. User overrides schedule by editing chunks.
7. Backend validates chunks, derives override range, stores override chunks on the run item, and returns the refreshed run review.
8. Apply handler writes the active work item plan and replaces plan allocations with the persisted chunks.

## Error Handling

- Empty override chunks are rejected with a clear invalid override message.
- Invalid chunk fields are rejected before saving decisions.
- Dependency violations continue to record invalid override warnings and reject the update.
- Apply-time stale item, stale plan, locked plan, and permission denial behavior stays unchanged: the affected change is skipped and a warning is recorded.

## Testing Plan

Backend focused tests:

- Update decision derives `overridePlannedStart` and `overridePlannedEnd` from override chunks.
- Update decision rejects empty or invalid override chunks.
- Update decision rejects dependency violations using derived ranges.
- Apply writes allocation rows for overridden schedules.
- Generate or registry normalization keeps objective and algorithm pairs aligned.

Frontend verification:

- Run `npm run lint`.
- Run `npm run type-check`.
- Run `npm run format:check`.
- No frontend unit test command is required because the current repo guide says `serp_web` has no configured test script.

## Rollout Notes

The change is backward-compatible for generated runs because accepted schedules continue reading existing `allocationChunksJson`. Existing range-only schedule overrides should be treated as legacy data: apply can still use their planned start and end, but new override saves require chunks.
