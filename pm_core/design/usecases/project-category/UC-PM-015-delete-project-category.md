# UC-PM-015 - Delete Project Category

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
| **Use Case ID** | UC-PM-015 |
| **Use Case Name** | Delete Project Category |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Low |
| **Complexity** | Simple |

### Description

Soft-delete a tenant-owned project category that is no longer referenced by active projects. Delete is limited to the current tenant's own data and must be rejected when the category is still assigned to any active project. Soft-deleted categories are excluded from normal reads and lists but remain in storage for audit and recovery.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Deletes unused project categories in the current tenant |
| System | System | Validates tenant ownership and usage constraints, performs soft delete, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Write APIs may delete only tenant-owned categories where `project_categories.tenant_id = tenantId`
- Delete is soft delete only

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-category administration
4. Target category exists, is not soft-deleted, and belongs to the current tenant
5. Target category is not referenced by active projects in the same tenant

### Postconditions

#### Success Postconditions

1. Target category is soft-deleted by setting `deleted_at`
2. `updated_at` and `updated_by` are updated as part of the delete operation
3. A `PROJECT_CATEGORY_DELETED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-category.events`
4. Deleted category is excluded from active read/list queries

#### Failure Postconditions

1. No delete is committed
2. No outbox event is committed
3. Error response is returned with authorization, lookup, or usage-conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/project-categories/{categoryId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads category by `id=categoryId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates the category is not referenced by active projects in the same tenant |
| 6 | System | Begins database transaction |
| 7 | System | Soft-deletes the category and sets delete audit fields |
| 8 | System | Persists `PROJECT_CATEGORY_DELETED` to domain outbox |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

#### EF-1: Category Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `CATEGORY_NOT_FOUND` |

#### EF-2: Category Still In Use

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `CATEGORY_IN_USE` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-015-01 | Tenant callers may delete only categories owned by their own tenant | Authorization + Repository layer |
| BR-PM-015-02 | Delete operation is soft delete only; data is excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-015-03 | Delete must be rejected when active projects still reference the category | Service layer |
| BR-PM-015-04 | Domain events use outbox pattern: `PROJECT_CATEGORY_DELETED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| categoryId | int64 | Yes | min:1 | Category identifier from path |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Deleted category ID |
| deleted | bool | Delete confirmation flag |
| deleted_at | timestamp | Soft delete time |
| updated_by | int64 | User who performed delete |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the delete |
| tenantId | JWT token | Tenant scope for write isolation |
