/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.PaymentStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trip_order")
@EntityListeners(AuditingEntityListener.class)
public class TripOrder extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "distance_from_previous_km")
    private Double distanceFromPreviousKm;

    @Column(name = "travel_minutes")
    private Long travelMinutes;

    @Column(name = "planned_arrival_time")
    private LocalDateTime plannedArrivalTime;

    @Column(name = "planned_start_service_time")
    private LocalDateTime plannedStartServiceTime;

    @Column(name = "planned_departure_time")
    private LocalDateTime plannedDepartureTime;

    @Column(name = "lateness_minutes")
    private Long latenessMinutes;

    @Column(name = "scan_out_time")
    private LocalDateTime scanOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 50)
    private DeliveryOrderStatus deliveryStatus;

    @Column(name = "delivery_attempt_count", nullable = false)
    @Builder.Default
    private Integer deliveryAttemptCount = 0;

    @Column(name = "cod_collected", nullable = false)
    @Builder.Default
    private Long codCollected = 0L;

    @Column(name = "shipping_fee_collected", nullable = false)
    @Builder.Default
    private Long shippingFeeCollected = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus deliveryPaymentStatus = PaymentStatus.UNPAID;

    @Column(name = "delivery_payment_amount", nullable = false)
    @Builder.Default
    private Long deliveryPaymentAmount = 0L;

    @Column(name = "delivery_payment_app_trans_id", length = 100)
    private String deliveryPaymentAppTransId;

    @Column(name = "delivery_payment_confirmed_at")
    private LocalDateTime deliveryPaymentConfirmedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "delivery_note")
    private String deliveryNote;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
}
