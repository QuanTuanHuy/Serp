# UC-PM-103 - Get Work Item by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.1
> Last Updated: 2026-03-30

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Workflow schema: `schema/03_workflows.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-103 |
| **Use Case Name** | Get Work Item by ID |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-03-30 |
| **Priority** | High |
| **Complexity** | Simple |

### Description

Retrieve one work item in tenant scope by numeric ID, with support for alternative lookup by immutable issue key. Read access follows Jira-style project authorization: caller must satisfy `BROWSE_PROJECTS` for the target project, and if the work item is protected by `security_level_id`, caller must also satisfy membership in that issue security level. The response returns the canonical work item view with resolved issue type, workflow/status, priority, assignee/reporter, and parent references.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Reads work item details in projects they can browse |
| System | System | Resolves tenant-scoped work item lookup, permission checks, issue-security access, and response enrichment |

### Authorization (Jira Project Permissions)

- Baseline permission: `BROWSE_PROJECTS`
- Conditional read constraint: if `security_level_id` is set on the work item, caller must be a member of that issue security level
- Permission resolution is grant-only through the project's `permission_scheme_id` and may resolve via `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. The target work item exists in tenant scope and is not soft-deleted
4. Caller is granted `BROWSE_PROJECTS` in the work item's project
5. If `security_level_id` is set, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. System returns HTTP 200 with enriched work item details
3. Response includes current status/workflow position and core assignment/security metadata

#### Failure Postconditions

1. No data mutation occurs
2. System returns an authorization or not-found error with details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends GET `/api/v1/work-items/{workItemId}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and security context |
| 3 | System | Loads work item by `id=workItemId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 4 | System | Loads target project context from the work item and evaluates `BROWSE_PROJECTS` |
| 5 | System | If `security_level_id` is not null, evaluates membership in that issue security level |
| 6 | System | Enriches core references: issue type, workflow step, status, priority, resolution, assignee, reporter, and parent |
| 7 | System | Loads custom field values for the work item from typed custom field storage |
| 8 | System | Returns HTTP 200 with work item detail response |

### Alternative Flows

#### AF-1: Lookup by Work Item Key

**Branches from**: Main Flow Step 1  
**Condition**: Client calls key-based endpoint

| Step | Actor/System | Action |
|------|-------------|--------|
| 1.1 | Team Member | Sends GET `/api/v1/work-items/key/{key}` (e.g., `SERP-123`) |
| 3.1 | System | Resolves work item by `key`, `tenant_id=tenantId`, `deleted_at IS NULL` |

**Rejoins**: Main Flow Step 4

#### AF-2: No Issue Security Level

**Branches from**: Main Flow Step 5  
**Condition**: `security_level_id` is null on the work item

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Skips issue-security membership evaluation |

**Rejoins**: Main Flow Step 6

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 3 or AF-1 Step 3.1

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Browse Permission Denied

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission `BROWSE_PROJECTS` |

#### EF-3: Issue Security Access Denied

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-4: Invalid Work Item Key Format

**Triggered at**: AF-1 Step 1.1

| Step | Actor/System | Action |
|------|-------------|--------|
| 1.1.E1 | System | Returns HTTP 400 with error: `WORK_ITEM_KEY_INVALID` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-103-01 | Work item read access requires `BROWSE_PROJECTS` on the target project | Authorization layer |
| BR-PM-103-02 | If `security_level_id` is set, caller must satisfy issue-security membership in addition to `BROWSE_PROJECTS` | Authorization layer |
| BR-PM-103-03 | Read lookup must be tenant-scoped and exclude soft-deleted rows (`deleted_at IS NULL`) | Repository layer |
| BR-PM-103-04 | `key` lookup and numeric `id` lookup must resolve to the same canonical work item resource | UseCase layer |
| BR-PM-103-05 | Work item detail response includes denormalized `status_id` and current `workflow_step_id` for consistent workflow-state reads | Query layer |
| BR-PM-103-06 | Read operation is side-effect free and must not update audit or ranking fields | Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes (ID endpoint) | min:1 | Work item numeric identifier |
| key | string | Yes (key endpoint) | regex: `^[A-Z][A-Z0-9]{1,9}-[1-9][0-9]*$` | Immutable work item key |
| includeCustomFields | bool | No | default:true | Include `custom_fields` in detail payload |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Work item ID |
| issue_no | int64 | Project-scoped issue number |
| key | string | Work item key |
| project_id | int64 | Parent project ID |
| summary | string | Work item title |
| description | string | Work item description |
| issue_type | object | Issue type details |
| workflow_step_id | int64 | Current workflow step |
| status | object | Current status details |
| priority | object | Priority details |
| resolution | object | Resolution details (nullable) |
| assignee_id | int64 | Assignee user ID (nullable) |
| reporter_id | int64 | Reporter user ID |
| parent_id | int64 | Parent work item ID (nullable) |
| security_level_id | int64 | Issue security level ID (nullable) |
| due_date | timestamp | Due date (nullable) |
| rank | string | Lexorank value |
| time_original_estimate | int64 | Original estimate in seconds (nullable) |
| time_remaining_estimate | int64 | Remaining estimate in seconds (nullable) |
| time_spent | int64 | Logged time in seconds |
| custom_fields | map<string, unknown> | Typed custom field values by field key |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing read |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission evaluation |
