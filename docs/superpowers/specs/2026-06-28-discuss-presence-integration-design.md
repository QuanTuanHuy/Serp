# Design Spec: Discuss Module Presence Integration

- **Author**: Antigravity & QuanTuanHuy
- **Date**: 2026-06-28
- **Status**: Approved (Brainstorming Complete)

---

## 1. Goal & Context

This document outlines the design to integrate the **Presence** functionality from the backend `discuss_service` into the frontend `serp_web` UI.

The goal is to enable real-time user presence visibility and status updates across the `discuss` module:
- Displaying other users' presence status (Online, Do Not Disturb, Offline) and custom status messages in the sidebar, chat header, and channel member list.
- Allowing the current user to manually update their presence status and set a custom status message.

---

## 2. Architecture & Approach

We will follow **Approach 1 (RTK Query Cache Invalidation & Refetch)**:
- **Data Fetching**: Use existing RTK Query hooks in [presence.api.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/api/presence.api.ts) (`useGetMyPresenceQuery`, `useGetChannelPresenceQuery`).
- **Real-time Updates**: The existing WebSocket handler in [useDiscussWebSocket.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/hooks/useDiscussWebSocket.ts) listens to `USER_ONLINE`, `USER_OFFLINE`, and `USER_PRESENCE_CHANGED` events. When these events occur, it dispatches cache invalidation for the tag `Presence` and `{ type: 'Presence', id: 'USER-userId' }`.
- **Automatic Refetches**: RTK Query automatically refetches the active queries (e.g., current channel's presence or current user's presence) once their tags are invalidated.

### Status Mappings
Backend `UserStatus` (defined in [UserStatus.java](file:///d:/User2/open_source/serp/discuss_service/src/main/java/serp/project/discuss_service/core/domain/enums/UserStatus.java)) will map to frontend `OnlineStatus` (defined in [OnlineStatusIndicator.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/OnlineStatusIndicator.tsx)):
- `ONLINE` -> `online` (renders green dot with ping pulse)
- `BUSY` -> `busy` (renders red dot representing "Do Not Disturb")
- `OFFLINE` -> `offline` (renders gray dot)

---

## 3. UI/UX Changes & Components

### 3.1. User Profile Card & Status Switcher (Bottom of Sidebar)
- **Target File**: [ChannelList.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelList.tsx)
- **Changes**:
  - Insert a `UserProfileCard` component at the bottom of the sidebar (just above the **New Channel** button).
  - It displays the current user's avatar, name, and current status message.
  - Clicking this card opens a Popover dropdown:
    - **Quick Status Selector**: Buttons to switch between Online (`ONLINE`), Busy/Do Not Disturb (`BUSY`), and Offline (`OFFLINE`).
    - **Status Message Input**: A text input allowing up to 255 characters for a custom message.
    - **Actions**: "Clear Status" button (when a custom message is present) and "Save" button.
  - Invokes `useUpdateMyPresenceMutation()` to push changes to backend `/users/me/presence`.

### 3.2. Direct Message Presence (Channel List Items)
- **Target File**: [ChannelItem.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelItem.tsx)
- **Changes**:
  - Replace the static green dot rendering logic for direct message (DM) channels.
  - Instead of relying solely on `otherDirectUser?.isOnline`, use `<OnlineStatusIndicator>` with the mapped status (`online`, `busy`, or `offline`).
  - Add hover tooltips displaying the other user's status message (e.g. `"Nguyen Van A - Busy (In a meeting)"`).

### 3.3. Chat Header Status (Chat Window)
- **Target File**: [ChatWindow.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChatWindow.tsx)
- **Changes**:
  - For `DIRECT` channels, render `<OnlineStatusIndicator>` next to the user status text in the header.
  - Display the custom status message next to the status (e.g., `🔴 Busy - WFH`).

### 3.4. Channel Members Panel
- **Target File**: [ChannelMembersPanel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/discuss/components/ChannelMembersPanel.tsx)
- **Changes**:
  - Convert `presenceResponse.data.statusGroups` into a `Map<string, UserPresenceResponse>` for efficient lookups by `userId`.
  - For each member in the list, replace the static green circle icon with `<OnlineStatusIndicator>` matching their exact status (`online`, `busy`, `offline`).
  - If a member has a custom `statusMessage`, render it directly under their name/details (e.g. `💬 In a meeting`).

---

## 4. Verification Plan

### Manual Verification
1. Run backend services and start `serp_web` (`npm run dev`).
2. Open two browser sessions (different accounts) or use Postman/WS client to simulate multiple users.
3. Test manual status switching (Online -> Busy -> Offline) and custom status message updating on User A.
4. Verify User B's sidebar immediately shows User A's updated presence dot and status message.
5. Verify the Chat Header and Channel Members Panel in User B's window updates correctly in real-time.
