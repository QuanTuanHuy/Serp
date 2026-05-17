# PM Core Optimization Plan

Date: 2026-05-12
Last reviewed: 2026-05-17
Scope: selected-work-item planning assistant for assignment, schedule, dependency, capacity, and risk suggestions.

## Objective

Build `Optimize Plan` as a planning assistant:

```text
Generate -> Review -> Adjust -> Apply
```

The optimizer must not update project data during generation. It creates a persisted optimization run, lets the user review suggestions, then applies only selected accepted changes.

MVP target:

```text
Backlog / Board -> Optimize selected items
```

MVP includes:

1. Persisted optimization runs.
2. Dedicated planning table for planned start/end.
3. Assignment suggestions that can update `work_items.assignee_id` on apply.
4. Schedule suggestions that can upsert `work_item_plans` on apply.
5. Dependency behavior on issue link types instead of text heuristics.

## Current Baseline

- `WorkItemEntity` already has `tenantId`, `projectId`, `assigneeId`, `reporterId`, `parentId`, `prioritySequence`, `statusCategoryKey`, `resolutionId`, `startDate`, `dueDate`, `rank`, `timeOriginalEstimate`, and `timeRemainingEstimate`.
- `IssueLinkEntity` stores `sourceId`, `targetId`, and `linkTypeId`.
- `IssueLinkTypeEntity` now has machine-readable `dependencyBehavior`.
- `ProjectEntity` has `leadUserId`.
- `ProjectComponentEntity` has `leadUserId` and `assigneeType`.
- `work_item_plans`, `optimization_runs`, `optimization_run_items`, and `optimization_run_warnings` are implemented by migration `V21__add_optimization_foundation.sql`.
- Backend API endpoints for generate, get review, update item decision, apply, and discard are implemented in `OptimizationRunController`.
- Domain generation is implemented by `OptimizationProjectModelBuilder` and `GreedyOptimizationRunGenerator`.
- Review/apply flow is implemented by `UpdateOptimizationRunItemDecisionCommandHandler` and `ApplyOptimizationRunCommandHandler`.
- Existing candidate sources are current assignee, project lead, and reporter. Component lead flag exists in the model but component leads are not loaded into the builder yet.
- Work item component relation is stored in `work_item_components` through `WorkItemComponentModel`.
- Project member pool can be derived from `ProjectRoleActorEntity` rows where `subjectType = USER`.
- Assignable project members must be determined through `ASSIGNABLE_USER` permission evaluation.
- Calendar, cross-project workload, and skill data do not exist in `pm_core` yet.

## Non-Negotiable Rules

- Snapshot-first, apply-later.
- No direct mutation during generation.
- No overwrite of `work_items.due_date`; due date remains business deadline.
- Schedule apply writes `work_item_plans`, not `work_items.due_date`.
- Domain algorithms stay pure and deterministic.
- Optimization domain must not access JPA repositories or persistence models.
- All persisted reads/writes remain tenant-scoped.
- Every suggestion must include reasons, warnings, or constraint violations.
- MVP must not introduce CP-SAT, OR-Tools, ML prediction, calendar integration, or whole-project optimization.

## Product Use Case

Use case:

```text
UC-PM-OPT-001 - Generate And Apply Optimization Plan
```

Primary actors:

```text
Project Manager
Scrum Master
Team Lead
Project Admin
```

Secondary actors:

```text
Team Member
System Optimizer
Outbox Publisher
Notification Consumer
```

Generate permission is out of MVP scope. Apply still must revalidate write permissions through existing command/write rules when actual data changes.

### Main Flow

```text
1. User selects 10-50 work items from backlog or board.
2. User clicks Optimize selected.
3. User selects optimization mode, planning range, allow reassignment, and allow schedule changes.
4. Backend builds OptimizationProjectModel.
5. Backend persists OptimizationRun and OptimizationRunItems.
6. UI shows Summary, Assignment Suggestions, Schedule Suggestions, and Risks tabs.
7. User accepts, rejects, or overrides suggestions.
8. User clicks Apply selected.
9. Backend revalidates run, work item state, and write permission.
10. Backend applies accepted assignment changes to work_items.
11. Backend applies accepted schedule changes to work_item_plans.
12. Backend marks run/items applied or skipped.
13. UI refreshes planning/board view.
```

### MVP Modes

```text
BALANCED_WORKLOAD
MINIMAL_REASSIGNMENT
ASSIGNMENT_ONLY
SCHEDULE_ONLY
```

Later modes:

```text
FASTEST_DELIVERY
DEADLINE_SAFE
```

### MVP Constraints

```text
allowReassignment: true/false
allowScheduleChanges: true/false
planningStart/planningEnd: required
selectedWorkItemIds: required, max 50
```

Default rules:

```text
respect dependencies: hard
respect due dates: soft
respect current assignee: soft when allowReassignment=true, hard when false
daily capacity: 8h Monday-Friday, 0h Saturday-Sunday
```

## Review Screen

### Tab 1 - Summary

Show:

```text
Scope size
Assignee count
Dependency count
Planning horizon
Assignment suggestion count
Scheduled item count
Late items before/after
Overloaded hours before/after
Warnings count
Confidence level
```

### Tab 2 - Assignment Suggestions

Columns:

```text
Issue
Current assignee
Suggested assignee
Decision
Reason
Capacity impact
Warning
```

Actions:

```text
Accept
Reject
Override assignee
Lock current assignee
```

### Tab 3 - Schedule Suggestions

Columns:

```text
Issue
Assignee
Planned start
Planned end
Due date
Late by
Dependency reason
Confidence
```

Actions:

```text
Accept
Reject
Override planned dates
Lock current plan
```

### Tab 4 - Risks And Constraints

Show:

```text
Hard blockers
Soft violations
Missing estimates
Default duration usage
Dependency cycles
No eligible assignee
Overload
Late risks
Stale item warnings
```

## Architecture Boundaries

```text
domain/optimization/
  entity/
  enums/
  model/
  port/
  service/
  service/impl/

application/optimization/
  command/
  query/
  dto/

infrastructure/optimization/
  adapter/
  mapper/
```

Domain:

```text
Pure models and algorithms.
No Spring Data repositories.
No JPA models.
No HTTP DTOs.
```

Application:

```text
Builds project model through domain ports.
Runs optimization services.
Persists run snapshots through ports.
Applies accepted changes through existing write paths or dedicated application commands.
Owns transaction boundaries for generate/apply commands.
```

Infrastructure:

```text
Implements ports with repositories.
Maps between domain entities and persistence models.
Stores optimization runs, run items, warnings, and work item plans.
```

Advanced solver integration, if any, belongs in infrastructure later:

```text
infrastructure/optimization/solver/
```

## Data Model Plan

### Issue Link Dependency Behavior

Add machine-readable dependency behavior to issue link type.

Domain enum:

```text
IssueLinkDependencyBehavior
  NONE
  SOURCE_BLOCKS_TARGET
  SOURCE_DEPENDS_ON_TARGET
```

Mapping:

```text
NONE:
  ignore for scheduling

SOURCE_BLOCKS_TARGET:
  dependency edge = sourceId -> targetId

  Example: SERP-10 blocks SERP-11
  SERP-10 must finish before SERP-11 starts.

SOURCE_DEPENDS_ON_TARGET:
  dependency edge = targetId -> sourceId

  Example: SERP-11 depends on SERP-10
  SERP-10 must finish before SERP-11 starts.
```

Required changes:

```text
IssueLinkTypeEntity.dependencyBehavior
IssueLinkTypeModel.dependencyBehavior
IssueLinkTypeMapper mapping
Flyway migration: issue_link_types.dependency_behavior VARCHAR(50) NOT NULL DEFAULT 'NONE'
Seed/data migration: Blocks -> SOURCE_BLOCKS_TARGET, Clones/Relates -> NONE
Create/update issue link type DTO validation if field is exposed
```

### Work Item Planning Table

Add dedicated table:

```text
work_item_plans
```

Purpose:

```text
Store planned start/end separately from due date.
Allow optimization and manual planning without overwriting business deadlines.
```

Fields:

```text
id
tenant_id
project_id
work_item_id
planned_start
planned_end
source
source_run_id
locked
created_at
created_by
updated_at
updated_by
deleted_at
```

Source enum:

```text
WorkItemPlanSource
  MANUAL
  OPTIMIZATION
  IMPORT
```

Indexes:

```text
unique active (tenant_id, work_item_id) where deleted_at is null
index (tenant_id, project_id)
index (tenant_id, source_run_id)
```

Apply schedule rule:

```text
Upsert active work_item_plans by tenantId + workItemId.
Set plannedStart/plannedEnd from accepted suggestion.
Set source = OPTIMIZATION.
Set sourceRunId = optimization run id.
Never update work_items.due_date.
```

### Optimization Run Tables

Add:

```text
optimization_runs
optimization_run_items
optimization_run_warnings
```

`optimization_runs` fields:

```text
id
tenant_id
project_id
scope
mode
status
planning_start
planning_end
allow_reassignment
allow_schedule_changes
selected_work_item_count
summary_json
created_at
created_by
updated_at
updated_by
applied_at
applied_by
discarded_at
deleted_at
```

`optimization_run_items` fields:

```text
id
tenant_id
run_id
project_id
work_item_id
work_item_updated_at_snapshot
plan_updated_at_snapshot
current_assignee_id
suggested_assignee_id
override_assignee_id
current_planned_start
current_planned_end
suggested_planned_start
suggested_planned_end
override_planned_start
override_planned_end
current_due_date
assignment_decision
schedule_decision
assignment_apply_status
schedule_apply_status
score
cost
confidence
assignment_reasons_json
schedule_reasons_json
violations_json
applied_at
assignment_skipped_reason
schedule_skipped_reason
created_at
created_by
updated_at
updated_by
deleted_at
```

`optimization_run_warnings` fields:

```text
id
tenant_id
run_id
work_item_id nullable
severity
code
message
details_json
created_at
created_by
deleted_at
```

Run status:

```text
GENERATED
PARTIALLY_APPLIED
APPLIED
DISCARDED
FAILED
EXPIRED
```

Item decision:

```text
OptimizationDecision
PENDING
ACCEPTED
REJECTED
OVERRIDDEN
```

Apply status:

```text
OptimizationApplyStatus
NOT_APPLIED
APPLIED
SKIPPED
FAILED
```

Use separate decisions:

```text
assignmentDecision
scheduleDecision
assignmentApplyStatus
scheduleApplyStatus
```

Reason:

```text
User may accept assignment but reject schedule, or accept schedule but reject assignment.
User intent and apply result must remain separate after partial/stale apply.
```

## API Plan

Generate optimization run:

```http
POST /api/v1/projects/{projectId}/optimization-runs
```

Request:

```json
{
  "scope": "SELECTED_WORK_ITEMS",
  "mode": "BALANCED_WORKLOAD",
  "planningStart": 1715523600000,
  "planningEnd": 1716733200000,
  "allowReassignment": true,
  "allowScheduleChanges": true,
  "selectedWorkItemIds": [101, 102, 103]
}
```

Get review payload:

```http
GET /api/v1/projects/{projectId}/optimization-runs/{runId}
```

Update item decision:

```http
PATCH /api/v1/projects/{projectId}/optimization-runs/{runId}/items/{workItemId}
```

Request:

```json
{
  "assignmentDecision": "ACCEPTED",
  "scheduleDecision": "OVERRIDDEN",
  "overrideAssigneeId": 99,
  "overridePlannedStart": 1715523600000,
  "overridePlannedEnd": 1715610000000
}
```

Apply selected changes:

```http
POST /api/v1/projects/{projectId}/optimization-runs/{runId}/apply
```

Request:

```json
{
  "applyAssignment": true,
  "applySchedule": true,
  "workItemIds": [101, 102, 103]
}
```

Discard run:

```http
POST /api/v1/projects/{projectId}/optimization-runs/{runId}/discard
```

## Phase 1 - Foundation And Schema

### Goals

- Add dependency behavior to issue link types.
- Add `work_item_plans` table.
- Add optimization run persistence tables.
- Add domain contracts and ports required for generation/apply.

### Changes

1. Add `IssueLinkDependencyBehavior` enum.
2. Add `dependencyBehavior` to issue link type domain/model/mapper.
3. Add Flyway migration for `issue_link_types.dependency_behavior`.
4. Add/update seed data so `Blocks` is `SOURCE_BLOCKS_TARGET`.
5. Add work item plan domain/model/mapper/port/adapter.
6. Add optimization run, run item, and warning domain/model/mapper/port/adapter.
7. Add required list/batch port methods.

Required methods:

```text
listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds)
listActivePlansByWorkItemIds(Long tenantId, List<Long> workItemIds)
save/update run and run items
upsert work item plans
```

Definition of Done:

```text
Migrations compile with JPA validate.
Issue link type dependency behavior maps end-to-end.
Optimization run can be persisted and loaded.
Work item plan can be upserted and loaded.
No optimizer algorithm required yet.
```

## Phase 2 - Optimization Project Model

### Core Models

```text
OptimizationProjectModel
OptimizationWorkItem
OptimizationDependencyGraph
OptimizationDependencyEdge
OptimizationDuration
OptimizationPriorityScore
OptimizationCandidateAssignee
ResourceCapacitySlot
OptimizationConstraintViolation
OptimizationSuggestionReason
```

### Builder Input

```text
tenantId
projectId
selectedWorkItemIds
planningStart
planningEnd
allowReassignment
allowScheduleChanges
mode
```

### Builder Flow

```text
1. Load project.
2. Load selected work items.
3. Load current active work item plans.
4. Load issue links among selected work items.
5. Load issue link types.
6. Build dependency graph from dependencyBehavior.
7. Detect cycles.
8. Resolve durations.
9. Calculate critical path if graph is acyclic.
10. Calculate priority/risk scores.
11. Resolve candidate assignees.
12. Build default capacity slots.
13. Collect warnings.
14. Return OptimizationProjectModel.
```

### Dependency Graph Rules

```text
SOURCE_BLOCKS_TARGET -> edge sourceId -> targetId
SOURCE_DEPENDS_ON_TARGET -> edge targetId -> sourceId
NONE -> ignored
```

External dependency rules:

```text
Load links where selected work items are source or target.
Build internal edges only when both source and target are selected.
When external predecessor blocks selected item, add EXTERNAL_DEPENDENCY warning.
When selected item blocks external successor, add informational EXTERNAL_DEPENDENCY warning.
If external predecessor is done, do not constrain selected item.
If external predecessor has active plan, use its plannedEnd as earliestStart lower bound.
If external predecessor state is unknown, mark selected item schedule confidence LOW.
```

Examples:

```text
External SERP-1 blocks selected SERP-2 -> warning and schedule constraint if plan exists.
Selected SERP-2 blocks external SERP-3 -> warning only.
```

If hard dependency cycle exists:

```text
Schedule generation is disabled for affected graph.
Assignment optimization may still run.
Warnings include dependency cycle path.
```

### Done Detection

Treat work item as done when:

```text
statusCategoryKey equals DONE ignoring case
or resolutionId is not null
```

Done items are excluded from assignment/schedule suggestions by default.

### Duration Resolver

Order:

```text
1. timeRemainingEstimate > 0
2. timeOriginalEstimate > 0
3. default by issueTypeHierarchyLevel
4. global fallback
```

Defaults:

```text
Subtask: 2h
Standard issue: 1 day
Epic/higher: 3 days
Unknown: 1 day
```

Warnings:

```text
MISSING_ESTIMATE
DEFAULT_DURATION_USED
LOW_CONFIDENCE_DURATION
```

### Priority/Risk Score

MVP factors:

```text
prioritySequence
due date pressure
critical path membership
outgoing blocker count
missing estimate penalty
```

Sequence normalization must be explicit in code. If priority order cannot be confirmed, use neutral priority factor and warning.

### Candidate Assignee Resolver

Implemented sources:

```text
current assignee
project lead
reporter
```

Sprint 6 sources:

```text
component lead
active project role actors with USER subject
members with ASSIGNABLE_USER permission
```

Current exclusions:

```text
historical expertise
team calendar
skills matching
multi-project workload
```

Current implementation notes:

```text
OptimizationCandidateAssignee has componentLead flag and cost bonus support.
OptimizationProjectModelBuilder does not load project components yet, so componentLead is never set.
Override assignee validation only accepts generated candidates.
Until project member source exists, override choices are limited to current assignee, project lead, and reporter.
```

Next resolver target:

```text
1. Load work item component links from work_item_components.
2. Load project components and add component lead as high-signal candidate.
3. Load active project role actors where subjectType = USER.
4. Evaluate ASSIGNABLE_USER for each user in the selected project.
5. Add assignable project members as lower-priority fallback candidates.
6. Keep current assignee, project lead, and reporter as high-signal candidates when valid.
7. Keep deterministic sorting by effectiveCost, candidateId, workItemId.
```

If no candidate exists:

```text
Keep current/unassigned state.
Add NO_ELIGIBLE_ASSIGNEE warning.
Skip assignment apply for item.
Skip schedule if no assignee after optimization.
```

### Deterministic Ordering

All optimization output must be deterministic for same input.

Work item ordering:

```text
priorityScore desc
dueDate asc nulls last
rank asc nulls last
workItemId asc
```

Candidate tie-breaker:

```text
effectiveCost asc
candidateId asc
workItemId asc
```

Schedule ready queue tie-breaker:

```text
criticalPath desc
priorityScore desc
dueDate asc nulls last
duration asc
rank asc nulls last
workItemId asc
```

Definition of Done:

```text
Model builder returns deterministic project model for selected work items.
Cycle detection works.
Durations never <= 0.
Warnings are structured.
Unit tests cover graph, duration, critical path, scoring, candidates, and model builder.
```

## Phase 3 - Generate Optimization Run MVP

### Assignment Algorithm

Use greedy MVP:

```text
1. Sort work items by priority/risk score descending.
2. For each work item, choose eligible candidate with lowest effective cost.
3. Respect allowReassignment.
4. Prefer current assignee when mode is MINIMAL_REASSIGNMENT.
5. Avoid capacity overload when possible.
6. If overload is unavoidable, choose least-bad candidate and add violation.
```

Cost factors:

```text
candidate base cost
current assignee retention bonus
component lead bonus
project lead bonus
overload penalty
reassignment penalty
```

### Schedule Algorithm

Use serial schedule generation MVP:

```text
1. Stop scheduling affected graph if hard dependency cycle exists.
2. Use topological order.
3. Sort ready items by critical path, priority score, due date, then duration.
4. For each item, find earliest available capacity slot for chosen/current assignee.
5. Set suggestedPlannedStart/suggestedPlannedEnd.
6. Calculate lateness against dueDate.
7. Add reasons for delays and violations.
```

### Generate Output

Persist:

```text
OptimizationRun status GENERATED
OptimizationRunItems with assignment/schedule suggestions
OptimizationRunWarnings
```

Default item decisions:

```text
assignmentDecision = PENDING when suggestion changes assignee
assignmentDecision = ACCEPTED when no assignment change is needed
scheduleDecision = PENDING when schedule suggestion exists
assignmentApplyStatus = NOT_APPLIED
scheduleApplyStatus = NOT_APPLIED
```

Definition of Done:

```text
POST generate creates persisted run.
GET run returns review payload.
Summary metrics include before/after late/overload counts where available.
No work item or planning row changes during generation.
```

## Phase 4 - Review, Override, Apply

Status: implemented in backend.

Implemented files:

```text
ui/rest/optimization/OptimizationRunController.java
application/optimization/command/update/UpdateOptimizationRunItemDecisionCommandHandler.java
application/optimization/command/apply/ApplyOptimizationRunCommandHandler.java
application/optimization/command/discard/DiscardOptimizationRunCommandHandler.java
```

### Review/Override

User can update item decisions before apply:

```text
Accept assignment
Reject assignment
Override assignee
Accept schedule
Reject schedule
Override planned dates
```

Backend must persist decisions on run item. Backend must not trust apply request to carry final suggestion details.

Assignment override validation:

```text
overrideAssigneeId is required when assignmentDecision = OVERRIDDEN.
MVP only allows overrideAssigneeId when user appears in generated candidate list.
If later user/member source is available, also validate user exists, belongs to tenant, is active, and can be assigned to project.
Invalid override is rejected and INVALID_OVERRIDE warning is added.
```

Schedule override validation:

```text
overridePlannedStart and overridePlannedEnd are required when scheduleDecision = OVERRIDDEN.
overridePlannedStart < overridePlannedEnd.
Override dates are stored as epoch millis.
MVP requires override range to stay within run planningStart/planningEnd.
Hard dependency violations are rejected.
Capacity and due-date violations are allowed only as soft warnings.
Locked active plans are not overwritten in MVP.
```

Locked plan rule:

```text
If current active work_item_plans.locked = true, schedule apply is skipped with LOCKED_PLAN.
```

### Apply Assignment

For selected items:

```text
1. Load run item.
2. Check assignmentDecision is ACCEPTED or OVERRIDDEN.
3. Revalidate work item still belongs to tenant/project.
4. Detect stale assignment state.
5. Update work_items.assignee_id to overrideAssigneeId or suggestedAssigneeId through existing work item write flow where available.
6. Store audit/outbox through existing write flow where applicable.
7. Set assignmentApplyStatus = APPLIED, SKIPPED, or FAILED.
```

### Apply Schedule

For selected items:

```text
1. Load run item.
2. Check scheduleDecision is ACCEPTED or OVERRIDDEN.
3. Revalidate work item still belongs to tenant/project.
4. Detect stale planning state.
5. Upsert work_item_plans with override or suggested planned start/end.
6. Set source = OPTIMIZATION and sourceRunId = runId.
7. Set scheduleApplyStatus = APPLIED, SKIPPED, or FAILED.
```

### Apply Completion

After apply:

```text
Set assignmentApplyStatus or scheduleApplyStatus to APPLIED for applied changes.
Set assignmentApplyStatus or scheduleApplyStatus to SKIPPED for stale, locked, denied, or invalid changes.
Mark run APPLIED if all selected accepted items applied.
Mark run PARTIALLY_APPLIED if some selected accepted items skipped.
Never apply discarded, failed, expired, or already applied runs.
If no accepted/overridden changes exist, return no-op and keep run GENERATED.
```

Definition of Done:

```text
PATCH item decision persists user choices.
POST apply updates assignment and planning rows only for accepted selected items.
Apply is tenant-scoped and transaction-safe.
Stale items are skipped, not silently overwritten.
Run status reflects actual result.
```

Remaining backend gaps after Phase 4:

```text
Component lead candidate source is modeled but not loaded.
Project member pool is not used as candidate source.
ASSIGNABLE_USER eligibility is not used for assignment ranking or override validation.
Skill matching is not used for candidate ranking.
Capacity uses fixed 8h weekday slots only.
Cross-project workload is not subtracted from capacity.
```

## Phase 6 - Resource Intelligence

Status: planned next backend phase.

Sprint 6 scope is constrained to data available in `pm_core`. It improves candidate quality and creates extension seams for future resource data without pretending calendar, workload, or skill integrations exist today.

### Goals

```text
1. Expand candidate source beyond current assignee, project lead, and reporter.
2. Add component lead and active project member candidate sources.
3. Use ASSIGNABLE_USER permission as assignment eligibility gate for project members.
4. Keep role actor data as member source, not as assignability rule by itself.
5. Keep fixed 8h weekday capacity, but isolate it behind a capacity provider seam.
6. Emit unavailable-data warnings for missing calendar, workload, and skill sources where relevant.
```

### Candidate Sources

Sprint 6 candidate source priority:

```text
1. Current assignee.
2. Component lead.
3. Project lead.
4. Reporter.
5. Active project role actors with USER subject.
6. Project members passing ASSIGNABLE_USER permission.
```

Eligibility rules:

```text
tenant scoped user
active project role actor with subjectType = USER
ASSIGNABLE_USER permission for selected project
current assignee/project lead/reporter may appear as high-signal candidates even when not role-actor sourced
skill match is not evaluated in Sprint 6 because skill data does not exist
```

Ranking factors:

```text
current assignee retention bonus
component lead bonus
project lead bonus
reporter small bonus
assignable project member bonus
capacity availability
reassignment penalty
overload penalty
```

Component lead resolution:

```text
1. Load selected work item ids.
2. Load active work_item_components rows by tenantId + workItemId.
3. Load linked ProjectComponentEntity rows by component ids, projectId, and tenantId.
4. For each selected work item, add component.leadUserId as component lead candidate when present.
5. Merge duplicate candidates and keep all source flags.
```

Project member resolution:

```text
1. Load ProjectRoleActorEntity rows by projectId and tenantId.
2. Keep rows where subjectType = USER.
3. Parse subjectId to userId.
4. Evaluate ASSIGNABLE_USER for each user in project context.
5. Add only assignable users as project member fallback candidates.
6. If role actors cannot be loaded, emit NO_PROJECT_MEMBER_POOL warning and continue with high-signal candidates.
```

### Required Sprint 6 Ports

Add domain ports or extend existing ports. Infrastructure must use existing `pm_core` data only.

```text
IWorkItemComponentReadPort
listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds)

IProjectMemberCandidatePort
listAssignableMembers(Long tenantId, Long projectId)

IResourceCapacityPort
getCapacitySlots(Long tenantId, List<Long> userIds, Long planningStart, Long planningEnd)
```

Defer these ports until real data source exists:

```text
IResourceCalendarPort

IResourceWorkloadPort

IResourceSkillPort
```

### Calendar And Cross-Project Workload

Current schedule uses fallback capacity:

```text
8h Monday-Friday
0h Saturday-Sunday
UTC day slots
```

Sprint 6 behavior:

```text
1. Move default capacity generation behind IResourceCapacityPort.
2. Keep default 8h weekday UTC slots as only concrete provider.
3. Emit LOW_CONFIDENCE_CAPACITY or MISSING_CALENDAR warning to make fallback visible.
4. Emit MISSING_CROSS_PROJECT_WORKLOAD warning when schedule quality depends on workload data.
5. Do not subtract cross-project workload in Sprint 6 because data source does not exist.
6. Keep deterministic slot ordering.
```

### Skill Matching

Skill matching is out of Sprint 6 implementation because skill data does not exist in `pm_core`.

```text
Sprint 6 behavior:
1. Do not infer skills from labels or text.
2. Do not add skill cost factors.
3. Emit SKILL_DATA_UNAVAILABLE only if API/review output needs to explain absence of skill ranking.
4. Keep candidate model extensible for future skill flags or reasons.
```

Later skill rule:

```text
Missing skill data must not block assignment.
Known skill match reduces candidate cost.
Known skill mismatch increases candidate cost only when enough candidates exist.
```

### Definition Of Done

```text
Component lead candidates are loaded and tested.
Active USER role actors passing ASSIGNABLE_USER are included as candidates.
Generated candidate list contains source/reason data for review UI.
Override assignee validation accepts generated assignable project members.
Default 8h weekday capacity is provided through IResourceCapacityPort.
Fallback capacity warning is emitted.
Missing calendar/workload/skill data is explicit and does not block generation.
Candidate resolver tests cover component leads, role actors, ASSIGNABLE_USER gating, duplicates, and deterministic ordering.
Scheduler tests cover fallback capacity provider behavior.
```

## Alternative Flows

### AF-1 Dependency Cycle Exists

```text
System detects cycle.
Schedule generation disabled for affected items.
Assignment optimization may continue.
UI shows cycle path and asks user to fix dependency.
```

Warning code:

```text
DEPENDENCY_CYCLE
```

### AF-2 External Dependency Exists

```text
Selected item has dependency edge to item outside selected scope.
If outside item blocks selected item, schedule confidence is reduced.
If outside item has active plan, selected item cannot start before outside plannedEnd.
If outside item state is unknown, user should review before apply.
```

Warning code:

```text
EXTERNAL_DEPENDENCY
```

### AF-3 Work Item Missing Estimate

```text
System uses default duration.
Duration confidence is low.
UI shows warning and lets user continue or edit estimate then regenerate.
```

Warning code:

```text
DEFAULT_DURATION_USED
```

### AF-4 No Eligible Assignee

```text
System cannot suggest assignee.
Current assignee is kept if present.
Unassigned item remains unassigned.
Schedule skipped if no assignee exists.
```

Warning code:

```text
NO_ELIGIBLE_ASSIGNEE
```

### AF-5 Over Capacity Unavoidable

```text
System returns best-effort plan.
UI shows overload and late warnings.
Suggested remedies: remove items, extend horizon, increase capacity, or allow reassignment.
```

Warning code:

```text
OVER_CAPACITY
```

### AF-6 No Reassignment

```text
allowReassignment = false.
Assignment optimizer is skipped.
Scheduler uses current assignees only.
Unassigned items are flagged and not scheduled.
```

### AF-7 Minimal Changes

```text
mode = MINIMAL_REASSIGNMENT.
Reassignment penalty is high.
System changes assignee only when it materially reduces overload or deadline risk.
```

### AF-8 Stale Run

```text
Work item or planning row changed after run generation.
Apply skips stale item or returns conflict depending API option.
User can regenerate run.
```

Warning code:

```text
STALE_ITEM
```

### AF-9 Permission Changed Before Apply

```text
User can generate run but loses write permission before apply.
Apply is denied by write flow.
Run remains GENERATED or PARTIALLY_APPLIED.
```

### AF-10 Partial Apply

```text
Some selected items apply successfully.
Some are stale, invalid, or denied.
System marks valid items APPLIED and invalid items SKIPPED.
Run becomes PARTIALLY_APPLIED.
```

### Warning Codes

```text
DEPENDENCY_CYCLE
EXTERNAL_DEPENDENCY
MISSING_ESTIMATE
DEFAULT_DURATION_USED
LOW_CONFIDENCE_DURATION
NO_ELIGIBLE_ASSIGNEE
OVER_CAPACITY
LATE_RISK
STALE_ITEM
PERMISSION_DENIED
INVALID_OVERRIDE
LOCKED_PLAN
NEUTRAL_PRIORITY_USED
LOW_CONFIDENCE_CAPACITY
MISSING_CALENDAR
MISSING_CROSS_PROJECT_WORKLOAD
NO_PROJECT_MEMBER_POOL
SKILL_DATA_UNAVAILABLE
```

## Implementation Order

### Sprint 1 - Schema And Contracts

Status: implemented.

```text
1. Add IssueLinkDependencyBehavior.
2. Add dependency_behavior column and seed values.
3. Add work_item_plans table and domain/persistence plumbing.
4. Add optimization run tables and domain/persistence plumbing.
5. Add compile-level tests/mapping tests.
```

### Sprint 2 - Model Builder

Status: implemented with candidate-source limitations.

```text
1. Add optimization domain models.
2. Add dependency graph builder.
3. Add cycle detection and topological sort.
4. Add duration resolver.
5. Add critical path calculator.
6. Add priority/risk scoring.
7. Add candidate assignee resolver.
8. Add OptimizationProjectModelBuilder.
```

### Sprint 3 - Generate Run

Status: implemented in backend.

```text
1. Add GenerateOptimizationRunCommand.
2. Add greedy assignment optimizer.
3. Add serial schedule generator.
4. Persist run, items, warnings, and summary.
5. Add GET review query.
```

### Sprint 4 - Review And Apply

Status: implemented in backend.

```text
1. Add update item decision command.
2. Add apply selected command.
3. Apply assignment changes.
4. Apply schedule changes to work_item_plans.
5. Add stale checks.
6. Add run/item status transitions.
```

### Sprint 5 - UI Integration

Status: not implemented in this backend service.

```text
1. Add Optimize selected entry point.
2. Add generate form.
3. Add review tabs.
4. Add accept/reject/override interactions.
5. Add apply selected flow.
```

### Sprint 6 - Resource Intelligence

Status: planned next backend phase.

```text
1. Add work item component read port/adapter for work_item_components.
2. Load component leads into candidate resolver.
3. Add project member candidate port/adapter backed by ProjectRoleActorEntity USER subjects.
4. Gate project member candidates with ASSIGNABLE_USER permission.
5. Update override validation to accept generated assignable project member candidates.
6. Add capacity provider seam that returns current 8h weekday UTC fallback slots.
7. Emit missing calendar, workload, and skill warnings without adding fake integrations.
8. Update optimization tests for candidate sources, assignability, deterministic ordering, and fallback capacity.
```

### Later - Advanced Optimization

```text
1. Add deadline-safe and fastest-delivery modes.
2. Add resource leveling local search.
3. Add async deep optimizer with solver fallback.
```

## Test Strategy

Unit tests:

```text
Dependency graph empty/simple/branch/cycle/non-dependency ignored
Dependency behavior direction mapping
Duration resolver by each source
Critical path chain/fork/join
Priority/risk scoring deterministic factors
Candidate resolver duplicate and no-candidate cases
Greedy assignment respects allowReassignment
Serial scheduler respects hard dependencies
Apply skips stale items
Candidate resolver includes component leads and project members
Candidate resolver gates project members by ASSIGNABLE_USER
Candidate resolver merges duplicate source flags deterministically
Scheduler uses fallback capacity provider
Warnings emitted for missing calendar, workload, and skill data when applicable
```

Handler/query tests:

```text
Generate run persists run/items/warnings
Get run returns review payload
Patch item decision persists override
Apply assignment updates assignee only for accepted items
Apply schedule upserts work_item_plans only for accepted items
Partial apply marks skipped items
```

Validation commands:

```bash
./mvnw.cmd -Dtest='*Optimization*Test' test
./mvnw.cmd clean compile
```

## Risks And Open Notes

- Default 8h/day capacity is current implementation and ignores holidays, PTO, and part-time schedules.
- Candidate sources are weak until component lead and assignable project member integration exists.
- Component lead cost support exists, but component leads are not loaded into current model builder.
- Project member source should come from `ProjectRoleActorEntity` USER subjects, then be gated by `ASSIGNABLE_USER`.
- Dependency behavior migration must preserve existing `Blocks`, `Clones`, and `Relates` rows.
- Selected items may omit external blockers; model builder should warn when links point outside selected scope.
- Schedule quality remains limited without real calendars and cross-project workload.
- Cross-project workload requires integration outside `pm_core` or a future shared planning store.
- Skill data does not exist yet; do not implement text/label skill heuristics in Sprint 6.
- Workload data may be sensitive; apply/review permission model must be revisited after MVP.
- Whole-project optimization remains out of MVP because results are slower, noisier, and harder to review.
