package serp.project.tms_order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@Table(name = "order_history")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "customer_order_code")
    private String customerOrderCode;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "post_office_code")
    private String postOfficeCode;

    @Column(name = "post_office_id")
    private Long postOfficeId;

    @Column(name = "post_office_name")
    private String postOfficeName;

    @Column(name = "staff_code")
    private String staffCode;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "trip_code")
    private String tripCode;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_license_plate")
    private String vehicleLicensePlate;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_label")
    private String locationLabel;

    @Column(name = "event_time")
    private LocalDateTime eventTime;
}
