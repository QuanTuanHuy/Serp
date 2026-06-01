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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.HandoverManifestStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "handover_manifests")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class HandoverManifest extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manifest_code", nullable = false, length = 100)
    private String manifestCode;

    @Column(name = "origin_post_office_code", nullable = false, length = 255)
    private String originPostOfficeCode;

    @Column(name = "target_hub_id", nullable = false)
    private Long targetHubId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "assigned_driver_id")
    private Long assignedDriverId;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "planned_departure_at")
    private LocalDateTime plannedDepartureAt;

    @Column(name = "planned_arrival_at")
    private LocalDateTime plannedArrivalAt;

    @Column(name = "origin_post_office_latitude")
    private Double originPostOfficeLatitude;

    @Column(name = "origin_post_office_longitude")
    private Double originPostOfficeLongitude;

    @Column(name = "driver_start_checkin_at")
    private LocalDateTime driverStartCheckinAt;

    @Column(name = "driver_start_latitude")
    private Double driverStartLatitude;

    @Column(name = "driver_start_longitude")
    private Double driverStartLongitude;

    @Column(name = "driver_start_distance_m")
    private Double driverStartDistanceM;

    @Column(name = "driver_end_checkin_at")
    private LocalDateTime driverEndCheckinAt;

    @Column(name = "driver_end_latitude")
    private Double driverEndLatitude;

    @Column(name = "driver_end_longitude")
    private Double driverEndLongitude;

    @Column(name = "driver_end_distance_m")
    private Double driverEndDistanceM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private HandoverManifestStatus status;
}
