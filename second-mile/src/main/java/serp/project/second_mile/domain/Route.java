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
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;

import java.time.LocalTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "routes")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Route extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_code", nullable = false, length = 100)
    private String routeCode;

    @Column(name = "route_name", nullable = false, length = 255)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type", nullable = false, length = 30)
    private RouteEndpointType originType;

    @Column(name = "origin_hub_id")
    private Long originHubId;

    @Column(name = "origin_post_office_code", length = 255)
    private String originPostOfficeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 30)
    private RouteDestinationType destinationType;

    @Column(name = "destination_hub_id")
    private Long destinationHubId;

    @Column(name = "destination_post_office_code", length = 255)
    private String destinationPostOfficeCode;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "fixed_departure_time")
    private LocalTime fixedDepartureTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteStatus status;

    @Column(name = "note")
    private String note;
}
