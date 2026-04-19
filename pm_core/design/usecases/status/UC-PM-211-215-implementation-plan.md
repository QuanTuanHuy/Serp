# Status UC-PM-211..215 Implementation Plan

> Scope: status CRUD use cases in PM Core
> Date: 2026-04-19

## Objective

Implement `UC-PM-211` to `UC-PM-215` with behavior aligned to canonical status use case docs:

- Read path includes tenant-owned + system-owned statuses
- Write path is tenant-only
- Delete status is blocked when in use by workflow steps or active work items
- No outbox/Kafka publication for these status CRUD write flows

## Existing Baseline

- Domain/infra currently has partial status support: create bulk, get by id, get by status key, list tenant-only
- No dedicated application package/controller for status CRUD APIs
- Existing status category implementation provides target pattern for architecture and API style

## Target API Surface

- `POST /api/v1/statuses` - create status
- `PUT /api/v1/statuses/{id}` - update status
- `GET /api/v1/statuses/{id}` - get status by id (visible scope)
- `GET /api/v1/statuses` - list statuses (visible scope, paged)
- `DELETE /api/v1/statuses/{id}` - delete status (soft delete)

## Domain Layer Tasks

1. Add status update DTO:
   - `domain/workitem/dto/StatusUpdateData.java`
2. Add status list criteria:
   - `domain/workitem/query/StatusListCriteria.java`
3. Extend status service contract:
   - `domain/workitem/service/IStatusService.java`
   - add create/update/get/list/delete methods for single-status CRUD
   - keep existing methods used by provisioning flows
4. Implement status CRUD rules in:
   - `domain/workitem/service/impl/StatusService.java`
   - normalize/validate fields
   - write scope tenant-only
   - read scope include system
   - use `TextNormalizationUtils` for text fields

## Port & Infrastructure Tasks

1. Extend status port:
   - `domain/workitem/port/IStatusPort.java`
   - add visible list paging/filter, update, existence checks, key uniqueness helpers
2. Extend status repository:
   - `infrastructure/store/repository/IStatusRepository.java`
   - visible-scope filtered paged query
   - key uniqueness checks (`existsByTenantIdAndStatusKey...`)
   - in-use checks for delete path where appropriate
3. Extend status adapter:
   - `infrastructure/store/adapter/StatusAdapter.java`
   - implement new port methods
   - map sorting via `PageableUtils`
4. Add workflow-step reference check for delete guard:
   - `domain/workflow/port/IWorkflowStepPort.java`
   - `infrastructure/store/repository/IWorkflowStepRepository.java`
   - `infrastructure/store/adapter/WorkflowStepAdapter.java`
   - method: exists active workflow step by `statusId` in tenant scope
5. Add active work item reference check for delete guard:
   - `domain/workitem/port/read/IWorkItemReadPort.java`
   - `infrastructure/store/repository/IWorkItemRepository.java`
   - `infrastructure/store/adapter/WorkItemReadAdapter.java`
   - method: exists active work item by `statusId` in tenant scope

## Application Layer Tasks

Create package `application/status/**`:

- `StatusView`
- command:
  - create: command + handler
  - update: command + handler
  - delete: command + handler + result
- query:
  - get: query + handler
  - list: query + handler

Conventions:

- Use `PageViews.from(...)` for list responses
- Mark system rows as `readOnly=true` in get/list view mapping

## UI Layer Tasks

1. Add path constant:
   - `ui/rest/shared/constant/PathConstants.java`
   - `STATUSES = API_BASE_PATH + "/statuses"`
2. Create controller and request DTOs:
   - `ui/rest/status/StatusController.java`
   - `ui/rest/status/dto/request/CreateStatusRequest.java`
   - `ui/rest/status/dto/request/UpdateStatusRequest.java`
3. Reuse `AuthUtils` and `ResponseUtils` patterns from existing controllers

## Error Handling & Rules Mapping

Use existing error codes where possible:

- `STATUS_NOT_FOUND`
- `STATUS_KEY_ALREADY_EXISTS`
- `STATUS_CATEGORY_NOT_FOUND`
- `STATUS_IN_USE_BY_WORKFLOW`
- `STATUS_IN_USE_BY_WORK_ITEMS`

Check `GlobalExceptionHandler` mapping and add missing conflict mapping only if needed.

## Data & Validation Rules

- `status_key`: required on create, max 100, unique per tenant among active rows
- `name`: required on create, max 255
- `description`: optional, max 2000
- `icon_url`: optional, valid absolute URL, max 255
- `status_category_id`:
  - required on create
  - if provided in update, must be visible to tenant (tenant-owned or system-owned)

## Testing Plan

1. Domain tests:
   - add `domain/workitem/service/impl/StatusServiceTest.java`
2. Application tests:
   - add `application/status/StatusHandlersTest.java`
3. Regression tests to rerun:
   - `StatusCategoryServiceTest`
   - `StatusCategoryHandlersTest`
   - `IssueTypeServiceTest`
   - `PriorityServiceTest`

## Validation Commands

Run from `pm_core/`:

```bash
./mvnw.cmd "-Dtest=StatusServiceTest,StatusHandlersTest,StatusCategoryServiceTest,StatusCategoryHandlersTest" test
./mvnw.cmd "-Dtest=IssueTypeServiceTest,PriorityServiceTest" test
```

Optional broader check:

```bash
./mvnw.cmd test
```
