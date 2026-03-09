/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelMemberMapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;
import serp.project.discuss_service.infrastructure.store.model.ChannelMemberModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChannelMemberMapper Tests")
class ChannelMemberMapperTest {

    private ChannelMemberMapper channelMemberMapper;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long CHANNEL_ID = 1000L;
    private static final Long MEMBER_ID = 5000L;

    @BeforeEach
    void setUp() {
        channelMemberMapper = new ChannelMemberMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void shouldReturnNullWhenModelIsNull() {
            ChannelMemberEntity result = channelMemberMapper.toEntity(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from model to entity")
        void shouldMapAllFieldsFromModelToEntity() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("nickname", "TestUser");

            ChannelMemberModel model = ChannelMemberModel.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now.minusDays(10))
                    .leftAt(null)
                    .removedBy(null)
                    .lastReadMsgId(2000L)
                    .unreadCount(5)
                    .isMuted(false)
                    .isPinned(true)
                    .notificationLevel(NotificationLevel.MENTIONS)
                    .metadata(metadata)
                    .createdAt(now.minusDays(10))
                    .updatedAt(now)
                    .build();

            // When
            ChannelMemberEntity entity = channelMemberMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(MEMBER_ID, entity.getId());
            assertEquals(CHANNEL_ID, entity.getChannelId());
            assertEquals(USER_ID, entity.getUserId());
            assertEquals(TENANT_ID, entity.getTenantId());
            assertEquals(MemberRole.ADMIN, entity.getRole());
            assertEquals(MemberStatus.ACTIVE, entity.getStatus());
            assertNotNull(entity.getJoinedAt());
            assertNull(entity.getLeftAt());
            assertNull(entity.getRemovedBy());
            assertEquals(2000L, entity.getLastReadMsgId());
            assertEquals(5, entity.getUnreadCount());
            assertFalse(entity.getIsMuted());
            assertTrue(entity.getIsPinned());
            assertEquals(NotificationLevel.MENTIONS, entity.getNotificationLevel());
            assertEquals(metadata, entity.getMetadata());
        }

        @Test
        @DisplayName("Should handle null timestamps correctly")
        void shouldHandleNullTimestamps() {
            // Given
            ChannelMemberModel model = ChannelMemberModel.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(null)
                    .leftAt(null)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            ChannelMemberEntity entity = channelMemberMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertNull(entity.getJoinedAt());
            assertNull(entity.getLeftAt());
            assertNull(entity.getCreatedAt());
            assertNull(entity.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle left member status")
        void shouldHandleLeftMemberStatus() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChannelMemberModel model = ChannelMemberModel.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.LEFT)
                    .joinedAt(now.minusDays(30))
                    .leftAt(now.minusDays(1))
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(now.minusDays(30))
                    .updatedAt(now.minusDays(1))
                    .build();

            // When
            ChannelMemberEntity entity = channelMemberMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(MemberStatus.LEFT, entity.getStatus());
            assertNotNull(entity.getLeftAt());
        }

        @Test
        @DisplayName("Should handle removed member with removedBy field")
        void shouldHandleRemovedMember() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Long adminUserId = 999L;
            
            ChannelMemberModel model = ChannelMemberModel.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.REMOVED)
                    .joinedAt(now.minusDays(30))
                    .leftAt(now)
                    .removedBy(adminUserId)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(now.minusDays(30))
                    .updatedAt(now)
                    .build();

            // When
            ChannelMemberEntity entity = channelMemberMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(MemberStatus.REMOVED, entity.getStatus());
            assertEquals(adminUserId, entity.getRemovedBy());
        }
    }

    @Nested
    @DisplayName("toModel() Tests")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            ChannelMemberModel result = channelMemberMapper.toModel(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from entity to model")
        void shouldMapAllFieldsFromEntityToModel() {
            // Given
            long now = Instant.now().toEpochMilli();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("color", "blue");

            ChannelMemberEntity entity = ChannelMemberEntity.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.OWNER)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now - 86400000 * 7)
                    .leftAt(null)
                    .removedBy(null)
                    .lastReadMsgId(3000L)
                    .unreadCount(0)
                    .isMuted(true)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.NONE)
                    .metadata(metadata)
                    .createdAt(now - 86400000 * 7)
                    .updatedAt(now)
                    .build();

            // When
            ChannelMemberModel model = channelMemberMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertEquals(MEMBER_ID, model.getId());
            assertEquals(CHANNEL_ID, model.getChannelId());
            assertEquals(USER_ID, model.getUserId());
            assertEquals(TENANT_ID, model.getTenantId());
            assertEquals(MemberRole.OWNER, model.getRole());
            assertEquals(MemberStatus.ACTIVE, model.getStatus());
            assertNotNull(model.getJoinedAt());
            assertNull(model.getLeftAt());
            assertNull(model.getRemovedBy());
            assertEquals(3000L, model.getLastReadMsgId());
            assertEquals(0, model.getUnreadCount());
            assertTrue(model.getIsMuted());
            assertFalse(model.getIsPinned());
            assertEquals(NotificationLevel.NONE, model.getNotificationLevel());
            assertEquals(metadata, model.getMetadata());
        }

        @Test
        @DisplayName("Should handle null timestamps correctly")
        void shouldHandleNullTimestamps() {
            // Given
            ChannelMemberEntity entity = ChannelMemberEntity.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.GUEST)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(null)
                    .leftAt(null)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            ChannelMemberModel model = channelMemberMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertNull(model.getJoinedAt());
            assertNull(model.getLeftAt());
            assertNull(model.getCreatedAt());
            assertNull(model.getUpdatedAt());
        }

        @Test
        @DisplayName("Should correctly convert epoch millis to LocalDateTime")
        void shouldConvertEpochMillisToLocalDateTime() {
            // Given
            long timestamp = 1736676600000L;
            LocalDateTime expected = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

            ChannelMemberEntity entity = ChannelMemberEntity.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(timestamp)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(timestamp)
                    .updatedAt(timestamp)
                    .build();

            // When
            ChannelMemberModel model = channelMemberMapper.toModel(entity);

            // Then
            assertEquals(expected, model.getJoinedAt());
            assertEquals(expected, model.getCreatedAt());
            assertEquals(expected, model.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toEntityList() Tests")
    class ToEntityListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<ChannelMemberEntity> result = channelMemberMapper.toEntityList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<ChannelMemberEntity> result = channelMemberMapper.toEntityList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of models to entities")
        void shouldConvertListOfModelsToEntities() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChannelMemberModel model1 = ChannelMemberModel.builder()
                    .id(1L)
                    .channelId(CHANNEL_ID)
                    .userId(100L)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.OWNER)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(true)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            ChannelMemberModel model2 = ChannelMemberModel.builder()
                    .id(2L)
                    .channelId(CHANNEL_ID)
                    .userId(200L)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now)
                    .unreadCount(10)
                    .isMuted(true)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.MENTIONS)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<ChannelMemberModel> models = Arrays.asList(model1, model2);

            // When
            List<ChannelMemberEntity> entities = channelMemberMapper.toEntityList(models);

            // Then
            assertNotNull(entities);
            assertEquals(2, entities.size());
            assertEquals(1L, entities.get(0).getId());
            assertEquals(MemberRole.OWNER, entities.get(0).getRole());
            assertEquals(100L, entities.get(0).getUserId());
            assertEquals(2L, entities.get(1).getId());
            assertEquals(MemberRole.MEMBER, entities.get(1).getRole());
            assertEquals(200L, entities.get(1).getUserId());
        }
    }

    @Nested
    @DisplayName("toModelList() Tests")
    class ToModelListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<ChannelMemberModel> result = channelMemberMapper.toModelList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<ChannelMemberModel> result = channelMemberMapper.toModelList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of entities to models")
        void shouldConvertListOfEntitiesToModels() {
            // Given
            long now = Instant.now().toEpochMilli();
            ChannelMemberEntity entity1 = ChannelMemberEntity.builder()
                    .id(1L)
                    .channelId(CHANNEL_ID)
                    .userId(100L)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now)
                    .unreadCount(5)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.ALL)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            ChannelMemberEntity entity2 = ChannelMemberEntity.builder()
                    .id(2L)
                    .channelId(CHANNEL_ID)
                    .userId(200L)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.GUEST)
                    .status(MemberStatus.LEFT)
                    .joinedAt(now - 86400000)
                    .leftAt(now)
                    .unreadCount(0)
                    .isMuted(false)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.NONE)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            List<ChannelMemberEntity> entities = Arrays.asList(entity1, entity2);

            // When
            List<ChannelMemberModel> models = channelMemberMapper.toModelList(entities);

            // Then
            assertNotNull(models);
            assertEquals(2, models.size());
            assertEquals(1L, models.get(0).getId());
            assertEquals(MemberRole.ADMIN, models.get(0).getRole());
            assertEquals(MemberStatus.ACTIVE, models.get(0).getStatus());
            assertEquals(2L, models.get(1).getId());
            assertEquals(MemberRole.GUEST, models.get(1).getRole());
            assertEquals(MemberStatus.LEFT, models.get(1).getStatus());
        }
    }

    @Nested
    @DisplayName("Round-trip Conversion Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve all data through entity -> model -> entity conversion")
        void shouldPreserveDataThroughRoundTrip() {
            // Given
            long now = Instant.now().toEpochMilli();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("theme", "dark");

            ChannelMemberEntity original = ChannelMemberEntity.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .joinedAt(now - 86400000 * 30)
                    .leftAt(null)
                    .removedBy(null)
                    .lastReadMsgId(5000L)
                    .unreadCount(3)
                    .isMuted(false)
                    .isPinned(true)
                    .notificationLevel(NotificationLevel.MENTIONS)
                    .metadata(metadata)
                    .createdAt(now - 86400000 * 30)
                    .updatedAt(now)
                    .build();

            // When
            ChannelMemberModel model = channelMemberMapper.toModel(original);
            ChannelMemberEntity converted = channelMemberMapper.toEntity(model);

            // Then
            assertEquals(original.getId(), converted.getId());
            assertEquals(original.getChannelId(), converted.getChannelId());
            assertEquals(original.getUserId(), converted.getUserId());
            assertEquals(original.getTenantId(), converted.getTenantId());
            assertEquals(original.getRole(), converted.getRole());
            assertEquals(original.getStatus(), converted.getStatus());
            assertEquals(original.getJoinedAt(), converted.getJoinedAt());
            assertEquals(original.getLeftAt(), converted.getLeftAt());
            assertEquals(original.getRemovedBy(), converted.getRemovedBy());
            assertEquals(original.getLastReadMsgId(), converted.getLastReadMsgId());
            assertEquals(original.getUnreadCount(), converted.getUnreadCount());
            assertEquals(original.getIsMuted(), converted.getIsMuted());
            assertEquals(original.getIsPinned(), converted.getIsPinned());
            assertEquals(original.getNotificationLevel(), converted.getNotificationLevel());
            assertEquals(original.getMetadata(), converted.getMetadata());
            assertEquals(original.getCreatedAt(), converted.getCreatedAt());
            assertEquals(original.getUpdatedAt(), converted.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle muted member round trip")
        void shouldHandleMutedMemberRoundTrip() {
            // Given
            long now = Instant.now().toEpochMilli();
            ChannelMemberEntity original = ChannelMemberEntity.builder()
                    .id(MEMBER_ID)
                    .channelId(CHANNEL_ID)
                    .userId(USER_ID)
                    .tenantId(TENANT_ID)
                    .role(MemberRole.MEMBER)
                    .status(MemberStatus.MUTED)
                    .joinedAt(now - 86400000)
                    .unreadCount(0)
                    .isMuted(true)
                    .isPinned(false)
                    .notificationLevel(NotificationLevel.NONE)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            ChannelMemberModel model = channelMemberMapper.toModel(original);
            ChannelMemberEntity converted = channelMemberMapper.toEntity(model);

            // Then
            assertEquals(MemberStatus.MUTED, converted.getStatus());
            assertTrue(converted.getIsMuted());
            assertEquals(NotificationLevel.NONE, converted.getNotificationLevel());
        }
    }
}
