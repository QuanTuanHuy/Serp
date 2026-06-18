# Discuss Presence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make discuss presence update correctly through WebSocket session changes and manual status updates.

**Architecture:** Backend keeps Redis presence as the source of truth, then fans out `USER_PRESENCE_CHANGED` events to online users who share at least one channel with the changed user. Frontend keeps RTK Query presence data as the rendered source and uses WebSocket events to invalidate the right cache entries and update active UI callbacks.

**Tech Stack:** Spring Boot 3.5, JUnit 5, Mockito, Redis-backed cache service, Kafka event handlers, Next.js 15, React 19, RTK Query, STOMP.

---

### Task 1: Backend Presence Fan-Out

**Files:**
- Create: `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/DeliveryServiceTest.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DeliveryService.java`

- [ ] **Step 1: Write the failing test**

Add a test where user `100` shares channel `1000` with online user `200` and offline user `300`. Call `notifyPresenceChange(100L)` and assert `IWebSocketHubPort.sendToUsers(Set.of(200L), event)` is called with `USER_PRESENCE_CHANGED`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=DeliveryServiceTest#testNotifyPresenceChange_FansOutToOnlineSharedChannelMembers test`
Expected: FAIL because `notifyPresenceChange` currently has a TODO and never sends.

- [ ] **Step 3: Implement minimal fan-out**

In `DeliveryService.notifyPresenceChange`, build the payload as today, get channels from `memberService.getUserChannels(userId)`, collect recipients from each channel member list, exclude the changed user, filter through `presenceService.getOnlineUsers`, and call `sendToUsers`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=DeliveryServiceTest test`
Expected: PASS.

### Task 2: Backend Manual Status Event

**Files:**
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/PresenceServiceTest.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/PresenceService.java`

- [ ] **Step 1: Write the failing test**

Add tests that `updateUserStatus(..., ONLINE, ...)` publishes `publishUserOnline(userId)` and `updateUserStatus(..., OFFLINE, ...)` publishes `publishUserOffline(userId)`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=PresenceServiceTest test`
Expected: FAIL because status update only writes cache.

- [ ] **Step 3: Publish after cache update**

After `cacheService.setUserPresence(presence)`, publish online/offline based on `presence.isOnline()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=PresenceServiceTest test`
Expected: PASS.

### Task 3: Frontend Presence Consumer

**Files:**
- Modify: `serp_web/src/modules/discuss/context/WebSocketContext.tsx`
- Modify: `serp_web/src/modules/discuss/hooks/useDiscussWebSocket.ts`
- Modify: `serp_web/src/modules/discuss/components/ChannelItem.tsx`
- Modify: `serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Expose status callback**

Add `setOnUserStatusUpdate` to `WebSocketAPI` and the hook API object.

- [ ] **Step 2: Narrow invalidation**

On `USER_PRESENCE_CHANGED`, invalidate `Presence` globally plus `{ type: 'Presence', id: USER-${userId} }`; keep channel presence invalidation broad because the event does not include channel IDs.

- [ ] **Step 3: Fix direct-channel online logic**

In `ChannelItem`, inspect `presenceData.data.statusGroups`, find any member other than the current user when possible, and read `isOnline` instead of `onlineCount > 1`.

### Task 4: Verification

**Files:**
- No code changes.

- [ ] **Step 1: Backend targeted tests**

Run: `mvn -Dtest=DeliveryServiceTest,PresenceServiceTest test`
Expected: PASS.

- [ ] **Step 2: Backend compile**

Run: `mvn -q -DskipTests compile`
Expected: PASS.

- [ ] **Step 3: Frontend checks**

Run from `serp_web`: `npm run type-check`
Expected: PASS.

Run from `serp_web`: `npm run lint`
Expected: PASS or report existing unrelated lint failures.
