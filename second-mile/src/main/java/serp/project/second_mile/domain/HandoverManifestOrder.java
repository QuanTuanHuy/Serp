/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(
        name = "handover_manifest_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_handover_manifest_order",
                columnNames = {"manifest_id", "order_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class HandoverManifestOrder extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manifest_id", nullable = false)
    private HandoverManifest manifest;

    @Column(name = "order_id", nullable = false)
    private Long tmsOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "customer_order_code")
    private String customerOrderCode;

    @Column(name = "last_known_status")
    private String lastKnownStatus;

    @Column(name = "origin_post_office_code")
    private String originPostOfficeCode;

    @Column(name = "destination_post_office_code")
    private String destinationPostOfficeCode;

    @Column(name = "total_weight_snapshot")
    private Double totalWeightSnapshot;

    @Column(name = "total_volume_snapshot")
    private Double totalVolumeSnapshot;

    @Column(name = "scan_out_time")
    private LocalDateTime scanOutTime;

    @Column(name = "scan_in_time")
    private LocalDateTime scanInTime;
}
