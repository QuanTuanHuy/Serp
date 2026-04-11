# UC-PM-135 - Delete Issue Type

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-11

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-135 |
| **Use Case Name** | Delete Issue Type |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-11 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned issue type that is no longer in use. Delete is limited to the current tenant's own data; system-owned issue types remain visible through read APIs but are read-only and cannot be deleted through tenant APIs. The system must reject deletion when the issue type is still referenced by active work items or active tenant configuration that depends on it.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes unused tenant-owned issue types |
| System | System | Validates tenant ownership, checks usage constraints, performs soft delete, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned issue types where `issue_types.tenant_id = tenantId`
- System-owned issue types are read-only and cannot be deleted through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type administration
4. Target issue type exists, is not soft-deleted, and belongs to the current tenant
5. Target issue type is not referenced by active work items or active tenant configuration that blocks deletion

### Postconditions

#### Success Postconditions

1. Target issue type is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. A `ISSUE_TYPE_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.issuetype.events`
4. Deleted issue type is excluded from active read/list queries

#### Failure Postconditions

1. No delete is committed
2. No outbox event is committed
3. Error response is returned with authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/issue-types/{issueTypeId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads issue type by `id=issueTypeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates the issue type is not a system-owned read-only row |
| 6 | System | Validates the issue type is not in use by active work items or active scheme/configuration references in the same tenant |
| 7 | System | Begins database transaction |
| 8 | System | Soft-deletes the issue type and sets delete audit fields |
| 9 | System | Persists `ISSUE_TYPE_DELETED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with deletion confirmation |

### Alternative Flows

#### AF-1: Unused Tenant-Owned Issue Type

**Branches from**: Main Flow Step 6  
**Condition**: No active dependency blocks deletion

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Allows delete to proceed |

**Rejoins**: Main Flow Step 7

### Exception Flows

#### EF-1: Issue Type Not Found in Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-2: Issue Type Still In Use

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_IN_USE` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-135-01 | Tenant callers may delete only issue types owned by their own tenant | Authorization + Repository layer |
| BR-PM-135-02 | System-owned issue types are visible through read APIs but are read-only and cannot be deleted through tenant APIs | Service layer |
| BR-PM-135-03 | Delete operation is soft delete only; data is excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-135-04 | Delete must be rejected when active work items still reference the issue type | Service layer |
| BR-PM-135-05 | Delete must be rejected when active tenant scheme/configuration still depends on the issue type | Service layer |
| BR-PM-135-06 | Domain events use outbox pattern: `ISSUE_TYPE_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| issueTypeId | int64 | Yes | min:1 | Issue type identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted issue type ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
