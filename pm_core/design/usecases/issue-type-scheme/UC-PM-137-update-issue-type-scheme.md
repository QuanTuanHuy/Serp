# UC-PM-137 - Update Issue Type Scheme

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
| **Use Case ID** | UC-PM-137 |
| **Use Case Name** | Update Issue Type Scheme |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of a tenant-owned issue type scheme. The caller may update fields such as `name`, `description`, and `default_issue_type_id`, but may not change ownership, may not modify system-owned schemes through the tenant API, and may not use this endpoint to replace scheme items.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates tenant-owned issue type schemes in the current tenant |
| System | System | Validates tenant scope, read-only system rules, mutable fields, default issue type rules, and persists changes |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned issue type schemes where `issue_type_schemes.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type scheme administration
4. Target scheme exists, is not soft-deleted, and belongs to the current tenant

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the tenant-owned scheme row
2. `updated_at` and `updated_by` are updated
3. Existing scheme items remain unchanged
4. Response returns the updated scheme payload

#### Failure Postconditions

1. No partial update is committed
2. Existing scheme items are unchanged
3. Error response is returned with validation, authorization, lookup, or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/issue-type-schemes/{schemeId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload |
| 6 | System | If `name` is provided, validates uniqueness among active tenant-owned schemes in the same tenant |
| 7 | System | If `default_issue_type_id` is provided, validates the issue type is tenant-visible |
| 8 | System | If current scheme already has items, validates the resulting `default_issue_type_id` remains included in those items |
| 9 | System | Begins database transaction |
| 10 | System | Applies allowed updates and sets `updated_by=userId` |
| 11 | System | Commits transaction |
| 12 | System | Returns HTTP 200 with updated scheme |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 5-10  
**Condition**: Request updates only a subset of mutable fields

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Treats omitted mutable fields as unchanged |
| 10.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 11

#### AF-2: Clear Description

**Branches from**: Main Flow Step 10  
**Condition**: Request explicitly sends `null` for `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.1 | System | Clears the `description` column to `NULL` |

**Rejoins**: Main Flow Step 11

### Exception Flows

#### EF-1: Scheme Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_SCHEME_NOT_FOUND` |

#### EF-2: Duplicate Scheme Name

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_SCHEME_NAME_ALREADY_EXISTS` |

#### EF-3: Default Issue Type Not Found in Visible Scope

**Triggered at**: Main Flow Step 7

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-4: Default Issue Type Not Included in Existing Items

**Triggered at**: Main Flow Step 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.E1 | System | Returns HTTP 422 with error: `ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS` |

#### EF-5: Validation Error

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with validation details |

#### EF-6: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-137-01 | Tenant callers may update only issue type schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-137-02 | Write lookup for update is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-137-03 | `tenant_id` is system-controlled and cannot be updated by API clients | Service layer |
| BR-PM-137-04 | Scheme name must remain unique among active tenant-owned issue type schemes in the same tenant | Service layer + DB constraint |
| BR-PM-137-05 | If `default_issue_type_id` is changed, it must resolve to a tenant-visible issue type | Service layer |
| BR-PM-137-06 | `UC-PM-137` updates metadata only; item membership and ordering are managed separately by `UC-PM-141` | UseCase layer |
| BR-PM-137-07 | When a scheme already has items, its resulting `default_issue_type_id` must remain included in the current item set | Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| schemeId | int64 | Yes | min:1 | Scheme identifier from path |
| name | string | No | min:1, max:255; unique in tenant | Updated scheme name |
| description | string | No | max:2000; `null` clears value | Updated description |
| default_issue_type_id | int64 | No | min:1; must be tenant-visible | Updated default issue type |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Scheme ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Updated scheme name |
| description | string | Updated description |
| default_issue_type_id | int64 | Updated default issue type ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
