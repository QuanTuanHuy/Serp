# UC-PM-106 - Transition Work Item Status

> Extracted from `PM_USECASE_SPEC.md`
> Version: 1.1
> Last Updated: 2026-03-30

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
| **Use Case ID** | UC-PM-106 |
| **Use Case Name** | Transition Work Item Status |
| **Module** | PM Core |
| **Version** | 1.1 |
| **Last Updated** | 2026-03-30 |
| **Priority** | High |
| **Complexity** | Complex |

### Description

Execute a workflow transition for a work item using Jira-aligned authorization and workflow semantics. The system resolves the work item's effective workflow version from project scheme bindings, validates that the requested transition is available from the current workflow step (or is global), evaluates transition conditions and validators in order, applies optional transition-screen fields, updates both `workflow_step_id` and denormalized `status_id` atomically, and writes a `WORK_ITEM_STATUS_CHANGED` outbox event.

### Actors

| Actor | Type | Description |
|-------|------|-------------|
| Team Member | Primary | Initiates a workflow transition on a visible work item |
| System | System | Resolves permissions, evaluates workflow transition rules, performs state mutation, and records outbox event |

### Authorization (Jira Project Permissions)

- Baseline permissions: `BROWSE_PROJECTS` and `TRANSITION_ISSUES`
- Conditional read/write constraint: if `security_level_id` is set on the work item, caller must satisfy issue-security membership
- Permission resolution is grant-only through the project's `permission_scheme_id` and may resolve via `PROJECT_ROLE`, `GROUP`, `USER`, `PROJECT_LEAD`, `REPORTER`, and `ASSIGNEE`

### Preconditions

1. User is authenticated with valid JWT token
2. User belongs to an active tenant
3. Work item exists in tenant scope and is not soft-deleted
4. Project exists and is not archived
5. Caller is granted `BROWSE_PROJECTS` and `TRANSITION_ISSUES` for the target project
6. If `security_level_id` is set, caller is a member of that issue security level
7. Effective workflow version for the work item is resolvable and published

### Postconditions

#### Success Postconditions

1. Work item `workflow_step_id` is updated to transition target step
2. Work item `status_id` is updated to the status mapped by target workflow step
3. If transition rules or target status category require resolution, `resolution_id` is validated/applied accordingly
4. Transition-screen and transition payload field updates (if allowed) are persisted in same transaction
5. A `WORK_ITEM_STATUS_CHANGED` outbox record is persisted in the same transaction for Kafka publication to `serp.pm.workitem.events`

#### Failure Postconditions

1. Work item state remains unchanged
2. No partial field updates are committed
3. No outbox event is committed
4. Error response is returned with transition, validation, or authorization details

### Main Flow

| Step | Actor/System | Action                                                                                                                                                                   |
|------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Team Member | Sends POST `/api/v1/projects/{projectId}/work-items/{workItemId}/transitions` with `{ transition_id, resolution_id?, fields? }`                                          |
| 2 | System | Validates JWT and extracts `userId`, `tenantId`, and security context                                                                                                    |
| 3 | System | Loads work item by `id=workItemId`, `tenant_id=tenantId`, `deleted_at IS NULL`                                                                                           |
| 4 | System | Loads project context and validates project is not archived                                                                                                              |
| 5 | System | Evaluates `BROWSE_PROJECTS` and `TRANSITION_ISSUES` permissions                                                                                                          |
| 6 | System | If `security_level_id` is set, evaluates issue-security membership                                                                                                       |
| 7 | System | Resolves effective workflow from project workflow scheme using work item `issue_type_id`; loads `current_published_version_id`                                           |
| 8 | System | Validates current `workflow_step_id` belongs to effective workflow version and maps to current denormalized `status_id`                                                  |
| 9 | System | Loads transition by `transition_id` in workflow version and validates availability from current step (`from_step_id = current_step_id` or global `from_step_id IS NULL`) |
| 10 | System | Evaluates transition CONDITION rules in sequence                                                                                                                         |
| 11 | System | Evaluates transition VALIDATOR rules in sequence                                                                                                                         |
| 12 | System | If transition has `screen_id`, resolves transition screen and validates writable fields in `fields` payload                                                              |
| 13 | System | Begins transaction                                                                                                                                                       |
| 14 | System | Updates `workflow_step_id` to transition `to_step_id` and updates denormalized `status_id` from target step                                                              |
| 15 | System | Applies resolution behavior (`resolution_id` from request if provided/required; otherwise retain or clear based on rule config)                                          |
| 16 | System | Executes POST_FUNCTION rules in sequence (e.g., field update, event marker)                                                                                              |
| 17 | System | Persists transition audit record and change history entry                                                                                                                |
| 18 | System | Persists `WORK_ITEM_STATUS_CHANGED` to domain outbox with transition metadata                                                                                            |
| 19 | System | Commits transaction                                                                                                                                                      |
| 20 | System | Returns HTTP 200 with updated work item and executed transition details                                                                                                  |

### Alternative Flows

#### AF-1: Global Transition

**Branches from**: Main Flow Step 9  
**Condition**: Transition has `from_step_id IS NULL`

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.1 | System | Treats transition as available regardless of current step |

**Rejoins**: Main Flow Step 10

#### AF-2: Transition Without Screen

**Branches from**: Main Flow Step 12  
**Condition**: Transition `screen_id` is null

| Step | Actor/System | Action |
|------|-------------|--------|
| 12.1 | System | Skips transition-screen field validation |

**Rejoins**: Main Flow Step 13

#### AF-3: Resolution Required by Validator

**Branches from**: Main Flow Step 11  
**Condition**: Transition validator requires resolution for target status/category

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.1 | System | Validates `resolution_id` is provided and valid in tenant scope |
| 11.2 | System | Proceeds when resolution requirement is satisfied |

**Rejoins**: Main Flow Step 12

### Exception Flows

#### EF-1: Work Item Not Found

**Triggered at**: Main Flow Step 3

| Step | Actor/System | Action |
|------|-------------|--------|
| 3.E1 | System | Returns HTTP 404 with error: `WORK_ITEM_NOT_FOUND` |

#### EF-2: Project Archived

**Triggered at**: Main Flow Step 4

| Step | Actor/System | Action |
|------|-------------|--------|
| 4.E1 | System | Returns HTTP 409 with error: `PROJECT_ARCHIVED` |

#### EF-3: Project Permission Denied

**Triggered at**: Main Flow Step 5

| Step | Actor/System | Action |
|------|-------------|--------|
| 5.E1 | System | Returns HTTP 403 with error: `PROJECT_PERMISSION_DENIED` and missing permission detail (`BROWSE_PROJECTS` or `TRANSITION_ISSUES`) |

#### EF-4: Issue Security Access Denied

**Triggered at**: Main Flow Step 6

| Step | Actor/System | Action |
|------|-------------|--------|
| 6.E1 | System | Returns HTTP 403 with error: `WORK_ITEM_SECURITY_ACCESS_DENIED` |

#### EF-5: Workflow Not Resolvable

**Triggered at**: Main Flow Step 7-8

| Step | Actor/System | Action |
|------|-------------|--------|
| 7.E1 | System | Returns HTTP 422 with error: `WORKFLOW_NOT_RESOLVABLE` when workflow mapping or published version cannot be resolved |
| 8.E1 | System | Returns HTTP 422 with error: `WORK_ITEM_WORKFLOW_STATE_INVALID` when current step/status is inconsistent with effective workflow |

#### EF-6: Invalid Transition

**Triggered at**: Main Flow Step 9

| Step | Actor/System | Action |
|------|-------------|--------|
| 9.E1 | System | Returns HTTP 400 with error: `INVALID_TRANSITION` and list of available transitions from current step |

#### EF-7: Transition Condition Failed

**Triggered at**: Main Flow Step 10

| Step | Actor/System | Action |
|------|-------------|--------|
| 10.E1 | System | Returns HTTP 403 with error: `TRANSITION_CONDITION_FAILED` and condition details |

#### EF-8: Transition Validator Failed

**Triggered at**: Main Flow Step 11

| Step | Actor/System | Action |
|------|-------------|--------|
| 11.E1 | System | Returns HTTP 400 with error: `TRANSITION_VALIDATION_FAILED` and validator details |

#### EF-9: Transition Screen Field Invalid

**Triggered at**: Main Flow Step 12

| Step | Actor/System | Action |
|------|-------------|--------|
| 12.E1 | System | Returns HTTP 400 with error: `TRANSITION_FIELD_INVALID` for hidden/non-screen/non-writable fields or invalid field values |

### Business Rules

| Rule ID | Description | Enforcement |
|---------|-------------|-------------|
| BR-PM-106-01 | Transition requires `BROWSE_PROJECTS` and `TRANSITION_ISSUES` in target project | Authorization layer |
| BR-PM-106-02 | If work item has `security_level_id`, caller must satisfy issue-security membership in addition to project permissions | Authorization layer |
| BR-PM-106-03 | Effective workflow resolution uses project workflow scheme mapping by issue type, with scheme default fallback | UseCase layer |
| BR-PM-106-04 | Only transitions from current step (or global transitions with `from_step_id IS NULL`) are executable | UseCase layer |
| BR-PM-106-05 | CONDITION rules execute before VALIDATOR rules; failure stops transition immediately | UseCase layer |
| BR-PM-106-06 | Transition-screen payload may update only fields allowed by transition screen and field configuration | Service layer |
| BR-PM-106-07 | Work item `workflow_step_id` and denormalized `status_id` must always be updated together from the same target step | Service layer |
| BR-PM-106-08 | If transition target is in done category or validator requires it, `resolution_id` is mandatory and must be valid | Service layer |
| BR-PM-106-09 | POST_FUNCTION rules execute in the same transaction as status transition | UseCase layer |
| BR-PM-106-10 | Domain events use outbox pattern: `WORK_ITEM_STATUS_CHANGED` is stored in same transaction and published asynchronously after commit | UseCase layer |

### Data Requirements

#### Input Data

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| workItemId | int64 | Yes | min:1 | Work item numeric identifier from path |
| transition_id | int64 | Yes | must exist in effective workflow and be valid from current step (or global) | Transition to execute |
| resolution_id | int64 | No | required when transition validator/target status requires resolution | Resolution to set |
| fields | map<string, unknown> | No | validated by transition screen and field configuration | Additional field values collected on transition screen |

#### Output Data

| Field | Type | Description |
|-------|------|-------------|
| work_item | object | Updated work item with new workflow step and status |
| transition | object | Executed transition (`id`, `name`, `from_step_id`, `to_step_id`) |
| changed_fields | array<object> | Field-level changes applied during transition |
| transitioned_at | timestamp | Transition execution time |
| transitioned_by | int64 | User who executed the transition |

#### Context Data (from JWT)

| Field | Source | Description |
|-------|--------|-------------|
| userId | JWT token | Authenticated user performing transition |
| tenantId | JWT token | Tenant scope for data isolation |
| groups | JWT token | Group memberships used by permission and issue-security evaluation |
