# UC-PM-017 - Update Project Blueprint

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
| **Use Case ID** | UC-PM-017 |
| **Use Case Name** | Update Project Blueprint |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-16 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of an existing tenant-owned project blueprint. The caller may update `name`, `description`, and `avatar_url`; `project_type_key`, `tenant_id`, and `is_system` are immutable in this use case. System-owned blueprints are visible through read APIs but are outside the write scope of this endpoint.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates tenant-owned project blueprints |
| System | System | Validates tenant scope, uniqueness, immutable fields, persists changes |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned blueprints where `project_blueprints.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for blueprint administration
4. Target blueprint exists, is not soft-deleted, and belongs to the current tenant
5. Target blueprint belongs to the current tenant write scope

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the blueprint row
2. `updated_at` and `updated_by` are updated
3. Response returns the updated blueprint payload

#### Failure Postconditions

1. No partial update is committed
2. Error response is returned with validation, authorization, conflict, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/project-blueprints/{blueprintId}` with mutable metadata updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads blueprint by `id=blueprintId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload and rejects immutable field updates |
| 6 | System | If `name` is supplied and changed, validates uniqueness within tenant scope |
| 7 | System | Begins database transaction |
| 8 | System | Applies allowed updates and sets `updated_by=userId` |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 200 with updated blueprint |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 5-8  
**Condition**: Request updates only one mutable field

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Treats omitted mutable fields as unchanged |
| 8.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 9

#### AF-2: Clear Optional Metadata

**Branches from**: Main Flow Step 8  
**Condition**: Request explicitly sends `null` for `description` or `avatar_url`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Clears the corresponding persisted value to `NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Blueprint Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `BLUEPRINT_NOT_FOUND` |

#### EF-2: Duplicate Blueprint Name

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `BLUEPRINT_NAME_ALREADY_EXISTS` |

#### EF-3: Validation Error

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with validation details |

#### EF-4: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-017-01 | Tenant callers may update only blueprints owned by their own tenant | Authorization + Repository layer |
| BR-PM-017-02 | `name` must remain unique among active blueprints in the same tenant | Service layer + DB constraint |
| BR-PM-017-03 | `project_type_key`, `tenant_id`, and `is_system` are immutable in UC-PM-017 | UseCase + Service layer |
| BR-PM-017-04 | System-owned blueprints are visible through read APIs but are outside the write scope of UC-PM-017 and therefore are not resolved by tenant-owned update lookup | Service layer + Repository layer |
| BR-PM-017-05 | `description` and `avatar_url` may be cleared to `NULL`; omitted fields remain unchanged | UseCase + Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| blueprintId | int64 | Yes | min:1 | Blueprint identifier from path |
| name | string | No | min:1, max:255; unique in tenant when changed | Updated blueprint name |
| description | string | No | max:2000; `null` clears value | Updated blueprint description |
| avatar_url | string | No | valid URL, max:255; `null` clears value | Updated blueprint icon URL |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Blueprint ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Updated blueprint name |
| description | string | Updated blueprint description |
| project_type_key | string | Existing immutable project type |
| avatar_url | string | Updated blueprint icon URL |
| is_system | bool | Always `false` for writable tenant-owned blueprints |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
