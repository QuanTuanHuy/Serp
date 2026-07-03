# Discuss Realtime Performance Design

## Context

`discuss_service` is a Spring Boot service for real-time communication. The current
message path is:

1. A client sends a message through REST or WebSocket.
2. `MessageUseCase` validates membership, persists the message, updates channel
   and unread state, and publishes an internal Spring event.
3. `MessageEventListener` handles the internal event after transaction commit,
   updates caches, and publishes a Kafka event.
4. `DiscussKafkaConsumer` receives the Kafka event.
5. The event handler loads or enriches message data, resolves channel members and
   online presence, then sends to each user's `/user/queue/events` destination.

This keeps database writes transactional, but the realtime hot path can still do
expensive work after Kafka consumption: message reloads, Account Service calls for
sender data, attachment URL generation, member lookup, presence lookup, and
sequential WebSocket sends.

## Goals

- Reduce p95 send-to-receive latency for realtime events to 300-500 ms with
  hundreds of online recipients in one channel, assuming warm caches and healthy
  infrastructure.
- Increase throughput for concurrent chat activity.
- Reduce repeated DB, Redis, Account Service, and S3/presign work in the consumer
  hot path.
- Keep the first `MESSAGE_NEW` WebSocket event render-complete: message core,
  sender info, attachment URLs, reactions, and read fields should be present when
  available.
- Keep the service primarily optimized for one running instance, while creating a
  clean delivery boundary that can later be replaced with channel-topic or broker
  relay delivery.

## Non-Goals

- Do not introduce RabbitMQ, ActiveMQ, or a STOMP broker relay in this phase.
- Do not require frontend hydration after `MESSAGE_NEW` just to render the first
  message.
- Do not move message write side effects into the database transaction.
- Do not redesign persistence, membership, or attachment storage models.

## Recommended Approach

Use "enriched payload cache plus bounded fan-out".

The service keeps the current REST/WebSocket -> use case -> after-commit event ->
Kafka -> WebSocket delivery shape. The main change is to build a ready-to-deliver
`WsEvent` once before Kafka publication, then keep Kafka consumers and WebSocket
delivery lightweight.

## Components

### RealtimePayloadBuilder

`RealtimePayloadBuilder` builds complete `WsEvent` payloads for realtime delivery.

Responsibilities:

- Build `MESSAGE_NEW`, `MESSAGE_UPDATED`, `MESSAGE_DELETED`, `MESSAGE_READ`,
  reaction, typing, and presence events in one central place.
- For message events, enrich `MessageResponse` with sender info and attachment
  URLs before Kafka publication.
- Prefer cache reads over downstream calls.
- Degrade gracefully when downstream enrichment fails.
- Optionally cache ready-to-deliver payloads by message/event id for short-lived
  reuse and debugging.

Dependencies:

- `IMessageService` or already persisted `MessageEntity`, depending on event type.
- `IAttachmentUrlService`
- `IUserInfoService`
- `IDiscussCacheService`

### User Info Cache

`UserInfoService.getUserById()` and `getUsersByIds()` should use Redis cache
before calling Account Service.

Rules:

- Cache key: existing `discuss:user_info:{userId}` prefix.
- TTL: existing `USER_INFO_CACHE_TTL` of 3600 seconds.
- Batch lookup should read cache for all requested IDs, call Account Service only
  for misses, and write returned users back to cache.
- Account Service failure should return partial cached data when available.

### Attachment URL Cache

The existing attachment URL cache remains the source of reuse for presigned URLs.
`RealtimePayloadBuilder` should rely on `IAttachmentUrlService`, which already
tries cached URL data before generating new presigned URLs.

### RealtimeDeliveryService

`RealtimeDeliveryService` delivers ready-to-send `WsEvent` objects.

Responsibilities:

- Resolve channel recipients from member cache with DB fallback.
- Resolve online recipients using batch presence lookup.
- Exclude sender for typing events.
- Dedupe recipients for presence events.
- Send to `/user/queue/events` with bounded concurrency.
- Record timing and count information for observability.
- Isolate individual send failures so one failed user delivery does not fail the
  whole event.

Suggested config:

```yaml
discuss:
  realtime:
    delivery:
      max-concurrency: 64
    typing:
      debounce-ms: 2000
    payload-cache-ttl-seconds: 300
```

### RealtimeDeliveryPort

Add a delivery boundary so the current personal-queue implementation can later be
replaced by channel-topic or broker relay delivery.

Initial implementation:

- Resolve target users server-side.
- Send each user to `/user/queue/events` through `IWebSocketHubPort`.

Future implementation:

- Publish channel-scoped events to `/topic/channels/{channelId}/events`.
- Use an external broker relay if multiple service instances become the primary
  deployment model.

## Data Flow

### MESSAGE_NEW

1. `MessageUseCase.sendMessage()` and `sendMessageWithAttachments()` keep the
   current transactional write behavior.
2. After commit, `MessageEventListener.handleMessageSent()` caches the message,
   updates the first page cache, then asks `RealtimePayloadBuilder` to build a
   complete `MESSAGE_NEW` event.
3. The builder creates a `MessageResponse` from the saved message, enriches sender
   info from cache or Account Service fallback, enriches attachment URLs from URL
   cache or S3 presign fallback, and includes existing reaction/read fields.
4. `IDiscussEventPublisher` publishes a Kafka event that already contains the
   ready-to-deliver `WsEvent` payload.
5. `DiscussKafkaConsumer` parses the event and dispatches to the matching handler.
6. The `MESSAGE_NEW` handler calls `RealtimeDeliveryService.deliverToChannel()`.
7. Delivery resolves online recipients once and sends with bounded concurrency.
8. Legacy fallback remains available: if an old Kafka event contains only
   `messageId`, the handler may use the existing `notifyNewMessage()` path.

### Message Update, Delete, Read, and Reaction Events

- Update events should reuse the same payload builder pattern as `MESSAGE_NEW`.
- Delete events may remain lightweight because the client only needs delete
  metadata.
- Read and reaction events should avoid message reloads when their Kafka payload
  already has all required fields.
- Handlers should prefer ready payloads and fall back to legacy behavior only for
  incomplete events.

### Typing Events

- The WebSocket handler writes typing state to Redis with the existing TTL.
- Repeated `TYPING_START` events for the same user and channel are suppressed
  during the configured debounce window.
- Typing delivery excludes the sender and uses member plus presence cache.
- Payload remains lightweight.

### Presence Events

- Session registration and unregistration continue to maintain Redis presence.
- Presence delivery gathers the changed user's active channels, dedupes all
  recipient IDs, filters online recipients in one batch, and sends once per
  recipient.
- If presence is still noisy after this change, a small coalescing window can be
  added without changing the public WebSocket contract.

## Error Handling

- Database commit is the source of truth. Realtime enrichment failure must not
  roll back committed data.
- Sender info failure degrades to `senderId` with nullable `sender`.
- Attachment URL failure degrades to attachment metadata with nullable URLs.
- Cache failures should log and fall back to DB or downstream calls when safe.
- Kafka publish failures should log `messageId`, `channelId`, and event type and
  emit a metric.
- Consumer payload validation should reject invalid events clearly, while legacy
  supported event shapes should use fallback logic.
- Per-user WebSocket send failures are logged and counted but do not stop fan-out
  to other recipients.

## Backpressure

- Do not fan-out sequentially on the consumer thread for hundreds of recipients.
- Use a bounded executor or semaphore around `IWebSocketHubPort.sendToUser()`.
- Default max concurrency is 64.
- Avoid unbounded queues for delivery work.
- If recipient count is zero after presence filtering, skip WebSocket send work.
- For typing and presence, reduce event volume before delivery through debounce,
  dedupe, and lightweight payloads.

## Observability

Add structured logs or metrics for:

- Payload build duration.
- User info cache hit/miss count.
- Attachment URL cache hit/miss count.
- Kafka publish success/failure.
- Kafka consumer handling duration.
- Recipient count before and after online filtering.
- Fan-out duration.
- Per-event failed send count.
- Legacy fallback usage count.

These measurements are required to validate the 300-500 ms p95 target and to
detect regressions where enrichment re-enters the consumer hot path.

## Testing Strategy

### Unit Tests

- `RealtimePayloadBuilderTest`
  - Cache hit avoids Account Service calls.
  - Cache miss calls Account Service once and stores the result.
  - Attachment URL cache hit avoids presign generation.
  - Sender info failure returns a degraded but valid message payload.
  - Attachment URL failure returns a degraded but valid message payload.

- `RealtimeDeliveryServiceTest`
  - Resolves active channel members.
  - Filters offline users through batch presence lookup.
  - Excludes sender for typing events.
  - Dedupes recipients for presence events.
  - Does not fail the full event when one user send fails.
  - Respects configured bounded concurrency.

- `DiscussKafkaConsumer` and handler tests
  - Ready-to-deliver payload calls delivery directly.
  - Legacy message payload with only `messageId` uses fallback.
  - Invalid payloads are rejected or ignored consistently with current retry/DLT
    behavior.

- `UserInfoServiceTest`
  - Single user cache hit, cache miss, and downstream failure.
  - Batch user lookup with partial cache hits and only missing IDs fetched.

### Performance-Style Tests

Use fake ports and deterministic data to simulate a channel with hundreds of
online recipients. Verify that:

- The consumer path does not call Account Service, S3, or DB for ready payloads.
- Fan-out completes through the bounded delivery path.
- Recipient resolution uses batch presence calls instead of per-user calls.

### Verification Commands

From `discuss_service/`:

```bash
mvnw.cmd test -Dtest=RealtimePayloadBuilderTest,RealtimeDeliveryServiceTest,UserInfoServiceTest
mvnw.cmd test -Dtest=MessageUseCaseTest,DeliveryServiceTest
mvnw.cmd -q -DskipTests compile
```

The exact test class list may change during implementation based on final class
names and nearby test coverage.

## Rollout Plan

1. Add `RealtimePayloadBuilder`, `RealtimeDeliveryService`, config properties,
   and user info caching without removing legacy handlers.
2. Publish ready-to-deliver Kafka payloads for `MESSAGE_NEW`.
3. Update `MESSAGE_NEW` consumer handling to use ready payloads, with legacy
   fallback retained.
4. Apply the same pattern to update, read, reaction, typing, and presence events.
5. Enable timing logs/metrics and compare before/after p95 latency and downstream
   call counts.
6. Remove legacy fallback only after deployed producers can no longer emit old
   payload shapes.

## Contract Impact

Kafka payload changes from "event metadata plus ids" toward a ready-to-deliver
`WsEvent` shape. WebSocket payload remains render-complete for clients. The
frontend should not need to hydrate message detail after receiving the first
`MESSAGE_NEW` event.

## Future Path Toward Channel Topic Delivery

This design deliberately adds `RealtimeDeliveryPort` so a later phase can move
from personal queue fan-out to channel-topic fan-out or broker relay delivery.
That later phase would require frontend subscription changes and subscription
authorization, so it is not part of the current implementation.

