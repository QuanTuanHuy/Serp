# UC-PM-203 - Get Status Category by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Retrieve one status category visible to the current tenant by ID. Read path may return tenant-owned or system-owned category. Soft-deleted rows and rows from other tenants are never returned.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS_CATEGORY.READ`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/status-categories/{id}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Loads visible row where `id` matches and row belongs to tenant or system scope |
| 4 | System | Returns HTTP 200 with category details and read-only marker |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-203-01 | Read scope includes tenant-owned + system-owned categories visible to tenant | Repository layer |
| BR-PM-203-02 | Soft-deleted rows are excluded | Repository layer |
| BR-PM-203-03 | Returning system-owned row does not grant write permission | Service layer |

### Data Requirements

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Category ID |
| tenant_id | int64 | Owning tenant ID or `0` for system row |
| name | string | Category name |
| key | string | Stable key |
| color | string | Display color |
| is_system | bool | System-owned marker |
| read_only | bool | Whether row is writable by tenant APIs |
