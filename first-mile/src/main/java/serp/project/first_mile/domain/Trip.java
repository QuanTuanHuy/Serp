package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.enums.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trips")
@EntityListeners(AuditingEntityListener.class)
public class Trip extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_code", nullable = false, length = 100)
    private String tripCode;

    @Column(name = "post_office_id", nullable = false)
    private Long postOfficeId;

    @Column(name = "courier_staff_id", nullable = false)
    private Long courierStaffId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 20)
    private PickupShift shift;

    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;

    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @Column(name = "total_travel_minutes", nullable = false)
    private Long totalTravelMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripStatus status;
}
