# UC-PM-102 - Update Work Item

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.0
> Last Updated: 2026-04-08

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-102 |
| **Use Case Name** | Update Work Item |
| **Module** | PM Core |
| **Version** | 1.0 |
| **Last Updated** | 2026-04-08 |
| **Priority** | High |
| **Complexity** | Medium |

### Description

Update editable work item fields using Jira-aligned project authorization and EDIT-screen field rules. UC-PM-102 v1 supports partial updates for `summary`, `description`, `priority_id`, `assignee_id`, `due_date`, `time_original_estimate`, `security_level_id`, and `custom_fields`. Status changes remain in UC-PM-106, and changing `issue_type_id` is explicitly out of scope for UC-PM-102 v1.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Updates editable work item fields |
| System | System | Validates project permissions, issue security, field writability, custom field payload, persists update, and writes outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `EDIT_ISSUES`
- Conditional assignee update: if `assignee_id` is supplied, caller must satisfy `ASSIGN_ISSUES`; non-null target assignee must satisfy `ASSIGNABLE_USER`
- Conditional due date update: if `due_date` is supplied, caller must satisfy `SCHEDULE_ISSUES`
- Conditional security update: if `security_level_id` is supplied, caller must satisfy `SET_ISSUE_SECURITY`
- Conditional read/write constraint: if the target work item has `security_level_id`, caller must satisfy issue-security membership before editing

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists and is not archived
4. Work item exists in tenant scope and belongs to the target project
5. Caller is granted `BROWSE_PROJECTS` and `EDIT_ISSUES` for the target project
6. If the work item has `security_level_id`, caller is a member of the issue security level

### Postconditions

#### Success Postconditions

1. Editable work item fields are updated according to the request payload
2. Requested custom fields are replaced atomically: existing active values for the requested keys are soft-deleted, then new values are inserted when provided
3. Work item audit fields `updated_at` and `updated_by` are updated
4. A `WORK_ITEM_UPDATED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.workitem.events`
5. Response returns the updated work item state and `changed_fields`

#### Failure Postconditions

1. No partial update is committed
2. No outbox event is committed
3. Existing work item data remains unchanged
4. Error response is returned with validation, authorization, or lookup details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Team Member | Sends `PUT /api/v1/projects/{projectId}/work-items/{workItemId}` with partial field updates |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and group memberships |
| 3 | System | Loads project by `projectId` and validates it is not archived |
| 4 | System | Loads work item by `workItemId` and validates it belongs to the target project |
| 5 | System | Evaluates `BROWSE_PROJECTS` and `EDIT_ISSUES` permissions for the caller |
| 6 | System | If the work item has issue security, evaluates issue-security membership for the caller |
| 7 | System | Resolves the EDIT screen for the current `issue_type_id` and builds field writability rules from screen + field configuration |
| 8 | System | Validates that all requested system fields and custom fields are writable on the EDIT screen |
| 9 | System | Applies conditional permission checks for assignee, due date, and security-level updates |
| 10 | System | Resolves effective assignee, priority, and security level values |
| 11 | System | Resolves and validates requested custom field values for the current issue type |
| 12 | System | Validates required fields after applying the requested updates to the existing work item state |
| 13 | System | Persists work item changes and custom field value changes in one transaction |
| 14 | System | Persists `WORK_ITEM_UPDATED` to domain outbox with `changed_fields` metadata |
| 15 | System | Returns HTTP 200 with updated work item payload |

### Alternative Flows

#### AF-1: Clear Nullable System Field

**Branches from**: Main Flow Step 8-10  
**Condition**: Request explicitly sends `null` for `description`, `assignee_id`, `due_date`, or `security_level_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.1 | System | Treats the field as explicitly provided, not omitted |
| 10.1 | System | Clears the persisted value to `NULL` after authorization and field-rule validation |

**Rejoins**: Main Flow Step 11

#### AF-2: Replace or Clear Requested Custom Fields

**Branches from**: Main Flow Step 11-13  
**Condition**: Request includes `custom_fields`

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Validates each requested custom field key is writable on the EDIT screen |
| 11.2 | System | For non-null values, resolves context and validates the new value |
| 13.1 | System | Soft-deletes active values for the requested custom field keys |
| 13.2 | System | Inserts replacement values for non-null requests; `null` clears the requested field |

**Rejoins**: Main Flow Step 14

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Project Permission Denied

**Triggered at**: Main Flow Step 5 or 9

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Field Not Writable On Edit

**Triggered at**: Main Flow Step 8

| Step | Actor/System | Action |
|------|-------------|--------|
| 8.E1 | System | Returns HTTP 422 with error: `FIELD_NOT_WRITABLE_ON_UPDATE` and the rejected field key |

#### EF-6: Required Field Missing After Update

**Triggered at**: Main Flow Step 12

| Step | Actor/System | Action |
|------|-------------|--------|
| 12.E1 | System | Returns HTTP 400 with error: `REQUIRED_FIELDS_MISSING` and the missing field list |

#### EF-7: Invalid Assignee, Priority, or Security Level

**Triggered at**: Main Flow Step 10

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.E1 | System | If assignee does not exist, returns HTTP 404 with error: `USER_NOT_FOUND` |
| 10.E2 | System | If assignee does not satisfy `ASSIGNABLE_USER`, returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` |
| 10.E3 | System | If priority is not allowed in the project's priority scheme, returns HTTP 400 with error: `PRIORITY_NOT_IN_SCHEME` |
| 10.E4 | System | If security level is not allowed in the project's issue security scheme, returns HTTP 400 with error: `SECURITY_LEVEL_NOT_IN_SCHEME` |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-102-01 | Updating a work item requires `BROWSE_PROJECTS` and `EDIT_ISSUES` in the target project | Authorization layer |
| BR-PM-102-02 | If the target work item has issue security, caller must satisfy issue-security membership before editing | Authorization layer |
| BR-PM-102-03 | UC-PM-102 v1 does not support changing `issue_type_id`; issue-type changes are explicitly out of scope | UseCase layer |
| BR-PM-102-04 | `key`, `issue_no`, `status_id`, `workflow_step_id`, and `resolution_id` are immutable in UC-PM-102 v1 | Service layer |
| BR-PM-102-05 | Editable system fields are limited to `summary`, `description`, `priority_id`, `assignee_id`, `due_date`, `time_original_estimate`, and `security_level_id` | Service layer |
| BR-PM-102-06 | Requested fields must be visible on the resolved EDIT screen and not hidden by the effective field configuration | Service layer |
| BR-PM-102-07 | Updating `assignee_id` requires `ASSIGN_ISSUES`; non-null target assignee must satisfy `ASSIGNABLE_USER` | Authorization layer |
| BR-PM-102-08 | Updating `due_date` requires `SCHEDULE_ISSUES` | Authorization layer |
| BR-PM-102-09 | Updating `security_level_id` requires `SET_ISSUE_SECURITY`; non-null level must belong to the project's issue security scheme | Authorization + Service layer |
| BR-PM-102-10 | Requested custom fields replace only the keys present in the request; explicit `null` clears that requested custom field | Service layer |
| BR-PM-102-11 | Required-field validation is evaluated against the effective post-update state, not just the request payload | Service layer |
| BR-PM-102-12 | Domain events use the outbox pattern: `WORK_ITEM_UPDATED` is stored in the same transaction as the update and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| summary | string | No | max:512; if supplied and effective value becomes blank, request fails required validation | Updated summary |
| description | string | No | max:50000; `null` clears value | Updated description |
| priority_id | int64 | No | must be in project's priority scheme when non-null | Updated priority |
| assignee_id | int64 | No | non-null user must exist and satisfy `ASSIGNABLE_USER`; `null` unassigns | Updated assignee |
| due_date | int64 | No | non-negative epoch millis; `null` clears value | Updated due date |
| time_original_estimate | int64 | No | non-negative | Updated original estimate |
| security_level_id | int64 | No | must be in project's security scheme when non-null; `null` clears value | Updated security level |
| custom_fields | map<string, unknown> | No | keys must be writable custom field keys; non-null values validated per custom field type; `null` clears the requested field | Updated custom fields |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Work item ID |
| project_id | int64 | Owning project ID |
| issue_type_id | int64 | Existing issue type ID |
| key | string | Work item key |
| summary | string | Updated summary |
| description | string | Updated description |
| workflow_step_id | int64 | Existing workflow step ID |
| status_id | int64 | Existing status ID |
| priority_id | int64 | Updated priority |
| assignee_id | int64 | Updated assignee |
| security_level_id | int64 | Updated security level |
| due_date | int64 | Updated due date |
| time_original_estimate | int64 | Updated original estimate |
| changed_fields | array<string> | Fields changed by the update operation |
| updated_at | timestamp | Last update time |
| updated_by | int64 | User who performed the update |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing the update |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission and issue-security evaluation |

## Implementation Traceability (Current Code)

### Application/Domain Mapping

| Use Case Flow | Current Implementation |
|---------------|------------------------|
| Validate command payload | `UpdateWorkItemValidator.validate(...)` |
| Validate project exists and writable | `UpdateWorkItemCommandHandler.handle(...)` + `ensureProjectWritable(...)` |
| Load target work item in tenant scope | `IWorkItemService.getWorkItemById(...)` |
| Resolve EDIT screen + field rules | `UpdateWorkItemFieldRulesResolver.resolveEditFieldRules(...)` |
| Validate writable requested fields | `UpdateWorkItemFieldWriteValidator.validateClientSuppliedWritableFields(...)` |
| Enforce project permissions | `IWorkItemAuthorizationSupportService.checkRequiredPermissions(...)` |
| Enforce issue-security membership | `IIssueSecurityService.checkSecurityAccessIfNeeded(...)` |
| Resolve assignee existence/assignability | `RoleActorSubjectValidator.validateSubjectExistsForAdd(...)` + `IWorkItemAuthorizationSupportService.resolveAssigneeId(...)` |
| Resolve priority/security-level compatibility | `UpdateWorkItemConfigurationResolver` |
| Replace requested custom field values | `UpdateWorkItemCommandHandler.persistCustomFieldValues(...)` |
| Persist outbox event | `UpdateWorkItemCommandHandler.persistUpdatedOutboxEvent(...)` |

### Current Gap Notes (Spec vs Runtime)

1. Runtime currently reuses `PROJECT_PERMISSION_DENIED` for the non-assignable-assignee case; it does not yet expose a dedicated `ASSIGNEE_NOT_ASSIGNABLE` error code.

### Unit Test Coverage Added/Updated

1. `UpdateWorkItemCommandHandlerTest.handleShouldUpdateSummaryAndPublishOutboxEvent`
2. `UpdateWorkItemCommandHandlerTest.handleShouldResolveAssigneeAndUpdateWorkItem`
3. `UpdateWorkItemCommandHandlerTest.handleShouldPersistCustomFieldUpdates`
4. `UpdateWorkItemCommandHandlerTest.handleShouldRejectFieldNotWritableOnEditScreen`
