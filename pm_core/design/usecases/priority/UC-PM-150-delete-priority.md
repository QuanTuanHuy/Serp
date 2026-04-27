# UC-PM-150 - Delete Priority

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
| **Use Case ID** | UC-PM-150 |
| **Use Case Name** | Delete Priority |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned priority that is no longer in use. Delete is limited to the current tenant's own data; system-owned priorities remain visible through read APIs but are read-only and cannot be deleted through tenant APIs. The system must reject deletion when the priority is still referenced by active work items or active tenant priority schemes that depend on it.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes unused tenant-owned priorities |
| System | System | Validates tenant ownership, checks usage constraints, performs soft delete, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned priorities where `priorities.tenant_id = tenantId`
- System-owned priorities are read-only and cannot be deleted through tenant APIs

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority administration
4. Target priority exists, is not soft-deleted, and belongs to the current tenant
5. Target priority is not referenced by active work items or active tenant priority scheme configuration

### Postconditions

#### Success Postconditions

1. Target priority is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. A `PRIORITY_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.priority.events`
4. Deleted priority is excluded from active read/list queries

#### Failure Postconditions

1. No delete is committed
2. No outbox event is committed
3. Error response is returned with authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/priorities/{priorityId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads priority by `id=priorityId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates the priority is not a system-owned read-only row |
| 6 | System | Validates the priority is not in use by active work items or active priority scheme references in the same tenant |
| 7 | System | Begins database transaction |
| 8 | System | Soft-deletes the priority and sets delete audit fields |
| 9 | System | Persists `PRIORITY_DELETED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

#### EF-1: Priority Not Found in Tenant Scope

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |

#### EF-2: Priority Still In Use

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `PRIORITY_IN_USE` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-150-01 | Tenant callers may delete only priorities owned by their own tenant | Authorization + Repository layer |
| BR-PM-150-02 | System-owned priorities are visible through read APIs but are read-only and cannot be deleted through tenant APIs | Service layer |
| BR-PM-150-03 | Delete operation is soft delete only; data is excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-150-04 | Delete must be rejected when active work items still reference the priority | Service layer |
| BR-PM-150-05 | Delete must be rejected when active tenant priority schemes still include or default to the priority | Service layer |
| BR-PM-150-06 | Domain events use outbox pattern: `PRIORITY_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| priorityId | int64 | Yes | min:1 | Priority identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted priority ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
