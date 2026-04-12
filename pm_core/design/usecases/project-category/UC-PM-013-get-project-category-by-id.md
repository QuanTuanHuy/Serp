# UC-PM-013 - Get Project Category by ID

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
| **Use Case ID** | UC-PM-013 |
| **Use Case Name** | Get Project Category by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one active project category in the current tenant by ID. The use case returns metadata used by administrative screens and by project create/update flows. Soft-deleted rows and rows from other tenants are never returned.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Reads one project category in tenant administration scope |
| System | System | Resolves tenant-scoped lookup and returns category metadata |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Read lookup is tenant-scoped and side-effect free

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-category administration
4. Target category exists in the current tenant and is not soft-deleted

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns the requested project category metadata

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or not-found details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/project-categories/{categoryId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads category by `id=categoryId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Builds response payload |
| 6 | System | Returns HTTP 200 with category details |

### Exception Flows

#### EF-1: Category Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `CATEGORY_NOT_FOUND` |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-013-01 | Read lookup is limited to active categories in the current tenant | Repository layer |
| BR-PM-013-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-013-03 | Soft-deleted rows are excluded from read lookup | Repository layer |
| BR-PM-013-04 | Read operation is side-effect free and must not update audit fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| categoryId | int64 | Yes | min:1 | Category identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Category ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Category name |
| description | string | Category description |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used for lookup isolation |
