# UC-PM-146 - Create Priority

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-146 |
| **Use Case Name** | Create Priority |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-owned priority in PM Core. The priority becomes part of the tenant's reusable dictionary and can later be referenced by priority schemes and work items. The API always creates priorities in the caller's tenant scope, sets `is_system=false`, and never allows callers to create system-owned priorities.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned priorities for the current tenant |
| System | System | Validates tenant admin authority, enforces tenant scope and uniqueness, persists the new priority, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority administration
4. The requested `name` does not already exist among active tenant-owned priorities in the same tenant

### Postconditions

#### Success Postconditions

1. A new row is persisted in `priorities` with `tenant_id=tenantId` and `is_system=false`
2. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
3. A `PRIORITY_CREATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.priority.events`
4. Response returns the created priority payload

#### Failure Postconditions

1. No priority row is committed
2. No outbox event is committed
3. Error response is returned with validation or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/priorities` with priority data |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: `name`, optional `description`, optional `icon_url`, optional `color`, and `sequence` |
| 5 | System | Validates `name` is unique among active priorities in the same tenant |
| 6 | System | Begins database transaction |
| 7 | System | Creates priority with `tenant_id=tenantId` and `is_system=false` |
| 8 | System | Persists `PRIORITY_CREATED` to domain outbox |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created priority |

### Alternative Flows

#### AF-1: Optional Metadata Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description`, `icon_url`, or `color`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without optional metadata |
| 7.1 | System | Persists omitted optional fields as `NULL` |

**Rejoins**: Main Flow Step 8

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Priority Name

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `PRIORITY_NAME_ALREADY_EXISTS` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-146-01 | Create Priority always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-146-02 | Tenant callers may create priorities only inside their own tenant scope | Authorization + Repository layer |
| BR-PM-146-03 | `name` must be unique among active priorities in the same tenant | Service layer + DB constraint |
| BR-PM-146-04 | API-created priorities must always set `is_system=false` | Service layer |
| BR-PM-146-05 | API cannot create system-owned priorities | Service layer |
| BR-PM-146-06 | Domain events use outbox pattern: `PRIORITY_CREATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:50; unique in tenant | Priority label |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL, max:255 | Icon URL |
| color | string | No | hex color, max:20 | Display color |
| sequence | int | Yes | min:0 | Display order |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated priority ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Priority label |
| description | string | Description |
| icon_url | string | Icon URL |
| color | string | Display color |
| sequence | int | Display order |
| is_system | bool | Always `false` for API-created priorities |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
