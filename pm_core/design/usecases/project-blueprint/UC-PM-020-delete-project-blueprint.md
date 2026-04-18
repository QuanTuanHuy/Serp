# UC-PM-020 - Delete Project Blueprint

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-16

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Projects schema: `schema/01_projects.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-020 |
| **Use Case Name** | Delete Project Blueprint |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-16 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned project blueprint that is no longer needed. Delete is limited to the current tenant's own blueprint catalog; system-owned blueprints remain visible through read APIs but are outside the delete scope of this endpoint. Existing projects are not affected because blueprints are provisioning templates, not active runtime bindings.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes tenant-owned project blueprints |
| System | System | Validates tenant ownership and system restrictions, performs soft delete |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned blueprints where `project_blueprints.tenant_id = tenantId`
- System-owned blueprints are read-only and cannot be deleted through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for blueprint administration
4. Target blueprint exists, is not soft-deleted, and belongs to the current tenant
5. Target blueprint belongs to the current tenant write scope

### Postconditions

#### Success Postconditions

1. Target blueprint is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. Deleted blueprint is excluded from active read/list queries
4. Existing projects remain unchanged because blueprint data is used only during provisioning

#### Failure Postconditions

1. No delete is committed
2. Error response is returned with authorization or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/project-blueprints/{blueprintId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads blueprint by `id=blueprintId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Begins database transaction |
| 6 | System | Soft-deletes the blueprint and sets delete audit fields |
| 7 | System | Commits transaction |
| 8 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

#### EF-1: Blueprint Not Found In Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `BLUEPRINT_NOT_FOUND` |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-020-01 | Tenant callers may delete only blueprints owned by their own tenant | Authorization + Repository layer |
| BR-PM-020-02 | System-owned blueprints are visible through read APIs but are outside the delete scope of UC-PM-020 and therefore are not resolved by tenant-owned delete lookup | Service layer + Repository layer |
| BR-PM-020-03 | Delete operation is soft delete only; data is excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-020-04 | Deleting a blueprint does not affect already-created projects because blueprints are provisioning templates only | Domain rule |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| blueprintId | int64 | Yes | min:1 | Blueprint identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted blueprint ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
