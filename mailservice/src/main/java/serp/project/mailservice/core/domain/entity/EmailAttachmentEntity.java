/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.ActiveStatus;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentEntity extends BaseEntity {
    private Long emailId;

    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private String contentType;

    private String storageLocation;
    private String checksum;

    private LocalDateTime uploadedAt;
    private LocalDateTime expiresAt;

    // ==================== Factory Methods ====================

    public static EmailAttachmentEntity createNew(Long emailId) {
        LocalDateTime now = LocalDateTime.now();
        return EmailAttachmentEntity.builder()
                .emailId(emailId)
                .uploadedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    // ==================== Validation ====================

    public void validate() {
        if (emailId == null) {
            throw new IllegalArgumentException("Email ID is required for attachment");
        }
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename is required");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type is required");
        }
    }

    // ==================== Query Methods ====================

    public boolean isExpired() {
        return isExpired(LocalDateTime.now());
    }

    public boolean isExpired(LocalDateTime referenceTime) {
        return expiresAt != null && referenceTime.isAfter(expiresAt);
    }
}
