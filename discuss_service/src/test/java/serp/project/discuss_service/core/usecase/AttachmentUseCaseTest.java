/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for AttachmentUseCase
 */

package serp.project.discuss_service.core.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.service.IAttachmentService;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttachmentUseCase.
 * Tests orchestration of attachment operations across multiple services.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentUseCaseTest {

    @Mock
    private IAttachmentService attachmentService;

    @Mock
    private IAttachmentUrlService attachmentUrlService;

    @Mock
    private IDiscussCacheService cacheService;

    @InjectMocks
    private AttachmentUseCase attachmentUseCase;

    // ==================== UPLOAD TESTS ====================

    @Nested
    @DisplayName("uploadAttachment")
    class UploadAttachmentTests {

        @Test
        @DisplayName("should upload file and return enriched response")
        void testUploadAttachment_ValidFile_ReturnsEnrichedResponse() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", "test.png", "image/png", "test content".getBytes());
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            AttachmentResponse expectedResponse = AttachmentResponse.fromEntity(attachment);
            expectedResponse.setDownloadUrl("https://presigned-url.example.com/test.png");

            when(attachmentService.uploadAttachment(eq(file), eq(TestDataFactory.MESSAGE_ID),
                    eq(TestDataFactory.CHANNEL_ID), eq(TestDataFactory.TENANT_ID)))
                    .thenReturn(attachment);
            when(attachmentUrlService.enrichWithUrls(attachment)).thenReturn(expectedResponse);

            // When
            AttachmentResponse result = attachmentUseCase.uploadAttachment(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.MESSAGE_ID,
                    file,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(result);
            assertEquals(expectedResponse.getDownloadUrl(), result.getDownloadUrl());
            verify(attachmentService).uploadAttachment(file, TestDataFactory.MESSAGE_ID,
                    TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID);
            verify(attachmentUrlService).enrichWithUrls(attachment);
        }

        @Test
        @DisplayName("should upload multiple files and return enriched responses")
        void testUploadAttachments_MultipleFiles_ReturnsEnrichedResponses() {
            // Given
            List<MultipartFile> files = List.of(
                    new MockMultipartFile("file1", "test1.png", "image/png", "content1".getBytes()),
                    new MockMultipartFile("file2", "test2.pdf", "application/pdf", "content2".getBytes())
            );
            List<AttachmentEntity> attachments = List.of(
                    TestDataFactory.createImageAttachment(),
                    TestDataFactory.createDocumentAttachment()
            );
            List<AttachmentResponse> expectedResponses = attachments.stream()
                    .map(AttachmentResponse::fromEntity)
                    .toList();

            when(attachmentService.uploadAttachments(eq(files), eq(TestDataFactory.MESSAGE_ID),
                    eq(TestDataFactory.CHANNEL_ID), eq(TestDataFactory.TENANT_ID)))
                    .thenReturn(attachments);
            when(attachmentUrlService.enrichWithUrls(attachments)).thenReturn(expectedResponses);

            // When
            List<AttachmentResponse> results = attachmentUseCase.uploadAttachments(
                    TestDataFactory.CHANNEL_ID,
                    TestDataFactory.MESSAGE_ID,
                    files,
                    TestDataFactory.TENANT_ID
            );

            // Then
            assertNotNull(results);
            assertEquals(2, results.size());
            verify(attachmentService).uploadAttachments(files, TestDataFactory.MESSAGE_ID,
                    TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID);
            verify(attachmentUrlService).enrichWithUrls(attachments);
        }
    }

    // ==================== QUERY TESTS ====================

    @Nested
    @DisplayName("getAttachment")
    class GetAttachmentTests {

        @Test
        @DisplayName("should return enriched attachment response")
        void testGetAttachment_ValidId_ReturnsEnrichedResponse() {
            // Given
            Long attachmentId = 1L;
            AttachmentEntity attachment = TestDataFactory.createCleanAttachment();
            AttachmentResponse expectedResponse = AttachmentResponse.fromEntity(attachment);
            expectedResponse.setDownloadUrl("https://presigned-url.example.com/image.png");

            when(attachmentService.getAttachment(attachmentId, TestDataFactory.TENANT_ID))
                    .thenReturn(attachment);
            when(attachmentUrlService.enrichWithUrls(attachment)).thenReturn(expectedResponse);

            // When
            AttachmentResponse result = attachmentUseCase.getAttachment(attachmentId, TestDataFactory.TENANT_ID);

            // Then
            assertNotNull(result);
            assertEquals(expectedResponse.getDownloadUrl(), result.getDownloadUrl());
            verify(attachmentService).getAttachment(attachmentId, TestDataFactory.TENANT_ID);
            verify(attachmentUrlService).enrichWithUrls(attachment);
        }
    }

    @Nested
    @DisplayName("getAttachmentsByMessage")
    class GetAttachmentsByMessageTests {

        @Test
        @DisplayName("should return list of enriched attachment responses")
        void testGetAttachmentsByMessage_HasAttachments_ReturnsEnrichedResponses() {
            // Given
            List<AttachmentEntity> attachments = List.of(
                    TestDataFactory.createCleanAttachment(),
                    TestDataFactory.createDocumentAttachment()
            );
            List<AttachmentResponse> expectedResponses = attachments.stream()
                    .map(AttachmentResponse::fromEntity)
                    .toList();

            when(attachmentService.getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID))
                    .thenReturn(attachments);
            when(attachmentUrlService.enrichWithUrls(attachments)).thenReturn(expectedResponses);

            // When
            List<AttachmentResponse> results = attachmentUseCase.getAttachmentsByMessage(
                    TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            assertNotNull(results);
            assertEquals(2, results.size());
            verify(attachmentService).getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);
            verify(attachmentUrlService).enrichWithUrls(attachments);
        }

        @Test
        @DisplayName("should return empty list when no attachments")
        void testGetAttachmentsByMessage_NoAttachments_ReturnsEmptyList() {
            // Given
            when(attachmentService.getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID))
                    .thenReturn(List.of());
            when(attachmentUrlService.enrichWithUrls(List.of())).thenReturn(List.of());

            // When
            List<AttachmentResponse> results = attachmentUseCase.getAttachmentsByMessage(
                    TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("getDownloadUrl")
    class GetDownloadUrlTests {

        @Test
        @DisplayName("should delegate to attachment service")
        void testGetDownloadUrl_ValidId_ReturnsUrl() {
            // Given
            Long attachmentId = 1L;
            String expectedUrl = "https://presigned-url.example.com/download";
            int expirationMinutes = 60;

            when(attachmentService.generateDownloadUrl(attachmentId, TestDataFactory.TENANT_ID, expirationMinutes))
                    .thenReturn(expectedUrl);

            // When
            String result = attachmentUseCase.getDownloadUrl(attachmentId, TestDataFactory.TENANT_ID, expirationMinutes);

            // Then
            assertEquals(expectedUrl, result);
            verify(attachmentService).generateDownloadUrl(attachmentId, TestDataFactory.TENANT_ID, expirationMinutes);
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("deleteAttachment")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("should delete attachment and invalidate cache")
        void testDeleteAttachment_ValidId_DeletesAndInvalidatesCache() {
            // Given
            Long attachmentId = 1L;
            doNothing().when(attachmentService).deleteAttachment(attachmentId, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1);
            doNothing().when(cacheService).invalidateAttachmentUrl(attachmentId);

            // When
            attachmentUseCase.deleteAttachment(attachmentId, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1);

            // Then
            verify(attachmentService).deleteAttachment(attachmentId, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1);
            verify(cacheService).invalidateAttachmentUrl(attachmentId);
        }
    }

    @Nested
    @DisplayName("deleteAttachmentsByMessage")
    class DeleteAttachmentsByMessageTests {

        @Test
        @DisplayName("should delete all attachments and invalidate all caches")
        void testDeleteAttachmentsByMessage_HasAttachments_DeletesAllAndInvalidatesCache() {
            // Given
            AttachmentEntity att1 = TestDataFactory.createImageAttachment();
            att1.setId(1L);
            AttachmentEntity att2 = TestDataFactory.createDocumentAttachment();
            att2.setId(2L);
            List<AttachmentEntity> attachments = List.of(att1, att2);

            when(attachmentService.getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID))
                    .thenReturn(attachments);
            doNothing().when(attachmentService).deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);
            doNothing().when(cacheService).invalidateAttachmentUrl(anyLong());

            // When
            attachmentUseCase.deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            verify(attachmentService).getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);
            verify(attachmentService).deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);
            verify(cacheService).invalidateAttachmentUrl(1L);
            verify(cacheService).invalidateAttachmentUrl(2L);
        }

        @Test
        @DisplayName("should handle empty attachment list gracefully")
        void testDeleteAttachmentsByMessage_NoAttachments_HandlesGracefully() {
            // Given
            when(attachmentService.getAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID))
                    .thenReturn(List.of());
            doNothing().when(attachmentService).deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // When
            attachmentUseCase.deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            verify(attachmentService).deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);
            verify(cacheService, never()).invalidateAttachmentUrl(anyLong());
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation Methods")
    class ValidationTests {

        @Test
        @DisplayName("isAllowedContentType should delegate to attachment service")
        void testIsAllowedContentType_DelegatesCorrectly() {
            // Given
            when(attachmentService.isAllowedContentType("image/png")).thenReturn(true);
            when(attachmentService.isAllowedContentType("application/x-executable")).thenReturn(false);

            // When/Then
            assertTrue(attachmentUseCase.isAllowedContentType("image/png"));
            assertFalse(attachmentUseCase.isAllowedContentType("application/x-executable"));
        }

        @Test
        @DisplayName("isFileSizeAllowed should delegate to attachment service")
        void testIsFileSizeAllowed_DelegatesCorrectly() {
            // Given
            when(attachmentService.isFileSizeAllowed(1024L)).thenReturn(true);
            when(attachmentService.isFileSizeAllowed(100_000_000L)).thenReturn(false);

            // When/Then
            assertTrue(attachmentUseCase.isFileSizeAllowed(1024L));
            assertFalse(attachmentUseCase.isFileSizeAllowed(100_000_000L));
        }
    }
}
