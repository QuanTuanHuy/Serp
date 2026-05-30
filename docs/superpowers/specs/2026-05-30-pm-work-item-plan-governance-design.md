# PM Work Item Plan Source Governance Design

## Context

`WorkItemPlanEntity` is the source of truth for scheduled work in PM.
The current implementation creates active plans from optimization apply only.
The product should keep that rule for now and avoid letting users create plans directly from work item detail.

## Scope

Define the current governance model for work item plans:

- optimization may generate suggestions and apply them into `WorkItemPlanEntity`
- work item detail may display the current plan, but not create or edit it
- calendar must render the plan timeline, not `workItem.dueDate`

Do not add manual plan editing in this iteration.

## Rules

1. `WorkItemPlanEntity` is the schedule source of truth.
2. `workItem.startDate` and `workItem.dueDate` remain business fields, not the execution timeline.
3. `plannedStart` and `plannedEnd` come from `WorkItemPlanEntity`.
4. `source` must be preserved on plan records.
5. Current product behavior only allows `WorkItemPlanSource.OPTIMIZATION`.

## UI Behavior

### Work Item Detail

The detail dialog should show a schedule block with:

- current plan start/end
- plan source
- locked state
- due date as a separate field

If no plan exists, show `Unscheduled`.

### Calendar

The calendar should render plan-backed items by default:

- event range = `plannedStart -> plannedEnd`
- grouping may be by assignee or all assignees
- unscheduled items stay in a separate list or lane
- due date may appear as a warning marker, not as the primary event range

### Optimization

Optimization stays the only path that writes plans in the current release:

- generate run -> review -> apply
- apply persists `WorkItemPlanEntity`
- calendar and detail then reflect the stored plan

## Future Extension

The schema and UI should stay open for later plan sources such as manual scheduling, but that is out of scope now.
Any future manual schedule editor must go through a controlled workflow and set `source` explicitly.

## Acceptance Criteria

- users can see when a work item starts by opening work item detail
- calendar shows schedule time from `WorkItemPlanEntity`
- no UI path creates a plan directly outside optimization apply
- due date remains visible, but does not masquerade as plan start/end
- the design can later accept additional plan sources without breaking the model

