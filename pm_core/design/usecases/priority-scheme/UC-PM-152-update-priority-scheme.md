# UC-PM-152 - Update Priority Scheme

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-152 |
| **Use Case Name** | Update Priority Scheme |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-18 |
| **Priority** | Medium |
| **Complexity** | Simple |

### Description

Update mutable metadata of a tenant-owned priority scheme. The caller may update `name`, `description`, and `default_priority_id`, but may not change ownership and may not use this endpoint to replace scheme items.

### Authorization (Tenant-Scoped Admin RBAC)

- Write APIs may mutate only tenant-owned priority schemes where `priority_schemes.tenant_id = tenantId`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Caller has tenant-scoped PM Admin authority for priority scheme administration
4. Target scheme exists, is not soft-deleted, and belongs to the current tenant

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/priority-schemes/{schemeId}` with mutable field updates |
| 2 | System | Validates JWT and extracts `userId` and `tenantId` |
| 3 | System | Validates caller has tenant-scoped PM Admin authority |
| 4 | System | Loads scheme by `id=schemeId`, `tenant_id=tenantId`, `deleted_at IS NULL` |
| 5 | System | Validates input payload |
| 6 | System | If `name` is provided, validates uniqueness among active tenant-owned schemes in the same tenant |
| 7 | System | If `default_priority_id` is provided, validates the priority is tenant-visible |
| 8 | System | If current scheme already has items, validates the resulting `default_priority_id` remains included in those items |
| 9 | System | Begins database transaction |
| 10 | System | Applies allowed updates and sets `updated_by=userId` |
| 11 | System | Commits transaction |
| 12 | System | Returns HTTP 200 with updated scheme |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `PRIORITY_SCHEME_NOT_FOUND` |
| 6.E1 | System | Returns HTTP 409 with error: `PRIORITY_SCHEME_NAME_ALREADY_EXISTS` |
| 7.E1 | System | Returns HTTP 404 with error: `PRIORITY_NOT_FOUND` |
| 8.E1 | System | Returns HTTP 422 with error: `PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS` |
| 5.E1 | System | Returns HTTP 400 with validation details |
| 3.E1 | System | Returns HTTP 403 with authorization error |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-152-01 | Tenant callers may update only priority schemes owned by their own tenant | Authorization + Repository layer |
| BR-PM-152-02 | Write lookup for update is tenant-only; system-owned schemes remain visible only through read APIs and are not addressable through tenant write paths | Service layer |
| BR-PM-152-03 | Scheme name must remain unique among active tenant-owned priority schemes in the same tenant | Service layer + DB constraint |
| BR-PM-152-04 | If `default_priority_id` is changed, it must resolve to a tenant-visible priority | Service layer |
| BR-PM-152-05 | `UC-PM-152` updates metadata only; item membership and ordering are managed separately by `UC-PM-156` | UseCase layer |
