# PM Optimization Batch Decision Design

## Context

The optimization run review page currently updates decisions one work item at a time through
`PATCH /projects/{projectId}/optimization-runs/{runId}/items/{workItemId}` and only later applies the
selected items with the run-level `Apply` action. That makes large runs slow to review because the user
must repeat accept/reject clicks for every item.

The backend already stores per-item review state on the run items, so the natural fix is to batch the
review decision step while keeping apply as a separate action.

## Scope

Replace the single-work-item decision update flow with a batch decision API and update the PM review UI
to use it for both bulk review and row-level review actions.

In scope:

- Add a batch endpoint for updating decisions on multiple optimization run items in one request.
- Remove the live frontend dependency on the single-work-item decision endpoint.
- Keep override support, but send it through the same batch API.
- Add bulk review actions in the optimization run review page.

Out of scope:

- Changing the run-level `Apply selected` behavior.
- Adding auto-approval rules, presets, or heuristics.
- Changing optimization generation, scoring, or validation logic unrelated to review decisions.

## Backend Contract

Add a new endpoint:

`PATCH /projects/{projectId}/optimization-runs/{runId}/items/decisions`

Request body:

```json
{
  "items": [
    {
      "workItemId": 123,
      "assignmentDecision": "ACCEPTED",
      "scheduleDecision": "REJECTED",
      "overrideAssigneeId": 456,
      "overridePlannedStart": 1714876800000,
      "overridePlannedEnd": 1715308800000
    }
  ]
}
```

Rules:

- `workItemId` is required for every item.
- `assignmentDecision` and `scheduleDecision` remain optional per item.
- Override fields are optional unless the submitted decision is `OVERRIDDEN`.
- The request may contain one item or many items.
- The response remains `OptimizationRunReviewView` wrapped in the standard API envelope so the UI can
  refresh in one pass.

Behavior:

- The batch request replaces the single-item decision endpoint as the supported review contract.
- The backend applies the same validation rules that currently exist for single-item review.
- A request is treated as an atomic review operation: invalid input aborts the request and no partial
  decision state is committed.
- The backend still persists audit warnings for invalid override attempts where the current behavior
  already does so.

## Frontend Behavior

The optimization run review page should keep the current selection model, but add explicit bulk actions
for the active tab:

- `Accept selected`
- `Reject selected`
- `Clear selection`

The row-level `Accept`, `Reject`, and `Override` controls should also use the batch endpoint, with a
single-item payload when the user acts on one row.

The review page should continue to support separate assignment and schedule tabs:

- In the assignment tab, bulk actions update `assignmentDecision`.
- In the schedule tab, bulk actions update `scheduleDecision`.
- `Override` remains row-specific in UX because the override fields are item-specific, but it still uses
  the batch endpoint under the hood.

Selection state should stay on the page so the user can review multiple rows and then apply a single bulk
decision action.

## Data Flow

1. The page loads one optimization run and renders the item table.
2. The user selects one or more rows in the active tab.
3. The user clicks `Accept selected` or `Reject selected`.
4. The frontend sends one batch PATCH request with all selected `workItemId` values and the decision for
   the active mode.
5. The backend validates each requested item, updates the persisted review state, and returns the
   refreshed run view.
6. The frontend replaces its cached run data from the response and keeps the selection model in sync.

## Validation And Error Handling

- Reject duplicate `workItemId` values in the same batch request.
- Reject missing, malformed, or non-positive ids.
- Reject override payloads that do not satisfy the existing candidate or schedule rules.
- Keep the current warning/error shape exposed through the optimization run warnings list.
- Surface validation failures through the existing toast/error message handling on the frontend.

## Files Expected To Change

- `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/OptimizationRunController.java`
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/optimization/dto/request/BatchUpdateOptimizationRunItemDecisionsRequest.java`
- `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/update/...`
- `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/update/...`
- `serp_web/src/modules/pm/api/optimizationApi.ts`
- `serp_web/src/modules/pm/types/optimization.types.ts`
- `serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx`
- `serp_web/src/modules/pm/components/optimization/PMOptimizationRunItemTable.tsx`

## Verification

Backend:

- `./mvnw.cmd -Dtest=... test` for the new batch handler and controller tests.
- `./mvnw.cmd clean compile` if the handler/controller wiring changes materially.

Frontend:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`
