/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for AttachmentEntity
 */

package serp.project.discuss_service.core.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import serp.project.discuss_service.core.domain.enums.StorageProvider;
import serp.project.discuss_service.testutil.TestDataFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AttachmentEntity domain logic.
 * Tests factory methods, storage info, thumbnails, query methods, and file size formatting.
 */
class AttachmentEntityTest {

    // ==================== FACTORY METHOD TESTS ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - should create attachment with correct properties")
        void testCreate_ValidInput_CreatesAttachment() {
            // Given
            Long messageId = 100L;
            Long channelId = 200L;
            Long tenantId = 1L;
            String fileName = "report.pdf";
            Long fileSize = 2048L;
            String fileType = "application/pdf";

            // When
            AttachmentEntity attachment = AttachmentEntity.create(
                    messageId, channelId, tenantId, fileName, fileSize, fileType
            );

            // Then
            assertNotNull(attachment);
            assertEquals(messageId, attachment.getMessageId());
            assertEquals(channelId, attachment.getChannelId());
            assertEquals(tenantId, attachment.getTenantId());
            assertEquals(fileName, attachment.getFileName());
            assertEquals(fileSize, attachment.getFileSize());
            assertEquals(fileType, attachment.getFileType());
            assertEquals("pdf", attachment.getFileExtension());
            assertNotNull(attachment.getCreatedAt());
            assertNotNull(attachment.getUpdatedAt());
        }

        @Test
        @DisplayName("create - should extract file extension correctly")
        void testCreate_FileWithExtension_ExtractsExtension() {
            // When
            AttachmentEntity attachment = AttachmentEntity.create(
                    1L, 1L, 1L, "image.PNG", 1024L, "image/png"
            );

            // Then - extension should be lowercase
            assertEquals("png", attachment.getFileExtension());
        }

        @Test
        @DisplayName("create - should handle file without extension")
        void testCreate_FileWithoutExtension_EmptyExtension() {
            // When
            AttachmentEntity attachment = AttachmentEntity.create(
                    1L, 1L, 1L, "README", 512L, "text/plain"
            );

            // Then
            assertEquals("", attachment.getFileExtension());
        }

        @Test
        @DisplayName("create - should handle null fileName for extension")
        void testCreate_NullFileName_EmptyExtension() {
            // When
            AttachmentEntity attachment = AttachmentEntity.create(
                    1L, 1L, 1L, null, 512L, "text/plain"
            );

            // Then
            assertEquals("", attachment.getFileExtension());
        }
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Nested
    @DisplayName("Business Logic - Storage Info")
    class StorageInfoTests {

        @Test
        @DisplayName("setStorageInfo - should set storage provider, bucket, key, and URL")
        void testSetStorageInfo_ValidInput_SetsAllFields() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            String bucket = "prod-bucket";
            String key = "attachments/123/image.png";
            String url = "https://s3.amazonaws.com/prod-bucket/attachments/123/image.png";
            Long originalUpdatedAt = attachment.getUpdatedAt();

            // When
            attachment.setStorageInfo(StorageProvider.S3, bucket, key, url);

            // Then
            assertEquals(StorageProvider.S3, attachment.getStorageProvider());
            assertEquals(bucket, attachment.getStorageBucket());
            assertEquals(key, attachment.getStorageKey());
            assertEquals(url, attachment.getStorageUrl());
            assertTrue(attachment.getUpdatedAt() >= originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Business Logic - Thumbnail")
    class ThumbnailTests {

        @Test
        @DisplayName("setThumbnail - should set thumbnail URL and dimensions")
        void testSetThumbnail_ValidInput_SetsAllFields() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            String thumbnailUrl = "https://cdn.example.com/thumb/image_thumb.png";
            Integer width = 150;
            Integer height = 100;

            // When
            attachment.setThumbnail(thumbnailUrl, width, height);

            // Then
            assertEquals(thumbnailUrl, attachment.getThumbnailUrl());
            assertEquals(width, attachment.getWidth());
            assertEquals(height, attachment.getHeight());
        }
    }

    // ==================== QUERY METHOD TESTS ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("isImage - should return true for image MIME type")
        void testIsImage_ImageMimeType_ReturnsTrue() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();

            // When/Then
            assertTrue(attachment.isImage());
            assertFalse(attachment.isVideo());
            assertFalse(attachment.isDocument());
        }

        @Test
        @DisplayName("isVideo - should return true for video MIME type")
        void testIsVideo_VideoMimeType_ReturnsTrue() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createVideoAttachment();

            // When/Then
            assertTrue(attachment.isVideo());
            assertFalse(attachment.isImage());
            assertFalse(attachment.isDocument());
        }

        @Test
        @DisplayName("isDocument - should return true for non-image/non-video type")
        void testIsDocument_PdfMimeType_ReturnsTrue() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createDocumentAttachment();

            // When/Then
            assertTrue(attachment.isDocument());
            assertFalse(attachment.isImage());
            assertFalse(attachment.isVideo());
        }

        @Test
        @DisplayName("isDocument - should return true when fileType is null")
        void testIsDocument_NullMimeType_ReturnsTrue() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileType(null)
                    .build();

            // When/Then
            assertTrue(attachment.isDocument());
            assertFalse(attachment.isImage());
            assertFalse(attachment.isVideo());
        }
    }

    // ==================== FILE SIZE FORMATTING TESTS ====================

    @Nested
    @DisplayName("File Size Formatting")
    class FileSizeFormattingTests {

        @Test
        @DisplayName("getFileSizeFormatted - should format bytes correctly")
        void testGetFileSizeFormatted_Bytes_FormatsCorrectly() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileSize(500L)
                    .build();

            // When
            String formatted = attachment.getFileSizeFormatted();

            // Then
            assertEquals("500 B", formatted);
        }

        @Test
        @DisplayName("getFileSizeFormatted - should format kilobytes correctly")
        void testGetFileSizeFormatted_Kilobytes_FormatsCorrectly() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileSize(2048L) // 2 KB
                    .build();

            // When
            String formatted = attachment.getFileSizeFormatted();

            // Then
            assertEquals("2.0 KB", formatted);
        }

        @Test
        @DisplayName("getFileSizeFormatted - should format megabytes correctly")
        void testGetFileSizeFormatted_Megabytes_FormatsCorrectly() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileSize(5242880L) // 5 MB
                    .build();

            // When
            String formatted = attachment.getFileSizeFormatted();

            // Then
            assertEquals("5.0 MB", formatted);
        }

        @Test
        @DisplayName("getFileSizeFormatted - should format gigabytes correctly")
        void testGetFileSizeFormatted_Gigabytes_FormatsCorrectly() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileSize(2147483648L) // 2 GB
                    .build();

            // When
            String formatted = attachment.getFileSizeFormatted();

            // Then
            assertEquals("2.0 GB", formatted);
        }

        @Test
        @DisplayName("getFileSizeFormatted - should return '0 B' for null size")
        void testGetFileSizeFormatted_NullSize_ReturnsZeroBytes() {
            // Given
            AttachmentEntity attachment = AttachmentEntity.builder()
                    .fileSize(null)
                    .build();

            // When
            String formatted = attachment.getFileSizeFormatted();

            // Then
            assertEquals("0 B", formatted);
        }
    }
}
