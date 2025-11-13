/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.ActiveStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_attachments", indexes = {
    @Index(name = "idx_email_id", columnList = "email_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_id", nullable = false)
    private Long emailId;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "storage_location", length = 50)
    private String storageLocation;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false, length = 10)
    private ActiveStatus activeStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        uploadedAt = LocalDateTime.now();
        if (activeStatus == null) {
            activeStatus = ActiveStatus.ACTIVE;
        }
        if (storageLocation == null) {
            storageLocation = "LOCAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
