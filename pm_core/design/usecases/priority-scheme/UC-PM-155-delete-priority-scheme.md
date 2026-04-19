# UC-PM-155 - Delete Priority Scheme

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Project provisioning schema: `schema/00_project_provisioning.md`

## Use Case Specification

### Description

Soft-delete a tenant-owned priority scheme that is no longer bound to active projects. Delete is limited to the current tenant's own data; system-owned schemes remain visible through read APIs but are read-only and cannot be deleted through tenant APIs.

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/priority-schemes/{schemeId}` |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates no active project currently binds to the scheme |
| 6 | System | Begins database transaction |
| 7 | System | Soft-deletes the scheme and sets delete audit fields |
| 8 | System | Commits transaction |
| 9 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_SCHEME_NOT_FOUND` |
| 5.E1 | System | Returns HTTP 409 with error: `PRIORITY_SCHEME_BOUND_TO_PROJECT` |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-155-01 | Tenant callers may delete only priority schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-155-02 | Write lookup for delete is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-155-03 | Delete operation is soft delete only; deleted rows are excluded from active reads by `deleted_at IS NULL` | Service layer + Repository layer |
| BR-PM-155-04 | Delete must be rejected when any active project still binds to the scheme | Service layer |
