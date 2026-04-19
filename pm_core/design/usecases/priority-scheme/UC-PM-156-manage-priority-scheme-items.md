# UC-PM-156 - Manage Priority Scheme Items

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Description

Replace the ordered list of priorities assigned to a tenant-owned priority scheme. This use case supports adding, removing, and reordering priorities within the scheme by replacing all active scheme items inside one transaction.

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/priority-schemes/{schemeId}/items` with an ordered list of `priority_id` values |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates request list is non-empty and contains no duplicate `priority_id` values |
| 6 | System | Validates every requested `priority_id` resolves to a tenant-visible priority |
| 7 | System | Validates the scheme's `default_priority_id` is included in the resulting list |
| 8 | System | Compares the current item set with the requested item set and identifies removed priorities |
| 9 | System | If the scheme is used by active projects, validates removed priorities do not have active work items in those projects |
| 10 | System | Begins database transaction |
| 11 | System | Soft-deletes or removes existing active scheme items for the scheme |
| 12 | System | Inserts the new ordered item set with `sequence` starting from `1` |
| 13 | System | Updates scheme audit fields |
| 14 | System | Commits transaction |
| 15 | System | Returns HTTP 200 with the updated ordered item list |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_SCHEME_NOT_FOUND` |
| 5.E1 | System | Returns HTTP 400 with validation error for empty or duplicate item list |
| 6.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |
| 7.E1 | System | Returns HTTP 422 with error: `PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS` |
| 9.E1 | System | Returns HTTP 409 with error: `PRIORITY_SCHEME_IN_USE` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-156-01 | Tenant callers may manage items only for priority schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-156-02 | Write lookup for manage-items is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-156-03 | The scheme's `default_priority_id` must always be included in the active item list | Service layer |
| BR-PM-156-04 | Item replacement is transactional: the active item set is replaced atomically, not incrementally | UseCase layer |
| BR-PM-156-05 | Resulting item order is defined solely by request order and persisted via `sequence` starting at `1` | Service layer |
| BR-PM-156-06 | Duplicate `priority_id` values are not allowed in the resulting item list | Service layer + DB constraint |
| BR-PM-156-07 | Priorities removed from a scheme cannot be removed when active projects using that scheme still contain active work items of those priorities | Service layer |
