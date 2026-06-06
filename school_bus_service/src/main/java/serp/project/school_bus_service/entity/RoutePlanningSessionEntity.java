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
import serp.project.school_bus_service.enums.PlanningMethod;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single planning session for one (school + schedule + serviceDate + direction) context.
 * A session groups all routes produced in one planning run (manual or greedy).
 * The unique partial index uk_planning_session_context_active prevents duplicate active sessions
 * for the same context.
 */
@Entity
@Table(name = "school_bus_route_planning_session")
@Getter
@Setter
public class RoutePlanningSessionEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_schedule_id")
    private SchoolScheduleEntity schoolSchedule;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_direction", nullable = false, length = 30)
    private RouteDirection routeDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "planning_method", nullable = false, length = 30)
    private PlanningMethod planningMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PlanningSessionStatus status;

    // ── Summary counters (updated after each generate/recalculate) ──

    @Column(name = "total_eligible_students", nullable = false)
    private Integer totalEligibleStudents = 0;

    @Column(name = "total_planned_students", nullable = false)
    private Integer totalPlannedStudents = 0;

    @Column(name = "total_unassigned_students", nullable = false)
    private Integer totalUnassignedStudents = 0;

    @Column(name = "total_routes", nullable = false)
    private Integer totalRoutes = 0;

    @Column(name = "total_stops", nullable = false)
    private Integer totalStops = 0;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "total_duration_min")
    private Integer totalDurationMin;

    // ── Lifecycle timestamps ─────────────────────────────────────────

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private Long generatedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    // ── Config ───────────────────────────────────────────────────────

    @Column(name = "constraint_json", columnDefinition = "TEXT")
    private String constraintJson;

    @Column(name = "planning_notes", columnDefinition = "TEXT")
    private String planningNotes;
}
