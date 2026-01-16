/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for AttachmentService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.vo.FileUploadResult;
import serp.project.discuss_service.core.domain.vo.StorageLocation;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.client.IStoragePort;
import serp.project.discuss_service.core.port.store.IAttachmentPort;
import serp.project.discuss_service.kernel.property.StorageProperties;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttachmentService.
 * Tests all attachment operations with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private IStoragePort storagePort;

    @Mock
    private IAttachmentPort attachmentPort;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private StorageProperties.UploadLimits uploadLimits;

    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        // Default upload limits setup
        lenient().when(storageProperties.getUpload()).thenReturn(uploadLimits);
        lenient().when(uploadLimits.getMaxFileSize()).thenReturn(50L * 1024 * 1024); // 50MB
        lenient().when(uploadLimits.getMaxFilesPerMessage()).thenReturn(10);
        lenient().when(uploadLimits.getAllowedContentTypes()).thenReturn(new String[]{
                "image/jpeg", "image/png", "image/gif",
                "application/pdf",
                "video/mp4"
        });
        
        // Create a synchronous executor for tests (runs tasks immediately in the same thread)
        ExecutorService syncExecutor = Executors.newSingleThreadExecutor();
        
        // Manually construct the service with all dependencies
        attachmentService = new AttachmentService(
                storagePort,
                attachmentPort,
                storageProperties,
                syncExecutor
        );
    }

    // ==================== UPLOAD ATTACHMENT TESTS ====================

    @Nested
    @DisplayName("uploadAttachment")
    class UploadAttachmentTests {

        @Test
        @DisplayName("should upload file successfully and save attachment")
        void testUploadAttachment_ValidFile_UploadsAndSaves() throws IOException {
            // Given
            MultipartFile file = createMockMultipartFile("test.png", "image/png", 1024L);
            StorageLocation location = StorageLocation.ofS3(
                    "test-bucket",
                    "attachments/tenant1/channel1/test.png",
                    "https://s3.example.com/test-bucket/attachments/test.png"
            );
            FileUploadResult uploadResult = FileUploadResult.success(location, "image/png", 1024L);

            when(storagePort.upload(any(InputStream.class), eq("test.png"), eq("image/png"),
                    eq(1024L), eq(TestDataFactory.TENANT_ID), eq(TestDataFactory.CHANNEL_ID)))
                    .thenReturn(uploadResult);

            AttachmentEntity savedAttachment = TestDataFactory.createImageAttachment();
            when(attachmentPort.save(any(AttachmentEntity.class))).thenReturn(savedAttachment);

            // When
            AttachmentEntity result = attachmentService.uploadAttachment(
                    file, TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID);

            // Then
            assertNotNull(result);
            assertEquals("image.png", result.getFileName());
            verify(storagePort).upload(any(InputStream.class), eq("test.png"), eq("image/png"),
                    eq(1024L), eq(TestDataFactory.TENANT_ID), eq(TestDataFactory.CHANNEL_ID));
            verify(attachmentPort).save(any(AttachmentEntity.class));
        }

        @Test
        @DisplayName("should throw FILE_REQUIRED when file is null")
        void testUploadAttachment_NullFile_ThrowsFileRequired() {
            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(null,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_REQUIRED.getMessage(), exception.getMessage());
            verify(storagePort, never()).upload(any(), any(), any(), anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("should throw FILE_REQUIRED when file is empty")
        void testUploadAttachment_EmptyFile_ThrowsFileRequired() throws IOException {
            // Given
            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(emptyFile,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_REQUIRED.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("should throw FILE_TOO_LARGE when file exceeds max size")
        void testUploadAttachment_FileTooLarge_ThrowsFileTooLarge() throws IOException {
            // Given - file larger than 50MB limit
            MultipartFile largeFile = createMockMultipartFile("large.pdf", "application/pdf", 100L * 1024 * 1024);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(largeFile,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_TOO_LARGE.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("should throw FILE_TYPE_NOT_ALLOWED when content type not allowed")
        void testUploadAttachment_DisallowedType_ThrowsFileTypeNotAllowed() throws IOException {
            // Given - executable file type not in allowed list
            MultipartFile exeFile = createMockMultipartFile("virus.exe", "application/x-msdownload", 1024L);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(exeFile,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_TYPE_NOT_ALLOWED.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("should throw FILE_UPLOAD_FAILED when storage upload fails")
        void testUploadAttachment_StorageFailure_ThrowsUploadFailed() throws IOException {
            // Given
            MultipartFile file = createMockMultipartFile("test.png", "image/png", 1024L);
            FileUploadResult failedResult = FileUploadResult.failure("Storage unavailable");

            when(storagePort.upload(any(InputStream.class), any(), any(), anyLong(), anyLong(), anyLong()))
                    .thenReturn(failedResult);

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(file,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_UPLOAD_FAILED.getMessage(), exception.getMessage());
            verify(attachmentPort, never()).save(any());
        }

        @Test
        @DisplayName("should throw FILE_UPLOAD_FAILED when IOException occurs")
        void testUploadAttachment_IOException_ThrowsUploadFailed() throws IOException {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getOriginalFilename()).thenReturn("test.png");
            when(file.getInputStream()).thenThrow(new IOException("Stream error"));

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachment(file,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.FILE_UPLOAD_FAILED.getMessage(), exception.getMessage());
        }
    }

    // ==================== UPLOAD MULTIPLE ATTACHMENTS TESTS ====================

    @Nested
    @DisplayName("uploadAttachments")
    class UploadMultipleAttachmentsTests {

        @Test
        @DisplayName("should upload multiple files successfully")
        void testUploadAttachments_MultipleValidFiles_UploadsAll() throws IOException {
            // Given
            MultipartFile file1 = createMockMultipartFile("file1.png", "image/png", 1024L);
            MultipartFile file2 = createMockMultipartFile("file2.pdf", "application/pdf", 2048L);
            List<MultipartFile> files = List.of(file1, file2);

            StorageLocation location = StorageLocation.ofS3("test-bucket", "key", "url");
            FileUploadResult uploadResult = FileUploadResult.success(location);

            when(storagePort.upload(any(InputStream.class), any(), any(), anyLong(), anyLong(), anyLong()))
                    .thenReturn(uploadResult);

            AttachmentEntity saved1 = TestDataFactory.createImageAttachment();
            AttachmentEntity saved2 = TestDataFactory.createDocumentAttachment();
            when(attachmentPort.save(any(AttachmentEntity.class)))
                    .thenReturn(saved1)
                    .thenReturn(saved2);

            // When
            List<AttachmentEntity> result = attachmentService.uploadAttachments(
                    files, TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID);

            // Then
            assertEquals(2, result.size());
            verify(storagePort, times(2)).upload(any(InputStream.class), any(), any(), anyLong(), anyLong(), anyLong());
            verify(attachmentPort, times(2)).save(any(AttachmentEntity.class));
        }

        @Test
        @DisplayName("should throw TOO_MANY_FILES when exceeding limit")
        void testUploadAttachments_TooManyFiles_ThrowsTooManyFiles() {
            // Given - limit is 10, try to upload 11
            when(uploadLimits.getMaxFilesPerMessage()).thenReturn(10);
            
            // Create simple mocks - no stubbing needed since validation fails before any file is processed
            List<MultipartFile> files = new java.util.ArrayList<>();
            for (int i = 0; i < 11; i++) {
                files.add(mock(MultipartFile.class));
            }

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.uploadAttachments(files,
                            TestDataFactory.MESSAGE_ID, TestDataFactory.CHANNEL_ID, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.TOO_MANY_FILES.getMessage(), exception.getMessage());
        }
    }

    // ==================== GET ATTACHMENT TESTS ====================

    @Nested
    @DisplayName("getAttachment")
    class GetAttachmentTests {

        @Test
        @DisplayName("should return attachment when found and tenant matches")
        void testGetAttachment_FoundAndTenantMatches_ReturnsAttachment() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            when(attachmentPort.findById(1L)).thenReturn(Optional.of(attachment));

            // When
            AttachmentEntity result = attachmentService.getAttachment(1L, TestDataFactory.TENANT_ID);

            // Then
            assertNotNull(result);
            assertEquals(attachment.getId(), result.getId());
            verify(attachmentPort).findById(1L);
        }

        @Test
        @DisplayName("should throw ATTACHMENT_NOT_FOUND when not found")
        void testGetAttachment_NotFound_ThrowsAttachmentNotFound() {
            // Given
            when(attachmentPort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.getAttachment(999L, TestDataFactory.TENANT_ID));

            assertEquals(ErrorCode.ATTACHMENT_NOT_FOUND.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("should throw ATTACHMENT_NOT_FOUND when tenant does not match")
        void testGetAttachment_TenantMismatch_ThrowsAttachmentNotFound() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            attachment.setTenantId(1L);
            when(attachmentPort.findById(1L)).thenReturn(Optional.of(attachment));

            // When/Then - requesting with different tenant
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.getAttachment(1L, 999L));

            assertEquals(ErrorCode.ATTACHMENT_NOT_FOUND.getMessage(), exception.getMessage());
        }
    }

    // ==================== GET ATTACHMENTS BY MESSAGE TESTS ====================

    @Nested
    @DisplayName("getAttachmentsByMessage")
    class GetAttachmentsByMessageTests {

        @Test
        @DisplayName("should return attachments for message")
        void testGetAttachmentsByMessage_HasAttachments_ReturnsAll() {
            // Given
            List<AttachmentEntity> attachments = List.of(
                    TestDataFactory.createImageAttachment(),
                    TestDataFactory.createDocumentAttachment()
            );
            when(attachmentPort.findByMessageId(TestDataFactory.MESSAGE_ID)).thenReturn(attachments);

            // When
            List<AttachmentEntity> result = attachmentService.getAttachmentsByMessage(
                    TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            assertEquals(2, result.size());
            verify(attachmentPort).findByMessageId(TestDataFactory.MESSAGE_ID);
        }

        @Test
        @DisplayName("should return empty list when no attachments")
        void testGetAttachmentsByMessage_NoAttachments_ReturnsEmpty() {
            // Given
            when(attachmentPort.findByMessageId(TestDataFactory.MESSAGE_ID)).thenReturn(List.of());

            // When
            List<AttachmentEntity> result = attachmentService.getAttachmentsByMessage(
                    TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    // ==================== GENERATE DOWNLOAD URL TESTS ====================

    @Nested
    @DisplayName("generateDownloadUrl")
    class GenerateDownloadUrlTests {

        @Test
        @DisplayName("should generate presigned URL for attachment with storage key")
        void testGenerateDownloadUrl_AttachmentWithStorageKey_ReturnsUrl() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            when(attachmentPort.findById(1L)).thenReturn(Optional.of(attachment));
            when(storagePort.generatePresignedUrl(any(StorageLocation.class), any(Duration.class)))
                    .thenReturn("https://s3.example.com/presigned-url");

            // When
            String url = attachmentService.generateDownloadUrl(1L, TestDataFactory.TENANT_ID, 60);

            // Then
            assertEquals("https://s3.example.com/presigned-url", url);
            verify(storagePort).generatePresignedUrl(any(StorageLocation.class), eq(Duration.ofMinutes(60)));
        }
    }

    // ==================== DELETE ATTACHMENT TESTS ====================

    @Nested
    @DisplayName("deleteAttachment")
    class DeleteAttachmentTests {

        @Test
        @DisplayName("should delete from storage and database")
        void testDeleteAttachment_ValidAttachment_DeletesBoth() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            when(attachmentPort.findById(1L)).thenReturn(Optional.of(attachment));
            when(storagePort.delete(any(StorageLocation.class))).thenReturn(true);

            // When
            attachmentService.deleteAttachment(1L, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1);

            // Then
            verify(storagePort).delete(any(StorageLocation.class));
            verify(attachmentPort).deleteById(1L);
        }

        @Test
        @DisplayName("should still delete from database even if storage delete fails")
        void testDeleteAttachment_StorageFails_StillDeletesFromDb() {
            // Given
            AttachmentEntity attachment = TestDataFactory.createImageAttachment();
            when(attachmentPort.findById(1L)).thenReturn(Optional.of(attachment));
            when(storagePort.delete(any(StorageLocation.class))).thenThrow(new RuntimeException("Storage error"));

            // When
            attachmentService.deleteAttachment(1L, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1);

            // Then - should still delete from database
            verify(attachmentPort).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ATTACHMENT_NOT_FOUND when attachment not found")
        void testDeleteAttachment_NotFound_ThrowsAttachmentNotFound() {
            // Given
            when(attachmentPort.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            AppException exception = assertThrows(AppException.class,
                    () -> attachmentService.deleteAttachment(999L, TestDataFactory.TENANT_ID, TestDataFactory.USER_ID_1));

            assertEquals(ErrorCode.ATTACHMENT_NOT_FOUND.getMessage(), exception.getMessage());
            verify(storagePort, never()).delete(any());
            verify(attachmentPort, never()).deleteById(anyLong());
        }
    }

    // ==================== DELETE BY MESSAGE TESTS ====================

    @Nested
    @DisplayName("deleteAttachmentsByMessage")
    class DeleteAttachmentsByMessageTests {

        @Test
        @DisplayName("should delete all attachments for message")
        void testDeleteAttachmentsByMessage_HasAttachments_DeletesAll() {
            // Given
            List<AttachmentEntity> attachments = List.of(
                    TestDataFactory.createImageAttachment(),
                    TestDataFactory.createDocumentAttachment()
            );
            when(attachmentPort.findByMessageId(TestDataFactory.MESSAGE_ID)).thenReturn(attachments);
            when(storagePort.delete(any(StorageLocation.class))).thenReturn(true);

            // When
            attachmentService.deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            verify(storagePort, times(2)).delete(any(StorageLocation.class));
            verify(attachmentPort).deleteByMessageId(TestDataFactory.MESSAGE_ID);
        }

        @Test
        @DisplayName("should handle empty attachment list")
        void testDeleteAttachmentsByMessage_NoAttachments_HandlesGracefully() {
            // Given
            when(attachmentPort.findByMessageId(TestDataFactory.MESSAGE_ID)).thenReturn(List.of());

            // When
            attachmentService.deleteAttachmentsByMessage(TestDataFactory.MESSAGE_ID, TestDataFactory.TENANT_ID);

            // Then
            verify(storagePort, never()).delete(any());
            verify(attachmentPort).deleteByMessageId(TestDataFactory.MESSAGE_ID);
        }
    }

    // ==================== CONTENT TYPE VALIDATION TESTS ====================

    @Nested
    @DisplayName("isAllowedContentType")
    class IsAllowedContentTypeTests {

        @Test
        @DisplayName("should return true for allowed image types")
        void testIsAllowedContentType_AllowedImage_ReturnsTrue() {
            assertTrue(attachmentService.isAllowedContentType("image/png"));
            assertTrue(attachmentService.isAllowedContentType("image/jpeg"));
        }

        @Test
        @DisplayName("should return true for allowed document types")
        void testIsAllowedContentType_AllowedDocument_ReturnsTrue() {
            assertTrue(attachmentService.isAllowedContentType("application/pdf"));
        }

        @Test
        @DisplayName("should return false for disallowed types")
        void testIsAllowedContentType_DisallowedType_ReturnsFalse() {
            assertFalse(attachmentService.isAllowedContentType("application/x-msdownload"));
            assertFalse(attachmentService.isAllowedContentType("application/x-executable"));
        }

        @Test
        @DisplayName("should return false for null content type")
        void testIsAllowedContentType_NullType_ReturnsFalse() {
            assertFalse(attachmentService.isAllowedContentType(null));
        }

        @Test
        @DisplayName("should allow all types when no restrictions configured")
        void testIsAllowedContentType_NoRestrictions_AllowsAll() {
            // Given
            when(uploadLimits.getAllowedContentTypes()).thenReturn(new String[]{});

            // When/Then
            assertTrue(attachmentService.isAllowedContentType("application/x-msdownload"));
            assertTrue(attachmentService.isAllowedContentType("any/type"));
        }
    }

    // ==================== FILE SIZE VALIDATION TESTS ====================

    @Nested
    @DisplayName("isFileSizeAllowed")
    class IsFileSizeAllowedTests {

        @Test
        @DisplayName("should return true for file within limit")
        void testIsFileSizeAllowed_WithinLimit_ReturnsTrue() {
            assertTrue(attachmentService.isFileSizeAllowed(10 * 1024 * 1024)); // 10MB
            assertTrue(attachmentService.isFileSizeAllowed(50 * 1024 * 1024)); // Exactly 50MB
        }

        @Test
        @DisplayName("should return false for file exceeding limit")
        void testIsFileSizeAllowed_ExceedsLimit_ReturnsFalse() {
            assertFalse(attachmentService.isFileSizeAllowed(51 * 1024 * 1024)); // 51MB
            assertFalse(attachmentService.isFileSizeAllowed(100 * 1024 * 1024)); // 100MB
        }

        @Test
        @DisplayName("should return true for zero-size file")
        void testIsFileSizeAllowed_ZeroSize_ReturnsTrue() {
            assertTrue(attachmentService.isFileSizeAllowed(0));
        }
    }

    // ==================== HELPER METHODS ====================

    private MultipartFile createMockMultipartFile(String filename, String contentType, long size) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getOriginalFilename()).thenReturn(filename);
        lenient().when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getSize()).thenReturn(size);
        lenient().when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[(int) Math.min(size, 1024)]));
        return file;
    }
}
