# UC-PM-011 - Create Project Category

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
| **Use Case ID** | UC-PM-011 |
| **Use Case Name** | Create Project Category |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-12 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-scoped project category used to classify projects in PM Core. A category is lightweight metadata for grouping and filtering projects; it does not change project permissions or scheme bindings. The API always creates the category inside the caller's tenant scope and enforces unique category names per active tenant data.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates project categories for the current tenant |
| System | System | Validates tenant admin authority, enforces uniqueness, persists the category, and writes outbox event |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for project-category administration
4. The requested category `name` does not already exist among active categories in the same tenant

### Postconditions

#### Success Postconditions

1. A new row is persisted in `project_categories` with `tenant_id=tenantId`
2. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
3. A `PROJECT_CATEGORY_CREATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.project-category.events`
4. Response returns the created category payload

#### Failure Postconditions

1. No category row is committed
2. No outbox event is committed
3. Error response is returned with validation or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/project-categories` with category data |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: required `name` and optional `description` |
| 5 | System | Validates `name` is unique among active categories in the same tenant |
| 6 | System | Begins database transaction |
| 7 | System | Creates category with `tenant_id=tenantId` |
| 8 | System | Persists `PROJECT_CATEGORY_CREATED` to domain outbox |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created category |

### Alternative Flows

#### AF-1: Optional Description Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without description |
| 7.1 | System | Persists `description = NULL` |

**Rejoins**: Main Flow Step 8

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Category Name

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `CATEGORY_NAME_ALREADY_EXISTS` |

#### EF-3: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-011-01 | Category name must be unique among active categories in the same tenant | Service layer + DB constraint |
| BR-PM-011-02 | Create Project Category always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-011-03 | `name` is required and must be between 1 and 255 characters | DTO validation |
| BR-PM-011-04 | `description` is optional and may be stored as `NULL` when omitted | DTO + Service layer |
| BR-PM-011-05 | Domain events use outbox pattern: `PROJECT_CATEGORY_CREATED` is stored in the same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255; unique in tenant | Category name |
| description | string | No | max:2000 | Category description |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated category ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Category name |
| description | string | Category description |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
