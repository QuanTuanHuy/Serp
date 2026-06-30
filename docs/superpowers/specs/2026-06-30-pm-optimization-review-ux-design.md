# PM Optimization Review UX Design

## Context

The optimization run review page currently splits review work into separate
`Assignment` and `Schedule` tabs. Each tab has its own accept, reject, and
override path. This makes the user review the same work item in two places when
the optimization changes both assignee and schedule.

The page also renders the run overview above the tabs. That pushes the actual
review list below the first viewport, even though the main job after generation
is to review and apply item-level suggestions.

The override editor currently opens as a compact dialog. This works for a simple
assignment override, but schedule allocation chunks need more room to scan and
edit assignee, start, end, and effort values.

## Goals

- Make the first screen action-oriented after an optimization run is generated.
- Let users accept, reject, or override a work item once instead of repeating the
  action separately in assignment and schedule tabs.
- Keep assignment and schedule detail views available for investigation.
- Make schedule chunk editing readable and comfortable.
- Make apply behavior safer by applying only reviewed, actionable changes.
- Preserve the backend decision model: assignment and schedule decisions remain
  separate fields.

## Non-Goals

- Do not change the optimizer algorithm or scoring behavior.
- Do not change the backend apply contract in this UX pass.
- Do not remove assignment and schedule tabs entirely.
- Do not introduce a new frontend test framework.

## UX Direction

Use a hybrid review model.

The default tab becomes `Review`. It shows one row per work item and combines
assignment and schedule information in the same row. `Assignment` and `Schedule`
remain as secondary tabs for lane-specific inspection.

When a user opens an optimization run, no work items are selected by default.
The user can review items inline, or explicitly select rows for bulk actions and
apply.

## Page Structure

The run page has a compact header and action bar above the tabs.

The header keeps:

- Back navigation.
- Run id and status.
- Objective, change scope, and algorithm preset badges.

The action bar keeps:

- `Accept selected`
- `Reject selected`
- `Clear selection`
- `Apply ready`
- `Discard`

`Discard` remains a run-level action and should be visually separated from the
review/apply actions.

The large run overview moves into the `Summary` tab. The first visible content
after the header is the tab list and the `Review` tab content.

## Tabs

### Review

Default tab.

Shows one row per work item. Each row includes:

- Selection checkbox for bulk operations.
- Work item key, summary, status, priority, and issue type.
- Assignment comparison: current assignee to effective suggested or override
  assignee.
- Schedule comparison: current planned range to effective suggested or override
  derived range.
- Assignment decision badge.
- Schedule decision badge.
- Inline actions: `Accept`, `Reject`, `Override`.
- Compact warnings, violations, or reasons.

Inline actions do not require the row checkbox to be selected.

### Summary

Contains the existing `PMOptimizationRunOverview` content and the before/after
metrics currently shown in the summary tab.

### Assignment

Remains available as a lane-specific inspection view. It can reuse the existing
assignment-focused table or a filtered version of the combined review table.

### Schedule

Remains available as a lane-specific inspection view. It should show schedule
range and allocation chunk detail clearly.

### Risks and History

Keep the existing warning and history content.

## Decision Actions

### Meaningful Lane Detection

`Accept` and `Reject` on a work item operate only on lanes that have a real,
applicable change and are enabled by the run change scope.

Assignment has a meaningful change when:

- The run scope allows assignment changes.
- The suggested or override assignee is present.
- The effective target assignee differs from the current assignee.

Schedule has a meaningful change when:

- The run scope allows schedule changes.
- The suggested or override schedule range is present.
- The effective target range differs from the current planned range, or the item
  has allocation chunks that should be applied.

If both lanes are meaningful, a row-level `Accept` sets both decisions to
`ACCEPTED`. If only one lane is meaningful, it sets only that lane. `Reject`
follows the same rule with `REJECTED`.

If no lane is meaningful, the action is disabled or shows a clear toast.

### Bulk Accept and Reject

`Accept selected` and `Reject selected` use the same meaningful-lane rule for
each selected row.

The buttons are disabled when no rows are selected.

If selected rows contain no meaningful lanes, show a toast instead of sending an
empty update request.

## Selection and Apply

No rows are selected by default when the run page opens.

Selection is used for bulk review and apply, not for inline row actions.

`Apply ready` replaces the current `Apply selected` label. It applies only
selected work items that have at least one actionable lane:

- `assignmentDecision` is `ACCEPTED` or `OVERRIDDEN`, or
- `scheduleDecision` is `ACCEPTED` or `OVERRIDDEN`.

The frontend should filter the apply request to only those ready work item ids.
Rejected and pending work items should not be sent to apply.

`Apply ready` is disabled when the selected rows do not contain any actionable
lane. The empty helper text is:

`Select reviewed work items to apply.`

This keeps backend behavior unchanged while making the frontend intent clearer.

## Override Editor

Replace the compact dialog with a right-side full-height sheet.

The sheet has:

- Header with work item key, summary, and current decision badges.
- Scrollable body.
- Sticky footer with `Cancel` and `Save override`.

### Assignment Section

Shows:

- Assignment decision selector.
- Assignee override selector.
- Current assignee.
- Suggested assignee.

### Schedule Section

Shows:

- Schedule decision selector.
- Read-only derived planned range.
- Allocation chunk editor.

`plannedStart` and `plannedEnd` are not directly editable for schedule override.
They are derived from the earliest chunk start and latest chunk end. Chunks are
the source of truth.

The allocation chunk editor uses a wider, readable row layout:

- Assignee.
- Start datetime.
- End datetime.
- Effort.
- Remove action.

Users can add chunks. If there are many chunks, the sheet body scrolls while the
footer remains visible.

The assignee list defaults to relevant users first and keeps the option to show
all project members.

## Data Flow

1. User opens an optimization run.
2. The page defaults to the `Review` tab with no selected rows.
3. User accepts, rejects, or overrides work items inline.
4. Inline and bulk decision updates send separate assignment and schedule fields
   according to meaningful-lane detection.
5. User selects reviewed rows.
6. `Apply ready` sends only selected work item ids with accepted or overridden
   assignment or schedule decisions.
7. Backend apply continues to use persisted decisions and skips pending or
   rejected lanes.

## Error Handling

- If a row action has no meaningful lane, do not send a request and show a short
  toast.
- If bulk accept or reject has no selected rows, show the existing selection
  toast.
- If selected rows have no actionable accepted or overridden lanes, keep
  `Apply ready` disabled.
- If backend apply skips items because of stale data, locked plans, or
  permission rules, keep the existing warning behavior.

## Testing and Verification

Frontend verification:

- `npm run lint`
- `npm run type-check`
- Prettier check for touched PM files

Manual UX checks:

- Opening a run defaults to `Review`.
- No rows are selected initially.
- Row-level accept updates both meaningful lanes in one request.
- Row-level reject updates both meaningful lanes in one request.
- Inline actions work without checkbox selection.
- Bulk accept and reject operate only on selected rows.
- `Apply ready` ignores pending and rejected items.
- Summary content no longer pushes review content below the first viewport.
- The override sheet gives schedule chunks enough vertical room and keeps save
  actions visible.

## Rollout Notes

This is primarily a frontend UX change. It depends on the existing backend
decision fields and the override allocation chunk contract from the optimization
schedule override work. Backend apply already treats only `ACCEPTED` and
`OVERRIDDEN` decisions as actionable, so the frontend should align its labels and
filtering with that behavior.
