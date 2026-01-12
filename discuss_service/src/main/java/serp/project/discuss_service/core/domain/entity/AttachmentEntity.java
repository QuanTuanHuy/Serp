/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment entity
 */

package serp.project.discuss_service.core.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.ScanStatus;
import serp.project.discuss_service.core.domain.enums.StorageProvider;

import java.time.Instant;
import java.util.Map;

/**
 * Attachment entity represents a file attached to a message.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class AttachmentEntity extends BaseEntity {

    private Long messageId;
    private Long channelId;
    private Long tenantId;
    
    // File info
    private String fileName;
    private Long fileSize; // bytes
    private String fileType; // MIME type
    private String fileExtension;
    
    // Storage - generic fields for any storage provider (S3/MinIO/GCS/Azure/Local)
    @Builder.Default
    private StorageProvider storageProvider = StorageProvider.S3;
    private String storageBucket;
    private String storageKey;
    private String storageUrl;
    
    // Preview (for images/videos)
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    
    // Virus scan
    @Builder.Default
    private ScanStatus scanStatus = ScanStatus.PENDING;
    private Long scannedAt;
    
    // Metadata
    private Map<String, Object> metadata;

    // ==================== FACTORY METHODS ====================

    /**
     * Create an attachment for upload
     */
    public static AttachmentEntity create(Long messageId, Long channelId, Long tenantId,
                                          String fileName, Long fileSize, String fileType) {
        long now = Instant.now().toEpochMilli();
        
        String extension = extractExtension(fileName);
        
        return AttachmentEntity.builder()
                .messageId(messageId)
                .channelId(channelId)
                .tenantId(tenantId)
                .fileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .fileExtension(extension)
                .scanStatus(ScanStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Set storage info after upload (works with any storage provider)
     */
    public void setStorageInfo(StorageProvider provider, String bucket, String key, String url) {
        this.storageProvider = provider;
        this.storageBucket = bucket;
        this.storageKey = key;
        this.storageUrl = url;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Set storage info after upload (defaults to S3 provider for backward compatibility)
     */
    public void setStorageInfo(String bucket, String key, String url) {
        setStorageInfo(StorageProvider.S3, bucket, key, url);
    }

    /**
     * Set thumbnail info for images/videos
     */
    public void setThumbnail(String thumbnailUrl, Integer width, Integer height) {
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.height = height;
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mark file as clean (virus scan passed)
     */
    public void markClean() {
        this.scanStatus = ScanStatus.CLEAN;
        this.scannedAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mark file as infected
     */
    public void markInfected() {
        this.scanStatus = ScanStatus.INFECTED;
        this.scannedAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    /**
     * Mark scan as failed
     */
    public void markScanError() {
        this.scanStatus = ScanStatus.ERROR;
        this.scannedAt = Instant.now().toEpochMilli();
        setUpdatedAt(Instant.now().toEpochMilli());
    }

    // ==================== QUERY METHODS ====================

    @JsonIgnore
    public boolean canDownload() {
        return this.scanStatus.canDownload();
    }

    @JsonIgnore
    public boolean isPendingScan() {
        return this.scanStatus == ScanStatus.PENDING;
    }

    @JsonIgnore
    public boolean isImage() {
        return this.fileType != null && this.fileType.startsWith("image/");
    }

    @JsonIgnore
    public boolean isVideo() {
        return this.fileType != null && this.fileType.startsWith("video/");
    }

    @JsonIgnore
    public boolean isDocument() {
        return !isImage() && !isVideo();
    }

    @JsonIgnore
    public String getFileSizeFormatted() {
        if (this.fileSize == null) {
            return "0 B";
        }
        
        if (this.fileSize < 1024) {
            return this.fileSize + " B";
        } else if (this.fileSize < 1024 * 1024) {
            return String.format("%.1f KB", this.fileSize / 1024.0);
        } else if (this.fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", this.fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", this.fileSize / (1024.0 * 1024 * 1024));
        }
    }

    // ==================== HELPERS ====================

    private static String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
