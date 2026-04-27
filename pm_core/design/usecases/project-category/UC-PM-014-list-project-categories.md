# UC-PM-014 - List Project Categories

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-12

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Projects schema: `schema/01_projects.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-014 |
| **Use Case Name** | List Project Categories |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Return a paginated list of active project categories owned by the current tenant. The list supports administrative management, project-create category selectors, and project filters. The API never returns soft-deleted rows or rows belonging to another tenant.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Lists project categories in tenant administration scope |
| System | System | Applies tenant filtering, optional search, sorting, and pagination |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- List results are tenant-scoped and side-effect free

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-category administration

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns paginated active categories for the current tenant

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or validation details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/project-categories` with optional query filters |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Parses and validates query parameters |
| 5 | System | Builds query limited to `tenant_id=tenantId` and `deleted_at IS NULL` |
| 6 | System | Applies optional filters such as `search` |
| 7 | System | Executes count query for total items |
| 8 | System | Executes paginated query with sorting |
| 9 | System | Returns HTTP 200 with paginated category list |

### Alternative Flows

#### AF-1: Search by Name

**Branches from**: Main Flow Step 6  
**Condition**: Client supplies `search`

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.1 | System | Filters result by case-insensitive category `name` match |

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
| BR-PM-014-01 | List results include only active categories in the current tenant | Query layer |
| BR-PM-014-02 | Rows from another tenant must never be returned | Query layer |
| BR-PM-014-03 | Soft-deleted rows are excluded from list results | Query layer |
| BR-PM-014-04 | Default pagination is `page=0`, `pageSize=10`, maximum `pageSize=100` | Controller layer |
| BR-PM-014-05 | Default sort is `created_at DESC` | Query layer |
| BR-PM-014-06 | Search filter applies case-insensitively to `name` | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| search | string | No | min:1 | Search by category `name` |
| sortBy | string | No | `name`, `created_at`, `updated_at` | Sort field |
| sortOrder | string | No | `ASC`, `DESC` | Sort direction |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| data | ProjectCategory[] | Array of categories |
| data[].id | int64 | Category ID |
| data[].name | string | Category name |
| data[].description | string | Category description |
| data[].created_at | timestamp | Creation time |
| data[].updated_at | timestamp | Last update time |
| meta.page | int | Current page |
| meta.pageSize | int | Items per page |
| meta.totalItems | int64 | Total matching items |
| meta.totalPages | int | Total pages |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used for list filtering |
