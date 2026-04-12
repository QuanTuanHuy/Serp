# UC-PM-183 - Delete Worklog

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
| **Use Case ID** | UC-PM-183 |
| **Use Case Name** | Delete Worklog |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Soft-delete a single worklog entry in tenant scope using Jira-aligned project authorization. The caller must be able to browse the project and must either hold the global worklog delete permission for the project or hold the own-worklog delete permission and be the worklog author. If the parent work item is protected by issue security, the caller must also satisfy that security level. The system soft-deletes the worklog, recalculates the parent work item's rolled-up time-tracking fields, and writes a `WORKLOG_DELETED` outbox event.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Deletes a worklog they authored or are authorized to manage |
| System | System | Validates scope and authorization, soft-deletes worklog, recalculates work item time totals, and stores outbox event |

### Authorization (Jira Project Permissions)

- Baseline permission: `BROWSE_PROJECTS`
- Delete permission: caller must satisfy either `DELETE_ALL_WORKLOGS`, or `DELETE_OWN_WORKLOGS` and `author_id=userId`
- Conditional read constraint: if the parent work item has `security_level_id`, caller must satisfy membership in that issue security level

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Target work item exists in tenant scope, belongs to the target project, and is not soft-deleted
5. Target worklog exists in tenant scope, belongs to the target work item, and is not soft-deleted
6. Caller is granted `BROWSE_PROJECTS` for the target project
7. Caller is granted `DELETE_ALL_WORKLOGS`, or is the worklog author and is granted `DELETE_OWN_WORKLOGS`
8. If the parent work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. Target worklog row has `deleted_at` set and is excluded from active reads
2. Parent work item `time_spent` is recalculated as the sum of active worklogs for that work item
3. Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when `time_original_estimate` is present
4. Parent work item audit fields `updated_at` and `updated_by` are updated as part of the rollup refresh
5. A `WORKLOG_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.worklog.events`
6. Response returns deletion confirmation and the refreshed work item time-tracking summary

#### Failure Postconditions

1. No partial delete is committed
2. No partial rollup update is committed
3. No outbox event is committed
4. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `DELETE /api/v1/projects/{projectId}/work-items/{workItemId}/worklogs/{worklogId}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` and validates it is not archived |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Loads worklog by `worklogId` and validates it belongs to the target work item |
| 6 | System | Evaluates `BROWSE_PROJECTS` for the caller |
| 7 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 8 | System | Evaluates worklog delete authorization: `DELETE_ALL_WORKLOGS` or (`DELETE_OWN_WORKLOGS` and caller is the author) |
| 9 | System | Begins database transaction |
| 10 | System | Soft-deletes the worklog by setting `deleted_at`, `updated_at`, and `updated_by` |
| 11 | System | Recalculates the parent work item's `time_spent` and `time_remaining_estimate` from active worklogs |
| 12 | System | Persists `WORKLOG_DELETED` to domain outbox with worklog and work item identifiers |
| 13 | System | Commits transaction and returns HTTP 200 with deletion confirmation payload |

### Alternative Flows

#### AF-1: Work Item Without Original Estimate

**Branches from**: Main Flow Step 11  
**Condition**: Parent work item has `time_original_estimate=NULL`

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Recalculates `time_spent` normally from remaining active worklogs |
| 11.2 | System | Leaves `time_remaining_estimate=NULL` because no original estimate exists |

**Rejoins**: Main Flow Step 12

#### AF-2: Deleting the Last Active Worklog

**Branches from**: Main Flow Step 11  
**Condition**: No active worklogs remain for the parent work item after delete

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Sets parent work item `time_spent=0` |
| 11.2 | System | Sets parent work item `time_remaining_estimate` to the original estimate when present |

**Rejoins**: Main Flow Step 12

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

#### EF-5: Worklog Delete Not Allowed

**Triggered at**: Main Flow Step 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.E1 | System | If caller lacks both delete permissions, returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` |
| 8.E2 | System | If caller has only `DELETE_OWN_WORKLOGS` but is not the author, returns HTTP 403 with error: `WORKLOG_NOT_OWNER` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-183-01 | Deleting a worklog requires `BROWSE_PROJECTS` and either `DELETE_ALL_WORKLOGS` or `DELETE_OWN_WORKLOGS` for the author | Authorization layer |
| BR-PM-183-02 | If the parent work item has `security_level_id`, caller must satisfy issue-security membership before deleting the worklog | Authorization layer |
| BR-PM-183-03 | Delete is soft delete only (`deleted_at`), never physical row removal | Service layer |
| BR-PM-183-04 | Parent work item `time_spent` equals the sum of active worklogs for that work item after delete | Service layer |
| BR-PM-183-05 | Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when original estimate exists | Service layer |
| BR-PM-183-06 | Worklog delete is rejected when the parent project is archived | Service layer |
| BR-PM-183-07 | Active reads and list operations exclude soft-deleted worklogs (`deleted_at IS NULL`) | Repository layer |
| BR-PM-183-08 | Domain events use outbox pattern: `WORKLOG_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Parent work item identifier from path |
| worklogId | int64 | Yes | min:1 | Worklog identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| worklog_id | int64 | Deleted worklog ID |
| deleted_at | timestamp | Soft-delete timestamp |
| deleted_by | int64 | User who performed the delete |
| work_item_id | int64 | Parent work item ID |
| work_item_time_spent | int64 | Recalculated total logged time for the parent work item |
| work_item_time_remaining_estimate | int64 | Recalculated remaining estimate for the parent work item |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user deleting the worklog |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
