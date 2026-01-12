/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for MessageUseCase
 */

package serp.project.discuss_service.core.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IChannelService;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.IMessageService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageUseCase.
 * Tests orchestration of message operations across multiple services.
 */
@ExtendWith(MockitoExtension.class)
class MessageUseCaseTest {

    @Mock
    private IMessageService messageService;

    @Mock
    private IChannelService channelService;

    @Mock
    private IChannelMemberService memberService;

    @Mock
    private IDiscussEventPublisher eventPublisher;

    @Mock
    private IDiscussCacheService cacheService;

    @InjectMocks
    private MessageUseCase messageUseCase;

    // ==================== SEND MESSAGE TESTS ====================

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("should send message when user can send messages and channel is active")
        void testSendMessage_ValidRequest_SendsSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            MessageEntity saved = TestDataFactory.createTextMessage();

            when(memberService.canSendMessages(channel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(channelService.getChannelByIdOrThrow(channel.getId())).thenReturn(channel);
            when(messageService.sendMessage(any(MessageEntity.class))).thenReturn(saved);

            // When
            MessageEntity result = messageUseCase.sendMessage(
                    channel.getId(),
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    "Hello world",
                    null
            );

            // Then
            assertNotNull(result);
            verify(channelService).recordMessage(channel.getId());
            verify(memberService).incrementUnreadForChannel(channel.getId(), TestDataFactory.USER_ID_1);
            verify(eventPublisher).publishMessageSent(saved);
        }

        @Test
        @DisplayName("should throw when user cannot send messages")
        void testSendMessage_UserCannotSend_ThrowsException() {
            // Given
            when(memberService.canSendMessages(1L, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> messageUseCase.sendMessage(1L, TestDataFactory.USER_ID_3, 1L, "Hi", null));

            assertEquals(ErrorCode.CANNOT_SEND_MESSAGES.getMessage(), exception.getMessage());
            verify(messageService, never()).sendMessage(any());
        }

        @Test
        @DisplayName("should throw when channel is archived")
        void testSendMessage_ChannelArchived_ThrowsException() {
            // Given
            ChannelEntity archivedChannel = TestDataFactory.createArchivedChannel();

            when(memberService.canSendMessages(archivedChannel.getId(), TestDataFactory.USER_ID_1)).thenReturn(true);
            when(channelService.getChannelByIdOrThrow(archivedChannel.getId())).thenReturn(archivedChannel);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> messageUseCase.sendMessage(
                            archivedChannel.getId(),
                            TestDataFactory.USER_ID_1,
                            TestDataFactory.TENANT_ID,
                            "Message",
                            null
                    ));

            assertEquals(ErrorCode.CHANNEL_ARCHIVED.getMessage(), exception.getMessage());
        }
    }

    // ==================== SEND REPLY TESTS ====================

    @Nested
    @DisplayName("sendReply")
    class SendReplyTests {

        @Test
        @DisplayName("should send reply when parent is in same channel")
        void testSendReply_ValidParent_SendsReply() {
            // Given
            MessageEntity parent = TestDataFactory.createTextMessage();
            parent.setId(100L);
            parent.setChannelId(TestDataFactory.CHANNEL_ID);

            MessageEntity reply = TestDataFactory.createReplyMessage(100L);

            when(memberService.canSendMessages(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.getMessageByIdOrThrow(100L)).thenReturn(parent);
            when(messageService.sendReply(eq(100L), any(MessageEntity.class))).thenReturn(reply);

            // When
            MessageEntity result = messageUseCase.sendReply(
                    TestDataFactory.CHANNEL_ID,
                    100L,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    "Reply content",
                    null
            );

            // Then
            assertNotNull(result);
            verify(channelService).recordMessage(TestDataFactory.CHANNEL_ID);
            verify(eventPublisher).publishMessageSent(reply);
        }

        @Test
        @DisplayName("should throw when parent message is in different channel")
        void testSendReply_ParentInDifferentChannel_ThrowsException() {
            // Given
            MessageEntity parent = TestDataFactory.createTextMessage();
            parent.setId(100L);
            parent.setChannelId(999L); // Different channel

            when(memberService.canSendMessages(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.getMessageByIdOrThrow(100L)).thenReturn(parent);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> messageUseCase.sendReply(
                            TestDataFactory.CHANNEL_ID,
                            100L,
                            TestDataFactory.USER_ID_1,
                            TestDataFactory.TENANT_ID,
                            "Reply",
                            null
                    ));

            assertEquals(ErrorCode.PARENT_MESSAGE_NOT_IN_CHANNEL.getMessage(), exception.getMessage());
        }
    }

    // ==================== GET CHANNEL MESSAGES TESTS ====================

    @Nested
    @DisplayName("getChannelMessages")
    class GetChannelMessagesTests {

        @Test
        @DisplayName("should return messages when user is member")
        void testGetChannelMessages_UserIsMember_ReturnsMessages() {
            // Given
            List<MessageEntity> messages = List.of(TestDataFactory.createTextMessage());

            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.getMessagesByChannel(TestDataFactory.CHANNEL_ID, 0, 20))
                    .thenReturn(Pair.of(1L, messages));

            // When
            Pair<Long, List<MessageEntity>> result = messageUseCase.getChannelMessages(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    0,
                    20
            );

            // Then
            assertEquals(1L, result.getFirst());
            assertEquals(1, result.getSecond().size());
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testGetChannelMessages_UserNotMember_ThrowsException() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> messageUseCase.getChannelMessages(
                            TestDataFactory.CHANNEL_ID,
                            TestDataFactory.USER_ID_3,
                            0,
                            20
                    ));

            assertEquals(ErrorCode.NOT_CHANNEL_MEMBER.getMessage(), exception.getMessage());
        }
    }

    // ==================== GET MESSAGES BEFORE TESTS ====================

    @Nested
    @DisplayName("getMessagesBefore")
    class GetMessagesBeforeTests {

        @Test
        @DisplayName("should return messages before specified ID")
        void testGetMessagesBefore_UserIsMember_ReturnsMessages() {
            // Given
            List<MessageEntity> messages = List.of(TestDataFactory.createTextMessage());

            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.getMessagesBefore(TestDataFactory.CHANNEL_ID, 100L, 20))
                    .thenReturn(messages);

            // When
            List<MessageEntity> result = messageUseCase.getMessagesBefore(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    100L,
                    20
            );

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testGetMessagesBefore_UserNotMember_ThrowsException() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.getMessagesBefore(
                            TestDataFactory.CHANNEL_ID,
                            TestDataFactory.USER_ID_3,
                            100L,
                            20
                    ));
        }
    }

    // ==================== GET THREAD REPLIES TESTS ====================

    @Nested
    @DisplayName("getThreadReplies")
    class GetThreadRepliesTests {

        @Test
        @DisplayName("should return thread replies when user is member")
        void testGetThreadReplies_UserIsMember_ReturnsReplies() {
            // Given
            List<MessageEntity> replies = List.of(TestDataFactory.createReplyMessage(100L));

            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.getThreadReplies(100L)).thenReturn(replies);

            // When
            List<MessageEntity> result = messageUseCase.getThreadReplies(
                    TestDataFactory.CHANNEL_ID, 100L, TestDataFactory.USER_ID_1);

            // Then
            assertEquals(1, result.size());
        }
    }

    // ==================== SEARCH MESSAGES TESTS ====================

    @Nested
    @DisplayName("searchMessages")
    class SearchMessagesTests {

        @Test
        @DisplayName("should search messages when user is member")
        void testSearchMessages_UserIsMember_ReturnsResults() {
            // Given
            List<MessageEntity> results = List.of(TestDataFactory.createTextMessage("search term"));

            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.searchMessages(TestDataFactory.CHANNEL_ID, "search", 0, 20))
                    .thenReturn(results);

            // When
            List<MessageEntity> result = messageUseCase.searchMessages(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    "search",
                    0,
                    20
            );

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testSearchMessages_UserNotMember_ThrowsException() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.searchMessages(
                            TestDataFactory.CHANNEL_ID,
                            TestDataFactory.USER_ID_3,
                            "query",
                            0,
                            20
                    ));
        }
    }

    // ==================== EDIT MESSAGE TESTS ====================

    @Nested
    @DisplayName("editMessage")
    class EditMessageTests {

        @Test
        @DisplayName("should edit message when user is member")
        void testEditMessage_UserIsMember_EditsSuccessfully() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            MessageEntity edited = TestDataFactory.createTextMessage();
            edited.setIsEdited(true);

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.editMessage(1L, "New content", TestDataFactory.USER_ID_1)).thenReturn(edited);

            // When
            MessageEntity result = messageUseCase.editMessage(1L, TestDataFactory.USER_ID_1, "New content");

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishMessageUpdated(edited);
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testEditMessage_UserNotMember_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.editMessage(1L, TestDataFactory.USER_ID_3, "New content"));
        }
    }

    // ==================== DELETE MESSAGE TESTS ====================

    @Nested
    @DisplayName("deleteMessage")
    class DeleteMessageTests {

        @Test
        @DisplayName("should delete message and publish event")
        void testDeleteMessage_ValidRequest_DeletesSuccessfully() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            MessageEntity deleted = TestDataFactory.createDeletedMessage();

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.canManageChannel(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.deleteMessage(1L, TestDataFactory.USER_ID_1, true)).thenReturn(deleted);

            // When
            MessageEntity result = messageUseCase.deleteMessage(1L, TestDataFactory.USER_ID_1);

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishMessageDeleted(deleted);
        }
    }

    // ==================== REACTION TESTS ====================

    @Nested
    @DisplayName("addReaction / removeReaction")
    class ReactionTests {

        @Test
        @DisplayName("addReaction should add reaction when user is member")
        void testAddReaction_UserIsMember_AddsReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.addReaction(1L, TestDataFactory.USER_ID_1, "👍")).thenReturn(message);

            // When
            MessageEntity result = messageUseCase.addReaction(1L, TestDataFactory.USER_ID_1, "👍");

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishReactionAdded(1L, TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, "👍");
        }

        @Test
        @DisplayName("addReaction should throw when user is not member")
        void testAddReaction_UserNotMember_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.addReaction(1L, TestDataFactory.USER_ID_3, "👍"));
        }

        @Test
        @DisplayName("removeReaction should remove reaction when user is member")
        void testRemoveReaction_UserIsMember_RemovesReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setId(1L);
            message.setChannelId(TestDataFactory.CHANNEL_ID);

            when(messageService.getMessageByIdOrThrow(1L)).thenReturn(message);
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(messageService.removeReaction(1L, TestDataFactory.USER_ID_1, "👍")).thenReturn(message);

            // When
            MessageEntity result = messageUseCase.removeReaction(1L, TestDataFactory.USER_ID_1, "👍");

            // Then
            assertNotNull(result);
            verify(eventPublisher).publishReactionRemoved(1L, TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, "👍");
        }
    }

    // ==================== MARK AS READ TESTS ====================

    @Nested
    @DisplayName("markAsRead")
    class MarkAsReadTests {

        @Test
        @DisplayName("should mark messages as read when user is member")
        void testMarkAsRead_UserIsMember_MarksRead() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);

            // When
            messageUseCase.markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, 100L);

            // Then
            verify(memberService).markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, 100L);
            verify(messageService).markAsRead(100L, TestDataFactory.USER_ID_1);
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testMarkAsRead_UserNotMember_ThrowsException() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.markAsRead(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3, 100L));
        }
    }

    // ==================== TYPING INDICATOR TESTS ====================

    @Nested
    @DisplayName("sendTypingIndicator")
    class TypingIndicatorTests {

        @Test
        @DisplayName("should set typing when user is member and isTyping=true")
        void testSendTypingIndicator_TypingTrue_SetsTyping() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);

            // When
            messageUseCase.sendTypingIndicator(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, true);

            // Then
            verify(cacheService).setUserTyping(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);
            verify(eventPublisher).publishTypingIndicator(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, true);
        }

        @Test
        @DisplayName("should clear typing when isTyping=false")
        void testSendTypingIndicator_TypingFalse_ClearsTyping() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);

            // When
            messageUseCase.sendTypingIndicator(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, false);

            // Then
            verify(cacheService).clearUserTyping(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);
            verify(eventPublisher).publishTypingIndicator(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1, false);
        }

        @Test
        @DisplayName("should silently ignore if user is not member")
        void testSendTypingIndicator_UserNotMember_IgnoresSilently() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When - should not throw
            messageUseCase.sendTypingIndicator(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3, true);

            // Then
            verify(cacheService, never()).setUserTyping(any(), any());
            verify(eventPublisher, never()).publishTypingIndicator(any(), any(), anyBoolean());
        }
    }

    // ==================== GET TYPING USERS TESTS ====================

    @Nested
    @DisplayName("getTypingUsers")
    class GetTypingUsersTests {

        @Test
        @DisplayName("should return typing users when user is member")
        void testGetTypingUsers_UserIsMember_ReturnsTypingUsers() {
            // Given
            Set<Long> typingUsers = Set.of(100L, 200L);

            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1)).thenReturn(true);
            when(cacheService.getTypingUsers(TestDataFactory.CHANNEL_ID)).thenReturn(typingUsers);

            // When
            Set<Long> result = messageUseCase.getTypingUsers(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_1);

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should throw when user is not member")
        void testGetTypingUsers_UserNotMember_ThrowsException() {
            // Given
            when(memberService.isMember(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3)).thenReturn(false);

            // When/Then
            assertThrows(AppException.class,
                    () -> messageUseCase.getTypingUsers(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3));
        }
    }

    // ==================== GET UNREAD COUNT TESTS ====================

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCountTests {

        @Test
        @DisplayName("should return unread count based on last read message")
        void testGetUnreadCount_WithLastReadId_ReturnsCount() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            member.setLastReadMsgId(50L);

            when(memberService.getMemberOrThrow(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(member);
            when(messageService.countUnreadMessages(TestDataFactory.CHANNEL_ID, 50L)).thenReturn(10L);

            // When
            long count = messageUseCase.getUnreadCount(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            assertEquals(10L, count);
        }

        @Test
        @DisplayName("should count from beginning if no last read message")
        void testGetUnreadCount_NoLastReadId_CountsFromZero() {
            // Given
            ChannelMemberEntity member = TestDataFactory.createRegularMember();
            member.setLastReadMsgId(null);

            when(memberService.getMemberOrThrow(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3))
                    .thenReturn(member);
            when(messageService.countUnreadMessages(TestDataFactory.CHANNEL_ID, 0L)).thenReturn(25L);

            // When
            long count = messageUseCase.getUnreadCount(TestDataFactory.CHANNEL_ID, TestDataFactory.USER_ID_3);

            // Then
            assertEquals(25L, count);
        }
    }
}
