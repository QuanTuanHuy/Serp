# UC-PM-214 - List Statuses

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-19

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Return a paginated list of statuses visible to current tenant. List includes tenant-owned rows and visible system-owned rows. API excludes soft-deleted data and foreign-tenant rows.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS.READ`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/statuses` with optional filters |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Parses query params (`search`, `is_system`, `status_category_id`, paging, sorting) |
| 4 | System | Builds visible-scope query (`tenant_id=tenantId` or system-owned) with `deleted_at IS NULL` |
| 5 | System | Applies filters, sorting, pagination |
| 6 | System | Returns HTTP 200 with paged result |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-214-01 | List includes tenant-owned + system-owned visible rows | Query layer |
| BR-PM-214-02 | Rows from other tenants are never returned | Query layer |
| BR-PM-214-03 | `status_category_id` filter applies in visible scope | Query layer |
| BR-PM-214-04 | Default pagination: `page=0`, `pageSize=10`, max `pageSize=100` | Controller layer |
| BR-PM-214-05 | Default sort: `created_at DESC` | Query layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| page | int | No | min:0, default:0 | Page number |
| pageSize | int | No | min:1, max:100, default:10 | Items per page |
| search | string | No | min:1 | Search by `name` or `status_key` |
| status_category_id | int64 | No | min:1 | Filter by category |
| is_system | bool | No | true or false | Filter by ownership |
| sortBy | string | No | `name`, `status_key`, `created_at` | Sort field |
| sortOrder | string | No | `ASC`, `DESC` | Sort direction |
