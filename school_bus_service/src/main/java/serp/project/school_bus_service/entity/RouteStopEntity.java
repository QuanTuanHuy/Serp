package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStopPurpose;

import java.time.LocalTime;

@Entity
@Table(name = "school_bus_route_stop")
@Getter
@Setter
public class RouteStopEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    /** Nullable — only set for PICKUP_POINT stops. */
    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    /** Nullable — only set for SCHOOL stops. */
    @ManyToOne
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    /** Nullable — only set for DEPOT stops. */
    @ManyToOne
    @JoinColumn(name = "depot_id")
    private DepotEntity depot;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private RouteLocationType locationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_purpose", nullable = false)
    private RouteStopPurpose stopPurpose;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "estimated_student_count", nullable = false)
    private Integer estimatedStudentCount;

    @Column(name = "planned_boarding_count", nullable = false)
    private Integer plannedBoardingCount = 0;

    @Column(name = "planned_dropoff_count", nullable = false)
    private Integer plannedDropoffCount = 0;

    @Column(name = "estimated_travel_time_from_previous")
    private Integer estimatedTravelTimeFromPrevious;

    @Column(name = "distance_from_previous_km")
    private Double distanceFromPreviousKm;

    @Column(name = "planned_arrival_time")
    private LocalTime plannedArrivalTime;

    @Column(name = "planned_departure_time")
    private LocalTime plannedDepartureTime;

    // ── Derived helpers ───────────────────────────────────────────────────────

    /** Human-readable name derived from the linked location entity. */
    public String getDisplayName() {
        return switch (locationType) {
            case PICKUP_POINT -> pickupPoint != null ? pickupPoint.getName() : "Unknown Stop";
            case SCHOOL       -> school  != null ? school.getName()  : "School";
            case DEPOT        -> depot   != null ? depot.getName()   : "Depot";
        };
    }

    /** Latitude of the underlying location (nullable if coordinates not configured). */
    public Double getLatitude() {
        return switch (locationType) {
            case PICKUP_POINT -> pickupPoint != null ? pickupPoint.getLatitude()  : null;
            case SCHOOL       -> school  != null ? school.getLatitude()  : null;
            case DEPOT        -> depot   != null ? depot.getLatitude()   : null;
        };
    }

    /** Longitude of the underlying location (nullable if coordinates not configured). */
    public Double getLongitude() {
        return switch (locationType) {
            case PICKUP_POINT -> pickupPoint != null ? pickupPoint.getLongitude() : null;
            case SCHOOL       -> school  != null ? school.getLongitude() : null;
            case DEPOT        -> depot   != null ? depot.getLongitude()  : null;
        };
    }
}
