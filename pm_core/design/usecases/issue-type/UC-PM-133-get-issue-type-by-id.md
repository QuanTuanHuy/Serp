# UC-PM-133 - Get Issue Type by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-11

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-133 |
| **Use Case Name** | Get Issue Type by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-11 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one issue type visible to the current tenant by numeric ID. Read APIs may return either a tenant-owned issue type or a system-owned issue type exposed to that tenant as reusable read-only catalog data. Soft-deleted rows and data from other tenants are never returned.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Reads issue type details in tenant administration scope |
| System | System | Resolves tenant-visible lookup and returns read-only metadata |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC
- Read scope includes tenant-owned rows and system-owned rows visible to the tenant
- Read access does not grant permission to update or delete system-owned rows

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type administration
4. Target issue type is visible to the tenant: either `tenant_id=tenantId` or system-owned read-only data

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns the requested visible issue type
3. Response indicates whether the record is system-owned/read-only

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or not-found details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/issue-types/{issueTypeId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads issue type by `id=issueTypeId` if it belongs to the tenant or is a visible system-owned row, and validates `deleted_at IS NULL` |
| 5 | System | Builds response including ownership/read-only metadata |
| 6 | System | Returns HTTP 200 with issue type details |

### Alternative Flows

#### AF-1: Read System-Owned Issue Type

**Branches from**: Main Flow Step 4  
**Condition**: Matched row is system-owned and visible to the tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Marks the returned row as read-only |
| 5.1 | System | Includes `is_system=true` in response |

**Rejoins**: Main Flow Step 6

#### AF-2: Read Tenant-Owned Issue Type

**Branches from**: Main Flow Step 4  
**Condition**: Matched row belongs to the current tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Treats the row as tenant-owned data |

**Rejoins**: Main Flow Step 5

### Exception Flows

#### EF-1: Issue Type Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-133-01 | Read lookup is limited to rows visible to the tenant: tenant-owned rows and system-owned rows exposed as reusable catalog data | Repository layer |
| BR-PM-133-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-133-03 | Soft-deleted rows are excluded from read lookup | Repository layer |
| BR-PM-133-04 | Returning a system-owned issue type through read API does not permit write operations on that row | Service layer |
| BR-PM-133-05 | Read operation is side-effect free and must not update audit fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| issueTypeId | int64 | Yes | min:1 | Issue type identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Issue type ID |
| tenant_id | int64 | Owning tenant ID or `0` for system-owned row |
| type_key | string | Stable issue type key |
| name | string | Display name |
| description | string | Description |
| icon_url | string | Icon URL |
| hierarchy_level | int | Hierarchy level |
| is_system | bool | Read-only system marker |
| read_only | bool | Whether the row is writable by tenant APIs |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used to evaluate visibility |
