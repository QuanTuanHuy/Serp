# UC-PM-045 - Delete Project Role

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Projects schema: `schema/01_projects.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-045 |
| **Use Case Name** | Delete Project Role |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned project role that is no longer referenced by permission schemes. Delete is limited to the current tenant's own role catalog; system-owned roles remain visible through read APIs but are read-only and cannot be deleted through tenant APIs. The system must reject deletion when the role is still referenced by active permission scheme grants using `grantee_type=PROJECT_ROLE`.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes unused tenant-owned project roles |
| System | System | Validates tenant ownership and usage constraints, performs soft delete, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned roles where `project_roles.tenant_id = tenantId`
- System-owned roles are read-only and cannot be deleted through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-role administration
4. Target role exists, is not soft-deleted, and belongs to the current tenant
5. Target role is not a system-owned read-only row
6. Target role is not referenced by active permission scheme entries in the same tenant

### Postconditions

#### Success Postconditions

1. Target role is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. A `PROJECT_ROLE_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-role.events`
4. Deleted role is excluded from active read/list queries

#### Failure Postconditions

1. No delete is committed
2. No outbox event is committed
3. Error response is returned with authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/roles/{roleId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads role by `id=roleId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates the role is not a system-owned read-only row |
| 6 | System | Validates the role is not referenced by active permission scheme grants in the same tenant |
| 7 | System | Begins database transaction |
| 8 | System | Soft-deletes the role and sets delete audit fields |
| 9 | System | Persists `PROJECT_ROLE_DELETED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

#### EF-1: Role Not Found in Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ROLE_NOT_FOUND` |

#### EF-2: System Role Delete Rejected

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ROLE_IS_SYSTEM` |

#### EF-3: Role Still In Use By Permission Scheme

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `ROLE_IN_USE_BY_PERMISSION` |

#### EF-4: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-045-01 | Tenant callers may delete only roles owned by their own tenant | Authorization + Repository layer |
| BR-PM-045-02 | System-owned roles are visible through read APIs but are read-only and cannot be deleted through tenant APIs | Service layer |
| BR-PM-045-03 | Delete operation is soft delete only; data is excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-045-04 | Delete must be rejected when active permission scheme entries still reference the role via `grantee_type=PROJECT_ROLE` | Service layer |
| BR-PM-045-05 | Domain events use outbox pattern: `PROJECT_ROLE_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| roleId | int64 | Yes | min:1 | Role identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted role ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
