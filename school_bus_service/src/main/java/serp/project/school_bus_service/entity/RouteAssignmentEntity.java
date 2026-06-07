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
import serp.project.school_bus_service.enums.RouteAssignmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_route_assignment")
@Getter
@Setter
public class RouteAssignmentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bus_id")
    private BusEntity bus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id")
    private DriverProfileEntity driver;

    @ManyToOne
    @JoinColumn(name = "attendant_id")
    private BusAttendantProfileEntity attendant;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    // ── Phase 1 additions ──────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RouteAssignmentStatus status = RouteAssignmentStatus.ASSIGNED;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "assignment_note", columnDefinition = "TEXT")
    private String assignmentNote;
}
