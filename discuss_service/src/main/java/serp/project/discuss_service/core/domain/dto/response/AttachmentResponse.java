/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.enums.ScanStatus;
import serp.project.discuss_service.core.domain.enums.StorageProvider;

import java.util.Map;

/**
 * Response DTO for attachment data
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AttachmentResponse {
    
    private Long id;
    private Long messageId;
    private Long channelId;
    private Long tenantId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String fileExtension;
    private StorageProvider storageProvider;
    private String storageBucket;
    private String storageKey;
    private String storageUrl;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private ScanStatus scanStatus;
    private Long scannedAt;
    private Map<String, Object> metadata;
    private Long createdAt;
    private Long updatedAt;
    
    // Computed fields
    private String fileSizeFormatted;
    private Boolean canDownload;

    public static AttachmentResponse fromEntity(AttachmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return AttachmentResponse.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .channelId(entity.getChannelId())
                .tenantId(entity.getTenantId())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .fileType(entity.getFileType())
                .fileExtension(entity.getFileExtension())
                .storageProvider(entity.getStorageProvider())
                .storageBucket(entity.getStorageBucket())
                .storageKey(entity.getStorageKey())
                .storageUrl(entity.getStorageUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .scanStatus(entity.getScanStatus())
                .scannedAt(entity.getScannedAt())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .fileSizeFormatted(entity.getFileSizeFormatted())
                .canDownload(entity.canDownload())
                .build();
    }
}
