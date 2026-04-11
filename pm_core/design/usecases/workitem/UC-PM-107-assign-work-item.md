# UC-PM-107 - Assign Work Item

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-08

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-107 |
| **Use Case Name** | Assign Work Item |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-08 |
| **Priority** | High |
| **Complexity** | Simple |

### Description

Assign or unassign a work item in tenant scope using Jira-aligned project authorization. The caller must satisfy browse and assign permissions for the target project. If the work item is protected by issue security, the caller must also satisfy issue-security membership before changing the assignee. When a non-null `assignee_id` is requested, the system validates that the target user exists and is assignable in the target project, updates `assignee_id` atomically, and writes a `WORK_ITEM_ASSIGNED` outbox event.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Assigns or clears the assignee of a visible work item |
| System | System | Resolves project permissions, validates assignee eligibility, updates the work item, and persists outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `ASSIGN_ISSUES`
- Conditional assignee eligibility: when `assignee_id` is non-null, the target assignee must satisfy `ASSIGNABLE_USER` in the same project
- Conditional read/write constraint: if `security_level_id` is set on the work item, caller must satisfy issue-security membership
- Permission resolution is grant-only via the project's `permission_scheme_id` and may resolve through `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Target work item exists in tenant scope and is not soft-deleted
5. Caller is granted `BROWSE_PROJECTS` and `ASSIGN_ISSUES` for the target project
6. If the target work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. Work item `assignee_id` is updated to the requested user or cleared to `NULL`
2. Work item audit fields `updated_at` and `updated_by` are updated when the assignment changes
3. A `WORK_ITEM_ASSIGNED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.workitem.events`
4. Response returns the updated assignment state

#### Failure Postconditions

1. Work item assignment remains unchanged
2. No partial update is committed
3. No outbox event is committed
4. Error response is returned with authorization, validation, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `PUT /api/v1/projects/{projectId}/work-items/{workItemId}/assign` with `{ assignee_id }` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` with tenant scope and validates it is not archived |
| 4 | System | Loads work item by `workItemId` with tenant scope and validates it belongs to the target project |
| 5 | System | Evaluates `BROWSE_PROJECTS` and `ASSIGN_ISSUES` permissions for the caller |
| 6 | System | If `security_level_id` is set, evaluates issue-security membership for the caller |
| 7 | System | If `assignee_id` is non-null, validates the target user exists in Account Service |
| 8 | System | If `assignee_id` is non-null, validates the target assignee satisfies `ASSIGNABLE_USER` in the project |
| 9 | System | If the requested assignment is unchanged, returns the current assignment state without persisting mutation |
| 10 | System | Begins transaction |
| 11 | System | Updates `assignee_id`, `updated_at`, and `updated_by` |
| 12 | System | Persists `WORK_ITEM_ASSIGNED` to domain outbox with assignment metadata |
| 13 | System | Commits transaction |
| 14 | System | Returns HTTP 200 with updated assignment payload |

### Alternative Flows

#### AF-1: Unassign Work Item

**Branches from**: Main Flow Step 7  
**Condition**: Request sends `assignee_id=null`

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.1 | System | Skips remote user lookup |
| 8.1 | System | Resolves target assignment as `NULL` |

**Rejoins**: Main Flow Step 9

#### AF-2: Assignment Unchanged

**Branches from**: Main Flow Step 9  
**Condition**: Requested `assignee_id` equals current `assignee_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Returns current assignment payload without updating audit fields |
| 9.2 | System | Does not persist outbox event because no state change occurred |

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

**Triggered at**: Main Flow Step 5 or 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail (`BROWSE_PROJECTS`, `ASSIGN_ISSUES`, or assignable-user check failure) |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Assignee Not Found

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 404 with error: `USER_NOT_FOUND` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-107-01 | Assigning a work item requires `BROWSE_PROJECTS` and `ASSIGN_ISSUES` in the target project | Authorization layer |
| BR-PM-107-02 | If `security_level_id` is set on the work item, caller must satisfy issue-security membership in addition to project permissions | Authorization layer |
| BR-PM-107-03 | A non-null `assignee_id` must refer to an existing user and satisfy `ASSIGNABLE_USER` in the target project | Authorization + Service layer |
| BR-PM-107-04 | `assignee_id=null` clears the assignment and is treated as a valid unassign operation | Service layer |
| BR-PM-107-05 | Assignment updates are rejected when the project is archived | Service layer |
| BR-PM-107-06 | If requested `assignee_id` equals the current `assignee_id`, the operation is treated as a no-op and does not emit an outbox event | UseCase layer |
| BR-PM-107-07 | Domain events use the outbox pattern: `WORK_ITEM_ASSIGNED` is stored in the same transaction as the assignment update and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Work item numeric identifier from path |
| assignee_id | int64 | No | must be a positive user ID when provided; `null` clears assignment | New assignee |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Work item ID |
| project_id | int64 | Owning project ID |
| key | string | Work item key |
| assignee_id | int64 | Updated assignee user ID (nullable) |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who applied the assignment change |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing assignment |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission and issue-security evaluation |

## Implementation Traceability (Current Code)

### Application/Domain Mapping

| Use Case Flow | Current Implementation |
|---------------|------------------------|
| Validate command payload | `AssignWorkItemValidator.validate(...)` |
| Validate project exists and writable | `AssignWorkItemCommandHandler.handle(...)` + `ensureProjectWritable(...)` |
| Load target work item in tenant scope | `AssignWorkItemCommandHandler.handle(...)` + `IWorkItemService.getWorkItemById(...)` |
| Enforce project boundary (`projectId` path vs work item project) | `AssignWorkItemCommandHandler.ensureWorkItemBelongsToProject(...)` |
| Enforce project permissions (`BROWSE_PROJECTS`, `ASSIGN_ISSUES`) | `IWorkItemAuthorizationSupportService.checkRequiredPermissions(...)` |
| Enforce issue-security membership | `IIssueSecurityService.checkSecurityAccessIfNeeded(...)` |
| Validate target assignee exists | `RoleActorSubjectValidator.validateSubjectExistsForAdd(...)` |
| Validate target assignee is assignable | `IWorkItemAuthorizationSupportService.resolveAssigneeId(...)` |
| Persist assignment update | `IWorkItemService.updateWorkItem(...)` |
| Persist outbox event | `AssignWorkItemCommandHandler.persistAssignedOutboxEvent(...)` |

### Current Gap Notes (Spec vs Runtime)

1. Runtime currently returns `PROJECT_PERMISSION_DENIED` when the requested assignee fails `ASSIGNABLE_USER`; it does not yet expose a dedicated `ASSIGNEE_NOT_ASSIGNABLE` error code.

### Unit Test Coverage Added/Updated

1. `AssignWorkItemCommandHandlerTest.handleShouldAssignWorkItemAndPersistOutboxEvent`
2. `AssignWorkItemCommandHandlerTest.handleShouldUnassignWorkItemWithoutUserLookup`
3. `AssignWorkItemCommandHandlerTest.handleShouldSkipPersistenceWhenAssignmentUnchanged`
4. `AssignWorkItemCommandHandlerTest.handleShouldRejectWhenWorkItemDoesNotBelongToProject`
