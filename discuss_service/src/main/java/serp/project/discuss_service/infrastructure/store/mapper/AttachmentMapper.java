/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment entity/model mapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.infrastructure.store.model.AttachmentModel;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttachmentMapper extends BaseMapper {

    public AttachmentEntity toEntity(AttachmentModel model) {
        if (model == null) {
            return null;
        }

        return AttachmentEntity.builder()
                .id(model.getId())
                .messageId(model.getMessageId())
                .channelId(model.getChannelId())
                .tenantId(model.getTenantId())
                .fileName(model.getFileName())
                .fileSize(model.getFileSize())
                .fileType(model.getFileType())
                .fileExtension(model.getFileExtension())
                .s3Bucket(model.getS3Bucket())
                .s3Key(model.getS3Key())
                .s3Url(model.getS3Url())
                .thumbnailUrl(model.getThumbnailUrl())
                .width(model.getWidth())
                .height(model.getHeight())
                .scanStatus(model.getScanStatus())
                .scannedAt(localDateTimeToLong(model.getScannedAt()))
                .metadata(model.getMetadata())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public AttachmentModel toModel(AttachmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return AttachmentModel.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .channelId(entity.getChannelId())
                .tenantId(entity.getTenantId())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .fileType(entity.getFileType())
                .fileExtension(entity.getFileExtension())
                .s3Bucket(entity.getS3Bucket())
                .s3Key(entity.getS3Key())
                .s3Url(entity.getS3Url())
                .thumbnailUrl(entity.getThumbnailUrl())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .scanStatus(entity.getScanStatus())
                .scannedAt(longToLocalDateTime(entity.getScannedAt()))
                .metadata(entity.getMetadata())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<AttachmentEntity> toEntityList(List<AttachmentModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<AttachmentModel> toModelList(List<AttachmentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
