package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_trip_history")
@Getter
@Setter
public class TripHistoryEntity extends BaseModel {

    @OneToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @Column(name = "route_code", nullable = false)
    private String routeCode;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private BusEntity bus;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverProfileEntity driver;

    @ManyToOne
    @JoinColumn(name = "attendant_id")
    private BusAttendantProfileEntity attendant;
}
