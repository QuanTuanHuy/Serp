# UC-PM-132 - Update Issue Type

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
| **Use Case ID** | UC-PM-132 |
| **Use Case Name** | Update Issue Type |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-11 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of a tenant-owned issue type. The caller may update fields such as `name`, `description`, `icon_url`, and `hierarchy_level`, but may not change ownership, may not change `type_key`, and may not modify a system-owned issue type through the tenant API.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates tenant-owned issue types in the current tenant |
| System | System | Validates tenant scope, read-only system rules, mutable fields, persists changes, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned issue types where `issue_types.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type administration
4. Target issue type exists, is not soft-deleted, and belongs to the current tenant

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the tenant-owned issue type row
2. `updated_at` and `updated_by` are updated
3. A `ISSUE_TYPE_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.issuetype.events`
4. Response returns the updated issue type payload

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/issue-types/{issueTypeId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads issue type by `id=issueTypeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload |
| 6 | System | Rejects immutable field changes such as `type_key`, `tenant_id`, or `is_system` |
| 7 | System | Begins database transaction |
| 8 | System | Applies allowed updates and sets `updated_by=userId` |
| 9 | System | Persists `ISSUE_TYPE_UPDATED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with updated issue type |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 5-8  
**Condition**: Request updates only a subset of mutable fields

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Treats omitted mutable fields as unchanged |
| 8.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 9

#### AF-2: Clear Optional Metadata

**Branches from**: Main Flow Step 8  
**Condition**: Request explicitly sends `null` for `description` or `icon_url`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Clears the corresponding column to `NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Issue Type Not Found in Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-2: Immutable Field Update Rejected

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 400 with validation error for immutable fields |

#### EF-3: Validation Error

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 400 with validation details |

#### EF-4: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-132-01 | Tenant callers may update only issue types owned by their own tenant | Authorization + Repository layer |
| BR-PM-132-02 | `type_key` is immutable after creation | Service layer |
| BR-PM-132-03 | `tenant_id` and `is_system` are system-controlled fields and cannot be updated by API clients | Service layer |
| BR-PM-132-04 | System-owned issue types are visible through read APIs but are read-only and cannot be updated through tenant APIs | Service layer |
| BR-PM-132-05 | Issue types belonging to another tenant must never be updated and should not be exposed through write lookup | Repository layer |
| BR-PM-132-06 | Domain events use outbox pattern: `ISSUE_TYPE_UPDATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| issueTypeId | int64 | Yes | min:1 | Issue type identifier from path |
| name | string | No | min:1, max:255 | Updated display name |
| description | string | No | max:2000; `null` clears value | Updated description |
| icon_url | string | No | valid URL, max:255; `null` clears value | Updated icon URL |
| hierarchy_level | int | No | one of `0`, `1`, `2` | Updated hierarchy level |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Issue type ID |
| tenant_id | int64 | Owning tenant ID |
| type_key | string | Stable issue type key |
| name | string | Updated display name |
| description | string | Updated description |
| icon_url | string | Updated icon URL |
| hierarchy_level | int | Updated hierarchy level |
| is_system | bool | Always `false` for writable tenant-owned rows |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
