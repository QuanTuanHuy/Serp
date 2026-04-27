# UC-PM-185 - Get Worklog by ID

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
| **Use Case ID** | UC-PM-185 |
| **Use Case Name** | Get Worklog by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one active worklog by ID within the scope of its parent work item and project. Read access follows Jira-style project authorization: caller must satisfy `BROWSE_PROJECTS` for the target project, and if the parent work item is protected by `security_level_id`, caller must also satisfy membership in that issue security level. The response returns the canonical worklog view together with parent work item time-tracking totals.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Reads a single visible worklog |
| System | System | Resolves scoped worklog lookup, permission checks, issue-security access, and response enrichment |

### Authorization (Jira Project Permissions)

- Baseline permission: `BROWSE_PROJECTS`
- Conditional read constraint: if the parent work item has `security_level_id`, caller must satisfy membership in that issue security level

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists in tenant scope
4. Target work item exists in tenant scope, belongs to the target project, and is not soft-deleted
5. Target worklog exists in tenant scope, belongs to the target work item, and is not soft-deleted
6. Caller is granted `BROWSE_PROJECTS` for the target project
7. If the parent work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. System returns HTTP 200 with one worklog detail payload
3. Response includes current parent work item `time_spent` and `time_remaining_estimate`

#### Failure Postconditions

1. No data mutation occurs
2. System returns an authorization or not-found error with details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `GET /api/v1/projects/{projectId}/work-items/{workItemId}/worklogs/{worklogId}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Evaluates `BROWSE_PROJECTS` for the caller |
| 6 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 7 | System | Loads worklog by `worklogId` and validates it belongs to the target work item |
| 8 | System | Returns HTTP 200 with worklog detail and current parent work item time-tracking totals |

### Alternative Flows

#### AF-1: Worklog Without Comment

**Branches from**: Main Flow Step 8  
**Condition**: Stored worklog has no comment

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Returns `comment=null` or empty string according to the persisted representation |

**Rejoins**: End use case

#### AF-2: Parent Work Item Without Original Estimate

**Branches from**: Main Flow Step 8  
**Condition**: Parent work item has `time_original_estimate=NULL`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Returns current `time_spent` normally |
| 8.2 | System | Returns `work_item_time_remaining_estimate=NULL` |

**Rejoins**: End use case

### Exception Flows

#### EF-1: Work Item or Worklog Not Found

**Triggered at**: Main Flow Step 4 or Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |
| 7.E1 | System | Returns HTTP 404 with error: `WORKLOG_NOT_FOUND` |

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

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-185-01 | Reading a worklog requires `BROWSE_PROJECTS` on the target project | Authorization layer |
| BR-PM-185-02 | If the parent work item has `security_level_id`, caller must satisfy issue-security membership before viewing the worklog | Authorization layer |
| BR-PM-185-03 | Worklog lookup is tenant-scoped, nested under the parent work item path, and excludes soft-deleted rows (`deleted_at IS NULL`) | Repository layer |
| BR-PM-185-04 | `worklogId` must resolve to a worklog that belongs to the requested `workItemId`; mismatched scope is treated as not found | Query layer |
| BR-PM-185-05 | Read operation is side-effect free and must not update audit or rollup fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Parent work item identifier from path |
| worklogId | int64 | Yes | min:1 | Worklog identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Worklog ID |
| work_item_id | int64 | Parent work item ID |
| author_id | int64 | Author user ID |
| comment | string | Worklog comment |
| start_date | timestamp | Work start time |
| time_spent | int64 | Logged duration in seconds |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |
| work_item_time_spent | int64 | Current total logged time for the parent work item |
| work_item_time_remaining_estimate | int64 | Current remaining estimate for the parent work item |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user reading the worklog |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
