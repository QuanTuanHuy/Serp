# UC-PM-201 - Create Status Category

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-201 |
| **Use Case Name** | Create Status Category |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Create a new tenant-owned status category in PM Core. Status categories are reusable dictionary data for workflow statuses. The API always creates rows inside caller tenant scope and never creates system-owned categories.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS_CATEGORY.CREATE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- `tenant_id` is resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for status-category administration
4. Requested `key` does not already exist among active status categories in current tenant

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/status-categories` with category payload |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Validates caller permission |
| 4 | System | Validates input: `name`, `key`, optional `color` |
| 5 | System | Validates `key` uniqueness in tenant |
| 6 | System | Creates row with `tenant_id=tenantId`, `is_system=false` |
| 7 | System | Returns HTTP 201 with created payload |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |
| 5.E1 | System | Returns HTTP 409 with error: `STATUS_CATEGORY_KEY_ALREADY_EXISTS` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-201-01 | Create always writes tenant-owned row using `tenant_id` from JWT context | UseCase layer |
| BR-PM-201-02 | Tenant callers may create categories only in their own tenant scope | Authorization + Repository layer |
| BR-PM-201-03 | `key` must be unique among active status categories in same tenant | Service layer + DB constraint |
| BR-PM-201-04 | API cannot create system-owned categories | Service layer |
| BR-PM-201-05 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| name | string | Yes | min:1, max:50 | Category name |
| key | string | Yes | min:1, max:50; unique in tenant | Stable key |
| color | string | No | max:50 | Display color name |
