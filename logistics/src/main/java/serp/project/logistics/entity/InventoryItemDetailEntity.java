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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_inventory_item_detail")
public class InventoryItemDetailEntity {

    @Id
    private String id;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "order_item_id")
    private String orderItemId;

    private String note;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "lot_id")
    private String lotId;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "facility_id")
    private String facilityId;

    private String unit;

    private long price;

    @Column(name = "tenant_id")
    private Long tenantId;

}
