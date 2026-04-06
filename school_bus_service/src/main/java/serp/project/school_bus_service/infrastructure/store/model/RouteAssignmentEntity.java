package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
}
