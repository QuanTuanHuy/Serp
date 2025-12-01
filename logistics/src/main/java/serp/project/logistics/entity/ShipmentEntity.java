package serp.project.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phieu nhap / xuat hang
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_shipment")
public class ShipmentEntity {

    @Id
    private String id;

    @Column(name = "shipment_type_id")
    private String shipmentTypeId;

    @Column(name = "from_supplier_id")
    private String fromSupplierId;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "order_id")
    private String orderId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "shipment_name")
    private String shipmentName;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "handled_by_user_id")
    private Long handledByUserId;

    private String note;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "user_cancelled_id")
    private Long userCancelledId;

    @Column(name = "total_weight")
    private long totalWeight;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "tenant_id")
    private Long tenantId;

}
