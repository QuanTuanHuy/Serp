# UC-PM-101 - Create Work Item

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.1
> Last Updated: 2026-03-24

## Related References

- Main spec: `PM_USECASE_SPEC.md`
- Issues schema: `schema/02_issues.md`
- Workflow schema: `schema/03_workflows.md`
- Fields and screens schema: `schema/04_fields_screens.md`
- Permissions and security schema: `schema/05_permissions_security.md`

## Use Case Specification

### Basic Information

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-PM-101 |
| **Use Case Name** | Create Work Item |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-03-24 |
| **Priority** | High |
| **Complexity** | Complex |

### Description

Create a new work item (issue) within a project using Jira-aligned project permissions. The caller must satisfy the project's permission scheme grants for browse and create, and any optional fields that imply stronger authority (assignee, due date, issue security) are validated against their corresponding Jira permissions. The system resolves the effective workflow, field configuration, and CREATE screen from the project's scheme bindings, applies defaults, validates writable fields and custom field contexts, allocates the next project issue number, persists the work item, and writes a `WORK_ITEM_CREATED` outbox event.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Authenticated Project User | Primary | Any authenticated user who satisfies the project's permission grants for issue creation |
| System | System | Resolves permission grants, project configuration, workflow initial step/status, defaults, issue numbering, rank, and outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `CREATE_ISSUES`
- Conditional permissions:
  - `ASSIGN_ISSUES` when `assignee_id` is supplied
  - `ASSIGNABLE_USER` must match the target assignee when `assignee_id` is supplied
  - `SCHEDULE_ISSUES` when `due_date` is supplied
  - `SET_ISSUE_SECURITY` when `security_level_id` is supplied explicitly
- Permission resolution is grant-only and may resolve through `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, or `ASSIGNEE` grants defined in the project's permission scheme

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Project exists, is not archived, and is not deleted
4. Project permission evaluation grants `BROWSE_PROJECTS` and `CREATE_ISSUES` to the caller
5. The requested `issue_type_id` belongs to the project's issue type scheme
6. The project's bound workflow, field configuration, screen, priority, and issue-security schemes are internally consistent enough to resolve create-time behavior

### Postconditions

#### Success Postconditions

1. Work item is persisted with auto-generated immutable `key` (`{PROJECT_KEY}-{issue_no}`), initial `workflow_step_id`, and matching denormalized `status_id`
2. Reporter is set to the authenticated user; system-managed fields (`status_id`, `workflow_step_id`, `resolution_id`, audit fields, rank, key) are not accepted from the client
3. Custom field values are persisted in `work_item_custom_field_values` with resolved `custom_field_context_id` and deterministic `sort_order`
4. Initial worklog totals are set (`time_spent=0`), `resolution_id=NULL`, and `time_remaining_estimate` is initialized from `time_original_estimate` when provided
5. A `WORK_ITEM_CREATED` outbox record is persisted in the same transaction for downstream Kafka publication to `serp.pm.workitem.events`
6. A deterministic backlog rank is assigned for the new work item within the project

#### Failure Postconditions

1. No work item, custom field values, or outbox record are committed
2. Error response is returned with validation, authorization, or configuration details

### Main Flow

| Step | Actor/System | Action |
|------|-------------|--------|
| 1 | Authenticated Project User | Sends POST `/api/v1/projects/{projectId}/work-items` with work item data |
| 2 | System | Validates JWT and extracts `userId`, `tenantId` |
| 3 | System | Loads project by `projectId` with `tenant_id=tenantId` and validates it is not deleted or archived |
| 4 | System | Resolves the project's bound permission scheme and evaluates `BROWSE_PROJECTS` for the caller |
| 5 | System | Evaluates `CREATE_ISSUES` for the caller; if a matching grant uses `PROJECT_ROLE`, resolves role membership through `project_role_actors` |
| 6 | System | Validates `issue_type_id` is in the project's issue type scheme |
| 7 | System | Resolves the effective workflow from the project's workflow scheme using the issue-type-specific mapping or scheme default |
| 8 | System | Loads the workflow's `current_published_version_id` and validates the published version contains exactly one initial step (`is_initial=true`) |
| 9 | System | Resolves the effective field configuration from the project's field config scheme using the issue-type-specific mapping or scheme default |
| 10 | System | Resolves the effective screen scheme from the project's issue type screen scheme using the issue-type-specific mapping or scheme default, then resolves the CREATE screen from `screen_scheme_items(operation_key='CREATE')` or `screen_schemes.default_screen_id` |
| 11 | System | Builds the writable field set from supported system fields plus fields present on the resolved CREATE screen, and rejects any hidden, non-screen, or system-managed fields sent by the client |
| 12 | System | Determines the effective priority: if `priority_id` is provided, validates it belongs to the project's priority scheme; otherwise applies the scheme `default_priority_id` |
| 13 | System | If `due_date` is provided, validates the caller has `SCHEDULE_ISSUES` |
| 14 | System | If `assignee_id` is provided, validates the caller has `ASSIGN_ISSUES`, validates the assignee exists and is active in tenant scope, and validates the assignee matches `ASSIGNABLE_USER` for the project |
| 15 | System | If `security_level_id` is provided, validates the caller has `SET_ISSUE_SECURITY` and validates the level belongs to the project's issue security scheme; otherwise, if the scheme defines `default_level_id`, applies that default |
| 16 | System | If `parent_id` is provided, loads the parent work item in the same project, validates the caller can view the parent, and validates the parent-child hierarchy rules |
| 17 | System | Resolves custom field contexts for `(projectId, issue_type_id)` using context specificity rules, applies field defaults and custom field context defaults, and builds the effective create payload |
| 18 | System | Validates required fields after defaults are applied; validates typed custom field values, options, user/group references, and estimate values |
| 19 | System | Begins database transaction |
| 20 | System | Allocates the next `issue_no` atomically under a project-scoped lock and computes immutable `key = PROJECT_KEY + "-" + issue_no` |
| 21 | System | Generates a Lexorank value that appends the work item to the end of the project's backlog |
| 22 | System | Persists the Work Item entity with initial `workflow_step_id`, matching `status_id`, `reporter_id=userId`, `resolution_id=NULL`, and computed rank |
| 23 | System | Persists resolved custom field values to `work_item_custom_field_values` |
| 24 | System | Persists `WORK_ITEM_CREATED` to the domain outbox with the standard event envelope |
| 25 | System | Commits transaction |
| 26 | System | Returns HTTP 201 with the created work item |

### Alternative Flows

#### AF-1: Assignee Omitted

**Branches from**: Main Flow Step 14  
**Condition**: Request omits `assignee_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 14.1 | System | Leaves `assignee_id=NULL` on create |
| 14.2 | System | Does not apply project/component default assignee logic in UC-PM-101 v1 because component assignment is managed by separate use cases |

**Rejoins**: Main Flow Step 15

#### AF-2: Default Security Level Applied

**Branches from**: Main Flow Step 15  
**Condition**: Request omits `security_level_id` and the project's issue security scheme defines `default_level_id`

| Step | Actor/System | Action |
|------|-------------|--------|
| 15.1 | System | Applies `default_level_id` from the project's issue security scheme |
| 15.2 | System | Continues without requiring `SET_ISSUE_SECURITY` because the caller did not explicitly override the security level |

**Rejoins**: Main Flow Step 16

#### AF-3: Subtask Creation

**Branches from**: Main Flow Step 16  
**Condition**: `parent_id` is provided and issue type has `hierarchy_level=0` (subtask)

| Step | Actor/System | Action |
|------|-------------|--------|
| 16.1 | System | Validates parent exists in the same project and is visible to the caller |
| 16.2 | System | Validates parent issue type has `hierarchy_level=1` |
| 16.3 | System | Sets `parent_id` on the new work item |

**Rejoins**: Main Flow Step 17

#### AF-4: Standard Item Under Epic or Higher Parent

**Branches from**: Main Flow Step 16  
**Condition**: `parent_id` is provided and issue type has `hierarchy_level=1` (standard issue)

| Step | Actor/System | Action |
|------|-------------|--------|
| 16.1 | System | Validates parent exists in the same project and is visible to the caller |
| 16.2 | System | Validates parent issue type has `hierarchy_level >= 2` |
| 16.3 | System | Sets `parent_id` on the new work item |

**Rejoins**: Main Flow Step 17

#### AF-5: Custom Field Defaults Applied

**Branches from**: Main Flow Step 17  
**Condition**: A writable field is omitted from the request but has a resolved default value

| Step | Actor/System | Action |
|------|-------------|--------|
| 17.1 | System | Applies the resolved default value from system defaults, scheme defaults, or custom field context defaults |
| 17.2 | System | Uses the defaulted value during required-field validation and persistence |

**Rejoins**: Main Flow Step 18

### Exception Flows

#### EF-1: Browse/Create Permission Denied

**Triggered at**: Main Flow Step 4-5

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail (`BROWSE_PROJECTS` or `CREATE_ISSUES`) |

#### EF-2: Project Not Found or Archived

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | If project does not exist or is deleted, returns HTTP 404 with error: `PROJECT_NOT_FOUND` |
| 3.E2 | System | If project is archived, returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Invalid Issue Type for Project

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 400 with error: `ISSUE_TYPE_NOT_IN_SCHEME` |

#### EF-4: Workflow Configuration Not Resolvable

**Triggered at**: Main Flow Step 7-8

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | If no workflow mapping or default workflow can be resolved, returns HTTP 422 with error: `WORKFLOW_NOT_RESOLVABLE` |
| 8.E1 | System | If no published workflow version exists or the version does not contain exactly one initial step, returns HTTP 422 with error: `WORKFLOW_INITIAL_STEP_INVALID` |

#### EF-5: Priority Invalid or Not Configured

**Triggered at**: Main Flow Step 12

| Step | Actor/System | Action |
|------|-------------|--------|
| 12.E1 | System | If provided `priority_id` is not in the project's priority scheme, returns HTTP 400 with error: `PRIORITY_NOT_IN_SCHEME` |
| 12.E2 | System | If `priority_id` is omitted and the scheme has no `default_priority_id`, returns HTTP 422 with error: `DEFAULT_PRIORITY_NOT_CONFIGURED` |

#### EF-6: Conditional Permission Denied for Optional Fields

**Triggered at**: Main Flow Step 13-15

| Step | Actor/System | Action |
|------|-------------|--------|
| 13.E1 | System | If `due_date` is supplied without `SCHEDULE_ISSUES`, returns HTTP 403 with error: `SCHEDULE_ISSUES_REQUIRED` |
| 14.E1 | System | If `assignee_id` is supplied without `ASSIGN_ISSUES`, returns HTTP 403 with error: `ASSIGN_ISSUES_REQUIRED` |
| 15.E1 | System | If `security_level_id` is supplied without `SET_ISSUE_SECURITY`, returns HTTP 403 with error: `SET_ISSUE_SECURITY_REQUIRED` |

#### EF-7: Invalid Assignee

**Triggered at**: Main Flow Step 14

| Step | Actor/System | Action |
|------|-------------|--------|
| 14.E2 | System | If assignee does not exist or is inactive in tenant scope, returns HTTP 404 with error: `ASSIGNEE_NOT_FOUND` |
| 14.E3 | System | If assignee does not satisfy `ASSIGNABLE_USER` in the project, returns HTTP 422 with error: `ASSIGNEE_NOT_ASSIGNABLE` |

#### EF-8: Invalid Issue Security Level

**Triggered at**: Main Flow Step 15

| Step | Actor/System | Action |
|------|-------------|--------|
| 15.E2 | System | If `security_level_id` is not in the project's issue security scheme, returns HTTP 400 with error: `SECURITY_LEVEL_NOT_IN_SCHEME` |

#### EF-9: Invalid Parent or Hierarchy

**Triggered at**: Main Flow Step 16

| Step | Actor/System | Action |
|------|-------------|--------|
| 16.E1 | System | If parent does not exist in the same project or is deleted, returns HTTP 404 with error: `PARENT_WORK_ITEM_NOT_FOUND` |
| 16.E2 | System | If caller cannot view the parent due to browse/security restrictions, returns HTTP 403 with error: `PARENT_WORK_ITEM_NOT_VISIBLE` |
| 16.E3 | System | If parent-child hierarchy is invalid, returns HTTP 400 with error: `INVALID_PARENT_HIERARCHY` with details |

#### EF-10: Field Not Writable on CREATE

**Triggered at**: Main Flow Step 11

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.E1 | System | If the request contains a field that is hidden, absent from the CREATE screen, or system-managed, returns HTTP 400 with error: `FIELD_NOT_WRITABLE_ON_CREATE` |

#### EF-11: Required Field Missing or Invalid Custom Field Value

**Triggered at**: Main Flow Step 17-18

| Step | Actor/System | Action |
|------|-------------|--------|
| 17.E1 | System | If a custom field resolves to zero or multiple contexts for `(projectId, issue_type_id)`, returns HTTP 422 with error: `CUSTOM_FIELD_CONTEXT_UNRESOLVABLE` |
| 18.E1 | System | If required fields are still missing after defaults are applied, returns HTTP 400 with error: `REQUIRED_FIELDS_MISSING` and the missing field list |
| 18.E2 | System | If a custom field value fails type, option, user/group, or multi-value validation, returns HTTP 400 with error: `CUSTOM_FIELD_VALUE_INVALID` and field-level details |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-101-01 | Project-scoped authorization for work item creation follows Jira project permission keys, not legacy `PM.*` placeholders | Authorization layer |
| BR-PM-101-02 | Creating a work item requires both `BROWSE_PROJECTS` and `CREATE_ISSUES` on the target project | Authorization layer |
| BR-PM-101-03 | `PROJECT_ROLE` grants are resolved via `project_role_actors` within the target project | Authorization layer |
| BR-PM-101-04 | Supplying `assignee_id` requires `ASSIGN_ISSUES`; the target assignee must satisfy `ASSIGNABLE_USER` in the same project | Authorization layer |
| BR-PM-101-05 | Supplying `due_date` requires `SCHEDULE_ISSUES` | Authorization layer |
| BR-PM-101-06 | Supplying `security_level_id` explicitly requires `SET_ISSUE_SECURITY`; omitting it may still result in the project's default security level being applied | Authorization + Service layer |
| BR-PM-101-07 | Work item read access after creation is governed by `BROWSE_PROJECTS` plus issue-security membership; creator/reporter are not auto-added to the selected security level | Authorization layer |
| BR-PM-101-08 | Issue type must belong to the project's issue type scheme | UseCase layer |
| BR-PM-101-09 | Workflow resolution uses issue-type-specific mapping first, then the workflow scheme default; the effective workflow must have a published version with exactly one initial step | UseCase layer |
| BR-PM-101-10 | `workflow_step_id` and denormalized `status_id` must be derived from the same initial workflow step | Service layer |
| BR-PM-101-11 | Only fields present on the resolved CREATE screen and not hidden by field configuration are writable on create | Service layer |
| BR-PM-101-12 | System-managed fields (`reporter_id`, `status_id`, `workflow_step_id`, `resolution_id`, `rank`, `issue_no`, `key`, audit fields) must not be accepted from the request | DTO validation |
| BR-PM-101-13 | `summary` is always required regardless of field configuration or screen setup | DTO validation |
| BR-PM-101-14 | Defaults are applied before required-field validation: priority scheme default, issue-security default, and custom field context defaults may satisfy missing values | Service layer |
| BR-PM-101-15 | Each custom field must resolve exactly one context for `(projectId, issue_type_id)` using the documented specificity order; zero or multiple matches are configuration errors | Service layer |
| BR-PM-101-16 | Parent work item must belong to the same project, be visible to the caller, and satisfy the allowed hierarchy matrix | Service layer |
| BR-PM-101-17 | Allowed hierarchy matrix for v1 create: child level `0` -> parent level `1`; child level `1` -> parent level `>=2`; child level `>=2` cannot set `parent_id` via UC-PM-101 | Service layer |
| BR-PM-101-18 | `issue_no` is allocated atomically per project, is never reused, and may be non-contiguous after retries or rollbacks | DB + Service layer |
| BR-PM-101-19 | Work item key format is `{PROJECT_KEY}-{issue_no}`, auto-generated, immutable, and not recomputed if the project key changes later | Service layer |
| BR-PM-101-20 | Reporter is always the authenticated user in v1 create; changing reporter requires a separate future use case aligned with Jira `MODIFY_REPORTER` semantics | Service layer |
| BR-PM-101-21 | If `time_original_estimate` is provided, `time_remaining_estimate` is initialized to the same value and `time_spent` starts at `0` | Service layer |
| BR-PM-101-22 | New work items are inserted at the tail of the project backlog by generating a Lexorank value after the current maximum rank in project scope | Service layer |
| BR-PM-101-23 | UC-PM-101 v1 does not create components, fix versions, sprint links, issue links, or worklogs; those relations are managed by separate use cases | Service layer |
| BR-PM-101-24 | Domain events use the outbox pattern: the outbox row is stored in the same transaction as the work item, and Kafka publication happens asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| summary | string | Yes | min:1, max:512 | Work item title |
| description | string | No | max:50000 | Markdown/JSON description |
| issue_type_id | int64 | Yes | must be in project's scheme | Issue type |
| priority_id | int64 | No | must be in project's priority scheme; defaults to scheme default when omitted | Priority |
| assignee_id | int64 | No | requires `ASSIGN_ISSUES`; assignee must exist and satisfy `ASSIGNABLE_USER` | Assignee user |
| parent_id | int64 | No | must exist in same project, be visible to caller, and satisfy allowed hierarchy | Parent work item |
| due_date | timestamp | No | valid date; requires `SCHEDULE_ISSUES` | Due date |
| time_original_estimate | int64 | No | min:0 | Original estimate in seconds |
| security_level_id | int64 | No | must be in project's security scheme; requires `SET_ISSUE_SECURITY` when supplied explicitly | Security level |
| custom_fields | map<string, unknown> | No | keys must be writable custom field keys; values validated per field config, screen, context, and field type; multi-value fields use arrays | Custom field values |

**Unsupported client-supplied fields in UC-PM-101 v1**: `reporter_id`, `status_id`, `workflow_step_id`, `resolution_id`, `rank`, `issue_no`, `key`, `components`, `fix_versions`, `sprint_id`, `links`, and audit fields.

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| id | int64 | Generated work item ID |
| issue_no | int64 | Project-scoped sequential issue number |
| key | string | Human key (e.g., SERP-123) |
| summary | string | Title |
| description | string | Description |
| issue_type | object | Issue type details |
| workflow_step_id | int64 | Initial workflow step ID |
| status | object | Initial status |
| priority | object | Priority details |
| assignee_id | int64 | Assignee (nullable) |
| reporter_id | int64 | Reporter (creator) |
| parent_id | int64 | Parent (nullable) |
| security_level_id | int64 | Applied security level (nullable) |
| due_date | timestamp | Due date (nullable) |
| rank | string | Lexorank value |
| time_original_estimate | int64 | Original estimate in seconds (nullable) |
| time_remaining_estimate | int64 | Remaining estimate in seconds (nullable) |
| time_spent | int64 | Logged time in seconds |
| created_at | timestamp | Creation time |
| created_by | int64 | Creator user ID |

## Detailed Implementation Plan For Next Session

### Objective

Implement UC-PM-101 end-to-end in the Java codebase with Jira-aligned authorization, scheme resolution, atomic persistence, and outbox publication.

### Current Codebase Snapshot

- Existing work item base objects are already present in `src/main/java/serp/project/pmcore/domain/dto/request/CreateWorkItemRequest.java`, `src/main/java/serp/project/pmcore/domain/entity/workitem/WorkItemEntity.java`, `src/main/java/serp/project/pmcore/infrastructure/store/model/WorkItemModel.java`, `src/main/java/serp/project/pmcore/domain/service/IWorkItemService.java`, and `src/main/java/serp/project/pmcore/domain/service/impl/WorkItemService.java`.
- Issue numbering and persistence building blocks already exist through `IProjectIssueCounterPort`, `IWorkItemPort`, `WorkItemAdapter`, `WorkItemRepository`, and `kernel/utils/LexorankUtils.java`.
- Workflow, field, screen, priority, and issue-security scheme ports already exist and can be reused for create-time resolution.
- Outbox infrastructure already exists through `IOutboxEventPort`, `IOutboxEventService`, `OutboxEventEntity`, and `EventConstants`.

### Gaps Identified Before Coding

1. No application command or REST controller currently exposes create-work-item.
2. `CreateWorkItemRequest` is not aligned with the spec:
   - still contains `projectId` in body while the spec uses path param `/projects/{projectId}/work-items`
   - still accepts `statusId`, which should be system-derived
   - does not include `securityLevelId` or `customFields`
3. `WorkItemEntity` and `WorkItemModel` are not aligned with the spec:
   - missing `workflowStepId`
   - missing `securityLevelId`
   - `dueDate` type is inconsistent between entity (`Long`) and model (`LocalDateTime`)
4. No permission evaluation service exists yet for Jira-style project permissions.
5. No Java implementation was found yet for `project_roles` / `project_role_actors`, even though the design depends on them for `PROJECT_ROLE` grants.
6. No custom field value persistence layer was found yet for `work_item_custom_field_values`.
7. Parent hierarchy validation in `WorkItemService.validateParentHierarchy(...)` is still too weak for the final rule set; it checks only relative hierarchy order, not full create-time constraints.

### Recommended Implementation Order

#### Phase 1 - Freeze the v1 API contract

1. Add `CreateWorkItemCommand` under `application/command/workitem/`.
2. Add `WorkItemController` under `ui/rest/controller/` with endpoint `POST /api/v1/projects/{projectId}/work-items`.
3. Refactor `CreateWorkItemRequest` so request body matches the spec:
   - keep `summary`, `description`, `issueTypeId`, `priorityId`, `assigneeId`, `parentId`, `dueDate`, `timeOriginalEstimate`, `securityLevelId`
   - add `Map<String, Object> customFields`
   - remove `projectId` from the request body and use path param instead
   - remove `statusId` from client input
4. Create or update `WorkItemResponse` so it can return `issueNo`, `key`, `workflowStepId`, `securityLevelId`, `rank`, and time fields.

#### Phase 2 - Align persistence model and migrations

1. Add missing columns to `work_items` via migration if they do not already exist in the database:
   - `workflow_step_id`
   - `security_level_id`
2. Normalize date/time representation for `due_date` across request, entity, mapper, and model.
3. Update `WorkItemEntity`, `WorkItemModel`, and `WorkItemMapper` to include the missing fields.
4. Add unique/index validation if needed for `(tenant_id, project_id, issue_no)` and `(tenant_id, key)`.

#### Phase 3 - Build create-time configuration resolution services

1. Create a dedicated orchestration service, for example `CreateWorkItemOrchestrator` or `WorkItemCreationService`, instead of overloading `WorkItemService` with all cross-cutting logic.
2. Implement project and scheme resolution using existing ports:
   - project lookup from `IProjectPort`
   - issue type scheme validation from `IIssueTypeSchemeItemPort`
   - workflow mapping from `IWorkflowSchemeItemPort`
   - workflow version / initial step from `IWorkflowPort`, `IWorkflowVersionPort`, `IWorkflowStepPort`
   - field configuration items from `IFieldConfigSchemeItemPort`, `IFieldConfigItemPort`
   - screen resolution from `IIssueTypeScreenSchemeItemPort`, `IScreenSchemeItemPort`
   - priority scheme default from `IPrioritySchemePort` and `IPrioritySchemeItemPort` if needed
   - issue security default / membership from `IIssueSecuritySchemePort`, `IIssueSecurityLevelPort`, `IIssueSecurityLevelMemberPort`
3. Keep each resolver small and testable:
   - `WorkItemCreatePermissionResolver`
   - `WorkItemWorkflowResolver`
   - `WorkItemScreenResolver`
   - `WorkItemFieldResolver`
   - `WorkItemPriorityResolver`
   - `WorkItemSecurityResolver`

#### Phase 4 - Implement Jira-style authorization

1. Add permission evaluation service for project-scoped grants:
   - load `permission_scheme_entries`
   - support `PROJECT_LEAD`, `USER`, `GROUP`, `PROJECT_ROLE`, `REPORTER`, `ASSIGNEE`
2. For v1 create, implement at least these checks:
   - `BROWSE_PROJECTS`
   - `CREATE_ISSUES`
   - `ASSIGN_ISSUES`
   - `ASSIGNABLE_USER`
   - `SCHEDULE_ISSUES`
   - `SET_ISSUE_SECURITY`
3. If `PROJECT_ROLE` support is still missing in Java, either:
   - implement `project_roles` and `project_role_actors` properly before create-work-item, or
   - explicitly limit v1 runtime authorization to `PROJECT_LEAD`, `USER`, `GROUP` until project-role persistence is added.
4. Document whichever fallback is chosen, because it changes parity with the extracted spec.

#### Phase 5 - Implement business validation pipeline

1. Validate project state: exists, tenant-owned, not archived, not deleted.
2. Validate issue type membership in the project issue type scheme.
3. Resolve workflow and require exactly one initial step.
4. Resolve writable fields from CREATE screen + field config.
5. Reject unsupported or non-writable client fields.
6. Validate priority or apply default priority.
7. Validate assignee existence and assignability.
8. Validate explicit security level or apply default security level.
9. Validate parent visibility and exact hierarchy matrix:
   - child level `0` -> parent level `1`
   - child level `1` -> parent level `>= 2`
   - child level `>= 2` cannot set `parent_id`
10. Apply defaults before required-field validation.
11. Add placeholder strategy for `customFields` if custom field persistence is not yet ready; do not silently ignore them.

#### Phase 6 - Implement atomic persistence

1. Start one transaction in the command/orchestrator.
2. Allocate `issue_no` using `IProjectIssueCounterPort.getNextIssueNo(projectId, tenantId)`.
3. Build immutable key `{PROJECT_KEY}-{issue_no}`.
4. Generate tail rank using `LexorankUtils`.
5. Persist `WorkItemEntity` with:
   - `reporterId = userId`
   - derived `statusId`
   - derived `workflowStepId`
   - `resolutionId = null`
   - `timeSpent = 0`
   - `timeRemainingEstimate = timeOriginalEstimate` when provided
6. Persist custom field values in the same transaction once that store exists.
7. Persist outbox event in the same transaction.

#### Phase 7 - Publish domain event payload

1. Reuse outbox infrastructure rather than direct Kafka send.
2. Create a mapper from saved work item to `WorkItemEventPayload`.
3. Publish event type `WORK_ITEM_CREATED` using `EventConstants`.
4. Include enough metadata to support downstream notifications and discuss feed.

#### Phase 8 - Testing strategy

1. Unit tests for:
   - permission evaluation
   - workflow initial-step resolution
   - parent hierarchy validation
   - priority/security default application
2. Integration tests for command/controller:
   - happy path create
   - missing browse/create permission
   - invalid issue type for project
   - archived project
   - invalid assignee
   - invalid parent hierarchy
   - workflow misconfiguration
3. Persistence tests for:
   - issue counter increments
   - key generation
   - outbox row written on success only

### Concrete File-Level Task List

#### A. Update existing files

- `src/main/java/serp/project/pmcore/domain/dto/request/CreateWorkItemRequest.java`
- `src/main/java/serp/project/pmcore/domain/dto/response/workitem/WorkItemResponse.java`
- `src/main/java/serp/project/pmcore/domain/entity/workitem/WorkItemEntity.java`
- `src/main/java/serp/project/pmcore/infrastructure/store/model/WorkItemModel.java`
- `src/main/java/serp/project/pmcore/infrastructure/store/mapper/WorkItemMapper.java`
- `src/main/java/serp/project/pmcore/domain/service/impl/WorkItemService.java`

#### B. New application files likely needed

- `src/main/java/serp/project/pmcore/application/command/workitem/CreateWorkItemCommand.java`
- `src/main/java/serp/project/pmcore/application/command/workitem/validator/CreateWorkItemValidator.java`
- `src/main/java/serp/project/pmcore/ui/rest/controller/WorkItemController.java`

#### C. New domain support files likely needed

- `src/main/java/serp/project/pmcore/domain/service/workitem/WorkItemCreateAuthorizationService.java`
- `src/main/java/serp/project/pmcore/domain/service/workitem/WorkItemCreateConfigurationResolver.java`
- `src/main/java/serp/project/pmcore/domain/service/workitem/WorkItemCreateOutboxFactory.java`

#### D. Likely missing infrastructure for full parity

- persistence/model/adapter/repository for `project_roles`
- persistence/model/adapter/repository for `project_role_actors`
- persistence/model/adapter/repository for `work_item_custom_field_values`

### Suggested Delivery Slices

#### Slice 1 - Create minimal happy path without custom fields

- controller + command + request/response alignment
- project lookup
- issue type membership validation
- workflow initial-step resolution
- issue counter + key + rank + save + outbox

#### Slice 2 - Add Jira conditional permissions

- browse/create checks
- assignee permission checks
- due-date permission check
- issue-security permission check

#### Slice 3 - Add parent hierarchy + defaults

- parent validation matrix
- priority default
- security default
- time estimate handling

#### Slice 4 - Add custom field resolution

- create-screen writable field filtering
- field config required/hidden validation
- custom field context resolution and persistence

### Acceptance Checklist For Next Session

- API path matches the spec: `POST /api/v1/projects/{projectId}/work-items`
- Request contract no longer accepts client-supplied status/key/issue number
- `workflowStepId` and `statusId` are both derived from the initial workflow step
- `issueNo` and `key` are generated atomically
- create fails for archived projects
- create fails when browse/create permission is missing
- create enforces conditional permissions for assignee, due date, and security level
- parent hierarchy matrix matches the extracted use case
- outbox event is written in the same transaction as the work item
- tests cover both happy path and the major exception flows listed above

### Recommended First Coding Target

For the next implementation session, start with **Slice 1** and **Slice 2** together. That gives a usable vertical path early, while keeping custom-field complexity and project-role parity as a second wave.
