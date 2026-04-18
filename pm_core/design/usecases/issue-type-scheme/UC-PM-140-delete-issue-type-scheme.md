# UC-PM-140 - Delete Issue Type Scheme

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-140 |
| **Use Case Name** | Delete Issue Type Scheme |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned issue type scheme that is no longer bound to active projects. Delete is limited to the current tenant's own data; system-owned schemes remain visible through read APIs but are read-only and cannot be deleted through tenant APIs.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes unused tenant-owned issue type schemes |
| System | System | Validates tenant ownership, checks active project bindings, performs soft delete, and removes the scheme from active reads |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned issue type schemes where `issue_type_schemes.tenant_id = tenantId`
- System-owned issue type schemes are read-only and cannot be deleted through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type scheme administration
4. Target scheme exists, is not soft-deleted, belongs to the current tenant, and is not bound to any active project

### Postconditions

#### Success Postconditions

1. Target issue type scheme is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. The deleted scheme is excluded from active read or list queries
4. Response returns deletion confirmation

#### Failure Postconditions

1. No delete is committed
2. Error response is returned with authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/issue-type-schemes/{schemeId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates no active project currently binds to the scheme |
| 6 | System | Begins database transaction |
| 7 | System | Soft-deletes the scheme and sets delete audit fields |
| 8 | System | Commits transaction |
| 9 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

#### EF-1: Scheme Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_SCHEME_NOT_FOUND` |

#### EF-2: Scheme Is Still Bound to Active Projects

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_SCHEME_BOUND_TO_PROJECT` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-140-01 | Tenant callers may delete only issue type schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-140-02 | Write lookup for delete is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-140-03 | Delete operation is soft delete only; deleted rows are excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-140-04 | Delete must be rejected when any active project still binds to the scheme | Service layer |
| BR-PM-140-05 | Deleting a scheme does not delete issue type dictionary rows | Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| schemeId | int64 | Yes | min:1 | Scheme identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted scheme ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
