# Design Spec: Discuss Unread Counts and Read Receipts

- **Author**: Codex & QuanTuanHuy
- **Date**: 2026-06-30
- **Status**: Approved (Brainstorming Complete)

---

## 1. Goal & Context

The `discuss_service` backend and `serp_web` discuss UI already contain partial support for unread counts and read state:

- `channel_members` stores `last_read_msg_id` and `unread_count`.
- `messages` has a `read_by` database column from the original Flyway migration.
- `MessageEntity` has a `readBy` field, `readCount`, and `markReadBy`.
- The frontend `Channel` type has `unreadCount`, and the sidebar already renders unread badges.
- The frontend `Message` type has `readCount` and `isReadByMe`.
- WebSocket event type `MESSAGE_READ` already exists.

The missing pieces are persistence mapping for `messages.read_by`, a complete read event pipeline, automatic mark-read behavior in the chat window, and a UI surface for showing which users have read the current user's messages.

The goal is to implement:

- Correct unread message counts per channel for the current user.
- Automatic clearing of unread counts when the user reads the latest visible message.
- Read receipts for sent messages, showing which users have read them.
- Real-time updates when another channel member reads a message.

---

## 2. Chosen Approach

Use the existing `messages.read_by` column for read receipts and the existing `channel_members` unread fields for unread counts.

This avoids adding a new read-receipt table while still persisting the data needed by the requested feature. The trade-off is that the system will not store per-user `read_at` timestamps for every message. That is acceptable for this scope because the UI only needs to show whether a user has read a message, not exactly when.

Read receipts will be displayed only on messages sent by the current user. This keeps the chat UI quiet and follows common messaging-product behavior.

---

## 3. Backend Design

### 3.1 Persistence Mapping

`MessageModel` will map the existing `messages.read_by` column as a PostgreSQL `BIGINT[]`.

`MessageMapper` will include `readBy` in both directions:

- `MessageModel.readBy` -> `MessageEntity.readBy`
- `MessageEntity.readBy` -> `MessageModel.readBy`

No new migration is required because `V2__create_messages_table.sql` already creates `read_by BIGINT[] DEFAULT '{}'`.

### 3.2 Read Receipt Response Shape

`MessageResponse` will expose:

- `readCount`: existing count derived from `MessageEntity.getReadCount()`.
- `readBy`: raw list of user ids as `List<Long>`.
- `readByUsers`: enriched user info for users in `readBy`.
- `isReadByMe`: whether the current user is in `readBy`.

`MessageUseCase.enrichMessageResponseList` will batch-load sender and read-receipt users together or in two focused batches. The final response should avoid per-message account-service calls.

### 3.3 Mark As Read Flow

`MessageUseCase.markAsRead(channelId, userId, messageId)` will:

1. Confirm the user is a member of the channel.
2. Load the target message and confirm it belongs to the channel.
3. Update the user's `ChannelMemberEntity`:
   - `lastReadMsgId = messageId`
   - `unreadCount = 0`
4. Add `userId` to the target message's `readBy` list if absent.
5. Save the updated message.
6. Publish an internal `MessageReadInternalEvent`.

Repeated mark-read calls for the same user/message must be idempotent.

### 3.4 Real-Time Event Pipeline

Add a post-commit event path that mirrors the existing message and reaction patterns:

- Internal event: `MessageReadInternalEvent`
- Publisher method: `publishMessageRead(channelId, messageId, userId, readBy, readCount)`
- Kafka payload topic: reuse `discuss.message.events`
- WebSocket event type: existing `MESSAGE_READ`
- Handler: `MessageReadHandler`
- Delivery method: `notifyMessageRead(channelId, messageId, userId, readBy, readCount)`

The WebSocket payload will include:

```json
{
  "messageId": 123,
  "channelId": 456,
  "userId": 789,
  "readBy": [789],
  "readCount": 1
}
```

The delivery service will fan out this event to online channel members.

### 3.5 Cache Behavior

When a message is marked as read:

- Invalidate or refresh the cached message by `messageId`.
- Invalidate channel message page cache for the channel, or update the cached message if the cache service has a safe helper for that.
- Reset the current user's unread cache for the channel via the existing unread cache path.

The implementation should prefer correctness over overly clever cache mutation.

---

## 4. Frontend Design

### 4.1 Types and Transformers

Extend frontend message types to include:

- `readBy: string[]`
- `readByUsers?: SenderInfo[]`

`transformMessage` will convert backend `readBy` user ids to strings and map `readByUsers` through the existing sender/user transformer shape.

### 4.2 Automatic Mark As Read

`ChatWindow` will mark the latest message as read when:

- A channel is opened and messages are loaded.
- The user is near the bottom of the message list and new messages arrive.
- The user scrolls back to the bottom after unread messages accumulated locally.

The preferred path is WebSocket `wsApi.markAsRead(latestMessage.id)`. If the socket is unavailable, use the existing RTK Query `markAsRead` mutation as a fallback.

The component must avoid noisy repeated calls by remembering the last message id it successfully attempted to mark as read for the active channel.

### 4.3 Sidebar Unread Counts

The sidebar already renders unread badges from `channel.unreadCount`.

Updates required:

- When the current active channel is read, update cached channel list entries and the single-channel cache to set `unreadCount = 0`.
- When `MESSAGE_NEW` arrives for a non-active channel and the message is not from the current user, increment the cached `unreadCount`.
- When `MESSAGE_READ` arrives from the current user, clear unread count for that channel.

### 4.4 Message Read Receipt UI

`MessageItem` will show read receipts only for own messages.

Display behavior:

- No read users: keep the existing check icon or show no read-receipt text.
- One read user: show `Seen by {name}`.
- Multiple read users: show `Seen by {firstName} +{n}`.
- Add `title` text listing the names so users can inspect the full list without a heavier popover.

Read receipt text should be small, visually secondary, and kept inside the message metadata row so it does not compete with message content.

The current sender should be excluded from the visible read receipt list if the backend includes them.

### 4.5 Real-Time Message Cache Updates

When `MESSAGE_READ` is received:

- Update cached `getMessages` entries for that channel:
  - add `userId` to `message.readBy` if absent,
  - update `message.readCount`,
  - keep existing `message.readByUsers` entries when present; missing display names will be filled by the next enriched response.
- Update `ChatWindow` local `allMessages` through a registered callback or by syncing from RTK Query cache.

The event payload only needs ids and counts for real-time correctness. Display names come from enriched message responses after initial load or refetch. If the current cache does not have a user's display name yet, the UI should show a generic fallback until the next enriched response arrives.

---

## 5. Error Handling and Edge Cases

- If `messageId` does not belong to `channelId`, return `MESSAGE_NOT_FOUND` or equivalent existing app error.
- If the user is not a channel member, return `NOT_CHANNEL_MEMBER`.
- If the message is already marked read by the same user, do not duplicate the user id.
- If no messages exist in a channel, do not mark read.
- If the latest message was sent by the current user, mark-read may be skipped unless needed to clear stale channel unread state.
- If WebSocket mark-read fails silently, REST fallback on the next near-bottom/read effect should recover the unread count.

---

## 6. Verification Plan

### Backend

Run focused tests from `discuss_service/`:

```bash
./mvnw -Dtest=MessageMapperTest test
./mvnw -Dtest=MessageUseCaseTest test
./mvnw -q -DskipTests compile
```

Add or update tests for:

- `MessageMapper` mapping `readBy`.
- `MessageUseCase.markAsRead` validating channel membership and target message channel.
- `MessageUseCase.markAsRead` updating member unread state and message `readBy`.
- `MESSAGE_READ` publisher/handler behavior if existing test seams make it practical.

### Frontend

Run from `serp_web/`:

```bash
npm run lint
npm run type-check
```

Manual checks:

1. User A sends a message in a group channel.
2. User B opens the channel and reaches the bottom.
3. User A sees the message update to `Seen by User B`.
4. User B's sidebar unread count for the channel clears.
5. A new message in a different channel increments that channel's unread badge.
6. Reopening the page still shows the persisted read receipt.

---

## 7. Out of Scope

- Per-user `read_at` timestamps for each message.
- A dedicated `message_read_receipts` table.
- Full read receipt popover with searchable user list.
- Read receipt display for every message from every sender.
- Push notifications or mobile-specific unread synchronization.
