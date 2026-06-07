# PM Workflow Status And Transition Editing Design

## Context

The PM workflow editor currently supports adding existing statuses to a workflow draft and already has API wiring for removing workflow transitions. Users need two workflow settings improvements:

- Remove a transition between two workflow steps reliably from the workflow editor.
- Create a status that does not yet exist and add it to the workflow in one flow.

The backend already exposes these relevant endpoints:

- `POST /statuses`
- `GET /statuses`
- `POST /workflows/{workflowId}/steps`
- `DELETE /workflows/{workflowId}/transitions/{transitionId}`

The frontend already has RTK Query workflow editor hooks and an `AddStepDialog` that only selects existing statuses.

## Approved Approach

Use an inline create-and-add flow in the workflow editor.

The `AddStepDialog` will let users choose either an existing status or create a new status. Creating a new status requires only:

- `name`
- `statusCategoryId`

The frontend generates `statusKey` from `name`, for example `In QA Review` becomes `IN_QA_REVIEW`.

## Frontend Behavior

When user chooses an existing status:

1. Submit `POST /workflows/{workflowId}/steps` with selected `statusId`, `isInitial`, and `isTerminal`.
2. Refetch workflow editor data.
3. Clear workflow validation state.
4. Show existing success or error toast behavior.

When user creates a new status:

1. Submit `POST /statuses` with generated `statusKey`, entered `name`, selected `statusCategoryId`, and null optional fields.
2. Read returned status `id`.
3. Submit `POST /workflows/{workflowId}/steps` with created status id, `isInitial`, and `isTerminal`.
4. Refetch workflow editor data and statuses list.
5. Clear workflow validation state.
6. Show success or error toast behavior.

Transition removal should keep using the existing remove-transition mutation. The frontend should fix the removal path so it reliably resolves the matching draft transition after `ensureDraft()` and exposes a reachable remove action from the current transition UI.

## Backend Behavior

No schema change is needed.

Backend status creation remains tenant-scoped through existing `POST /statuses`. Backend workflow step creation remains responsible for duplicate-status and workflow draft validation. Backend transition removal remains responsible for soft-deleting the transition and related rules.

If generated `statusKey` conflicts with an existing status key, the current backend validation should return an error. The frontend will surface that error through existing toast handling.

## Testing

Backend verification:

- Run focused workflow/status tests if present.
- Run `./mvnw.cmd clean compile` from `pm_core` after backend-impacting changes.

Frontend verification:

- Run `npm run lint` from `serp_web`.
- Run `npm run type-check` from `serp_web`.
- Run `npm run format:check` from `serp_web`.

Manual checks:

- Remove transition between two workflow steps and confirm editor refresh removes it.
- Create new status from workflow editor and confirm it appears as a workflow step.
- Confirm duplicate generated status key shows backend error without adding a workflow step.
