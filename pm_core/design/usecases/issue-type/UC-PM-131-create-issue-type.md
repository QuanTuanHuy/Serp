# UC-PM-131 - Create Issue Type

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-11

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-131 |
| **Use Case Name** | Create Issue Type |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-11 |
| **Priority** | High |
| **Complexity** | Simple |

### Description

Create a new tenant-owned issue type in PM Core. The issue type becomes part of the tenant's reusable dictionary and can later be referenced by issue type schemes, workflows, field configurations, and screens. The API always creates issue types in the caller's tenant scope, sets `is_system=false`, and never allows callers to create system-owned issue types.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned issue types for the current tenant |
| System | System | Validates tenant admin authority, enforces tenant scope and uniqueness, persists the new issue type, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type administration
4. The requested `type_key` does not already exist among active tenant-owned issue types in the same tenant

### Postconditions

#### Success Postconditions

1. A new row is persisted in `issue_types` with `tenant_id=tenantId` and `is_system=false`
2. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
3. A `ISSUE_TYPE_CREATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.issuetype.events`
4. Response returns the created issue type payload

#### Failure Postconditions

1. No issue type row is committed
2. No outbox event is committed
3. Error response is returned with validation or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/issue-types` with issue type data |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: `type_key`, `name`, optional `description`, optional `icon_url`, and `hierarchy_level` |
| 5 | System | Validates `type_key` is unique among active issue types in the same tenant |
| 6 | System | Begins database transaction |
| 7 | System | Creates issue type with `tenant_id=tenantId` and `is_system=false` |
| 8 | System | Persists `ISSUE_TYPE_CREATED` to domain outbox |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created issue type |

### Alternative Flows

#### AF-1: Description Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without description |
| 7.1 | System | Persists `description=NULL` |

**Rejoins**: Main Flow Step 8

#### AF-2: Icon Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `icon_url`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without icon |
| 7.1 | System | Persists `icon_url=NULL` |

**Rejoins**: Main Flow Step 8

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Issue Type Key

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_KEY_ALREADY_EXISTS` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-131-01 | Create Issue Type always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-131-02 | Tenant callers may create issue types only inside their own tenant scope | Authorization + Repository layer |
| BR-PM-131-03 | `type_key` must be unique among active issue types in the same tenant | Service layer + DB constraint |
| BR-PM-131-04 | `type_key` is stable and intended for downstream scheme/context resolution | Service layer |
| BR-PM-131-05 | API-created issue types must always set `is_system=false` | Service layer |
| BR-PM-131-06 | API cannot create system-owned issue types | Service layer |
| BR-PM-131-07 | Domain events use outbox pattern: `ISSUE_TYPE_CREATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| type_key | string | Yes | min:1, max:100; unique in tenant | Stable issue type key |
| name | string | Yes | min:1, max:255 | Display name |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL, max:255 | Icon URL |
| hierarchy_level | int | Yes | one of `0`, `1`, `2` | Hierarchy level (`0=subtask`, `1=standard`, `2=epic+`) |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated issue type ID |
| tenant_id | int64 | Owning tenant ID |
| type_key | string | Stable issue type key |
| name | string | Display name |
| description | string | Description |
| icon_url | string | Icon URL |
| hierarchy_level | int | Hierarchy level |
| is_system | bool | Always `false` for API-created issue types |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
