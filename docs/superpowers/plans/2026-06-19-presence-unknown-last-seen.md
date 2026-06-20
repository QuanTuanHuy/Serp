# Presence Unknown Last-Seen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Missing presence records should not imply the user was seen just now.

**Architecture:** Keep the backend presence API as the source of truth for `lastSeenText`. Represent an absent presence record as offline with no last-seen timestamp, so DTO formatting returns `Unknown` instead of `Just now`.

**Tech Stack:** Java 21, Spring Boot, JUnit 5, Mockito, Maven.

---

### Task 1: Preserve Unknown Last-Seen For Missing Presence

**Files:**
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/domain/entity/UserPresenceEntity.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/response/UserPresenceResponse.java`
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/PresenceServiceTest.java`

- [ ] **Step 1: Write the failing regression test**

Add a test that stubs the presence cache as empty, calls `getPresenceBatch`, and verifies the fallback presence has no last-seen timestamp and formats as `Unknown`.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `mvn -Dtest=PresenceServiceTest test`

Expected: FAIL because the fallback currently sets `lastSeenAt` to the current time.

- [ ] **Step 3: Implement the minimal production change**

Add a factory for an unknown offline presence record and use it only for missing cache entries. Make `formatLastSeen` handle null timestamps.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run: `mvn -Dtest=PresenceServiceTest test`

Expected: PASS.

- [ ] **Step 5: Run broader discuss_service verification**

Run: `mvn -q -DskipTests compile` and `mvn test`.

Expected: both commands exit 0.
