/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.store.IChannelPort;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChannelService.
 * Tests all channel business operations with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private IChannelPort channelPort;

    @Mock
    private IDiscussCacheService cacheService;

    @InjectMocks
    private ChannelService channelService;

    // ==================== CREATE CHANNEL TESTS ====================

    @Nested
    @DisplayName("createChannel")
    class CreateChannelTests {

        @Test
        @DisplayName("should create and cache valid channel")
        void testCreateChannel_ValidChannel_SavesAndCaches() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            channel.setId(null); // New channel
            ChannelEntity savedChannel = TestDataFactory.createGroupChannel();
            savedChannel.setId(1L);

            when(channelPort.save(any(ChannelEntity.class))).thenReturn(savedChannel);

            // When
            ChannelEntity result = channelService.createChannel(channel);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(channelPort).save(channel);
            verify(cacheService).cacheChannel(savedChannel);
        }

        @Test
        @DisplayName("should throw exception for invalid channel")
        void testCreateChannel_InvalidChannel_ThrowsException() {
            // Given - channel without required fields
            ChannelEntity channel = ChannelEntity.builder()
                    .name("Test")
                    .type(ChannelType.GROUP)
                    .build(); // Missing tenantId

            // When/Then
            assertThrows(IllegalArgumentException.class,
                    () -> channelService.createChannel(channel));

            verify(channelPort, never()).save(any());
            verify(cacheService, never()).cacheChannel(any());
        }
    }

    // ==================== GET OR CREATE DIRECT CHANNEL TESTS ====================

    @Nested
    @DisplayName("getOrCreateDirectChannel")
    class GetOrCreateDirectChannelTests {

        @Test
        @DisplayName("should return existing direct channel")
        void testGetOrCreateDirectChannel_ExistingChannel_ReturnsExisting() {
            // Given
            ChannelEntity existing = TestDataFactory.createDirectChannel();
            when(channelPort.findDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            )).thenReturn(Optional.of(existing));

            // When
            ChannelEntity result = channelService.getOrCreateDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );

            // Then
            assertNotNull(result);
            assertEquals(existing.getId(), result.getId());
            verify(channelPort, never()).save(any());
        }

        @Test
        @DisplayName("should create new direct channel if not exists")
        void testGetOrCreateDirectChannel_NewChannel_CreatesNew() {
            // Given
            when(channelPort.findDirectChannel(any(), any(), any())).thenReturn(Optional.empty());

            ChannelEntity saved = TestDataFactory.createDirectChannel();
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(saved);

            // When
            ChannelEntity result = channelService.getOrCreateDirectChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.USER_ID_2
            );

            // Then
            assertNotNull(result);
            assertEquals(ChannelType.DIRECT, result.getType());
            verify(channelPort).save(any(ChannelEntity.class));
            verify(cacheService).cacheChannel(saved);
        }
    }

    // ==================== CREATE GROUP CHANNEL TESTS ====================

    @Nested
    @DisplayName("createGroupChannel")
    class CreateGroupChannelTests {

        @Test
        @DisplayName("should create public group channel")
        void testCreateGroupChannel_PublicGroup_CreatesSuccessfully() {
            // Given
            ChannelEntity saved = TestDataFactory.createGroupChannel("Team Chat", "Discussion");
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(saved);

            // When
            ChannelEntity result = channelService.createGroupChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Team Chat",
                    "Discussion",
                    false
            );

            // Then
            assertNotNull(result);
            assertEquals("Team Chat", result.getName());
            assertEquals(ChannelType.GROUP, result.getType());
            verify(cacheService).cacheChannel(saved);
        }

        @Test
        @DisplayName("should create private group channel")
        void testCreateGroupChannel_PrivateGroup_SetsPrivateFlag() {
            // Given
            ChannelEntity saved = TestDataFactory.createGroupChannel();
            saved.setIsPrivate(true);
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(saved);

            // When
            ChannelEntity result = channelService.createGroupChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Private Team",
                    "Secret discussion",
                    true
            );

            // Then
            assertTrue(result.getIsPrivate());
        }
    }

    // ==================== CREATE TOPIC CHANNEL TESTS ====================

    @Nested
    @DisplayName("createTopicChannel")
    class CreateTopicChannelTests {

        @Test
        @DisplayName("should return existing topic channel for entity")
        void testCreateTopicChannel_ExistingChannel_ReturnsExisting() {
            // Given
            ChannelEntity existing = TestDataFactory.createTopicChannel("PROJECT", 500L);
            when(channelPort.findByEntity(TestDataFactory.TENANT_ID, "PROJECT", 500L))
                    .thenReturn(Optional.of(existing));

            // When
            ChannelEntity result = channelService.createTopicChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Project Discussion",
                    "PROJECT",
                    500L
            );

            // Then
            assertEquals(existing.getId(), result.getId());
            verify(channelPort, never()).save(any());
        }

        @Test
        @DisplayName("should create new topic channel if not exists")
        void testCreateTopicChannel_NewChannel_CreatesWithEntity() {
            // Given
            when(channelPort.findByEntity(any(), any(), any())).thenReturn(Optional.empty());

            ChannelEntity saved = TestDataFactory.createTopicChannel("TASK", 100L);
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(saved);

            // When
            ChannelEntity result = channelService.createTopicChannel(
                    TestDataFactory.TENANT_ID,
                    TestDataFactory.USER_ID_1,
                    "Task Discussion",
                    "TASK",
                    100L
            );

            // Then
            assertEquals(ChannelType.TOPIC, result.getType());
            verify(channelPort).save(any(ChannelEntity.class));
            verify(cacheService).cacheChannel(saved);
        }
    }

    // ==================== GET CHANNEL BY ID TESTS ====================

    @Nested
    @DisplayName("getChannelById / getChannelByIdOrThrow")
    class GetChannelByIdTests {

        @Test
        @DisplayName("should return cached channel on cache hit")
        void testGetChannelById_CacheHit_ReturnsCached() {
            // Given
            ChannelEntity cached = TestDataFactory.createGroupChannel();
            when(cacheService.getCachedChannel(1L)).thenReturn(Optional.of(cached));

            // When
            Optional<ChannelEntity> result = channelService.getChannelById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(cached, result.get());
            verify(channelPort, never()).findById(any());
        }

        @Test
        @DisplayName("should query database on cache miss and cache result")
        void testGetChannelById_CacheMiss_QueriesDbAndCaches() {
            // Given
            when(cacheService.getCachedChannel(1L)).thenReturn(Optional.empty());
            ChannelEntity fromDb = TestDataFactory.createGroupChannel();
            when(channelPort.findById(1L)).thenReturn(Optional.of(fromDb));

            // When
            Optional<ChannelEntity> result = channelService.getChannelById(1L);

            // Then
            assertTrue(result.isPresent());
            verify(channelPort).findById(1L);
            verify(cacheService).cacheChannel(fromDb);
        }

        @Test
        @DisplayName("should return empty when not found anywhere")
        void testGetChannelById_NotFound_ReturnsEmpty() {
            // Given
            when(cacheService.getCachedChannel(999L)).thenReturn(Optional.empty());
            when(channelPort.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<ChannelEntity> result = channelService.getChannelById(999L);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getChannelByIdOrThrow should throw AppException when not found")
        void testGetChannelByIdOrThrow_NotFound_ThrowsAppException() {
            // Given
            when(cacheService.getCachedChannel(999L)).thenReturn(Optional.empty());
            when(channelPort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> channelService.getChannelByIdOrThrow(999L));

            assertEquals(ErrorCode.CHANNEL_NOT_FOUND.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("getChannelByIdOrThrow should return channel when found")
        void testGetChannelByIdOrThrow_Found_ReturnsChannel() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            when(cacheService.getCachedChannel(1L)).thenReturn(Optional.of(channel));

            // When
            ChannelEntity result = channelService.getChannelByIdOrThrow(1L);

            // Then
            assertNotNull(result);
            assertEquals(channel, result);
        }
    }

    // ==================== QUERY METHODS TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("getChannelsByTenantId should delegate to port")
        void testGetChannelsByTenantId_DelegatesToPort() {
            // Given
            List<ChannelEntity> channels = List.of(
                    TestDataFactory.createGroupChannel(),
                    TestDataFactory.createDirectChannel()
            );
            when(channelPort.findByTenantId(TestDataFactory.TENANT_ID)).thenReturn(channels);

            // When
            List<ChannelEntity> result = channelService.getChannelsByTenantId(TestDataFactory.TENANT_ID);

            // Then
            assertEquals(2, result.size());
            verify(channelPort).findByTenantId(TestDataFactory.TENANT_ID);
        }

        @Test
        @DisplayName("getChannelsByType should filter by type")
        void testGetChannelsByType_DelegatesToPort() {
            // Given
            List<ChannelEntity> groupChannels = List.of(TestDataFactory.createGroupChannel());
            when(channelPort.findByTenantIdAndType(TestDataFactory.TENANT_ID, ChannelType.GROUP))
                    .thenReturn(groupChannels);

            // When
            List<ChannelEntity> result = channelService.getChannelsByType(
                    TestDataFactory.TENANT_ID, ChannelType.GROUP);

            // Then
            assertEquals(1, result.size());
            verify(channelPort).findByTenantIdAndType(TestDataFactory.TENANT_ID, ChannelType.GROUP);
        }

        @Test
        @DisplayName("getChannelByEntity should delegate to port")
        void testGetChannelByEntity_DelegatesToPort() {
            // Given
            ChannelEntity topic = TestDataFactory.createTopicChannel("PROJECT", 1L);
            when(channelPort.findByEntity(TestDataFactory.TENANT_ID, "PROJECT", 1L))
                    .thenReturn(Optional.of(topic));

            // When
            Optional<ChannelEntity> result = channelService.getChannelByEntity(
                    TestDataFactory.TENANT_ID, "PROJECT", 1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(topic, result.get());
        }
    }

    // ==================== UPDATE CHANNEL TESTS ====================

    @Nested
    @DisplayName("updateChannel")
    class UpdateChannelTests {

        @Test
        @DisplayName("should update name and description")
        void testUpdateChannel_ValidUpdate_UpdatesAndCaches() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            when(cacheService.getCachedChannel(channel.getId())).thenReturn(Optional.of(channel));

            ChannelEntity updated = TestDataFactory.createGroupChannel();
            updated.setName("New Name");
            updated.setDescription("New Description");
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(updated);

            // When
            ChannelEntity result = channelService.updateChannel(channel.getId(), "New Name", "New Description");

            // Then
            assertNotNull(result);
            verify(channelPort).save(any(ChannelEntity.class));
            verify(cacheService).cacheChannel(updated);
        }

        @Test
        @DisplayName("should throw when channel not found")
        void testUpdateChannel_NotFound_ThrowsException() {
            // Given
            when(cacheService.getCachedChannel(999L)).thenReturn(Optional.empty());
            when(channelPort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(AppException.class,
                    () -> channelService.updateChannel(999L, "Name", "Desc"));
        }
    }

    // ==================== ARCHIVE TESTS ====================

    @Nested
    @DisplayName("archiveChannel / unarchiveChannel")
    class ArchiveTests {

        @Test
        @DisplayName("archiveChannel should archive and invalidate cache")
        void testArchiveChannel_ValidChannel_ArchivesAndInvalidates() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            when(cacheService.getCachedChannel(channel.getId())).thenReturn(Optional.of(channel));

            ChannelEntity archived = TestDataFactory.createArchivedChannel();
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(archived);

            // When
            ChannelEntity result = channelService.archiveChannel(channel.getId());

            // Then
            assertTrue(result.getIsArchived());
            verify(cacheService).invalidateChannel(channel.getId());
        }

        @Test
        @DisplayName("unarchiveChannel should unarchive and cache")
        void testUnarchiveChannel_ArchivedChannel_UnarchivesAndCaches() {
            // Given
            ChannelEntity archived = TestDataFactory.createArchivedChannel();
            when(cacheService.getCachedChannel(archived.getId())).thenReturn(Optional.of(archived));

            ChannelEntity unarchived = TestDataFactory.createGroupChannel();
            when(channelPort.save(any(ChannelEntity.class))).thenReturn(unarchived);

            // When
            ChannelEntity result = channelService.unarchiveChannel(archived.getId());

            // Then
            assertFalse(result.getIsArchived());
            verify(cacheService).cacheChannel(unarchived);
        }
    }

    // ==================== RECORD MESSAGE TESTS ====================

    @Nested
    @DisplayName("recordMessage")
    class RecordMessageTests {

        @Test
        @DisplayName("should increment message count and invalidate cache")
        void testRecordMessage_ValidChannel_IncrementsAndInvalidates() {
            // Given
            ChannelEntity channel = TestDataFactory.createGroupChannel();
            when(cacheService.getCachedChannel(channel.getId())).thenReturn(Optional.of(channel));
            when(channelPort.save(any(ChannelEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            channelService.recordMessage(channel.getId());

            // Then
            verify(channelPort).save(any(ChannelEntity.class));
            verify(cacheService).invalidateChannel(channel.getId());
        }
    }

    // ==================== DELETE CHANNEL TESTS ====================

    @Nested
    @DisplayName("deleteChannel")
    class DeleteChannelTests {

        @Test
        @DisplayName("should invalidate caches and delete")
        void testDeleteChannel_ValidChannel_InvalidatesAndDeletes() {
            // Given
            Long channelId = 1L;

            // When
            channelService.deleteChannel(channelId);

            // Then
            verify(cacheService).invalidateChannel(channelId);
            verify(channelPort).deleteById(channelId);
        }
    }
}
