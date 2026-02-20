/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment service implementation
 */

package serp.project.discuss_service.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.vo.FileUploadResult;
import serp.project.discuss_service.core.domain.vo.StorageLocation;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.port.client.IStoragePort;
import serp.project.discuss_service.core.port.store.IAttachmentPort;
import serp.project.discuss_service.core.service.IAttachmentService;
import serp.project.discuss_service.kernel.property.StorageProperties;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttachmentService implements IAttachmentService {

    private final IStoragePort storagePort;
    private final IAttachmentPort attachmentPort;
    private final StorageProperties storageProperties;
    private final ExecutorService attachmentUploadExecutor;

    public AttachmentService(
            IStoragePort storagePort,
            IAttachmentPort attachmentPort,
            StorageProperties storageProperties,
            @Qualifier("attachmentUploadExecutor") ExecutorService attachmentUploadExecutor) {
        this.storagePort = storagePort;
        this.attachmentPort = attachmentPort;
        this.storageProperties = storageProperties;
        this.attachmentUploadExecutor = attachmentUploadExecutor;
    }

    @Override
    @Transactional
    public AttachmentEntity uploadAttachment(MultipartFile file, Long messageId, Long channelId, Long tenantId) {
        validateFile(file);

        AttachmentEntity attachment = AttachmentEntity.create(
                messageId,
                channelId,
                tenantId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );

        try {
            FileUploadResult uploadResult = storagePort.upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    tenantId,
                    channelId
            );

            if (uploadResult.isFailed()) {
                log.error("Failed to upload file {} for message {}: {}",
                        file.getOriginalFilename(), messageId, uploadResult.getErrorMessage());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            StorageLocation location = uploadResult.getStorageLocation();
            attachment.setStorageInfo(
                    location.getProvider(),
                    location.getBucket(),
                    location.getKey(),
                    location.getUrl()
            );

            AttachmentEntity savedAttachment = attachmentPort.save(attachment);

            log.info("Successfully uploaded attachment {} for message {} in channel {}",
                    savedAttachment.getId(), messageId, channelId);

            return savedAttachment;

        } catch (IOException e) {
            log.error("IO error while uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public List<AttachmentEntity> uploadAttachments(List<MultipartFile> files, Long messageId, Long channelId, Long tenantId) {
        if (files.size() > storageProperties.getUpload().getMaxFilesPerMessage()) {
            throw new AppException(ErrorCode.TOO_MANY_FILES);
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }

        List<CompletableFuture<AttachmentEntity>> uploadFutures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(
                        () -> uploadFileToStorage(file, messageId, channelId, tenantId),
                        attachmentUploadExecutor))
                .toList();

        List<AttachmentEntity> uploadedAttachments;
        try {
            uploadedAttachments = uploadFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } catch (CompletionException e) {
            log.error("Failed to upload files to storage in parallel", e.getCause());
            if (e.getCause() instanceof AppException appException) {
                throw appException;
            }
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // This ensures they can see the committed message and share the same transaction
        List<AttachmentEntity> savedAttachments = new ArrayList<>();
        for (AttachmentEntity attachment : uploadedAttachments) {
            AttachmentEntity saved = attachmentPort.save(attachment);
            savedAttachments.add(saved);
            log.info("Saved attachment {} for message {} in channel {}",
                    saved.getId(), messageId, channelId);
        }

        log.info("Successfully uploaded and saved {} attachments for message {}", 
                savedAttachments.size(), messageId);
        return savedAttachments;
    }

    /**
     * Upload a single file to storage (S3/MinIO) WITHOUT saving to database.
     */
    private AttachmentEntity uploadFileToStorage(MultipartFile file, Long messageId, Long channelId, Long tenantId) {
        AttachmentEntity attachment = AttachmentEntity.create(
                messageId,
                channelId,
                tenantId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
        );

        try {
            FileUploadResult uploadResult = storagePort.upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    tenantId,
                    channelId
            );

            if (uploadResult.isFailed()) {
                log.error("Failed to upload file {} to storage for message {}: {}",
                        file.getOriginalFilename(), messageId, uploadResult.getErrorMessage());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            StorageLocation location = uploadResult.getStorageLocation();
            attachment.setStorageInfo(
                    location.getProvider(),
                    location.getBucket(),
                    location.getKey(),
                    location.getUrl()
            );

            log.debug("Uploaded file {} to storage: {}", file.getOriginalFilename(), location.getKey());
            return attachment;

        } catch (IOException e) {
            log.error("IO error while uploading file {} to storage: {}", 
                    file.getOriginalFilename(), e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public AttachmentEntity getAttachment(Long attachmentId, Long tenantId) {
        AttachmentEntity attachment = attachmentPort.findById(attachmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTACHMENT_NOT_FOUND));

        if (!attachment.getTenantId().equals(tenantId)) {
            log.warn("Tenant {} attempted to access attachment {} belonging to tenant {}",
                    tenantId, attachmentId, attachment.getTenantId());
            throw new AppException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }

        return attachment;
    }

    @Override
    public List<AttachmentEntity> getAttachmentsByMessage(Long messageId, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentPort.findByMessageId(messageId);
        return attachments.stream()
                .filter(att -> att.getTenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    @Override
    public String generateDownloadUrl(Long attachmentId, Long tenantId, int expirationMinutes) {
        AttachmentEntity attachment = getAttachment(attachmentId, tenantId);

        StorageLocation location = StorageLocation.ofS3(
                attachment.getStorageBucket(),
                attachment.getStorageKey()
        );

        return storagePort.generatePresignedUrl(location, Duration.ofMinutes(expirationMinutes));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long tenantId, Long userId) {
        AttachmentEntity attachment = getAttachment(attachmentId, tenantId);

        StorageLocation location = StorageLocation.ofS3(
                attachment.getStorageBucket(),
                attachment.getStorageKey()
        );

        try {
            storagePort.delete(location);
        } catch (Exception e) {
            log.error("Failed to delete file from storage: {} - {}", attachment.getStorageKey(), e.getMessage());
        }

        attachmentPort.deleteById(attachmentId);

        log.info("Attachment {} deleted by user {} from tenant {}", attachmentId, userId, tenantId);
    }

    @Override
    @Transactional
    public void deleteAttachmentsByMessage(Long messageId, Long tenantId) {
        List<AttachmentEntity> attachments = getAttachmentsByMessage(messageId, tenantId);

        for (AttachmentEntity attachment : attachments) {
            StorageLocation location = StorageLocation.ofS3(
                    attachment.getStorageBucket(),
                    attachment.getStorageKey()
            );
            try {
                storagePort.delete(location);
            } catch (Exception e) {
                log.error("Failed to delete file from storage: {} - {}", attachment.getStorageKey(), e.getMessage());
            }
        }

        attachmentPort.deleteByMessageId(messageId);
        log.info("Deleted {} attachments for message {}", attachments.size(), messageId);
    }

    @Override
    public Map<Long, List<AttachmentEntity>> getAttachmentsByMessageIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<AttachmentEntity> allAttachments = attachmentPort.findByMessageIds(messageIds);
        
        return allAttachments.stream()
                .collect(Collectors.groupingBy(AttachmentEntity::getMessageId));
    }

    @Override
    public boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String[] allowedTypes = storageProperties.getUpload().getAllowedContentTypes();
        if (allowedTypes == null || allowedTypes.length == 0) {
            return true;
        }
        return Arrays.stream(allowedTypes).anyMatch(contentType::startsWith);
    }

    @Override
    public boolean isFileSizeAllowed(long fileSize) {
        return fileSize <= storageProperties.getUpload().getMaxFileSize();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_REQUIRED);
        }

        if (!isFileSizeAllowed(file.getSize())) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        if (!isAllowedContentType(file.getContentType())) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }
}
