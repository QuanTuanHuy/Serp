# UC-PM-154 - List Priority Schemes

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Description

Return a paginated list of priority schemes visible to the current tenant. The list includes tenant-owned schemes and visible system-owned schemes exposed as reusable read-only configuration data. The list response returns scheme metadata only and does not expand ordered item details.

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/priority-schemes` with optional query filters |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Parses and validates query parameters |
| 5 | System | Builds query limited to tenant-visible active rows: `tenant_id=tenantId` or system-owned visible rows, with `deleted_at IS NULL` |
| 6 | System | Applies optional filters such as `search` and `is_system` |
| 7 | System | Executes count query for total items |
| 8 | System | Executes paginated query with sorting |
| 9 | System | Returns HTTP 200 with paginated scheme list |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-154-01 | List results include tenant-owned priority schemes plus visible system-owned read-only schemes | Query layer |
| BR-PM-154-02 | Rows from another tenant must never be returned | Query layer |
| BR-PM-154-03 | Soft-deleted rows are excluded from list results | Query layer |
| BR-PM-154-04 | Default pagination is `page=0`, `pageSize=10`, maximum `pageSize=100` | Controller layer |
| BR-PM-154-05 | Default sort is `created_at DESC` | Query layer |
