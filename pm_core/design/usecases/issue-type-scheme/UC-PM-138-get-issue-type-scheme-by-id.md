# UC-PM-138 - Get Issue Type Scheme by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-138 |
| **Use Case Name** | Get Issue Type Scheme by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one issue type scheme visible to the current tenant by ID. Read APIs may return either a tenant-owned scheme or a system-owned scheme exposed to that tenant as reusable read-only configuration data. The detail response includes ordered scheme items and basic issue type summaries for each item. Soft-deleted rows and data from other tenants are never returned.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Reads issue type scheme details in tenant administration scope |
| System | System | Resolves tenant-visible lookup, loads ordered items, and returns read-only metadata |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC
- Read scope includes tenant-owned rows and system-owned rows visible to the tenant
- Read access does not grant permission to update, delete, or manage items on system-owned rows

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type scheme administration
4. Target scheme is visible to the tenant: either `tenant_id=tenantId` or a system-owned read-only row

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns the requested visible scheme
3. Response includes ordered scheme items and indicates whether the scheme is read-only

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or not-found details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/issue-type-schemes/{schemeId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId` if it belongs to the tenant or is a visible system-owned row, and validates `deleted_at IS NULL` |
| 5 | System | Loads scheme items in ascending `sequence` order within the same visible scope |
| 6 | System | Resolves lightweight issue type details for each item and for `default_issue_type_id` |
| 7 | System | Builds response including ownership and `read_only` metadata |
| 8 | System | Returns HTTP 200 with scheme details |

### Alternative Flows

#### AF-1: Read System-Owned Scheme

**Branches from**: Main Flow Step 4  
**Condition**: Matched row is system-owned and visible to the tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Marks the returned scheme as read-only |
| 7.1 | System | Includes `tenant_id=0` and `read_only=true` in response |

**Rejoins**: Main Flow Step 8

#### AF-2: Scheme Has No Items Yet

**Branches from**: Main Flow Step 5  
**Condition**: No active items currently exist for the scheme

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Returns an empty `items` array |

**Rejoins**: Main Flow Step 6

### Exception Flows

#### EF-1: Scheme Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_SCHEME_NOT_FOUND` |

#### EF-2: Referenced Issue Type Not Found in Visible Scope

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-138-01 | Read lookup is limited to schemes visible to the tenant: tenant-owned rows and system-owned rows exposed as reusable catalog data | Repository layer |
| BR-PM-138-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-138-03 | Soft-deleted scheme rows and soft-deleted scheme items are excluded from read lookup | Repository layer |
| BR-PM-138-04 | Returned items must be ordered by `sequence ASC` | Query layer |
| BR-PM-138-05 | Returning a system-owned scheme through read API does not permit write operations on that row | Service layer |
| BR-PM-138-06 | Read operation is side-effect free and must not update audit fields | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| schemeId | int64 | Yes | min:1 | Scheme identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Scheme ID |
| tenant_id | int64 | Owning tenant ID or `0` for system-owned row |
| name | string | Scheme name |
| description | string | Description |
| default_issue_type_id | int64 | Default issue type ID |
| read_only | bool | Whether tenant APIs may mutate the scheme |
| items | IssueTypeSchemeItem[] | Ordered issue types configured in the scheme |
| items[].issue_type_id | int64 | Issue type ID |
| items[].sequence | int | Display order within the scheme |
| items[].issue_type.type_key | string | Stable issue type key |
| items[].issue_type.name | string | Issue type display name |
| items[].issue_type.hierarchy_level | int | Issue type hierarchy level |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used to evaluate visibility |
