# Discuss Read Receipts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add persisted unread-count clearing and read receipts to `discuss_service` and the `serp_web` discuss UI.

**Architecture:** Reuse the existing `messages.read_by` PostgreSQL array for read receipts and `channel_members.last_read_msg_id/unread_count` for per-user unread state. Publish a post-commit `MESSAGE_READ` event through the existing Kafka-to-WebSocket pipeline, then update frontend RTK Query caches and local chat state from that event.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA/Hibernate, JUnit 5, Mockito, Kafka/STOMP WebSocket, Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS.

---

## File Structure

Backend files:

- Modify `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/model/MessageModel.java`
  - Add JPA mapping for existing `messages.read_by`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapper.java`
  - Map `readBy` between model and entity.
- Modify `discuss_service/src/test/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapperTest.java`
  - Add mapper coverage for `readBy`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/response/MessageResponse.java`
  - Add `readBy` and `readByUsers`.
- Create `discuss_service/src/main/java/serp/project/discuss_service/core/domain/event/MessageReadInternalEvent.java`
  - Internal Spring event emitted inside the mark-read transaction.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/usecase/MessageUseCase.java`
  - Validate target message channel, publish internal read event, enrich read receipt users.
- Modify `discuss_service/src/test/java/serp/project/discuss_service/core/usecase/MessageUseCaseTest.java`
  - Add mark-read validation and event tests.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/IMessageService.java`
  - Change `markAsRead` to return the saved `MessageEntity`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/MessageService.java`
  - Return saved message from `markAsRead`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/IDiscussEventPublisher.java`
  - Add `publishMessageRead`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DiscussEventPublisherService.java`
  - Publish `MESSAGE_READ` payload to `discuss.message.events`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/listener/MessageEventListener.java`
  - Handle `MessageReadInternalEvent` after commit.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/IDeliveryService.java`
  - Add `notifyMessageRead`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DeliveryService.java`
  - Fan out `MESSAGE_READ`.
- Create `discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/websocket/WsMessageReadPayload.java`
  - Typed WebSocket payload for read events.
- Create `discuss_service/src/main/java/serp/project/discuss_service/ui/messaging/handler/MessageReadHandler.java`
  - Kafka handler for `MESSAGE_READ`.
- Modify `discuss_service/src/main/java/serp/project/discuss_service/kernel/utils/KafkaPayloadUtils.java`
  - Add typed helpers for integer and long-list payload values.
- Modify `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/DeliveryServiceTest.java`
  - Test WebSocket read event fan-out.

Frontend files:

- Modify `serp_web/src/modules/discuss/types/message.ts`
  - Add `readBy` and `readByUsers`.
- Modify `serp_web/src/modules/discuss/api/transformers.ts`
  - Transform read receipt fields.
- Modify `serp_web/src/modules/discuss/context/WebSocketContext.tsx`
  - Add read-event callback to the WebSocket API contract.
- Modify `serp_web/src/modules/discuss/hooks/useDiscussWebSocket.ts`
  - Add read-event callback, update message/channel caches.
- Modify `serp_web/src/modules/discuss/components/ChatWindow.tsx`
  - Mark latest message as read and update local messages on read events.
- Modify `serp_web/src/modules/discuss/components/MessageItem.tsx`
  - Render lightweight read receipt text for own messages.

---

### Task 1: Persist `readBy` Through Message Mapping

**Files:**
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapperTest.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/model/MessageModel.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapper.java`

- [ ] **Step 1: Write failing mapper tests**

Add assertions to the existing "Should correctly map all fields from model to entity" test:

```java
Long[] readBy = {400L, 500L};

MessageModel model = MessageModel.builder()
        .id(MESSAGE_ID)
        .channelId(CHANNEL_ID)
        .senderId(USER_ID)
        .tenantId(TENANT_ID)
        .content("Test message content")
        .messageType(MessageType.STANDARD)
        .mentions(mentions)
        .readBy(readBy)
        .parentId(1500L)
        .threadCount(5)
        .isEdited(true)
        .editedAt(now)
        .isDeleted(false)
        .deletedAt(null)
        .deletedBy(null)
        .reactions(reactions)
        .metadata(metadata)
        .createdAt(now.minusDays(1))
        .updatedAt(now)
        .build();

assertNotNull(entity.getReadBy());
assertEquals(2, entity.getReadBy().size());
assertTrue(entity.getReadBy().containsAll(Arrays.asList(400L, 500L)));
```

Add assertions to the existing "Should correctly map all fields from entity to model" test:

```java
.readBy(Arrays.asList(500L, 600L))

assertNotNull(model.getReadBy());
assertEquals(2, model.getReadBy().length);
assertTrue(Arrays.asList(model.getReadBy()).containsAll(Arrays.asList(500L, 600L)));
```

Add a focused null test inside `ToEntityTests`:

```java
@Test
@DisplayName("Should handle null readBy array")
void shouldHandleNullReadByArray() {
    MessageModel model = MessageModel.builder()
            .id(MESSAGE_ID)
            .channelId(CHANNEL_ID)
            .senderId(USER_ID)
            .tenantId(TENANT_ID)
            .content("Test message")
            .messageType(MessageType.STANDARD)
            .readBy(null)
            .threadCount(0)
            .isEdited(false)
            .isDeleted(false)
            .build();

    MessageEntity entity = messageMapper.toEntity(model);

    assertNotNull(entity);
    assertNotNull(entity.getReadBy());
    assertTrue(entity.getReadBy().isEmpty());
}
```

- [ ] **Step 2: Run mapper test and verify failure**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageMapperTest test
```

Expected: compile failure because `MessageModel` has no `readBy` field or generated accessor.

- [ ] **Step 3: Map `read_by` in `MessageModel`**

Add this field after `reactions` in `MessageModel.java`:

```java
@JdbcTypeCode(SqlTypes.ARRAY)
@Column(name = "read_by", columnDefinition = "BIGINT[]")
private Long[] readBy;
```

- [ ] **Step 4: Map `readBy` in `MessageMapper`**

Add `.readBy(arrayToList(model.getReadBy()))` in `toEntity`:

```java
.reactions(parseReactions(model.getReactions()))
.readBy(arrayToList(model.getReadBy()))
.metadata(model.getMetadata())
```

Add `.readBy(listToArray(entity.getReadBy()))` in `toModel`:

```java
.reactions(serializeReactions(entity.getReactions()))
.readBy(listToArray(entity.getReadBy()))
.metadata(entity.getMetadata())
```

- [ ] **Step 5: Run mapper test and verify pass**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageMapperTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit Task 1**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/model/MessageModel.java discuss_service/src/main/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapper.java discuss_service/src/test/java/serp/project/discuss_service/infrastructure/store/mapper/MessageMapperTest.java
git commit -m "feat(discuss): persist message read receipts"
```

---

### Task 2: Return Read Receipt Data in Message Responses

**Files:**
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/response/MessageResponse.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/usecase/MessageUseCase.java`
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/usecase/MessageUseCaseTest.java`

- [ ] **Step 1: Write failing response enrichment test**

Add imports to `MessageUseCaseTest.java`:

```java
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.Map;
```

Add these mocks near existing mocks:

```java
@Mock
private IAttachmentUrlService attachmentUrlService;

@Mock
private IUserInfoService userInfoService;
```

Add this nested test class:

```java
@Nested
@DisplayName("enrichMessageResponseList")
class EnrichMessageResponseListTests {

    @Test
    @DisplayName("should include read receipt user info")
    void testEnrichMessageResponseList_WithReadBy_IncludesReadByUsers() {
        MessageEntity message = TestDataFactory.createTextMessage();
        message.setReadBy(List.of(TestDataFactory.USER_ID_2, TestDataFactory.USER_ID_3));

        UserInfo sender = UserInfo.builder()
                .id(TestDataFactory.USER_ID_1)
                .name("Sender")
                .email("sender@example.com")
                .build();
        UserInfo readerOne = UserInfo.builder()
                .id(TestDataFactory.USER_ID_2)
                .name("Reader One")
                .email("reader1@example.com")
                .build();
        UserInfo readerTwo = UserInfo.builder()
                .id(TestDataFactory.USER_ID_3)
                .name("Reader Two")
                .email("reader2@example.com")
                .build();

        when(userInfoService.getUsersByIds(List.of(
                TestDataFactory.USER_ID_1,
                TestDataFactory.USER_ID_2,
                TestDataFactory.USER_ID_3
        ))).thenReturn(List.of(sender, readerOne, readerTwo));
        when(attachmentService.getAttachmentsByMessageIds(List.of(TestDataFactory.MESSAGE_ID)))
                .thenReturn(Map.of());
        when(attachmentUrlService.enrichWithUrls(List.of()))
                .thenReturn(List.of());

        List<MessageResponse> responses = messageUseCase.enrichMessageResponseList(
                List.of(message),
                TestDataFactory.USER_ID_2
        );

        assertEquals(1, responses.size());
        MessageResponse response = responses.get(0);
        assertEquals(List.of(TestDataFactory.USER_ID_2, TestDataFactory.USER_ID_3), response.getReadBy());
        assertEquals(2, response.getReadCount());
        assertTrue(response.getIsReadByMe());
        assertEquals(2, response.getReadByUsers().size());
        assertEquals("Reader One", response.getReadByUsers().get(0).getName());
        assertEquals("Reader Two", response.getReadByUsers().get(1).getName());
    }
}
```

- [ ] **Step 2: Run use case test and verify failure**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageUseCaseTest#testEnrichMessageResponseList_WithReadBy_IncludesReadByUsers test
```

Expected: compile failure because `MessageResponse` does not expose `readBy` and `readByUsers`.

- [ ] **Step 3: Extend `MessageResponse`**

Add fields after `readCount`:

```java
private List<Long> readBy;
private List<UserInfo> readByUsers;
```

Add builder fields in `fromEntity`:

```java
.reactions(reactionResponses)
.readCount(entity.getReadCount())
.readBy(entity.getReadBy())
.metadata(entity.getMetadata())
```

- [ ] **Step 4: Enrich read receipt users in `MessageUseCase`**

Replace the existing sender lookup block in `enrichMessageResponseList` with this combined lookup:

```java
List<Long> userIdsToLoad = messages.stream()
        .flatMap(message -> {
            List<Long> ids = new ArrayList<>();
            ids.add(message.getSenderId());
            if (message.getReadBy() != null) {
                ids.addAll(message.getReadBy());
            }
            return ids.stream();
        })
        .filter(id -> id != null)
        .distinct()
        .toList();

Map<Long, UserInfo> userInfoMap = userInfoService.getUsersByIds(userIdsToLoad).stream()
        .collect(Collectors.toMap(UserInfo::getId, Function.identity()));
```

Inside the response mapping lambda, after `response.setIsSentByMe(...)`, add:

```java
response.setIsReadByMe(msg.isReadBy(currentUserId));

List<UserInfo> readByUsers = msg.getReadBy() == null
        ? List.of()
        : msg.getReadBy().stream()
                .map(userInfoMap::get)
                .filter(userInfo -> userInfo != null)
                .toList();
response.setReadByUsers(readByUsers);
```

- [ ] **Step 5: Run focused use case test**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageUseCaseTest#testEnrichMessageResponseList_WithReadBy_IncludesReadByUsers test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Run all use case tests**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageUseCaseTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit Task 2**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/response/MessageResponse.java discuss_service/src/main/java/serp/project/discuss_service/core/usecase/MessageUseCase.java discuss_service/src/test/java/serp/project/discuss_service/core/usecase/MessageUseCaseTest.java
git commit -m "feat(discuss): include read receipt users in messages"
```

---

### Task 3: Publish `MESSAGE_READ` From Mark-As-Read

**Files:**
- Create: `discuss_service/src/main/java/serp/project/discuss_service/core/domain/event/MessageReadInternalEvent.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/IMessageService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/MessageService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/usecase/MessageUseCase.java`
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/usecase/MessageUseCaseTest.java`

- [ ] **Step 1: Write failing mark-read tests**

Add import:

```java
import serp.project.discuss_service.core.domain.event.MessageReadInternalEvent;
import org.mockito.ArgumentCaptor;
```

Replace the existing successful mark-read test with:

```java
@Test
@DisplayName("should mark target message as read and publish read event when user is member")
void testMarkAsRead_UserIsMember_MarksReadAndPublishesEvent() {
    MessageEntity target = TestDataFactory.createTextMessage();
    target.setId(100L);
    target.setChannelId(TestDataFactory.CHANNEL_ID);
    target.setReadBy(List.of(TestDataFactory.USER_ID_1));

    when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2)).thenReturn(true);
    when(messageService.getMessageByIdOrThrow(100L)).thenReturn(target);
    when(messageService.markAsRead(100L, TestDataFactory.USER_ID_2)).thenAnswer(invocation -> {
        target.markReadBy(TestDataFactory.USER_ID_2);
        return target;
    });

    messageUseCase.markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2, 100L);

    verify(memberService).markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_2, 100L);
    verify(messageService).markAsRead(100L, TestDataFactory.USER_ID_2);

    ArgumentCaptor<MessageReadInternalEvent> eventCaptor =
            ArgumentCaptor.forClass(MessageReadInternalEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

    MessageReadInternalEvent event = eventCaptor.getValue();
    assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());
    assertEquals(100L, event.getMessageId());
    assertEquals(TestDataFactory.USER_ID_2, event.getUserId());
    assertEquals(2, event.getReadCount());
    assertTrue(event.getReadBy().contains(TestDataFactory.USER_ID_2));
}
```

Add channel mismatch test:

```java
@Test
@DisplayName("should throw when target message is not in channel")
void testMarkAsRead_MessageInDifferentChannel_ThrowsException() {
    MessageEntity target = TestDataFactory.createTextMessage();
    target.setId(100L);
    target.setChannelId(999L);

    when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
    when(messageService.getMessageByIdOrThrow(100L)).thenReturn(target);

    AppException exception = assertThrows(AppException.class,
            () -> messageUseCase.markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, 100L));

    assertEquals(ErrorCode.MESSAGE_NOT_FOUND.getMessage(), exception.getMessage());
    verify(memberService, never()).markAsRead(anyLong(), anyLong(), anyLong());
    verify(messageService, never()).markAsRead(anyLong(), anyLong());
    verify(applicationEventPublisher, never()).publishEvent(any());
}
```

- [ ] **Step 2: Run focused mark-read tests and verify failure**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageUseCaseTest#testMarkAsRead_UserIsMember_MarksReadAndPublishesEvent test
```

Expected: compile failure because `MessageReadInternalEvent` does not exist and `IMessageService.markAsRead` returns `void`.

- [ ] **Step 3: Create internal event**

Create `MessageReadInternalEvent.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Internal event for message read receipts
 */

package serp.project.discuss_service.core.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class MessageReadInternalEvent extends ApplicationEvent {

    private final Long channelId;
    private final Long messageId;
    private final Long userId;
    private final List<Long> readBy;
    private final Integer readCount;

    public MessageReadInternalEvent(
            Object source,
            Long channelId,
            Long messageId,
            Long userId,
            List<Long> readBy,
            Integer readCount) {
        super(source);
        this.channelId = channelId;
        this.messageId = messageId;
        this.userId = userId;
        this.readBy = readBy == null ? List.of() : List.copyOf(readBy);
        this.readCount = readCount == null ? 0 : readCount;
    }
}
```

- [ ] **Step 4: Return saved message from `markAsRead` service**

Change interface:

```java
MessageEntity markAsRead(Long messageId, Long userId);
```

Change implementation:

```java
@Override
public MessageEntity markAsRead(Long messageId, Long userId) {
    MessageEntity message = getMessageByIdOrThrow(messageId);
    message.markReadBy(userId);
    MessageEntity saved = messagePort.save(message);
    log.debug("Message {} marked as read by user {}", messageId, userId);
    return saved;
}
```

- [ ] **Step 5: Validate channel and publish event in use case**

In `MessageUseCase.markAsRead`, replace the body after membership check with:

```java
MessageEntity target = messageService.getMessageByIdOrThrow(messageId);
if (!target.getChannelId().equals(channelId)) {
    throw new AppException(ErrorCode.MESSAGE_NOT_FOUND);
}

memberService.markAsRead(channelId, userId, messageId);
MessageEntity updated = messageService.markAsRead(messageId, userId);

applicationEventPublisher.publishEvent(new MessageReadInternalEvent(
        this,
        channelId,
        messageId,
        userId,
        updated.getReadBy(),
        updated.getReadCount()
));

log.debug("User {} marked messages as read in channel {} up to {}", userId, channelId, messageId);
```

- [ ] **Step 6: Run mark-read tests**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageUseCaseTest#testMarkAsRead_UserIsMember_MarksReadAndPublishesEvent test
./mvnw -Dtest=MessageUseCaseTest#testMarkAsRead_MessageInDifferentChannel_ThrowsException test
```

Expected: both commands report `BUILD SUCCESS`.

- [ ] **Step 7: Commit Task 3**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/core/domain/event/MessageReadInternalEvent.java discuss_service/src/main/java/serp/project/discuss_service/core/service/IMessageService.java discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/MessageService.java discuss_service/src/main/java/serp/project/discuss_service/core/usecase/MessageUseCase.java discuss_service/src/test/java/serp/project/discuss_service/core/usecase/MessageUseCaseTest.java
git commit -m "feat(discuss): publish message read events"
```

---

### Task 4: Deliver `MESSAGE_READ` Through Kafka and WebSocket

**Files:**
- Create: `discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/websocket/WsMessageReadPayload.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/IDiscussEventPublisher.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DiscussEventPublisherService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/listener/MessageEventListener.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/IDeliveryService.java`
- Modify: `discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DeliveryService.java`
- Create: `discuss_service/src/main/java/serp/project/discuss_service/ui/messaging/handler/MessageReadHandler.java`
- Modify: `discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/DeliveryServiceTest.java`

- [ ] **Step 1: Write failing delivery test**

Add imports:

```java
import java.util.Map;
```

Add test:

```java
@Test
@DisplayName("notifyMessageRead should fan out read event to online channel members")
void testNotifyMessageRead_FansOutReadEvent() {
    Long readerId = TestDataFactory.USER_ID_2;
    Set<Long> members = Set.of(TestDataFactory.USER_ID_1, readerId, TestDataFactory.USER_ID_3);
    Set<Long> onlineMembers = Set.of(TestDataFactory.USER_ID_1, readerId);

    when(memberService.getMemberIds(TestDataFactory.CHANNEL_ID)).thenReturn(members);
    when(presenceService.getOnlineUsers(members)).thenReturn(onlineMembers);

    deliveryService.notifyMessageRead(
            TestDataFactory.CHANNEL_ID,
            TestDataFactory.MESSAGE_ID,
            readerId,
            List.of(readerId),
            1
    );

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(webSocketHub).sendToUsers(eq(onlineMembers), eventCaptor.capture());

    WsEvent<?> event = (WsEvent<?>) eventCaptor.getValue();
    assertEquals(WsEventType.MESSAGE_READ, event.getType());
    assertEquals(TestDataFactory.CHANNEL_ID, event.getChannelId());

    Object payload = event.getPayload();
    assertNotNull(payload);
}
```

- [ ] **Step 2: Run delivery test and verify failure**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=DeliveryServiceTest#testNotifyMessageRead_FansOutReadEvent test
```

Expected: compile failure because `notifyMessageRead` does not exist.

- [ ] **Step 3: Create WebSocket read payload**

Create `WsMessageReadPayload.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket payload for message read receipts
 */

package serp.project.discuss_service.core.domain.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WsMessageReadPayload {

    private Long messageId;
    private Long channelId;
    private Long userId;
    private List<Long> readBy;
    private Integer readCount;
}
```

- [ ] **Step 4: Add delivery contract and implementation**

Add to `IDeliveryService`:

```java
void notifyMessageRead(Long channelId, Long messageId, Long userId, List<Long> readBy, Integer readCount);
```

Add `java.util.List` import to `IDeliveryService`.

Add `java.util.List` import to `DeliveryService`.

Add to `DeliveryService`:

```java
@Override
public void notifyMessageRead(Long channelId, Long messageId, Long userId, List<Long> readBy, Integer readCount) {
    if (channelId == null || messageId == null || userId == null) {
        log.warn("Cannot notify message read with null channelId, messageId or userId");
        return;
    }

    WsMessageReadPayload payload = WsMessageReadPayload.builder()
            .messageId(messageId)
            .channelId(channelId)
            .userId(userId)
            .readBy(readBy == null ? List.of() : readBy)
            .readCount(readCount == null ? 0 : readCount)
            .build();

    WsEvent<WsMessageReadPayload> event = WsEvent.of(WsEventType.MESSAGE_READ, payload, channelId);
    fanOutToChannelMembers(channelId, event);
    log.debug("Notified message read for message {} in channel {} by user {}", messageId, channelId, userId);
}
```

- [ ] **Step 5: Add publisher contract and implementation**

Add to `IDiscussEventPublisher`:

```java
void publishMessageRead(Long channelId, Long messageId, Long userId, List<Long> readBy, Integer readCount);
```

Add `java.util.List` import to `IDiscussEventPublisher`.

Add to `DiscussEventPublisherService`:

```java
@Override
public void publishMessageRead(Long channelId, Long messageId, Long userId, List<Long> readBy, Integer readCount) {
    if (channelId == null || messageId == null || userId == null) {
        log.warn("Cannot publish MESSAGE_READ event: missing required fields");
        return;
    }

    Map<String, Object> event = new HashMap<>();
    event.put("eventType", WsEventType.MESSAGE_READ.name());
    event.put("messageId", messageId);
    event.put("channelId", channelId);
    event.put("userId", userId);
    event.put("readBy", readBy == null ? List.of() : readBy);
    event.put("readCount", readCount == null ? 0 : readCount);
    event.put("timestamp", System.currentTimeMillis());

    kafkaProducer.sendMessageAsync(String.valueOf(channelId), event, TOPIC_MESSAGE_EVENTS);
    log.debug("Published MESSAGE_READ event for message {} by user {}", messageId, userId);
}
```

Add `java.util.List` import to `DiscussEventPublisherService`.

- [ ] **Step 6: Handle internal event after commit**

Add import to `MessageEventListener`:

```java
import serp.project.discuss_service.core.domain.event.MessageReadInternalEvent;
```

Add method:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleMessageRead(MessageReadInternalEvent event) {
    CompletableFuture.runAsync(() -> {
        try {
            log.debug("Processing post-commit for message read: messageId={}, userId={}",
                    event.getMessageId(), event.getUserId());

            eventPublisher.publishMessageRead(
                    event.getChannelId(),
                    event.getMessageId(),
                    event.getUserId(),
                    event.getReadBy(),
                    event.getReadCount());

            cacheService.invalidateMessage(event.getMessageId());
            cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());
        } catch (Exception e) {
            log.error("Failed to process post-commit for message read {}: {}",
                    event.getMessageId(), e.getMessage(), e);
        }
    }, messageAsyncExecutor);
}
```

- [ ] **Step 7: Add Kafka handler**

Create `MessageReadHandler.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Handler for MESSAGE_READ events
 */

package serp.project.discuss_service.ui.messaging.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.websocket.WsEvent;
import serp.project.discuss_service.core.domain.dto.websocket.WsEventType;
import serp.project.discuss_service.core.service.IDeliveryService;
import serp.project.discuss_service.kernel.utils.KafkaPayloadUtils;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReadHandler implements IMessageEventHandler {

    private final IDeliveryService deliveryService;

    @Override
    public WsEventType getType() {
        return WsEventType.MESSAGE_READ;
    }

    @Override
    public void handle(WsEvent<Map<String, Object>> event) {
        Long channelId = event.getChannelId();
        Long messageId = KafkaPayloadUtils.getLong(event.getPayload(), "messageId");
        Long userId = KafkaPayloadUtils.getLong(event.getPayload(), "userId");
        Integer readCount = KafkaPayloadUtils.getInteger(event.getPayload(), "readCount");
        List<Long> readBy = KafkaPayloadUtils.getLongList(event.getPayload(), "readBy");

        if (channelId == null || messageId == null || userId == null) {
            log.warn("Missing required fields for MESSAGE_READ event");
            return;
        }

        deliveryService.notifyMessageRead(channelId, messageId, userId, readBy, readCount);
    }
}
```

- [ ] **Step 8: Add typed helpers to `KafkaPayloadUtils`**

Add import:

```java
import java.util.List;
```

Add these methods:

```java
public static Integer getInteger(Map<String, Object> payload, String key) {
    Long value = getLong(payload, key);
    return value == null ? null : value.intValue();
}

public static List<Long> getLongList(Map<String, Object> payload, String key) {
    Object value = payload.get(key);
    if (!(value instanceof List<?> list)) {
        return List.of();
    }
    return list.stream()
            .filter(item -> item instanceof Number)
            .map(item -> ((Number) item).longValue())
            .toList();
}
```

- [ ] **Step 9: Run delivery test**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=DeliveryServiceTest#testNotifyMessageRead_FansOutReadEvent test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 10: Compile backend**

Run from `discuss_service/`:

```bash
./mvnw -q -DskipTests compile
```

Expected: command exits `0`.

- [ ] **Step 11: Commit Task 4**

```bash
git add discuss_service/src/main/java/serp/project/discuss_service/core/domain/dto/websocket/WsMessageReadPayload.java discuss_service/src/main/java/serp/project/discuss_service/core/service/IDiscussEventPublisher.java discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DiscussEventPublisherService.java discuss_service/src/main/java/serp/project/discuss_service/core/listener/MessageEventListener.java discuss_service/src/main/java/serp/project/discuss_service/core/service/IDeliveryService.java discuss_service/src/main/java/serp/project/discuss_service/core/service/impl/DeliveryService.java discuss_service/src/main/java/serp/project/discuss_service/ui/messaging/handler/MessageReadHandler.java discuss_service/src/main/java/serp/project/discuss_service/kernel/utils/KafkaPayloadUtils.java discuss_service/src/test/java/serp/project/discuss_service/core/service/impl/DeliveryServiceTest.java
git commit -m "feat(discuss): deliver message read websocket events"
```

---

### Task 5: Add Frontend Read Receipt Types and WebSocket Cache Updates

**Files:**
- Modify: `serp_web/src/modules/discuss/types/message.ts`
- Modify: `serp_web/src/modules/discuss/types/index.ts`
- Modify: `serp_web/src/modules/discuss/api/transformers.ts`
- Modify: `serp_web/src/modules/discuss/context/WebSocketContext.tsx`
- Modify: `serp_web/src/modules/discuss/hooks/useDiscussWebSocket.ts`

- [ ] **Step 1: Add frontend message fields**

In `message.ts`, add fields to `Message` after `readCount`:

```ts
readBy: string[];
readByUsers?: SenderInfo[];
```

- [ ] **Step 2: Add read event payload type**

In `types/index.ts`, add:

```ts
export interface MessageReadPayload {
  messageId: string | number;
  channelId: string | number;
  userId: string | number;
  readBy?: Array<string | number>;
  readCount?: number;
}
```

- [ ] **Step 3: Transform backend read receipt fields**

In `transformMessage`, add these properties:

```ts
readBy: Array.isArray(backendMsg.readBy)
  ? backendMsg.readBy.map(String)
  : [],
readByUsers: Array.isArray(backendMsg.readByUsers)
  ? backendMsg.readByUsers.map(transformSenderInfo)
  : [],
```

- [ ] **Step 4: Add read callback to WebSocket context contract**

In `WebSocketContext.tsx`, update imports:

```ts
import type { Message, MessageReadPayload } from '../types';
```

Add this field to `WebSocketAPI` after `setOnMessage`:

```ts
setOnMessageRead: (
  cb: ((payload: MessageReadPayload) => void) | undefined
) => void;
```

- [ ] **Step 5: Add read callback plumbing to `useDiscussWebSocket`**

Import `MessageReadPayload`:

```ts
import type {
  Message,
  MessageReaction,
  MessageReadPayload,
  WsEvent,
  ChannelFilters,
  PaginationParams,
} from '../types';
```

Add ref:

```ts
const onMessageReadRef = useRef<
  ((payload: MessageReadPayload) => void) | undefined
>(undefined);
```

Add setter:

```ts
const setOnMessageRead = useCallback(
  (cb: ((payload: MessageReadPayload) => void) | undefined) => {
    onMessageReadRef.current = cb;
  },
  []
);
```

Return it from the API object:

```ts
setOnMessageRead,
```

Add it to the `useMemo` dependency list:

```ts
setOnMessageRead,
```

- [ ] **Step 6: Update `MESSAGE_READ` handler in `useDiscussWebSocket`**

Replace the current `MESSAGE_READ` case with:

```ts
case 'MESSAGE_READ': {
  console.log('[WebSocket] Message read:', data);

  const normalizedChannelId = String(data.channelId);
  const normalizedMessageId = String(data.messageId);
  const normalizedUserId = String(data.userId);
  const readBy = Array.isArray(data.readBy)
    ? data.readBy.map(String)
    : [normalizedUserId];
  const readCount =
    typeof data.readCount === 'number' ? data.readCount : readBy.length;
  const currentUserId = String(state.account?.user?.profile?.id || '');

  const cacheInfo = findMessagesCacheEntry(state, data.channelId);
  if (cacheInfo) {
    dispatch(
      messageApi.util.updateQueryData(
        'getMessages',
        { channelId: normalizedChannelId, pagination: cacheInfo },
        (draft) => {
          const message = draft.data?.items?.find(
            (item) => item.id === normalizedMessageId
          );
          if (!message) {
            return;
          }

          const existingReadBy = new Set(message.readBy || []);
          for (const readerId of readBy) {
            existingReadBy.add(readerId);
          }
          message.readBy = Array.from(existingReadBy);
          message.readCount = readCount;
          message.isReadByMe = currentUserId
            ? message.readBy.includes(currentUserId)
            : message.isReadByMe;
        }
      )
    );
  }

  if (currentUserId && normalizedUserId === currentUserId) {
    const channelCaches = findChannelsCacheEntries(state);
    for (const args of channelCaches) {
      dispatch(
        channelApi.util.updateQueryData('getChannels', args, (draft) => {
          const channel = draft.data?.items?.find(
            (item) => String(item.id) === normalizedChannelId
          );
          if (channel) {
            channel.unreadCount = 0;
          }
        })
      );
    }

    dispatch(
      channelApi.util.updateQueryData(
        'getChannel',
        normalizedChannelId,
        (draft) => {
          if (draft.data) {
            draft.data.unreadCount = 0;
          }
        }
      )
    );
  }

  onMessageReadRef.current?.({
    messageId: normalizedMessageId,
    channelId: normalizedChannelId,
    userId: normalizedUserId,
    readBy,
    readCount,
  });
  break;
}
```

- [ ] **Step 7: Run frontend type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: command exits `0`.

- [ ] **Step 8: Commit Task 5**

```bash
git add serp_web/src/modules/discuss/types/message.ts serp_web/src/modules/discuss/types/index.ts serp_web/src/modules/discuss/api/transformers.ts serp_web/src/modules/discuss/context/WebSocketContext.tsx serp_web/src/modules/discuss/hooks/useDiscussWebSocket.ts
git commit -m "feat(discuss): handle message read events in client cache"
```

---

### Task 6: Mark Latest Visible Message as Read in `ChatWindow`

**Files:**
- Modify: `serp_web/src/modules/discuss/components/ChatWindow.tsx`

- [ ] **Step 1: Import REST fallback mutation**

Ensure `useMarkAsReadMutation` is imported from `../api/discussApi`:

```ts
useMarkAsReadMutation,
```

- [ ] **Step 2: Add mutation and mark-read refs**

Add mutation near other message mutations:

```ts
const [markAsRead] = useMarkAsReadMutation();
```

Add refs near the existing refs:

```ts
const lastMarkedReadRef = useRef<Record<string, string>>({});
const markReadInFlightRef = useRef<Record<string, string>>({});
```

- [ ] **Step 3: Add `markLatestMessageAsRead` callback**

Add after `const messages = allMessages;`:

```ts
const markLatestMessageAsRead = useCallback(
  async (messageId?: string) => {
    if (!messageId) {
      return;
    }

    const channelId = channel.id;
    if (lastMarkedReadRef.current[channelId] === messageId) {
      return;
    }
    if (markReadInFlightRef.current[channelId] === messageId) {
      return;
    }

    markReadInFlightRef.current[channelId] = messageId;

    try {
      if (wsApi?.isConnected) {
        wsApi.markAsRead(messageId);
      } else {
        await markAsRead({ channelId, messageId }).unwrap();
      }

      lastMarkedReadRef.current[channelId] = messageId;
      setUnreadCount(0);
      setLastReadMessageId(null);
    } catch (error) {
      console.error('[ChatWindow] Failed to mark latest message as read:', error);
    } finally {
      if (markReadInFlightRef.current[channelId] === messageId) {
        delete markReadInFlightRef.current[channelId];
      }
    }
  },
  [channel.id, markAsRead, wsApi]
);
```

- [ ] **Step 4: Mark read when loaded and near bottom**

Add effect after `messages` and `hasMore` declarations:

```ts
useEffect(() => {
  if (!isNearBottom || allMessages.length === 0) {
    return;
  }

  const latestMessage = allMessages.reduce((latest, message) => {
    if (!latest) {
      return message;
    }
    return new Date(message.createdAt).getTime() >
      new Date(latest.createdAt).getTime()
      ? message
      : latest;
  }, allMessages[0]);

  void markLatestMessageAsRead(latestMessage?.id);
}, [allMessages, isNearBottom, markLatestMessageAsRead]);
```

- [ ] **Step 5: Mark read when scroll button sends user to bottom**

In `scrollToBottom`, after clearing local unread state, add:

```ts
const latestMessage = allMessages.reduce((latest, message) => {
  if (!latest) {
    return message;
  }
  return new Date(message.createdAt).getTime() >
    new Date(latest.createdAt).getTime()
    ? message
    : latest;
}, allMessages[0]);
void markLatestMessageAsRead(latestMessage?.id);
```

Update the dependency array for `scrollToBottom`:

```ts
}, [allMessages, markLatestMessageAsRead]);
```

- [ ] **Step 6: Register read event callback**

Add effect near the existing WebSocket callback registrations:

```ts
useEffect(() => {
  wsApi.setOnMessageRead((payload) => {
    setAllMessages((prev) =>
      prev.map((message) => {
        if (message.id !== String(payload.messageId)) {
          return message;
        }

        const existingReadBy = new Set(message.readBy || []);
        const payloadReadBy = payload.readBy?.map(String) || [String(payload.userId)];
        for (const readerId of payloadReadBy) {
          existingReadBy.add(readerId);
        }

        return {
          ...message,
          readBy: Array.from(existingReadBy),
          readCount: payload.readCount ?? existingReadBy.size,
          isReadByMe: existingReadBy.has(currentUserId),
        };
      })
    );

    if (String(payload.userId) === currentUserId) {
      setUnreadCount(0);
      setLastReadMessageId(null);
    }
  });

  return () => wsApi.setOnMessageRead(undefined);
}, [currentUserId, wsApi]);
```

- [ ] **Step 7: Run frontend type check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: command exits `0`.

- [ ] **Step 8: Commit Task 6**

```bash
git add serp_web/src/modules/discuss/components/ChatWindow.tsx
git commit -m "feat(discuss): mark messages read from chat window"
```

---

### Task 7: Render Read Receipts on Own Messages

**Files:**
- Modify: `serp_web/src/modules/discuss/components/MessageItem.tsx`

- [ ] **Step 1: Add read receipt label helper**

Add helper above `export const MessageItem`:

```ts
const getReadReceiptLabel = (message: Message, currentUserId: string) => {
  const readUsers = (message.readByUsers || []).filter(
    (user) => user.id !== currentUserId
  );

  if (readUsers.length === 0) {
    const readerIds = (message.readBy || []).filter((id) => id !== currentUserId);
    if (readerIds.length === 0) {
      return null;
    }
    if (readerIds.length === 1) {
      return {
        label: 'Seen by 1 person',
        title: 'Seen by 1 person',
      };
    }
    return {
      label: `Seen by ${readerIds.length} people`,
      title: `Seen by ${readerIds.length} people`,
    };
  }

  if (readUsers.length === 1) {
    return {
      label: `Seen by ${readUsers[0].name}`,
      title: `Seen by ${readUsers[0].name}`,
    };
  }

  const [firstUser, ...otherUsers] = readUsers;
  return {
    label: `Seen by ${firstUser.name} +${otherUsers.length}`,
    title: `Seen by ${readUsers.map((user) => user.name).join(', ')}`,
  };
};
```

- [ ] **Step 2: Render read receipt in own-message metadata**

Inside `MessageItem`, after `senderAvatar`, add:

```ts
const readReceipt = isOwn
  ? getReadReceiptLabel(message, currentUserId)
  : null;
```

Replace the existing read receipt check icon block:

```tsx
{isOwn && <Check className='h-3.5 w-3.5 text-white/70' />}
```

with:

```tsx
{isOwn && (
  <span
    title={readReceipt?.title || 'Sent'}
    className='inline-flex items-center gap-1 text-xs text-white/75'
  >
    <Check className='h-3.5 w-3.5 text-white/70' />
    {readReceipt ? (
      <span className='max-w-[12rem] truncate'>{readReceipt.label}</span>
    ) : null}
  </span>
)}
```

- [ ] **Step 3: Run frontend lint and type check**

Run from `serp_web/`:

```bash
npm run lint
npm run type-check
```

Expected: both commands exit `0`.

- [ ] **Step 4: Commit Task 7**

```bash
git add serp_web/src/modules/discuss/components/MessageItem.tsx
git commit -m "feat(discuss): show read receipts on sent messages"
```

---

### Task 8: Final Verification

**Files:**
- Verify all changed backend and frontend files.

- [ ] **Step 1: Run backend focused tests**

Run from `discuss_service/`:

```bash
./mvnw -Dtest=MessageMapperTest,MessageUseCaseTest,DeliveryServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run backend compile**

Run from `discuss_service/`:

```bash
./mvnw -q -DskipTests compile
```

Expected: command exits `0`.

- [ ] **Step 3: Run frontend checks**

Run from `serp_web/`:

```bash
npm run lint
npm run type-check
```

Expected: both commands exit `0`.

- [ ] **Step 4: Inspect working tree**

Run from repo root:

```bash
git status --short
```

Expected: no unstaged or staged implementation changes remain.

- [ ] **Step 5: Manual behavior check**

Use two accounts in two browser sessions:

1. User A opens a group channel.
2. User B sends a message while User A is not viewing that channel.
3. User A sees the channel unread badge increment.
4. User A opens the channel and scrolls to the bottom.
5. User A's unread badge clears.
6. User B sees their sent message show `Seen by User A`.
7. Refresh User B's page and confirm the read receipt still appears.

If local infrastructure is unavailable, record this as a manual-verification gap in the final handoff and include the passing automated commands.
