# UC-PM-136 - Create Issue Type Scheme

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-136 |
| **Use Case Name** | Create Issue Type Scheme |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-owned issue type scheme in PM Core. The scheme defines a reusable issue-type catalog that projects may bind to later. `UC-PM-136` creates only the scheme metadata and default issue type reference; ordered scheme items are managed separately by `UC-PM-141`.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned issue type schemes for the current tenant |
| System | System | Validates tenant admin authority, enforces tenant scope and uniqueness, validates the default issue type reference, and persists the new scheme |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for issue type scheme administration
4. The requested scheme `name` does not already exist among active tenant-owned issue type schemes in the same tenant
5. The requested `default_issue_type_id` is visible to the tenant: either tenant-owned or a system-owned read-only issue type

### Postconditions

#### Success Postconditions

1. A new row is persisted in `issue_type_schemes` with `tenant_id=tenantId`
2. Audit fields `created_at`, `updated_at`, `created_by`, and `updated_by` are set
3. No scheme items are created automatically by this use case
4. Response returns the created scheme payload

#### Failure Postconditions

1. No scheme row is committed
2. No partial data is persisted
3. Error response is returned with validation, authorization, lookup, or conflict details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/issue-type-schemes` with scheme metadata |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: required `name`, required `default_issue_type_id`, and optional `description` |
| 5 | System | Validates `name` is unique among active tenant-owned schemes in the same tenant |
| 6 | System | Validates `default_issue_type_id` resolves to a tenant-visible issue type |
| 7 | System | Begins database transaction |
| 8 | System | Creates scheme with `tenant_id=tenantId` |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created scheme |

### Alternative Flows

#### AF-1: Description Omitted

**Branches from**: Main Flow Step 4  
**Condition**: Request omits `description`

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.1 | System | Accepts request without description |
| 8.1 | System | Persists `description=NULL` |

**Rejoins**: Main Flow Step 9

### Exception Flows

#### EF-1: Validation Error

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |

#### EF-2: Duplicate Scheme Name

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 409 with error: `ISSUE_TYPE_SCHEME_NAME_ALREADY_EXISTS` |

#### EF-3: Default Issue Type Not Found in Visible Scope

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 404 with error: `ISSUE_TYPE_NOT_FOUND` |

#### EF-4: Tenant Admin Permission Denied

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-136-01 | Create Issue Type Scheme always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-136-02 | Tenant callers may create issue type schemes only inside their own tenant scope | Authorization + Repository layer |
| BR-PM-136-03 | Scheme name must be unique among active tenant-owned issue type schemes in the same tenant | Service layer + DB constraint |
| BR-PM-136-04 | `default_issue_type_id` must resolve to a tenant-visible issue type | Service layer |
| BR-PM-136-05 | `UC-PM-136` creates only scheme metadata; item membership and order are managed separately by `UC-PM-141` | UseCase layer |
| BR-PM-136-06 | API cannot create system-owned issue type schemes | Service layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255; unique in tenant | Scheme name |
| description | string | No | max:2000 | Description |
| default_issue_type_id | int64 | Yes | min:1; must be tenant-visible | Default issue type for the scheme |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated scheme ID |
| tenant_id | int64 | Owning tenant ID |
| name | string | Scheme name |
| description | string | Description |
| default_issue_type_id | int64 | Default issue type ID |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |
| updated_at | timestamp | Last update time |
| updated_by | int64 | Last updater user ID |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the action |
| tenantId | JWT token | Tenant scope for create operation |
