# UC-PM-213 - Get Status by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-19

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Retrieve one status visible to current tenant by ID. Read path may return tenant-owned or system-owned status. Soft-deleted rows and rows from other tenants are never returned.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS.READ`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/statuses/{id}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Loads visible row where `id` matches and row belongs to tenant or system scope |
| 4 | System | Resolves linked status category in visible scope |
| 5 | System | Returns HTTP 200 with status details and read-only marker |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_NOT_FOUND` |
| 4.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-213-01 | Read scope includes tenant-owned + system-owned statuses visible to tenant | Repository layer |
| BR-PM-213-02 | Soft-deleted rows are excluded | Repository layer |
| BR-PM-213-03 | Returning system-owned row does not grant write permission | Service layer |

### Data Requirements

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Status ID |
| tenant_id | int64 | Owning tenant ID or `0` for system row |
| status_key | string | Stable key |
| name | string | Display name |
| description | string | Description |
| icon_url | string | Icon URL |
| status_category_id | int64 | Linked category |
| is_system | bool | System-owned marker |
| read_only | bool | Whether row is writable by tenant APIs |
