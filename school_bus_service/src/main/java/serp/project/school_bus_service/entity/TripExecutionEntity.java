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
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.ShiftType;
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

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_direction", nullable = false)
    private RouteDirection routeDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

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

    @Column(name = "planned_distance_km")
    private Double plannedDistanceKm;

    @Column(name = "planned_duration_min")
    private Integer plannedDurationMin;

    @Column(name = "actual_distance_km")
    private Double actualDistanceKm;

    @Column(name = "actual_duration_min")
    private Integer actualDurationMin;

    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // BIGINT: consistent with published_by, assigned_by, approved_by (all store actor/user ID as Long).
    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;



    @ManyToOne
    @JoinColumn(name = "bus_id")
    private BusEntity bus;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverProfileEntity driver;

    @ManyToOne
    @JoinColumn(name = "attendant_id")
    private BusAttendantProfileEntity attendant;

    @Column(name = "route_geometry_path", columnDefinition = "TEXT")
    private String routeGeometryPath;

    @Column(name = "start_location_type")
    private String startLocationType;

    @ManyToOne
    @JoinColumn(name = "start_school_id")
    private SchoolEntity startSchool;

    @ManyToOne
    @JoinColumn(name = "start_depot_id")
    private DepotEntity startDepot;

    @Column(name = "end_location_type")
    private String endLocationType;

    @ManyToOne
    @JoinColumn(name = "end_school_id")
    private SchoolEntity endSchool;

    @ManyToOne
    @JoinColumn(name = "end_depot_id")
    private DepotEntity endDepot;
}
