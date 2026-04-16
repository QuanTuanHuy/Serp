# UC-PM-019 - List Project Blueprints

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
| **Use Case ID** | UC-PM-019 |
| **Use Case Name** | List Project Blueprints |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-16 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Return a paginated list of project blueprints visible to the current tenant. The list includes tenant-owned blueprints and visible system-owned blueprints exposed as reusable read-only template data. The list endpoint returns blueprint metadata only; scheme defaults are inspected through `UC-PM-018`.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Lists project blueprints in tenant administration scope |
| System | System | Applies tenant visibility rules, optional filters, sorting, and pagination |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Read scope includes tenant-owned rows and system-owned read-only rows visible to the tenant
- Returned system-owned rows remain read-only

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for blueprint administration

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns paginated tenant-visible blueprints
3. Each item includes ownership/read-only metadata needed by administration UI

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or validation details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/project-blueprints` with optional query filters |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Parses and validates query parameters |
| 5 | System | Builds query limited to tenant-visible active rows: `tenant_id=tenantId` or system-owned visible rows, with `deleted_at IS NULL` |
| 6 | System | Applies optional filters such as `search`, `project_type_key`, and `is_system` |
| 7 | System | Executes count query for total items |
| 8 | System | Executes paginated query with sorting |
| 9 | System | Returns HTTP 200 with paginated blueprint list |

### Alternative Flows

#### AF-1: Filter By Project Type

**Branches from**: Main Flow Step 6  
**Condition**: Client supplies `project_type_key`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Filters result to blueprints matching the given `project_type_key` |

**Rejoins**: Main Flow Step 7

#### AF-2: Filter By Ownership Type

**Branches from**: Main Flow Step 6  
**Condition**: Client supplies `is_system=true` or `is_system=false`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Filters result to the requested ownership type |

**Rejoins**: Main Flow Step 7

### Exception Flows

#### EF-1: Invalid Query Parameter

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with query validation details |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-019-01 | List results include tenant-owned blueprints plus visible system-owned read-only blueprints | Query layer |
| BR-PM-019-02 | Rows from another tenant must never be returned | Query layer |
| BR-PM-019-03 | Soft-deleted rows are excluded from list results | Query layer |
| BR-PM-019-04 | Default pagination is `page=0`, `pageSize=10`, maximum `pageSize=100` | Controller layer |
| BR-PM-019-05 | Default sort is `name ASC` | Query layer |
| BR-PM-019-06 | `UC-PM-019` returns blueprint metadata only and does not expand scheme defaults | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| search | string | No | min:1 | Search by `name` |
| project_type_key | string | No | `software`, `business`, `service_desk` | Filter by blueprint project type |
| is_system | bool | No | true or false | Filter by ownership type |
| sortBy | string | No | `name`, `created_at`, `updated_at` | Sort field |
| sortOrder | string | No | `ASC`, `DESC` | Sort direction |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| data | ProjectBlueprint[] | Array of visible blueprints |
| data[].id | int64 | Blueprint ID |
| data[].tenant_id | int64 | Owning tenant ID or `0` for system row |
| data[].name | string | Blueprint name |
| data[].description | string | Blueprint description |
| data[].project_type_key | string | Blueprint project type |
| data[].avatar_url | string | Blueprint icon URL |
| data[].is_system | bool | System-owned marker |
| data[].read_only | bool | Whether tenant APIs may mutate the row |
| meta.page | int | Current page |
| meta.pageSize | int | Items per page |
| meta.totalItems | int64 | Total matching items |
| meta.totalPages | int | Total pages |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used for visibility filtering |
