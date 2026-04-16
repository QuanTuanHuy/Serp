# UC-PM-042 - Update Project Role

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
| **Use Case ID** | UC-PM-042 |
| **Use Case Name** | Update Project Role |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of an existing tenant-owned project role. The caller may update `name` and `description`, but may not change ownership or modify system-seeded roles through the tenant API. Changes affect how the role appears in project administration and how permission scheme grants reference that role by ID.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates tenant-owned project roles |
| System | System | Validates tenant scope, uniqueness, system-role restrictions, persists changes, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned roles where `project_roles.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-role administration
4. Target role exists, is not soft-deleted, and belongs to the current tenant
5. Target role is not a system-seeded read-only role

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the role row
2. `updated_at` and `updated_by` are updated
3. A `PROJECT_ROLE_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-role.events`
4. Response returns the updated role payload

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Error response is returned with validation, authorization, conflict, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/roles/{roleId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads role by `id=roleId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates target role is not a system role |
| 6 | System | Validates input payload |
| 7 | System | If `name` is supplied and changed, validates uniqueness within tenant scope |
| 8 | System | Begins database transaction |
| 9 | System | Applies allowed updates and sets `updated_by=userId` |
| 10 | System | Persists `PROJECT_ROLE_UPDATED` to domain outbox |
| 11 | System | Commits transaction |
| 12 | System | Returns HTTP 200 with updated role |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 6-9  
**Condition**: Request updates only one mutable field

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Treats omitted mutable fields as unchanged |
| 9.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 10

#### AF-2: Clear Description

**Branches from**: Main Flow Step 9  
**Condition**: Request explicitly sends `null` for `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Clears `description` to `NULL` |

**Rejoins**: Main Flow Step 10

### Exception Flows

#### EF-1: Role Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ROLE_NOT_FOUND` |

#### EF-2: System Role Update Rejected

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ROLE_IS_SYSTEM` |

#### EF-3: Duplicate Role Name

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 409 with error: `ROLE_NAME_ALREADY_EXISTS` |

#### EF-4: Validation Error

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 400 with validation details |

#### EF-5: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-042-01 | Tenant callers may update only roles owned by their own tenant | Authorization + Repository layer |
| BR-PM-042-02 | `name` must remain unique among active roles in the same tenant | Service layer + DB constraint |
| BR-PM-042-03 | `tenant_id` and `is_system` are system-controlled fields and cannot be updated by API clients | Service layer |
| BR-PM-042-04 | System-owned roles are visible through read APIs but are read-only and cannot be updated through tenant APIs | Service layer |
| BR-PM-042-05 | `description` may be cleared to `NULL`; omitted fields remain unchanged | UseCase + Service layer |
| BR-PM-042-06 | Domain events use outbox pattern: `PROJECT_ROLE_UPDATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| roleId | int64 | Yes | min:1 | Role identifier from path |
| name | string | No | min:1, max:255; unique in tenant when changed | Updated role name |
| description | string | No | max:2000; `null` clears value | Updated role description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Role ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Updated role name |
| description | string | Updated role description |
| is_system | bool | Always `false` for writable tenant-owned roles |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
