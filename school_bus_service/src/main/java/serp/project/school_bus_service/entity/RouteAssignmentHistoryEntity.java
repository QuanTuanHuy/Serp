package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_route_assignment_history")
@Getter
@Setter
public class RouteAssignmentHistoryEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @Column(name = "old_bus_id")
    private Long oldBusId;

    @Column(name = "new_bus_id")
    private Long newBusId;

    @Column(name = "old_driver_id")
    private Long oldDriverId;

    @Column(name = "new_driver_id")
    private Long newDriverId;

    @Column(name = "old_attendant_id")
    private Long oldAttendantId;

    @Column(name = "new_attendant_id")
    private Long newAttendantId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String reason;
}

