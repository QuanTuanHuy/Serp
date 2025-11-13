/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.mapper;

import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.infrastructure.store.model.EmailAttachmentModel;

import java.util.List;
import java.util.stream.Collectors;

public class EmailAttachmentModelMapper {
    
    public static EmailAttachmentEntity toEntity(EmailAttachmentModel model) {
        if (model == null) {
            return null;
        }
        
        return EmailAttachmentEntity.builder()
                .id(model.getId())
                .emailId(model.getEmailId())
                .originalFilename(model.getOriginalFilename())
                .storedFilename(model.getStoredFilename())
                .filePath(model.getFilePath())
                .fileSize(model.getFileSize())
                .contentType(model.getContentType())
                .storageLocation(model.getStorageLocation())
                .checksum(model.getChecksum())
                .uploadedAt(model.getUploadedAt())
                .expiresAt(model.getExpiresAt())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .activeStatus(model.getActiveStatus())
                .build();
    }
    
    public static EmailAttachmentModel toModel(EmailAttachmentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return EmailAttachmentModel.builder()
                .id(entity.getId())
                .emailId(entity.getEmailId())
                .originalFilename(entity.getOriginalFilename())
                .storedFilename(entity.getStoredFilename())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .storageLocation(entity.getStorageLocation())
                .checksum(entity.getChecksum())
                .uploadedAt(entity.getUploadedAt())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .activeStatus(entity.getActiveStatus())
                .build();
    }
    
    public static List<EmailAttachmentEntity> toEntities(List<EmailAttachmentModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(EmailAttachmentModelMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<EmailAttachmentModel> toModels(List<EmailAttachmentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(EmailAttachmentModelMapper::toModel)
                .collect(Collectors.toList());
    }
    
    private EmailAttachmentModelMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
