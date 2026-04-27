# UC-PM-205 - Delete Status Category

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Soft-delete a tenant-owned status category that is no longer in use. Delete path is tenant-only. System-owned categories remain read-only and cannot be deleted via tenant APIs.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS_CATEGORY.DELETE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- Delete can target only tenant-owned rows (`tenant_id=tenantId`)

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/status-categories/{id}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Loads category in tenant-owned write scope |
| 4 | System | Validates category is not referenced by active statuses |
| 5 | System | Soft-deletes row (`deleted_at`) and updates audit fields |
| 6 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |
| 4.E1 | System | Returns HTTP 409 with error: `STATUS_CATEGORY_IN_USE` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-205-01 | Delete path is tenant-only; system-owned rows are not writable | Repository + Service layer |
| BR-PM-205-02 | Delete is soft-delete only (`deleted_at`) | Service layer |
| BR-PM-205-03 | Delete must be rejected while active statuses still reference category | Service layer |
| BR-PM-205-04 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| id | int64 | Yes | min:1 | Category ID from path |
