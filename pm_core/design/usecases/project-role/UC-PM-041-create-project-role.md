# UC-PM-041 - Create Project Role

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
| **Use Case ID** | UC-PM-041 |
| **Use Case Name** | Create Project Role |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-scoped project role used by permission schemes and per-project role actor assignments. A project role is catalog data at tenant scope, not project-scoped runtime data. API-created roles are always tenant-owned and mutable by tenant administrators; system-seeded roles remain outside this use case.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned project roles |
| System | System | Validates tenant admin authority, enforces uniqueness, persists role, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-role administration
4. The requested role `name` does not already exist among active roles in the same tenant

### Postconditions

#### Success Postconditions

1. A new row is persisted in `project_roles` with `tenant_id=tenantId`
2. The created row is persisted with `is_system=false`
3. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
4. A `PROJECT_ROLE_CREATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-role.events`
5. Response returns the created project role payload

#### Failure Postconditions

1. No role row is committed
2. No outbox event is committed
3. Error response is returned with validation or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/roles` with role data |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: required `name` and optional `description` |
| 5 | System | Validates `name` is unique among active roles in the same tenant |
| 6 | System | Begins database transaction |
| 7 | System | Creates project role with `tenant_id=tenantId` and `is_system=false` |
| 8 | System | Persists `PROJECT_ROLE_CREATED` to domain outbox |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created project role |

### Alternative Flows

#### AF-1: Optional Description Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without description |
| 7.1 | System | Persists `description = NULL` |

**Rejoins**: Main Flow Step 8

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Role Name

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ROLE_NAME_ALREADY_EXISTS` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-041-01 | Role name must be unique among active roles in the same tenant | Service layer + DB constraint |
| BR-PM-041-02 | Create Project Role always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-041-03 | API-created roles are always persisted with `is_system=false` | Service layer |
| BR-PM-041-04 | `name` is required and must be between 1 and 255 characters | DTO validation |
| BR-PM-041-05 | `description` is optional and may be stored as `NULL` when omitted | DTO + Service layer |
| BR-PM-041-06 | Domain events use outbox pattern: `PROJECT_ROLE_CREATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255; unique in tenant | Role name |
| description | string | No | max:2000 | Role description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated role ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Role name |
| description | string | Role description |
| is_system | bool | Always `false` for API-created roles |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
