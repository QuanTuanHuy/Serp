# UC-PM-147 - Update Priority

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
| **Use Case ID** | UC-PM-147 |
| **Use Case Name** | Update Priority |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of a tenant-owned priority. The caller may update fields such as `name`, `description`, `icon_url`, `color`, and `sequence`, but may not change ownership and may not modify a system-owned priority through the tenant API.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates tenant-owned priorities in the current tenant |
| System | System | Validates tenant scope, read-only system rules, mutable fields, persists changes, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned priorities where `priorities.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority administration
4. Target priority exists, is not soft-deleted, and belongs to the current tenant

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the tenant-owned priority row
2. `updated_at` and `updated_by` are updated
3. A `PRIORITY_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.priority.events`
4. Response returns the updated priority payload

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/priorities/{priorityId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads priority by `id=priorityId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload |
| 6 | System | Rejects immutable field changes such as `tenant_id` or `is_system` |
| 7 | System | Begins database transaction |
| 8 | System | Applies allowed updates and sets `updated_by=userId` |
| 9 | System | Persists `PRIORITY_UPDATED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with updated priority |

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
**Condition**: Request explicitly sends `null` for `description`, `icon_url`, or `color`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Clears the corresponding column to `NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Priority Not Found in Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |

#### EF-2: Duplicate Priority Name

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `PRIORITY_NAME_ALREADY_EXISTS` |

#### EF-3: Immutable Field Update Rejected

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 400 with validation error for immutable fields |

#### EF-4: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-147-01 | Tenant callers may update only priorities owned by their own tenant | Authorization + Repository layer |
| BR-PM-147-02 | `name` must be unique among active priorities in the same tenant | Service layer + DB constraint |
| BR-PM-147-03 | `tenant_id` and `is_system` are system-controlled fields and cannot be updated by API clients | Service layer |
| BR-PM-147-04 | System-owned priorities are visible through read APIs but are read-only and cannot be updated through tenant APIs | Service layer |
| BR-PM-147-05 | Priorities belonging to another tenant must never be updated and should not be exposed through write lookup | Repository layer |
| BR-PM-147-06 | Domain events use outbox pattern: `PRIORITY_UPDATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| priorityId | int64 | Yes | min:1 | Priority identifier from path |
| name | string | No | min:1, max:50; unique in tenant | Updated priority label |
| description | string | No | max:2000; `null` clears value | Updated description |
| icon_url | string | No | valid URL, max:255; `null` clears value | Updated icon URL |
| color | string | No | hex color, max:20; `null` clears value | Updated display color |
| sequence | int | No | min:0 | Updated display order |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Priority ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Updated priority label |
| description | string | Updated description |
| icon_url | string | Updated icon URL |
| color | string | Updated display color |
| sequence | int | Updated display order |
| is_system | bool | Always `false` for writable tenant-owned rows |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
