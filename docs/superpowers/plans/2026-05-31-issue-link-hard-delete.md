# Issue Link Hard Delete Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert issue links from app-level soft delete to tenant-scoped hard delete while leaving existing DB `deleted_at` column and partial indexes intact.

**Architecture:** Keep Clean Architecture boundaries. Domain service invokes `IIssueLinkPort.delete(...)`; infrastructure adapter performs physical delete through Spring Data repository. Reads remain tenant-aware and existing native detail query keeps `il.deleted_at IS NULL` for compatibility with historical soft-deleted rows.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, JUnit 5, Mockito, Maven.

---

### Task 1: Domain And Mapper Soft-Delete Removal

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/entity/IssueLinkEntity.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/IssueLinkModel.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/mapper/IssueLinkMapper.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/service/impl/IssueLinkService.java`

- [ ] **Step 1: Remove soft-delete fields from domain mapping**

Remove `deletedAt` from `IssueLinkEntity`, remove `@SQLRestriction("deleted_at IS NULL")` from `IssueLinkModel`, remove `.deletedAt(...)` mapping calls from `IssueLinkMapper`, and remove `draft.setDeletedAt(null)` from create flow.

- [ ] **Step 2: Run compile check**

Run: `mvn -DskipTests compile`
Expected: compile fails only for remaining `softDelete` contract references, if any.

### Task 2: Hard Delete Port And Adapter

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/port/IIssueLinkPort.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/service/IIssueLinkService.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/domain/issuelink/service/impl/IssueLinkService.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/IssueLinkAdapter.java`
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IIssueLinkRepository.java`

- [ ] **Step 1: Change service contract to hard delete**

Replace `softDelete(IssueLinkEntity issueLink, Long userId, Long deletedAt)` with `delete(IssueLinkEntity issueLink)` and route to `issueLinkPort.delete(issueLink.getId(), issueLink.getTenantId())`.

- [ ] **Step 2: Add tenant-scoped repository delete**

Add `@Modifying(flushAutomatically = true, clearAutomatically = true)` JPQL delete by `id` and `tenantId` in `IIssueLinkRepository`.

- [ ] **Step 3: Implement adapter delete**

Add `void delete(Long id, Long tenantId)` to `IssueLinkAdapter`, delegating to repository delete.

### Task 3: Tests And Verification

**Files:**
- Modify: `pm_core/src/test/java/serp/project/pmcore/domain/issuelink/service/impl/IssueLinkServiceTest.java`
- Create if absent: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/IssueLinkAdapterTest.java`

- [ ] **Step 1: Update service tests**

Assert delete delegates to port hard delete and no longer mutates `deletedAt`.

- [ ] **Step 2: Add adapter tests**

Assert adapter delegates to `deleteByIdAndTenantId(...)`; assert repository method has `@Modifying(flushAutomatically = true, clearAutomatically = true)`.

- [ ] **Step 3: Run focused tests**

Run: `mvn "-Dtest=IssueLinkServiceTest,IssueLinkAdapterTest" test`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Run compile gate**

Run: `mvn clean -DskipTests compile`
Expected: `BUILD SUCCESS`.

### Self-Review

- Spec coverage: app-level hard delete only; DB column/index kept.
- Placeholder scan: no placeholder implementation steps left.
- Type consistency: port/service/adapter use `delete(Long id, Long tenantId)` for tenant-scoped physical delete.
