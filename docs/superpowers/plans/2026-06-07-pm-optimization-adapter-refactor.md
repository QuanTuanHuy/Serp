# PM Optimization Adapter Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor PM Core optimization calendar and capacity adapters so persistence remains in store adapters and business logic moves into domain services.

**Architecture:** `IResourceCalendarService` and `IResourceCapacityService` expose domain behavior, with Spring implementations in `domain.optimization.service.impl`. New store adapters expose raw real calendar slots and workload data through lower-level ports, with no fallback or capacity orchestration in infrastructure.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, JUnit 5, Mockito.

---

### Task 1: Calendar Domain Service

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/port/IResourceCalendarSlotReadPort.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IResourceCalendarService.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/ResourceCalendarService.java`
- Move/Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/DatabaseResourceCalendarAdapter.java` to `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotReadAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/ResourceCalendarServiceTest.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotReadAdapterTest.java`

- [ ] Write `ResourceCalendarServiceTest` for full real-slot coverage.
- [ ] Write `ResourceCalendarServiceTest` for partial fallback coverage.
- [ ] Add `IResourceCalendarSlotReadPort.findOverlappingSlots(Long tenantId, List<Long> userIds, Long planningStart, Long planningEnd)`.
- [ ] Implement `ResourceCalendarService implements IResourceCalendarService` by moving fallback, merge, coverage, ordering, and warning logic out of the old adapters.
- [ ] Implement `ResourceCalendarSlotReadAdapter implements IResourceCalendarSlotReadPort` with repository delegation and model-to-`ResourceCapacitySlot` mapping.
- [ ] Run `mvn -Dtest=ResourceCalendarServiceTest,ResourceCalendarSlotReadAdapterTest test`.

### Task 2: Capacity Domain Service

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/ResourceWorkloadPlan.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/ResourceWorkloadItem.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/model/ResourceWorkloadAllocation.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/port/IResourceWorkloadReadPort.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/IResourceCapacityService.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/optimization/service/impl/ResourceCapacityService.java`
- Move/Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/FallbackResourceCapacityAdapter.java` to `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceWorkloadReadAdapter.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/optimization/service/ResourceCapacityServiceTest.java`

- [ ] Write `ResourceCapacityServiceTest` for no workload returning calendar slots unchanged.
- [ ] Write `ResourceCapacityServiceTest` for planned workload deduction.
- [ ] Write `ResourceCapacityServiceTest` for unplanned same-project and cross-project workload buckets.
- [ ] Add `IResourceWorkloadReadPort` methods for active workload plans, unplanned workload items, plan work items, and plan allocations.
- [ ] Implement `ResourceWorkloadReadAdapter` with repository calls and persistence-to-domain read model mapping.
- [ ] Implement `ResourceCapacityService implements IResourceCapacityService` by moving capacity source mode, workload deduction, overlap, bucket, and warning logic into domain.
- [ ] Run `mvn -Dtest=ResourceCapacityServiceTest test`.

### Task 3: Remove Old Infrastructure Optimization Adapters

**Files:**
- Delete: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/DatabaseResourceCalendarAdapter.java`
- Delete: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/FallbackResourceCalendarAdapter.java`
- Delete: `pm_core/src/main/java/serp/project/pmcore/infrastructure/optimization/adapter/FallbackResourceCapacityAdapter.java`
- Modify tests under `pm_core/src/test/java/serp/project/pmcore/infrastructure/optimization/adapter`

- [ ] Delete old adapter classes after equivalent domain/store tests pass.
- [ ] Delete or move old infrastructure adapter tests so package names match new ownership.
- [ ] Run `rg "infrastructure.optimization.adapter|FallbackResourceCalendarAdapter|FallbackResourceCapacityAdapter|DatabaseResourceCalendarAdapter" pm_core/src/main/java pm_core/src/test/java` and remove stale imports.

### Task 4: Verification

**Files:**
- All changed optimization domain and store files.

- [ ] Run `mvn -Dtest=ResourceCalendarServiceTest,ResourceCalendarSlotReadAdapterTest,ResourceCapacityServiceTest,OptimizationProjectModelBuilderTest,GreedyOptimizationRunGeneratorTest test`.
- [ ] Run `mvn clean compile`.
- [ ] Inspect `git diff --stat` and `git diff --check`.
