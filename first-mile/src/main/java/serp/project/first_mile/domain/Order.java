package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AbstractAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "customer_order_code")
    private String customerOrderCode;

    @Column(name = "sender_name")
    private String senderName;
    @Column(name = "sender_phone")
    private String senderPhone;
    @Column(name = "sender_ward_code")
    private String senderWardCode;
    @Column(name = "sender_province_code")
    private String senderProvinceCode;
    @Column(name = "sender_address_detail")
    private String senderAddressDetail;
    @Column(name = "sender_location", columnDefinition = "geography(Point, 4326)")
    private Point senderLocation;

    // Khung giờ yêu cầu lấy hàng
    @Column(name = "pickup_time_start")
    private LocalDateTime pickupTimeStart;
    @Column(name = "pickup_time_end")
    private LocalDateTime pickupTimeEnd;

    // Khung giờ yêu cầu giao hàng
    @Column(name = "delivery_request_time")
    @Enumerated(EnumType.STRING)
    private DeliveryRequestTime deliveryRequestTime;

    @Column(name = "receiver_name")
    private String receiverName;
    @Column(name = "receiver_phone")
    private String receiverPhone;
    @Column(name = "receiver_ward_code")
    private String receiverWardCode;
    @Column(name = "receiver_province_code")
    private String receiverProvinceCode;
    @Column(name = "receiver_address_detail")
    private String receiverAddressDetail;
    @Column(name = "receiver_location", columnDefinition = "geography(Point, 4326)")
    private Point receiverLocation;

    // ĐỊNH TUYẾN BƯU CỤC
    @Column(name = "origin_post_office_code")
    private String originPostOfficeCode; // Bưu cục nhận hàng ở First-mile

    @Column(name = "destination_post_office_code")
    private String destinationPostOfficeCode; // Bưu cục sẽ đi giao ở Last-mile

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "is_confirm")
    @Builder.Default
    private Boolean isConfirm = false;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "total_value")
    private Double totalValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions", columnDefinition = "jsonb")
    private Dimension dimensions;

    @Column(name = "total_volume")
    private Double totalVolume;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @Column(name = "pickup_attempts")
    @Builder.Default
    private Integer pickupAttempts = 0;

    @Column(name = "order_product_category")
    @Enumerated(EnumType.STRING)
    private OrderProductCategory orderProductCategory;

    @Column(name = "order_type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "base_shipping_fee")
    private Long baseShippingFee;

    @Column(name = "cod_fee")
    private Long codFee;

    @Column(name = "extra_fee")
    private Long extraFee;

    @Column(name = "total_shipping_fee")
    private Long totalShippingFee;

    @Column(name = "cod_amount")
    private Long codAmount;

    @Column(name = "fee_payer")
    @Enumerated(EnumType.STRING)
    private FeePayer feePayer;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "note")
    private String note;

    public void addProduct(Product product) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);
        product.setOrder(this);
    }

    public void removeProduct(Product product) {
        if (products == null) {
            return;
        }
        products.remove(product);
        product.setOrder(null);
    }
}
