# UC-PM-153 - Get Priority Scheme by ID

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Description

Retrieve one priority scheme visible to the current tenant by ID. Read APIs may return either a tenant-owned scheme or a system-owned scheme exposed to that tenant as reusable read-only configuration data. The detail response includes ordered scheme items and basic priority summaries for each item.

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `GET /api/v1/priority-schemes/{schemeId}` |
| 2 | System | Validates JWT and extracts `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId` if it belongs to the tenant or is a visible system-owned row, and validates `deleted_at IS NULL` |
| 5 | System | Loads scheme items in ascending `sequence` order within the same visible scope |
| 6 | System | Resolves lightweight priority details for each item and for `default_priority_id` |
| 7 | System | Builds response including ownership and `read_only` metadata |
| 8 | System | Returns HTTP 200 with scheme details |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_SCHEME_NOT_FOUND` |
| 6.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-153-01 | Read lookup is limited to schemes visible to the tenant: tenant-owned rows and system-owned rows exposed as reusable catalog data | Repository layer |
| BR-PM-153-02 | Rows from another tenant must not be returned | Repository layer |
| BR-PM-153-03 | Soft-deleted scheme rows and scheme items are excluded from read lookup | Repository layer |
| BR-PM-153-04 | Returned items must be ordered by `sequence ASC` | Query layer |
