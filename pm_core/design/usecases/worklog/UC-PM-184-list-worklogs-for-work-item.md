# UC-PM-184 - List Worklogs for Work Item

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
| **Use Case ID** | UC-PM-184 |
| **Use Case Name** | List Worklogs for Work Item |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Retrieve a paginated list of active worklogs for a single work item in tenant scope. Read access follows Jira-style project authorization: caller must satisfy `BROWSE_PROJECTS` for the target project, and if the parent work item is protected by `security_level_id`, caller must also satisfy membership in that issue security level. The response returns worklog rows ordered deterministically together with pagination metadata and current parent work item time-tracking totals.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Reads worklog history for a visible work item |
| System | System | Resolves project and work item access, applies pagination and sorting, and returns active worklogs |

### Authorization (Jira Project Permissions)

- Baseline permission: `BROWSE_PROJECTS`
- Conditional read constraint: if the parent work item has `security_level_id`, caller must satisfy membership in that issue security level

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists in tenant scope
4. Target work item exists in tenant scope, belongs to the target project, and is not soft-deleted
5. Caller is granted `BROWSE_PROJECTS` for the target project
6. If the parent work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. System returns HTTP 200 with paginated active worklogs for the target work item
3. Response includes current parent work item `time_spent` and `time_remaining_estimate`

#### Failure Postconditions

1. No data mutation occurs
2. System returns an authorization, validation, or not-found error with details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `GET /api/v1/projects/{projectId}/work-items/{workItemId}/worklogs` with pagination and sort query parameters |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Evaluates `BROWSE_PROJECTS` for the caller |
| 6 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 7 | System | Parses and validates pagination and sort parameters |
| 8 | System | Queries active worklogs for the target work item with `deleted_at IS NULL`, tenant scoping, and requested sort order |
| 9 | System | Returns HTTP 200 with paginated worklog list and current work item time-tracking totals |

### Alternative Flows

#### AF-1: Empty Worklog History

**Branches from**: Main Flow Step 8  
**Condition**: Target work item has no active worklogs

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Returns an empty `items` array and `totalItems=0` |
| 9.1 | System | Includes current work item time-tracking totals for the parent work item |

**Rejoins**: Main Flow Step 9

#### AF-2: Author Filter Applied

**Branches from**: Main Flow Step 7-8  
**Condition**: Request includes `author_id` filter

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.1 | System | Validates `author_id` format |
| 8.1 | System | Adds `author_id` predicate to the active worklog query |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Browse Permission Denied

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission `BROWSE_PROJECTS` |

#### EF-3: Issue Security Access Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-4: Invalid Query Parameters

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | If `page < 0` or `pageSize` is outside supported range, returns HTTP 400 with validation error |
| 7.E2 | System | If `sortBy` or `sortDirection` is unsupported, returns HTTP 400 with validation error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-184-01 | Listing worklogs requires `BROWSE_PROJECTS` on the target project | Authorization layer |
| BR-PM-184-02 | If the parent work item has `security_level_id`, caller must satisfy issue-security membership before viewing worklogs | Authorization layer |
| BR-PM-184-03 | List results are tenant-scoped and exclude soft-deleted worklogs (`deleted_at IS NULL`) | Repository layer |
| BR-PM-184-04 | Default pagination is `page=0`, `pageSize=20`, and max `pageSize=100` | Controller or query validation layer |
| BR-PM-184-05 | Default sort is `start_date DESC`, then `id DESC` for deterministic ordering | Repository layer |
| BR-PM-184-06 | List operation is side-effect free and must not update audit or work item rollup fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Parent work item identifier from path |
| page | int | No | min:0, default:0 | Page number (0-indexed) |
| pageSize | int | No | min:1, max:100, default:20 | Items per page |
| author_id | int64 | No | min:1 | Optional filter by worklog author |
| sortBy | string | No | `start_date`, `time_spent`, `created_at` | Sort field |
| sortDirection | string | No | `ASC`, `DESC` | Sort direction |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| items | Worklog[] | Paginated active worklogs |
| totalItems | int64 | Total matching worklogs |
| totalPages | int | Total pages |
| currentPage | int | Current page |
| pageSize | int | Requested page size |
| work_item_id | int64 | Parent work item ID |
| work_item_time_spent | int64 | Current total logged time for the parent work item |
| work_item_time_remaining_estimate | int64 | Current remaining estimate for the parent work item |

#### Worklog Item Shape

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Worklog ID |
| author_id | int64 | Author user ID |
| comment | string | Worklog comment |
| start_date | timestamp | Work start time |
| time_spent | int64 | Logged duration in seconds |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user reading the worklog list |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
