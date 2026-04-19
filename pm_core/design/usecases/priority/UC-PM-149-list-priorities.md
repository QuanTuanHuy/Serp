# UC-PM-149 - List Priorities

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-149 |
| **Use Case Name** | List Priorities |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Return a paginated list of priorities visible to the current tenant. The list includes tenant-owned priorities and visible system-owned priorities exposed as reusable read-only catalog data. The API never returns soft-deleted rows or rows belonging to another tenant.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Lists priorities in tenant administration scope |
| System | System | Applies tenant visibility rules, filters, sorting, and pagination |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Read scope includes tenant-owned rows and system-owned read-only rows visible to the tenant
- Returned system-owned rows remain read-only

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority administration

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns paginated tenant-visible priorities
3. Each item includes ownership/read-only metadata needed by management UI

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or validation details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/priorities` with optional query filters |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Parses and validates query parameters |
| 5 | System | Builds query limited to tenant-visible active rows: `tenant_id=tenantId` or system-owned visible rows, with `deleted_at IS NULL` |
| 6 | System | Applies optional filters such as `search` and `is_system` |
| 7 | System | Executes count query for total items |
| 8 | System | Executes paginated query with sorting |
| 9 | System | Returns HTTP 200 with paginated priority list |

### Alternative Flows

#### AF-1: List Only Tenant-Owned Priorities

**Branches from**: Main Flow Step 6  
**Condition**: Client supplies `is_system=false`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Filters result to tenant-owned priorities only |

**Rejoins**: Main Flow Step 7

#### AF-2: List Only System-Owned Priorities

**Branches from**: Main Flow Step 6  
**Condition**: Client supplies `is_system=true`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Filters result to visible system-owned priorities only |

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
| BR-PM-149-01 | List results include tenant-owned priorities plus visible system-owned read-only priorities | Query layer |
| BR-PM-149-02 | Rows from another tenant must never be returned | Query layer |
| BR-PM-149-03 | Soft-deleted rows are excluded from list results | Query layer |
| BR-PM-149-04 | Default pagination is `page=0`, `pageSize=10`, maximum `pageSize=100` | Controller layer |
| BR-PM-149-05 | Default sort is `sequence ASC, name ASC` | Query layer |
| BR-PM-149-06 | Search filter applies case-insensitively to `name` | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| search | string | No | min:1 | Search by `name` |
| is_system | bool | No | true or false | Filter by ownership type |
| sortBy | string | No | `sequence`, `name`, `created_at` | Sort field |
| sortOrder | string | No | `ASC`, `DESC` | Sort direction |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| data | Priority[] | Array of visible priorities |
| data[].id | int64 | Priority ID |
| data[].tenant_id | int64 | Owning tenant ID or `0` for system row |
| data[].name | string | Priority label |
| data[].color | string | Display color |
| data[].sequence | int | Display order |
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
