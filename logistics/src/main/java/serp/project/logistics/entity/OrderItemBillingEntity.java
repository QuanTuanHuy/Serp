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

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_order_item_billing")
public class OrderItemBillingEntity {

    @Id
    private String id;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    private long amount;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "order_item_id")
    private String orderItemId;

    @Column(name = "invoice_item_id")
    private String invoiceItemId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "inventory_item_detail_id")
    private String inventoryItemDetailId;

    @Column(name = "facility_id")
    private String facilityId;

    private String unit;

    @Column(name = "order_item_billing_type_id")
    private String orderItemBillingTypeId;

    @Column(name = "tenant_id")
    private Long tenantId;

}
