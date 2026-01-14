/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelEntity
 */

package serp.project.discuss_service.core.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChannelEntity domain logic.
 * Tests factory methods, business logic, query methods, and validation.
 */
class ChannelEntityTest {

    // ==================== FACTORY METHOD TESTS ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("createDirect - should create DIRECT channel with correct properties")
        void testCreateDirect_ValidInput_CreatesDirectChannel() {
            // When
            ChannelEntity channel = ChannelEntity.createDirect(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );

            // Then
            assertNotNull(channel);
            assertEquals(ChannelType.DIRECT, channel.getType());
            assertTrue(channel.getIsPrivate());
            assertEquals(2, channel.getMemberCount());
            assertNotNull(channel.getCreatedAt());
            assertNotNull(channel.getUpdatedAt());
        }

        @Test
        @DisplayName("createDirect - should order user IDs consistently (smaller first)")
        void testCreateDirect_UserIds_OrderedConsistently() {
            // Given - different order of user IDs
            Long smallerId = 100L;
            Long largerId = 200L;

            // When - create with different orders
            ChannelEntity channel1 = ChannelEntity.createDirect(1L, smallerId, largerId);
            ChannelEntity channel2 = ChannelEntity.createDirect(1L, largerId, smallerId);

            // Then - both should have same creator and entityId
            assertEquals(channel1.getCreatedBy(), channel2.getCreatedBy());
            assertEquals(smallerId, channel1.getCreatedBy());
            assertEquals(largerId, channel1.getEntityId());
            assertEquals(channel1.getName(), channel2.getName());
        }

        @Test
        @DisplayName("createGroup - should create GROUP channel with given name")
        void testCreateGroup_ValidInput_CreatesGroupChannel() {
            // Given
            String name = "Engineering Team";
            String description = "Team discussion";

            // When
            ChannelEntity channel = ChannelEntity.createGroup(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    name,
                    description,
                    false
            );

            // Then
            assertNotNull(channel);
            assertEquals(ChannelType.GROUP, channel.getType());
            assertEquals(name, channel.getName());
            assertEquals(description, channel.getDescription());
            assertFalse(channel.getIsPrivate());
            assertEquals(1, channel.getMemberCount());
        }

        @Test
        @DisplayName("createGroup - should create private group when isPrivate=true")
        void testCreateGroup_PrivateFlag_SetsCorrectly() {
            // When
            ChannelEntity channel = ChannelEntity.createGroup(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Private Group",
                    "Secret",
                    true
            );

            // Then
            assertTrue(channel.getIsPrivate());
        }

        @Test
        @DisplayName("createTopic - should create TOPIC channel linked to entity")
        void testCreateTopic_ValidInput_CreatesTopicChannel() {
            // Given
            String entityType = "PROJECT";
            Long entityId = 500L;

            // When
            ChannelEntity channel = ChannelEntity.createTopic(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Project Discussion",
                    entityType,
                    entityId
            );

            // Then
            assertNotNull(channel);
            assertEquals(ChannelType.TOPIC, channel.getType());
            assertEquals(entityType, channel.getEntityType());
            assertEquals(entityId, channel.getEntityId());
            assertFalse(channel.getIsPrivate());
        }
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Nested
    @DisplayName("Business Logic - updateInfo")
    class UpdateInfoTests {

        @Test
        @DisplayName("updateInfo - should update name and description for GROUP channel")
        void testUpdateInfo_GroupChannel_UpdatesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            String newName = "New Name";
            String newDescription = "New Description";
            Long originalUpdatedAt = channel.getUpdatedAt();

            // When
            channel.updateInfo(newName, newDescription);

            // Then
            assertEquals(newName, channel.getName());
            assertEquals(newDescription, channel.getDescription());
            assertTrue(channel.getUpdatedAt() >= originalUpdatedAt);
        }

        @Test
        @DisplayName("updateInfo - should throw exception for DIRECT channel")
        void testUpdateInfo_DirectChannel_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> channel.updateInfo("New Name", "New Description")
            );
            assertTrue(exception.getMessage().contains("DIRECT"));
        }

        @Test
        @DisplayName("updateInfo - should throw exception for archived channel")
        void testUpdateInfo_ArchivedChannel_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createArchivedChannel();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> channel.updateInfo("New Name", "New Description")
            );
        }
    }

    @Nested
    @DisplayName("Business Logic - archive/unarchive")
    class ArchiveTests {

        @Test
        @DisplayName("archive - should archive active GROUP channel")
        void testArchive_ActiveGroupChannel_ArchivesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            assertFalse(channel.getIsArchived());

            // When
            channel.archive();

            // Then
            assertTrue(channel.getIsArchived());
        }

        @Test
        @DisplayName("archive - should throw exception if already archived")
        void testArchive_AlreadyArchived_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createArchivedChannel();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> channel.archive()
            );
            assertTrue(exception.getMessage().contains("already archived"));
        }

        @Test
        @DisplayName("archive - should throw exception for DIRECT channel")
        void testArchive_DirectChannel_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();

            // When/Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> channel.archive()
            );
            assertTrue(exception.getMessage().contains("DIRECT"));
        }

        @Test
        @DisplayName("unarchive - should unarchive archived channel")
        void testUnarchive_ArchivedChannel_UnarchivesSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createArchivedChannel();
            assertTrue(channel.getIsArchived());

            // When
            channel.unarchive();

            // Then
            assertFalse(channel.getIsArchived());
        }

        @Test
        @DisplayName("unarchive - should throw exception if not archived")
        void testUnarchive_NotArchived_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> channel.unarchive()
            );
        }
    }

    @Nested
    @DisplayName("Business Logic - message and member count")
    class CountTests {

        @Test
        @DisplayName("recordMessage - should increment message count and update lastMessageAt")
        void testRecordMessage_ValidChannel_IncrementsCount() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            int originalCount = channel.getMessageCount();

            // When
            channel.recordMessage();

            // Then
            assertEquals(originalCount + 1, channel.getMessageCount());
            assertNotNull(channel.getLastMessageAt());
        }

        @Test
        @DisplayName("recordMessage - should throw exception for archived channel")
        void testRecordMessage_ArchivedChannel_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createArchivedChannel();

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> channel.recordMessage()
            );
        }

        @Test
        @DisplayName("incrementMemberCount - should increment for GROUP channel")
        void testIncrementMemberCount_GroupChannel_IncrementsSuccessfully() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            int originalCount = channel.getMemberCount();

            // When
            channel.incrementMemberCount();

            // Then
            assertEquals(originalCount + 1, channel.getMemberCount());
        }

        @Test
        @DisplayName("incrementMemberCount - should throw for DIRECT channel with 2 members")
        void testIncrementMemberCount_DirectChannelMax2_ThrowsException() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();
            assertEquals(2, channel.getMemberCount());

            // When/Then
            assertThrows(
                    IllegalStateException.class,
                    () -> channel.incrementMemberCount()
            );
        }

        @Test
        @DisplayName("decrementMemberCount - should decrement when count > 0")
        void testDecrementMemberCount_PositiveCount_Decrements() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            channel.setMemberCount(5);

            // When
            channel.decrementMemberCount();

            // Then
            assertEquals(4, channel.getMemberCount());
        }

        @Test
        @DisplayName("decrementMemberCount - should not go below 0")
        void testDecrementMemberCount_ZeroCount_StaysAtZero() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            channel.setMemberCount(0);

            // When
            channel.decrementMemberCount();

            // Then
            assertEquals(0, channel.getMemberCount());
        }
    }

    // ==================== QUERY METHOD TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("isOwner - should return true for creator")
        void testIsOwner_CreatorId_ReturnsTrue() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            // When/Then
            assertTrue(channel.isOwner(TestDataFactory.USER_ID_1));
        }

        @Test
        @DisplayName("isOwner - should return false for non-creator")
        void testIsOwner_OtherId_ReturnsFalse() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            // When/Then
            assertFalse(channel.isOwner(TestDataFactory.USER_ID_2));
        }

        @Test
        @DisplayName("isDirect - should return true for DIRECT channel")
        void testIsDirect_DirectChannel_ReturnsTrue() {
            ChannelEntity channel = TestDataFactory.createDirectChannel();
            assertTrue(channel.isDirect());
            assertFalse(channel.isGroup());
            assertFalse(channel.isTopic());
        }

        @Test
        @DisplayName("isGroup - should return true for GROUP channel")
        void testIsGroup_GroupChannel_ReturnsTrue() {
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            assertTrue(channel.isGroup());
            assertFalse(channel.isDirect());
            assertFalse(channel.isTopic());
        }

        @Test
        @DisplayName("isTopic - should return true for TOPIC channel")
        void testIsTopic_TopicChannel_ReturnsTrue() {
            ChannelEntity channel = TestDataFactory.createTopicChannel("PROJECT", 1L);
            assertTrue(channel.isTopic());
            assertFalse(channel.isDirect());
            assertFalse(channel.isGroup());
        }

        @Test
        @DisplayName("isActive - should return true when not archived")
        void testIsActive_NotArchived_ReturnsTrue() {
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            assertTrue(channel.isActive());
        }

        @Test
        @DisplayName("isActive - should return false when archived")
        void testIsActive_Archived_ReturnsFalse() {
            ChannelEntity channel = TestDataFactory.createArchivedChannel();
            assertFalse(channel.isActive());
        }

        @Test
        @DisplayName("getOtherUserId - should return other user for DIRECT channel")
        void testGetOtherUserId_DirectChannel_ReturnsCorrectUser() {
            // Given
            ChannelEntity channel = TestDataFactory.createDirectChannel();

            // When - asking from creator's perspective
            Optional<Long> otherUser = channel.getOtherUserId(TestDataFactory.USER_ID_1);

            // Then
            assertTrue(otherUser.isPresent());
            assertEquals(TestDataFactory.USER_ID_2, otherUser.get());
        }

        @Test
        @DisplayName("getOtherUserId - should return empty for non-DIRECT channel")
        void testGetOtherUserId_GroupChannel_ReturnsEmpty() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            // When
            Optional<Long> otherUser = channel.getOtherUserId(TestDataFactory.USER_ID_1);

            // Then
            assertTrue(otherUser.isEmpty());
        }

        @Test
        @DisplayName("isLinkedTo - should return true for matching entity")
        void testIsLinkedTo_MatchingEntity_ReturnsTrue() {
            // Given
            String entityType = "PROJECT";
            Long entityId = 500L;
            ChannelEntity channel = TestDataFactory.createTopicChannel(entityType, entityId);

            // When/Then
            assertTrue(channel.isLinkedTo(entityType, entityId));
        }

        @Test
        @DisplayName("isLinkedTo - should return false for non-matching entity")
        void testIsLinkedTo_NonMatchingEntity_ReturnsFalse() {
            // Given
            ChannelEntity channel = TestDataFactory.createTopicChannel("PROJECT", 500L);

            // When/Then
            assertFalse(channel.isLinkedTo("TASK", 500L));
            assertFalse(channel.isLinkedTo("PROJECT", 999L));
        }

        @Test
        @DisplayName("findMember - should find member by userId")
        void testFindMember_ExistingMember_ReturnsMember() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity member = TestDataFactory.createOwnerMember();
            channel.setMembers(new ArrayList<>());
            channel.getMembers().add(member);

            // When
            Optional<ChannelMemberEntity> found = channel.findMember(TestDataFactory.USER_ID_1);

            // Then
            assertTrue(found.isPresent());
            assertEquals(TestDataFactory.USER_ID_1, found.get().getUserId());
        }

        @Test
        @DisplayName("canManage - should return true for owner")
        void testCanManage_Owner_ReturnsTrue() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();

            // When/Then
            assertTrue(channel.canManage(TestDataFactory.USER_ID_1)); // creator
        }

        @Test
        @DisplayName("canManage - should return true for admin member")
        void testCanManage_AdminMember_ReturnsTrue() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            ChannelMemberEntity admin = TestDataFactory.createAdminMember();
            channel.setMembers(new ArrayList<>());
            channel.getMembers().add(admin);

            // When/Then
            assertTrue(channel.canManage(TestDataFactory.USER_ID_2));
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("validateForCreation - should throw when tenantId is null")
        void testValidateForCreation_MissingTenantId_ThrowsException() {
            // Given
            ChannelEntity channel = ChannelEntity.builder()
                    .createdBy(1L)
                    .name("Test")
                    .type(ChannelType.GROUP)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    channel::validateForCreation
            );
            assertTrue(exception.getMessage().contains("Tenant ID"));
        }

        @Test
        @DisplayName("validateForCreation - should throw when name is blank")
        void testValidateForCreation_BlankName_ThrowsException() {
            // Given
            ChannelEntity channel = ChannelEntity.builder()
                    .tenantId(1L)
                    .createdBy(1L)
                    .name("   ")
                    .type(ChannelType.GROUP)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    channel::validateForCreation
            );
            assertTrue(exception.getMessage().contains("name"));
        }

        @Test
        @DisplayName("validateForCreation - should throw when TOPIC missing entity")
        void testValidateForCreation_TopicMissingEntity_ThrowsException() {
            // Given
            ChannelEntity channel = ChannelEntity.builder()
                    .tenantId(1L)
                    .createdBy(1L)
                    .name("Topic")
                    .type(ChannelType.TOPIC)
                    .build();

            // When/Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    channel::validateForCreation
            );
            assertTrue(exception.getMessage().contains("Entity"));
        }

        @Test
        @DisplayName("validateForCreation - should pass for valid GROUP channel")
        void testValidateForCreation_ValidGroup_Passes() {
            // Given
            ChannelEntity channel = ChannelEntity.builder()
                    .tenantId(1L)
                    .createdBy(1L)
                    .name("Valid Group")
                    .type(ChannelType.GROUP)
                    .build();

            // When/Then - no exception
            assertDoesNotThrow(channel::validateForCreation);
        }

        @Test
        @DisplayName("validateForCreation - should pass for valid TOPIC channel")
        void testValidateForCreation_ValidTopic_Passes() {
            // Given
            ChannelEntity channel = ChannelEntity.builder()
                    .tenantId(1L)
                    .createdBy(1L)
                    .name("Valid Topic")
                    .type(ChannelType.TOPIC)
                    .entityType("PROJECT")
                    .entityId(100L)
                    .build();

            // When/Then - no exception
            assertDoesNotThrow(channel::validateForCreation);
        }
    }
}
