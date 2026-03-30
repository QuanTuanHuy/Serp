# UC-PM-105 - Delete Work Item

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
| **Use Case ID** | UC-PM-105 |
| **Use Case Name** | Delete Work Item |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-03-30 |
| **Priority** | Medium |
| **Complexity** | Medium |

### Description

Soft-delete a work item in tenant scope using Jira-aligned project authorization. The caller must satisfy delete permissions for the target project and, when the work item is protected by issue security, must also satisfy issue-security membership. Deletion marks the target work item as deleted, recursively soft-deletes descendant subtasks in the same project, soft-deletes bundled relations, and writes a `WORK_ITEM_DELETED` outbox event in the same transaction.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Project Lead | Primary | Deletes work items they are authorized to manage |
| System | System | Resolves authorization, computes delete scope, applies cascading soft delete, and persists outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `DELETE_ISSUES`
- Conditional read/delete constraint: if `security_level_id` is set on the target work item, caller must satisfy membership in that issue security level
- Permission resolution is grant-only via the project's `permission_scheme_id` and may resolve through `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Target work item exists in tenant scope and is not soft-deleted
5. Caller is granted `BROWSE_PROJECTS` and `DELETE_ISSUES` for the target project
6. If the target work item has `security_level_id`, caller is a member of that issue security level

### Postconditions

#### Success Postconditions

1. Target work item has `deleted_at` set and is excluded from active reads
2. All descendant work items in the same project hierarchy are soft-deleted recursively
3. Bundled relations for deleted work items are soft-deleted (`work_item_components`, `work_item_fix_versions`, `work_item_sprints`, `worklogs`, `work_item_custom_field_values`, and `issue_links` where source/target is deleted)
4. A `WORK_ITEM_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.workitem.events`
5. Response returns deletion summary (root work item and cascade counts)

#### Failure Postconditions

1. No partial soft-delete is committed
2. No outbox event is committed
3. Error response is returned with authorization, validation, or configuration details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Project Lead | Sends DELETE `/api/v1/work-items/{workItemId}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and security context |
| 3 | System | Loads work item by `id=workItemId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 4 | System | Loads project context and validates the project is not archived |
| 5 | System | Evaluates `BROWSE_PROJECTS` permission for the caller |
| 6 | System | Evaluates `DELETE_ISSUES` permission for the caller |
| 7 | System | If `security_level_id` is set, evaluates issue-security membership for the caller |
| 8 | System | Begins database transaction |
| 9 | System | Resolves delete scope: target work item plus recursive descendants in the same tenant and project |
| 10 | System | Soft-deletes scoped work items by setting `deleted_at=NOW()`, `updated_by=userId` |
| 11 | System | Soft-deletes bundled relations for scoped work items and linked issue-link rows |
| 12 | System | Persists `WORK_ITEM_DELETED` to domain outbox with root ID and cascade summary |
| 13 | System | Commits transaction |
| 14 | System | Returns HTTP 200 with deletion confirmation payload |

### Alternative Flows

#### AF-1: Leaf Work Item Deletion

**Branches from**: Main Flow Step 9  
**Condition**: Target work item has no active descendants

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Delete scope contains only the target work item |
| 11.1 | System | Soft-deletes only bundled relations directly attached to the target work item |

**Rejoins**: Main Flow Step 12

#### AF-2: Parent With Descendants

**Branches from**: Main Flow Step 9  
**Condition**: Target work item has one or more active descendants

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Builds recursive descendant set within same project hierarchy |
| 10.1 | System | Soft-deletes target and descendants in one transaction |

**Rejoins**: Main Flow Step 11

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Project Permission Denied

**Triggered at**: Main Flow Step 5-6

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail (`BROWSE_PROJECTS` or `DELETE_ISSUES`) |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Invalid Delete Scope

**Triggered at**: Main Flow Step 9

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.E1 | System | If recursive hierarchy traversal detects corrupted scope (cross-project or cyclic parent chain), returns HTTP 422 with error: `WORK_ITEM_DELETE_SCOPE_INVALID` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-105-01 | Deleting a work item requires `BROWSE_PROJECTS` and `DELETE_ISSUES` in the target project | Authorization layer |
| BR-PM-105-02 | If `security_level_id` is set on the work item, caller must satisfy issue-security membership in addition to project permissions | Authorization layer |
| BR-PM-105-03 | Deletion is soft delete only (`deleted_at`), never physical row removal | Service layer |
| BR-PM-105-04 | Deleting a parent work item recursively soft-deletes descendant work items in the same project hierarchy | Service layer |
| BR-PM-105-05 | Issue links where source or target belongs to the delete scope are soft-deleted in the same transaction | Service layer |
| BR-PM-105-06 | Bundled relation rows attached to deleted work items are soft-deleted in the same transaction | Service layer |
| BR-PM-105-07 | Write operations, including delete, are rejected when the project is archived | Service layer |
| BR-PM-105-08 | Domain events use outbox pattern: `WORK_ITEM_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |
| BR-PM-105-09 | Active reads and searches exclude soft-deleted work items and bundled relations (`deleted_at IS NULL`) | Repository layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Work item numeric identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| root_work_item_id | int64 | Requested work item ID |
| deleted_work_item_count | int | Number of work item rows soft-deleted (root + descendants) |
| deleted_relation_count | int | Number of bundled relation rows soft-deleted |
| deleted_link_count | int | Number of issue-link rows soft-deleted |
| deleted_at | timestamp | Server deletion timestamp |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing delete |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission and issue-security evaluation |
