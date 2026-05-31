# Work Item Plan Allocations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist applied schedule allocation chunks and use them as the source of planned workload capacity deduction.

**Architecture:** Add a child `work_item_plan_allocations` persistence path behind a domain port. The apply handler writes allocations after upserting the active plan. The capacity adapter reads allocations for active plans and only falls back to summary plan overlap when no allocation rows exist.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, Flyway, JUnit 5, Mockito.

---

### Task 1: Allocation Persistence Model

**Files:**
- Modify: `pm_core/src/main/resources/db/migration/V26__add_resource_calendar_slots_and_schedule_chunks.sql`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/WorkItemPlanAllocationEntity.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/port/IWorkItemPlanAllocationPort.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/WorkItemPlanAllocationModel.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/WorkItemPlanAllocationMapper.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IWorkItemPlanAllocationRepository.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/WorkItemPlanAllocationAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationFoundationMapperTest.java`

- [ ] Write a failing mapper test that round-trips allocation fields.
- [ ] Add migration table and JPA/domain/port/adapter classes.
- [ ] Run `mvn -Dtest=OptimizationFoundationMapperTest test`.

### Task 2: Apply Schedule Writes Allocations

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/apply/ApplyOptimizationRunCommandHandlerTest.java`

- [ ] Write a failing test that accepted schedule apply saves allocation chunks.
- [ ] Parse `allocationChunksJson` for non-overridden schedules.
- [ ] After `upsertActivePlan`, replace allocations for the saved plan id.
- [ ] Run `mvn -Dtest=ApplyOptimizationRunCommandHandlerTest test`.

### Task 3: Capacity Deducts Allocation Chunks

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/FallbackResourceCapacityAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/optimization/adapter/FallbackResourceCapacityAdapterTest.java`

- [ ] Write a failing test where a multi-day summary plan has only a short allocation and capacity deducts only the allocation.
- [ ] Query allocations for active workload plans.
- [ ] Use allocation overlaps before summary plan overlap.
- [ ] Run `mvn -Dtest=FallbackResourceCapacityAdapterTest test`.

### Task 4: Work Item Views Expose Allocations

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/WorkItemDetailView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/get/GetWorkItemByIdQueryHandler.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/WorkItemTimelineItemView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/timeline/ListWorkItemTimelineQueryHandler.java`
- Test: nearest work item detail/timeline tests.

- [ ] Add allocation view DTO fields.
- [ ] Load allocations by active plan ids.
- [ ] Attach allocations to detail/timeline responses.
- [ ] Run related work item query tests.

### Task 5: Verification

- [ ] Run focused optimization tests.
- [ ] Run `mvn test` from `pm_core`.
