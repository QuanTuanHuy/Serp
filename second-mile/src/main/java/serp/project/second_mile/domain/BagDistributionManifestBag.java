/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import serp.project.second_mile.enums.BagDestinationType;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(
        name = "bag_distribution_manifest_bags",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_bag_distribution_manifest_bag",
                columnNames = {"manifest_id", "bag_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class BagDistributionManifestBag extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manifest_id", nullable = false)
    private BagDistributionManifest manifest;

    @Column(name = "bag_id", nullable = false)
    private Long bagId;

    @Column(name = "bag_code", nullable = false, length = 100)
    private String bagCode;

    @Column(name = "origin_hub_id", nullable = false)
    private Long originHubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 30)
    private BagDestinationType destinationType;

    @Column(name = "destination_hub_id")
    private Long destinationHubId;

    @Column(name = "destination_post_office_code", length = 255)
    private String destinationPostOfficeCode;

    @Column(name = "total_weight_snapshot")
    private Double totalWeightSnapshot;

    @Column(name = "total_volume_snapshot")
    private Double totalVolumeSnapshot;

    @Column(name = "total_orders_snapshot")
    private Integer totalOrdersSnapshot;

    @Column(name = "scan_out_time")
    private LocalDateTime scanOutTime;

    @Column(name = "scan_in_time")
    private LocalDateTime scanInTime;
}
