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
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_stats", indexes = {
    @Index(name = "idx_stats_key", columnList = "tenant_id, provider, email_type, status, stat_date, stat_hour"),
    @Index(name = "idx_tenant_date", columnList = "tenant_id, stat_date"),
    @Index(name = "idx_provider_date", columnList = "provider, stat_date")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private EmailProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", length = 30)
    private EmailType emailType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EmailStatus status;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "stat_hour")
    private Integer statHour;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "success_count")
    private Long successCount;

    @Column(name = "failed_count")
    private Long failedCount;

    @Column(name = "retry_count")
    private Long retryCount;

    @Column(name = "avg_response_time_ms")
    private Long avgResponseTimeMs;

    @Column(name = "min_response_time_ms")
    private Long minResponseTimeMs;

    @Column(name = "max_response_time_ms")
    private Long maxResponseTimeMs;

    @Column(name = "total_size_bytes")
    private Long totalSizeBytes;

    @Column(name = "attachment_count")
    private Long attachmentCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalCount == null) {
            totalCount = 0L;
        }
        if (successCount == null) {
            successCount = 0L;
        }
        if (failedCount == null) {
            failedCount = 0L;
        }
        if (retryCount == null) {
            retryCount = 0L;
        }
        if (attachmentCount == null) {
            attachmentCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
