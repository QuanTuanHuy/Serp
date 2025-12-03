package serp.project.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_delivery_bill")
public class DeliveryBillEntity {

    @Id
    private String id;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "total_weight")
    private float totalWeight;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    private String note;

    @Column(name = "status_id")
    private String statusId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    private int priority;

    @Column(name = "sequence_id")
    private String sequenceId;

    @Column(name = "delivery_bill_name")
    private String deliveryBillName;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "facility_id")
    private String facilityId;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "delivery_status_id")
    private String deliveryStatusId;

    @Column(name = "tenant_id")
    private Long tenantId;

}
