# UC-PM-181 - Create Worklog

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
| **Use Case ID** | UC-PM-181 |
| **Use Case Name** | Create Worklog |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | High |
| **Complexity** | Simple |

### Description

Create a worklog entry under an existing work item in tenant scope using Jira-aligned project authorization. The caller must be able to browse the project and log work on the target work item. If the parent work item is protected by issue security, the caller must also satisfy that security level. The system persists the worklog with `author_id` set to the authenticated user, recalculates the parent work item's rolled-up time tracking fields, and writes a `WORKLOG_CREATED` outbox event in the same transaction.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Logs work performed on a work item |
| System | System | Validates project and work item access, persists worklog, recalculates time tracking totals, and stores outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `WORK_ON_ISSUES`
- Conditional read constraint: if the parent work item has `security_level_id`, caller must satisfy membership in that issue security level
- Permission resolution is grant-only through the project's `permission_scheme_id` and may resolve via `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Target work item exists in tenant scope, belongs to the target project, and is not soft-deleted
5. Caller is granted `BROWSE_PROJECTS` and `WORK_ON_ISSUES` for the target project
6. If the target work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. A new active row is persisted in `worklogs` with `author_id=userId`
2. Parent work item `time_spent` is recalculated as the sum of active worklogs for that work item
3. Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when `time_original_estimate` is present
4. Work item audit fields `updated_at` and `updated_by` are updated as part of the rollup refresh
5. A `WORKLOG_CREATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.worklog.events`
6. Response returns the created worklog and the refreshed work item time-tracking summary

#### Failure Postconditions

1. No worklog row is committed
2. No time-tracking totals are partially updated
3. No outbox event is committed
4. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `POST /api/v1/projects/{projectId}/work-items/{workItemId}/worklogs` with worklog payload |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` and validates it is not archived |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Evaluates `BROWSE_PROJECTS` and `WORK_ON_ISSUES` permissions for the caller |
| 6 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 7 | System | Validates payload: `time_spent >= 60`, `start_date` is not in the future, and `comment` length is within limits |
| 8 | System | Begins database transaction |
| 9 | System | Persists a new worklog with `author_id=userId`, audit fields, and `deleted_at=NULL` |
| 10 | System | Recalculates the parent work item's `time_spent` from active worklogs and refreshes `time_remaining_estimate` from `time_original_estimate` |
| 11 | System | Persists `WORKLOG_CREATED` to domain outbox with worklog and work item identifiers |
| 12 | System | Commits transaction and returns HTTP 201 with created worklog payload |

### Alternative Flows

#### AF-1: Work Item Without Original Estimate

**Branches from**: Main Flow Step 10  
**Condition**: Parent work item has `time_original_estimate=NULL`

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.1 | System | Recalculates `time_spent` normally from active worklogs |
| 10.2 | System | Leaves `time_remaining_estimate=NULL` because no original estimate exists |

**Rejoins**: Main Flow Step 11

#### AF-2: Empty Comment

**Branches from**: Main Flow Step 7  
**Condition**: Request omits `comment` or sends blank comment

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.1 | System | Normalizes blank input to `NULL` or empty-text policy defined by implementation |
| 9.1 | System | Persists the worklog without a comment body |

**Rejoins**: Main Flow Step 10

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Project Permission Denied

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail (`BROWSE_PROJECTS` or `WORK_ON_ISSUES`) |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Invalid Worklog Payload

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | If `time_spent < 60`, returns HTTP 400 with validation error |
| 7.E2 | System | If `start_date` is in the future, returns HTTP 400 with validation error |
| 7.E3 | System | If `comment` exceeds max length, returns HTTP 400 with validation error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-181-01 | Creating a worklog requires `BROWSE_PROJECTS` and `WORK_ON_ISSUES` in the target project | Authorization layer |
| BR-PM-181-02 | If the parent work item has `security_level_id`, caller must satisfy issue-security membership before logging work | Authorization layer |
| BR-PM-181-03 | `author_id` is always the authenticated user and cannot be provided by the client | Service layer |
| BR-PM-181-04 | `time_spent` must be stored in seconds and be at least 60 seconds | Validation layer |
| BR-PM-181-05 | `start_date` cannot be in the future | Validation layer |
| BR-PM-181-06 | Parent work item `time_spent` equals the sum of active worklogs for that work item | Service layer |
| BR-PM-181-07 | Parent work item `time_remaining_estimate` is recalculated as `max(time_original_estimate - time_spent, 0)` when original estimate exists | Service layer |
| BR-PM-181-08 | Worklog create is rejected when the parent project is archived | Service layer |
| BR-PM-181-09 | Domain events use outbox pattern: `WORKLOG_CREATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Parent work item identifier from path |
| time_spent | int64 | Yes | min:60 | Logged work duration in seconds |
| start_date | timestamp | Yes | not in future | When the logged work started |
| comment | string | No | max:5000 | Optional work description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Created worklog ID |
| work_item_id | int64 | Parent work item ID |
| author_id | int64 | Author user ID |
| comment | string | Worklog comment |
| start_date | timestamp | Work start time |
| time_spent | int64 | Logged duration in seconds |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |
| work_item_time_spent | int64 | Recalculated total logged time for the parent work item |
| work_item_time_remaining_estimate | int64 | Recalculated remaining estimate for the parent work item |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user creating the worklog |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
