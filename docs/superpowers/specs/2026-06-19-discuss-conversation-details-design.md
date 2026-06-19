# Discuss Conversation Details Sidebar Design

## Context

The Discuss module already supports channel lists, message history, real-time updates, presence, message search, threaded replies, reactions, and inline file attachments. The current chat header also shows phone and video call buttons for direct conversations, but the backend has no call, voice, or video domain support. Those controls are therefore misleading and should be removed until a real calling feature exists.

The existing conversation info experience is a `ChannelMembersPanel` dialog focused on members. This design replaces that narrow dialog with a persistent right sidebar, similar to Slack or Discord, so users can keep the conversation visible while inspecting channel details, members, and shared files.

## Goals

- Remove unsupported call and video-call actions from the Discuss chat header.
- Add a right-side conversation details sidebar that can stay open while reading or sending messages.
- Reuse existing channel, member, presence, attachment, and download-url behavior where possible.
- Add a focused backend endpoint for paginated file history by channel.
- Keep the first version intentionally small: overview, members, files.

## Non-goals

- No audio/video call implementation.
- No pinned messages, shared links, media gallery, or activity timeline in this version.
- No new file storage system; file history is based on existing message attachments.
- No broad redesign of the Discuss route shell or channel list.

## Recommended approach

Implement the sidebar inside `ChatWindow`. This keeps the change local to the current chat experience, where the existing search and members-panel state already lives. The chat body should resize when the sidebar is open on desktop. On smaller screens, the same sidebar can behave as an overlay drawer to avoid crushing the message area.

Alternative approaches considered:

- Keeping the current dialog: lowest effort, but it hides the conversation and does not match the desired Slack/Discord interaction.
- Managing the sidebar from `app/discuss/page.tsx`: cleaner for a future global layout, but premature because the panel is only used by the active chat window today.

## Frontend design

### Chat header

Update `serp_web/src/modules/discuss/components/ChatWindow.tsx`:

- Remove `Phone` and `Video` imports and buttons.
- Keep the search button.
- Keep the info button, but make it toggle the conversation details sidebar.
- Remove the `MoreVertical` button unless it is wired to a real menu during implementation. A visible no-op menu should not remain in the cleaned-up UI.

### Sidebar shell

Create a `ConversationDetailsSidebar` component under `serp_web/src/modules/discuss/components/`.

Responsibilities:

- Render as a right sidebar with close button, channel title, and tabs.
- Accept `channel`, `currentUserId`, `open`, and `onOpenChange`.
- Keep layout concerns local: width around 380-400px on desktop; overlay drawer behavior on smaller viewports.
- Avoid owning message state or WebSocket behavior.

The `ChatWindow` layout becomes:

- Left/main area: current header, message list, input.
- Right area: `ConversationDetailsSidebar` when open.

### Sidebar tabs

The sidebar should have three first-version tabs:

1. Overview
   - Channel name, description, type.
   - Member count, message count, created/updated time if available.
   - Entity information for `TOPIC` channels: `entityType` and `entityId`.
   - Archived/private indicators if available in the current response.

2. Members
   - Move or extract the useful body of `ChannelMembersPanel` into a tab-friendly component.
   - Preserve existing member list, presence, add member, and remove member behavior.
   - Keep role badges and online indicators.

3. Files
   - Show channel attachments newest first.
   - Display file name, type/icon, size, uploaded time, and download/open action.
   - Use existing download-url endpoint for opening files.
   - Add lightweight filtering only if cheap: all/images/documents. Search by file name can be added if backend support is included in the same small endpoint.

## Backend design

Add a channel-scoped attachment history endpoint:

```http
GET /channels/{channelId}/attachments?page=0&size=20&fileType=image&query=report
```

`fileType` and `query` are optional. The minimum useful endpoint is paginated attachments by channel. If filtering makes the first slice too large, implement pagination first and leave filters for a follow-up.

Response shape should follow existing `GeneralResponse<PaginatedResponse<AttachmentResponse>>`.

Authorization and tenancy:

- Resolve `currentUserId` and `tenantId` from `SerpAuthContext`.
- Verify the current user is an active member of the channel before returning attachments.
- Scope attachment reads to the current tenant.

Implementation path:

- Add a method to `AttachmentUseCase` for channel attachment history.
- Add service/port/repository support for paginated `findByChannelId` ordered by created time descending.
- Reuse existing `AttachmentResponse.fromEntity`.
- Do not include permanent presigned URLs in the list response. Keep download URLs behind the existing `GET /attachments/{attachmentId}/download-url` flow.

The database already has `attachments.channel_id` and indexes for channel attachment lookup, so this design should not require a migration unless implementation discovers the JPA model and migrations are out of sync.

## Data flow

1. User opens a channel.
2. User clicks the info button in the chat header.
3. `ChatWindow` opens `ConversationDetailsSidebar`.
4. Overview tab uses the selected `Channel` object and may call `getChannel` if fresh full details are needed.
5. Members tab uses existing `getChannelMembers` and `getChannelPresence` queries.
6. Files tab calls the new channel attachments endpoint.
7. User clicks a file. The FE requests the existing attachment download URL, then opens the returned URL.

## Error handling

- If sidebar overview data is partial, render available fields instead of blocking the whole sidebar.
- If members fail to load, show a scoped error inside the Members tab.
- If files fail to load, show a scoped error and retry action inside the Files tab.
- If download-url generation fails, show a toast and keep the sidebar open.
- Backend should return existing `AppException(ErrorCode.UNAUTHORIZED/FORBIDDEN/NOT_FOUND)` style errors rather than raw exceptions.

## Testing and verification

Backend:

- Unit test channel attachment history use case:
  - active member can list attachments;
  - non-member is rejected;
  - tenant scoping is preserved;
  - pagination returns expected metadata.
- Repository or adapter test if a custom paginated query is added.
- Run the narrow Discuss Service Maven test or compile gate after implementation.

Frontend:

- Run TypeScript check and lint for touched Discuss files.
- Manually verify:
  - no phone/video controls appear;
  - sidebar opens and closes;
  - chat remains visible on desktop when sidebar is open;
  - members tab preserves existing behavior;
  - files tab lists attachments and can open/download a file.

## Acceptance criteria

- Discuss chat header no longer shows unsupported call/video actions.
- Clicking the info button opens a right-side conversation details sidebar.
- Sidebar includes Overview, Members, and Files sections.
- Existing member management behavior remains available.
- Files tab lists shared files for the current channel using a backend channel-history endpoint.
- Attachment downloads still use the existing presigned download-url endpoint.
- No unrelated Discuss features are redesigned or removed.
