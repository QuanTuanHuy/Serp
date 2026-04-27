# UC-PM-182 - Update Worklog

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-182 |
| **Use Case Name** | Update Worklog |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update a single worklog entry in tenant scope using Jira-aligned project authorization. The caller must be able to browse the project and must either hold the global worklog edit permission for the project or hold the own-worklog edit permission and be the worklog author. If the parent work item is protected by issue security, the caller must also satisfy that security level. The system updates editable worklog fields, recalculates the parent work item's rolled-up time-tracking fields, and writes a `WORKLOG_UPDATED` outbox event.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Edits a worklog they authored or are authorized to manage |
| System | System | Validates scope and authorization, updates worklog, recalculates work item time totals, and stores outbox event |

### Authorization (Jira Project Permissions)

- Baseline permission: `BROWSE_PROJECTS`
- Edit permission: caller must satisfy either `EDIT_ALL_WORKLOGS`, or `EDIT_OWN_WORKLOGS` and `author_id=userId`
- Conditional read constraint: if the parent work item has `security_level_id`, caller must satisfy membership in that issue security level

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Target work item exists in tenant scope, belongs to the target project, and is not soft-deleted
5. Target worklog exists in tenant scope, belongs to the target work item, and is not soft-deleted
6. Caller is granted `BROWSE_PROJECTS` for the target project
7. Caller is granted `EDIT_ALL_WORKLOGS`, or is the worklog author and is granted `EDIT_OWN_WORKLOGS`
8. If the parent work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. Editable worklog fields (`time_spent`, `start_date`, `comment`) are updated
2. Worklog audit fields `updated_at` and `updated_by` are updated
3. Parent work item `time_spent` is recalculated as the sum of active worklogs for that work item
4. Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when `time_original_estimate` is present
5. A `WORKLOG_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.worklog.events`
6. Response returns the updated worklog and the refreshed work item time-tracking summary

#### Failure Postconditions

1. No partial worklog update is committed
2. No partial rollup update is committed
3. No outbox event is committed
4. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `PUT /api/v1/projects/{projectId}/work-items/{workItemId}/worklogs/{worklogId}` with editable worklog fields |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` and validates it is not archived |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Loads worklog by `worklogId` and validates it belongs to the target work item |
| 6 | System | Evaluates `BROWSE_PROJECTS` for the caller |
| 7 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 8 | System | Evaluates worklog edit authorization: `EDIT_ALL_WORKLOGS` or (`EDIT_OWN_WORKLOGS` and caller is the author) |
| 9 | System | Validates payload: `time_spent >= 60`, `start_date` is not in the future, and `comment` length is within limits |
| 10 | System | Begins database transaction |
| 11 | System | Updates the worklog row and audit fields |
| 12 | System | Recalculates the parent work item's `time_spent` and `time_remaining_estimate` from active worklogs |
| 13 | System | Persists `WORKLOG_UPDATED` to domain outbox with worklog and work item identifiers |
| 14 | System | Commits transaction and returns HTTP 200 with updated worklog payload |

### Alternative Flows

#### AF-1: Clear Comment

**Branches from**: Main Flow Step 9-11  
**Condition**: Request explicitly sends `comment=null` or blank comment

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Treats the comment field as explicitly provided |
| 11.1 | System | Clears the persisted comment according to the implementation's null-or-empty policy |

**Rejoins**: Main Flow Step 12

#### AF-2: Work Item Without Original Estimate

**Branches from**: Main Flow Step 12  
**Condition**: Parent work item has `time_original_estimate=NULL`

| Step | Actor/System | Action |
|------|-------------|--------|
| 12.1 | System | Recalculates `time_spent` normally from active worklogs |
| 12.2 | System | Leaves `time_remaining_estimate=NULL` because no original estimate exists |

**Rejoins**: Main Flow Step 13

### Exception Flows

#### EF-1: Work Item or Worklog Not Found

**Triggered at**: Main Flow Step 4-5

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |
| 5.E1 | System | Returns HTTP 404 with error: `WORKLOG_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Browse Permission Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission `BROWSE_PROJECTS` |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Worklog Edit Not Allowed

**Triggered at**: Main Flow Step 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.E1 | System | If caller lacks both edit permissions, returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` |
| 8.E2 | System | If caller has only `EDIT_OWN_WORKLOGS` but is not the author, returns HTTP 403 with error: `WORKLOG_NOT_OWNER` |

#### EF-6: Invalid Worklog Payload

**Triggered at**: Main Flow Step 9

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.E1 | System | If `time_spent < 60`, returns HTTP 400 with validation error |
| 9.E2 | System | If `start_date` is in the future, returns HTTP 400 with validation error |
| 9.E3 | System | If `comment` exceeds max length, returns HTTP 400 with validation error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-182-01 | Updating a worklog requires `BROWSE_PROJECTS` and either `EDIT_ALL_WORKLOGS` or `EDIT_OWN_WORKLOGS` for the author | Authorization layer |
| BR-PM-182-02 | If the parent work item has `security_level_id`, caller must satisfy issue-security membership before editing the worklog | Authorization layer |
| BR-PM-182-03 | Only `time_spent`, `start_date`, and `comment` are editable in UC-PM-182 v1 | UseCase layer |
| BR-PM-182-04 | `author_id` and `work_item_id` are immutable through this use case | Service layer |
| BR-PM-182-05 | Parent work item `time_spent` equals the sum of active worklogs for that work item after update | Service layer |
| BR-PM-182-06 | Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when original estimate exists | Service layer |
| BR-PM-182-07 | Worklog update is rejected when the parent project is archived | Service layer |
| BR-PM-182-08 | Domain events use outbox pattern: `WORKLOG_UPDATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Parent work item identifier from path |
| worklogId | int64 | Yes | min:1 | Worklog identifier from path |
| time_spent | int64 | Yes | min:60 | Logged work duration in seconds |
| start_date | timestamp | Yes | not in future | When the logged work started |
| comment | string | No | max:5000 | Optional work description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Updated worklog ID |
| work_item_id | int64 | Parent work item ID |
| author_id | int64 | Author user ID |
| comment | string | Updated comment |
| start_date | timestamp | Updated work start time |
| time_spent | int64 | Updated logged duration in seconds |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |
| work_item_time_spent | int64 | Recalculated total logged time for the parent work item |
| work_item_time_remaining_estimate | int64 | Recalculated remaining estimate for the parent work item |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user updating the worklog |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
