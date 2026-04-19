# UC-PM-018 - Get Project Blueprint by ID

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
| **Use Case ID** | UC-PM-018 |
| **Use Case Name** | Get Project Blueprint by ID |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-16 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Retrieve one project blueprint visible to the current tenant by ID. Read APIs may return either a tenant-owned blueprint or a system-owned blueprint exposed to that tenant as reusable read-only template data. This use case expands `blueprint_scheme_defaults` so administration UI can inspect blueprint provisioning inputs in one response.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Reads project blueprint details in tenant administration scope |
| System | System | Resolves tenant-visible lookup, loads scheme defaults, and returns read-only metadata when applicable |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Read scope includes tenant-owned rows and system-owned rows visible to the tenant
- Read access does not grant permission to update or delete system-owned rows

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for blueprint administration
4. Target blueprint is visible to the tenant: either `tenant_id=tenantId` or system-owned read-only data

### Postconditions

#### Success Postconditions

1. No data mutation occurs
2. Response returns the requested visible blueprint metadata
3. Response includes expanded `blueprint_scheme_defaults`

#### Failure Postconditions

1. No data mutation occurs
2. Error response is returned with authorization or not-found details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/project-blueprints/{blueprintId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads blueprint by `id=blueprintId` if it belongs to the tenant or is a visible system-owned row, and validates `deleted_at IS NULL` |
| 5 | System | Loads `blueprint_scheme_defaults` visible to the same tenant |
| 6 | System | Builds response payload including ownership/read-only metadata and expanded scheme defaults |
| 7 | System | Returns HTTP 200 with blueprint details |

### Alternative Flows

#### AF-1: Read System-Owned Blueprint

**Branches from**: Main Flow Step 4  
**Condition**: Matched row is system-owned and visible to the tenant

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Marks the returned row as read-only |
| 6.1 | System | Includes `is_system=true` and `read_only=true` in response |

**Rejoins**: Main Flow Step 7

### Exception Flows

#### EF-1: Blueprint Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `BLUEPRINT_NOT_FOUND` |

#### EF-2: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-018-01 | Read lookup is limited to rows visible to the tenant: tenant-owned rows and system-owned rows exposed as reusable catalog data | Repository layer |
| BR-PM-018-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-018-03 | Soft-deleted rows are excluded from read lookup | Repository layer |
| BR-PM-018-04 | Returning a system-owned blueprint through read API does not permit write operations on that row | Service layer |
| BR-PM-018-05 | `blueprint_scheme_defaults` must be filtered with the same visibility rule as the parent blueprint | Query layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| blueprintId | int64 | Yes | min:1 | Blueprint identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Blueprint ID |
| tenant_id | int64 | Owning tenant ID or `0` for system-owned row |
| name | string | Blueprint name |
| description | string | Blueprint description |
| project_type_key | string | Blueprint project type |
| avatar_url | string | Blueprint icon URL |
| is_system | bool | System-owned marker |
| read_only | bool | Whether the row is writable by tenant APIs |
| scheme_defaults | BlueprintSchemeDefault[] | Expanded scheme-default mappings |
| scheme_defaults[].scheme_type | string | Scheme family |
| scheme_defaults[].scheme_id | int64 | Referenced scheme ID |
| created_at | timestamp | Creation time |
| updated_at | timestamp | Last update time |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| tenantId | JWT token | Tenant scope used to evaluate visibility |
