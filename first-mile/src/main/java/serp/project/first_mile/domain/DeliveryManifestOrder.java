/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.PaymentStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "delivery_manifest_orders")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryManifestOrder extends AbstractAudit implements DeliveryStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    private DeliveryManifest manifest;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code", nullable = false, length = 100)
    private String orderCode;

    @Column(name = "sequence", nullable = false)
    @Builder.Default
    private Integer sequence = 0;

    @Column(name = "delivery_attempt_count", nullable = false)
    @Builder.Default
    private Integer deliveryAttemptCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private DeliveryOrderStatus status = DeliveryOrderStatus.PENDING;

    // Cached receiver info
    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone", length = 50)
    private String receiverPhone;

    @Column(name = "receiver_address_detail")
    private String receiverAddressDetail;

    @Column(name = "receiver_ward_code", length = 50)
    private String receiverWardCode;

    @Column(name = "receiver_province_code", length = 50)
    private String receiverProvinceCode;

    @Column(name = "receiver_lat")
    private Double receiverLat;

    @Column(name = "receiver_lng")
    private Double receiverLng;

    // Financial
    @Column(name = "cod_amount", nullable = false)
    @Builder.Default
    private Long codAmount = 0L;

    @Column(name = "cod_collected", nullable = false)
    @Builder.Default
    private Long codCollected = 0L;

    @Column(name = "shipping_fee", nullable = false)
    @Builder.Default
    private Long shippingFee = 0L;

    @Column(name = "shipping_fee_collected", nullable = false)
    @Builder.Default
    private Long shippingFeeCollected = 0L;

    @Column(name = "fee_payer", length = 20)
    private String feePayer;

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

    // Result
    @Column(name = "proof_photo_url")
    private String proofPhotoUrl;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_checkin_lat")
    private Double deliveryCheckinLat;

    @Column(name = "delivery_checkin_lng")
    private Double deliveryCheckinLng;

    @Column(name = "delivery_checkin_distance_m")
    private Double deliveryCheckinDistanceM;

    @Column(name = "note")
    private String note;

    @Override
    public double getLat() {
        return receiverLat != null ? receiverLat : 0.0;
    }

    @Override
    public double getLng() {
        return receiverLng != null ? receiverLng : 0.0;
    }
}
