# Module 03 Workflow Engine Backlog

> Scope: implementation backlog for workflow engine use cases in PM Core
> Date: 2026-04-19

## Goal

Implement the remaining Module 03 use cases after `UC-PM-201..205` and
`UC-PM-211..215` are already in place.

This backlog follows the actual runtime dependency chain already used by
`UC-PM-001`, `UC-PM-101`, and `UC-PM-106`:

- project binds `workflow_scheme_id`
- scheme resolves workflow by issue type or default fallback
- workflow resolves through `current_published_version_id`
- runtime transition executes on workflow steps, transitions, and rules

## Completed Baseline

- `UC-PM-201..205` Status Category CRUD is implemented
- `UC-PM-211..215` Status CRUD is implemented

The remaining backlog starts from workflow root authoring and moves outward to
publish/runtime-safe scheme binding.

## Canonical Implementation Order

1. Workflow root foundation: `UC-PM-221`, `UC-PM-223`, `UC-PM-224`
2. Workflow step management: `UC-PM-231..233`
3. Workflow transition management: `UC-PM-236..239`
4. Workflow transition rule management: `UC-PM-241..243`
5. Workflow validation and publish: `UC-PM-228`, `UC-PM-226`
6. Workflow update semantics for active workflows: `UC-PM-222`
7. Workflow scheme CRUD: `UC-PM-251..255`
8. Workflow scheme item management: `UC-PM-256`
9. Workflow clone and delete hardening: `UC-PM-227`, `UC-PM-225`

## Backlog Waves

## Wave 1 - Workflow Root Foundation

Use cases:

- `UC-PM-221` Create Workflow
- `UC-PM-223` Get Workflow by ID
- `UC-PM-224` List Workflows

Deliverables:

- canonical workflow application package and controller
- create/get/list APIs for workflow root
- workflow root view including published and draft version references
- list criteria with visible-scope filtering and paging

Detailed tasks:

1. Domain layer
   - extend `IWorkflowService` and `WorkflowService`
   - define create/update data objects if missing
   - normalize workflow name/description/workflow key generation rules
   - decide visible read path: tenant-owned + system-owned
2. Port and infrastructure
   - extend `IWorkflowPort`
   - add list paging/filter methods on repository and adapter
   - add uniqueness helper for `workflow_key`
3. Application layer
   - create `application/workflow/**`
   - add create command/handler
   - add get query/handler
   - add list query/handler
4. UI layer
   - add `PathConstants.WORKFLOWS`
   - add `WorkflowController`
   - add request DTOs for create and list filters
5. Tests
   - `WorkflowServiceTest`
   - `WorkflowHandlersTest`

Acceptance gate:

- workflow root can be created in draft state
- get/list can show tenant-visible workflows
- response exposes `currentPublishedVersionId`, `draftVersionId`, `lifecycleState`, `readOnly`

## Wave 2 - Workflow Step Management

Use cases:

- `UC-PM-231` Add Workflow Step
- `UC-PM-232` Remove Workflow Step
- `UC-PM-233` Reorder Workflow Steps

Deliverables:

- step authoring against draft workflow version only
- exactly-one-initial-step enforcement at authoring time
- cascade cleanup for transitions when a step is removed

Detailed tasks:

1. Domain layer
   - add service methods for add/remove/reorder step
   - ensure edit target is `draft_version_id`, not `current_published_version_id`
   - reject edits when no draft version exists
   - enforce duplicate-status guard per workflow version
2. Port and infrastructure
   - extend `IWorkflowStepPort` for create/delete/reorder helpers
   - add step-order queries
   - add transition cleanup hook for remove-step flow
3. Application/UI
   - add nested step endpoints under `/workflows/{workflowId}/steps`
   - add request DTOs for add and reorder
4. Tests
   - initial step uniqueness
   - duplicate status in same workflow version
   - removing a step removes related transitions

Acceptance gate:

- draft workflow can be shaped into a valid step graph
- published versions remain immutable

## Wave 3 - Workflow Transition Management

Use cases:

- `UC-PM-236` Add Workflow Transition
- `UC-PM-237` Update Workflow Transition
- `UC-PM-238` Remove Workflow Transition
- `UC-PM-239` List Workflow Transitions

Deliverables:

- transition CRUD on draft version only
- support both direct and global transitions
- optional `screen_id` binding for transition screens

Detailed tasks:

1. Domain layer
   - validate `from_step_id` and `to_step_id` belong to the same draft version
   - allow `from_step_id=null` for global transition
   - validate `screen_id` visibility if provided
2. Port and infrastructure
   - extend `IWorkflowTransitionPort` for update/delete/list-by-filter
   - add repository queries by workflow version and `from_step_id`
3. Application/UI
   - add endpoints under `/workflows/{workflowId}/transitions`
   - list transitions with optional `fromStepId`
4. Tests
   - invalid cross-version step reference is rejected
   - global transition is allowed
   - delete transition also removes transition rules

Acceptance gate:

- draft workflow exposes a complete transition graph consumable by validation and publish

## Wave 4 - Workflow Transition Rule Management

Use cases:

- `UC-PM-241` Add Workflow Transition Rule
- `UC-PM-242` Update Workflow Transition Rule
- `UC-PM-243` Remove Workflow Transition Rule

Deliverables:

- authoring APIs for transition rules
- rule-key validation aligned with phase-1 runtime support
- deterministic sequence ordering per transition and stage

Recommended phase-1 supported rule keys:

- CONDITION: `user_is_assignee`, `user_is_reporter`, `user_is_project_lead`
- VALIDATOR: `field_required`, `resolution_required`
- POST_FUNCTION: `clear_resolution`, `set_resolution_from_request`, `fire_event`

Detailed tasks:

1. Domain layer
   - add rule registry or validator helper for supported keys
   - validate `config_json` shape for `field_required`
   - limit authoring to draft workflow version
2. Port and infrastructure
   - extend `IWorkflowTransitionRulePort` for update/delete/list
3. Application/UI
   - add nested rule endpoints under `/workflows/{workflowId}/transitions/{transitionId}/rules`
4. Tests
   - unsupported rule key rejected
   - malformed config rejected
   - disabled rule remains persisted but ignored at runtime

Acceptance gate:

- authoring surface matches what `WorkItemTransitionRuleEvaluator` can safely execute

## Wave 5 - Workflow Validation and Publish

Use cases:

- `UC-PM-228` Validate Workflow
- `UC-PM-226` Publish Workflow

Deliverables:

- explicit validation endpoint with structured errors/warnings
- publish operation that promotes draft to current published version
- immutable published version semantics preserved

Detailed tasks:

1. Domain layer
   - implement validator service for rules `V-001..V-006`
   - validate against draft version graph
   - publish by updating workflow root pointers and version states
2. Port and infrastructure
   - extend `IWorkflowVersionPort` as needed for draft/current lookups
   - add repository methods for version-state transitions
3. Application/UI
   - `POST /workflows/{workflowId}/validate`
   - `POST /workflows/{workflowId}/publish`
4. Tests
   - no initial step rejected
   - multiple initial steps rejected
   - unreachable non-initial step rejected
   - publish updates `current_published_version_id`

Acceptance gate:

- a validated draft can become the only effective runtime version
- `UC-PM-101` and `UC-PM-106` can consume the published graph without behavioral drift

## Wave 6 - Workflow Update Semantics

Use case:

- `UC-PM-222` Update Workflow

Deliverables:

- metadata update for draft workflows
- fork-new-draft flow when editing an already active workflow

Detailed tasks:

1. Domain layer
   - if workflow already has a draft version, update metadata only
   - if workflow is active with no draft, clone current published version into a new draft version
   - preserve historical published versions as immutable snapshots
2. Port and infrastructure
   - add helpers to create draft from published version tree
3. Tests
   - active workflow edit creates new draft version
   - draft update does not mutate published version

Acceptance gate:

- workflow authoring model matches schema design in `03_workflows.md`

## Wave 7 - Workflow Scheme CRUD

Use cases:

- `UC-PM-251` Create Workflow Scheme
- `UC-PM-252` Update Workflow Scheme
- `UC-PM-253` Get Workflow Scheme by ID
- `UC-PM-254` List Workflow Schemes
- `UC-PM-255` Delete Workflow Scheme

Deliverables:

- scheme CRUD with visible read scope
- delete guard when bound to projects
- default workflow must be published/effective

Detailed tasks:

1. Domain layer
   - implement workflow scheme service with tenant-only write path
   - visible read path should include system-owned schemes when needed
   - validate `default_workflow_id` resolves to active published workflow
2. Port and infrastructure
   - extend `IWorkflowSchemePort` for paging/filter/update/delete
   - add project binding guard in project read port/repository/adapter
3. Application/UI
   - create `application/workflowscheme/**`
   - add controller and requests
4. Tests
   - delete blocked when active project uses scheme
   - system scheme is read-only in get/list

Acceptance gate:

- project provisioning and work item runtime can depend on workflow scheme CRUD safely

## Wave 8 - Workflow Scheme Item Management

Use case:

- `UC-PM-256` Manage Workflow Scheme Items

Deliverables:

- replace-all mapping API for issue type to workflow bindings
- default workflow fallback remains canonical

Detailed tasks:

1. Domain layer
   - bulk validate issue types and workflows in visible scope
   - enforce active workflow requirement
   - replace all scheme items transactionally
2. Port and infrastructure
   - bulk fetch issue types/workflows to avoid N+1
   - add list/detail response helpers for mapped workflows
3. Tests
   - duplicate issue type mapping rejected
   - inactive workflow rejected
   - unmapped issue type resolves to scheme default

Acceptance gate:

- `WorkflowSchemeCompatibilityValidator` passes for valid schemes
- `WorkflowService.resolveWorkflow(...)` behaves consistently for create/transition flows

## Wave 9 - Clone, Delete, and Regression Hardening

Use cases:

- `UC-PM-227` Clone Workflow
- `UC-PM-225` Delete Workflow

Deliverables:

- explicit admin-facing clone API
- delete guard when referenced by workflow schemes
- focused regression against project provisioning and work item transition runtime

Detailed tasks:

1. Domain layer
   - expose clone flow already partially present in provisioning support
   - block delete for system workflow and in-use workflow
2. Tests
   - clone preserves version tree, steps, transitions, and rules
   - delete blocked when workflow is referenced by scheme
   - regression against `UC-PM-001`, `UC-PM-101`, `UC-PM-106`

Acceptance gate:

- workflow engine is safe for tenant authoring and project runtime reuse

## Cross-Cutting Rules To Keep Consistent

- Read path should include tenant-owned + system-owned artifacts where runtime or provisioning already depends on system templates
- Write path should stay tenant-only for workflow/category/status/scheme edits
- Published workflow versions are immutable
- Runtime resolution must always use `current_published_version_id`
- New authoring changes must target `draft_version_id`
- Avoid Kafka/outbox for workflow authoring flows unless the use case explicitly requires integration events
- Prefer batch lookup in scheme/detail APIs to avoid N+1 on steps, transitions, rules, statuses, and issue types

## Suggested Validation Commands

Run from `pm_core/` after each wave:

```bash
./mvnw.cmd clean compile
./mvnw.cmd "-Dtest=TransitionWorkItemCommandHandlerTest,WorkItemTransitionRuleEvaluatorTest" test
```

Wave-specific additions:

```bash
./mvnw.cmd "-Dtest=WorkflowServiceTest,WorkflowHandlersTest" test
./mvnw.cmd "-Dtest=WorkflowSchemeServiceTest,WorkflowSchemeHandlersTest" test
./mvnw.cmd "-Dtest=CreateProjectCommandHandlerTest,TransitionWorkItemCommandHandlerTest" test
```

Optional broader check:

```bash
./mvnw.cmd test
```
