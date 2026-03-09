/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for MessageEntity
 */

package serp.project.discuss_service.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.enums.MessageType;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageEntity domain logic.
 * Tests factory methods, business logic, query methods, and validation.
 */
class MessageEntityTest {

    // ==================== FACTORY METHOD TESTS ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("createText - should create TEXT message with correct properties")
        void testCreateText_ValidInput_CreatesTextMessage() {
            // Given
            String content = "Hello, World!";
            List<Long> mentions = List.of(200L, 300L);

            // When
            MessageEntity message = MessageEntity.createText(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    content,
                    mentions
            );

            // Then
            assertNotNull(message);
            assertEquals(MessageType.STANDARD, message.getMessageType());
            assertEquals(content, message.getContent());
            assertEquals(TestDataFactory.CHANNEL_ID, message.getChannelId());
            assertEquals(TestDataFactory.USER_ID_1, message.getSenderId());
            assertEquals(mentions, message.getMentions());
            assertFalse(message.getIsEdited());
            assertFalse(message.getIsDeleted());
            assertNotNull(message.getCreatedAt());
        }

        @Test
        @DisplayName("createText - should handle null mentions gracefully")
        void testCreateText_NullMentions_CreatesEmptyMentionsList() {
            // When
            MessageEntity message = MessageEntity.createText(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    "Test",
                    null
            );

            // Then
            assertNotNull(message.getMentions());
            assertTrue(message.getMentions().isEmpty());
        }

        @Test
        @DisplayName("createSystem - should create SYSTEM message with senderId=0")
        void testCreateSystem_ValidInput_CreatesSystemMessage() {
            // Given
            String content = "User joined the channel";

            // When
            MessageEntity message = MessageEntity.createSystem(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.TENANT_ID,
                    content
            );

            // Then
            assertNotNull(message);
            assertEquals(MessageType.SYSTEM, message.getMessageType());
            assertEquals(0L, message.getSenderId());
            assertEquals(content, message.getContent());
        }

        @Test
        @DisplayName("createReply - should create message with parentId set")
        void testCreateReply_WithParentId_CreatesReplyMessage() {
            // Given
            Long parentId = 999L;

            // When
            MessageEntity message = MessageEntity.createReply(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    "This is a reply",
                    parentId,
                    null
            );

            // Then
            assertNotNull(message);
            assertEquals(parentId, message.getParentId());
            assertTrue(message.isReply());
        }

        @Test
        @DisplayName("createWithAttachments - should create message for file attachments")
        void testCreateWithAttachments_ValidInput_CreatesMessageForAttachments() {
            // When
            MessageEntity message = MessageEntity.createWithAttachments(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    "Check this image",
                    null
            );

            // Then
            assertNotNull(message);
            assertEquals(MessageType.STANDARD, message.getMessageType());
            assertEquals("Check this image", message.getContent());
        }

        @Test
        @DisplayName("createWithAttachments - should allow null content for file-only messages")
        void testCreateWithAttachments_NullContent_AllowsEmptyContent() {
            // When
            MessageEntity message = MessageEntity.createWithAttachments(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    null,
                    null
            );

            // Then
            assertNotNull(message);
            assertEquals(MessageType.STANDARD, message.getMessageType());
            assertEquals("", message.getContent());
        }
    }

    // ==================== EDIT TESTS ====================

    @Nested
    @DisplayName("Business Logic - edit")
    class EditTests {

        @Test
        @DisplayName("edit - should update content and set isEdited flag")
        void testEdit_OwnerEdits_UpdatesContentAndIsEdited() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String newContent = "Updated content";
            Long originalUpdatedAt = message.getUpdatedAt();

            // When
            message.edit(newContent, TestDataFactory.USER_ID_1);

            // Then
            assertEquals(newContent, message.getContent());
            assertTrue(message.getIsEdited());
            assertNotNull(message.getEditedAt());
            assertTrue(message.getUpdatedAt() >= originalUpdatedAt);
        }

        @Test
        @DisplayName("edit - should throw exception when non-owner tries to edit")
        void testEdit_NonOwner_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            Long nonOwnerId = 999L;

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> message.edit("New content", nonOwnerId)
            );
            assertTrue(exception.getMessage().contains("sender"));
        }

        @Test
        @DisplayName("edit - should throw exception for deleted message")
        void testEdit_DeletedMessage_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createDeletedMessage();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> message.edit("New content", TestDataFactory.USER_ID_1)
            );
        }

        @Test
        @DisplayName("edit - should throw exception for system message")
        void testEdit_SystemMessage_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createSystemMessage();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> message.edit("New content", 0L)
            );
            assertTrue(exception.getMessage().contains("system"));
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Business Logic - delete")
    class DeleteTests {

        @Test
        @DisplayName("delete - owner should be able to delete own message")
        void testDelete_OwnerDeletes_SoftDeletesSuccessfully() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();

            // When
            message.delete(TestDataFactory.USER_ID_1, false);

            // Then
            assertTrue(message.getIsDeleted());
            assertNotNull(message.getDeletedAt());
            assertEquals(TestDataFactory.USER_ID_1, message.getDeletedBy());
        }

        @Test
        @DisplayName("delete - admin should be able to delete any message")
        void testDelete_AdminDeletes_SoftDeletesSuccessfully() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            Long adminId = 999L;

            // When
            message.delete(adminId, true);

            // Then
            assertTrue(message.getIsDeleted());
            assertEquals(adminId, message.getDeletedBy());
        }

        @Test
        @DisplayName("delete - non-owner non-admin should not be able to delete")
        void testDelete_NonOwnerNonAdmin_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            Long otherId = 999L;

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> message.delete(otherId, false)
            );
        }

        @Test
        @DisplayName("delete - should throw exception for already deleted message")
        void testDelete_AlreadyDeleted_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createDeletedMessage();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> message.delete(TestDataFactory.USER_ID_1, false)
            );
        }
    }

    // ==================== REACTION TESTS ====================

    @Nested
    @DisplayName("Business Logic - reactions")
    class ReactionTests {

        @Test
        @DisplayName("addReaction - should add new reaction with user")
        void testAddReaction_NewEmoji_AddsReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String emoji = "👍";

            // When
            message.addReaction(emoji, TestDataFactory.USER_ID_1);

            // Then
            assertTrue(message.hasReactions());
            assertEquals(1, message.getReactionCount());
        }

        @Test
        @DisplayName("addReaction - should add user to existing reaction")
        void testAddReaction_ExistingEmoji_AddsUserToReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String emoji = "👍";
            message.addReaction(emoji, TestDataFactory.USER_ID_1);

            // When
            message.addReaction(emoji, TestDataFactory.USER_ID_2);

            // Then
            assertEquals(2, message.getReactionCount());
            assertEquals(1, message.getReactions().size()); // Still one emoji type
        }

        @Test
        @DisplayName("addReaction - same user adding same emoji should not duplicate")
        void testAddReaction_SameUserSameEmoji_NoDuplicate() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String emoji = "👍";
            message.addReaction(emoji, TestDataFactory.USER_ID_1);

            // When
            message.addReaction(emoji, TestDataFactory.USER_ID_1);

            // Then
            assertEquals(1, message.getReactionCount());
        }

        @Test
        @DisplayName("addReaction - should throw for deleted message")
        void testAddReaction_DeletedMessage_ThrowsException() {
            // Given
            MessageEntity message = TestDataFactory.createDeletedMessage();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> message.addReaction("👍", TestDataFactory.USER_ID_1)
            );
        }

        @Test
        @DisplayName("removeReaction - should remove user from reaction")
        void testRemoveReaction_ExistingUser_RemovesFromReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String emoji = "👍";
            message.addReaction(emoji, TestDataFactory.USER_ID_1);
            message.addReaction(emoji, TestDataFactory.USER_ID_2);
            assertEquals(2, message.getReactionCount());

            // When
            message.removeReaction(emoji, TestDataFactory.USER_ID_1);

            // Then
            assertEquals(1, message.getReactionCount());
        }

        @Test
        @DisplayName("removeReaction - should remove entire reaction when last user")
        void testRemoveReaction_LastUser_RemovesEntireReaction() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            String emoji = "👍";
            message.addReaction(emoji, TestDataFactory.USER_ID_1);

            // When
            message.removeReaction(emoji, TestDataFactory.USER_ID_1);

            // Then
            assertFalse(message.hasReactions());
            assertEquals(0, message.getReactionCount());
        }

        @Test
        @DisplayName("removeReaction - should do nothing for non-existing reaction")
        void testRemoveReaction_NonExisting_DoesNothing() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();

            // When/Then - should not throw
            assertDoesNotThrow(() -> message.removeReaction("👍", TestDataFactory.USER_ID_1));
        }
    }

    // ==================== THREADING TESTS ====================

    @Nested
    @DisplayName("Business Logic - threading")
    class ThreadingTests {

        @Test
        @DisplayName("incrementThreadCount - should increment count")
        void testIncrementThreadCount_ValidMessage_IncrementsCount() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            assertEquals(0, message.getThreadCount());

            // When
            message.incrementThreadCount();

            // Then
            assertEquals(1, message.getThreadCount());
            assertTrue(message.hasThread());
        }

        @Test
        @DisplayName("decrementThreadCount - should decrement when positive")
        void testDecrementThreadCount_PositiveCount_DecrementsCount() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.incrementThreadCount();
            message.incrementThreadCount();
            assertEquals(2, message.getThreadCount());

            // When
            message.decrementThreadCount();

            // Then
            assertEquals(1, message.getThreadCount());
        }

        @Test
        @DisplayName("decrementThreadCount - should not go below zero")
        void testDecrementThreadCount_ZeroCount_StaysAtZero() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            assertEquals(0, message.getThreadCount());

            // When
            message.decrementThreadCount();

            // Then
            assertEquals(0, message.getThreadCount());
        }
    }

    // ==================== READ RECEIPT TESTS ====================

    @Nested
    @DisplayName("Business Logic - read receipts")
    class ReadReceiptTests {

        @Test
        @DisplayName("markReadBy - should add user to read list")
        void testMarkReadBy_NewUser_AddsToReadList() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();

            // When
            message.markReadBy(TestDataFactory.USER_ID_2);

            // Then
            assertTrue(message.isReadBy(TestDataFactory.USER_ID_2));
            assertEquals(1, message.getReadCount());
        }

        @Test
        @DisplayName("markReadBy - should not add duplicate")
        void testMarkReadBy_ExistingUser_DoesNotDuplicate() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.markReadBy(TestDataFactory.USER_ID_2);

            // When
            message.markReadBy(TestDataFactory.USER_ID_2);

            // Then
            assertEquals(1, message.getReadCount());
        }
    }

    // ==================== QUERY METHOD TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("isReply - should return true when parentId is set")
        void testIsReply_HasParentId_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createReplyMessage(999L);
            assertTrue(message.isReply());
        }

        @Test
        @DisplayName("isReply - should return false when no parentId")
        void testIsReply_NoParentId_ReturnsFalse() {
            MessageEntity message = TestDataFactory.createTextMessage();
            assertFalse(message.isReply());
        }

        @Test
        @DisplayName("hasThread - should return true when threadCount > 0")
        void testHasThread_PositiveThreadCount_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createTextMessage();
            message.incrementThreadCount();
            assertTrue(message.hasThread());
        }

        @Test
        @DisplayName("hasMentions - should return true when mentions exist")
        void testHasMentions_WithMentions_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createMessageWithMentions(
                    List.of(TestDataFactory.USER_ID_2)
            );
            assertTrue(message.hasMentions());
        }

        @Test
        @DisplayName("mentions - should return true for mentioned user")
        void testMentions_UserInList_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createMessageWithMentions(
                    List.of(TestDataFactory.USER_ID_2)
            );
            assertTrue(message.mentions(TestDataFactory.USER_ID_2));
            assertFalse(message.mentions(TestDataFactory.USER_ID_3));
        }

        @Test
        @DisplayName("isSentBy - should return true for sender")
        void testIsSentBy_Sender_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createTextMessage();
            assertTrue(message.isSentBy(TestDataFactory.USER_ID_1));
            assertFalse(message.isSentBy(TestDataFactory.USER_ID_2));
        }

        @Test
        @DisplayName("isSystemMessage - should return true for SYSTEM type")
        void testIsSystemMessage_SystemType_ReturnsTrue() {
            MessageEntity message = TestDataFactory.createSystemMessage();
            assertTrue(message.isSystemMessage());
        }

        @Test
        @DisplayName("getReactionCount - should return total count across all emojis")
        void testGetReactionCount_MultipleReactions_ReturnsTotalCount() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.addReaction("👍", TestDataFactory.USER_ID_1);
            message.addReaction("👍", TestDataFactory.USER_ID_2);
            message.addReaction("❤️", TestDataFactory.USER_ID_1);

            // Then
            assertEquals(3, message.getReactionCount());
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("validateForCreation - should throw when channelId is null")
        void testValidateForCreation_MissingChannelId_ThrowsException() {
            // Given
            MessageEntity message = MessageEntity.builder()
                    .senderId(1L)
                    .tenantId(1L)
                    .content("Test")
                    .messageType(MessageType.STANDARD)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    message::validateForCreation
            );
            assertTrue(exception.getMessage().contains("Channel ID"));
        }

        @Test
        @DisplayName("validateForCreation - should throw when senderId is null")
        void testValidateForCreation_MissingSenderId_ThrowsException() {
            // Given
            MessageEntity message = MessageEntity.builder()
                    .channelId(1L)
                    .tenantId(1L)
                    .content("Test")
                    .messageType(MessageType.STANDARD)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    message::validateForCreation
            );
            assertTrue(exception.getMessage().contains("Sender ID"));
        }

        @Test
        @DisplayName("validateForCreation - STANDARD message with empty content passes (attachments validated at service layer)")
        void testValidateForCreation_StandardMessageEmptyContent_Passes() {
            // Given - STANDARD messages can have empty content if they have attachments
            // Content OR attachments validation is done at service layer
            MessageEntity message = MessageEntity.builder()
                    .channelId(1L)
                    .senderId(1L)
                    .tenantId(1L)
                    .content("   ")
                    .messageType(MessageType.STANDARD)
                    .build();

            // When/Then - validateForCreation passes, full validation happens at service layer
            assertDoesNotThrow(message::validateForCreation);
        }

        @Test
        @DisplayName("validateForCreation - SYSTEM message with empty content throws exception")
        void testValidateForCreation_SystemMessageEmptyContent_ThrowsException() {
            // Given - SYSTEM messages always require content
            MessageEntity message = MessageEntity.builder()
                    .channelId(1L)
                    .senderId(0L)
                    .tenantId(1L)
                    .content("   ")
                    .messageType(MessageType.SYSTEM)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    message::validateForCreation
            );
            assertTrue(exception.getMessage().contains("system"));
        }

        @Test
        @DisplayName("validateForCreation - should pass for valid TEXT message")
        void testValidateForCreation_ValidTextMessage_Passes() {
            // Given
            MessageEntity message = MessageEntity.builder()
                    .channelId(1L)
                    .senderId(1L)
                    .tenantId(1L)
                    .content("Valid content")
                    .messageType(MessageType.STANDARD)
                    .build();

            // When/Then
            assertDoesNotThrow(message::validateForCreation);
        }

        @Test
        @DisplayName("validateHasContentOrAttachments - should throw when both empty")
        void testValidateHasContentOrAttachments_BothEmpty_ThrowsException() {
            // Given
            MessageEntity message = MessageEntity.builder()
                    .channelId(1L)
                    .senderId(1L)
                    .tenantId(1L)
                    .content("")
                    .messageType(MessageType.STANDARD)
                    .attachments(new ArrayList<>())
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    message::validateHasContentOrAttachments
            );
            assertTrue(exception.getMessage().contains("content or attachments"));
        }
    }

    // ==================== REACTION VALUE OBJECT TESTS ====================

    @Nested
    @DisplayName("ReactionVO Value Object")
    class ReactionVOTests {

        @Test
        @DisplayName("ReactionVO - should create with single user")
        void testReactionVO_CreateWithUser_HasOneUser() {
            // When
            MessageEntity.ReactionVO reaction = new MessageEntity.ReactionVO("👍", 1L);

            // Then
            assertEquals("👍", reaction.getEmoji());
            assertEquals(1, reaction.getUserIds().size());
            assertTrue(reaction.hasUser(1L));
        }

        @Test
        @DisplayName("ReactionVO - addUser should add new user")
        void testReactionVO_AddUser_AddsNewUser() {
            // Given
            MessageEntity.ReactionVO reaction = new MessageEntity.ReactionVO("👍", 1L);

            // When
            reaction.addUser(2L);

            // Then
            assertEquals(2, reaction.getUserIds().size());
            assertTrue(reaction.hasUser(2L));
        }

        @Test
        @DisplayName("ReactionVO - addUser should not add duplicate")
        void testReactionVO_AddDuplicate_DoesNotAdd() {
            // Given
            MessageEntity.ReactionVO reaction = new MessageEntity.ReactionVO("👍", 1L);

            // When
            reaction.addUser(1L);

            // Then
            assertEquals(1, reaction.getUserIds().size());
        }

        @Test
        @DisplayName("ReactionVO - removeUser should remove user")
        void testReactionVO_RemoveUser_RemovesUser() {
            // Given
            MessageEntity.ReactionVO reaction = new MessageEntity.ReactionVO("👍", 1L);
            reaction.addUser(2L);

            // When
            reaction.removeUser(1L);

            // Then
            assertFalse(reaction.hasUser(1L));
            assertTrue(reaction.hasUser(2L));
        }

        @Test
        @DisplayName("ReactionVO - isEmpty should return true when no users")
        void testReactionVO_NoUsers_IsEmpty() {
            // Given
            MessageEntity.ReactionVO reaction = new MessageEntity.ReactionVO("👍", 1L);

            // When
            reaction.removeUser(1L);

            // Then
            assertTrue(reaction.isEmpty());
        }
    }
}
