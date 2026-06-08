/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "bags")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Bag extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "max_volume")
    private Double maxVolume;

    @Column(name = "max_orders")
    private Integer maxOrders;

    @Column(name = "current_weight")
    private Double currentWeight;

    @Column(name = "current_volume")
    private Double currentVolume;

    @Column(name = "current_orders")
    private Integer currentOrders;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BagStatus status;

    @Column(name = "sealed_at")
    private LocalDateTime sealedAt;

    @Column(name = "note")
    private String note;
}
