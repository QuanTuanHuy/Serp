# UC-PM-012 - Update Project Category

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
| **Use Case ID** | UC-PM-012 |
| **Use Case Name** | Update Project Category |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of an existing project category in the current tenant. The caller may update `name` and `description`, but may not change ownership. Category updates affect future reads and filters for projects that reference the category; they do not change the linked projects themselves.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Updates project categories in the current tenant |
| System | System | Validates tenant scope, uniqueness, mutable fields, persists changes, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- Write APIs may mutate only tenant-owned categories where `project_categories.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-category administration
4. Target category exists, is not soft-deleted, and belongs to the current tenant

### Postconditions

#### Success Postconditions

1. Requested mutable fields are updated on the category row
2. `updated_at` and `updated_by` are updated
3. A `PROJECT_CATEGORY_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-category.events`
4. Response returns the updated category payload

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Error response is returned with validation, authorization, conflict, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/project-categories/{categoryId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads category by `id=categoryId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload |
| 6 | System | If `name` is supplied and changed, validates uniqueness within tenant scope |
| 7 | System | Begins database transaction |
| 8 | System | Applies allowed updates and sets `updated_by=userId` |
| 9 | System | Persists `PROJECT_CATEGORY_UPDATED` to domain outbox |
| 10 | System | Commits transaction |
| 11 | System | Returns HTTP 200 with updated category |

### Alternative Flows

#### AF-1: Partial Metadata Update

**Branches from**: Main Flow Step 5-8  
**Condition**: Request updates only one mutable field

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.1 | System | Treats omitted mutable fields as unchanged |
| 8.1 | System | Persists only supplied mutable values |

**Rejoins**: Main Flow Step 9

#### AF-2: Clear Description

**Branches from**: Main Flow Step 8  
**Condition**: Request explicitly sends `null` for `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Clears `description` to `NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Category Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `CATEGORY_NOT_FOUND` |

#### EF-2: Duplicate Category Name

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 409 with error: `CATEGORY_NAME_ALREADY_EXISTS` |

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
| BR-PM-012-01 | Tenant callers may update only categories owned by their own tenant | Authorization + Repository layer |
| BR-PM-012-02 | `name` must remain unique among active categories in the same tenant | Service layer + DB constraint |
| BR-PM-012-03 | `tenant_id` is system-controlled and cannot be updated by API clients | Service layer |
| BR-PM-012-04 | `description` may be cleared to `NULL`; omitted fields remain unchanged | UseCase + Service layer |
| BR-PM-012-05 | Domain events use outbox pattern: `PROJECT_CATEGORY_UPDATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| categoryId | int64 | Yes | min:1 | Category identifier from path |
| name | string | No | min:1, max:255; unique in tenant when changed | Updated category name |
| description | string | No | max:2000; `null` clears value | Updated category description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Category ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Updated category name |
| description | string | Updated category description |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for write isolation |
