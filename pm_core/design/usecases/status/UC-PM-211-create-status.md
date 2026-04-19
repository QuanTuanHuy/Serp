# UC-PM-211 - Create Status

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-19

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-211 |
| **Use Case Name** | Create Status |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-19 |
| **Priority** | High |
| **Complexity** | Simple |

### Description

Create a new tenant-owned status in PM Core. Statuses are reusable workflow dictionary entries used by workflow steps and work items. The API always creates rows in caller tenant scope and never creates system-owned statuses.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS.CREATE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- `tenant_id` is resolved from JWT context and never accepted from request payload

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for status administration
4. Requested `status_key` does not already exist among active statuses in current tenant
5. `status_category_id` exists in visible scope of current tenant (tenant-owned or system-owned)

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `POST /api/v1/statuses` with status payload |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Validates caller permission |
| 4 | System | Validates input: `status_key`, `name`, optional metadata, `status_category_id` |
| 5 | System | Validates `status_key` uniqueness in tenant |
| 6 | System | Validates `status_category_id` is visible to tenant |
| 7 | System | Creates row with `tenant_id=tenantId`, `is_system=false` |
| 8 | System | Returns HTTP 201 with created payload |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 400 with validation details |
| 5.E1 | System | Returns HTTP 409 with error: `STATUS_KEY_ALREADY_EXISTS` |
| 6.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-211-01 | Create always writes tenant-owned row using `tenant_id` from JWT context | UseCase layer |
| BR-PM-211-02 | Tenant callers may create statuses only in their own tenant scope | Authorization + Repository layer |
| BR-PM-211-03 | `status_key` must be unique among active statuses in same tenant | Service layer + DB constraint |
| BR-PM-211-04 | API cannot create system-owned statuses | Service layer |
| BR-PM-211-05 | `status_category_id` must be visible in tenant scope (tenant-owned + system-owned) | Service layer |
| BR-PM-211-06 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| status_key | string | Yes | min:1, max:100; unique in tenant | Stable key |
| name | string | Yes | min:1, max:255 | Display name |
| description | string | No | max:2000 | Description |
| icon_url | string | No | valid URL; max:255 | Icon URL |
| status_category_id | int64 | Yes | min:1; must exist in visible scope | Parent status category |
