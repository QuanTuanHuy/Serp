package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "school_bus_route_stop")
@Getter
@Setter
public class RouteStopEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "estimated_student_count", nullable = false)
    private Integer estimatedStudentCount;

    @Column(name = "planned_arrival_time")
    private LocalTime plannedArrivalTime;

    @Column(name = "planned_departure_time")
    private LocalTime plannedDepartureTime;
}
