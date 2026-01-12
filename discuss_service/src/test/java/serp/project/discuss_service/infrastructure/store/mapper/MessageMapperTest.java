/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for MessageMapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.enums.MessageType;
import serp.project.discuss_service.infrastructure.store.model.MessageModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessageMapper Tests")
class MessageMapperTest {

    private MessageMapper messageMapper;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long CHANNEL_ID = 1000L;
    private static final Long MESSAGE_ID = 2000L;

    @BeforeEach
    void setUp() {
        messageMapper = new MessageMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void shouldReturnNullWhenModelIsNull() {
            MessageEntity result = messageMapper.toEntity(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from model to entity")
        void shouldMapAllFieldsFromModelToEntity() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", "value");
            Long[] mentions = {200L, 300L};

            List<Map<String, Object>> reactions = new ArrayList<>();
            Map<String, Object> reaction = new HashMap<>();
            reaction.put("emoji", "👍");
            reaction.put("userIds", Arrays.asList(200L, 300L));
            reactions.add(reaction);

            MessageModel model = MessageModel.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message content")
                    .messageType(MessageType.TEXT)
                    .mentions(mentions)
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

            // When
            MessageEntity entity = messageMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(MESSAGE_ID, entity.getId());
            assertEquals(CHANNEL_ID, entity.getChannelId());
            assertEquals(USER_ID, entity.getSenderId());
            assertEquals(TENANT_ID, entity.getTenantId());
            assertEquals("Test message content", entity.getContent());
            assertEquals(MessageType.TEXT, entity.getMessageType());
            assertNotNull(entity.getMentions());
            assertEquals(2, entity.getMentions().size());
            assertTrue(entity.getMentions().containsAll(Arrays.asList(200L, 300L)));
            assertEquals(1500L, entity.getParentId());
            assertEquals(5, entity.getThreadCount());
            assertTrue(entity.getIsEdited());
            assertNotNull(entity.getEditedAt());
            assertFalse(entity.getIsDeleted());
            assertNotNull(entity.getReactions());
            assertEquals(1, entity.getReactions().size());
            assertEquals("👍", entity.getReactions().get(0).getEmoji());
            assertEquals(metadata, entity.getMetadata());
        }

        @Test
        @DisplayName("Should handle null mentions array")
        void shouldHandleNullMentionsArray() {
            // Given
            MessageModel model = MessageModel.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .mentions(null)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .build();

            // When
            MessageEntity entity = messageMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertNotNull(entity.getMentions());
            assertTrue(entity.getMentions().isEmpty());
        }

        @Test
        @DisplayName("Should handle null reactions")
        void shouldHandleNullReactions() {
            // Given
            MessageModel model = MessageModel.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .reactions(null)
                    .build();

            // When
            MessageEntity entity = messageMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertNotNull(entity.getReactions());
            assertTrue(entity.getReactions().isEmpty());
        }

        @Test
        @DisplayName("Should correctly parse complex reactions")
        void shouldCorrectlyParseComplexReactions() {
            // Given
            List<Map<String, Object>> reactions = new ArrayList<>();
            
            Map<String, Object> reaction1 = new HashMap<>();
            reaction1.put("emoji", "👍");
            reaction1.put("userIds", Arrays.asList(100, 200, 300)); // Numbers, not Longs
            reactions.add(reaction1);
            
            Map<String, Object> reaction2 = new HashMap<>();
            reaction2.put("emoji", "❤️");
            reaction2.put("userIds", Arrays.asList(400));
            reactions.add(reaction2);

            MessageModel model = MessageModel.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .reactions(reactions)
                    .build();

            // When
            MessageEntity entity = messageMapper.toEntity(model);

            // Then
            assertNotNull(entity.getReactions());
            assertEquals(2, entity.getReactions().size());
            
            MessageEntity.ReactionVO firstReaction = entity.getReactions().get(0);
            assertEquals("👍", firstReaction.getEmoji());
            assertEquals(3, firstReaction.getUserIds().size());
            assertTrue(firstReaction.getUserIds().contains(100L));
            assertTrue(firstReaction.getUserIds().contains(200L));
            assertTrue(firstReaction.getUserIds().contains(300L));
            
            MessageEntity.ReactionVO secondReaction = entity.getReactions().get(1);
            assertEquals("❤️", secondReaction.getEmoji());
            assertEquals(1, secondReaction.getUserIds().size());
            assertTrue(secondReaction.getUserIds().contains(400L));
        }
    }

    @Nested
    @DisplayName("toModel() Tests")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            MessageModel result = messageMapper.toModel(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from entity to model")
        void shouldMapAllFieldsFromEntityToModel() {
            // Given
            long now = Instant.now().toEpochMilli();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("priority", "high");

            List<MessageEntity.ReactionVO> reactions = new ArrayList<>();
            reactions.add(new MessageEntity.ReactionVO("🎉", Arrays.asList(100L, 200L)));

            MessageEntity entity = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Entity content")
                    .messageType(MessageType.FILE)
                    .mentions(Arrays.asList(300L, 400L))
                    .parentId(1000L)
                    .threadCount(10)
                    .isEdited(false)
                    .editedAt(null)
                    .isDeleted(true)
                    .deletedAt(now)
                    .deletedBy(USER_ID)
                    .reactions(reactions)
                    .metadata(metadata)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertEquals(MESSAGE_ID, model.getId());
            assertEquals(CHANNEL_ID, model.getChannelId());
            assertEquals(USER_ID, model.getSenderId());
            assertEquals(TENANT_ID, model.getTenantId());
            assertEquals("Entity content", model.getContent());
            assertEquals(MessageType.FILE, model.getMessageType());
            assertNotNull(model.getMentions());
            assertEquals(2, model.getMentions().length);
            assertEquals(1000L, model.getParentId());
            assertEquals(10, model.getThreadCount());
            assertFalse(model.getIsEdited());
            assertTrue(model.getIsDeleted());
            assertNotNull(model.getDeletedAt());
            assertEquals(USER_ID, model.getDeletedBy());
            assertNotNull(model.getReactions());
            assertEquals(1, model.getReactions().size());
            assertEquals(metadata, model.getMetadata());
        }

        @Test
        @DisplayName("Should handle null mentions list")
        void shouldHandleNullMentionsList() {
            // Given
            MessageEntity entity = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .mentions(null)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertNotNull(model.getMentions());
            assertEquals(0, model.getMentions().length);
        }

        @Test
        @DisplayName("Should correctly serialize reactions to model")
        void shouldCorrectlySerializeReactions() {
            // Given
            List<MessageEntity.ReactionVO> reactions = new ArrayList<>();
            reactions.add(new MessageEntity.ReactionVO("👍", Arrays.asList(100L, 200L)));
            reactions.add(new MessageEntity.ReactionVO("👎", Arrays.asList(300L)));

            MessageEntity entity = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .reactions(reactions)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(entity);

            // Then
            assertNotNull(model.getReactions());
            assertEquals(2, model.getReactions().size());
            
            Map<String, Object> firstReaction = model.getReactions().get(0);
            assertEquals("👍", firstReaction.get("emoji"));
            @SuppressWarnings("unchecked")
            List<Long> userIds1 = (List<Long>) firstReaction.get("userIds");
            assertEquals(2, userIds1.size());
            
            Map<String, Object> secondReaction = model.getReactions().get(1);
            assertEquals("👎", secondReaction.get("emoji"));
            @SuppressWarnings("unchecked")
            List<Long> userIds2 = (List<Long>) secondReaction.get("userIds");
            assertEquals(1, userIds2.size());
        }

        @Test
        @DisplayName("Should handle null reactions list")
        void shouldHandleNullReactionsList() {
            // Given
            MessageEntity entity = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Test message")
                    .messageType(MessageType.TEXT)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .reactions(null)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertNotNull(model.getReactions());
            assertTrue(model.getReactions().isEmpty());
        }
    }

    @Nested
    @DisplayName("toEntityList() Tests")
    class ToEntityListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<MessageEntity> result = messageMapper.toEntityList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<MessageEntity> result = messageMapper.toEntityList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of models to entities")
        void shouldConvertListOfModelsToEntities() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MessageModel model1 = MessageModel.builder()
                    .id(1L)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Message 1")
                    .messageType(MessageType.TEXT)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            MessageModel model2 = MessageModel.builder()
                    .id(2L)
                    .channelId(CHANNEL_ID)
                    .senderId(200L)
                    .tenantId(TENANT_ID)
                    .content("Message 2")
                    .messageType(MessageType.IMAGE)
                    .threadCount(3)
                    .isEdited(true)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<MessageModel> models = Arrays.asList(model1, model2);

            // When
            List<MessageEntity> entities = messageMapper.toEntityList(models);

            // Then
            assertNotNull(entities);
            assertEquals(2, entities.size());
            assertEquals(1L, entities.get(0).getId());
            assertEquals("Message 1", entities.get(0).getContent());
            assertEquals(MessageType.TEXT, entities.get(0).getMessageType());
            assertEquals(2L, entities.get(1).getId());
            assertEquals("Message 2", entities.get(1).getContent());
            assertEquals(MessageType.IMAGE, entities.get(1).getMessageType());
        }
    }

    @Nested
    @DisplayName("toModelList() Tests")
    class ToModelListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<MessageModel> result = messageMapper.toModelList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<MessageModel> result = messageMapper.toModelList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of entities to models")
        void shouldConvertListOfEntitiesToModels() {
            // Given
            long now = Instant.now().toEpochMilli();
            MessageEntity entity1 = MessageEntity.builder()
                    .id(1L)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Entity 1")
                    .messageType(MessageType.SYSTEM)
                    .mentions(new ArrayList<>())
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            MessageEntity entity2 = MessageEntity.builder()
                    .id(2L)
                    .channelId(CHANNEL_ID)
                    .senderId(200L)
                    .tenantId(TENANT_ID)
                    .content("Entity 2")
                    .messageType(MessageType.FILE)
                    .mentions(Arrays.asList(300L, 400L))
                    .parentId(1L)
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(true)
                    .deletedAt(now)
                    .deletedBy(USER_ID)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<MessageEntity> entities = Arrays.asList(entity1, entity2);

            // When
            List<MessageModel> models = messageMapper.toModelList(entities);

            // Then
            assertNotNull(models);
            assertEquals(2, models.size());
            assertEquals(1L, models.get(0).getId());
            assertEquals("Entity 1", models.get(0).getContent());
            assertEquals(MessageType.SYSTEM, models.get(0).getMessageType());
            assertEquals(2L, models.get(1).getId());
            assertEquals("Entity 2", models.get(1).getContent());
            assertTrue(models.get(1).getIsDeleted());
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

            List<MessageEntity.ReactionVO> reactions = new ArrayList<>();
            reactions.add(new MessageEntity.ReactionVO("👍", Arrays.asList(100L, 200L)));

            MessageEntity original = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Round trip message")
                    .messageType(MessageType.TEXT)
                    .mentions(Arrays.asList(300L, 400L))
                    .parentId(500L)
                    .threadCount(5)
                    .isEdited(true)
                    .editedAt(now - 3600000)
                    .isDeleted(false)
                    .reactions(reactions)
                    .metadata(metadata)
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(original);
            MessageEntity converted = messageMapper.toEntity(model);

            // Then
            assertEquals(original.getId(), converted.getId());
            assertEquals(original.getChannelId(), converted.getChannelId());
            assertEquals(original.getSenderId(), converted.getSenderId());
            assertEquals(original.getTenantId(), converted.getTenantId());
            assertEquals(original.getContent(), converted.getContent());
            assertEquals(original.getMessageType(), converted.getMessageType());
            assertEquals(original.getMentions().size(), converted.getMentions().size());
            assertTrue(converted.getMentions().containsAll(original.getMentions()));
            assertEquals(original.getParentId(), converted.getParentId());
            assertEquals(original.getThreadCount(), converted.getThreadCount());
            assertEquals(original.getIsEdited(), converted.getIsEdited());
            assertEquals(original.getEditedAt(), converted.getEditedAt());
            assertEquals(original.getIsDeleted(), converted.getIsDeleted());
            assertEquals(original.getReactions().size(), converted.getReactions().size());
            assertEquals(original.getReactions().get(0).getEmoji(), converted.getReactions().get(0).getEmoji());
            assertEquals(original.getMetadata(), converted.getMetadata());
            assertEquals(original.getCreatedAt(), converted.getCreatedAt());
            assertEquals(original.getUpdatedAt(), converted.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle deleted message round trip")
        void shouldHandleDeletedMessageRoundTrip() {
            // Given
            long now = Instant.now().toEpochMilli();
            MessageEntity original = MessageEntity.builder()
                    .id(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .senderId(USER_ID)
                    .tenantId(TENANT_ID)
                    .content("Deleted message")
                    .messageType(MessageType.TEXT)
                    .mentions(new ArrayList<>())
                    .threadCount(0)
                    .isEdited(false)
                    .isDeleted(true)
                    .deletedAt(now)
                    .deletedBy(USER_ID)
                    .reactions(new ArrayList<>())
                    .createdAt(now - 86400000)
                    .updatedAt(now)
                    .build();

            // When
            MessageModel model = messageMapper.toModel(original);
            MessageEntity converted = messageMapper.toEntity(model);

            // Then
            assertTrue(converted.getIsDeleted());
            assertEquals(original.getDeletedAt(), converted.getDeletedAt());
            assertEquals(original.getDeletedBy(), converted.getDeletedBy());
        }
    }
}
