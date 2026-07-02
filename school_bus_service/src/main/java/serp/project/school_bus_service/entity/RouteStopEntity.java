package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStopPurpose;


@Entity
@Table(name = "school_bus_route_stop")
@Getter
@Setter
public class RouteStopEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /** Nullable — only set for PICKUP_POINT stops. */
    @Transient
    private PickupPointEntity pickupPoint;

    /** Nullable — only set for SCHOOL stops. */
    @Transient
    private SchoolEntity school;

    /** Nullable — only set for DEPOT stops. */
    @Transient
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

    public void setPickupPoint(PickupPointEntity pickupPoint) {
        this.pickupPoint = pickupPoint;
        if (pickupPoint != null) {
            this.school = null;
            this.depot = null;
            this.locationType = RouteLocationType.PICKUP_POINT;
            this.locationId = pickupPoint.getId();
        }
    }

    public void setSchool(SchoolEntity school) {
        this.school = school;
        if (school != null) {
            this.pickupPoint = null;
            this.depot = null;
            this.locationType = RouteLocationType.SCHOOL;
            this.locationId = school.getId();
        }
    }

    public void setDepot(DepotEntity depot) {
        this.depot = depot;
        if (depot != null) {
            this.pickupPoint = null;
            this.school = null;
            this.locationType = RouteLocationType.DEPOT;
            this.locationId = depot.getId();
        }
    }
}
