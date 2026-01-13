/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for AttachmentUrlService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.vo.StorageLocation;
import serp.project.discuss_service.core.port.client.IStoragePort;
import serp.project.discuss_service.kernel.property.StorageProperties;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttachmentUrlService.
 * Tests URL enrichment functionality for attachments.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentUrlServiceTest {

    @Mock
    private IStoragePort storagePort;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private StorageProperties.S3Properties s3Properties;

    @InjectMocks
    private AttachmentUrlService attachmentUrlService;

    private static final String PRESIGNED_URL = "http://localhost:9000/bucket/key?X-Amz-Signature=abc123";
    private static final int URL_EXPIRY_DAYS = 7;

    @BeforeEach
    void setUp() {
        lenient().when(storageProperties.getS3()).thenReturn(s3Properties);
        lenient().when(s3Properties.getDownloadUrlExpiryDays()).thenReturn(URL_EXPIRY_DAYS);
    }

    // ==================== ENRICH SINGLE ATTACHMENT TESTS ====================

    @Nested
    @DisplayName("enrichWithUrls - single attachment")
    class EnrichSingleAttachmentTests {

        @Test
        @DisplayName("should enrich attachment with presigned URL when attachment can be downloaded")
        void testEnrichWithUrls_ValidAttachment_EnrichesWithUrl() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // When
            AttachmentResponse result = attachmentUrlService.enrichWithUrls(attachment);

            // Then
            assertNotNull(result);
            assertEquals(PRESIGNED_URL, result.getDownloadUrl());
            assertNotNull(result.getUrlExpiresAt());
            assertTrue(result.getUrlExpiresAt() > System.currentTimeMillis());
            verify(storagePort).generatePresignedUrl(any(StorageLocation.class), eq(Duration.ofDays(URL_EXPIRY_DAYS)));
        }

        @Test
        @DisplayName("should set thumbnailUrl same as downloadUrl for images")
        void testEnrichWithUrls_ImageAttachment_SetsThumbnailUrl() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // When
            AttachmentResponse result = attachmentUrlService.enrichWithUrls(attachment);

            // Then
            assertNotNull(result);
            assertEquals(PRESIGNED_URL, result.getDownloadUrl());
            assertEquals(PRESIGNED_URL, result.getThumbnailUrl());
        }

        @Test
        @DisplayName("should return null when attachment is null")
        void testEnrichWithUrls_NullAttachment_ReturnsNull() {
            // When
            AttachmentResponse result = attachmentUrlService.enrichWithUrls((AttachmentEntity) null);

            // Then
            assertNull(result);
            verify(storagePort, never()).generatePresignedUrl(any(), any());
        }

        @Test
        @DisplayName("should not generate URL when attachment has no storage key")
        void testEnrichWithUrls_NoStorageKey_NoUrlGenerated() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            attachment.setStorageKey(null);

            // When
            AttachmentResponse result = attachmentUrlService.enrichWithUrls(attachment);

            // Then
            assertNotNull(result);
            assertNull(result.getDownloadUrl());
            verify(storagePort, never()).generatePresignedUrl(any(), any());
        }

        @Test
        @DisplayName("should handle storage port exception gracefully")
        void testEnrichWithUrls_StorageException_HandlesGracefully() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenThrow(new RuntimeException("S3 error"));

            // When
            AttachmentResponse result = attachmentUrlService.enrichWithUrls(attachment);

            // Then
            assertNotNull(result);
            assertNull(result.getDownloadUrl());
            // Should not throw exception
        }
    }

    // ==================== ENRICH MULTIPLE ATTACHMENTS TESTS ====================

    @Nested
    @DisplayName("enrichWithUrls - multiple attachments")
    class EnrichMultipleAttachmentsTests {

        @Test
        @DisplayName("should enrich all attachments in list")
        void testEnrichWithUrls_MultipleAttachments_EnrichesAll() {
            // Given
            List<AttachmentEntity> attachments = List.of(
                    TestDataFactory.createCleanAttachment(),
                    TestDataFactory.createCleanAttachment()
            );
            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // When
            List<AttachmentResponse> results = attachmentUrlService.enrichWithUrls(attachments);

            // Then
            assertEquals(2, results.size());
            results.forEach(r -> {
                assertEquals(PRESIGNED_URL, r.getDownloadUrl());
                assertNotNull(r.getUrlExpiresAt());
            });
            verify(storagePort, times(2)).generatePresignedUrl(any(StorageLocation.class), any(Duration.class));
        }

        @Test
        @DisplayName("should return empty list for null input")
        void testEnrichWithUrls_NullList_ReturnsEmptyList() {
            // When
            List<AttachmentResponse> results = attachmentUrlService.enrichWithUrls((List<AttachmentEntity>) null);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for empty input")
        void testEnrichWithUrls_EmptyList_ReturnsEmptyList() {
            // When
            List<AttachmentResponse> results = attachmentUrlService.enrichWithUrls(new ArrayList<>());

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    // ==================== ENRICH MESSAGE TESTS ====================

    @Nested
    @DisplayName("enrichMessageWithUrls")
    class EnrichMessageTests {

        @Test
        @DisplayName("should enrich message with attachment URLs")
        void testEnrichMessageWithUrls_MessageWithAttachments_EnrichesAttachments() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            message.setAttachments(List.of(attachment));

            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // When
            MessageResponse result = attachmentUrlService.enrichMessageWithUrls(message);

            // Then
            assertNotNull(result);
            assertNotNull(result.getAttachments());
            assertEquals(1, result.getAttachments().size());
            assertEquals(PRESIGNED_URL, result.getAttachments().get(0).getDownloadUrl());
        }

        @Test
        @DisplayName("should return message without attachments when none present")
        void testEnrichMessageWithUrls_NoAttachments_ReturnsMessageWithoutAttachments() {
            // Given
            MessageEntity message = TestDataFactory.createTextMessage();
            message.setAttachments(null);

            // When
            MessageResponse result = attachmentUrlService.enrichMessageWithUrls(message);

            // Then
            assertNotNull(result);
            verify(storagePort, never()).generatePresignedUrl(any(), any());
        }

        @Test
        @DisplayName("should return null when message is null")
        void testEnrichMessageWithUrls_NullMessage_ReturnsNull() {
            // When
            MessageResponse result = attachmentUrlService.enrichMessageWithUrls(null);

            // Then
            assertNull(result);
        }
    }

    // ==================== ENRICH MULTIPLE MESSAGES TESTS ====================

    @Nested
    @DisplayName("enrichMessagesWithUrls")
    class EnrichMultipleMessagesTests {

        @Test
        @DisplayName("should enrich all messages with attachment URLs")
        void testEnrichMessagesWithUrls_MultipleMessages_EnrichesAll() {
            // Given
            MessageEntity message1 = TestDataFactory.createTextMessage();
            message1.setAttachments(List.of(TestDataFactory.createCleanAttachment()));
            
            MessageEntity message2 = TestDataFactory.createTextMessage();
            message2.setAttachments(List.of(TestDataFactory.createCleanAttachment()));
            
            List<MessageEntity> messages = List.of(message1, message2);

            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn(PRESIGNED_URL);

            // When
            List<MessageResponse> results = attachmentUrlService.enrichMessagesWithUrls(messages);

            // Then
            assertEquals(2, results.size());
            results.forEach(r -> {
                assertNotNull(r.getAttachments());
                assertEquals(1, r.getAttachments().size());
                assertEquals(PRESIGNED_URL, r.getAttachments().get(0).getDownloadUrl());
            });
        }

        @Test
        @DisplayName("should return empty list for null input")
        void testEnrichMessagesWithUrls_NullList_ReturnsEmptyList() {
            // When
            List<MessageResponse> results = attachmentUrlService.enrichMessagesWithUrls(null);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    // ==================== URL EXPIRY TESTS ====================

    @Nested
    @DisplayName("getUrlExpiryDays")
    class UrlExpiryTests {

        @Test
        @DisplayName("should return configured expiry days")
        void testGetUrlExpiryDays_ReturnsConfiguredValue() {
            // When
            int expiryDays = attachmentUrlService.getUrlExpiryDays();

            // Then
            assertEquals(URL_EXPIRY_DAYS, expiryDays);
        }
    }
}
