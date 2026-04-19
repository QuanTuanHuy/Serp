# UC-PM-202 - Update Status Category

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-18

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Workflow schema: `schema/03_workflows.md`

## Use Case Specification

### Description

Update mutable metadata of a tenant-owned status category. Write path is tenant-only. System-owned categories are visible in read APIs but cannot be modified through tenant write APIs.

### Authorization (Tenant-Scoped Admin RBAC)

- Permission: `PM.STATUS_CATEGORY.UPDATE`
- Caller must be authenticated and authorized as PM Admin in current `tenantId`
- Write APIs can modify only rows where `tenant_id=tenantId`

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | PM Admin | Sends `PUT /api/v1/status-categories/{id}` with mutable fields |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Loads category by tenant-owned scope (`tenant_id=tenantId`) |
| 4 | System | Validates payload and uniqueness if `key` changed |
| 5 | System | Persists update and audit fields |
| 6 | System | Returns HTTP 200 with updated payload |

### Exception Flows

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `STATUS_CATEGORY_NOT_FOUND` |
| 4.E1 | System | Returns HTTP 409 with error: `STATUS_CATEGORY_KEY_ALREADY_EXISTS` |
| 4.E2 | System | Returns HTTP 400 with validation details |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-202-01 | Update path is tenant-only; system-owned rows are not writable | Repository + Service layer |
| BR-PM-202-02 | `key` must remain unique among active rows in tenant | Service layer + DB constraint |
| BR-PM-202-03 | Omitted fields remain unchanged; explicit `null` for optional fields clears value | UseCase layer |
| BR-PM-202-04 | No Kafka/outbox publication is performed in this scope | UseCase layer |

### Data Requirements

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| id | int64 | Yes | min:1 | Category ID from path |
| name | string | No | min:1, max:50 | Updated category name |
| key | string | No | min:1, max:50; unique in tenant | Updated stable key |
| color | string | No | max:50; `null` clears | Updated display color |
