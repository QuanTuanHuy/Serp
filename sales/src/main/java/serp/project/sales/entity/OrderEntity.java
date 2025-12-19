package serp.project.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_order_header")
@Slf4j
public class OrderEntity {

    @Id
    private String id;

    @Column(name = "order_type_id")
    private String orderTypeId;

    @Column(name = "from_supplier_id")
    private String fromSupplierId;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "status_id")
    private String statusId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "delivery_before_date")
    private LocalDate deliveryBeforeDate;

    @Column(name = "delivery_after_date")
    private LocalDate deliveryAfterDate;

    private String note;

    @Column(name = "order_name")
    private String orderName;

    private int priority;

    @Column(name = "delivery_address_id")
    private String deliveryAddressId;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "sale_channel_id")
    private String saleChannelId;

    @Column(name = "delivery_full_address")
    private String deliveryFullAddress;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "total_amount")
    private Long totalAmount;

    private String costs;

    @Column(name = "user_approved_id")
    private Long userApprovedId;

    @Column(name = "user_cancelled_id")
    private Long userCancelledId;

    @Column(name = "cancellation_note")
    private String cancellationNote;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private List<OrderItemEntity> items = new ArrayList<>();


}
