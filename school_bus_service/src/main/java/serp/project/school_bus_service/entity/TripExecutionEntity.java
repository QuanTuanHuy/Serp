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
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_trip_execution")
@Getter
@Setter
public class TripExecutionEntity extends BaseModel {

    @Column(name = "trip_code", nullable = false)
    private String tripCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(name = "planned_start_at")
    private LocalDateTime plannedStartAt;

    @Column(name = "planned_end_at")
    private LocalDateTime plannedEndAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // BIGINT: consistent with published_by, assigned_by, approved_by (all store actor/user ID as Long).
    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Transient
    private BusEntity bus;

    @Transient
    private DriverProfileEntity driver;

    @Transient
    private BusAttendantProfileEntity attendant;

    @Transient
    private String routeGeometryPath;

    @Transient
    private String startLocationType;

    @Transient
    private SchoolEntity startSchool;

    @Transient
    private DepotEntity startDepot;

    @Transient
    private String endLocationType;

    @Transient
    private SchoolEntity endSchool;

    @Transient
    private DepotEntity endDepot;

    public LocalDate getServiceDate() {
        return route != null ? route.getServiceDate() : null;
    }

    public void setServiceDate(LocalDate serviceDate) {
        // Service date is derived from route.planningSession after normalization.
    }

    public RouteDirection getRouteDirection() {
        return route != null ? route.getRouteDirection() : null;
    }

    public void setRouteDirection(RouteDirection routeDirection) {
        // Route direction is derived from route.planningSession after normalization.
    }

    public String getRouteGeometryPath() {
        return routeGeometryPath != null ? routeGeometryPath : (route != null ? route.getGeometryPath() : null);
    }

    public void setRouteGeometryPath(String routeGeometryPath) {
        this.routeGeometryPath = routeGeometryPath;
    }
}
