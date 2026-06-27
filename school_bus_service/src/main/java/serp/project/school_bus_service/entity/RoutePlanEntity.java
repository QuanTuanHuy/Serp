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
import serp.project.school_bus_service.enums.RouteGeometrySource;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "school_bus_route_plan")
@Getter
@Setter
public class RoutePlanEntity extends BaseModel {

    @Transient
    private BusEntity selectedBus;

    @Transient
    private RouteLocationType startLocationType;

    @Transient
    private SchoolEntity startSchool;

    @Transient
    private DepotEntity startDepot;

    @Transient
    private RouteLocationType endLocationType;

    @Transient
    private SchoolEntity endSchool;

    @Transient
    private DepotEntity endDepot;

    @Column(name = "route_code", nullable = false)
    private String routeCode;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status;

    @Column(name = "planned_distance_km")
    private Double plannedDistanceKm;

    @Column(name = "planned_duration_min")
    private Integer plannedDurationMin;

    @Column(name = "planned_student_count", nullable = false)
    private Integer plannedStudentCount = 0;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "planning_notes", columnDefinition = "TEXT")
    private String planningNotes;

    @Column(name = "geometry_path", columnDefinition = "TEXT")
    private String geometryPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "geometry_source", nullable = false)
    private RouteGeometrySource geometrySource = RouteGeometrySource.UNKNOWN;

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
    @ManyToOne(optional = false)
    @JoinColumn(name = "planning_session_id")
    private RoutePlanningSessionEntity planningSession;


    /** Minimum bus capacity required to carry all planned students. */
    @Column(name = "required_capacity")
    private Integer requiredCapacity;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    @org.hibernate.annotations.Formula("COALESCE(updated_at, created_at)")
    private LocalDateTime lastModifiedDate;

    public SchoolEntity getSchool() {
        return planningSession != null ? planningSession.getSchool() : null;
    }

    public void setSchool(SchoolEntity school) {
        // School is derived from planningSession after normalization.
    }

    public LocalDate getServiceDate() {
        return planningSession != null ? planningSession.getServiceDate() : null;
    }

    public void setServiceDate(LocalDate serviceDate) {
        // Service date is derived from planningSession after normalization.
    }

    public RouteDirection getRouteDirection() {
        return planningSession != null ? planningSession.getRouteDirection() : null;
    }

    public void setRouteDirection(RouteDirection routeDirection) {
        // Route direction is derived from planningSession after normalization.
    }
}
