/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment JPA model
 */

package serp.project.discuss_service.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import serp.project.discuss_service.core.domain.enums.StorageProvider;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "attachments", indexes = {
    @Index(name = "idx_attachments_message", columnList = "message_id"),
    @Index(name = "idx_attachments_channel", columnList = "channel_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class AttachmentModel extends BaseModel {

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Column(name = "storage_provider", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StorageProvider storageProvider = StorageProvider.S3;

    @Column(name = "storage_bucket")
    private String storageBucket;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "storage_url", length = 500)
    private String storageUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
