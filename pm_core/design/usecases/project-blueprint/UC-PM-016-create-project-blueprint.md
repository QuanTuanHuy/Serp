# UC-PM-016 - Create Project Blueprint

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
| **Use Case ID** | UC-PM-016 |
| **Use Case Name** | Create Project Blueprint |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-16 |
| **Priority** | Medium |
| **Complexity** | Medium |

### Description

Create a tenant-owned project blueprint used as a provisioning template for new projects. A blueprint stores project metadata defaults such as `name`, `description`, `project_type_key`, and `avatar_url`; the actual scheme-default mappings are managed separately by `UC-PM-021`.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned project blueprints |
| System | System | Validates tenant admin authority, validates blueprint metadata, and persists the new blueprint |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for blueprint administration
4. The requested blueprint `name` does not already exist among active blueprints in the same tenant
5. `project_type_key` is one of `software`, `business`, or `service_desk`

### Postconditions

#### Success Postconditions

1. A new row is persisted in `project_blueprints` with `tenant_id=tenantId`
2. The created row is persisted with `is_system=false`
3. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
4. Response returns the created blueprint payload

#### Failure Postconditions

1. No blueprint row is committed
2. Error response is returned with validation or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/project-blueprints` with blueprint metadata |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: required `name`, `project_type_key`; optional `description`, `avatar_url` |
| 5 | System | Validates `project_type_key` is supported |
| 6 | System | Validates `name` is unique among active blueprints in the same tenant |
| 7 | System | Begins database transaction |
| 8 | System | Creates blueprint with `tenant_id=tenantId` and `is_system=false` |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created blueprint |

### Alternative Flows

#### AF-1: Optional Fields Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description` or `avatar_url`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without optional fields |
| 8.1 | System | Persists omitted optional fields as `NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4-5

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Blueprint Name

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `BLUEPRINT_NAME_ALREADY_EXISTS` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-016-01 | Blueprint name must be unique among active blueprints in the same tenant | Service layer + DB constraint |
| BR-PM-016-02 | Create Project Blueprint always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-016-03 | API-created blueprints are always persisted with `is_system=false` | Service layer |
| BR-PM-016-04 | `project_type_key` must be one of `software`, `business`, or `service_desk` | DTO + Service layer |
| BR-PM-016-05 | Scheme-default mappings are not created in UC-PM-016; they are managed separately by UC-PM-021 | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255; unique in tenant | Blueprint name |
| description | string | No | max:2000 | Blueprint description |
| project_type_key | string | Yes | `software`, `business`, `service_desk` | Blueprint project type |
| avatar_url | string | No | valid URL, max:255 | Blueprint icon URL |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated blueprint ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Blueprint name |
| description | string | Blueprint description |
| project_type_key | string | Blueprint project type |
| avatar_url | string | Blueprint icon URL |
| is_system | bool | Always `false` for API-created blueprints |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
