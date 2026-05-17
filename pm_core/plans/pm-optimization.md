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
- Existing candidate sources are current assignee, component lead, project lead, reporter, and assignable project members.
- Work item component relation is stored in `work_item_components` through `WorkItemComponentModel`.
- Project member pool is derived from `ProjectRoleActorEntity` rows where `subjectType = USER`.
- Assignable project members are gated by `ASSIGNABLE_USER` permission through `ProjectMemberService`.
- Component leads are loaded through `IWorkItemComponentReadPort` and `IProjectComponentPort`.
- Fallback resource capacity seam exists through `IResourceCapacityPort` and `IResourceCalendarPort`.
- Current calendar implementation is fallback 8h weekday UTC slots only.
- Cross-project and same-project outside-scope workload are derived from active `work_item_plans` in `pm_core` and subtracted from fallback capacity.
- Capacity source metadata is stored in optimization summary and exposed by review payload.
- Skill data does not exist yet. Future implementation will store `skills`, `work_item_skills`, and `user_skills` inside `pm_core`.

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
component lead
project lead
reporter
active project role actors with USER subject and ASSIGNABLE_USER permission
```

Deferred sources:

```text
historical expertise
team calendar
external/shared workload outside pm_core
```

Current implementation notes:

```text
OptimizationCandidateAssignee has currentAssignee, componentLead, projectLead, reporter, and projectMember flags.
OptimizationProjectModelBuilder loads work_item_components and project components for component leads.
ProjectMemberService loads USER role actors and gates them with ASSIGNABLE_USER.
Override assignee validation accepts generated candidates, including assignable project member candidates.
```

Resolver behavior:

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

Backend gaps after Phase 4 that were closed in later sprints:

```text
Component lead candidate source is loaded in Sprint 6.
Project member pool is used as candidate source in Sprint 6.
ASSIGNABLE_USER eligibility is used for project member candidates in Sprint 6.
Capacity seam and fallback calendar provider were added in Sprint 6/9.
Cross-project workload is subtracted from capacity in Sprint 8/10.
Skill matching remains deferred because skill data does not exist yet.
```

## Phase 6 - Resource Intelligence

Status: implemented in backend.

Sprint 6 scope is constrained to data available in `pm_core`. It improves candidate quality and creates extension seams for future resource data without pretending calendar, workload, or skill integrations exist today.

Implemented files:

```text
domain/optimization/service/impl/OptimizationProjectModelBuilder.java
domain/project/service/impl/ProjectMemberService.java
domain/optimization/port/IWorkItemComponentReadPort.java
infrastructure/store/adapter/WorkItemComponentReadAdapter.java
domain/optimization/port/IResourceCapacityPort.java
domain/optimization/port/IResourceCalendarPort.java
infrastructure/optimization/adapter/FallbackResourceCapacityAdapter.java
infrastructure/optimization/adapter/FallbackResourceCalendarAdapter.java
```

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

Implemented behavior:

```text
1. Move default capacity generation behind IResourceCapacityPort.
2. Keep default 8h weekday UTC slots as only concrete provider.
3. Keep fallback calendar generation behind IResourceCalendarPort.
4. Emit LOW_CONFIDENCE_CAPACITY and MISSING_CALENDAR warnings to make fallback visible.
5. Subtract same-project outside-scope and cross-project workload from active work_item_plans.
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

## Phase 7 - Real Calendar And Cross-Project Workload Integration

Status: partially implemented.

Implemented:

```text
1. Capacity source modes and coverage statuses exist.
2. Fallback calendar provider contract exists through IResourceCalendarPort.
3. Capacity resolver subtracts active work_item_plans for same-project outside-scope and cross-project workload.
4. Capacity source metadata is persisted in summary_json through OptimizationRunSummary.
5. Review payload exposes summary metadata through OptimizationRunReviewView.summary.
6. Aggregated workload buckets exist through CapacityWorkloadBucket.
```

Not implemented:

```text
1. Authoritative real calendar source for working hours, holidays, leave, and exceptions.
2. Partial/failed external calendar provider behavior beyond fallback.
3. External/shared workload source outside pm_core.
4. Hourly real-calendar slot normalization.
5. Dedicated UI rendering for capacity source metadata.
```

This phase improves schedule quality by replacing fallback-only scheduling with net usable capacity. Net usable capacity subtracts committed workload from available calendar capacity when data exists.

Skill data remains out of this phase and will be handled later.

### Product Goal

```text
1. Give project managers schedule suggestions that reflect real working availability when calendar data exists.
2. Avoid planning selected work into already committed work_item_plans outside the selected scope.
3. Make cross-project workload impact visible without exposing sensitive project details.
4. Keep optimization as review-first suggestion, not automatic resource commitment.
5. Preserve deterministic generation for the same input snapshot.
```

### User Value

Before:

```text
Schedule suggestions assume 8h Monday-Friday capacity and ignore real absence or other planned project commitments.
```

After:

```text
Schedule suggestions use real calendar capacity when available, subtract planned workload from work_item_plans, and clearly show coverage or fallback gaps.
```

### Product Scope

In scope:

```text
calendar integration contract for a future authoritative source
working-hour, holiday, leave, and exception normalization when source exists
cross-project planned workload from active work_item_plans in pm_core
same-project workload outside selected scope
aggregated workload impact in review payload
capacity coverage and fallback visibility in review payload
run-level snapshot metadata for calendar and workload source freshness
```

Out of scope:

```text
skill matching
ML duration prediction
CP-SAT or OR-Tools optimization
automatic rescheduling of other projects
whole-organization capacity planning
automatic conflict resolution across projects
calendar write-back
external meeting scheduling
showing cross-project work item or project details in review payload
```

### Product Flow

```text
1. User selects work items and clicks Optimize selected.
2. Backend resolves candidate assignees.
3. Backend resolves resource availability for candidate assignees.
4. Calendar source provides working/non-working intervals when a source exists; otherwise fallback calendar is used.
5. Workload source loads committed work_item_plans outside selected scope.
6. Capacity resolver subtracts committed workload from calendar capacity.
7. Optimizer schedules selected items into net usable capacity.
8. Review screen shows schedule suggestions, calendar coverage, workload coverage, fallback count, and aggregated workload impact.
9. User accepts, rejects, or overrides suggestions.
10. Apply still updates only selected accepted items.
```

### Review Screen Additions

Summary tab should show:

```text
Capacity source
Calendar coverage status
Workload coverage status
Fallback assignee count
Aggregated workload hours deducted
Same-project outside-scope workload hours deducted
Cross-project workload hours deducted
Calendar source timestamp nullable
Workload source timestamp
Schedule confidence
```

Schedule tab should show per item:

```text
Assignee calendar coverage
Capacity source used
Aggregated workload considered
Fallback warnings if any
```

Risks tab should show:

```text
Missing calendar data
Partial calendar coverage
Missing workload data
Partial workload coverage
Calendar source unavailable
Workload source failure
Cross-project capacity conflict
```

Review payload privacy rule:

```text
Always aggregate cross-project workload details.
Do not expose other project ids, project names, issue ids, issue keys, or issue titles in optimization review payload.
Show only workload totals by assignee and time bucket.
```

### Architecture Goal

The optimizer domain must consume normalized net capacity, not calendar or workload source details.

```text
calendar data + work_item_plans workload -> capacity resolver -> ResourceCapacitySlot list -> scheduler
```

Domain scheduler remains pure:

```text
Input:
ResourceCapacitySlot list
OptimizationWorkItem list
Dependency graph
Assignments
Planning horizon

Output:
Assignment suggestions
Schedule suggestions
Warnings
Summary
```

Infrastructure/application resolves source-specific details.

### Capacity Semantics

`IResourceCapacityPort` should represent net usable capacity for optimization.

```text
net usable capacity = working calendar capacity - committed workload reservations
```

The scheduler must not subtract workload itself. It should only consume available slots returned by the capacity port.

Current fallback provider remains valid as degradation path:

```text
FALLBACK_WEEKDAY_8H_UTC
8h Monday-Friday
0h Saturday-Sunday
UTC day slots
```

Future source modes:

```text
REAL_CALENDAR_WITH_WORKLOAD
REAL_CALENDAR_ONLY
FALLBACK_WITH_WORKLOAD
FALLBACK_WEEKDAY_8H_UTC
```

### Proposed Capacity Resolution Model

Add or represent equivalent metadata in `summary_json`:

```text
CapacityResolutionResult
  slots
  sourceMode
  calendarCoverageStatus
  workloadCoverageStatus
  fallbackUserIds
  calendarFetchedAt nullable
  workloadFetchedAt
  deductedWorkloadMillis
  sameProjectOutsideScopeDeductedMillis
  crossProjectDeductedMillis
  warnings
```

Coverage statuses:

```text
FULL
PARTIAL
MISSING
FAILED
NOT_REQUIRED
```

### Calendar Integration Design

No authoritative calendar service exists yet. Phase implementation must keep calendar integration behind a provider contract and use fallback calendar until a source is available.

Future calendar source must normalize into working intervals before scheduling.

Required calendar concepts:

```text
userId
timezone
working intervals
holiday intervals
leave/PTO intervals
calendar exception intervals
source fetchedAt
source version nullable
```

Normalization rules:

```text
1. Convert source intervals to UTC epoch millis.
2. Keep source timezone in metadata for diagnostics.
3. Split into deterministic capacity slots.
4. Remove holidays and non-working intervals.
5. Reduce capacity for full-day or partial-day leave.
6. Keep capacity non-negative.
7. Sort by assigneeId, slotStart, slotEnd.
```

Recommended granularity:

```text
Phase 7A: daily slots while only fallback calendar exists.
Phase 7B: hourly slots when real working-hour or partial-day leave source exists.
```

### Cross-Project Workload Design

Initial authoritative workload source is active `work_item_plans` in `pm_core`.

Reason:

```text
work_item_plans already represent planned commitment.
They are tenant-scoped.
They support cross-project queries inside pm_core when projects share the same service database.
They are more deterministic than inferring workload from assigned-but-unplanned items.
```

Workload included:

```text
active work_item_plans
same tenant
assignee exists on linked work item
planned range overlaps optimization planning range
work item is not done
work item is outside selected optimization scope
same project outside selected scope
other projects in same tenant
```

Workload excluded:

```text
selected work items in current optimization run
assigned items without active plan in initial implementation
external meetings or non-project work until a calendar/workload source exists
```

Required query shape:

```text
listActivePlansByAssigneeIdsAndRange(
  tenantId,
  assigneeIds,
  planningStart,
  planningEnd,
  excludedWorkItemIds
)
```

Subtraction rules:

```text
1. Convert each active plan into reserved intervals by assignee.
2. Intersect reserved intervals with working capacity slots.
3. Subtract overlapping reserved millis from slot capacity.
4. Do not allow slot capacity below zero.
5. Track deducted millis by assignee and source scope only.
6. Use source scope values SAME_PROJECT_OUTSIDE_SCOPE and CROSS_PROJECT.
7. Emit warning when reservations exceed calendar capacity.
```

Aggregated review output:

```text
assigneeId
timeBucketStart
timeBucketEnd
sameProjectOutsideScopeReservedMillis
crossProjectReservedMillis
totalReservedMillis
```

Do not include source project/work item identity in review output.

### Snapshot And Apply Rules

Generation must snapshot capacity source metadata.

```text
calendarFetchedAt nullable
workloadFetchedAt
capacitySourceMode
coverage status
fallback users
deducted workload summary
```

Apply must not recompute capacity.

Reason:

```text
User applies reviewed suggestions.
Recomputing external workload during apply could produce unreviewed behavior.
Stale work item and plan checks remain the apply safety mechanism.
```

If calendar/workload changes after generation:

```text
Run remains a snapshot.
Review can display stale source warning if detected later.
User should regenerate for latest capacity.
```

### Failure And Fallback Policy

Use per-assignee fallback, not whole-run failure.

```text
1. If real calendar exists and workload exists: use real net capacity.
2. If real calendar exists but workload fails: use calendar-only capacity and emit PARTIAL_WORKLOAD_COVERAGE.
3. If real calendar is missing but workload exists: use fallback calendar minus workload and emit MISSING_CALENDAR.
4. If both calendar and workload fail: use fallback 8h weekday capacity and emit LOW_CONFIDENCE_CAPACITY.
5. Never silently ignore missing source data.
```

Run confidence:

```text
HIGH: full calendar and workload coverage for all scheduled assignees.
MEDIUM: partial fallback or partial workload coverage.
LOW: fallback calendar for most scheduled assignees or workload unavailable.
```

### New Warning Codes

Add when implementation starts:

```text
PARTIAL_CALENDAR_COVERAGE
PARTIAL_WORKLOAD_COVERAGE
CALENDAR_SOURCE_FAILED
WORKLOAD_SOURCE_FAILED
CAPACITY_RESERVATION_EXCEEDS_AVAILABILITY
CROSS_PROJECT_CAPACITY_CONFLICT
```

Keep existing warning codes:

```text
LOW_CONFIDENCE_CAPACITY
MISSING_CALENDAR
MISSING_CROSS_PROJECT_WORKLOAD
OVER_CAPACITY
LATE_RISK
```

### Security And Privacy

Workload data may reveal other project commitments. Review payload must always aggregate cross-project workload details.

Review payload rules:

```text
1. Show aggregated reserved workload by assignee and time bucket.
2. Do not expose other project names, project ids, issue ids, issue keys, or issue titles.
3. Do not make cross-project detail visibility depend on read permission in this phase; aggregate always.
4. Keep apply permission unchanged; apply can only mutate selected accepted items.
5. Tenant scoping is mandatory for all calendar and workload reads.
```

### Definition Of Done

```text
Calendar provider contract exists and fallback remains active while no authoritative source exists.
Cross-project active work_item_plans are subtracted from candidate capacity.
Same-project outside-scope active work_item_plans are subtracted from candidate capacity.
Fallback capacity remains available per assignee when calendar source data is missing.
Review payload shows capacity source, calendar coverage, workload coverage, fallback users, and aggregated workload deductions.
Review payload never exposes cross-project project/work item identity.
Warnings are explicit for missing, partial, or failed source data.
Scheduler consumes net usable capacity and remains deterministic.
Generation persists source metadata in summary_json.
Apply does not recompute external capacity.
Tests cover calendar contract, workload subtraction, fallback, privacy, and deterministic scheduling.
```

## Phase 8 - Skill-Aware Assignment

Status: implemented in backend.

This phase stores skill data in `pm_core` and uses it to rank candidate assignees for selected work items. Skill matching improves assignment quality but does not replace permissions, capacity, dependency, or review/apply rules.

### Product Goal

```text
1. Let project managers define skills required or preferred by a work item.
2. Let pm_core store user skill profiles for assignment optimization.
3. Prefer candidates whose skills match work item needs.
4. Explain skill fit and missing skills in review payload.
5. Keep generation snapshot-based and apply-later.
```

### Data Ownership

```text
pm_core owns skills.
pm_core owns work_item_skills.
pm_core owns user_skills.
No external HR/account skill source is required for initial implementation.
```

### Non-Negotiable Skill Rules

```text
1. Do not infer skills from summary, description, labels, comments, or component names.
2. Only explicit work_item_skills and user_skills affect ranking.
3. Missing skill data must not block optimization generation.
4. ASSIGNABLE_USER permission remains the eligibility gate for project member candidates.
5. Skill matching changes ranking cost, not tenant/project membership rules.
6. Apply must not recompute skill fit.
7. Review payload must not expose unrelated user skills outside selected work item requirements.
```

### Skill Data Model

Add tables:

```text
skills
work_item_skills
user_skills
```

`skills` fields:

```text
id
tenant_id
code
name
description
active
created_at
created_by
updated_at
updated_by
deleted_at
```

`work_item_skills` fields:

```text
id
tenant_id
project_id
work_item_id
skill_id
requirement_type
min_proficiency
weight
source
created_at
created_by
updated_at
updated_by
deleted_at
```

`user_skills` fields:

```text
id
tenant_id
user_id
skill_id
proficiency
confidence
source
verified_at
created_at
created_by
updated_at
updated_by
deleted_at
```

Enums:

```text
SkillRequirementType
  REQUIRED
  PREFERRED

SkillProficiency
  NOVICE
  WORKING
  PROFICIENT
  EXPERT

SkillSource
  MANUAL
  IMPORT
  HR_PROFILE
  INTEGRATION
```

Indexes:

```text
skills: unique active (tenant_id, code) where deleted_at is null
work_item_skills: unique active (tenant_id, work_item_id, skill_id) where deleted_at is null
work_item_skills: index (tenant_id, project_id, work_item_id)
user_skills: unique active (tenant_id, user_id, skill_id) where deleted_at is null
user_skills: index (tenant_id, skill_id, proficiency)
```

### Domain Models And Ports

Domain entities:

```text
SkillEntity
WorkItemSkillEntity
UserSkillEntity
```

Optimization models:

```text
OptimizationSkillRequirement
OptimizationCandidateSkill
OptimizationCandidateSkillFit
```

Read ports:

```text
ISkillReadPort
  listActiveByIds(Long tenantId, List<Long> skillIds)

IWorkItemSkillReadPort
  listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds)

IUserSkillReadPort
  listActiveByUserIds(Long tenantId, List<Long> userIds)
```

### Skill Fit Model

`OptimizationCandidateSkillFit` should contain:

```text
workItemId
candidateId
matchedRequiredSkillCount
totalRequiredSkillCount
matchedPreferredSkillCount
totalPreferredSkillCount
requiredCoveragePercent
preferredCoveragePercent
proficiencyScore
missingRequiredSkillIds
missingPreferredSkillIds
matchedSkillIds
confidence
```

Attach skill fit to `OptimizationCandidateAssignee` so assignment algorithm can rank candidates without side-channel lookup.

### Model Builder Flow Update

```text
1. Resolve candidates as current implementation does.
2. Collect selected workItemIds.
3. Collect candidate userIds.
4. Load work_item_skills by selected work item ids.
5. Load user_skills by candidate user ids.
6. Build skill fit per workItemId + candidateId.
7. Attach skill fit to candidate assignees.
8. Add structured warnings for missing or partial skill data.
```

### Skill-Aware Ranking

Add skill factors to candidate cost:

```text
baseCost
- requiredSkillMatchBonus
- preferredSkillMatchBonus
- proficiencyBonus
+ missingRequiredSkillPenalty
+ missingPreferredSkillPenalty
+ lowConfidenceSkillPenalty
+ existing reassignment/capacity penalties
```

Initial cost constants:

```text
REQUIRED_SKILL_MATCH_BONUS = 25
PREFERRED_SKILL_MATCH_BONUS = 8
EXPERT_PROFICIENCY_BONUS = 6
PROFICIENT_PROFICIENCY_BONUS = 4
WORKING_PROFICIENCY_BONUS = 2
MISSING_REQUIRED_SKILL_PENALTY = 40
MISSING_PREFERRED_SKILL_PENALTY = 6
LOW_CONFIDENCE_SKILL_PENALTY = 5
```

Ranking behavior:

```text
1. Required skill match can outweigh project member fallback and component lead bonus.
2. Current assignee retention still matters in MINIMAL_REASSIGNMENT.
3. Capacity overload remains a heavy penalty.
4. Candidate with missing required skills remains eligible by default but receives strong penalty.
5. If every candidate misses required skills, choose least-bad candidate and explain gap.
6. Deterministic tie-breaker remains effectiveCost, candidateId, workItemId.
```

### Assignment Reasons

Examples:

```text
Candidate matches 2/2 required skills.
Candidate matches 1/3 preferred skills.
Candidate has EXPERT proficiency in backend-java.
Candidate missing required skill kubernetes.
Skill data missing for candidate; ranking confidence is low.
```

### Review Payload Additions

Per item:

```text
candidateSkillFit
  suggestedAssigneeId
  requiredCoveragePercent
  preferredCoveragePercent
  matchedRequiredSkills
  missingRequiredSkills
  matchedPreferredSkills
  missingPreferredSkills
  proficiencySummary
  confidence
```

Skill item shape:

```text
skillId
skillCode
skillName
requiredProficiency
candidateProficiency
requirementType
matched
```

Summary additions:

```text
itemsWithSkillRequirements
itemsMissingSkillRequirements
candidatesWithSkillProfiles
candidatesMissingSkillProfiles
requiredSkillMismatchCount
skillRankingConfidence
```

Privacy rule:

```text
Show only skill fit related to selected work item requirements.
Do not expose full user skill profile for unrelated skills.
Do not expose skills for users outside generated candidates.
```

### New Warning Codes

```text
WORK_ITEM_SKILL_DATA_MISSING
CANDIDATE_SKILL_DATA_MISSING
REQUIRED_SKILL_MISSING
PARTIAL_SKILL_MATCH
LOW_CONFIDENCE_SKILL_DATA
```

### Definition Of Done

```text
skills, work_item_skills, and user_skills tables exist and validate.
Skill domain entities, persistence models, repositories, mappers, and read ports exist.
OptimizationProjectModelBuilder loads work item skill requirements and user skills.
OptimizationCandidateAssignee carries skill fit.
GreedyOptimizationRunGenerator uses skill fit in candidate cost.
Assignment reasons explain skill matches and gaps.
Review payload exposes skill fit without leaking unrelated user skills.
Missing skill data produces warnings but does not block generation.
Apply remains snapshot-based and does not recompute skill fit.
Tests cover skill fit, ranking, warnings, privacy, and determinism.
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
PARTIAL_CALENDAR_COVERAGE
PARTIAL_WORKLOAD_COVERAGE
CALENDAR_SOURCE_FAILED
WORKLOAD_SOURCE_FAILED
CAPACITY_RESERVATION_EXCEEDS_AVAILABILITY
CROSS_PROJECT_CAPACITY_CONFLICT
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

Status: implemented.

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

Status: implemented in backend.

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

### Sprint 7 - Capacity Source Metadata And Review Contract

Status: implemented in backend.

```text
1. Define capacity source modes and coverage statuses.
2. Extend optimization summary JSON with capacity source metadata.
3. Expose capacity source, calendar coverage, workload coverage, fallback users, and schedule confidence in GET review payload.
4. Keep fallback weekday provider as current concrete calendar implementation because no authoritative calendar source exists yet.
5. Add aggregated workload summary shape for review payload.
6. Do not expose cross-project project/work item identity.
7. Remove skill-data work from this sprint; skill matching is a later phase.
8. Add tests for summary metadata, review payload, and aggregate-only privacy behavior.
```

### Sprint 8 - Cross-Project Workload From pm_core Plans

Status: implemented in backend.

```text
1. Add read query for active work_item_plans by assignee ids and planning range.
2. Load linked work items to determine assignee, project, done state, and tenant scope.
3. Exclude selected work item ids from workload subtraction.
4. Include same-project outside-scope plans and other-project plans in same tenant.
5. Convert overlapping plans into reserved capacity intervals.
6. Subtract reserved intervals from fallback capacity slots.
7. Track deducted workload millis by assignee and source scope only.
8. Emit PARTIAL_WORKLOAD_COVERAGE or CROSS_PROJECT_CAPACITY_CONFLICT warnings when needed.
9. Add tests for overlap, exclusion, done items, aggregate review output, and deterministic subtraction.
```

### Sprint 9 - Calendar Provider Contract

Status: implemented as fallback provider contract.

```text
1. Define calendar provider contract for future working hours, holidays, leave, and exceptions.
2. Keep fallback weekday provider as implementation until an authoritative calendar source exists.
3. Add calendar coverage metadata and per-assignee fallback tracking.
4. Normalize provider output to UTC capacity slots.
5. Support daily fallback slots now and allow hourly slots when real source exists.
6. Emit MISSING_CALENDAR or PARTIAL_CALENDAR_COVERAGE warnings when needed.
7. Add tests for fallback contract, timezone normalization contract, missing calendar behavior, and deterministic ordering.
```

### Sprint 10 - Integrated Net Capacity Scheduling

Status: partially implemented.

```text
1. Route scheduler through net usable capacity from IResourceCapacityPort.
2. Ensure workload subtraction happens before schedule generation.
3. Add schedule reasons explaining fallback calendar and work_item_plans workload deductions.
4. Persist source fetchedAt and coverage metadata in summary_json.
5. Keep apply snapshot-based and do not recompute capacity during apply.
6. Add end-to-end generate/review tests for fallback calendar plus cross-project workload.
7. Add privacy tests to ensure review payload stays aggregate-only for cross-project workload.
```

Implemented subset:

```text
1. Scheduler receives net usable capacity from IResourceCapacityPort.
2. Workload subtraction happens before schedule generation in FallbackResourceCapacityAdapter.
3. Source fetchedAt and coverage metadata are persisted in summary_json.
4. Apply remains snapshot-based and does not recompute capacity.
5. Tests cover fallback calendar plus workload subtraction behavior.
```

Remaining work:

```text
1. Add richer schedule reasons explaining workload deductions per item.
2. Add end-to-end generate/review tests for cross-project workload metadata.
3. Add privacy tests focused on aggregate-only cross-project review output.
4. Add UI consumption of summary capacity metadata.
```

### Sprint 11 - Skill Data Foundation

Status: planned.

Goal:
1. Store canonical skills, work item skill requirements, and user skill profiles inside `pm_core`.

Scope:
1. Add Flyway migration for `skills`, `work_item_skills`, and `user_skills`.
2. Add enums `SkillRequirementType`, `SkillProficiency`, and `SkillSource`.
3. Add domain entities, persistence models, repositories, mappers, and ports.
4. Add read ports `ISkillReadPort`, `IWorkItemSkillReadPort`, and `IUserSkillReadPort`.
5. Keep all reads and writes tenant-scoped and soft-delete aware.
6. Add minimal management service/command handlers only if needed to seed test data.

Out of scope:
1. No optimizer ranking changes.
2. No UI skill management.
3. No text/label/component-name skill inference.
4. No external skill service integration.

Definition of Done:
1. Migrations compile with JPA validate.
2. Active skill queries work by tenant.
3. Duplicate active `skills.code`, `work_item_skills`, and `user_skills` are prevented.
4. Soft-deleted skill rows are excluded.
5. Tests cover tenant scope, soft delete, uniqueness, and mapper conversion.

Verification:
1. `./mvnw.cmd -Dtest='*Skill*Test' test`
2. `./mvnw.cmd clean compile`

### Sprint 12 - Skill Fit In Optimization Model

Status: implemented in backend.

Goal:
1. Load skill requirements and user skill profiles during optimization model build.
2. Attach deterministic skill fit to each candidate assignee.

Scope:
1. Add `OptimizationSkillRequirement`, `OptimizationCandidateSkill`, and `OptimizationCandidateSkillFit` models.
2. Extend `OptimizationCandidateAssignee` with nullable or neutral `skillFit`.
3. Extend `OptimizationProjectModelBuilder` to load `work_item_skills` for selected items.
4. Extend `OptimizationProjectModelBuilder` to load `user_skills` for generated candidate ids.
5. Calculate skill fit for every work item + candidate pair.
6. Emit `WORK_ITEM_SKILL_DATA_MISSING`, `CANDIDATE_SKILL_DATA_MISSING`, `PARTIAL_SKILL_MATCH`, and `LOW_CONFIDENCE_SKILL_DATA` warnings where applicable.

Out of scope:
1. No cost/ranking changes yet.
2. No review payload changes yet.
3. No hard rejection for missing required skills.

Definition of Done:
1. Model builder returns candidates with skill fit.
2. No skill data produces neutral fit and warning, not generation failure.
3. Full, partial, and missing skill matches are represented explicitly.
4. Skill fit ordering and percentages are deterministic.
5. Tests cover full match, partial match, missing required skill, missing candidate profile, and no work item skill requirements.

Verification:
1. `./mvnw.cmd -Dtest=OptimizationProjectModelBuilderTest test`
2. `./mvnw.cmd -Dtest='*Skill*Test' test`

### Sprint 13 - Skill-Aware Assignment Ranking

Status: implemented in backend.

Goal:
1. Use candidate skill fit to improve assignment suggestions.
2. Explain skill-based ranking decisions in assignment reasons.

Scope:
1. Add skill cost constants to `OptimizationConstants`.
2. Update `GreedyOptimizationRunGenerator.candidateCost` to apply skill bonuses and penalties.
3. Keep required skill missing as strong penalty, not hard rejection.
4. Add assignment reasons for required matches, preferred matches, proficiency, missing required skills, and low confidence skill data.
5. Preserve capacity, overload, reassignment, and `MINIMAL_REASSIGNMENT` behavior.
6. Add `REQUIRED_SKILL_MISSING` warning when selected candidate lacks required skill.

Out of scope:
1. No CP-SAT/solver-based skill assignment.
2. No team-level skill balancing.
3. No skill learning from past work.

Definition of Done:
1. Skill match can change chosen assignee when capacity and role costs are otherwise close.
2. Overload and no-reassignment constraints still behave correctly.
3. Assignment reasons include skill match/gap details.
4. Deterministic tie-breaker remains effectiveCost, candidateId, workItemId.
5. Tests cover skill match beats project member fallback, missing required skill penalty, capacity beats skill bonus, and minimal reassignment behavior.

Verification:
1. `./mvnw.cmd -Dtest=GreedyOptimizationRunGeneratorTest test`
2. `./mvnw.cmd -Dtest='*Optimization*Test' test`

### Sprint 14 - Skill Review Contract

Status: implemented in backend.

Goal:
1. Expose skill fit and skill warnings in optimization review payload.
2. Keep unrelated user skills private.

Scope:
1. Extend `OptimizationRunSummary` with skill ranking summary fields.
2. Persist skill fit details in `assignment_reasons_json` and/or structured summary JSON.
3. Extend `OptimizationRunItemView` with selected candidate skill fit if needed by UI.
4. Expose matched/missing required and preferred skills for selected work item requirements only.
5. Add privacy tests to ensure unrelated user skills are not exposed.

Out of scope:
1. No UI implementation in `pm_core`.
2. No full user skill profile endpoint in optimization review.
3. No external skill visibility rules beyond tenant scoping and candidate-only exposure.

Definition of Done:
1. GET optimization run returns skill fit summary for generated suggestions.
2. Review payload explains why skill fit affected assignment.
3. Review payload never exposes unrelated user skills or non-candidate user skills.
4. Apply still uses persisted suggestion snapshot and does not recompute skill fit.
5. Tests cover payload shape, warnings, summary metrics, and privacy.

Verification:
1. `./mvnw.cmd -Dtest=GetOptimizationRunQueryHandlerTest test`
2. `./mvnw.cmd -Dtest='*Optimization*Test,*Skill*Test' test`

### Sprint 15 - Skill Management APIs

Status: implemented in backend.

Goal:
1. Provide backend APIs to manage skill catalog, work item skill requirements, and user skill profiles owned by `pm_core`.

Scope:
1. Add skill catalog create/update/list/archive APIs.
2. Add work item skill requirement list/replace APIs under project work item routes.
3. Add user skill profile list/replace APIs for tenant users.
4. Validate skill ids are active and tenant-scoped.
5. Validate proficiency and requirement type transitions.
6. Keep API responses in `GeneralResponse<?>` through `ResponseUtils`.

Proposed APIs:

```http
GET /api/v1/skills
POST /api/v1/skills
PATCH /api/v1/skills/{skillId}
DELETE /api/v1/skills/{skillId}

GET /api/v1/projects/{projectId}/work-items/{workItemId}/skills
PUT /api/v1/projects/{projectId}/work-items/{workItemId}/skills

GET /api/v1/users/{userId}/skills
PUT /api/v1/users/{userId}/skills
```

Out of scope:
1. No frontend UI in this backend sprint.
2. No automatic skill inference.
3. No external HR/account skill synchronization.
4. No historical expertise calculation.

Definition of Done:
1. Skill catalog APIs are tenant-scoped and soft-delete aware.
2. Work item skill requirement APIs validate project/work item ownership.
3. User skill profile APIs validate tenant scope and active skills.
4. Replace operations are transactional.
5. Tests cover validation, tenant isolation, soft delete, and duplicate handling.

Verification:
1. `./mvnw.cmd -Dtest='*Skill*ControllerTest,*Skill*CommandHandlerTest,*Skill*QueryHandlerTest' test`
2. `./mvnw.cmd clean compile`

### Future Sprint Template

Use this format when adding new sprint sections:

```text
### Sprint N - <Name>

Status: planned | in progress | implemented in backend | implemented in UI | partially implemented | deferred.

Goal:
1. <business or technical outcome>

Scope:
1. <concrete code/data/API work>

Out of scope:
1. <explicit exclusions>

Definition of Done:
1. <observable completion criteria>

Verification:
1. <focused test/compile command>
```

### Next Backlog Candidates

```text
1. UI integration for Optimize selected and review/apply workflow in serp_web.
2. Richer review payload fields for per-item capacity/workload reasons.
3. Privacy regression tests for cross-project workload aggregation.
4. Real calendar provider integration when authoritative source exists.
5. Skill data foundation and skill-aware assignment ranking in pm_core.
6. Deadline-safe and fastest-delivery modes.
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
Calendar provider contract produces deterministic fallback capacity slots
Calendar coverage metadata is emitted when no authoritative source exists
Workload subtraction excludes selected work items
Workload subtraction includes same-project outside-scope plans
Workload subtraction includes other-project active plans in same tenant
Workload subtraction ignores done or deleted planned work
Capacity resolver falls back per assignee when calendar source is missing
Capacity resolver reports partial calendar/workload coverage
Scheduler uses net usable capacity after workload subtraction
Cross-project workload review output is aggregate-only
Skill read ports return tenant-scoped active skill data
Skill fit full required match
Skill fit partial required match
Skill fit preferred-only match
Candidate missing user skill profile
Work item missing skill requirements
Required skill penalty beats component lead bonus
Required skill match beats project member fallback
Minimal reassignment keeps current assignee when skill delta is small
Overload penalty still beats skill bonus when capacity is unavailable
Skill fit deterministic ordering with equal scores
```

Handler/query tests:

```text
Generate run persists run/items/warnings
Get run returns review payload
Patch item decision persists override
Apply assignment updates assignee only for accepted items
Apply schedule upserts work_item_plans only for accepted items
Partial apply marks skipped items
Get run returns capacity source and coverage metadata
Get run returns only aggregated cross-project workload impact
Generate run persists calendar/workload fetchedAt metadata
Generate run persists skill fit reasons and warnings
Get run returns skill fit summary without unrelated user skills
Skill management APIs enforce tenant scope and soft delete
```

Validation commands:

```bash
./mvnw.cmd -Dtest='*Optimization*Test' test
./mvnw.cmd -Dtest='*Skill*Test' test
./mvnw.cmd clean compile
```

## Risks And Open Notes

- Default 8h/day capacity is current fallback implementation and ignores holidays, PTO, and part-time schedules until an authoritative calendar source exists.
- Candidate sources now include component lead and assignable project members, but no skill/history matching exists yet.
- Project member source comes from `ProjectRoleActorEntity` USER subjects and is gated by `ASSIGNABLE_USER`.
- Dependency behavior migration must preserve existing `Blocks`, `Clones`, and `Relates` rows.
- Selected items may omit external blockers; model builder should warn when links point outside selected scope.
- Schedule quality depends on calendar and workload source coverage; fallback capacity remains low confidence.
- Initial cross-project workload integration uses active `work_item_plans` in `pm_core`; external/shared planning store integration can follow when workload spans services.
- Calendar integration requires a future authoritative source for working hours, holidays, leave, and exceptions.
- Cross-project workload data must stay aggregate-only in optimization review payload.
- Skill data is planned as pm_core-owned `skills`, `work_item_skills`, and `user_skills`; do not implement text/label/component-name skill heuristics.
- Skill profile data may become stale; optimization generation must snapshot skill fit and apply must not recompute it.
- User skill profile visibility must stay limited to selected work item requirements and generated candidates in optimization review.
- Workload data may be sensitive; apply/review permission model must be revisited after MVP.
- Whole-project optimization remains out of MVP because results are slower, noisier, and harder to review.
