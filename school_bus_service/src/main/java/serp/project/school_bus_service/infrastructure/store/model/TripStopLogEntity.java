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
import serp.project.school_bus_service.enums.TripStopStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_trip_stop_log")
@Getter
@Setter
public class TripStopLogEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id")
    private TripExecutionEntity trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_stop_id")
    private RouteStopEntity routeStop;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStopStatus status;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(name = "actual_boarded_count", nullable = false)
    private Integer actualBoardedCount = 0;

    @Column(name = "actual_dropped_count", nullable = false)
    private Integer actualDroppedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String note;
}

