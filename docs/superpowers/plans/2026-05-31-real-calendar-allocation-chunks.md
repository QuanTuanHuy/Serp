# Real Calendar Allocation Chunks Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add database-backed calendar capacity and expose split schedule allocation chunks in optimization review results.

**Architecture:** `DatabaseResourceCalendarAdapter` becomes the primary `IResourceCalendarPort` and reads `resource_calendar_slots`, using the existing fallback adapter only for users without real slots. `GreedySchedulingPolicy` returns chunked allocations inside `OptimizationScheduleSuggestion`, and generated run items persist those chunks as JSON for review output.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA repositories, Flyway migrations, JUnit 5, Mockito.

---

### Task 1: Real Calendar Adapter

**Files:**
- Create: `pm_core/src/main/resources/db/migration/V26__add_resource_calendar_slots_and_schedule_chunks.sql`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarSlotModel.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarSlotRepository.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/DatabaseResourceCalendarAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/optimization/adapter/DatabaseResourceCalendarAdapterTest.java`

- [ ] Write a failing test that real slots are returned as `FULL` coverage with no fallback users.
- [ ] Write a failing test that users missing real slots receive fallback slots and `PARTIAL` coverage.
- [ ] Add Flyway table and JPA model/repository.
- [ ] Implement the primary database adapter.
- [ ] Run `mvn -Dtest=DatabaseResourceCalendarAdapterTest test`.

### Task 2: Schedule Allocation Chunks

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleAllocation.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/OptimizationScheduleSuggestion.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/schedule/GreedySchedulingPolicy.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/impl/GreedyOptimizationRunGeneratorTest.java`

- [ ] Write a failing test that a single task split over two slots returns two allocation chunks.
- [ ] Add the allocation record and include chunks in schedule suggestions.
- [ ] Update scheduling window allocation to record chunks.
- [ ] Run `mvn -Dtest=GreedyOptimizationRunGeneratorTest test`.

### Task 3: Persist And Return Chunks

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/entity/OptimizationRunItemEntity.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/OptimizationRunItemModel.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/OptimizationRunItemMapper.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandler.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationScheduleAllocationView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunItemView.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssembler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/query/get/OptimizationRunReviewAssemblerTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/optimization/command/generate/GenerateOptimizationRunCommandHandlerTest.java`

- [ ] Write failing tests for JSON persistence and review deserialization.
- [ ] Add `allocation_chunks_json` to run item entity/model/mapper.
- [ ] Serialize schedule allocation chunks during run generation.
- [ ] Deserialize chunks into review item views.
- [ ] Run focused application tests.

### Task 4: Verification

**Files:**
- All changed optimization files.

- [ ] Run focused optimization test set.
- [ ] Run `mvn clean -Dtest=GreedyOptimizationRunGeneratorTest,OptimizationProjectModelBuilderTest,FallbackResourceCalendarAdapterTest,FallbackResourceCapacityAdapterTest,DatabaseResourceCalendarAdapterTest,OptimizationRunReviewAssemblerTest,GenerateOptimizationRunCommandHandlerTest test`.
- [ ] Inspect git diff for unrelated changes.
