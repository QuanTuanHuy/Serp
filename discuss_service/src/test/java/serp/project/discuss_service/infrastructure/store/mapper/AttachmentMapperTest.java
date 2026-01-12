/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for AttachmentMapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.enums.ScanStatus;
import serp.project.discuss_service.core.domain.enums.StorageProvider;
import serp.project.discuss_service.infrastructure.store.model.AttachmentModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttachmentMapper Tests")
class AttachmentMapperTest {

    private AttachmentMapper attachmentMapper;
    private static final Long TENANT_ID = 1L;
    private static final Long CHANNEL_ID = 1000L;
    private static final Long MESSAGE_ID = 2000L;
    private static final Long ATTACHMENT_ID = 3000L;

    @BeforeEach
    void setUp() {
        attachmentMapper = new AttachmentMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void shouldReturnNullWhenModelIsNull() {
            AttachmentEntity result = attachmentMapper.toEntity(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from model to entity")
        void shouldMapAllFieldsFromModelToEntity() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalName", "test_image.png");

            AttachmentModel model = AttachmentModel.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("test_image.png")
                    .fileSize(1048576L) // 1 MB
                    .fileType("image/png")
                    .fileExtension("png")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("discuss-attachments")
                    .storageKey("tenant-1/channel-1000/test_image.png")
                    .storageUrl("https://s3.amazonaws.com/discuss-attachments/tenant-1/channel-1000/test_image.png")
                    .thumbnailUrl("https://s3.amazonaws.com/discuss-attachments/thumbnails/test_image_thumb.png")
                    .width(1920)
                    .height(1080)
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(now)
                    .metadata(metadata)
                    .createdAt(now.minusHours(1))
                    .updatedAt(now)
                    .build();

            // When
            AttachmentEntity entity = attachmentMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(ATTACHMENT_ID, entity.getId());
            assertEquals(MESSAGE_ID, entity.getMessageId());
            assertEquals(CHANNEL_ID, entity.getChannelId());
            assertEquals(TENANT_ID, entity.getTenantId());
            assertEquals("test_image.png", entity.getFileName());
            assertEquals(1048576L, entity.getFileSize());
            assertEquals("image/png", entity.getFileType());
            assertEquals("png", entity.getFileExtension());
            assertEquals(StorageProvider.S3, entity.getStorageProvider());
            assertEquals("discuss-attachments", entity.getStorageBucket());
            assertEquals("tenant-1/channel-1000/test_image.png", entity.getStorageKey());
            assertNotNull(entity.getStorageUrl());
            assertNotNull(entity.getThumbnailUrl());
            assertEquals(1920, entity.getWidth());
            assertEquals(1080, entity.getHeight());
            assertEquals(ScanStatus.CLEAN, entity.getScanStatus());
            assertNotNull(entity.getScannedAt());
            assertEquals(metadata, entity.getMetadata());
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            AttachmentModel model = AttachmentModel.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("document.pdf")
                    .fileSize(2048L)
                    .fileType("application/pdf")
                    .fileExtension("pdf")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("discuss-attachments")
                    .storageKey("documents/document.pdf")
                    .storageUrl("https://s3.amazonaws.com/document.pdf")
                    .thumbnailUrl(null) // No thumbnail for PDF
                    .width(null)
                    .height(null)
                    .scanStatus(ScanStatus.PENDING)
                    .scannedAt(null)
                    .metadata(null)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // When
            AttachmentEntity entity = attachmentMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertNull(entity.getThumbnailUrl());
            assertNull(entity.getWidth());
            assertNull(entity.getHeight());
            assertNull(entity.getScannedAt());
            assertNull(entity.getMetadata());
        }

        @Test
        @DisplayName("Should correctly convert LocalDateTime to epoch millis")
        void shouldConvertLocalDateTimeToEpochMillis() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2025, 1, 12, 10, 30, 0);
            long expectedMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            AttachmentModel model = AttachmentModel.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("file.txt")
                    .fileSize(100L)
                    .fileType("text/plain")
                    .fileExtension("txt")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key")
                    .storageUrl("url")
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(dateTime)
                    .createdAt(dateTime)
                    .updatedAt(dateTime)
                    .build();

            // When
            AttachmentEntity entity = attachmentMapper.toEntity(model);

            // Then
            assertEquals(expectedMillis, entity.getScannedAt());
            assertEquals(expectedMillis, entity.getCreatedAt());
            assertEquals(expectedMillis, entity.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle infected scan status")
        void shouldHandleInfectedScanStatus() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            AttachmentModel model = AttachmentModel.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("malware.exe")
                    .fileSize(5000L)
                    .fileType("application/octet-stream")
                    .fileExtension("exe")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key")
                    .storageUrl("url")
                    .scanStatus(ScanStatus.INFECTED)
                    .scannedAt(now)
                    .createdAt(now.minusMinutes(5))
                    .updatedAt(now)
                    .build();

            // When
            AttachmentEntity entity = attachmentMapper.toEntity(model);

            // Then
            assertNotNull(entity);
            assertEquals(ScanStatus.INFECTED, entity.getScanStatus());
        }
    }

    @Nested
    @DisplayName("toModel() Tests")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull() {
            AttachmentModel result = attachmentMapper.toModel(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should correctly map all fields from entity to model")
        void shouldMapAllFieldsFromEntityToModel() {
            // Given
            long now = Instant.now().toEpochMilli();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("duration", 120);

            AttachmentEntity entity = AttachmentEntity.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("video.mp4")
                    .fileSize(52428800L) // 50 MB
                    .fileType("video/mp4")
                    .fileExtension("mp4")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("discuss-videos")
                    .storageKey("videos/video.mp4")
                    .storageUrl("https://cdn.example.com/video.mp4")
                    .thumbnailUrl("https://cdn.example.com/video_thumb.jpg")
                    .width(1280)
                    .height(720)
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(now)
                    .metadata(metadata)
                    .createdAt(now - 3600000)
                    .updatedAt(now)
                    .build();

            // When
            AttachmentModel model = attachmentMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertEquals(ATTACHMENT_ID, model.getId());
            assertEquals(MESSAGE_ID, model.getMessageId());
            assertEquals(CHANNEL_ID, model.getChannelId());
            assertEquals(TENANT_ID, model.getTenantId());
            assertEquals("video.mp4", model.getFileName());
            assertEquals(52428800L, model.getFileSize());
            assertEquals("video/mp4", model.getFileType());
            assertEquals("mp4", model.getFileExtension());
            assertEquals("discuss-videos", model.getStorageBucket());
            assertEquals("videos/video.mp4", model.getStorageKey());
            assertNotNull(model.getStorageUrl());
            assertNotNull(model.getThumbnailUrl());
            assertEquals(1280, model.getWidth());
            assertEquals(720, model.getHeight());
            assertEquals(ScanStatus.CLEAN, model.getScanStatus());
            assertNotNull(model.getScannedAt());
            assertEquals(metadata, model.getMetadata());
        }

        @Test
        @DisplayName("Should handle null timestamps correctly")
        void shouldHandleNullTimestamps() {
            // Given
            AttachmentEntity entity = AttachmentEntity.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("file.doc")
                    .fileSize(1024L)
                    .fileType("application/msword")
                    .fileExtension("doc")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key")
                    .storageUrl("url")
                    .scanStatus(ScanStatus.PENDING)
                    .scannedAt(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            AttachmentModel model = attachmentMapper.toModel(entity);

            // Then
            assertNotNull(model);
            assertNull(model.getScannedAt());
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

            AttachmentEntity entity = AttachmentEntity.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("file.txt")
                    .fileSize(100L)
                    .fileType("text/plain")
                    .fileExtension("txt")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key")
                    .storageUrl("url")
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(timestamp)
                    .createdAt(timestamp)
                    .updatedAt(timestamp)
                    .build();

            // When
            AttachmentModel model = attachmentMapper.toModel(entity);

            // Then
            assertEquals(expected, model.getScannedAt());
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
            List<AttachmentEntity> result = attachmentMapper.toEntityList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<AttachmentEntity> result = attachmentMapper.toEntityList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of models to entities")
        void shouldConvertListOfModelsToEntities() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            AttachmentModel model1 = AttachmentModel.builder()
                    .id(1L)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("image1.jpg")
                    .fileSize(500000L)
                    .fileType("image/jpeg")
                    .fileExtension("jpg")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key1")
                    .storageUrl("url1")
                    .scanStatus(ScanStatus.CLEAN)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            AttachmentModel model2 = AttachmentModel.builder()
                    .id(2L)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("image2.png")
                    .fileSize(750000L)
                    .fileType("image/png")
                    .fileExtension("png")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key2")
                    .storageUrl("url2")
                    .scanStatus(ScanStatus.PENDING)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<AttachmentModel> models = Arrays.asList(model1, model2);

            // When
            List<AttachmentEntity> entities = attachmentMapper.toEntityList(models);

            // Then
            assertNotNull(entities);
            assertEquals(2, entities.size());
            assertEquals(1L, entities.get(0).getId());
            assertEquals("image1.jpg", entities.get(0).getFileName());
            assertEquals(ScanStatus.CLEAN, entities.get(0).getScanStatus());
            assertEquals(2L, entities.get(1).getId());
            assertEquals("image2.png", entities.get(1).getFileName());
            assertEquals(ScanStatus.PENDING, entities.get(1).getScanStatus());
        }
    }

    @Nested
    @DisplayName("toModelList() Tests")
    class ToModelListTests {

        @Test
        @DisplayName("Should return null when list is null")
        void shouldReturnNullWhenListIsNull() {
            List<AttachmentModel> result = attachmentMapper.toModelList(null);
            assertNull(result);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<AttachmentModel> result = attachmentMapper.toModelList(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should correctly convert list of entities to models")
        void shouldConvertListOfEntitiesToModels() {
            // Given
            long now = Instant.now().toEpochMilli();
            AttachmentEntity entity1 = AttachmentEntity.builder()
                    .id(1L)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("doc1.pdf")
                    .fileSize(100000L)
                    .fileType("application/pdf")
                    .fileExtension("pdf")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key1")
                    .storageUrl("url1")
                    .scanStatus(ScanStatus.CLEAN)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            AttachmentEntity entity2 = AttachmentEntity.builder()
                    .id(2L)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("doc2.docx")
                    .fileSize(200000L)
                    .fileType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .fileExtension("docx")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("key2")
                    .storageUrl("url2")
                    .scanStatus(ScanStatus.INFECTED)
                    .scannedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<AttachmentEntity> entities = Arrays.asList(entity1, entity2);

            // When
            List<AttachmentModel> models = attachmentMapper.toModelList(entities);

            // Then
            assertNotNull(models);
            assertEquals(2, models.size());
            assertEquals(1L, models.get(0).getId());
            assertEquals("doc1.pdf", models.get(0).getFileName());
            assertEquals(ScanStatus.CLEAN, models.get(0).getScanStatus());
            assertEquals(2L, models.get(1).getId());
            assertEquals("doc2.docx", models.get(1).getFileName());
            assertEquals(ScanStatus.INFECTED, models.get(1).getScanStatus());
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
            metadata.put("compression", "lossless");

            AttachmentEntity original = AttachmentEntity.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("archive.zip")
                    .fileSize(10485760L)
                    .fileType("application/zip")
                    .fileExtension("zip")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("discuss-files")
                    .storageKey("archives/archive.zip")
                    .storageUrl("https://cdn.example.com/archive.zip")
                    .thumbnailUrl(null)
                    .width(null)
                    .height(null)
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(now - 60000)
                    .metadata(metadata)
                    .createdAt(now - 120000)
                    .updatedAt(now)
                    .build();

            // When
            AttachmentModel model = attachmentMapper.toModel(original);
            AttachmentEntity converted = attachmentMapper.toEntity(model);

            // Then
            assertEquals(original.getId(), converted.getId());
            assertEquals(original.getMessageId(), converted.getMessageId());
            assertEquals(original.getChannelId(), converted.getChannelId());
            assertEquals(original.getTenantId(), converted.getTenantId());
            assertEquals(original.getFileName(), converted.getFileName());
            assertEquals(original.getFileSize(), converted.getFileSize());
            assertEquals(original.getFileType(), converted.getFileType());
            assertEquals(original.getFileExtension(), converted.getFileExtension());
            assertEquals(original.getStorageBucket(), converted.getStorageBucket());
            assertEquals(original.getStorageKey(), converted.getStorageKey());
            assertEquals(original.getStorageUrl(), converted.getStorageUrl());
            assertEquals(original.getThumbnailUrl(), converted.getThumbnailUrl());
            assertEquals(original.getWidth(), converted.getWidth());
            assertEquals(original.getHeight(), converted.getHeight());
            assertEquals(original.getScanStatus(), converted.getScanStatus());
            assertEquals(original.getScannedAt(), converted.getScannedAt());
            assertEquals(original.getMetadata(), converted.getMetadata());
            assertEquals(original.getCreatedAt(), converted.getCreatedAt());
            assertEquals(original.getUpdatedAt(), converted.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle image with dimensions round trip")
        void shouldHandleImageWithDimensionsRoundTrip() {
            // Given
            long now = Instant.now().toEpochMilli();
            AttachmentEntity original = AttachmentEntity.builder()
                    .id(ATTACHMENT_ID)
                    .messageId(MESSAGE_ID)
                    .channelId(CHANNEL_ID)
                    .tenantId(TENANT_ID)
                    .fileName("photo.jpg")
                    .fileSize(2097152L)
                    .fileType("image/jpeg")
                    .fileExtension("jpg")
                    .storageProvider(StorageProvider.S3)
                    .storageBucket("bucket")
                    .storageKey("photos/photo.jpg")
                    .storageUrl("https://cdn.example.com/photo.jpg")
                    .thumbnailUrl("https://cdn.example.com/photo_thumb.jpg")
                    .width(4032)
                    .height(3024)
                    .scanStatus(ScanStatus.CLEAN)
                    .scannedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // When
            AttachmentModel model = attachmentMapper.toModel(original);
            AttachmentEntity converted = attachmentMapper.toEntity(model);

            // Then
            assertEquals(4032, converted.getWidth());
            assertEquals(3024, converted.getHeight());
            assertNotNull(converted.getThumbnailUrl());
        }
    }
}
