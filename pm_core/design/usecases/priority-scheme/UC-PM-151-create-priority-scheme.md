# UC-PM-151 - Create Priority Scheme

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
| **Use Case ID** | UC-PM-151 |
| **Use Case Name** | Create Priority Scheme |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-owned priority scheme in PM Core. The scheme defines a reusable subset and ordering of priorities that projects may bind to later. `UC-PM-151` creates only the scheme metadata and default priority reference; ordered scheme items are managed separately by `UC-PM-156`.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| PM Admin | Primary | Creates tenant-owned priority schemes for the current tenant |
| System | System | Validates tenant admin authority, enforces tenant scope and uniqueness, validates the default priority reference, and persists the new scheme |

### Authorization (Tenant-Scoped Admin RBAC)

- This use case is protected by tenant-scoped PM administration RBAC, not Jira project permission schemes
- Caller must be authenticated and authorized as PM Admin for the current `tenantId`
- `tenant_id` is always resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority scheme administration
4. The requested scheme `name` does not already exist among active tenant-owned priority schemes in the same tenant
5. The requested `default_priority_id` is visible to the tenant: either tenant-owned or a system-owned read-only priority

### Postconditions

#### Success Postconditions

1. A new row is persisted in `priority_schemes` with `tenant_id=tenantId`
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
| 1 | PM Admin | Sends `POST /api/v1/priority-schemes` with scheme metadata |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Validates input data: required `name`, required `default_priority_id`, and optional `description` |
| 5 | System | Validates `name` is unique among active tenant-owned schemes in the same tenant |
| 6 | System | Validates `default_priority_id` resolves to a tenant-visible priority |
| 7 | System | Begins database transaction |
| 8 | System | Creates scheme with `tenant_id=tenantId` |
| 9 | System | Commits transaction |
| 10 | System | Returns HTTP 201 with created scheme |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |
| 5.E1 | System | Returns HTTP 409 with error: `PRIORITY_SCHEME_NAME_ALREADY_EXISTS` |
| 6.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-151-01 | Create Priority Scheme always writes a tenant-owned row with `tenant_id` taken from JWT context | UseCase layer |
| BR-PM-151-02 | Tenant callers may create priority schemes only inside their own tenant scope | Authorization + Repository layer |
| BR-PM-151-03 | Scheme name must be unique among active tenant-owned priority schemes in the same tenant | Service layer + DB constraint |
| BR-PM-151-04 | `default_priority_id` must resolve to a tenant-visible priority | Service layer |
| BR-PM-151-05 | `UC-PM-151` creates only scheme metadata; item membership and order are managed separately by `UC-PM-156` | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:255; unique in tenant | Scheme name |
| description | string | No | max:2000 | Description |
| default_priority_id | int64 | Yes | min:1; must be tenant-visible | Default priority for the scheme |
