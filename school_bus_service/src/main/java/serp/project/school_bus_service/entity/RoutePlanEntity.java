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
import serp.project.school_bus_service.enums.RouteGenerationMethod;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


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

    @ManyToOne
    @JoinColumn(name = "selected_bus_id")
    private BusEntity selectedBus;

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

    @Column(name = "planned_student_count", nullable = false)
    private Integer plannedStudentCount = 0;

    @Column(name = "assigned_bus_capacity")
    private Integer assignedBusCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_generation_method", nullable = false)
    private RouteGenerationMethod routeGenerationMethod = RouteGenerationMethod.MANUAL;



    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "planning_notes", columnDefinition = "TEXT")
    private String planningNotes;

    @Column(name = "geometry_path", columnDefinition = "TEXT")
    private String geometryPath;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Earliest time the bus departs/arrives for this route (for conflict detection).
     * OUTBOUND: departure from first pickup. RETURN: departure from school.
     * Derived from schedule.departureTime / arrivalDeadline and set during generation.
     */
    @Column(name = "planned_start_time")
    private LocalTime plannedStartTime;

    /**
     * Latest time the bus finishes this route (for conflict detection).
     * OUTBOUND: arrives at school. RETURN: arrives at last dropoff.
     */
    @Column(name = "planned_end_time")
    private LocalTime plannedEndTime;

    // ── Phase 1 planning workspace additions ───────────────────────────


    /** The planning session this route belongs to (null for legacy/standalone routes). */
    @ManyToOne
    @JoinColumn(name = "planning_session_id")
    private RoutePlanningSessionEntity planningSession;

    /** The school schedule this route covers. Required from Phase 1 onwards. */
    @ManyToOne
    @JoinColumn(name = "school_schedule_id")
    private SchoolScheduleEntity schoolSchedule;

    /** Minimum bus capacity required to carry all planned students. */
    @Column(name = "required_capacity")
    private Integer requiredCapacity;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    @org.hibernate.annotations.Formula("COALESCE(updated_at, created_at)")
    private LocalDateTime lastModifiedDate;
}
