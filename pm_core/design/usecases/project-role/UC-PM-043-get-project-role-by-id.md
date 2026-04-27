# UC-PM-043 - Get Project Role by ID

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
| **Use Case ID** | UC-PM-043 |
| **Use Case Name** | Get Project Role by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one project role visible to the current tenant by ID. Read APIs may return either a tenant-owned role or a system-owned role exposed to that tenant as reusable read-only catalog data. Soft-deleted rows and rows from other tenants are never returned.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Reads project role details in tenant administration scope |
| System | System | Resolves tenant-visible lookup and returns read-only metadata when applicable |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Read scope includes tenant-owned rows and system-owned rows visible to the tenant
- Read access does not grant permission to update or delete system-owned rows

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-role administration
4. Target role is visible to the tenant: either `tenant_id=tenantId` or system-owned read-only data

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns the requested visible project role
3. Response indicates whether the record is system-owned/read-only

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or not-found details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/roles/{roleId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads role by `id=roleId` if it belongs to the tenant or is a visible system-owned row, and validates `deleted_at IS NULL` |
| 5 | System | Builds response including ownership/read-only metadata |
| 6 | System | Returns HTTP 200 with project role details |

### Alternative Flows

#### AF-1: Read System-Owned Role

**Branches from**: Main Flow Step 4  
**Condition**: Matched row is system-owned and visible to the tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Marks the returned row as read-only |
| 5.1 | System | Includes `is_system=true` and `read_only=true` in response |

**Rejoins**: Main Flow Step 6

### Exception Flows

#### EF-1: Role Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ROLE_NOT_FOUND` |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-043-01 | Read lookup is limited to rows visible to the tenant: tenant-owned rows and system-owned rows exposed as reusable catalog data | Repository layer |
| BR-PM-043-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-043-03 | Soft-deleted rows are excluded from read lookup | Repository layer |
| BR-PM-043-04 | Returning a system-owned role through read API does not permit write operations on that row | Service layer |
| BR-PM-043-05 | Read operation is side-effect free and must not update audit fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| roleId | int64 | Yes | min:1 | Role identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Role ID |
| tenant_id | int64 | Owning tenant ID or `0` for system-owned row |
| name | string | Role name |
| description | string | Role description |
| is_system | bool | System-owned marker |
| read_only | bool | Whether the row is writable by tenant APIs |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used to evaluate visibility |
