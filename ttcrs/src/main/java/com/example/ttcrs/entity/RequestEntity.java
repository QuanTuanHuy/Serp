package com.example.ttcrs.entity;

import com.example.ttcrs.constant.ContainerSize;
import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "requests", indexes = {
        @Index(name = "requests_transport_plan_id_index", columnList = "transport_plan_id")
})
@SQLRestriction("is_deleted = false")
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "src_location_code", length = 50)
    private String srcLocationCode;

    @Column(name = "dest_location_code", nullable = false, length = 50)
    private String destLocationCode;

    @Column(name = "early_at_src")
    private LocalDateTime earlyAtSrc;

    @Column(name = "late_at_src")
    private LocalDateTime lateAtSrc;

    @Column(name = "early_at_dest")
    private LocalDateTime earlyAtDest;

    @Column(name = "late_at_dest")
    private LocalDateTime lateAtDest;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "container_size", length = 20)
    @Enumerated(EnumType.STRING)
    private ContainerSize containerSize;

    @Column(name = "drop_trailer_required")
    private Boolean dropTrailerRequired;

    /** Lý do huỷ hoặc ghi chú bổ sung */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /** URL ảnh bằng chứng tại điểm đầu */
    @Column(name = "evidence_at_src", columnDefinition = "TEXT")
    private String evidenceAtSrc;

    /** URL ảnh bằng chứng tại điểm đích */
    @Column(name = "evidence_at_dest", columnDefinition = "TEXT")
    private String evidenceAtDest;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Column(name = "transport_plan_id")
    private Long transportPlanId;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_stamp", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
