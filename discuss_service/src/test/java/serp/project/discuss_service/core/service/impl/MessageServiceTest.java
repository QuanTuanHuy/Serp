/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for MessageService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IMessagePort;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 * Tests all message business operations with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private IMessagePort messagePort;

    @Mock
    private IDiscussCacheService cacheService;

    @InjectMocks
    private MessageService messageService;

    // ==================== SEND MESSAGE TESTS ====================

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("should send valid message")
        void testSendMessage_ValidMessage_SavesAndCaches() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(null); // New message
            
            MessageEntity saved = TestDataFactory.createTextMessage();
            saved.setId(1L);
            when(messagePort.save(any(MessageEntity.class))).thenReturn(saved);

            // When
            MessageEntity result = messageService.sendMessage(message);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(messagePort).save(message);
        }

        @Test
        @DisplayName("should throw exception for invalid message")
        void testSendMessage_InvalidMessage_ThrowsException() {
            // Given - message without required fields
            MessageEntity message = MessageEntity.builder()
                    .content("Test")
                    .build(); // Missing channelId, senderId, tenantId

            // When/Then
            assertThrows(IllegalArgumentException.class,
                    () -> messageService.sendMessage(message));

            verify(messagePort, never()).save(any());
            verify(cacheService, never()).cacheMessage(any());
        }
    }

    // ==================== SEND REPLY TESTS ====================

    @Nested
    @DisplayName("sendReply")
    class SendReplyTests {

        @Test
        @DisplayName("should send reply and increment parent thread count")
        void testSendReply_ValidReply_IncrementsParentThread() {
            // Given
            MessageEntity parent = TestDataFactory.createTextMessage();
            parent.setId(100L);
            parent.setThreadCount(0);
            
            MessageEntity reply = TestDataFactory.createTextMessage();
            reply.setId(null);
            
            MessageEntity savedReply = TestDataFactory.createTextMessage();
            savedReply.setId(101L);
            savedReply.setParentId(100L);

            when(cacheService.getCachedMessage(100L)).thenReturn(Optional.of(parent));
            when(messagePort.save(any(MessageEntity.class)))
                    .thenReturn(parent) // First save for parent
                    .thenReturn(savedReply); // Second save for reply

            // When
            MessageEntity result = messageService.sendReply(100L, reply);

            // Then
            assertNotNull(result);
            assertEquals(101L, result.getId());
            assertEquals(100L, result.getParentId());
            
            // Verify parent was saved with incremented thread count
            verify(messagePort, times(2)).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("should throw when parent message not found")
        void testSendReply_ParentNotFound_ThrowsException() {
            // Given
            MessageEntity reply = TestDataFactory.createTextMessage();
            when(cacheService.getCachedMessage(999L)).thenReturn(Optional.empty());
            when(messagePort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(AppException.class,
                    () -> messageService.sendReply(999L, reply));
        }
    }

    // ==================== GET MESSAGE TESTS ====================

    @Nested
    @DisplayName("getMessageById / getMessageByIdOrThrow")
    class GetMessageByIdTests {

        @Test
        @DisplayName("should return cached message on cache hit")
        void testGetMessageById_CacheHit_ReturnsCached() {
            // Given
            MessageEntity cached = TestDataFactory.createTextMessage();
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(cached));

            // When
            Optional<MessageEntity> result = messageService.getMessageById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(cached, result.get());
            verify(messagePort, never()).findById(any());
        }

        @Test
        @DisplayName("should query database on cache miss and cache result")
        void testGetMessageById_CacheMiss_QueriesDbAndCaches() {
            // Given
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.empty());
            MessageEntity fromDb = TestDataFactory.createTextMessage();
            when(messagePort.findById(1L)).thenReturn(Optional.of(fromDb));

            // When
            Optional<MessageEntity> result = messageService.getMessageById(1L);

            // Then
            assertTrue(result.isPresent());
            verify(messagePort).findById(1L);
            verify(cacheService).cacheMessage(fromDb);
        }

        @Test
        @DisplayName("should return empty when not found")
        void testGetMessageById_NotFound_ReturnsEmpty() {
            // Given
            when(cacheService.getCachedMessage(999L)).thenReturn(Optional.empty());
            when(messagePort.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<MessageEntity> result = messageService.getMessageById(999L);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getMessageByIdOrThrow should throw AppException when not found")
        void testGetMessageByIdOrThrow_NotFound_ThrowsAppException() {
            // Given
            when(cacheService.getCachedMessage(999L)).thenReturn(Optional.empty());
            when(messagePort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> messageService.getMessageByIdOrThrow(999L));

            assertEquals(ErrorCode.MESSAGE_NOT_FOUND.getMessage(), exception.getMessage());
        }
    }

    // ==================== QUERY METHODS TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("getMessagesByChannel should delegate to port with pagination")
        void testGetMessagesByChannel_DelegatesToPort() {
            // Given
            List<MessageEntity> messages = List.of(
                    TestDataFactory.createTextMessage(),
                    TestDataFactory.createTextMessage()
            );
            when(messagePort.findByChannelId(1L, 0, 20)).thenReturn(Pair.of(2L, messages));

            // When
            Pair<Long, List<MessageEntity>> result = messageService.getMessagesByChannel(1L, 0, 20);

            // Then
            assertEquals(2L, result.getFirst());
            assertEquals(2, result.getSecond().size());
        }

        @Test
        @DisplayName("getMessagesBefore should delegate to port for infinite scroll")
        void testGetMessagesBefore_DelegatesToPort() {
            // Given
            List<MessageEntity> messages = List.of(TestDataFactory.createTextMessage());
            when(messagePort.findBeforeId(1L, 100L, 20)).thenReturn(messages);

            // When
            List<MessageEntity> result = messageService.getMessagesBefore(1L, 100L, 20);

            // Then
            assertEquals(1, result.size());
            verify(messagePort).findBeforeId(1L, 100L, 20);
        }

        @Test
        @DisplayName("getThreadReplies should delegate to port")
        void testGetThreadReplies_DelegatesToPort() {
            // Given
            List<MessageEntity> replies = List.of(
                    TestDataFactory.createReplyMessage(100L),
                    TestDataFactory.createReplyMessage(100L)
            );
            when(messagePort.findReplies(100L)).thenReturn(replies);

            // When
            List<MessageEntity> result = messageService.getThreadReplies(100L);

            // Then
            assertEquals(2, result.size());
            verify(messagePort).findReplies(100L);
        }

        @Test
        @DisplayName("searchMessages should delegate to port")
        void testSearchMessages_DelegatesToPort() {
            // Given
            List<MessageEntity> results = List.of(TestDataFactory.createTextMessage("search term"));
            when(messagePort.searchMessages(1L, "search", 0, 20)).thenReturn(results);

            // When
            List<MessageEntity> result = messageService.searchMessages(1L, "search", 0, 20);

            // Then
            assertEquals(1, result.size());
            verify(messagePort).searchMessages(1L, "search", 0, 20);
        }
    }

    // ==================== EDIT MESSAGE TESTS ====================

    @Nested
    @DisplayName("editMessage")
    class EditMessageTests {

        @Test
        @DisplayName("should edit message content")
        void testEditMessage_ValidEdit_EditsAndCaches() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(message));

            MessageEntity edited = TestDataFactory.createTextMessage();
            edited.setId(1L);
            edited.setContent("Updated content");
            edited.setIsEdited(true);
            when(messagePort.save(any(MessageEntity.class))).thenReturn(edited);

            // When
            MessageEntity result = messageService.editMessage(1L, "Updated content", TestDataFactory.USER_ID_1);

            // Then
            assertNotNull(result);
            assertTrue(result.getIsEdited());
        }

        @Test
        @DisplayName("should throw when message not found")
        void testEditMessage_NotFound_ThrowsException() {
            // Given
            when(cacheService.getCachedMessage(999L)).thenReturn(Optional.empty());
            when(messagePort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(AppException.class,
                    () -> messageService.editMessage(999L, "New content", 1L));
        }
    }

    // ==================== DELETE MESSAGE TESTS ====================

    @Nested
    @DisplayName("deleteMessage")
    class DeleteMessageTests {

        @Test
        @DisplayName("should soft delete message")
        void testDeleteMessage_ValidDelete_DeletesAndInvalidates() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(message));

            MessageEntity deleted = TestDataFactory.createDeletedMessage();
            when(messagePort.save(any(MessageEntity.class))).thenReturn(deleted);

            // When
            MessageEntity result = messageService.deleteMessage(1L, TestDataFactory.USER_ID_1, false);

            // Then
            assertTrue(result.getIsDeleted());
        }

        @Test
        @DisplayName("should decrement parent thread count when deleting reply")
        void testDeleteMessage_ReplyMessage_DecrementsParentThread() {
            // Given
            MessageEntity reply = TestDataFactory.createReplyMessage(100L);
            reply.setId(101L);
            when(cacheService.getCachedMessage(101L)).thenReturn(Optional.of(reply));

            MessageEntity parent = TestDataFactory.createTextMessage();
            parent.setId(100L);
            parent.setThreadCount(5);
            when(cacheService.getCachedMessage(100L)).thenReturn(Optional.of(parent));

            MessageEntity deleted = TestDataFactory.createDeletedMessage();
            deleted.setParentId(100L);
            when(messagePort.save(any(MessageEntity.class))).thenReturn(deleted);

            // When
            messageService.deleteMessage(101L, TestDataFactory.USER_ID_1, false);

            // Then
            // Verify parent was saved with decremented thread count
            verify(messagePort, times(2)).save(any(MessageEntity.class));
        }
    }

    // ==================== REACTION TESTS ====================

    @Nested
    @DisplayName("addReaction / removeReaction")
    class ReactionTests {

        @Test
        @DisplayName("addReaction should add emoji")
        void testAddReaction_ValidEmoji_AddsAndCaches() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(message));

            MessageEntity withReaction = TestDataFactory.createTextMessage();
            when(messagePort.save(any(MessageEntity.class))).thenReturn(withReaction);

            // When
            MessageEntity result = messageService.addReaction(1L, TestDataFactory.USER_ID_1, "👍");

            // Then
            assertNotNull(result);
            verify(messagePort).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("removeReaction should remove emoji")
        void testRemoveReaction_ExistingEmoji_RemovesAndCaches() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            message.addReaction("👍", TestDataFactory.USER_ID_1);
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(message));

            MessageEntity withoutReaction = TestDataFactory.createTextMessage();
            when(messagePort.save(any(MessageEntity.class))).thenReturn(withoutReaction);

            // When
            MessageEntity result = messageService.removeReaction(1L, TestDataFactory.USER_ID_1, "👍");

            // Then
            assertNotNull(result);
        }
    }

    // ==================== READ RECEIPT TESTS ====================

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark message as read by user")
        void testMarkAsRead_ValidUser_MarksRead() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            when(cacheService.getCachedMessage(1L)).thenReturn(Optional.of(message));
            when(messagePort.save(any(MessageEntity.class))).thenReturn(message);

            // When
            messageService.markAsRead(1L, TestDataFactory.USER_ID_2);

            // Then
            verify(messagePort).save(any(MessageEntity.class));
        }
    }

    // ==================== COUNT TESTS ====================

    @Nested
    @DisplayName("countUnreadMessages")
    class CountUnreadTests {

        @Test
        @DisplayName("should delegate to port")
        void testCountUnreadMessages_DelegatesToPort() {
            // Given
            when(messagePort.countUnreadMessages(1L, 50L)).thenReturn(10L);

            // When
            long count = messageService.countUnreadMessages(1L, 50L);

            // Then
            assertEquals(10L, count);
            verify(messagePort).countUnreadMessages(1L, 50L);
        }
    }
}
