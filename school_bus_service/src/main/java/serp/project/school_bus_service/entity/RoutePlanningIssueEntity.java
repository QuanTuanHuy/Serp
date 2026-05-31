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
import serp.project.school_bus_service.enums.PlanningIssueSeverity;

import java.time.LocalDate;

/**
 * Captures warnings and blocking errors discovered during route
 * planning, generation, or validation for a given session or route.
 */
@Entity
@Table(name = "school_bus_route_planning_issue")
@Getter
@Setter
public class RoutePlanningIssueEntity extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "planning_session_id")
    private RoutePlanningSessionEntity planningSession;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne
    @JoinColumn(name = "route_stop_id")
    private RouteStopEntity routeStop;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @Column(name = "issue_type", nullable = false, length = 50)
    private String issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private PlanningIssueSeverity severity;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = Boolean.FALSE;
}
