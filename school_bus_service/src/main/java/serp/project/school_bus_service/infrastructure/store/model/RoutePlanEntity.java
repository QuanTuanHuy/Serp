package serp.project.school_bus_service.infrastructure.store.model;

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
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_route_plan")
@Getter
@Setter
public class RoutePlanEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_direction", nullable = false)
    private RouteDirection routeDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_location_type", nullable = false)
    private RouteLocationType startLocationType;

    @ManyToOne
    @JoinColumn(name = "start_school_id")
    private SchoolEntity startSchool;

    @ManyToOne
    @JoinColumn(name = "start_depot_id")
    private DepotEntity startDepot;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_location_type", nullable = false)
    private RouteLocationType endLocationType;

    @ManyToOne
    @JoinColumn(name = "end_school_id")
    private SchoolEntity endSchool;

    @ManyToOne
    @JoinColumn(name = "end_depot_id")
    private DepotEntity endDepot;

    @Column(name = "route_code", nullable = false)
    private String routeCode;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status;

    @Column(name = "planned_distance_km")
    private Double plannedDistanceKm;

    @Column(name = "planned_duration_min")
    private Integer plannedDurationMin;

    @Column(name = "planning_notes", columnDefinition = "TEXT")
    private String planningNotes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
