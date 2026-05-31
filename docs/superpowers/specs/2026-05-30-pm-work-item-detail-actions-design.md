# PM Work Item Detail Actions Design

## Context

`PMWorkItemDetailDialog` currently reads work item details, subtasks, linked work items, comments, and activities. The backend already exposes the write APIs needed for subtask creation, issue links, and worklogs, but the frontend only wires the read side for children and links.

## Scope

Add enough detail-dialog actions for day-to-day work item management:

- Create a subtask from an open work item detail dialog.
- Create and delete linked work item relationships.
- List, create, edit, and delete worklogs.

Do not add new backend APIs unless implementation discovers a contract gap. Prefer existing `pm_core` endpoints.

## Backend Contract

Use existing endpoints:

- `POST /projects/{projectId}/work-items` with `parentId` for subtask creation.
- `GET /issue-link-types` for link type options.
- `GET /projects/{projectId}/work-items/{workItemId}/links`.
- `POST /projects/{projectId}/work-items/{workItemId}/links`.
- `DELETE /projects/{projectId}/work-items/{workItemId}/links/{linkId}`.
- `GET /projects/{projectId}/work-items/{workItemId}/worklogs`.
- `POST /projects/{projectId}/work-items/{workItemId}/worklogs`.
- `PUT /projects/{projectId}/work-items/{workItemId}/worklogs/{worklogId}`.
- `DELETE /projects/{projectId}/work-items/{workItemId}/worklogs/{worklogId}`.

Worklog `timeSpent` is stored and sent in seconds. The UI may accept minutes and convert to seconds.

## Frontend Design

Keep `PMWorkItemDetailDialog.tsx` as the container and split action surfaces into focused detail components:

- `PMWorkItemSubtaskActions.tsx`: renders subtask creation action and opens the existing create work item dialog with fixed project and default parent.
- `PMWorkItemLinkActions.tsx`: renders link type selection, target work item search, create action, and per-link delete actions.
- `PMWorkItemWorklogPanel.tsx`: renders worklog list plus create, edit, and delete flows.

Extend `workItemApi.ts` and `work-item-api.types.ts` with the missing link type, issue link, and worklog hooks/types. Add `pm/WorkItemWorklogs` to the base RTK Query tag list.

## Cache Behavior

Subtask creation invalidates:

- `pm/WorkItem` for the parent work item.
- `pm/WorkItemChildren` for the parent work item.
- `pm/WorkItemActivities` for the parent work item.
- `pm/WorkItem` list.

Link create/delete invalidates:

- `pm/WorkItem` for the current work item.
- `pm/WorkItemLinks` for the current work item.
- `pm/WorkItemActivities` for the current work item.

Worklog create/update/delete invalidates:

- `pm/WorkItem` for refreshed time tracking.
- `pm/WorkItemWorklogs` for the current work item.
- `pm/WorkItemActivities` for the current work item.

## UI Behavior

Subtasks:

- Add a compact create button in the Subtasks section.
- Reuse the current create work item dialog where possible.
- Default the parent to the current work item and keep project fixed.

Linked work items:

- Provide an add action that opens a compact form.
- Search target work items in the same project and exclude the current work item.
- Require both target work item and link type before submit.
- Show delete action on each existing link.

Worklogs:

- Add a Work logs section near Activity.
- Show time spent, start date, comment, and created/updated metadata when available.
- Support create/edit/delete inline or with compact forms.
- Accept time input in minutes and convert to backend seconds.

## Verification

Frontend verification:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

Backend verification:

- Run `./mvnw.cmd clean compile` in `pm_core/` only if backend code changes become necessary.
