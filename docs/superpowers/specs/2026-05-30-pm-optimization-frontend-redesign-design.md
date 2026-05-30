# PM Optimization Frontend Redesign Design

## Context

The PM optimization backend has already moved to a clean input model:

- generate requests use `objective`, `changeScope`, and `algorithmKey`
- persisted runs store `objective` and `changeScope`
- review responses expose clean metadata instead of legacy `mode` and flags
- algorithms are already split behind a strategy contract

The current `serp_web` optimization UI still reflects the old model:

- launch page uses `mode`, `allowReassignment`, and `allowScheduleChanges`
- review page renders legacy run metadata
- the launch page and review page are not visually or behaviorally separated enough

This redesign makes the optimization feature easier to understand for occasional users while staying compatible with the backend intent model.

## Goals

- Make optimization feel guided, not dense.
- Keep launch and review as separate mental steps.
- Expose `algorithmKey` at launch without making the page heavy.
- Make review the main place for decision-making, override, apply, and discard.
- Align the frontend contract with the backend clean input model.
- Keep the UI useful for both assignment and scheduling runs.

## Non-Goals

- Do not redesign unrelated PM pages.
- Do not add a multi-step wizard.
- Do not introduce new optimization algorithms in the UI.
- Do not add extra backend endpoints.
- Do not redesign the work item search page beyond what optimization launch needs.

## Product Shape

The feature will consist of two first-class pages:

1. `PMProjectOptimizationPage` for launch
2. `PMProjectOptimizationRunPage` for review

The navigation model stays simple:

- user selects work items from a project context
- user configures the run on launch page
- user lands on review page after generate
- user accepts, overrides, applies, or discards from review page

## Shared Input Model

Frontend request and response types will mirror backend clean input.

### Generate request

Replace legacy launch fields with:

- `algorithmKey`
- `objective`
- `changeScope`
- `planningStart`
- `planningEnd`
- `selectedWorkItemIds`

Remove from the UI contract:

- `mode`
- `allowReassignment`
- `allowScheduleChanges`

### Review response

Expose:

- `objective`
- `changeScope`
- `algorithmKey`
- `algorithmVersion`
- `solverStatus`
- `objectiveScore`

Remove from the review UI contract:

- `mode`
- `allowReassignment`
- `allowScheduleChanges`

## Launch Page Design

### Purpose

The launch page should help the user create a valid run with as few decisions as possible while still letting them choose solver strategy.

### Layout

Use a two-column layout:

- left: searchable work item selection list
- right: run settings and selection summary

The top section should contain:

- back navigation
- page title
- short helper copy
- primary `Generate run` action

### Left Column: Work Item Selection

The work item list should be dense and scan-friendly.

Each row should show:

- checkbox
- work item key
- summary
- assignee
- priority
- status
- compact badge for issue type or rank if available

The selection area should support:

- search by keyword
- select visible items
- clear selection
- quick count of total selected items

The selected items summary should remain visible on the right side and show:

- key
- summary
- status

### Right Column: Run Settings

The run settings panel should be grouped in this order:

1. `objective`
2. `changeScope`
3. `algorithmKey`
4. planning range
5. selection summary / constraints

#### Objective

Show as radio cards with short labels and short supporting text.

Recommended objectives:

- `BALANCED_WORKLOAD`
- `MINIMAL_REASSIGNMENT`
- `SKILL_FIRST`
- `DEADLINE_FIRST`

The objective selection should explain what the solver tries to optimize, not what it is allowed to change.

#### Change Scope

Show as radio cards or segmented options with clear scope labels:

- `ASSIGNMENT_ONLY`
- `SCHEDULE_ONLY`
- `ASSIGNMENT_AND_SCHEDULE`

This is a first-class setting, not a checkbox pair.

The UI should explain the difference between:

- what the solver is allowed to change
- what the objective prefers

#### Algorithm Key

Show `algorithmKey` as a dropdown.

Recommended defaults:

- `greedy-balanced` as the default selection

The control should stay visible on the launch page because the user explicitly wants solver choice available.

The dropdown should include a short description for each algorithm if metadata exists, but not long prose.

#### Planning Range

Use date inputs for start and end.

Validation should block generation when:

- start is missing
- end is missing
- start is not before end

#### Selection Summary

Show a compact summary card or panel with:

- selected item count
- current scope
- chosen objective
- chosen algorithm
- planning range

This summary should help the user confirm the run before clicking generate.

### Launch Page Behavior

- `Generate run` is disabled until at least one work item is selected.
- If the user changes the search filter, selection is preserved.
- Search should not reset the current selection.
- The page should avoid a wizard feel; all controls remain visible on one screen.
- If a selected work item is not visible in the current filter, it still counts toward the final run.
- If the chosen algorithm is incompatible with the selected `changeScope`, the UI should show a clear error before submit where possible.

### Launch Page Error Handling

The page should surface:

- invalid project id
- empty selection
- invalid date range
- backend validation errors from generate

Errors should be shown inline or as toast notifications depending on scope:

- field-level issues stay near the field
- submission failure uses toast with backend message

## Review Page Design

### Purpose

The review page is the working surface for interpreting the generated run and deciding what to apply.

It should help the user answer:

- what did the solver do
- what changed
- what is risky
- what should be applied

### Layout

Use a strong header, a metrics strip, and a tabbed content region.

The top-level structure should be:

1. header with back link and primary actions
2. metric row
3. run metadata and summary
4. tabbed decision area

### Header

Show:

- run title
- run id
- status badge
- objective
- change scope
- algorithm key

Primary actions:

- `Apply selected`
- `Discard`

The header should make it obvious whether the run is still reviewable, partially applied, applied, or discarded.

### Metrics Strip

Show small high-signal metrics such as:

- scope size
- assignment suggestion count
- scheduled item count
- warnings count
- confidence
- objective score

These metrics should come before the tabs because they help users decide where to inspect first.

### Metadata Panel

Show clean metadata fields:

- objective
- change scope
- planning start
- planning end
- algorithm key
- algorithm version
- solver status
- created / updated info

Do not show legacy mode or flags.

### Tabs

Use tabs to avoid overloading the page:

- `Summary`
- `Assignment`
- `Schedule`
- `Risks`
- `History`

#### Summary Tab

Show:

- run summary metrics before/after
- key highlights
- compact before/after comparison

This tab should answer the high-level question: was the run worthwhile?

#### Assignment Tab

Show assignment decision rows only.

Each row should include:

- checkbox for apply selection
- work item key
- decision badge
- current assignee
- suggested assignee
- score / cost / confidence
- reasons
- violations
- action buttons: accept, reject, override

#### Schedule Tab

Show schedule decision rows only.

Each row should include:

- checkbox for apply selection
- work item key
- decision badge
- current dates
- suggested dates
- score / confidence
- reasons
- violations
- action buttons: accept, reject, override

#### Risks Tab

Show warnings grouped by severity and code.

The user should be able to scan:

- error warnings
- warnings with stale or locked plans
- informational notes

#### History Tab

Show audit metadata and status transitions:

- created at / by
- updated at / by
- applied at / by
- discarded at / by if present

### Apply Flow

Apply should work on the selected row set only.

Rules:

- `Apply selected` uses the selected work item ids from the review page.
- The page should default to selecting all items when a new run is loaded.
- After apply, refresh the run view immediately.
- If some items are skipped because of stale data, locked plans, or permission rules, the run should move to `PARTIALLY_APPLIED`.

### Override Flow

Override should stay in a dialog, not inline.

The dialog should support:

- assignment decision
- schedule decision
- override assignee
- override planned start / end

The dialog should only present the fields relevant to the current item and current decision state.

### Review Page Behavior

- Summary and metrics load before deep item detail.
- The page should preserve selection while refetching after actions.
- Decision updates should revalidate the current run after save.
- The UI should surface stale/locked/permission feedback clearly and not hide it behind a generic failure message.
- If the run is only assignment or only schedule scoped, the inactive side should be visually de-emphasized or disabled where appropriate.

## Backend Mapping

The frontend must map cleanly to the backend behavior.

### Generate

The generated request should be sent directly to:

- `POST /pm/api/v1/projects/{projectId}/optimization-runs`

Payload must match the clean input model and no longer include legacy fields.

### Review

The review page should consume the generated response from:

- `GET /pm/api/v1/projects/{projectId}/optimization-runs/{runId}`

### Update / Apply / Discard

The review page should keep using:

- `PATCH /pm/api/v1/projects/{projectId}/optimization-runs/{runId}/items/{workItemId}`
- `POST /pm/api/v1/projects/{projectId}/optimization-runs/{runId}/apply`
- `POST /pm/api/v1/projects/{projectId}/optimization-runs/{runId}/discard`

## Acceptance Criteria

The redesign is complete when:

- launch page uses `objective`, `changeScope`, and `algorithmKey`
- review page no longer renders legacy mode/flags
- launch and review pages feel like separate but connected steps
- a user can generate a run, inspect it, override decisions, apply selected items, and discard the run without confusion
- the UI matches backend clean input semantics

## Testing

Minimum verification:

```bash
npm run type-check
npm run lint
npm run format:check
```

If route composition or data flow changes, also run:

```bash
npm run build
```

## Rollout Notes

This is a frontend contract migration, not a backend behavior change.

The rollout should happen in this order:

1. update frontend types
2. update launch page
3. update review page
4. verify route and request mapping against backend

The backend is already aligned with clean input and does not need additional compatibility shims for the UI redesign.
