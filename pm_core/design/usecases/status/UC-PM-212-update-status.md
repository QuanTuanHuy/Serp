# UC-PM-212 - Update Status

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-19

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Update mutable metadata of a tenant-owned status. Write path is tenant-only. System-owned statuses are visible in read APIs but cannot be modified through tenant write APIs.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS.UPDATE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- Write APIs can modify only rows where `tenant_id=tenantId`

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/statuses/{id}` with mutable fields |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Loads status by tenant-owned scope (`tenant_id=tenantId`) |
| 4 | System | Validates payload and uniqueness if `status_key` changed |
| 5 | System | Validates `status_category_id` if changed (visible scope) |
| 6 | System | Persists update and audit fields |
| 7 | System | Returns HTTP 200 with updated payload |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_NOT_FOUND` |
| 4.E1 | System | Returns HTTP 409 with error: `STATUS_KEY_ALREADY_EXISTS` |
| 5.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |
| 4.E2 | System | Returns HTTP 400 with validation details |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-212-01 | Update path is tenant-only; system-owned rows are not writable | Repository + Service layer |
| BR-PM-212-02 | `status_key` must remain unique among active rows in tenant | Service layer + DB constraint |
| BR-PM-212-03 | `status_category_id` may point to tenant-owned or system-owned category visible to tenant | Service layer |
| BR-PM-212-04 | Omitted fields remain unchanged; explicit `null` for optional fields clears value | UseCase layer |
| BR-PM-212-05 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| id | int64 | Yes | min:1 | Status ID from path |
| status_key | string | No | min:1, max:100; unique in tenant | Updated stable key |
| name | string | No | min:1, max:255 | Updated display name |
| description | string | No | max:2000; `null` clears | Updated description |
| icon_url | string | No | valid URL; max:255; `null` clears | Updated icon |
| status_category_id | int64 | No | min:1; must exist in visible scope | Updated parent category |
