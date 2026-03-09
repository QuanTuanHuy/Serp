/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for ChannelMapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.infrastructure.store.model.ChannelModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChannelMapper Tests")
class ChannelMapperTest {

    private ChannelMapper channelMapper;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long CHANNEL_ID = 1000L;

    @BeforeEach
    void setUp() {
        channelMapper = new ChannelMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void shouldReturnNullWhenModelIsNull() {
            ChannelEntity result = channelMapper.toEntity(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from model to entity")
        void shouldMapAllFieldsFromModelToEntity() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", "value");

            ChannelModel model = ChannelModel.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Test Channel")
                    .description("Test Description")
                    .type(ChannelType.GROUP)
                    .entityType("PROJECT")
                    .entityId(500L)
                    .isPrivate(true)
                    .isArchived(false)
                    .memberCount(5)
                    .messageCount(100)
                    .lastMessageAt(now)
                    .metadata(metadata)
                    .createdAt(now.minusDays(1))
                    .updatedAt(now)
                    .build();

            // When
            ChannelEntity entity = channelMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(CHANNEL_ID, entity.getId());
            assertEquals(TENANT_ID, entity.getTenantId());
            assertEquals(USER_ID, entity.getCreatedBy());
            assertEquals("Test Channel", entity.getName());
            assertEquals("Test Description", entity.getDescription());
            assertEquals(ChannelType.GROUP, entity.getType());
            assertEquals("PROJECT", entity.getEntityType());
            assertEquals(500L, entity.getEntityId());
            assertTrue(entity.getIsPrivate());
            assertFalse(entity.getIsArchived());
            assertEquals(5, entity.getMemberCount());
            assertEquals(100, entity.getMessageCount());
            assertNotNull(entity.getLastMessageAt());
            assertEquals(metadata, entity.getMetadata());
            assertNotNull(entity.getCreatedAt());
            assertNotNull(entity.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle null timestamps correctly")
        void shouldHandleNullTimestamps() {
            // Given
            ChannelModel model = ChannelModel.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Test Channel")
                    .type(ChannelType.DIRECT)
                    .isPrivate(false)
                    .isArchived(false)
                    .memberCount(2)
                    .messageCount(0)
                    .lastMessageAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            ChannelEntity entity = channelMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertNull(entity.getLastMessageAt());
            assertNull(entity.getCreatedAt());
            assertNull(entity.getUpdatedAt());
        }

        @Test
        @DisplayName("Should correctly convert LocalDateTime to epoch millis")
        void shouldConvertLocalDateTimeToEpochMillis() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2025, 1, 12, 10, 30, 0);
            long expectedMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            ChannelModel model = ChannelModel.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Test Channel")
                    .type(ChannelType.GROUP)
                    .isPrivate(false)
                    .isArchived(false)
                    .memberCount(1)
                    .messageCount(0)
                    .lastMessageAt(dateTime)
                    .createdAt(dateTime)
                    .updatedAt(dateTime)
                    .build();

            // When
            ChannelEntity entity = channelMapper.toEntity(model);

            // Then
            assertEquals(expectedMillis, entity.getLastMessageAt());
            assertEquals(expectedMillis, entity.getCreatedAt());
            assertEquals(expectedMillis, entity.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toModel() Tests")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            ChannelModel result = channelMapper.toModel(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from entity to model")
        void shouldMapAllFieldsFromEntityToModel() {
            // Given
            long now = Instant.now().toEpochMilli();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("setting", true);

            ChannelEntity entity = ChannelEntity.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Group Channel")
                    .description("A group channel")
                    .type(ChannelType.GROUP)
                    .entityType("TASK")
                    .entityId(999L)
                    .isPrivate(false)
                    .isArchived(true)
                    .memberCount(10)
                    .messageCount(500)
                    .lastMessageAt(now)
                    .metadata(metadata)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            ChannelModel model = channelMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertEquals(CHANNEL_ID, model.getId());
            assertEquals(TENANT_ID, model.getTenantId());
            assertEquals(USER_ID, model.getCreatedBy());
            assertEquals("Group Channel", model.getName());
            assertEquals("A group channel", model.getDescription());
            assertEquals(ChannelType.GROUP, model.getType());
            assertEquals("TASK", model.getEntityType());
            assertEquals(999L, model.getEntityId());
            assertFalse(model.getIsPrivate());
            assertTrue(model.getIsArchived());
            assertEquals(10, model.getMemberCount());
            assertEquals(500, model.getMessageCount());
            assertNotNull(model.getLastMessageAt());
            assertEquals(metadata, model.getMetadata());
            assertNotNull(model.getCreatedAt());
            assertNotNull(model.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle null timestamps correctly")
        void shouldHandleNullTimestamps() {
            // Given
            ChannelEntity entity = ChannelEntity.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Test Channel")
                    .type(ChannelType.TOPIC)
                    .isPrivate(false)
                    .isArchived(false)
                    .memberCount(1)
                    .messageCount(0)
                    .lastMessageAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            ChannelModel model = channelMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertNull(model.getLastMessageAt());
            assertNull(model.getCreatedAt());
            assertNull(model.getUpdatedAt());
        }

        @Test
        @DisplayName("Should correctly convert epoch millis to LocalDateTime")
        void shouldConvertEpochMillisToLocalDateTime() {
            // Given
            long timestamp = 1736676600000L; // 2025-01-12T10:30:00 UTC
            LocalDateTime expected = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

            ChannelEntity entity = ChannelEntity.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Test Channel")
                    .type(ChannelType.DIRECT)
                    .isPrivate(true)
                    .isArchived(false)
                    .memberCount(2)
                    .messageCount(50)
                    .lastMessageAt(timestamp)
                    .createdAt(timestamp)
                    .updatedAt(timestamp)
                    .build();

            // When
            ChannelModel model = channelMapper.toModel(entity);

            // Then
            assertEquals(expected, model.getLastMessageAt());
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
            List<ChannelEntity> result = channelMapper.toEntityList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<ChannelEntity> result = channelMapper.toEntityList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of models to entities")
        void shouldConvertListOfModelsToEntities() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            ChannelModel model1 = ChannelModel.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Channel 1")
                    .type(ChannelType.GROUP)
                    .isPrivate(false)
                    .isArchived(false)
                    .memberCount(3)
                    .messageCount(10)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            ChannelModel model2 = ChannelModel.builder()
                    .id(2L)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Channel 2")
                    .type(ChannelType.DIRECT)
                    .isPrivate(true)
                    .isArchived(false)
                    .memberCount(2)
                    .messageCount(5)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<ChannelModel> models = Arrays.asList(model1, model2);

            // When
            List<ChannelEntity> entities = channelMapper.toEntityList(models);

            // Then
            assertNotNull(entities);
            assertEquals(2, entities.size());
            assertEquals(1L, entities.get(0).getId());
            assertEquals("Channel 1", entities.get(0).getName());
            assertEquals(2L, entities.get(1).getId());
            assertEquals("Channel 2", entities.get(1).getName());
        }
    }

    @Nested
    @DisplayName("toModelList() Tests")
    class ToModelListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<ChannelModel> result = channelMapper.toModelList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<ChannelModel> result = channelMapper.toModelList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of entities to models")
        void shouldConvertListOfEntitiesToModels() {
            // Given
            long now = Instant.now().toEpochMilli();
            ChannelEntity entity1 = ChannelEntity.builder()
                    .id(1L)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Entity 1")
                    .type(ChannelType.TOPIC)
                    .entityType("ORDER")
                    .entityId(100L)
                    .isPrivate(false)
                    .isArchived(false)
                    .memberCount(5)
                    .messageCount(20)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            ChannelEntity entity2 = ChannelEntity.builder()
                    .id(2L)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Entity 2")
                    .type(ChannelType.GROUP)
                    .isPrivate(true)
                    .isArchived(true)
                    .memberCount(8)
                    .messageCount(150)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<ChannelEntity> entities = Arrays.asList(entity1, entity2);

            // When
            List<ChannelModel> models = channelMapper.toModelList(entities);

            // Then
            assertNotNull(models);
            assertEquals(2, models.size());
            assertEquals(1L, models.get(0).getId());
            assertEquals("Entity 1", models.get(0).getName());
            assertEquals("ORDER", models.get(0).getEntityType());
            assertEquals(2L, models.get(1).getId());
            assertEquals("Entity 2", models.get(1).getName());
            assertTrue(models.get(1).getIsArchived());
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
            metadata.put("custom", "data");

            ChannelEntity original = ChannelEntity.builder()
                    .id(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .createdBy(USER_ID)
                    .name("Round Trip Channel")
                    .description("Testing round trip")
                    .type(ChannelType.GROUP)
                    .entityType("LEAD")
                    .entityId(42L)
                    .isPrivate(true)
                    .isArchived(false)
                    .memberCount(7)
                    .messageCount(250)
                    .lastMessageAt(now)
                    .metadata(metadata)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            ChannelModel model = channelMapper.toModel(original);
            ChannelEntity converted = channelMapper.toEntity(model);

            // Then
            assertEquals(original.getId(), converted.getId());
            assertEquals(original.getTenantId(), converted.getTenantId());
            assertEquals(original.getCreatedBy(), converted.getCreatedBy());
            assertEquals(original.getName(), converted.getName());
            assertEquals(original.getDescription(), converted.getDescription());
            assertEquals(original.getType(), converted.getType());
            assertEquals(original.getEntityType(), converted.getEntityType());
            assertEquals(original.getEntityId(), converted.getEntityId());
            assertEquals(original.getIsPrivate(), converted.getIsPrivate());
            assertEquals(original.getIsArchived(), converted.getIsArchived());
            assertEquals(original.getMemberCount(), converted.getMemberCount());
            assertEquals(original.getMessageCount(), converted.getMessageCount());
            assertEquals(original.getLastMessageAt(), converted.getLastMessageAt());
            assertEquals(original.getMetadata(), converted.getMetadata());
            assertEquals(original.getCreatedAt(), converted.getCreatedAt());
            assertEquals(original.getUpdatedAt(), converted.getUpdatedAt());
        }
    }
}
