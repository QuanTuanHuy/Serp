# UC-PM-215 - Delete Status

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-19

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Soft-delete a tenant-owned status that is no longer in use. Delete path is tenant-only. System-owned statuses remain read-only and cannot be deleted via tenant APIs.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS.DELETE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- Delete can target only tenant-owned rows (`tenant_id=tenantId`)

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `DELETE /api/v1/statuses/{id}` |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Loads status in tenant-owned write scope |
| 4 | System | Validates status is not referenced by workflow steps |
| 5 | System | Validates status is not referenced by active work items |
| 6 | System | Soft-deletes row (`deleted_at`) and updates audit fields |
| 7 | System | Returns HTTP 200 with deletion confirmation |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_NOT_FOUND` |
| 4.E1 | System | Returns HTTP 409 with error: `STATUS_IN_USE_BY_WORKFLOW` |
| 5.E1 | System | Returns HTTP 409 with error: `STATUS_IN_USE_BY_WORK_ITEMS` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-215-01 | Delete path is tenant-only; system-owned rows are not writable | Repository + Service layer |
| BR-PM-215-02 | Delete is soft-delete only (`deleted_at`) | Service layer |
| BR-PM-215-03 | Delete must be rejected while workflow steps still reference status | Service layer |
| BR-PM-215-04 | Delete must be rejected while active work items still reference status | Service layer |
| BR-PM-215-05 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| id | int64 | Yes | min:1 | Status ID from path |
